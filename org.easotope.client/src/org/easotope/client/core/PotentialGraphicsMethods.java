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

package org.easotope.client.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.easotope.client.Messages;
import org.easotope.framework.core.util.SystemProperty;
import org.easotope.shared.core.Email;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Widget;

// Code that runs in both the server (where there is no graphics library) and
// the client (where there is) may want to optionally call graphical functions
// if they are available. For example, we may want to show an error message to
// the user in a pop-up window in addition to writing it into the log. This class
// contains all such "optional graphics" methods. They are not meant to be called
// directly but rather via reflection. If the reflection fails, we must be in
// server mode and no graphics call is made.

public class PotentialGraphicsMethods {
	public void reportErrorToUser(Object displaySource, Throwable t) {
		Display display = null;

		if (displaySource instanceof Widget) {
			display = ((Widget) displaySource).getDisplay();

		} else if (displaySource instanceof Display) {
			display = (Display) displaySource;

		} else {
			return;
		}

		if (Email.launchSupported()) {
			if (MessageDialog.openQuestion(display.getActiveShell(), Messages.potentialGraphicsMethods_title, Messages.potentialGraphicsMethods_askAboutEmail)) {
				final String UTF8 = "UTF-8";
				String body = null;

				try {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					PrintStream ps = new PrintStream(baos, true, UTF8);

					ps.println(Messages.potentialGraphicsMethods_instructions);
					ps.println();
					ps.print(Messages.potentialGraphicsMethods_version);
					ps.println(SystemProperty.getVersion());
					ps.println();
	
					if (t == null) {
						ps.println(Messages.potentialGraphicsMethods_noThrowable);

					} else {
						while (t != null) {
							t.printStackTrace(ps);
							ps.println();
							t = t.getCause();
						}
					}

					body = baos.toString(UTF8);

				} catch (IOException e) {
					// ignore
				}

				if (!Email.launch(Messages.potentialGraphicsMethods_email, Messages.potentialGraphicsMethods_subject, body)) {
					MessageDialog.openError(display.getActiveShell(), Messages.potentialGraphicsMethods_title, Messages.potentialGraphicsMethods_emailFailed);
				}
			}

		} else {
			MessageDialog.openError(display.getActiveShell(), Messages.potentialGraphicsMethods_title, Messages.potentialGraphicsMethods_suggestSendingLog);
		}
	}
}
