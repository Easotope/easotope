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

package org.easotope.shared.core.cache.logininfo.logininfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.commands.UserPasswordTimeZoneUpdate;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.events.TableLayoutUpdated;
import org.easotope.shared.core.events.UserUpdated;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.core.tables.TableLayout;

public class LoginInfoPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new LoginInfoCacheKey();
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		return Command.UNDEFINED_ID;
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		assert(false);
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		assert(false);
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		assert(false);
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
		HashSet<CacheKey> results = new HashSet<CacheKey>();

		CacheKey cacheKey = new LoginInfoCacheKey();

		if (cache.containsKey(cacheKey)) {
			Object[] objectArray = (Object[]) cache.get(cacheKey);
	
			User currentUser = (User) objectArray[0];
			Permissions currentPermissions = (Permissions) objectArray[1];
			Preferences currentPreferences = (Preferences) objectArray[2];
			@SuppressWarnings("unchecked")
			ArrayList<TableLayout> currentTableLayouts = (ArrayList<TableLayout>) objectArray[3];
	
			if (event instanceof UserUpdated) {
				UserUpdated userUpdated = (UserUpdated) event;
				User newUser = userUpdated.getUser();
	
				if (currentUser.getId() == newUser.getId()) {
					Permissions newPermissions = userUpdated.getPermissions() != null ? userUpdated.getPermissions() : currentPermissions;
					Preferences newPreferences = userUpdated.getPreferences() != null ? userUpdated.getPreferences() : currentPreferences;
	
					CacheKey oldCacheKey = cache.update(cacheKey, new Object[] { newUser, newPermissions, newPreferences, currentTableLayouts });
					results.add(oldCacheKey);
				}

			} else if (event instanceof TableLayoutUpdated) {
				TableLayoutUpdated tableLayoutUpdated = (TableLayoutUpdated) event;
				TableLayout newTableLayout = tableLayoutUpdated.getTableLayout();

				if (currentUser.getId() == newTableLayout.getUserId()) {
					ArrayList<TableLayout> newTableLayouts = new ArrayList<TableLayout>();
					boolean newHasBeenAdded = false;

					for (TableLayout oldTableLayout : currentTableLayouts) {
						if (oldTableLayout.getDataAnalysisId() == newTableLayout.getDataAnalysisId() && newTableLayout.getContext().equals(oldTableLayout.getContext())) {
							newTableLayouts.add(newTableLayout);
							newHasBeenAdded = true;
						} else {
							newTableLayouts.add(oldTableLayout);
						}
					}

					if (!newHasBeenAdded) {
						newTableLayouts.add(newTableLayout);
					}

					CacheKey oldCacheKey = cache.update(cacheKey, new Object[] { currentUser, currentPermissions, currentPreferences, newTableLayouts });
					results.add(oldCacheKey);
				}
			}
		}

		return results;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof LoginInfoCacheLoginInfoGetListener) {
			Object[] objectArray = (Object[]) updated;

			User user = (User) objectArray[0];
			Permissions permissions = (Permissions) objectArray[1];
			Preferences preferences = (Preferences) objectArray[2];
			
			((LoginInfoCacheLoginInfoGetListener) listener).loginInfoUpdated(commandId, user, permissions, preferences);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		Hashtable<String,Object> authenticationObjects = LoginInfoCache.getInstance().getAuthenticationObjects();
		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);

		String password = (String) parameters[0];
		String timeZoneId = (String) parameters[1];
		boolean checkForUpdates = (Boolean) parameters[2];
		boolean showTimeZone = (Boolean) parameters[3];
		boolean leadingExponent = (Boolean) parameters[4];
		boolean forceExponent = (Boolean) parameters[5];

		UserPasswordTimeZoneUpdate userPasswordTimeZoneUpdate = new UserPasswordTimeZoneUpdate(user.getId(), password, timeZoneId, showTimeZone, leadingExponent, forceExponent, checkForUpdates);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(userPasswordTimeZoneUpdate, authenticationObjects, abstractCache);

		return userPasswordTimeZoneUpdate.getClientUniqueId();
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		if (listener instanceof LoginInfoCacheLoginInfoSaveListener) {
			UserPasswordTimeZoneUpdate userPasswordTimeZoneUpdate = (UserPasswordTimeZoneUpdate) command;

			User user = userPasswordTimeZoneUpdate.getUser();
			Permissions permissions = userPasswordTimeZoneUpdate.getPermissions();
			Preferences preferences = userPasswordTimeZoneUpdate.getPreferences();

			((LoginInfoCacheLoginInfoSaveListener) listener).loginInfoSaveCompleted(command.getClientUniqueId(), user, permissions, preferences);
		}
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		if (listener instanceof LoginInfoCacheLoginInfoSaveListener) {
			((LoginInfoCacheLoginInfoSaveListener) listener).loginInfoSaveError(commandId, message);
		}
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
