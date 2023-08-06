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

package org.easotope.client.analysis.part.analysis.replicate.repsteps;

import java.util.TreeSet;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.scratchpadtable.ScratchPadTable;
import org.easotope.shared.analysis.execute.AnalysisWithParameters;
import org.easotope.shared.analysis.execute.CalculationError;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.AnalysisIdentifier;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.tables.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class TabResultsComposite extends EasotopeComposite {
	private RunCalculator runCalculator;

	private StackLayout stackLayout;

	private Composite waitingOnInput;
	private Composite waitingOnCalculations;
	private Composite calculationsReady;
	private ScratchPadTable scratchPadTableInput;
	private ScratchPadTable scratchPadTableOutput;
	private Composite calculationsError;
	private Label errorRepStep;
	private Label errorMessage;

	public TabResultsComposite(EasotopePart easotopePart, Composite parent, int style) {
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
        label.setText(Messages.repTabResultsComposite_waitingOnInput);

        waitingOnCalculations = new Composite(this, SWT.NONE);
        waitingOnCalculations.setLayout(new GridLayout());

        label = new Label(waitingOnCalculations, SWT.NONE);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        label.setLayoutData(gridData);
        label.setText(Messages.repTabResultsComposite_waitingOnCalculations);

        calculationsReady = new Composite(this, SWT.NONE);
        FormLayout formLayout = new FormLayout();
        formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
        formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
        formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
        formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
        calculationsReady.setLayout(formLayout);

        label = new Label(calculationsReady, SWT.WRAP);
        FormData formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        label.setLayoutData(formData);
        label.setText(Messages.repTabResultsComposite_instructions);

        Composite scratchPadComposite = new Composite(calculationsReady, SWT.NONE);
        formData = new FormData();
        formData.top = new FormAttachment(label);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(100);
        scratchPadComposite.setLayoutData(formData);
        scratchPadComposite.setLayout(new FormLayout());

        scratchPadTableInput = new ScratchPadTable(scratchPadComposite, false, false);
        formData = new FormData();
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(50);
        scratchPadTableInput.setLayoutData(formData);
        scratchPadTableInput.setCanSaveTableLayout(false);

        scratchPadTableOutput = new ScratchPadTable(scratchPadComposite, false, false);
        formData = new FormData();
        formData.top = new FormAttachment(50, 5);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(100);
        scratchPadTableOutput.setLayoutData(formData);
        scratchPadTableOutput.setCanSaveTableLayout(false);

        calculationsError = new Composite(this, SWT.NONE);
        calculationsError.setLayout(new FormLayout());

		Label errorTitle = new Label(calculationsError, SWT.WRAP);
		formData = new FormData();
		formData.top = new FormAttachment(45);
		formData.left = new FormAttachment(20);
		formData.right = new FormAttachment(80);
		errorTitle.setLayoutData(formData);
		errorTitle.setText(Messages.repTabResultsComposite_errorTitle);
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
		repStepLabel.setText(Messages.repTabResultsComposite_errorRepStepLabel);
		repStepLabel.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		errorRepStep = new Label(errorSubComposite, SWT.WRAP);
		errorRepStep.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		Label messageLabel = new Label(errorSubComposite, SWT.NONE);
		messageLabel.setText(Messages.repTabResultsComposite_errorMessageLabel);
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

	void setRunCalculator(RunCalculator runCalculator) {
		this.runCalculator = runCalculator;
	}

	void updateData(RepStep repStep) {
		if (runCalculator.isWaitingOnSelection() || runCalculator.isLoading()) {
			stackLayout.topControl = waitingOnInput;

		} else if (runCalculator.isCalculating()) {
			stackLayout.topControl = waitingOnCalculations;

		} else if (runCalculator.isSuccessful()) {
			AnalysisWithParameters dataAnalysisWithParameters = runCalculator.getDataAnalysisWithParameters();

			ColumnOrdering columnOrdering = new ColumnOrdering();
			columnOrdering.add(dataAnalysisWithParameters.getGeneratedOutputColumns());

			FormatLookup formatLookup = new FormatLookup();
			formatLookup.add(AnalysisIdentifier.Level.REPLICATE, dataAnalysisWithParameters.getAnalysis().getName(), dataAnalysisWithParameters.getOutputColumnToFormat());

			TableLayout inputTableLayout = new TableLayout();
			inputTableLayout.setFormattingOn(true);
			int count=0;
			TreeSet<String> columnNames = new TreeSet<String>(repStep.getInputs().values());

			String[] inputs = new String[columnNames.size()];
			int[] widths = new int[columnNames.size()];

			for (String value : columnOrdering.getOrdering()) {
				if (columnNames.contains(value)) {
					inputs[count] = value;
					widths[count++] = 100;
					columnNames.remove(value);
				}
			}

			for (String value : columnNames) {
				inputs[count] = value;
				widths[count++] = 100;
			}

			inputTableLayout.setColumnOrder(inputs);
			inputTableLayout.setColumnWidth(widths);

			scratchPadTableInput.setScratchPad(runCalculator.getResultsScratchPad(), new ColumnOrdering(columnOrdering), new FormatLookup(formatLookup), inputTableLayout);

			TableLayout outputTableLayout = new TableLayout();
			outputTableLayout.setFormattingOn(true);
			count=0;
			columnNames = new TreeSet<String>(repStep.getOutputs().values());

			String[] outputs = new String[columnNames.size()];
			widths = new int[columnNames.size()];
			
			for (String value : columnOrdering.getOrdering()) {
				if (columnNames.contains(value)) {
					outputs[count] = value;
					widths[count++] = 100;
					columnNames.remove(value);
				}
			}

			for (String value : columnNames) {
				outputs[count] = value;
				widths[count++] = 100;
			}

			outputTableLayout.setColumnOrder(outputs);
			outputTableLayout.setColumnWidth(widths);

			scratchPadTableOutput.setScratchPad(runCalculator.getResultsScratchPad(), columnOrdering, formatLookup, outputTableLayout);

			stackLayout.topControl = calculationsReady;

		} else if (runCalculator.getErrors() != null) {
			CalculationError error = runCalculator.getErrors().get(0);

			if (error.getStepController() == null) {
				errorRepStep.setText(Messages.repTabResultsComposite_emptyRepStepName);
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
