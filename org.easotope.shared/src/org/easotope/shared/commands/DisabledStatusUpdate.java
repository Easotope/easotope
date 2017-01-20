/*
 * Copyright © 2016-2017 by Devon Bowen.
 *
 * This file is part of Easotope.
 *
 * Easotope is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify this Program, or any covered work, by linking or combining
 * it with the Eclipse Rich Client Platform (or a modified version of that
 * library), containing parts covered by the terms of the Eclipse Public
 * License, the licensors of this Program grant you additional permission
 * to convey the resulting work. Corresponding Source for a non-source form
 * of such a combination shall include the source code for the parts of the
 * Eclipse Rich Client Platform used as well as that of the covered work.
 *
 * Easotope is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Easotope. If not, see <http://www.gnu.org/licenses/>.
 */

package org.easotope.shared.commands;

import java.util.Hashtable;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcByTime;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.events.ReplicateUpdated;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class DisabledStatusUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private int replicateId;
	private boolean disable;

	private transient ReplicateV1 replicate;
	private transient Sample sample;

	public DisabledStatusUpdate(int replicateId, boolean disable) {
		this.replicateId = replicateId;
		this.disable = disable;
	}

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
		replicate = replicateDao.queryForId(replicateId);

		if (replicate == null) {
			return true;
		}

		if (replicate.getSampleId() != DatabaseConstants.EMPTY_DB_ID) {
			Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
			sample = sampleDao.queryForId(replicate.getSampleId());

			if (sample == null) {
				return true;
			}
		}

		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		return replicate.getSampleId() == DatabaseConstants.EMPTY_DB_ID || user.id == sample.getUserId() || permissions.isCanEditAllReplicates();
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		if (replicate == null) {
			setStatus(Command.Status.EXECUTION_ERROR, Messages.disabledStatusUpdate_noSuchReplicate, new Object[] { replicate.getId() } );
			return;
		}

		if (replicate.getSampleId() != DatabaseConstants.EMPTY_DB_ID && sample == null) {
			setStatus(Command.Status.EXECUTION_ERROR, Messages.disabledStatusUpdate_noSampleForReplicate, new Object[] { replicate.getSampleId(), replicate.getId() } );
			return;
		}

		if (replicate.isDisabled() == disable) {
			setStatus(Command.Status.EXECUTION_ERROR, Messages.disabledStatusUpdate_replicateStatusUnchanged, new Object[] { disable ? Messages.disabledStatusUpdate_disabled : Messages.disabledStatusUpdate_enabled } );
			return;
		}

		if (disable == false) {
			Dao<AcquisitionInputV0,Integer> acqusitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInputV0.class);
			boolean atLeastOneAcquisitionEnabled = false;

			for (AcquisitionInputV0 acquisitionInput : acqusitionInputDao.queryForEq(AcquisitionInputV0.REPLICATEID_FIELD_NAME, replicate.getId())) {
				if (!acquisitionInput.isDisabled()) {
					atLeastOneAcquisitionEnabled = true;
				}
			}

			if (!atLeastOneAcquisitionEnabled) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.disabledStatusUpdate_noEnabledAcquisitions);
				return;
			}
		}

		Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
		replicate.setDisabled(disable);
		replicateDao.update(replicate);

		if (replicate.getStandardId() != DatabaseConstants.EMPTY_DB_ID) {
			CorrIntervalsNeedRecalcByTime corrIntervalsNeedRecalc = new CorrIntervalsNeedRecalcByTime();
			corrIntervalsNeedRecalc.addTime(replicate.getMassSpecId(), replicate.getDate());
			addEvent(corrIntervalsNeedRecalc);
		}

		ReplicateUpdated replicateUpdated = new ReplicateUpdated(replicate);
		
		if (sample != null) {
			replicateUpdated.setSampleId(sample.getId());
			replicateUpdated.setSampleName(sample.getName());
		}
		
		addEvent(replicateUpdated);
	}
}
