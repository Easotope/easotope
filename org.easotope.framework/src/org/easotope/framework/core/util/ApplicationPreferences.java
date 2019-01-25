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

package org.easotope.framework.core.util;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class ApplicationPreferences {
	public final static String NODE_NAME = SystemProperty.getPrefNodeName();
	private static Preferences prefs = Preferences.userRoot().node(NODE_NAME);

	public enum Key { DbType, Param1, Param2, LastUser };

	public synchronized static String get(Key key) {
		return prefs.get(key.toString(), null);
	}

	public synchronized static void put(Key key, String value) {
		prefs.put(key.toString(), value);
	}

	public synchronized static void clear() {
		try {
			prefs.clear();
		} catch (BackingStoreException e) {
			// do nothing
		}
	}

	public synchronized static void flush() {
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			// do nothing
		}
	}
}
