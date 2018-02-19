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

package org.easotope.client.core.part;

import javax.inject.Inject;

import org.easotope.client.Messages;
import org.easotope.client.core.ModalMessageWithViewChange;
import org.easotope.client.core.annotations.CanDelete;
import org.easotope.client.core.annotations.CanPersist;
import org.easotope.client.core.annotations.CanRevert;
import org.easotope.client.core.annotations.Delete;
import org.easotope.client.core.annotations.Revert;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class EasotopePart {
	@Inject
	private Composite parent;
	@Inject
	private Shell shell;
	@Inject
	private EPartService partService;
	@Inject
	private MPerspective perspective;
	@Inject
	private MPart part;
	@Inject
	private MDirtyable dirtyable;
	@Inject
	private MApplication application;

	private boolean canPersist = false;
	private boolean canDelete = false;

	public Composite getParent() {
		return parent;
	}

	public EPartService getPartService() {
		return partService;
	}

	public MPart getPart() {
		return part;
	}

	public MApplication getApplication() {
		return application;
	}

	public void setCanRevert(boolean canRevert) {
		dirtyable.setDirty(canRevert);
	}

	@CanRevert
	public boolean canRevert() {
		return parent.getCursor() == null && dirtyable.isDirty();
	}

	@Revert
	public void revert() {

	}

	public void setCanPersist(boolean canPersist) {
		this.canPersist = canPersist;
	}

	@CanPersist
	public boolean canPersist() {
		return parent.getCursor() == null && canPersist;
	}

	@Persist
	public void persist() {

	}

	public void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
	}

	@CanDelete
	public boolean canDelete() {
		return canDelete;
	}

	@Delete
	public void delete() {

	}

	public void raiseError(String message) {
		ModalMessageWithViewChange.raiseError(shell, partService, perspective, part, message);
	}

	public void raiseInfo(String message) {
		ModalMessageWithViewChange.raiseInfo(shell, partService, perspective, part, message);
	}

	public boolean raiseQuestion(String message) {
		return MessageDialog.open(MessageDialog.QUESTION, shell, Messages.modalMessage_genericErrorTitle, message, SWT.SHEET);
	}
}
