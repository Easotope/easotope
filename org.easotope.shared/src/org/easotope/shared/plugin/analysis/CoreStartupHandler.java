/*
 * Copyright © 2016 by Devon Bowen.
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

package org.easotope.shared.plugin.analysis;

import java.util.ArrayList;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.events.CoreStartup;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.analysis.server.LoadOrCalculateCorrInterval;
import org.easotope.shared.analysis.tables.CalcRepToCalcSamp;
import org.easotope.shared.analysis.tables.CalcReplicateCache;
import org.easotope.shared.analysis.tables.CalcSampleCache;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.CorrIntervalScratchPad;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.plugin.analysis.databaseupgradehandler.DatabaseUpgrade;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class CoreStartupHandler {
	public static ArrayList<Event> execute(CoreStartup event, RawFileManager rawFileManager, ConnectionSource connectionSource) {
		try {
			DatabaseUpgrade.upgradeFromVersion(event.getLastServerVersion(), rawFileManager, connectionSource);

			TableUtils.dropTable(connectionSource, CalcReplicateCache.class, true);
			TableUtils.createTable(connectionSource, CalcReplicateCache.class);

			TableUtils.dropTable(connectionSource, CalcRepToCalcSamp.class, true);
			TableUtils.createTable(connectionSource, CalcRepToCalcSamp.class);

			TableUtils.dropTable(connectionSource, CalcSampleCache.class, true);
			TableUtils.createTable(connectionSource, CalcSampleCache.class);

			TableUtils.dropTable(connectionSource, CorrIntervalScratchPad.class, true);
			TableUtils.createTable(connectionSource, CorrIntervalScratchPad.class);

			TableUtils.dropTable(connectionSource, CorrIntervalError.class, true);
			TableUtils.createTable(connectionSource, CorrIntervalError.class);

			if (event.getIsServerMode()) { 
				Log.getInstance().log(Level.INFO, CoreStartupHandler.class, "Rebuilding corr interval tables.");
	
				Dao<CorrIntervalV1,Integer> corrIntervalDao = DaoManager.createDao(connectionSource, CorrIntervalV1.class);
				for (CorrIntervalV1 corrInterval : corrIntervalDao) {
					for (int replicateAnalysisId : corrInterval.getDataAnalysis()) {
						new LoadOrCalculateCorrInterval(corrInterval.getId(), replicateAnalysisId, connectionSource);
					}
				}
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, CoreStartupHandler.class, "Error while rebuilding corr interval tables.", e);
		}

		return null;
	}
}
