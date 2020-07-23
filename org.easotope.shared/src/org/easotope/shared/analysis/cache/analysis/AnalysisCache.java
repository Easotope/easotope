/*
 * Copyright © 2016-2020 by Devon Bowen.
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

package org.easotope.shared.analysis.cache.analysis;

import org.easotope.shared.analysis.cache.analysis.repanalysis.RepAnalysisPlugin;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.RepAnalysisListPlugin;
import org.easotope.shared.analysis.cache.analysis.samanalysis.SamAnalysisPlugin;
import org.easotope.shared.analysis.cache.analysis.samanalysislist.SamAnalysisListPlugin;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheListener;

public class AnalysisCache extends AbstractCache {
	private RepAnalysisListPlugin repAnalysisListPlugin = new RepAnalysisListPlugin();
	private RepAnalysisPlugin repAnalysisPlugin = new RepAnalysisPlugin();
	private SamAnalysisListPlugin samAnalysisListPlugin = new SamAnalysisListPlugin();
	private SamAnalysisPlugin samAnalysisPlugin = new SamAnalysisPlugin();

	public static AnalysisCache getInstance() {
		return (AnalysisCache) AbstractCache.getCacheInstanceForThisThread(AnalysisCache.class);
	}

	public AnalysisCache() {
		addPlugin(repAnalysisListPlugin);
		addPlugin(repAnalysisPlugin);
		addPlugin(samAnalysisListPlugin);
		addPlugin(samAnalysisPlugin);
	}

	public int repAnalysisListGet(CacheListener listener) {
		return getObject(repAnalysisListPlugin, listener);
	}

	public int repAnalysisGet(int repAnalysisId, CacheListener listener) {
		return getObject(repAnalysisPlugin, listener, repAnalysisId);
	}

	public int samAnalysisListGet(CacheListener listener) {
		return getObject(samAnalysisListPlugin, listener);
	}
	
	public int samAnalysisGet(int samAnalysisId, CacheListener listener) {
		return getObject(samAnalysisPlugin, listener, samAnalysisId);
	}
}
