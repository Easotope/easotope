/*
 * Copyright © 2016-2018 by Devon Bowen.
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

package org.easotope.shared.core.events;

import java.util.Hashtable;

import org.easotope.framework.dbcore.events.CoreUserUpdated;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;

public class UserUpdated extends CoreUserUpdated {
	private static final long serialVersionUID = 1L;

	private Permissions permissions;
	private Preferences preferences;
	private boolean hasChildren;

	public UserUpdated(User user, Permissions permissions, Preferences preferences, boolean hasChildren) {
		super(user);
		this.permissions = permissions;
		this.preferences = preferences;
		this.hasChildren = hasChildren;
	}

	@Override
	public void updateAuthenticationObjects(Hashtable<String, Object> authenticationObjects) {
		super.updateAuthenticationObjects(authenticationObjects);

		if (permissions != null) {
			Permissions authPerms = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);

			if (authPerms == null || authPerms.getUserId() == permissions.getUserId()) {
				authenticationObjects.put(AuthenticationKeys.PERMISSIONS, permissions);
			}
		}
	}

	public Permissions getPermissions() {
		return permissions;
	}

	public Preferences getPreferences() {
		return preferences;
	}
	
	public boolean hasChildren() {
		return hasChildren;
	}
}
