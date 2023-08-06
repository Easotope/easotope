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

package org.easotope.shared.admin.cache.massspec.massspec;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.admin.cache.massspec.refgaslist.RefGasList;
import org.easotope.shared.admin.cache.massspec.refgaslist.RefGasListCacheKey;
import org.easotope.shared.admin.cache.massspec.refgaslist.RefGasListItem;
import org.easotope.shared.admin.events.MassSpecUpdated;
import org.easotope.shared.admin.tables.MassSpec;
import org.easotope.shared.commands.MassSpecGet;
import org.easotope.shared.commands.MassSpecUpdate;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class MassSpecPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		int massSpecId = (Integer) parameters[0];
		return new MassSpecCacheKey(massSpecId);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		MassSpecGet massSpecGet = new MassSpecGet();
		massSpecGet.setMassSpecId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(massSpecGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return massSpecGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		MassSpecGet massSpecGet = (MassSpecGet) command;
		MassSpec massSpec = massSpecGet.getMassSpec();

		MassSpecCacheKey massSpecCacheKey = new MassSpecCacheKey(massSpec.getId());
		cache.put(massSpecCacheKey, this, callParameters, massSpec);

		RefGasList refGasList = new RefGasList();
		refGasList.setMassSpecId(massSpec.getId());

		HashMap<Integer,Long> dates = massSpecGet.getRefGasList();

		for (Integer id : dates.keySet()) {
			refGasList.put(id, new RefGasListItem(dates.get(id)));
		}

		RefGasListCacheKey refGasListCacheKey = new RefGasListCacheKey(massSpec.getId());
		cache.put(refGasListCacheKey, this, callParameters, refGasList);
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof MassSpecCacheMassSpecGetListener) {
			((MassSpecCacheMassSpecGetListener) listener).massSpecGetCompleted(commandId, (MassSpec) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof MassSpecCacheMassSpecGetListener) {
			((MassSpecCacheMassSpecGetListener) listener).massSpecGetError(commandId, message);
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

		if (event instanceof MassSpecUpdated) {
			MassSpecUpdated massSpecUpdated = (MassSpecUpdated) event;
			MassSpec massSpec = massSpecUpdated.getMassSpec();

			CacheKey cacheKey = new MassSpecCacheKey(massSpec.getId());

			if (cache.containsKey(cacheKey)) {
				CacheKey oldCacheKey = cache.update(cacheKey, massSpec);
				result.add(oldCacheKey);
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof MassSpecCacheMassSpecGetListener) {
			((MassSpecCacheMassSpecGetListener) listener).massSpecUpdated(commandId, (MassSpec) updated);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		MassSpecUpdate massSpecUpdate = new MassSpecUpdate();
		massSpecUpdate.setMassSpec((MassSpec) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(massSpecUpdate, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return massSpecUpdate.getClientUniqueId();
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		if (listener instanceof MassSpecCacheMassSpecSaveListener) {
			MassSpecUpdate massSpecUpdate = (MassSpecUpdate) command;
			((MassSpecCacheMassSpecSaveListener) listener).massSpecSaveCompleted(command.getClientUniqueId(), massSpecUpdate.getMassSpec());
		}
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		if (listener instanceof MassSpecCacheMassSpecSaveListener) {
			((MassSpecCacheMassSpecSaveListener) listener).massSpecSaveError(commandId, message);
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
