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

package org.easotope.shared.rawdata.cache.input.samplelist;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.commands.SampleListGet;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.cache.input.sample.SampleCacheKey;
import org.easotope.shared.rawdata.events.ReplicateDeleted;
import org.easotope.shared.rawdata.events.ReplicateUpdated;
import org.easotope.shared.rawdata.events.SampleUpdated;
import org.easotope.shared.rawdata.tables.Sample;

public class SampleListPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new SampleListCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		SampleListGet sampleListGet = new SampleListGet();
		sampleListGet.setProjectId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(sampleListGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return sampleListGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		SampleListGet sampleListGet = (SampleListGet) command;

		for (Integer sampleId : sampleListGet.getSampleList().keySet()) {
			cache.put(new SampleCacheKey(sampleId), this, callParameters, sampleListGet.getSampleList().get(sampleId).getSample());
		}

		SampleList sampleList = new SampleList(sampleListGet.getProjectId());
		sampleList.putAll(sampleListGet.getSampleList());
		cache.put(new SampleListCacheKey(sampleListGet.getProjectId()), this, callParameters, sampleList);
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof InputCacheSampleListGetListener) {
			((InputCacheSampleListGetListener) listener).sampleListGetCompleted(commandId, (SampleList) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheSampleListGetListener) {
			((InputCacheSampleListGetListener) listener).sampleListGetError(commandId, message);
		}
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatNeedReloadBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof ReplicateDeleted) {
			ReplicateDeleted replicateDeleted = (ReplicateDeleted) event;

			for (CacheKey cacheKey : cache.keySet()) {
				if (!(cacheKey instanceof SampleListCacheKey)) {
					continue;
				}

				SampleList oldSampleList = (SampleList) cache.get(cacheKey);

				if (oldSampleList.containsKey(replicateDeleted.getSampleId())) {
					result.add(cacheKey);
				}
			}
		}

		return result;
	}

	@Override
	public HashSet<CacheKey> updateCacheBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof SampleUpdated) {
			SampleUpdated sampleUpdated = (SampleUpdated) event;
			Sample sample = sampleUpdated.getSample();

			CacheKey cacheKey = new SampleListCacheKey(sample.getProjectId());

			if (cache.containsKey(cacheKey)) {
				SampleList sampleList = (SampleList) cache.get(cacheKey);
				SampleList newSampleList = new SampleList(sample.getProjectId());
				newSampleList.putAll(sampleList);

				newSampleList.put(sample.getId(), new SampleListItem(sample, sampleUpdated.isHasChildren()));

				CacheKey oldCacheKey = cache.update(cacheKey, newSampleList);
				result.add(oldCacheKey);
			}

		} else if (event instanceof ReplicateUpdated) {
			ReplicateUpdated replicateUpdated = (ReplicateUpdated) event;

			for (CacheKey cacheKey : cache.keySet()) {
				if (!(cacheKey instanceof SampleListCacheKey)) {
					continue;
				}

				SampleList oldSampleList = (SampleList) cache.get(cacheKey);

				if (oldSampleList.containsKey(replicateUpdated.getReplicate().getSampleId())) {
					Sample oldSample = oldSampleList.get(replicateUpdated.getReplicate().getSampleId()).getSample();

					SampleList newSampleList = new SampleList(oldSampleList.getProjectId());
					newSampleList.putAll(oldSampleList);
					newSampleList.put(oldSample.getId(), new SampleListItem(oldSample, true));

					CacheKey oldCacheKey = cache.update(cacheKey, newSampleList);
					result.add(oldCacheKey);
				}
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof InputCacheSampleListGetListener) {
			((InputCacheSampleListGetListener) listener).sampleListUpdated(commandId, (SampleList) updated);
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
