/*
 * Copyright © 2016-2019 by Devon Bowen.
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

package org.easotope.shared.admin.cache.user;

import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.admin.cache.user.user.UserPlugin;
import org.easotope.shared.admin.cache.user.userlist.UserListPlugin;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;

public class UserCache extends AbstractCache {
	private UserListPlugin userListPlugin = new UserListPlugin();
	private UserPlugin userPlugin = new UserPlugin();

	public static UserCache getInstance() {
		return (UserCache) AbstractCache.getCacheInstanceForThisThread(UserCache.class);
	}

	public UserCache() {
		addPlugin(userListPlugin);
		addPlugin(userPlugin);
	}

	public int userListGet(CacheListener listener) {
		return getObject(userListPlugin, listener);
	}

	public int userGet(int userId, CacheListener listener) {
		return getObject(userPlugin, listener, userId);
	}

	public int userSave(User user, Permissions permissions, Preferences preferences, CacheListener listener) {
		return saveObject(userPlugin, listener, user, permissions, preferences);
	}
}
