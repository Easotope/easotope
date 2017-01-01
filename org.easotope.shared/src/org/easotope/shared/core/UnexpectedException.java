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

package org.easotope.shared.core;

import java.text.MessageFormat;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.Reflection;
import org.easotope.shared.Messages;

public class UnexpectedException {
	private static final String className = "org.easotope.client.core.PotentialGraphicsMethods";
	private static final String methodName = "reportErrorToUser";

	public static void reportErrorToUser(Object displaySource, Throwable t) {
		if (displaySource == null || t == null) {
			return;
		}

		Object object = null;

		try {
			object = Reflection.createObject(className);
		} catch (RuntimeException e) {
			String message = MessageFormat.format(Messages.unexpectedException_couldNotInstantiateGraphicObject, className);
			Log.getInstance().log(Level.INFO, UnexpectedException.class, message, t);
			return;
		}

		try {
			Reflection.callMethod(object, methodName, displaySource, t);
		} catch (RuntimeException e) {
			e.printStackTrace();
			String message = MessageFormat.format(Messages.unexpectedException_couldNotCallGraphicObject, methodName, className);
			Log.getInstance().log(Level.INFO, UnexpectedException.class, message, t);
			return;
		}
	}
}
