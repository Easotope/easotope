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

package org.easotope.shared.admin.cache.sampletype.sampletype;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.AcidTempList;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.AcidTempListCacheKey;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.AcidTempListItem;
import org.easotope.shared.admin.events.SampleTypeUpdated;
import org.easotope.shared.admin.tables.SampleType;
import org.easotope.shared.commands.SampleTypeGet;
import org.easotope.shared.commands.SampleTypeUpdate;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class SampleTypePlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new SampleTypeCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		SampleTypeGet sampleTypeGet = new SampleTypeGet();
		sampleTypeGet.setSampleTypeId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(sampleTypeGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return sampleTypeGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		SampleTypeGet sampleTypeGet = (SampleTypeGet) command;
		SampleType sampleType = sampleTypeGet.getSampleType();

		cache.put(new SampleTypeCacheKey(sampleType.getId()), this, callParameters, sampleType);

		AcidTempList acidTempList = new AcidTempList();
		acidTempList.setSampleTypeId(sampleType.getId());

		HashMap<Integer,Double> temps = sampleTypeGet.getAcidTempList();

		for (Integer id : temps.keySet()) {
			acidTempList.put(id, new AcidTempListItem(temps.get(id)));
		}

		cache.put(new AcidTempListCacheKey(sampleType.getId()), this, callParameters, acidTempList);
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof SampleTypeCacheSampleTypeGetListener) {
			((SampleTypeCacheSampleTypeGetListener) listener).sampleTypeGetCompleted(commandId, (SampleType) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof SampleTypeCacheSampleTypeGetListener) {
			((SampleTypeCacheSampleTypeGetListener) listener).sampleTypeGetError(commandId, message);
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

		if (event instanceof SampleTypeUpdated) {
			SampleTypeUpdated sampleTypeUpdated = (SampleTypeUpdated) event;
			SampleType sampleType = sampleTypeUpdated.getSampleType();

			CacheKey cacheKey = new SampleTypeCacheKey(sampleType.getId());

			if (cacheKey != null) {
				CacheKey oldCacheKey = cache.update(cacheKey, sampleType);
				result.add(oldCacheKey);
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof SampleTypeCacheSampleTypeGetListener) {
			((SampleTypeCacheSampleTypeGetListener) listener).sampleTypeUpdated(commandId, (SampleType) updated);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		SampleTypeUpdate sampleTypeUpdate = new SampleTypeUpdate();
		sampleTypeUpdate.setSampleType((SampleType) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(sampleTypeUpdate, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return sampleTypeUpdate.getClientUniqueId();
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		if (listener instanceof SampleTypeCacheSampleTypeSaveListener) {
			SampleTypeUpdate sampleTypeUpdate = (SampleTypeUpdate) command;
			((SampleTypeCacheSampleTypeSaveListener) listener).sampleTypeSaveCompleted(command.getClientUniqueId(), sampleTypeUpdate.getSampleType());
		}
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		if (listener instanceof SampleTypeCacheSampleTypeSaveListener) {
			((SampleTypeCacheSampleTypeSaveListener) listener).sampleTypeSaveError(commandId, message);
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
