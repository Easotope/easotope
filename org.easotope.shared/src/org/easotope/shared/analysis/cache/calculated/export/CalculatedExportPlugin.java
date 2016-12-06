/*
 * Copyright © 2016 by Devon Bowen.
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

package org.easotope.shared.analysis.cache.calculated.export;

import java.util.HashSet;
import java.util.TreeSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.commands.CalculatedExportGet;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class CalculatedExportPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new CalculatedExportCacheKey();
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		CalculatedExportGet calculatedExportGet = new CalculatedExportGet();
		
		@SuppressWarnings("unchecked")
		TreeSet<Integer> userIds = (TreeSet<Integer>) parameters[0];
		@SuppressWarnings("unchecked")
		TreeSet<Integer> projectIds = (TreeSet<Integer>) parameters[1];
		@SuppressWarnings("unchecked")
		TreeSet<Integer> sampleIds = (TreeSet<Integer>) parameters[2];

		calculatedExportGet.addUserIds(userIds);
		calculatedExportGet.addProjectIds(projectIds);
		calculatedExportGet.addSampleIds(sampleIds);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(calculatedExportGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return calculatedExportGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		CalculatedExportGet calculatedExportGet = (CalculatedExportGet) command;
		CalculatedExport calculatedExport = new CalculatedExport(calculatedExportGet);
		cache.put(new CalculatedExportCacheKey(), this, callParameters, calculatedExport);
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof CalculatedCacheCalculatedExportGetListener) {
			((CalculatedCacheCalculatedExportGetListener) listener).calculatedExportGetCompleted(commandId, (CalculatedExport) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof CalculatedCacheCalculatedExportGetListener) {
			((CalculatedCacheCalculatedExportGetListener) listener).calculatedExportGetError(commandId, message);
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
		return null;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		// ignore
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		// ignore
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
