/*
 * Copyright © 2016-2023 by Devon Bowen.
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

package org.easotope.shared.plugin.analysis.databaseupgradehandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.tables.Options;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class Upgrade20201231 extends DatabaseUpgrade {
	@Override
	public int appliesToVersion() {
		return 20201231;
	}

	@Override
	public int resultsInVersion() {
		return 20230220;
	}

	@Override
	public boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource, int originalServerVersion) {
		try {
			Dao<Options,Integer> optionsDao = DaoManager.createDao(connectionSource, Options.class);
			optionsDao.executeRaw("ALTER TABLE " + AcquisitionParsedV2.TABLE_NAME + " ADD COLUMN " + AcquisitionParsedV2.DATA_FORMAT_FIELD_NAME + " VARCHAR DEFAULT 'DUALINLET' NOT NULL;");

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20201231.class, "Error while adding DATAFORMAT to AcquisitionParsedV2 table.", e);
			return false;
		}

		// remove all CO2Calc steps from clumped analyses

		if (!removeCO2CalcPosition(connectionSource, 1, 1))		// Co2Clumped
			return false;

		if (!removeCO2CalcPosition(connectionSource, 2, 2))		// Co2ClumpedIclPbl
			return false;
		
		if (!removeCO2CalcPosition(connectionSource, 3, 2))		// Co2ClumpedEthPbl
			return false;
		
		if (!removeCO2CalcPosition(connectionSource, 5, 1))		// Co2ClumpedD48
			return false;
		
		if (!removeCO2CalcPosition(connectionSource, 6, 2))		// Co2ClumpedIclPblD48
			return false;
		
		if (!removeCO2CalcPosition(connectionSource, 7, 2))		// Co2ClumpedEthPblD48
			return false;

		// insert CO2{Bulk,Clump}RefCalc before CO2Calc and ClumpCalc steps

		if (!insertCO2RefCalc(connectionSource, 1, 1))		// Co2Clumped
			return false;
		
		if (!insertCO2RefCalc(connectionSource, 2, 2))		// Co2ClumpedIclPbl
			return false;
		
		if (!insertCO2RefCalc(connectionSource, 3, 2))		// Co2ClumpedEthPbl
			return false;
		
		if (!insertCO2RefCalc(connectionSource, 4, 1))		// Co2Bulk
			return false;
		
		if (!insertCO2RefCalc(connectionSource, 5, 1))		// Co2ClumpedD48
			return false;
		
		if (!insertCO2RefCalc(connectionSource, 6, 2))		// Co2ClumpedIclPblD48
			return false;
		
		if (!insertCO2RefCalc(connectionSource, 7, 2))		// Co2ClumpedEthPblD48
			return false;

		try {
			Dao<Preferences,Integer> preferencesDao = DaoManager.createDao(connectionSource, Preferences.class);
			preferencesDao.executeRaw("ALTER TABLE " + Preferences.TABLE_NAME + " ADD COLUMN " + Preferences.LIDI2REFRANGE + " INTEGER DEFAULT 10 NOT NULL;");
			preferencesDao.executeRaw("ALTER TABLE " + Preferences.TABLE_NAME + " ADD COLUMN " + Preferences.EXPORTPADDING + " INTEGER DEFAULT 0 NOT NULL;");

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20201231.class, "Error while adding fields to Preferences table.", e);
			return false;
		}

		rebuildAnalyses = true;
		return true;
	}

	private boolean removeCO2CalcPosition(ConnectionSource connectionSource, int analysisId, int position) {
		try {
			Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);

			// merge PARAMETER_ALLOW_UNAVERAGED_REF_VALUES step params

			@SuppressWarnings("serial")
			Map<String,Object> queryStepParams1 = new HashMap<String, Object>() {{
				put(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
				put(RepStepParams.POSITION_FIELD_NAME, position);
			}};

			List<RepStepParams> list = repStepParamsDao.queryForFieldValues(queryStepParams1);

			for (RepStepParams stepParams1 : list) {
				Boolean value = (Boolean) stepParams1.getParameters().get(org.easotope.shared.analysis.repstep.co2.co2calc.Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES);

				@SuppressWarnings("serial")
				Map<String,Object> queryStepParams2 = new HashMap<String, Object>() {{
					put(RepStepParams.CORR_INTERVAL_ID_FIELD_NAME, stepParams1.getCorrIntervalId());
					put(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
					put(RepStepParams.POSITION_FIELD_NAME, position+1);
				}};

				List<RepStepParams> list2 = repStepParamsDao.queryForFieldValues(queryStepParams2);

				for (RepStepParams stepParams2 : list2) {
					Boolean newValue = (Boolean) stepParams2.getParameters().get(org.easotope.shared.analysis.repstep.co2.co2calc.Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES);

					if (newValue != null && newValue == true) {
						value = true;
					}

					repStepParamsDao.deleteById(stepParams2.getId());
				}

				if (value != null) {
					stepParams1.getParameters().put(org.easotope.shared.analysis.repstep.co2.co2calc.Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES, value);
					repStepParamsDao.update(stepParams1);
				}
			}

			// shift parameters down one step
			
			QueryBuilder<RepStepParams,Integer> queryBuilder = repStepParamsDao.queryBuilder();
			Where<RepStepParams,Integer> where = queryBuilder.where();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
			where = where.and();
			where = where.gt(RepStepParams.POSITION_FIELD_NAME, position+1);
			PreparedQuery<RepStepParams> preparedQuery = queryBuilder.prepare();

			for (RepStepParams repStepParams : repStepParamsDao.query(preparedQuery)) {
				repStepParams.setPosition(repStepParams.getPosition()-1);
				repStepParamsDao.update(repStepParams);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20201231.class, "Error while removing CO2Calc from analysis " + analysisId, e);
			return false;
		}

		return true;
	}

	private boolean insertCO2RefCalc(ConnectionSource connectionSource, int analysisId, int position) {
		try {
			Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);

			// shift parameters up one step

			QueryBuilder<RepStepParams,Integer> queryBuilder = repStepParamsDao.queryBuilder();
			Where<RepStepParams,Integer> where = queryBuilder.where();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
			where = where.and();
			where = where.ge(RepStepParams.POSITION_FIELD_NAME, position);
			PreparedQuery<RepStepParams> preparedQuery = queryBuilder.prepare();

			for (RepStepParams repStepParams : repStepParamsDao.query(preparedQuery)) {
				repStepParams.setPosition(repStepParams.getPosition() + 1);
				repStepParamsDao.update(repStepParams);
			}

			// move PARAMETER_ALLOW_UNAVERAGED_REF_VALUES step param from next step to this one

			@SuppressWarnings("serial")
			Map<String,Object> queryStepParams1 = new HashMap<String, Object>() {{
				put(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
				put(RepStepParams.POSITION_FIELD_NAME, position+1);
			}};

			List<RepStepParams> list = repStepParamsDao.queryForFieldValues(queryStepParams1);

			for (RepStepParams stepParams1 : list) {
				if (stepParams1.getParameters().containsKey(org.easotope.shared.analysis.repstep.co2.co2calc.Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES)) {
					Boolean value = (Boolean) stepParams1.getParameters().get(org.easotope.shared.analysis.repstep.co2.co2calc.Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES);

					if (value != null) {
						RepStepParams newStepParams = new RepStepParams();
						newStepParams.setAnalysisId(analysisId);
						newStepParams.setPosition(position);
						HashMap<String, Object> newHashMap = new HashMap<String, Object>();
						newHashMap.put(org.easotope.shared.analysis.repstep.co2.co2calc.Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES, value);
						newStepParams.setParameters(newHashMap);
						newStepParams.setCorrIntervalId(stepParams1.getCorrIntervalId());

						repStepParamsDao.create(newStepParams);
					}

					stepParams1.getParameters().remove(org.easotope.shared.analysis.repstep.co2.co2calc.Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES);
					repStepParamsDao.update(stepParams1);
				}
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20201231.class, "Error while inserting CO2RefCalc into analysis " + analysisId, e);
			return false;
		}
		
		return true;
	}
}
