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

package org.easotope.shared.rawdata.cache.input.scanlist;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.commands.ScanListGet;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.events.ScanDeleted;
import org.easotope.shared.rawdata.events.ScanUpdated;

public class ScanListPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new ScanListCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		ScanListGet scanListGet = new ScanListGet();

		scanListGet.setMassSpecId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(scanListGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return scanListGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		ScanListGet scanListGet = (ScanListGet) command;

		int massSpecId = scanListGet.getMassSpecId();

		cache.put(new ScanListCacheKey(massSpecId), this, callParameters, scanListGet.getScanList());
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof InputCacheScanListGetListener) {
			((InputCacheScanListGetListener) listener).scanListGetCompleted(commandId, (ScanList) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheScanListGetListener) {
			((InputCacheScanListGetListener) listener).scanListGetError(commandId, message);
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

		if (event instanceof ScanUpdated) {
			ScanUpdated scanUpdated = (ScanUpdated) event;

			int scanId = scanUpdated.getScanId();
			long scanDate = scanUpdated.getDate();
			int scanMassSpecId = scanUpdated.getMassSpecId();
			int scanUserId = scanUpdated.getUserId();

			for (CacheKey cacheKey : cache.keySet()) {
				if (!(cacheKey instanceof ScanListCacheKey)) {
					continue;
				}

				ScanList oldScanList = (ScanList) cache.get(cacheKey);

				boolean listShouldContainScan = true;

				if (oldScanList.getMassSpecId() != DatabaseConstants.EMPTY_DB_ID && oldScanList.getMassSpecId() != scanMassSpecId) {
					listShouldContainScan = false;
				}

				if (oldScanList.containsKey(scanId) && listShouldContainScan) {
					ScanList newScanList = new ScanList(oldScanList);
					newScanList.putAll(oldScanList);

					newScanList.remove(scanId);

					ScanListItem newScanListItem = new ScanListItem(scanDate, scanUserId, scanId);
					newScanList.put(scanId, newScanListItem);

					CacheKey oldCacheKey = cache.update(cacheKey, newScanList);
					result.add(oldCacheKey);
				}

				if (oldScanList.containsKey(scanId) && !listShouldContainScan) {
					ScanList newScanList = new ScanList(oldScanList);
					newScanList.putAll(oldScanList);

					newScanList.remove(scanId);

					CacheKey oldCacheKey = cache.update(cacheKey, newScanList);
					result.add(oldCacheKey);
				}

				if (!oldScanList.containsKey(scanId) && listShouldContainScan) {
					ScanList newScanList = new ScanList(oldScanList);
					newScanList.putAll(oldScanList);

					ScanListItem newScanListItem = new ScanListItem(scanDate, scanUserId, scanId);
					newScanList.put(scanId, newScanListItem);

					CacheKey oldCacheKey = cache.update(cacheKey, newScanList);
					result.add(oldCacheKey);
				}
			}

		} else if (event instanceof ScanDeleted) {
			ScanDeleted scanDeleted = (ScanDeleted) event;

			for (CacheKey cacheKey : cache.keySet()) {
				if (!(cacheKey instanceof ScanListCacheKey)) {
					continue;
				}

				ScanList oldScanList = (ScanList) cache.get(cacheKey);

				if (oldScanList.containsKey(scanDeleted.getScanId())) {
					ScanList newScanList = new ScanList(oldScanList);
					newScanList.putAll(oldScanList);

					newScanList.remove(scanDeleted.getScanId());

					CacheKey oldCacheKey = cache.update(cacheKey, newScanList);
					result.add(oldCacheKey);
				}
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof InputCacheScanListGetListener) {
			((InputCacheScanListGetListener) listener).scanListUpdated(commandId, (ScanList) updated);
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
