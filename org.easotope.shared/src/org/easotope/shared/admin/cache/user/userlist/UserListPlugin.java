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

package org.easotope.shared.admin.cache.user.userlist;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.commands.UserListGet;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.events.UserNameUpdated;
import org.easotope.shared.rawdata.events.ProjectDeleted;
import org.easotope.shared.rawdata.events.ProjectUpdated;
import org.easotope.shared.rawdata.tables.Project;

public class UserListPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new UserListCacheKey();
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		Command command = new UserListGet();
		
		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(command, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return command.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		UserListGet userListGet = (UserListGet) command;
		cache.put(new UserListCacheKey(), this, callParameters, userListGet.getUserList());
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof UserCacheUserListGetListener) {
			((UserCacheUserListGetListener) listener).userListGetCompleted(commandId, (UserList) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof UserCacheUserListGetListener) {
			((UserCacheUserListGetListener) listener).userListGetError(commandId, message);
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

		if (event instanceof UserNameUpdated) {
			UserNameUpdated userNameUpdated = (UserNameUpdated) event;

			int id = userNameUpdated.getUserId();
			String name = userNameUpdated.getUsername();
			boolean hasChildren = userNameUpdated.hasChildren();

			CacheKey cacheKey = new UserListCacheKey();

			if (cache.containsKey(cacheKey)) {
				UserList userList = (UserList) cache.get(cacheKey);

				UserList userListCopy = new UserList();
				userListCopy.putAll(userList);
				userListCopy.put(id, new UserListItem(name, hasChildren));

				CacheKey oldCacheKey = cache.update(cacheKey, userListCopy);
				result.add(oldCacheKey);
			}

		} if (event instanceof ProjectUpdated) {
			ProjectUpdated projectUpdated = (ProjectUpdated) event;

			Project project = projectUpdated.getProject();
			int oldUserId = projectUpdated.getOldUserId();
			boolean oldUserIdHasChildren = projectUpdated.getOldUserHasChildren();
			int newUserId = project.getUserId();

			CacheKey cacheKey = new UserListCacheKey();

			if (cache.containsKey(cacheKey)) {
				UserList oldUserList = (UserList) cache.get(cacheKey);
				UserList newUserList = null;

				if (oldUserId != DatabaseConstants.EMPTY_DB_ID && oldUserId != newUserId) {
					if (oldUserList.containsKey(oldUserId)) {
						String oldUserName = oldUserList.get(oldUserId).getName();

						newUserList = new UserList();
						newUserList.putAll(oldUserList);
						newUserList.put(oldUserId, new UserListItem(oldUserName, oldUserIdHasChildren));
					}
				}

				if (oldUserList.containsKey(newUserId)) {
					String oldUserName = oldUserList.get(newUserId).getName();

					if (newUserList == null) {
						newUserList = new UserList();
						newUserList.putAll(oldUserList);
					}

					newUserList.put(project.getUserId(), new UserListItem(oldUserName, true));
				}

				if (newUserList != null) {
					CacheKey oldCacheKey = cache.update(cacheKey, newUserList);
					result.add(oldCacheKey);
				}
			}

		} if (event instanceof ProjectDeleted) {
			ProjectDeleted projectDeleted = (ProjectDeleted) event;

			int oldUserId = projectDeleted.getUserId();
			boolean oldUserHasChildren = projectDeleted.getUserHasChildren();

			CacheKey cacheKey = new UserListCacheKey();

			if (cache.containsKey(cacheKey)) {
				UserList oldUserList = (UserList) cache.get(cacheKey);

				if (oldUserList.containsKey(oldUserId)) {
					String oldUserName = oldUserList.get(oldUserId).getName();

					UserList newUserList = new UserList();
					newUserList.putAll(oldUserList);
					newUserList.put(oldUserId, new UserListItem(oldUserName, oldUserHasChildren));

					CacheKey oldCacheKey = cache.update(cacheKey, newUserList);
					result.add(oldCacheKey);
				}
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof UserCacheUserListGetListener) {
			((UserCacheUserListGetListener) listener).userListUpdated(commandId, (UserList) updated);
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
