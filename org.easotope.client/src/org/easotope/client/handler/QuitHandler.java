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

package org.easotope.client.handler;

import java.text.MessageFormat;
import java.util.TreeSet;

import org.easotope.client.Messages;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class QuitHandler {
	@Execute
	public void execute(Shell shell, MApplication application, IWorkbench workbench, EModelService modelService) {
		if (quitLogic(shell, application, modelService)) {
			workbench.close();
		}
	}

	public static boolean quitLogic(Shell shell, MApplication application, EModelService modelService) {
		TreeSet<String> dirtyParts = new TreeSet<String>();

		for (MPart part : modelService.findElements(application, null, MPart.class, null)) {
			if (part.isDirty() && part.isVisible()) {
				dirtyParts.add(part.getLocalizedLabel());
			}
		}

		if (!dirtyParts.isEmpty()) {
			String list = "";

			for (String dirtyPart : dirtyParts) {
				list += list.isEmpty() ? "" : ", ";
				list += dirtyPart;
			}

			String message = MessageFormat.format(Messages.quitHandler_error, new Object[] { list });
			MessageDialog.open(MessageDialog.ERROR, shell, Messages.quitHandler_title, message, SWT.SHEET);
			return false;
		}
		
		return true;
	}
}
