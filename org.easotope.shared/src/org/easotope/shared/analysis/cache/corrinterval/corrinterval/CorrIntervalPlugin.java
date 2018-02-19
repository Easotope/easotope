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

package org.easotope.shared.analysis.cache.corrinterval.corrinterval;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.analysis.events.CorrIntervalsUpdated;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.commands.CorrIntervalGet;
import org.easotope.shared.commands.CorrIntervalUpdate;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class CorrIntervalPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new CorrIntervalCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		CorrIntervalGet corrIntervalGet = new CorrIntervalGet();
		corrIntervalGet.setCorrIntervalId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(corrIntervalGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return corrIntervalGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		CorrIntervalGet corrIntervalGet = (CorrIntervalGet) command;
		CorrIntervalV1 corrInterval = corrIntervalGet.getCorrInterval();

		cache.put(new CorrIntervalCacheKey(corrInterval.getId()), this, callParameters, corrInterval);
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof CorrIntervalCacheCorrIntervalGetListener) {
			((CorrIntervalCacheCorrIntervalGetListener) listener).corrIntervalGetCompleted(commandId, (CorrIntervalV1) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof CorrIntervalCacheCorrIntervalGetListener) {
			((CorrIntervalCacheCorrIntervalGetListener) listener).corrIntervalGetError(commandId, message);
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

		if (event instanceof CorrIntervalsUpdated) {
			CorrIntervalsUpdated corrIntervalsUpdated = (CorrIntervalsUpdated) event;

			for (CorrIntervalV1 corrInterval : corrIntervalsUpdated.getCorrIntervals()) {
				CacheKey cacheKey = new CorrIntervalCacheKey(corrInterval.getId());

				if (cache.containsKey(cacheKey)) {
					CacheKey oldCacheKey = cache.update(cacheKey, corrInterval);
					result.add(oldCacheKey);
				}
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof CorrIntervalCacheCorrIntervalGetListener) {
			((CorrIntervalCacheCorrIntervalGetListener) listener).corrIntervalUpdated(commandId, (CorrIntervalV1) updated);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		CorrIntervalUpdate corrIntervalUpdate = new CorrIntervalUpdate();
		corrIntervalUpdate.setCorrInterval((CorrIntervalV1) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(corrIntervalUpdate, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return corrIntervalUpdate.getClientUniqueId();
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		if (listener instanceof CorrIntervalCacheCorrIntervalSaveListener) {
			CorrIntervalUpdate corrIntervalUpdate = (CorrIntervalUpdate) command;
			((CorrIntervalCacheCorrIntervalSaveListener) listener).corrIntervalSaveCompleted(command.getClientUniqueId(), corrIntervalUpdate.getCorrInterval());
		}
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		if (listener instanceof CorrIntervalCacheCorrIntervalSaveListener) {
			((CorrIntervalCacheCorrIntervalSaveListener) listener).corrIntervalSaveError(commandId, message);
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
