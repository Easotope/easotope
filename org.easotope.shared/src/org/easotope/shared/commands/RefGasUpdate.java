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

import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcByTime;
import org.easotope.shared.admin.events.RefGassesUpdated;
import org.easotope.shared.admin.tables.RefGas;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class RefGasUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private RefGas refGas = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		return permissions.isCanEditMassSpecs();
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<RefGas,Integer> refGasDao = DaoManager.createDao(connectionSource, RefGas.class);
		List<RefGas> allRefGassesForMassSpec = refGasDao.queryForEq(RefGas.MASSSPECID_FIELD_NAME, refGas.getMassSpecId());

		for (RefGas thisRefGas : allRefGassesForMassSpec) {
			if (thisRefGas.getValidFrom() == refGas.getValidFrom() && thisRefGas.getMassSpecId() == refGas.getMassSpecId() && thisRefGas.getId() != refGas.getId()) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.refGasUpdate_refGasValidFromAlreadyExists);
				return;
			}
		}

		if (refGas.getId() == DatabaseConstants.EMPTY_DB_ID) {
			refGasDao.create(refGas);

		} else {
			if (refGasDao.queryForId(refGas.getId()) == null) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.refGasUpdate_refGasDoesNotExist, new Object[] { refGas.getId() } );
				return;
			}

			refGasDao.update(refGas);
		}

		RefGassesUpdated refGassesUpdatedEvent = new RefGassesUpdated(refGas.getMassSpecId());
		long fromTime = Long.MAX_VALUE;
		long untilTime = Long.MIN_VALUE;
		boolean changedRefGasHasBeenAdded = false;

		allRefGassesForMassSpec = refGasDao.queryForEq(RefGas.MASSSPECID_FIELD_NAME, refGas.getMassSpecId());
		Collections.sort(allRefGassesForMassSpec, refGasComparator);

		for (int i=0; i<allRefGassesForMassSpec.size()-1; i++) {
			RefGas thisRefGas = allRefGassesForMassSpec.get(i);
			RefGas nextRefGas = allRefGassesForMassSpec.get(i+1);

			if (thisRefGas.getValidUntil() != nextRefGas.getValidFrom() - 1) {
				fromTime = Math.min(fromTime, thisRefGas.getValidFrom());
				untilTime = Math.max(untilTime, thisRefGas.getValidUntil());

				thisRefGas.setValidUntil(nextRefGas.getValidFrom() - 1);
				untilTime = Math.max(untilTime, thisRefGas.getValidUntil());

				refGasDao.update(thisRefGas);

				if (thisRefGas.getId() == refGas.getId()) {
					changedRefGasHasBeenAdded = true;
					refGas = thisRefGas;
				}

				refGassesUpdatedEvent.addRefGas(thisRefGas);
			}
		}

		RefGas thisRefGas = allRefGassesForMassSpec.get(allRefGassesForMassSpec.size() - 1);

		if (thisRefGas.getValidUntil() != DatabaseConstants.MAX_DATE) {
			fromTime = Math.min(fromTime, thisRefGas.getValidFrom());
			untilTime = Math.max(untilTime, thisRefGas.getValidUntil());

			thisRefGas.setValidUntil(DatabaseConstants.MAX_DATE);
			untilTime = Math.max(untilTime, thisRefGas.getValidUntil());

			refGasDao.update(thisRefGas);

			if (thisRefGas.getId() == refGas.getId()) {
				changedRefGasHasBeenAdded = true;
				refGas = thisRefGas;
			}

			refGassesUpdatedEvent.addRefGas(thisRefGas);
		}

		if (!changedRefGasHasBeenAdded) {
			fromTime = Math.min(fromTime, refGas.getValidFrom());
			untilTime = Math.max(untilTime, refGas.getValidUntil());

			refGassesUpdatedEvent.addRefGas(refGas);
		}

		addEvent(refGassesUpdatedEvent);

		if (fromTime != Long.MAX_VALUE && untilTime != Long.MIN_VALUE) {
			CorrIntervalsNeedRecalcByTime corrIntervalsNeedRecalc = new CorrIntervalsNeedRecalcByTime();
			corrIntervalsNeedRecalc.addFromToRange(refGas.getMassSpecId(), fromTime, untilTime);
			addEvent(corrIntervalsNeedRecalc);
		}
	}

	public void setRefGas(RefGas refGas) {
		this.refGas = refGas;
	}

	public RefGas getRefGas() {
		return refGas;
	}

	public static Comparator<RefGas> refGasComparator = new Comparator<RefGas>() {
		public int compare(RefGas refGas1, RefGas refGas2) {
			Long startDate1 = refGas1.getValidFrom();
			Long startDate2 = refGas2.getValidFrom();

			return startDate1.compareTo(startDate2);
		}
	};
}
