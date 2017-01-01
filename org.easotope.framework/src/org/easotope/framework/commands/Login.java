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

package org.easotope.framework.commands;

import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.dbcore.AuthenticationKeys;
import org.easotope.framework.dbcore.cmdprocessors.CommandThatDoesNotRequireInitializedDb;
import org.easotope.framework.dbcore.cmdprocessors.CommandThatSetsAuthenticationObjects;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.tables.Version;
import org.easotope.framework.dbcore.util.RawFileManager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;

public class Login extends Command implements CommandThatSetsAuthenticationObjects, CommandThatDoesNotRequireInitializedDb {
	private static final long serialVersionUID = 1L;

	private String username = null;
	private String password = null;
	private boolean databaseIsNotInitialized = false;
	private User user = null;

	@Override
	public String getName() {
		String displayedPassword = (password == null || password.length() == 0) ? "" : "******";
		return getClass().getSimpleName() + "(username=" + username + ",password=" + displayedPassword + ")";
	}

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		return true;
	}
	
	@Override
	public void updateAuthenticationObjects(Hashtable<String, Object> authenticationObjects) {
		if (user != null) {
			authenticationObjects.put(AuthenticationKeys.USER, user);
		}
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<Version,Integer> versionDao = DaoManager.createDao(connectionSource, Version.class);

		if (!versionDao.isTableExists()) {
			databaseIsNotInitialized = true;
			return;
		}

		Dao<User, Integer> userDao = DaoManager.createDao(connectionSource, User.class);
		List<User> users = userDao.queryForEq(User.USERNAME_FIELD_NAME, new SelectArg(username));

		if (users.size() == 1) {
			User potentialUser = users.get(0);

			if (!potentialUser.getIsDisabled() && potentialUser.passwordMatches(password)) {
				user = potentialUser;
			}
		}
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public boolean getDatabaseIsNotInitialized() {
		return databaseIsNotInitialized;
	}

	public User getUser() {
		return user;
	}
}
