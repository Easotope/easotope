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

package org.easotope.shared.commands;

import java.util.Hashtable;

import org.easotope.framework.commands.UserGet;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class UserPermsPrefsGet extends UserGet {
	private static final long serialVersionUID = 1L;

	private Permissions permission = null;
	private Preferences preference = null;

	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		super.execute(connectionSource, rawFileManager, authenticationObjects);

		if (getStatus() != Status.OK) {
			return;
		}

		Dao<Permissions,Integer> permsDao = DaoManager.createDao(connectionSource, Permissions.class);
		permission = permsDao.queryForId(getUserId());

		Dao<Preferences,Integer> prefsDao = DaoManager.createDao(connectionSource, Preferences.class);
		preference = prefsDao.queryForId(getUserId());
	}

	public Permissions getPermissions() {
		return permission;
	}

	public Preferences getPreferences() {
		return preference;
	}
}
