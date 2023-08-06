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

package org.easotope.client.analysis.part.analysis.sample.samsteps;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.SamStepParamComposite;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.framework.core.util.Reflection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class TabParametersComposite extends EasotopeComposite {
	private Composite emptyComposite;
	private Composite fullComposite;
	private Label instructions;
	private SamStepParamComposite samStepParamComposite;

	public TabParametersComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);
		setLayout(new StackLayout());

		emptyComposite = new Composite(this, SWT.NONE);
		emptyComposite.setLayout(new GridLayout());

        Label label = new Label(emptyComposite, SWT.NONE);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        label.setLayoutData(gridData);
        label.setText(Messages.emptySamStepParamComposite_noParameters);

		fullComposite = new Composite(this, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		fullComposite.setLayout(formLayout);

		instructions = new Label(fullComposite, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		instructions.setLayoutData(formData);
		instructions.setText(Messages.samTabParametersComposite_instructions);

		((StackLayout) getLayout()).topControl = emptyComposite;
		layout();
	}

	@Override
	protected void handleDispose() {
		
	}

	public void setCompositeClass(String parameterComposite) {
		if (parameterComposite == null) {
			if (samStepParamComposite != null) {
				samStepParamComposite.dispose();
				samStepParamComposite = null;
			}

			((StackLayout) getLayout()).topControl = emptyComposite;
			layout();

			return;
		}

		if (samStepParamComposite != null && !samStepParamComposite.getClass().getName().equals(parameterComposite)) {
			samStepParamComposite.dispose();
			samStepParamComposite = null;
		}

		if (samStepParamComposite != null) {
			return;
		}

		ChainedPart chainedPart = (ChainedPart) getEasotopePart();

		samStepParamComposite = (SamStepParamComposite) Reflection.createObject(parameterComposite, (ChainedPart) getEasotopePart(), fullComposite, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(instructions, 5);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		samStepParamComposite.setLayoutData(formData);

		((StackLayout) getLayout()).topControl = fullComposite;
		fullComposite.layout();
		layout();

		chainedPart.setSelection(-1, new HashMap<String,Object>());
		chainedPart.setCanPersist();
		chainedPart.setCanRevert();
	}
}
