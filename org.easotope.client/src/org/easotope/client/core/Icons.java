/*
 * Copyright © 2016-2020 by Devon Bowen.
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

package org.easotope.client.core;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class Icons {
	private static boolean triedToLoadError = false;
	private static Image error;

	private static boolean triedToLoadWarning = false;
	private static Image warning;

	private static boolean triedToLoadCalendar = false;
	private static Image calendar;

	private static boolean triedToLoadBlank = false;
	private static Image blank;

	public static Image getError(Display display) {
		if (!triedToLoadError) {
			InputStream inputStream = Icons.class.getClassLoader().getResourceAsStream("images/error.png");
			error = new Image(display, inputStream);

			try {
				inputStream.close();
			} catch (IOException e) {
				// do nothing
			}

			triedToLoadError = true;
		}

		return error;
	}

	public static Image getWarning(Display display) {
		if (!triedToLoadWarning) {
			InputStream inputStream = Icons.class.getClassLoader().getResourceAsStream("images/warning.png");
			warning = new Image(display, inputStream);

			try {
				inputStream.close();
			} catch (IOException e) {
				// do nothing
			}

			triedToLoadWarning = true;
		}

		return warning;
	}

	public static Image getCalendar(Display display) {
		if (!triedToLoadCalendar) {
			InputStream inputStream = Icons.class.getClassLoader().getResourceAsStream("images/calendar.png");
			calendar = new Image(display, inputStream);

			try {
				inputStream.close();
			} catch (IOException e) {
				// do nothing
			}

			triedToLoadCalendar = true;
		}
		
		return calendar;
	}

	public static Image getBlank(Display display) {
		if (!triedToLoadBlank) {
			InputStream inputStream = Icons.class.getClassLoader().getResourceAsStream("images/blank.png");
			blank = new Image(display, inputStream);

			try {
				inputStream.close();
			} catch (IOException e) {
				// do nothing
			}

			triedToLoadBlank = true;
		}
		
		return blank;
	}
}
