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

package org.easotope.shared.core.cache.logininfo;

import java.util.ArrayList;
import java.util.Hashtable;

import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.commands.PreviousBestServerUpdate;
import org.easotope.shared.commands.TableLayoutUpdate;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheListener;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheKey;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoPlugin;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.core.tables.TableLayout;

public class LoginInfoCache extends AbstractCache {
	private static LoginInfoCache instance = new LoginInfoCache();

	private LoginInfoPlugin loginInfoPlugin = new LoginInfoPlugin();

	public static LoginInfoCache getInstance() {
		return instance;
	}

	public LoginInfoCache() {
		addPlugin(loginInfoPlugin);
	}

	public synchronized int savePreferences(String password, String timeZoneId, Boolean checkForUpdates, Boolean showTimeZone, Boolean leadingExponent, Boolean forceExponent, int lidi2RefRange, int exportPadding, CacheListener listener) {
		return saveObject(loginInfoPlugin, listener, password, timeZoneId, checkForUpdates, showTimeZone, leadingExponent, forceExponent, lidi2RefRange, exportPadding);
	}

	public synchronized void saveTableLayout(TableLayout tableLayout) {
		TableLayoutUpdate tableLayoutUpdate = new TableLayoutUpdate(tableLayout);
		Hashtable<String,Object> authenticationObjects = LoginInfoCache.getInstance().getAuthenticationObjects();
		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(tableLayoutUpdate, authenticationObjects, null);
	}

	public synchronized void savePreviousBestServer(int bestServer) {
		int userId = LoginInfoCache.getInstance().getUser().getId();
		PreviousBestServerUpdate previousBestServerUpdate = new PreviousBestServerUpdate(userId, bestServer);
		Hashtable<String,Object> authenticationObjects = LoginInfoCache.getInstance().getAuthenticationObjects();
		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(previousBestServerUpdate, authenticationObjects, null);
	}

	// the following are non-standard AbstractCache methods specific to LoginInfoCache

	public synchronized void setLoginInfo(User currentUser, Permissions currentPermissions, Preferences currentPreferences, ArrayList<TableLayout> tableLayouts) {
		getCache().put(new LoginInfoCacheKey(), null, null, new Object[] { currentUser, currentPermissions, currentPreferences, tableLayouts });
	}

	public synchronized User getUser() {
		Object[] object = (Object[]) getCache().get(new LoginInfoCacheKey());
		return object == null ? null : (User) object[0];
	}

	public synchronized Permissions getPermissions() {
		Object[] object = (Object[]) getCache().get(new LoginInfoCacheKey());
		return object == null ? null : (Permissions) object[1];
	}

	public synchronized Preferences getPreferences() {
		Object[] object = (Object[]) getCache().get(new LoginInfoCacheKey());
		return object == null ? null : (Preferences) object[2];
	}

	public synchronized TableLayout getTableLayout(int dataAnalysisId, String context) {
		Object[] object = (Object[]) getCache().get(new LoginInfoCacheKey());
		@SuppressWarnings("unchecked")
		ArrayList<TableLayout> tableLayouts = (ArrayList<TableLayout>) object[3];

		for (TableLayout tableLayout : tableLayouts) {
			if (tableLayout.getDataAnalysisId() == dataAnalysisId && context.equals(tableLayout.getContext())) {
				return tableLayout;
			}
		}

		return null;
	}

	public synchronized Hashtable<String,Object> getAuthenticationObjects() {
		Object[] object = (Object[]) getCache().get(new LoginInfoCacheKey());

		if (object == null) {
			return null;
		}

		Hashtable<String,Object> authenticationObjects = new Hashtable<String,Object>();
		authenticationObjects.put(AuthenticationKeys.USER, (User) object[0]);
		authenticationObjects.put(AuthenticationKeys.PERMISSIONS, (Permissions) object[1]);

		return authenticationObjects;
	}
}
