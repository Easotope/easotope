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

package org.easotope.shared.rawdata.cache.input.sample;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.commands.SampleGet;
import org.easotope.shared.commands.SampleUpdate;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.events.ProjectDeleted;
import org.easotope.shared.rawdata.events.ProjectUpdated;
import org.easotope.shared.rawdata.events.SampleDeleted;
import org.easotope.shared.rawdata.events.SampleUpdated;
import org.easotope.shared.rawdata.tables.Sample;

public class SamplePlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new SampleCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		SampleGet sampleGet = new SampleGet();
		sampleGet.setSampleId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(sampleGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return sampleGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		SampleGet sampleGet = (SampleGet) command;
		cache.put(new SampleCacheKey(sampleGet.getSampleId()), this, callParameters, sampleGet.getSample());
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof InputCacheSampleGetListener) {
			((InputCacheSampleGetListener) listener).sampleGetCompleted(commandId, (Sample) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheSampleGetListener) {
			((InputCacheSampleGetListener) listener).sampleGetError(commandId, message);
		}
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof SampleDeleted) {
			SampleDeleted sampleDeleted = (SampleDeleted) event;
			SampleCacheKey sampleCacheKey = new SampleCacheKey(sampleDeleted.getSampleId());

			if (cache.containsKey(sampleCacheKey)) {
				CacheKey oldCacheKey = cache.getKey(sampleCacheKey);
				result.add(oldCacheKey);
			}

		} else if (event instanceof ProjectDeleted) {
			ProjectDeleted projectDeleted = (ProjectDeleted) event;

			for (int sampleId : projectDeleted.getDeletedSampleIds()) {
				SampleCacheKey sampleCacheKey = new SampleCacheKey(sampleId);

				if (cache.containsKey(sampleCacheKey)) {
					CacheKey oldCacheKey = cache.getKey(sampleCacheKey);
					result.add(oldCacheKey);
				}
			}
		}

		return result;
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatNeedReloadBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public HashSet<CacheKey> updateCacheBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof SampleUpdated) {
			SampleUpdated sampleUpdated = (SampleUpdated) event;
			Sample sample = sampleUpdated.getSample();

			CacheKey cacheKey = new SampleCacheKey(sample.getId());

			if (cache.containsKey(cacheKey)) {
				CacheKey oldCacheKey = cache.update(cacheKey, sample);
				result.add(oldCacheKey);

			} else {
				cache.put(cacheKey, null, null, sample);
			}

		} else if (event instanceof ProjectUpdated) {
			ProjectUpdated projectUpdated = (ProjectUpdated) event;

			int oldUserId = projectUpdated.getOldUserId();
			int newUserId = projectUpdated.getProject().getUserId();

			if (oldUserId != DatabaseConstants.EMPTY_DB_ID && oldUserId != newUserId) {
				for (int sampleId : projectUpdated.getReassignedSampleIds()) {
					CacheKey cacheKey = new SampleCacheKey(sampleId);

					if (cache.containsKey(cacheKey)) {
						Sample sample = (Sample) cache.get(cacheKey);

						Sample newSample = new Sample(sample);
						newSample.setUserId(newUserId);

						CacheKey oldCacheKey = cache.update(cacheKey, newSample);
						result.add(oldCacheKey);
					}
				}
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof InputCacheSampleGetListener) {
			((InputCacheSampleGetListener) listener).sampleUpdated(commandId, (Sample) updated);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		SampleUpdate sampleUpdate = new SampleUpdate();

		sampleUpdate.setSample((Sample) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(sampleUpdate, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return sampleUpdate.getClientUniqueId();
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		if (listener instanceof InputCacheSampleSaveListener) {
			SampleUpdate sampleUpdate = (SampleUpdate) command;
			Sample sample = sampleUpdate.getSample();
			((InputCacheSampleSaveListener) listener).sampleSaveCompleted(command.getClientUniqueId(), sample);
		}
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheSampleSaveListener) {
			((InputCacheSampleSaveListener) listener).sampleSaveError(commandId, message);
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
