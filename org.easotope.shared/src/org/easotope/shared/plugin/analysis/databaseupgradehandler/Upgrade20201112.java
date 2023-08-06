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

import java.sql.SQLException;
import java.util.HashMap;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.analysis.tables.RepAnalysisChoice;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.tables.TableLayout;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class Upgrade20201112 extends DatabaseUpgrade {
	@Override
	public int appliesToVersion() {
		return 20201112;
	}

	@Override
	public int resultsInVersion() {
		return 20201231;
	}

	@Override
	public boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource, int originalServerVersion) {
		try {
			Dao<AcquisitionInputV0,Integer> acquisitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInputV0.class);
			acquisitionInputDao.executeRaw("CREATE INDEX PUBLIC.ACQUISITIONINPUT_V0_REPLICATEID_IDX ON PUBLIC.ACQUISITIONINPUT_V0(REPLICATEID)");

		} catch (SQLException e) {
			Log.getInstance().log(Level.INFO, Upgrade20201112.class, "Error while adding REPLICATEID index on ACQUISITIONINPUT table.", e);
			return false;
		}

		if (originalServerVersion > 20190125) {
			try {
				Dao<CorrIntervalV1,Integer> corrIntervalV1Dao = DaoManager.createDao(connectionSource, CorrIntervalV1.class);
	
				for (CorrIntervalV1 corrIntervalV1 : corrIntervalV1Dao.queryForAll()) {
					int[] array = corrIntervalV1.getDataAnalysis();
	
					if (array != null) {
						boolean update = false;
	
						for (int i=0; i < array.length; i++) {
							if (array[i] == 1 || array[i] == 1 || array[i] == 3) {
								array[i] += 4;
								update = true;
							}
						}
	
						if (update) {
							corrIntervalV1Dao.update(corrIntervalV1);
						}
					}
				}
	
			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, Upgrade20201112.class, "Error while modifying DATAANALYSIS on CorrIntervalV1 table", e);
				return false;
			}
	
			try {
				Dao<RepAnalysisChoice,Integer> repAnalysisChoiceDao = DaoManager.createDao(connectionSource, RepAnalysisChoice.class);
	
				for (RepAnalysisChoice repAnalysisChoice : repAnalysisChoiceDao.queryForAll()) {
					HashMap<Integer, Integer> map = repAnalysisChoice.getRepIdsToRepAnalysisChoice();
	
					if (map != null) {
						boolean update = false;
						Integer[] keyset = map.keySet().toArray(new Integer[map.keySet().size()]);
	
						for (Integer key : keyset) {
							Integer value = map.get(key);
	
							if (value == 1 || value == 2 || value == 3) {
								map.put(key, value + 4);
								update = true;
							}
						}
	
						if (update) {
							repAnalysisChoiceDao.update(repAnalysisChoice);
						}
					}
				}
	
			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, Upgrade20201112.class, "Error while modifying REPANALYSISCHOICE on REPANALYSISCHOICE_V0 table", e);
				return false;
			}

			try {
				Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);
				repStepParamsDao.executeRaw("UPDATE REPSTEPPARAMS_V0 SET ANALYSISID = 5 WHERE ANALYSISID = 1");
				repStepParamsDao.executeRaw("UPDATE REPSTEPPARAMS_V0 SET ANALYSISID = 6 WHERE ANALYSISID = 2");
				repStepParamsDao.executeRaw("UPDATE REPSTEPPARAMS_V0 SET ANALYSISID = 7 WHERE ANALYSISID = 3");

			} catch (SQLException e) {
				Log.getInstance().log(Level.INFO, Upgrade20201112.class, "Error while modifying ANALYSISID on REPSTEPPARAMS_V0 table.", e);
				return false;
			}

			try {
				Dao<TableLayout,Integer> tableLayoutDao = DaoManager.createDao(connectionSource, TableLayout.class);
				tableLayoutDao.executeRaw("UPDATE TABLELAYOUT_V0 SET DATAANALYSISID = 5 WHERE DATAANALYSISID = 1 AND CONTEXT = 'INPUT RESULTS'");
				tableLayoutDao.executeRaw("UPDATE TABLELAYOUT_V0 SET DATAANALYSISID = 6 WHERE DATAANALYSISID = 2 AND CONTEXT = 'INPUT RESULTS'");
				tableLayoutDao.executeRaw("UPDATE TABLELAYOUT_V0 SET DATAANALYSISID = 7 WHERE DATAANALYSISID = 3 AND CONTEXT = 'INPUT RESULTS'");
	
			} catch (SQLException e) {
				Log.getInstance().log(Level.INFO, Upgrade20201112.class, "Error while modifying DATAANALYSISID on TABLELAYOUT_V0 table.", e);
				return false;
			}
		}

		rebuildAnalyses = true;
		return true;
	}
}
