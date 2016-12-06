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

package org.easotope.shared.commands;

import java.util.Hashtable;

import org.easotope.framework.commands.Command;
import org.easotope.framework.commands.CoreUserUpdate;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.events.CoreUserNameUpdated;
import org.easotope.framework.dbcore.events.CoreUserUpdated;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.core.events.UserNameUpdated;
import org.easotope.shared.core.events.UserUpdated;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.rawdata.tables.Project;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;

public class UserUpdate extends CoreUserUpdate {
	private static final long serialVersionUID = 1L;

	private Permissions permissions = null;
	private Preferences preferences = null;

	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		boolean isAdd = user.getId() == DatabaseConstants.EMPTY_DB_ID;
		
		super.execute(connectionSource, rawFileManager, authenticationObjects);
		removeEvent(CoreUserUpdated.class);

		boolean hasChildren = false;
		
		if (getStatus() != Status.OK) {
			return;
		}

		if (isAdd) {
			Dao<Permissions, Integer> permissionsDao = DaoManager.createDao(connectionSource, Permissions.class);

			if (permissionsDao.queryForEq(Permissions.USERID_FIELD_NAME, getUser().getId()).size() != 0) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.userPermsPrefsUpdate_permissionsExists, new Object[] { getUser().getId() } );
				return;
			}

			Dao<Preferences, Integer> preferencesDao = DaoManager.createDao(connectionSource, Preferences.class);

			if (preferencesDao.queryForEq(Preferences.USERID_FIELD_NAME, getUser().getId()).size() != 0) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.userPermsPrefsUpdate_preferencesExists, new Object[] { getUser().getId() } );
				return;
			}

			if (permissions == null) {
				permissions = new Permissions();
			}

			permissions.setUserId(getUser().getId());
			permissionsDao.create(permissions);

			if (preferences == null) {
				preferences = new Preferences();
			}

			preferences.setUserId(getUser().getId());
			preferencesDao.create(preferences);

		} else {
			Dao<Permissions, Integer> permissionsDao = DaoManager.createDao(connectionSource, Permissions.class);
			Dao<Preferences, Integer> preferencesDao = DaoManager.createDao(connectionSource, Preferences.class);

			if (permissions != null) {
				if (permissions.getUserId() != getUser().getId()) {
					setStatus(Command.Status.EXECUTION_ERROR, Messages.userPermsPrefsUpdate_permIdDoesntMatch, new Object[] { getUser().getId(), permissions.getId() } );
					return;
				}
				
				if (permissionsDao.queryForId(permissions.getId()) == null) {
					setStatus(Command.Status.EXECUTION_ERROR, Messages.userPermsPrefsUpdate_permsDoNotExist, new Object[] { permissions.getUserId() } );
					return;
				}
			}

			if (preferences != null) {
				if (preferences.getUserId() != getUser().getId()) {
					setStatus(Command.Status.EXECUTION_ERROR, Messages.userPermsPrefsUpdate_prefIdDoesntMatch, new Object[] { getUser().getId(), preferences.getId() } );
					return;
				}

				if (preferencesDao.queryForId(preferences.getId()) == null) {
					setStatus(Command.Status.EXECUTION_ERROR, Messages.userPermsPrefsUpdate_prefsDoNotExist, new Object[] { permissions.getUserId() } );
					return;
				}
			}

			if (permissions != null) {
				permissionsDao.update(permissions);
			}

			if (preferences != null) {
				preferencesDao.update(preferences);
			}

			Dao<User,Integer> userDao = DaoManager.createDao(connectionSource, User.class);

			GenericRawResults<User> rawResults = userDao.queryRaw(
			         "select " + User.USERNAME_FIELD_NAME + " from " + User.TABLE_NAME + " WHERE " + User.ID_FIELD_NAME + "=" + user.getId() + " AND " + 
			         "EXISTS (select * from " + Project.TABLE_NAME + " where " + User.TABLE_NAME + "." + User.ID_FIELD_NAME + " = " + Project.TABLE_NAME + "." + Project.USER_ID_FIELD_NAME + ")",
			         userDao.getRawRowMapper());

			hasChildren = (rawResults.getResults().size() != 0);
		}

		removeEvent(CoreUserUpdated.class);
		addEvent(new UserUpdated(getUser(), permissions, preferences, hasChildren));

		removeEvent(CoreUserNameUpdated.class);
		addEvent(new UserNameUpdated(getUser().getId(), getUser().getUsername(), hasChildren));
	}

	public void setPermissions(Permissions permissions) {
		this.permissions = permissions;
	}

	public Permissions getPermissions() {
		return permissions;
	}
	
	public void setPreferences(Preferences preferences) {
		this.preferences = preferences;
	}

	public Preferences getPreferences() {
		return preferences;
	}
}
