/*
 * Copyright © 2016-2018 by Devon Bowen.
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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.admin.tables.SampleType;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class ReplicateGet extends Command {
	private static final long serialVersionUID = 1L;

	private int replicateId;
	private ReplicateV1 replicate;
	private ArrayList<Acquisition> acquisitions;
	private Integer projectId;
	private SampleType sampleType;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
		replicate = replicateDao.queryForId(replicateId);

		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);

		return replicate.getStandardId() != DatabaseConstants.EMPTY_DB_ID || replicate.getUserId() == user.getId() || permissions.isCanEditAllReplicates();
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
		replicate = replicateDao.queryForId(replicateId);

		Dao<AcquisitionInputV0,Integer> acquisitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInputV0.class);
		List<AcquisitionInputV0> acquisitionInputs = acquisitionInputDao.queryForEq(AcquisitionInputV0.REPLICATEID_FIELD_NAME, replicateId);

		acquisitions = new ArrayList<Acquisition>();
		Dao<AcquisitionParsedV2,Integer> acquisitionParsedDao = DaoManager.createDao(connectionSource, AcquisitionParsedV2.class);

		for (AcquisitionInputV0 acquisitionInput : acquisitionInputs) {
			if (acquisitionInput.getOffPeakCycles() == null) {
				acquisitionInput.setOffPeakCycles(new boolean[acquisitionInput.getDisabledCycles().length]);
			}
			AcquisitionParsedV2 acquisitionParsed = acquisitionParsedDao.queryForId(acquisitionInput.getAcquisitionParsedId());
			acquisitions.add(new Acquisition(null, null, acquisitionParsed, acquisitionInput));
		}

		if (replicate.getSampleId() != DatabaseConstants.EMPTY_DB_ID) {
			Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
			Sample sample = sampleDao.queryForId(replicate.getSampleId());

			projectId = sample.getProjectId();

			Dao<SampleType,Integer> sampleTypeDao = DaoManager.createDao(connectionSource, SampleType.class);
			sampleType = sampleTypeDao.queryForId(sample.getSampleTypeId());
		}
	}

	public int getReplicateId() {
		return replicateId;
	}

	public void setReplicateId(int replicateId) {
		this.replicateId = replicateId;
	}

	public ReplicateV1 getReplicate() {
		return replicate;
	}

	public ArrayList<Acquisition> getAcquisitions() {
		return acquisitions;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public SampleType getSampleType() {
		return sampleType;
	}
}
