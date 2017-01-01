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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcById;
import org.easotope.shared.admin.tables.RefGas;
import org.easotope.shared.analysis.events.CorrIntervalsUpdated;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class CorrIntervalUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private CorrIntervalV1 corrInterval = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		return permissions.isCanEditCorrIntervals();
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<CorrIntervalV1,Integer> corrIntervalDao = DaoManager.createDao(connectionSource, CorrIntervalV1.class);
		List<CorrIntervalV1> allCorrIntervalsForMassSpec = corrIntervalDao.queryForEq(RefGas.MASSSPECID_FIELD_NAME, corrInterval.getMassSpecId());

		// check that this date doesn't already exist

		for (CorrIntervalV1 thisCorrInterval : allCorrIntervalsForMassSpec) {
			if (thisCorrInterval.getValidFrom() == corrInterval.getValidFrom() && thisCorrInterval.getId() != corrInterval.getId()) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.corrIntervalUpdate_corrIntervalValidFromAlreadyExists);
				return;
			}
		}

		// save corr interval
 
		CorrIntervalV1 oldCorrInterval = null;

		if (corrInterval.getId() == DatabaseConstants.EMPTY_DB_ID) {
			corrIntervalDao.create(corrInterval);

		} else {
			oldCorrInterval = corrIntervalDao.queryForId(corrInterval.getId());

			if (oldCorrInterval == null) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.corrIntervalUpdate_corrIntervalDoesNotExist, new Object[] { corrInterval.getId() } );
				return;
			}

			corrIntervalDao.update(corrInterval);
		}

		// update the "valid until" date on all other corr intervals and generate events

		CorrIntervalsUpdated corrIntervalsUpdatedEvent = new CorrIntervalsUpdated(corrInterval.getMassSpecId());
		CorrIntervalsNeedRecalcById corrIntervalsNeedRecalcById = new CorrIntervalsNeedRecalcById();
		boolean changedCorrIntervalHasBeenAdded = false;

		allCorrIntervalsForMassSpec = corrIntervalDao.queryForEq(RefGas.MASSSPECID_FIELD_NAME, corrInterval.getMassSpecId());
		Collections.sort(allCorrIntervalsForMassSpec, corrIntervalComparator);

		Iterator<CorrIntervalV1> iter = allCorrIntervalsForMassSpec.iterator();
		CorrIntervalV1 previousCorrInterval = null;
		CorrIntervalV1 thisCorrInterval = null;
		CorrIntervalV1 nextCorrInterval = iter.next();

		CorrIntervalV1 precedingCorrInterval = null;

		while (iter.hasNext()) {
			previousCorrInterval = thisCorrInterval;
			thisCorrInterval = nextCorrInterval;
			nextCorrInterval = iter.next();

			if (thisCorrInterval.getId() == corrInterval.getId()) {
				precedingCorrInterval = previousCorrInterval;
			}

			if (thisCorrInterval.getValidUntil() != nextCorrInterval.getValidFrom() - 1) {
				thisCorrInterval.setValidUntil(nextCorrInterval.getValidFrom() - 1);
				corrIntervalDao.update(thisCorrInterval);

				corrIntervalsUpdatedEvent.addCorrInterval(thisCorrInterval);
				corrIntervalsNeedRecalcById.addCorrIntervalId(thisCorrInterval.getId());

				if (thisCorrInterval.getId() == corrInterval.getId()) {
					changedCorrIntervalHasBeenAdded = true;
					corrInterval = thisCorrInterval;
				}
			}
		}

		previousCorrInterval = thisCorrInterval;
		thisCorrInterval = nextCorrInterval;
		nextCorrInterval = null;

		if (thisCorrInterval.getId() == corrInterval.getId()) {
			precedingCorrInterval = previousCorrInterval;
		}

		if (thisCorrInterval.getValidUntil() != DatabaseConstants.MAX_DATE) {
			thisCorrInterval.setValidUntil(DatabaseConstants.MAX_DATE);
			corrIntervalDao.update(thisCorrInterval);

			corrIntervalsUpdatedEvent.addCorrInterval(thisCorrInterval);
			corrIntervalsNeedRecalcById.addCorrIntervalId(thisCorrInterval.getId());

			if (thisCorrInterval.getId() == corrInterval.getId()) {
				changedCorrIntervalHasBeenAdded = true;
				corrInterval = thisCorrInterval;
			}
		}

		if (!changedCorrIntervalHasBeenAdded) {
			corrIntervalsUpdatedEvent.addCorrInterval(corrInterval);
			corrIntervalsNeedRecalcById.addCorrIntervalId(corrInterval.getId());
		}

		addEvent(corrIntervalsUpdatedEvent);
		addEvent(corrIntervalsNeedRecalcById);

		// remove any unnecessary step parameters

		Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);

		if (oldCorrInterval != null) {
			HashSet<Integer> oldIds = new HashSet<Integer>();

			for (int dataAnalysisId : oldCorrInterval.getDataAnalysis()) {
				oldIds.add(dataAnalysisId);
			}

			for (int dataAnalysisId : corrInterval.getDataAnalysis()) {
				oldIds.remove(dataAnalysisId);
			}

			for (Integer id : oldIds) {
				HashMap<String,Object> fieldValues = new HashMap<String,Object>();
				fieldValues.put(RepStepParams.ANALYSIS_ID_FIELD_NAME, id);
				fieldValues.put(RepStepParams.CORR_INTERVAL_ID_FIELD_NAME, corrInterval.getId());

				repStepParamsDao.delete(repStepParamsDao.queryForFieldValues(fieldValues));
			}
		}

		// add any new step parameters

		HashSet<Integer> newIds = new HashSet<Integer>();

		for (int dataAnalysisId : corrInterval.getDataAnalysis()) {
			newIds.add(dataAnalysisId);
		}

		if (oldCorrInterval != null) {
			for (int dataAnalysisId : oldCorrInterval.getDataAnalysis()) {
				newIds.remove(dataAnalysisId);
			}
		}
		
		for (Integer id : newIds) {
			if (precedingCorrInterval != null && arrayContainsInteger(precedingCorrInterval.getDataAnalysis(), id)) {
				// copy the preceding step parameters

				HashMap<String,Object> fieldValues = new HashMap<String,Object>();
				fieldValues.put(RepStepParams.ANALYSIS_ID_FIELD_NAME, id);
				fieldValues.put(RepStepParams.CORR_INTERVAL_ID_FIELD_NAME, precedingCorrInterval.getId());

				for (RepStepParams oldRepStepParameters : repStepParamsDao.queryForFieldValues(fieldValues)) {
					RepStepParams newRepStepParams = new RepStepParams(oldRepStepParameters);
					newRepStepParams.setId(DatabaseConstants.EMPTY_DB_ID);
					newRepStepParams.setCorrIntervalId(corrInterval.getId());

					repStepParamsDao.create(newRepStepParams);
				}
			}
		}
	}

	private boolean arrayContainsInteger(int[] dataAnalysis, Integer integer) {
		if (dataAnalysis == null) {
			return false;
		}

		for (int i : dataAnalysis) {
			if (i == integer) {
				return true;
			}
		}

		return false;
	}

	public void setCorrInterval(CorrIntervalV1 corrInterval) {
		this.corrInterval = corrInterval;
	}

	public CorrIntervalV1 getCorrInterval() {
		return corrInterval;
	}

	public static Comparator<CorrIntervalV1> corrIntervalComparator = new Comparator<CorrIntervalV1>() {
		public int compare(CorrIntervalV1 corrInterval1, CorrIntervalV1 corrInterval2) {
			Long startDate1 = corrInterval1.getValidFrom();
			Long startDate2 = corrInterval2.getValidFrom();

			return startDate1.compareTo(startDate2);
		}
	};
}
