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

package org.easotope.shared.core.cache;

import java.util.HashMap;
import java.util.Set;

public class CacheHashMap {
	private HashMap<CacheKey,Object> cache = new HashMap<CacheKey,Object>();

	public Set<CacheKey> keySet() {
		return cache.keySet();
	}

	public boolean containsKey(CacheKey cacheKey) {
		return cache.containsKey(cacheKey);
	}

	public CacheKey getKey(CacheKey cacheKey) {
		for (CacheKey thisCacheKey : cache.keySet()) {
			if (thisCacheKey.equals(cacheKey)) {
				return thisCacheKey;
			}
		}
		
		return null;
	}

	public Object get(CacheKey cacheKey) {
		return cache.get(cacheKey);
	}

	public CacheKey update(CacheKey cacheKey, Object object) {
		CacheKey oldCacheKey = getKey(cacheKey);

		if (oldCacheKey != null) {
			cache.put(oldCacheKey, object);
		}

		return oldCacheKey;
	}

	// cachePlugin and callParameters may be null only if this object cannot be reloaded
	public void put(CacheKey cacheKey, CachePlugin cachePlugin, Object[] callParameters, Object object) {
		cacheKey.setRecallInfo(cachePlugin, callParameters);
		cache.put(cacheKey, object);
	}

	public void remove(CacheKey cacheKey) {
		cache.remove(cacheKey);
	}

	public void clear() {
		cache.clear();
	}
}
