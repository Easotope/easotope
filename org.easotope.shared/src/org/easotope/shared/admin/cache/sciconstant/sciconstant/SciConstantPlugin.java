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

package org.easotope.shared.admin.cache.sciconstant.sciconstant;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.admin.SciConstantNames;
import org.easotope.shared.admin.cache.sciconstant.SciConstantSuperPlugin;
import org.easotope.shared.admin.events.SciConstantUpdated;
import org.easotope.shared.admin.tables.SciConstant;
import org.easotope.shared.commands.SciConstantUpdate;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class SciConstantPlugin extends SciConstantSuperPlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		if (parameters[0] instanceof Integer) {
			return new SciConstantCacheKey((Integer) parameters[0]);
		} else {
			return new SciConstantCacheKey((SciConstantNames) parameters[0]);
		}
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof SciConstantCacheSciConstantGetListener) {
			((SciConstantCacheSciConstantGetListener) listener).sciConstantGetCompleted(commandId, (SciConstant) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof SciConstantCacheSciConstantGetListener) {
			((SciConstantCacheSciConstantGetListener) listener).sciConstantGetError(commandId, message);
		}
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		assert(false);
	}

	@Override
	public HashSet<CacheKey> updateCacheBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof SciConstantUpdated) {
			SciConstantUpdated sciConstantUpdated = (SciConstantUpdated) event;
			SciConstant sciConstant = sciConstantUpdated.getSciConstant();

			CacheKey cacheKey = new SciConstantCacheKey(sciConstant.getId());

			if (cache.containsKey(cacheKey)) {
				CacheKey oldCacheKey = cache.update(cacheKey, sciConstant);
				result.add(oldCacheKey);
			}

			cacheKey = new SciConstantCacheKey(sciConstant.getEnumeration());

			if (cache.containsKey(cacheKey)) {
				cache.update(cacheKey, sciConstant);
				// don't add to results since this was already done for the id cache key
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof SciConstantCacheSciConstantGetListener) {
			((SciConstantCacheSciConstantGetListener) listener).sciConstantUpdated(commandId, (SciConstant) updated);
		}
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatNeedReloadBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		SciConstantUpdate sciConstantUpdate = new SciConstantUpdate();
		sciConstantUpdate.setSciConstant((SciConstant) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(sciConstantUpdate, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return sciConstantUpdate.getClientUniqueId();
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		if (listener instanceof SciConstantCacheSciConstantSaveListener) {
			SciConstantUpdate sciConstantUpdate = (SciConstantUpdate) command;
			((SciConstantCacheSciConstantSaveListener) listener).sciConstantSaveCompleted(command.getClientUniqueId(), sciConstantUpdate.getSciConstant());
		}
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		if (listener instanceof SciConstantCacheSciConstantSaveListener) {
			((SciConstantCacheSciConstantSaveListener) listener).sciConstantSaveError(commandId, message);
		}
	}

	@Override
	public int deleteData(AbstractCache abstractCache, Object[] parameters) {
		assert(false);
		return 0;
	}
	
	@Override
	public void callbackDeleteCompleted(Object listener, Command command) {
		assert(false);
	}

	@Override
	public void callbackDeleteError(Object listener, int commandId, String message) {
		assert(false);
	}
}
