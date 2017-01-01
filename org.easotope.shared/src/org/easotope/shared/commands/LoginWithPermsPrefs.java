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

package org.easotope.shared.commands;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.commands.Login;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.core.tables.TableLayout;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class LoginWithPermsPrefs extends Login {
	private static final long serialVersionUID = 1L;

	private Permissions permissions = null;
	private Preferences preferences = null;
	private ArrayList<TableLayout> tableLayouts = null;

	@Override
	public void updateAuthenticationObjects(Hashtable<String,Object> authenticationObjects) {
		super.updateAuthenticationObjects(authenticationObjects);
		
		if (permissions != null) {
			authenticationObjects.put(AuthenticationKeys.PERMISSIONS, permissions);
		}
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception{
		super.execute(connectionSource, 	rawFileManager, authenticationObjects);

		if (getStatus() != Status.OK || getUser() == null) {
			return;
		}

		Dao<Permissions, Integer> permissionsDao = DaoManager.createDao(connectionSource, Permissions.class);
		List<Permissions> permissionList = permissionsDao.queryForEq(Permissions.USERID_FIELD_NAME, getUser().getId());
		
		if (permissionList.size() != 1) {
			setStatus(Command.Status.DB_ERROR, Messages.loginWithPermissions_noPermissions, new Object[] { getUser().getId() } );
			return;
		}

		permissions = permissionList.get(0);

		Dao<Preferences, Integer> preferencesDao = DaoManager.createDao(connectionSource, Preferences.class);
		List<Preferences> preferencesList = preferencesDao.queryForEq(Preferences.USERID_FIELD_NAME, getUser().getId());

		if (preferencesList.size() != 1) {
			setStatus(Command.Status.DB_ERROR, Messages.loginWithPermissions_noPreferences, new Object[] { getUser().getId() } );
			return;
		}

		preferences = preferencesList.get(0);

		Dao<TableLayout,Integer> tableLayoutDao = DaoManager.createDao(connectionSource, TableLayout.class);
		List<TableLayout> tableLayoutList = tableLayoutDao.queryForEq(TableLayout.USERID_FIELD_NAME, getUser().getId());

		tableLayouts = new ArrayList<TableLayout>(tableLayoutList);
	}

	public Permissions getPermissions() {
		return permissions;
	}
	
	public Preferences getPreferences() {
		return preferences;
	}

	public ArrayList<TableLayout> getTableLayouts() {
		return tableLayouts;
	}
}
