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

package org.easotope.shared.admin.cache.standard.standardlist;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.admin.events.StandardUpdated;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.commands.StandardListGet;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class StandardListPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new StandardListCacheKey();
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		Command command = new StandardListGet();
		
		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(command, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return command.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		StandardListGet standardListGet = (StandardListGet) command;
		StandardList standardList = new StandardList();

		HashMap<Integer,String> names = standardListGet.getStandardList();

		for (Integer id : names.keySet()) {
			standardList.put(id, new StandardListItem(names.get(id)));
		}

		cache.put(new StandardListCacheKey(), this, callParameters, standardList);
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof StandardCacheStandardListGetListener) {
			((StandardCacheStandardListGetListener) listener).standardListGetCompleted(commandId, (StandardList) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof StandardCacheStandardListGetListener) {
			((StandardCacheStandardListGetListener) listener).standardListGetError(commandId, message);
		}
	}
	
	@Override
	public HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatNeedReloadBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public HashSet<CacheKey> updateCacheBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof StandardUpdated) {
			StandardUpdated standardUpdated = (StandardUpdated) event;

			CacheKey cacheKey = new StandardListCacheKey();

			if (cache.containsKey(cacheKey)) {
				StandardList standardList = (StandardList) cache.get(cacheKey);

				Standard standard = standardUpdated.getStandard();
				int id = standard.getId();
				String name = standard.getName();

				if (!standardList.containsKey(id) || !standardList.get(id).equals(name)) {
					StandardList standardListCopy = new StandardList();
					standardListCopy.putAll(standardList);

					standardListCopy.put(id, new StandardListItem(name));

					CacheKey oldCacheKey = cache.update(cacheKey, standardListCopy);
					result.add(oldCacheKey);
				}
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof StandardCacheStandardListGetListener) {
			((StandardCacheStandardListGetListener) listener).standardListUpdated(commandId, (StandardList) updated);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		assert(false);
		return 0;
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		assert(false);
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		assert(false);
	}
	
	@Override
	public void callbackVerifyAndResend(Object listener, int commandId, String message) {
		assert(false);
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
