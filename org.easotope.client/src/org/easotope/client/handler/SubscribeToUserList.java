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

package org.easotope.client.handler;

import org.easotope.client.Messages;
import org.easotope.shared.core.Email;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class SubscribeToUserList {
	@Execute
	public void execute(Shell shell) {
		MessageDialog.open(MessageDialog.INFORMATION, shell, Messages.subscribeToUserList_explainTitle, Messages.subscribeToUserList_explainMessage, SWT.SHEET);

		if (!Email.launch(Messages.subscribeToUserList_address, Messages.subscribeToUserList_subject, Messages.subscribeToUserList_body)) {
			MessageDialog.open(MessageDialog.INFORMATION, shell, Messages.subscribeToUserList_noEmailTitle, Messages.subscribeToUserList_noEmailMessage, SWT.SHEET);
		}
	}
}
