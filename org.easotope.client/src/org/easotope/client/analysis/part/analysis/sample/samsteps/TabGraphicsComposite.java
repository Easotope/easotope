/*
 * Copyright © 2016-2017 by Devon Bowen.
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

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.SamStepGraphicComposite;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.framework.core.util.Reflection;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class TabGraphicsComposite extends EasotopeComposite {
	private StackLayout stackLayout;

	private Composite waitingOnInput;
	private Composite calculationsReady;
	private Composite calculationsError;
	private Label errorSamStepLabel;
	private Label errorMessageLabel;

	private SamStepGraphicComposite samStepGraphicComposite;

	public TabGraphicsComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);

		stackLayout = new StackLayout();
        setLayout(stackLayout);

        waitingOnInput = new Composite(this, SWT.NONE);
        waitingOnInput.setLayout(new GridLayout());

        Label label = new Label(waitingOnInput, SWT.NONE);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        label.setLayoutData(gridData);
        label.setText(Messages.samTabGraphicsComposite_waitingOnInput);

        calculationsReady = new Composite(this, SWT.NONE);
        calculationsReady.setLayout(new FillLayout());

		samStepGraphicComposite = new EmptySamStepGraphicComposite(getEasotopePart(), calculationsReady, SWT.NONE);

        calculationsError = new Composite(this, SWT.NONE);
        calculationsError.setLayout(new FormLayout());

		Label errorTitle = new Label(calculationsError, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(45);
		formData.left = new FormAttachment(20);
		formData.right = new FormAttachment(80);
		errorTitle.setLayoutData(formData);
		errorTitle.setText(Messages.samTabGraphicsComposite_errorTitle);
		errorTitle.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		Composite errorSubComposite = new Composite(calculationsError, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(errorTitle, 10);
		formData.left = new FormAttachment(25);
		formData.right = new FormAttachment(75);
		formData.bottom = new FormAttachment(100);
		errorSubComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		errorSubComposite.setLayout(gridLayout);

		Label samStepLabel = new Label(errorSubComposite, SWT.NONE);
		samStepLabel.setText(Messages.samTabGraphicsComposite_errorSamStepLabel);
		samStepLabel.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		errorSamStepLabel = new Label(errorSubComposite, SWT.WRAP);
		errorSamStepLabel.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		Label messageLabel = new Label(errorSubComposite, SWT.NONE);
		messageLabel.setText(Messages.samTabGraphicsComposite_errorMessageLabel);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		messageLabel.setLayoutData(gridData);
		messageLabel.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		errorMessageLabel = new Label(errorSubComposite, SWT.WRAP);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		errorMessageLabel.setLayoutData(gridData);
		errorMessageLabel.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		stackLayout.topControl = calculationsReady;
		layout();
	}

	@Override
	protected void handleDispose() {
		
	}

	public void setCompositeClass(String graphicComposite) {
		if (samStepGraphicComposite != null && !samStepGraphicComposite.getClass().getName().equals(graphicComposite)) {
			samStepGraphicComposite.dispose();
			samStepGraphicComposite = null;
		}

		if (samStepGraphicComposite == null) {
			if (graphicComposite != null) {
				samStepGraphicComposite = (SamStepGraphicComposite) Reflection.createObject(graphicComposite, getEasotopePart(), calculationsReady, SWT.NONE);
			} else {
				samStepGraphicComposite = new EmptySamStepGraphicComposite(getEasotopePart(), calculationsReady, SWT.NONE);
			}
		}

		calculationsReady.layout();
	}

	public void updateData(ScratchPad<SamplePad> scratchPad, String errorStepName, String errorMessage) {
		if (scratchPad == null) {
			stackLayout.topControl = waitingOnInput;

		} else if (errorMessage == null) {
			samStepGraphicComposite.newCalculation(scratchPad);
			stackLayout.topControl = calculationsReady;

		} else {
			if (errorStepName == null) {
				errorSamStepLabel.setText(Messages.samTabGraphicsComposite_emptySamStepName);
			} else {
				errorSamStepLabel.setText(errorStepName);
			}

			errorMessageLabel.setText(errorMessage);
			errorMessageLabel.getParent().layout();

			stackLayout.topControl = calculationsError;
		}

		layout();
	}
}
