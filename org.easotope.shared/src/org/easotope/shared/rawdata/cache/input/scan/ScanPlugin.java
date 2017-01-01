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

package org.easotope.shared.rawdata.cache.input.scan;

import java.util.ArrayList;
import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.commands.ScanDelete;
import org.easotope.shared.commands.ScanGet;
import org.easotope.shared.commands.ScanUpdate;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.ScanFile;
import org.easotope.shared.rawdata.events.ScanDeleted;
import org.easotope.shared.rawdata.events.ScanUpdated;
import org.easotope.shared.rawdata.tables.ScanV2;

public class ScanPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new ScanCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		ScanGet scanGet = new ScanGet();
		scanGet.setScanId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(scanGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return scanGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		ScanGet scanGet = (ScanGet) command;

		ScanV2 scan = scanGet.getScan();
		ArrayList<ScanFile> scanFiles = scanGet.getScanFiles();

		cache.put(new ScanCacheKey(scan.getId()), this, callParameters, new Object[] { scan, scanFiles });
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof InputCacheScanGetListener) {
			Object[] resultArray = (Object[]) result;

			@SuppressWarnings("unchecked")
			ArrayList<ScanFile> scanFiles = (ArrayList<ScanFile>) resultArray[1];

			((InputCacheScanGetListener) listener).scanGetCompleted(commandId, (ScanV2) resultArray[0], scanFiles);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheScanGetListener) {
			((InputCacheScanGetListener) listener).scanGetError(commandId, message);
		}
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof ScanDeleted) {
			ScanDeleted scanDeleted = (ScanDeleted) event;
			ScanCacheKey scanCacheKey = new ScanCacheKey(scanDeleted.getScanId());
			CacheKey oldCacheKey = cache.getKey(scanCacheKey);

			if (oldCacheKey != null) {
				result.add(oldCacheKey);
			}
		}
		
		return result;
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatNeedReloadBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof ScanUpdated) {
			ScanUpdated scanUpdated = (ScanUpdated) event;
			ScanCacheKey cacheKey = new ScanCacheKey(scanUpdated.getScanId());
			CacheKey oldCacheKey = cache.getKey(cacheKey);

			if (oldCacheKey != null) {
				result.add(oldCacheKey);
			}
		}

		return result;
	}

	@Override
	public HashSet<CacheKey> updateCacheBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof InputCacheScanGetListener) {
			Object[] updatedArray = (Object[]) updated;

			@SuppressWarnings("unchecked")
			ArrayList<ScanFile> scanFileList = (ArrayList<ScanFile>) updatedArray[1];

			((InputCacheScanGetListener) listener).scanUpdated(commandId, (ScanV2) updatedArray[0], scanFileList);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		if (listener instanceof InputCacheScanGetListener) {
			int scanId = ((ScanCacheKey) cacheKey).getScanId();
			((InputCacheScanGetListener) listener).scanDeleted(scanId);
		}
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		ScanUpdate scanUpdate = new ScanUpdate();

		scanUpdate.setScan((ScanV2) parameters[0]);

		@SuppressWarnings("unchecked")
		ArrayList<ScanFile> scanFiles = (ArrayList<ScanFile>) parameters[1];
		scanUpdate.setScanFiles(scanFiles);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(scanUpdate, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return scanUpdate.getClientUniqueId();
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		if (listener instanceof InputCacheScanSaveListener) {
			((InputCacheScanSaveListener) listener).scanSaveCompleted(command.getClientUniqueId());
		}
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheScanSaveListener) {
			((InputCacheScanSaveListener) listener).scanSaveError(commandId, message);
		}
	}

	@Override
	public int deleteData(AbstractCache abstractCache, Object[] parameters) {
		ScanDelete scanDelete = new ScanDelete();

		scanDelete.setScanId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(scanDelete, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return scanDelete.getClientUniqueId();
	}

	@Override
	public void callbackDeleteCompleted(Object listener, Command command) {
		if (listener instanceof InputCacheScanSaveListener) {
			((InputCacheScanSaveListener) listener).scanDeleteCompleted(command.getClientUniqueId());
		}
	}

	@Override
	public void callbackDeleteError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheScanSaveListener) {
			((InputCacheScanSaveListener) listener).scanDeleteError(commandId, message);
		}
	}
}
