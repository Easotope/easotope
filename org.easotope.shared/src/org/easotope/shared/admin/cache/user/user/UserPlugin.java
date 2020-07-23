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

package org.easotope.shared.admin.cache.user.user;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.commands.UserPermsPrefsGet;
import org.easotope.shared.commands.UserUpdate;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.events.UserUpdated;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;

public class UserPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new UserCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		UserPermsPrefsGet userPermsPrefsGet = new UserPermsPrefsGet();
		userPermsPrefsGet.setUserId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(userPermsPrefsGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return userPermsPrefsGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		UserPermsPrefsGet userPermsPrefsGet = (UserPermsPrefsGet) command;
		User user = userPermsPrefsGet.getUser();
		Permissions permissions = userPermsPrefsGet.getPermissions();
		Preferences preferences = userPermsPrefsGet.getPreferences();

		cache.put(new UserCacheKey(user.getId()), this, callParameters, new Object[] { user, permissions, preferences });
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof UserCacheUserGetListener) {
			Object[] resultArray = (Object[]) result;
			((UserCacheUserGetListener) listener).userGetCompleted(commandId, (User) resultArray[0], (Permissions) resultArray[1], (Preferences) resultArray[2]);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof UserCacheUserGetListener) {
			((UserCacheUserGetListener) listener).userGetError(commandId, message);
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

		if (event instanceof UserUpdated) {
			UserUpdated userUpdated = (UserUpdated) event;

			User user = userUpdated.getUser();
			Permissions permissions = userUpdated.getPermissions();
			Preferences preferences = userUpdated.getPreferences();

			CacheKey cacheKey = new UserCacheKey(user.getId());

			if (cache.containsKey(cacheKey)) {
				CacheKey oldCacheKey = cache.update(cacheKey, new Object[] { user, permissions, preferences });
				result.add(oldCacheKey);
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof UserCacheUserGetListener) {
			Object[] updatedArray = (Object[]) updated;
			((UserCacheUserGetListener) listener).userUpdated(commandId, (User) updatedArray[0], (Permissions) updatedArray[1], (Preferences) updatedArray[2]);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		UserUpdate userPermsPrefsUpdate = new UserUpdate();
		userPermsPrefsUpdate.setUser((User) parameters[0]);
		userPermsPrefsUpdate.setPermissions((Permissions) parameters[1]);
		userPermsPrefsUpdate.setPreferences((Preferences) parameters[2]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(userPermsPrefsUpdate, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return userPermsPrefsUpdate.getClientUniqueId();
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		if (listener instanceof UserCacheUserSaveListener) {
			UserUpdate userPermsPrefsUpdate = (UserUpdate) command;
			User user = userPermsPrefsUpdate.getUser();
			Permissions permissions = userPermsPrefsUpdate.getPermissions();
			Preferences preferences = userPermsPrefsUpdate.getPreferences();

			((UserCacheUserSaveListener) listener).userSaveCompleted(command.getClientUniqueId(), user, permissions, preferences);
		}
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		if (listener instanceof UserCacheUserSaveListener) {
			((UserCacheUserSaveListener) listener).userSaveError(commandId, message);
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
