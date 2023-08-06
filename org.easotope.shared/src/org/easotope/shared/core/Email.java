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

package org.easotope.shared.core;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.net.URI;
import java.net.URLEncoder;

public class Email {
	public static boolean launchSupported() {
		if (!Desktop.isDesktopSupported()) {
			return false;
		}

		Desktop desktop = Desktop.getDesktop();

		if (!desktop.isSupported(Action.MAIL)) {			
			return false;
		}

		return true;
	}

	public static boolean launch(String address, String subject, String body) {
		final String UTF8 = "UTF-8";

		if (!launchSupported()) {
			return false;
		}

		try {
			boolean questionAlreadyAdded = false;
			String uriString = "mailto:" + URLEncoder.encode(address, UTF8).replaceAll("\\+", "%20");

			if (subject != null && !subject.isEmpty()) {
				if (questionAlreadyAdded) {
					uriString += "&";
				} else {
					uriString += "?";
					questionAlreadyAdded = true;
				}

				uriString += "subject=" + URLEncoder.encode(subject, UTF8).replaceAll("\\+", "%20");
			}

			if (body != null && !body.isEmpty()) {
				if (questionAlreadyAdded) {
					uriString += "&";
				} else {
					uriString += "?";
					questionAlreadyAdded = true;
				}

				uriString += "body=" + URLEncoder.encode(body, UTF8).replaceAll("\\+", "%20");
			}

			Desktop.getDesktop().mail(new URI(uriString));

			return true;

		} catch (Exception e) {
			// ignore
		}
		
		return false;
	}
}
