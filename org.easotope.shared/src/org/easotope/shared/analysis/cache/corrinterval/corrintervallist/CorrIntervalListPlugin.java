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

package org.easotope.shared.analysis.cache.corrinterval.corrintervallist;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.admin.events.MassSpecUpdated;
import org.easotope.shared.analysis.events.CorrIntervalsUpdated;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.commands.CorrIntervalListGet;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class CorrIntervalListPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new CorrIntervalListCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		CorrIntervalListGet corrIntervalListGet = new CorrIntervalListGet();
		corrIntervalListGet.setMassSpecId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(corrIntervalListGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return corrIntervalListGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		CorrIntervalListGet corrIntervalListGet = (CorrIntervalListGet) command;
		CorrIntervalList corrIntervalList = new CorrIntervalList();
		corrIntervalList.setMassSpecId(corrIntervalListGet.getMassSpecId());

		HashMap<Integer,Long> validFromList = corrIntervalListGet.getValidFromList();
		HashMap<Integer,int[]> selectedDataAnalysisList = corrIntervalListGet.getSelectedDataAnalysisList();
		HashMap<Integer,Integer> batchDelimiterList = corrIntervalListGet.getBatchDelimiterList();
		HashMap<Integer,Integer[]> channelToMZX10List = corrIntervalListGet.getChannelToMZX10List();

		for (Integer id : validFromList.keySet()) {
			corrIntervalList.put(id, new CorrIntervalListItem(validFromList.get(id), selectedDataAnalysisList.get(id), batchDelimiterList.get(id), channelToMZX10List.get(id)));
		}

		cache.put(new CorrIntervalListCacheKey(corrIntervalListGet.getMassSpecId()), this, callParameters, corrIntervalList);
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof CorrIntervalCacheCorrIntervalListGetListener) {
			((CorrIntervalCacheCorrIntervalListGetListener) listener).corrIntervalListGetCompleted(commandId, (CorrIntervalList) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof CorrIntervalCacheCorrIntervalListGetListener) {
			((CorrIntervalCacheCorrIntervalListGetListener) listener).corrIntervalListGetError(commandId, message);
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

			CacheKey cacheKey = new CorrIntervalListCacheKey(corrIntervalsUpdated.getMassSpecId());

			if (cache.containsKey(cacheKey)) {
				CorrIntervalList oldCorrIntervalList = (CorrIntervalList) cache.get(cacheKey);

				CorrIntervalList newCorrIntervalList = new CorrIntervalList();
				newCorrIntervalList.setMassSpecId(corrIntervalsUpdated.getMassSpecId());

				HashSet<Integer> updatedCorrIntervalIds = new HashSet<Integer>();
				for (CorrIntervalV1 corrInterval : corrIntervalsUpdated.getCorrIntervals()) {
					updatedCorrIntervalIds.add(corrInterval.getId());
				}

				for (int oldCorrIntervalListItemId : oldCorrIntervalList.keySet()) {
					if (!updatedCorrIntervalIds.contains(oldCorrIntervalListItemId)) {
						CorrIntervalListItem oldCorrIntervalListItem = oldCorrIntervalList.get(oldCorrIntervalListItemId);
						newCorrIntervalList.put(oldCorrIntervalListItemId, oldCorrIntervalListItem);
					}
				}

				for (CorrIntervalV1 corrInterval : corrIntervalsUpdated.getCorrIntervals()) {
					newCorrIntervalList.put(corrInterval.getId(), new CorrIntervalListItem(corrInterval.getValidFrom(), corrInterval.getDataAnalysis(), corrInterval.getBatchDelimiter(), corrInterval.getChannelToMzX10()));
				}

				CacheKey oldCacheKey = cache.update(cacheKey, newCorrIntervalList);
				result.add(oldCacheKey);
			}

		} else if (event instanceof MassSpecUpdated) {
			MassSpecUpdated massSpecUpdated = (MassSpecUpdated) event;

			if (massSpecUpdated.massSpecIsNew()) {
				int massSpecId = massSpecUpdated.getMassSpec().getId();
				CacheKey cacheKey = new CorrIntervalListCacheKey(massSpecId);

				CorrIntervalList corrIntervalListCopy = new CorrIntervalList();
				corrIntervalListCopy.setMassSpecId(massSpecId);

				cache.put(cacheKey, null, null, corrIntervalListCopy);
				result.add(cacheKey);
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof CorrIntervalCacheCorrIntervalListGetListener) {
			((CorrIntervalCacheCorrIntervalListGetListener) listener).corrIntervalListUpdated(commandId, (CorrIntervalList) updated);
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
