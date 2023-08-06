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

package org.easotope.shared.admin.cache.standard.standard;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.admin.events.StandardUpdated;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.commands.StandardGet;
import org.easotope.shared.commands.StandardUpdate;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class StandardPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new StandardCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		StandardGet standardGet = new StandardGet();
		standardGet.setStandardId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(standardGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return standardGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		StandardGet standardGet = (StandardGet) command;
		Standard standard = standardGet.getStandard();
		cache.put(new StandardCacheKey(standard.getId()), this, callParameters, standard);
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof StandardCacheStandardGetListener) {
			((StandardCacheStandardGetListener) listener).standardGetCompleted(commandId, (Standard) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof StandardCacheStandardGetListener) {
			((StandardCacheStandardGetListener) listener).standardGetError(commandId, message);
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
			Standard standard = standardUpdated.getStandard();

			CacheKey cacheKey = new StandardCacheKey(standard.getId());

			if (cache.containsKey(cacheKey)) {
				CacheKey oldCacheKey = cache.update(cacheKey, standard);
				result.add(oldCacheKey);
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof StandardCacheStandardGetListener) {
			((StandardCacheStandardGetListener) listener).standardUpdated(commandId, (Standard) updated);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		StandardUpdate standardUpdate = new StandardUpdate();
		standardUpdate.setStandard((Standard) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(standardUpdate, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return standardUpdate.getClientUniqueId();
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		if (listener instanceof StandardCacheStandardSaveListener) {
			StandardUpdate standardUpdate = (StandardUpdate) command;
			Standard standard = standardUpdate.getStandard();
			((StandardCacheStandardSaveListener) listener).standardSaveCompleted(command.getClientUniqueId(), standard);
		}
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		if (listener instanceof StandardCacheStandardSaveListener) {
			((StandardCacheStandardSaveListener) listener).standardSaveError(commandId, message);
		}
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
