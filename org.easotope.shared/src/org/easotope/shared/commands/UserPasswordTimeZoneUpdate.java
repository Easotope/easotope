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

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.Messages;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.events.UserUpdated;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.rawdata.tables.Project;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;

public class UserPasswordTimeZoneUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private int userId = -1;
	private String password = null;
	private String timeZoneId = null;
	private boolean checkForUpdates;
	private boolean showTimeZone;
	private boolean leadingExponent;
	private boolean forceExponent;

	private User user = null;
	private Permissions permissions = null;
	private Preferences preferences = null;

	public UserPasswordTimeZoneUpdate(int userId, String password, String timeZoneId, boolean showTimeZone, boolean leadingExponent, boolean forceExponent, boolean checkForUpdates) {
		this.userId = userId;
		this.password = password;
		this.timeZoneId = timeZoneId;
		this.checkForUpdates = checkForUpdates;
		this.showTimeZone = showTimeZone;
		this.leadingExponent = leadingExponent;
		this.forceExponent = forceExponent;
	}

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		User destUser = (User) authenticationObjects.get(AuthenticationKeys.USER);
		return destUser.id == userId;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<User,Integer> userDao = DaoManager.createDao(connectionSource, User.class);
        user = userDao.queryForId(userId);

        if (user == null) {
        		setStatus(Command.Status.EXECUTION_ERROR, MessageFormat.format(Messages.userPasswordUpdate_userDoesNotExist, userId));
        		return;
        }

        permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);

		Dao<Preferences,Integer> prefsDao = DaoManager.createDao(connectionSource, Preferences.class);
        List<Preferences> result = prefsDao.queryForEq(Preferences.USERID_FIELD_NAME, userId);

        if (result.size() == 0) {
        		setStatus(Command.Status.EXECUTION_ERROR, MessageFormat.format(Messages.userPasswordUpdate_noUniquePrefs, userId));
        		return;
        }

        preferences = result.get(0);

        if (password != null && password.length() != 0) {
        		user.setPassword(password);
        		userDao.update(user);
        }

        preferences.setTimeZoneId(timeZoneId);
        preferences.setCheckForUpdates(checkForUpdates);
        preferences.setShowTimeZone(showTimeZone);
        preferences.setLeadingExponent(leadingExponent);
        preferences.setForceExponent(forceExponent);

        prefsDao.update(preferences);

		GenericRawResults<User> rawResults = userDao.queryRaw(
	         "select id,username from " + User.TABLE_NAME + " WHERE " + User.ID_FIELD_NAME + "=" + user.getId() + " AND " + 
	         "EXISTS (select * from " + Project.TABLE_NAME + " where " + User.TABLE_NAME + "." + User.ID_FIELD_NAME + " = " + Project.TABLE_NAME + "." + Project.USER_ID_FIELD_NAME + ")",
	         userDao.getRawRowMapper());

		boolean hasChildren = (rawResults.getResults().size() != 0);

        addEvent(new UserUpdated(user, permissions, preferences, hasChildren));
	}

	public int getUserId() {
		return userId;
	}

	public User getUser() {
		return user;
	}

	public Permissions getPermissions() {
		return permissions;
	}

	public Preferences getPreferences() {
		return preferences;
	}
}
