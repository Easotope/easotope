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
import org.easotope.framework.commands.Initialize;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class InitializeWithPermsPrefs extends Initialize {
	private static final long serialVersionUID = 1L;

	private Permissions permissions = null;
	private Preferences preferences = null;

	@Override
	public void updateAuthenticationObjects(Hashtable<String,Object> authenticationObjects) {
		super.updateAuthenticationObjects(authenticationObjects);
		authenticationObjects.put(AuthenticationKeys.PERMISSIONS, permissions);
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<Permissions,Integer> permissionsDao = DaoManager.createDao(connectionSource, Permissions.class);

		if (permissionsDao.isTableExists()) {
			setStatus(Command.Status.EXECUTION_ERROR, Messages.initializeWithPermsPrefs_permissionsTableExists);
			return;
		}

		Dao<Preferences,Integer> preferencesDao = DaoManager.createDao(connectionSource, Preferences.class);

		if (preferencesDao.isTableExists()) {
			setStatus(Command.Status.EXECUTION_ERROR, Messages.initializeWithPermsPrefs_preferencesTableExists);
			return;
		}

		super.execute(connectionSource, rawFileManager, authenticationObjects);

		if (getStatus() != Status.OK || getUser() == null) {
			return;
		}

		TableUtils.createTable(connectionSource, Permissions.class);

		permissions = new Permissions();
		permissions.setUserId(getUser().getId());
		permissions.setCanEditMassSpecs(true);
		permissions.setCanEditSampleTypes(true);
		permissions.setCanEditStandards(true);
		permissions.setCanEditConstants(true);
		permissions.setCanEditAllReplicates(true);
		permissions.setCanEditCorrIntervals(true);
		permissions.setCanDeleteAll(true);
		permissions.setCanDeleteOwn(true);
		permissionsDao.create(permissions);

		TableUtils.createTable(connectionSource, Preferences.class);

		preferences = new Preferences();
		preferences.setUserId(getUser().getId());
		preferencesDao.create(preferences);
	}
	
	public Permissions getPermissions() {
		return permissions;
	}

	public Preferences getPreferences() {
		return preferences;
	}
}
