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

package org.easotope.shared.admin.cache.massspec;

import org.easotope.shared.admin.cache.massspec.massspec.MassSpecPlugin;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecListPlugin;
import org.easotope.shared.admin.cache.massspec.refgas.RefGasPlugin;
import org.easotope.shared.admin.cache.massspec.refgaslist.RefGasListPlugin;
import org.easotope.shared.admin.tables.MassSpec;
import org.easotope.shared.admin.tables.RefGas;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheListener;

public class MassSpecCache extends AbstractCache {
	private MassSpecListPlugin massSpecListPlugin = new MassSpecListPlugin();
	private MassSpecPlugin massSpecPlugin = new MassSpecPlugin();
	private RefGasListPlugin refGasListPlugin = new RefGasListPlugin();
	private RefGasPlugin refGasPlugin = new RefGasPlugin();

	public static MassSpecCache getInstance() {
		return (MassSpecCache) AbstractCache.getCacheInstanceForThisThread(MassSpecCache.class);
	}

	public MassSpecCache() {
		addPlugin(massSpecListPlugin);
		addPlugin(massSpecPlugin);
		addPlugin(refGasListPlugin);
		addPlugin(refGasPlugin);
	}

	public synchronized int massSpecListGet(CacheListener listener) {
		return getObject(massSpecListPlugin, listener);
	}

	public synchronized int massSpecGet(int massSpecId, CacheListener listener) {
		return getObject(massSpecPlugin, listener, massSpecId);
	}

	public synchronized int massSpecSave(MassSpec massSpec, CacheListener listener) {
		return saveObject(massSpecPlugin, listener, massSpec);
	}

	public synchronized int refGasListGet(int massSpecId, CacheListener listener) {
		return getObject(refGasListPlugin, listener, massSpecId);
	}

	public synchronized int refGasGet(int refGasId, CacheListener listener) {
		return getObject(refGasPlugin, listener, refGasId);
	}

	public synchronized int refGasSave(RefGas refGas, CacheListener listener) {
		return saveObject(refGasPlugin, listener, refGas);
	}
}
