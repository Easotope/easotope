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

package org.easotope.client.analysis.part.analysis.replicate.repsteps;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepGraphicComposite;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.framework.core.util.Reflection;
import org.easotope.shared.analysis.execute.CalculationError;
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
	private Composite waitingOnCalculations;
	private Composite calculationsReady;
	private Composite calculationsError;
	private Label errorRepStep;
	private Label errorMessage;

	private RepStepGraphicComposite repStepGraphicComposite;
	private RunCalculator runCalculator;

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
        label.setText(Messages.repTabGraphicsComposite_waitingOnInput);

        waitingOnCalculations = new Composite(this, SWT.NONE);
        waitingOnCalculations.setLayout(new GridLayout());

        label = new Label(waitingOnCalculations, SWT.NONE);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        label.setLayoutData(gridData);
        label.setText(Messages.repTabGraphicsComposite_waitingOnCalculations);

        calculationsReady = new Composite(this, SWT.NONE);
        calculationsReady.setLayout(new FillLayout());

		repStepGraphicComposite = new EmptyRepStepGraphicComposite(getEasotopePart(), calculationsReady, SWT.NONE);

        calculationsError = new Composite(this, SWT.NONE);
        calculationsError.setLayout(new FormLayout());

		Label errorTitle = new Label(calculationsError, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(45);
		formData.left = new FormAttachment(20);
		formData.right = new FormAttachment(80);
		errorTitle.setLayoutData(formData);
		errorTitle.setText(Messages.repTabGraphicsComposite_errorTitle);
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

		Label repStepLabel = new Label(errorSubComposite, SWT.NONE);
		repStepLabel.setText(Messages.repTabGraphicsComposite_errorRepStepLabel);
		repStepLabel.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		errorRepStep = new Label(errorSubComposite, SWT.WRAP);
		errorRepStep.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		Label messageLabel = new Label(errorSubComposite, SWT.NONE);
		messageLabel.setText(Messages.repTabGraphicsComposite_errorMessageLabel);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		messageLabel.setLayoutData(gridData);
		messageLabel.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		errorMessage = new Label(errorSubComposite, SWT.WRAP);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		errorMessage.setLayoutData(gridData);
		errorMessage.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		stackLayout.topControl = calculationsReady;
		layout();
	}

	@Override
	protected void handleDispose() {
		
	}

	public void setCompositeClass(String graphicComposite) {
		if (repStepGraphicComposite != null && !repStepGraphicComposite.getClass().getName().equals(graphicComposite)) {
			repStepGraphicComposite.dispose();
			repStepGraphicComposite = null;
		}

		if (repStepGraphicComposite == null) {
			if (graphicComposite != null) {
				repStepGraphicComposite = (RepStepGraphicComposite) Reflection.createObject(graphicComposite, getEasotopePart(), calculationsReady, SWT.NONE);
			} else {
				repStepGraphicComposite = new EmptyRepStepGraphicComposite(getEasotopePart(), calculationsReady, SWT.NONE);
			}
		}

		calculationsReady.layout();
	}

	void setRunCalculator(RunCalculator runCalculator) {
		this.runCalculator = runCalculator;
	}

	public void updateData() {
		if (runCalculator.isWaitingOnSelection() || runCalculator.isLoading()) {
			stackLayout.topControl = waitingOnInput;

		} else if (runCalculator.isCalculating()) {
			stackLayout.topControl = waitingOnCalculations;

		} else if (runCalculator.isSuccessful()) {
			repStepGraphicComposite.newCalculation(runCalculator.getResultsScratchPad());
			stackLayout.topControl = calculationsReady;

		} else if (runCalculator.getErrors() != null) {
			CalculationError error = runCalculator.getErrors().get(0);

			if (error.getStepController() == null) {
				errorRepStep.setText(Messages.repTabGraphicsComposite_emptyRepStepName);
			} else {
				errorRepStep.setText(error.getStepController().getStepName());
			}

			errorMessage.setText(error.getErrorMessage());
			errorMessage.getParent().layout();

			stackLayout.topControl = calculationsError;
		}

		layout();
	}
}
