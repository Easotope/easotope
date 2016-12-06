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

package org.easotope.client.admin.part.massspec;

import javax.annotation.PostConstruct;

import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

public class MassSpecPart extends ChainedPart {
	private static final String SELECTION_IGNORE_THIS = "SELECTION_IGNORE_THIS";

	public static final String SELECTION_MASS_SPEC_LIST = "SELECTION_MASS_SPEC_LIST";
	public static final String SELECTION_MASS_SPEC_ID = "SELECTION_MASS_SPEC_ID";

	public static final String SELECTION_REF_GAS_LIST = "SELECTION_REF_GAS_LIST";
	public static final String SELECTION_REF_GAS_ID = "SELECTION_REF_GAS_ID";

	public static final String SELECTION_CORR_INTERVAL_LIST = "SELECTION_CORR_INTERVAL_LIST";
	public static final String SELECTION_CORR_INTERVAL_ID = "SELECTION_CORR_INTERVAL_ID";

	private MassSpecSelectComposite massSpecSelectorComposite;
	private MassSpecComposite massSpecComposite;
	
	private RefGasSelectComposite refGasSelectorComposite;
	private RefGasComposite refGasComposite;

	private CorrIntervalSelectComposite corrIntervalSelectorComposite;
	private CorrIntervalComposite corrIntervalComposite;

	@PostConstruct
	public void postConstruct() {
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		getParent().setLayout(formLayout);

		massSpecSelectorComposite = new MassSpecSelectComposite(this, getParent(), SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(0, 160);
		formData.bottom = new FormAttachment(100);
		massSpecSelectorComposite.setLayoutData(formData);

		massSpecComposite = new MassSpecComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(massSpecSelectorComposite);
		formData.right = new FormAttachment(50);
		formData.bottom = new FormAttachment(100);
		massSpecComposite.setLayoutData(formData);

		massSpecSelectorComposite.addChild(massSpecComposite);

		refGasSelectorComposite = new RefGasSelectComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(massSpecComposite);
		formData.right = new FormAttachment(massSpecComposite, 160, SWT.RIGHT);
		formData.bottom = new FormAttachment(50);
		refGasSelectorComposite.setLayoutData(formData);

		refGasComposite = new RefGasComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(refGasSelectorComposite);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(50);
		refGasComposite.setLayoutData(formData);

		refGasSelectorComposite.addChild(refGasComposite);

		corrIntervalSelectorComposite = new CorrIntervalSelectComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(refGasSelectorComposite);
		formData.left = new FormAttachment(massSpecComposite);
		formData.right = new FormAttachment(massSpecComposite, 160, SWT.RIGHT);
		formData.bottom = new FormAttachment(100);
		corrIntervalSelectorComposite.setLayoutData(formData);

		corrIntervalComposite = new CorrIntervalComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(refGasComposite);
		formData.left = new FormAttachment(corrIntervalSelectorComposite);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		corrIntervalComposite.setLayoutData(formData);

		corrIntervalSelectorComposite.addChild(corrIntervalComposite);
		
		setSelection(-1, SELECTION_IGNORE_THIS, SELECTION_IGNORE_THIS);
	}

	@Override
	protected boolean closeOnSave() {
		return false;
	}
}
