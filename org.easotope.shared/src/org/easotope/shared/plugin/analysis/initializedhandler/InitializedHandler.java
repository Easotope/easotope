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

package org.easotope.shared.plugin.analysis.initializedhandler;

import java.util.ArrayList;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.events.Initialized;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.analysis.tables.CalcRepToCalcSamp;
import org.easotope.shared.analysis.tables.CalcReplicateCache;
import org.easotope.shared.analysis.tables.CalcSampleCache;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.CorrIntervalScratchPad;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.analysis.tables.RepAnalysisChoice;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.analysis.tables.SamStepParams;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class InitializedHandler {
	public static ArrayList<Event> execute(Initialized event, RawFileManager rawFileManager, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, CorrIntervalV1.class);
			TableUtils.createTable(connectionSource, RepStepParams.class);
			TableUtils.createTable(connectionSource, SamStepParams.class);
			TableUtils.createTable(connectionSource, RepAnalysisChoice.class);
			TableUtils.createTable(connectionSource, CalcReplicateCache.class);
			TableUtils.createTable(connectionSource, CalcRepToCalcSamp.class);
			TableUtils.createTable(connectionSource, CalcSampleCache.class);
			TableUtils.createTable(connectionSource, CorrIntervalScratchPad.class);
			TableUtils.createTable(connectionSource, CorrIntervalError.class);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, AnalysesCreator.class, "Error during initialization in analysis plugin.", e);
		}

		AnalysesCreator.create(connectionSource);

		return null;
	}
}
