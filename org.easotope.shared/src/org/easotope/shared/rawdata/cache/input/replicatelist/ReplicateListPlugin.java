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

package org.easotope.shared.rawdata.cache.input.replicatelist;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.commands.ReplicateListGet;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.events.ProjectDeleted;
import org.easotope.shared.rawdata.events.ReplicateDeleted;
import org.easotope.shared.rawdata.events.ReplicateUpdated;
import org.easotope.shared.rawdata.events.SampleDeleted;
import org.easotope.shared.rawdata.events.SampleUpdated;
import org.easotope.shared.rawdata.tables.ReplicateV1;

public class ReplicateListPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new ReplicateListCacheKey((Boolean) parameters[0], (Integer) parameters[1], (Integer) parameters[2], (Integer) parameters[3]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		ReplicateListGet replicateListGet = new ReplicateListGet();

		replicateListGet.setGetSamples((Boolean) parameters[0]);
		replicateListGet.setSampleId((Integer) parameters[1]);
		replicateListGet.setMassSpecId((Integer) parameters[2]);
		replicateListGet.setUserId((Integer) parameters[3]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(replicateListGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return replicateListGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		ReplicateListGet replicateListGet = (ReplicateListGet) command;

		boolean getSamples = replicateListGet.isGetSamples();
		int sampleId = replicateListGet.getSampleId();
		int massSpecId = replicateListGet.getMassSpecId();
		int userId = replicateListGet.getUserId();

		cache.put(new ReplicateListCacheKey(getSamples, sampleId, massSpecId, userId), this, callParameters, replicateListGet.getReplicateList());
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof InputCacheReplicateListGetListener) {
			((InputCacheReplicateListGetListener) listener).replicateListGetCompleted(commandId, (ReplicateList) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheReplicateListGetListener) {
			((InputCacheReplicateListGetListener) listener).replicateListGetError(commandId, message);
		}
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof SampleDeleted) {
			SampleDeleted sampleDeleted = (SampleDeleted) event;
			int sampleId = sampleDeleted.getSampleId();

			ReplicateListCacheKey replicateListCacheKey = new ReplicateListCacheKey(true, sampleId, DatabaseConstants.EMPTY_DB_ID, DatabaseConstants.EMPTY_DB_ID);

			if (cache.containsKey(replicateListCacheKey)) {
				CacheKey olcCacheKey = cache.getKey(replicateListCacheKey);
				result.add(olcCacheKey);
			}

		} if (event instanceof ProjectDeleted) {
			ProjectDeleted projectDeleted = (ProjectDeleted) event;

			for (Integer sampleId : projectDeleted.getDeletedSampleIds()) {
				ReplicateListCacheKey replicateListCacheKey = new ReplicateListCacheKey(true, sampleId, DatabaseConstants.EMPTY_DB_ID, DatabaseConstants.EMPTY_DB_ID);

				if (cache.containsKey(replicateListCacheKey)) {
					CacheKey oldCacheKey = cache.getKey(replicateListCacheKey);
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
			int sampleId = sampleUpdated.getSample().getId();
			String sampleName = sampleUpdated.getSample().getName();

			for (CacheKey cacheKey : cache.keySet()) {
				if (!(cacheKey instanceof ReplicateListCacheKey)) {
					continue;
				}

				ReplicateList oldReplicateList = (ReplicateList) cache.get(cacheKey);

				if (oldReplicateList.getSampleId() != DatabaseConstants.EMPTY_DB_ID && oldReplicateList.getSampleId() != sampleId) {
					continue;
				}

				if (!oldReplicateList.isGetSamples()) {
					continue;
				}

				boolean hasChanged = false;
				ReplicateList newReplicateList = new ReplicateList(oldReplicateList);

				for (Integer replicateId : oldReplicateList.keySet()) {
					ReplicateListItem oldReplicateListItem = oldReplicateList.get(replicateId);

					if (oldReplicateListItem.getSampleId() == sampleId) {
						ReplicateListItem newReplicateListItem = new ReplicateListItem(oldReplicateListItem.getDate(), oldReplicateListItem.getUserId(), oldReplicateListItem.getStandardId(), sampleId, sampleName, oldReplicateListItem.getDisabled());
						newReplicateList.put(replicateId, newReplicateListItem);
						hasChanged = true;
					} else {
						newReplicateList.put(replicateId, oldReplicateListItem);
					}
				}

				if (hasChanged) {
					CacheKey oldCacheKey = cache.update(cacheKey, newReplicateList);
					result.add(oldCacheKey);
				}
			}

		} else if (event instanceof ReplicateUpdated) {
			ReplicateUpdated replicateUpdated = (ReplicateUpdated) event;
			ReplicateV1 replicate = replicateUpdated.getReplicate();
			String replicateSampleName = replicateUpdated.getNewSampleName();

			int replicateId = replicate.getId();
			long replicateDate = replicate.getDate();
			int replicateStandardId = replicate.getStandardId();
			int replicateMassSpecId = replicate.getMassSpecId();
			int replicateSampleId = replicate.getSampleId();
			int replicateUserId = replicate.getUserId();
			boolean replicateIsDisabled = replicate.isDisabled();

			for (CacheKey cacheKey : cache.keySet()) {
				if (!(cacheKey instanceof ReplicateListCacheKey)) {
					continue;
				}

				ReplicateList oldReplicateList = (ReplicateList) cache.get(cacheKey);
				boolean listShouldContainReplicate = true;

				if (oldReplicateList.isGetSamples() && replicateSampleId == DatabaseConstants.EMPTY_DB_ID) {
					listShouldContainReplicate = false;
				}

				if (!oldReplicateList.isGetSamples() && oldReplicateList.getSampleId() == DatabaseConstants.EMPTY_DB_ID && replicateSampleId != DatabaseConstants.EMPTY_DB_ID) {
					listShouldContainReplicate = false;
				}

				if (oldReplicateList.getMassSpecId() != DatabaseConstants.EMPTY_DB_ID && oldReplicateList.getMassSpecId() != replicateMassSpecId) {
					listShouldContainReplicate = false;
				}

				if (oldReplicateList.getSampleId() != DatabaseConstants.EMPTY_DB_ID && oldReplicateList.getSampleId() != replicateSampleId) {
					listShouldContainReplicate = false;
				}

				if (oldReplicateList.getUserId() != DatabaseConstants.EMPTY_DB_ID && oldReplicateList.getUserId() != replicateUserId) {
					listShouldContainReplicate = false;
				}

				if (oldReplicateList.containsKey(replicateId) && listShouldContainReplicate) {
					ReplicateList newReplicateList = new ReplicateList(oldReplicateList);
					newReplicateList.putAll(oldReplicateList);

					newReplicateList.remove(replicateId);

					ReplicateListItem newReplicateListItem = new ReplicateListItem(replicateDate, replicateUserId, replicateStandardId, replicateSampleId, replicateSampleName, replicateIsDisabled);
					newReplicateList.put(replicateId, newReplicateListItem);

					CacheKey oldCacheKey = cache.update(cacheKey, newReplicateList);
					result.add(oldCacheKey);
				}

				if (oldReplicateList.containsKey(replicateId) && !listShouldContainReplicate) {
					ReplicateList newReplicateList = new ReplicateList(oldReplicateList);
					newReplicateList.putAll(oldReplicateList);

					newReplicateList.remove(replicateId);

					CacheKey oldCacheKey = cache.update(cacheKey, newReplicateList);
					result.add(oldCacheKey);
				}

				if (!oldReplicateList.containsKey(replicateId) && listShouldContainReplicate) {
					ReplicateList newReplicateList = new ReplicateList(oldReplicateList);
					newReplicateList.putAll(oldReplicateList);

					ReplicateListItem newReplicateListItem = new ReplicateListItem(replicateDate, replicateUserId, replicateStandardId, replicateSampleId, replicateSampleName, replicateIsDisabled);
					newReplicateList.put(replicateId, newReplicateListItem);

					CacheKey oldCacheKey = cache.update(cacheKey, newReplicateList);
					result.add(oldCacheKey);
				}
			}

		} else if (event instanceof ReplicateDeleted) {
			ReplicateDeleted replicateDeleted = (ReplicateDeleted) event;
			deleteReplicateById(replicateDeleted.getReplicateId(), cache, result);

		} else if (event instanceof SampleDeleted) {
			SampleDeleted sampleDeleted = (SampleDeleted) event;

			for (int replicateId : sampleDeleted.getDeletedReplicateIds()) {
				deleteReplicateById(replicateId, cache, result);
			}

		} else if (event instanceof ProjectDeleted) {
			ProjectDeleted projectDeleted = (ProjectDeleted) event;

			for (int replicateId : projectDeleted.getDeletedReplicateIds()) {
				deleteReplicateById(replicateId, cache, result);
			}
		}

		return result;
	}

	private void deleteReplicateById(int replicateId, CacheHashMap cache, HashSet<CacheKey> result) {
		for (CacheKey cacheKey : cache.keySet()) {
			if (!(cacheKey instanceof ReplicateListCacheKey)) {
				continue;
			}

			ReplicateList oldReplicateList = (ReplicateList) cache.get(cacheKey);

			if (oldReplicateList.containsKey(replicateId)) {
				ReplicateList newReplicateList = new ReplicateList(oldReplicateList);
				newReplicateList.putAll(oldReplicateList);
				newReplicateList.remove(replicateId);

				CacheKey oldCacheKey = cache.update(cacheKey, newReplicateList);
				result.add(oldCacheKey);
			}
		}
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof InputCacheReplicateListGetListener) {
			((InputCacheReplicateListGetListener) listener).replicateListUpdated(commandId, (ReplicateList) updated);
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
