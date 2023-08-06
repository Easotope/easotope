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

package org.easotope.shared.rawdata.cache.input.replicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.admin.tables.SampleType;
import org.easotope.shared.commands.ReplicateDelete;
import org.easotope.shared.commands.ReplicateGet;
import org.easotope.shared.commands.ReplicateUpdate;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.events.ProjectDeleted;
import org.easotope.shared.rawdata.events.ProjectUpdated;
import org.easotope.shared.rawdata.events.ReplicateDeleted;
import org.easotope.shared.rawdata.events.ReplicateUpdated;
import org.easotope.shared.rawdata.events.SampleDeleted;
import org.easotope.shared.rawdata.events.SampleUpdated;
import org.easotope.shared.rawdata.tables.ReplicateV1;

public class ReplicatePlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new ReplicateCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		ReplicateGet replicateGet = new ReplicateGet();
		replicateGet.setReplicateId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(replicateGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return replicateGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		ReplicateGet replicateGet = (ReplicateGet) command;

		ReplicateV1 replicate = replicateGet.getReplicate();
		ArrayList<Acquisition> acquisitions = replicateGet.getAcquisitions();
		Integer projectId = replicateGet.getProjectId();
		SampleType sampleType = replicateGet.getSampleType();

		cache.put(new ReplicateCacheKey(replicate.getId()), this, callParameters, new Object[] { replicate, acquisitions, projectId, sampleType });
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof InputCacheReplicateGetListener) {
			Object[] resultArray = (Object[]) result;

			@SuppressWarnings("unchecked")
			ArrayList<Acquisition> acquisitionList = (ArrayList<Acquisition>) resultArray[1];

			((InputCacheReplicateGetListener) listener).replicateGetCompleted(commandId, (ReplicateV1) resultArray[0], acquisitionList, (Integer) resultArray[2], (SampleType) resultArray[3]);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheReplicateGetListener) {
			((InputCacheReplicateGetListener) listener).replicateGetError(commandId, message);
		}
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof ReplicateDeleted) {
			ReplicateDeleted replicateDeleted = (ReplicateDeleted) event;
			ReplicateCacheKey replicateCacheKey = new ReplicateCacheKey(replicateDeleted.getReplicateId());

			if (cache.containsKey(replicateCacheKey)) {
				CacheKey oldCacheKey = cache.getKey(replicateCacheKey);
				result.add(oldCacheKey);
			}

		} if (event instanceof SampleDeleted) {
			SampleDeleted sampleDeleted = (SampleDeleted) event;

			for (int replicateId : sampleDeleted.getDeletedReplicateIds()) {
				ReplicateCacheKey replicateCacheKey = new ReplicateCacheKey(replicateId);

				if (cache.containsKey(replicateCacheKey)) {
					CacheKey oldCacheKey = cache.getKey(replicateCacheKey);
					result.add(oldCacheKey);
				}
			}

		} if (event instanceof ProjectDeleted) {
			ProjectDeleted projectDeleted = (ProjectDeleted) event;

			for (int replicateId : projectDeleted.getDeletedReplicateIds()) {
				ReplicateCacheKey replicateCacheKey = new ReplicateCacheKey(replicateId);

				if (cache.containsKey(replicateCacheKey)) {
					CacheKey oldCacheKey = cache.getKey(replicateCacheKey);
					result.add(oldCacheKey);
				}
			}
		}

		return result;
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatNeedReloadBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof ReplicateUpdated) {
			ReplicateUpdated replicateUpdated = (ReplicateUpdated) event;
			ReplicateCacheKey replicateCacheKey = new ReplicateCacheKey(replicateUpdated.getReplicate().getId());

			if (cache.containsKey(replicateCacheKey)) {
				CacheKey oldCacheKey = cache.getKey(replicateCacheKey);
				result.add(oldCacheKey);
			}
		}

		return result;
	}

	@Override
	public HashSet<CacheKey> updateCacheBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof SampleUpdated) {
			SampleUpdated sampleUpdated = (SampleUpdated) event;
			int newUserId = sampleUpdated.getSample().getUserId();
			int newSampleId = sampleUpdated.getSample().getId();
			int newProjectId = sampleUpdated.getSample().getProjectId();

			for (int replicateId : sampleUpdated.getReassignedReplicateIds()) {
				ReplicateCacheKey replicateCacheKey = new ReplicateCacheKey(replicateId);

				if (cache.containsKey(replicateCacheKey)) {
					Object[] oldArray = (Object[]) cache.get(replicateCacheKey);
					Object[] newArray = Arrays.copyOf(oldArray, oldArray.length);

					ReplicateV1 replicateCopy = new ReplicateV1((ReplicateV1) oldArray[0]);
					replicateCopy.setUserId(newUserId);
					replicateCopy.setSampleId(newSampleId);
					newArray[0] = replicateCopy;
					newArray[2] = newProjectId;

					CacheKey oldCacheKey = cache.update(replicateCacheKey, newArray);
					result.add(oldCacheKey);
				}
			}

		} if (event instanceof ProjectUpdated) {
			ProjectUpdated projectUpdated = (ProjectUpdated) event;
			int newUserId = projectUpdated.getProject().getUserId();

			for (int replicateId : projectUpdated.getReassignedReplicateIds()) {
				ReplicateCacheKey replicateCacheKey = new ReplicateCacheKey(replicateId);

				if (cache.containsKey(replicateCacheKey)) {
					Object[] oldArray = (Object[]) cache.get(replicateCacheKey);
					Object[] newArray = Arrays.copyOf(oldArray, oldArray.length);

					ReplicateV1 replicateCopy = new ReplicateV1((ReplicateV1) oldArray[0]);
					replicateCopy.setUserId(newUserId);
					newArray[0] = replicateCopy;

					CacheKey oldCacheKey = cache.update(replicateCacheKey, newArray);
					result.add(oldCacheKey);
				}
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof InputCacheReplicateGetListener) {
			Object[] updatedArray = (Object[]) updated;

			@SuppressWarnings("unchecked")
			ArrayList<Acquisition> acquisitionList = (ArrayList<Acquisition>) updatedArray[1];

			((InputCacheReplicateGetListener) listener).replicateUpdated(commandId, (ReplicateV1) updatedArray[0], acquisitionList, (Integer) updatedArray[2], (SampleType) updatedArray[3]);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {	
		if (listener instanceof InputCacheReplicateGetListener) {
			int replicateId = ((ReplicateCacheKey) cacheKey).getReplicateId();
			((InputCacheReplicateGetListener) listener).replicateDeleted(replicateId);
		}
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		ReplicateUpdate replicateUpdate = new ReplicateUpdate();

		replicateUpdate.setReplicate((ReplicateV1) parameters[0]);

		@SuppressWarnings("unchecked")
		ArrayList<Acquisition> acquisitionList = (ArrayList<Acquisition>) parameters[1];
		replicateUpdate.setAcquisitions(acquisitionList);

		replicateUpdate.setExplode((Boolean) parameters[2]);
		replicateUpdate.setAllowDuplicates((Boolean) parameters[3]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(replicateUpdate, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return replicateUpdate.getClientUniqueId();
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		if (listener instanceof InputCacheReplicateSaveListener) {
			((InputCacheReplicateSaveListener) listener).replicateSaveCompleted(command.getClientUniqueId());
		}
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheReplicateSaveListener) {
			((InputCacheReplicateSaveListener) listener).replicateSaveError(commandId, message);
		}
	}

	@Override
	public void callbackVerifyAndResend(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheReplicateSaveListener) {
			((InputCacheReplicateSaveListener) listener).replicateRequestResend(commandId, message);
		}
	}

	@Override
	public int deleteData(AbstractCache abstractCache, Object[] parameters) {
		ReplicateDelete replicateDelete = new ReplicateDelete();

		replicateDelete.setReplicateId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(replicateDelete, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return replicateDelete.getClientUniqueId();
	}

	@Override
	public void callbackDeleteCompleted(Object listener, Command command) {
		if (listener instanceof InputCacheReplicateSaveListener) {
			((InputCacheReplicateSaveListener) listener).replicateDeleteCompleted(command.getClientUniqueId());
		}
	}

	@Override
	public void callbackDeleteError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheReplicateSaveListener) {
			((InputCacheReplicateSaveListener) listener).replicateDeleteError(commandId, message);
		}
	}
}
