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

package org.easotope.shared.admin.cache.sampletype;

import org.easotope.shared.admin.cache.sampletype.acidtemp.AcidTempPlugin;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.AcidTempListPlugin;
import org.easotope.shared.admin.cache.sampletype.sampletype.SampleTypePlugin;
import org.easotope.shared.admin.cache.sampletype.sampletypelist.SampleTypeListPlugin;
import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.admin.tables.SampleType;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheListener;

public class SampleTypeCache extends AbstractCache {
	private SampleTypeListPlugin sampleTypeListPlugin = new SampleTypeListPlugin();
	private SampleTypePlugin sampleTypePlugin = new SampleTypePlugin();
	private AcidTempListPlugin acidTempListPlugin = new AcidTempListPlugin();
	private AcidTempPlugin acidTempPlugin = new AcidTempPlugin();

	public static SampleTypeCache getInstance() {
		return (SampleTypeCache) AbstractCache.getCacheInstanceForThisThread(SampleTypeCache.class);
	}

	public SampleTypeCache() {
		addPlugin(sampleTypeListPlugin);
		addPlugin(sampleTypePlugin);
		addPlugin(acidTempListPlugin);
		addPlugin(acidTempPlugin);
	}

	public synchronized int sampleTypeListGet(CacheListener listener) {
		return getObject(sampleTypeListPlugin, listener);
	}

	public synchronized int sampleTypeGet(int sampleTypeId, CacheListener listener) {
		return getObject(sampleTypePlugin, listener, sampleTypeId);
	}

	public synchronized int sampleTypeSave(SampleType sampleType, CacheListener listener) {
		return saveObject(sampleTypePlugin, listener, sampleType);
	}

	public synchronized int acidTempListGet(int sampleTypeId, CacheListener listener) {
		return getObject(acidTempListPlugin, listener, sampleTypeId);
	}

	public synchronized int acidTempGet(int acidTempId, CacheListener listener) {
		return getObject(acidTempPlugin, listener, acidTempId);
	}

	public synchronized int acidTempSave(AcidTemp acidTemp, CacheListener listener) {
		return saveObject(acidTempPlugin, listener, acidTemp);
	}
}
