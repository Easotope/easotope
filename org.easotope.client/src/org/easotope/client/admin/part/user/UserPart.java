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

package org.easotope.client.admin.part.user;

import javax.annotation.PostConstruct;

import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

public class UserPart extends ChainedPart {
	private static final String SELECTION_IGNORE_THIS = "SELECTION_IGNORE_THIS";

	public static final String SELECTION_USER_LIST = "SELECTION_USER_LIST";
	public static final String SELECTION_USER_ID = "SELECTION_USER_ID";

	private UserSelectComposite userSelectorComposite;
	private UserComposite userComposite;

	@PostConstruct
	public void postConstruct() {
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		getParent().setLayout(formLayout);

		userSelectorComposite = new UserSelectComposite(this, getParent(), SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(0, 160);
		formData.bottom = new FormAttachment(100);
		userSelectorComposite.setLayoutData(formData);

		userComposite = new UserComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(userSelectorComposite);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		userComposite.setLayoutData(formData);

		userSelectorComposite.addChild(userComposite);

		setSelection(-1, SELECTION_IGNORE_THIS, SELECTION_IGNORE_THIS);
	}

	@Override
	protected boolean closeOnSave() {
		return false;
	}
}
