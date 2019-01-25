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

package org.easotope.client.core;

import java.util.ArrayList;

import org.easotope.client.Messages;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class ModalMessageWithViewChange {
	private static ModalMessageWithViewChange instance = new ModalMessageWithViewChange();
	private ArrayList<ModalParameters> list = new ArrayList<ModalParameters>();

	public static void raiseInfo(Shell shell, EPartService partService, MPerspective perspective, MPart part, String message) {
		instance.raiseInfoPrivate(shell, partService, perspective, part, message);
	}

	public static void raiseError(Shell shell, EPartService partService, MPerspective perspective, MPart part, String message) {
		instance.raiseErrorPrivate(shell, partService, perspective, part, message);
	}

	private synchronized void raiseInfoPrivate(final Shell shell, final EPartService partService, final MPerspective perspective, final MPart part, final String message) {
		list.add(list.size(), new ModalParameters(shell, partService, perspective, part, MessageDialog.INFORMATION, Messages.modalMessage_genericInfoTitle, message));

		if (list.size() == 1) {
			raise();
		}
	}

	private synchronized void raiseErrorPrivate(final Shell shell, final EPartService partService, final MPerspective perspective, final MPart part, final String message) {
		list.add(list.size(), new ModalParameters(shell, partService, perspective, part, MessageDialog.ERROR, Messages.modalMessage_genericErrorTitle, message));
		
		if (list.size() == 1) {
			raise();
		}
	}

	private synchronized void raise() {
		while (list.size() != 0) {
			ModalParameters currentParameters = list.get(0);

			if (currentParameters.part.isVisible()) {
				if (currentParameters.partService.getActivePart() != currentParameters.part) {
					MessageDialog.open(currentParameters.kind, currentParameters.shell, currentParameters.title, Messages.modalMessage_inactive, SWT.SHEET);

					currentParameters.partService.switchPerspective(currentParameters.perspective);
					currentParameters.partService.showPart(currentParameters.part, PartState.ACTIVATE);
				}
		
				MessageDialog.open(currentParameters.kind, currentParameters.shell, currentParameters.title, currentParameters.message, SWT.SHEET);
			}
			
			list.remove(0);
		}
	}

	class ModalParameters {
		Shell shell;
		EPartService partService;
		MPerspective perspective;
		MPart part;
		int kind;
		String title;
		String message;

		ModalParameters(Shell shell, EPartService partService, MPerspective perspective, MPart part, int kind, String title, String message) {
			this.shell = shell;
			this.partService = partService;
			this.perspective = perspective;
			this.part = part;
			this.kind = kind;
			this.title = title;
			this.message = message;
		}
	}
}
