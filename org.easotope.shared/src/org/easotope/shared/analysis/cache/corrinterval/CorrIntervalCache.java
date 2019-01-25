/*
 * Copyright © 2016-2019 by Devon Bowen.
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

package org.easotope.shared.analysis.cache.corrinterval;

import org.easotope.shared.analysis.cache.corrinterval.corrinterval.CorrIntervalPlugin;
import org.easotope.shared.analysis.cache.corrinterval.corrintervalcomp.CorrIntervalCompPlugin;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListPlugin;
import org.easotope.shared.analysis.cache.corrinterval.repstepparams.RepStepParamsPlugin;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheListener;

public class CorrIntervalCache extends AbstractCache {
	private CorrIntervalListPlugin corrIntervalListPlugin = new CorrIntervalListPlugin();
	private CorrIntervalPlugin corrIntervalPlugin = new CorrIntervalPlugin();
	private CorrIntervalCompPlugin corrIntervalCompPlugin = new CorrIntervalCompPlugin();
	private RepStepParamsPlugin repStepParamsPlugin = new RepStepParamsPlugin();

	public static CorrIntervalCache getInstance() {
		return (CorrIntervalCache) AbstractCache.getCacheInstanceForThisThread(CorrIntervalCache.class);
	}

	public CorrIntervalCache() {
		addPlugin(corrIntervalListPlugin);
		addPlugin(corrIntervalPlugin);
		addPlugin(corrIntervalCompPlugin);
		addPlugin(repStepParamsPlugin);
	}

	public int corrIntervalListGet(int massSpecId, CacheListener listener) {
		return getObject(corrIntervalListPlugin, listener, massSpecId);
	}

	public int corrIntervalGet(int corrIntervalId, CacheListener listener) {
		return getObject(corrIntervalPlugin, listener, corrIntervalId);
	}

	public int corrIntervalSave(CorrIntervalV1 corrInterval, CacheListener listener) {
		return saveObject(corrIntervalPlugin, listener, corrInterval);
	}

	public int corrIntervalCompGet(int corrIntervalId, int dataAnalysisId, CacheListener listener) {
		return getObject(corrIntervalCompPlugin, listener, corrIntervalId, dataAnalysisId);
	}

	public int repStepParametersSave(RepStepParams repStepParams, CacheListener listener) {
		return saveObject(repStepParamsPlugin, listener, repStepParams);		
	}
}
