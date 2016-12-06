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

package org.easotope.client.analysis.part.analysis.sample.samsteps;

import java.util.TreeSet;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.scratchpadtable.ScratchPadTable;
import org.easotope.shared.analysis.cache.calculated.samples.CalculatedSample;
import org.easotope.shared.analysis.tables.SamStep;
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
	private StackLayout stackLayout;

	private Composite waitingOnInput;
	private Composite calculationsReady;
	private ScratchPadTable scratchPadTableInput;
	private ScratchPadTable scratchPadTableOutput;
	private Composite calculationsError;
	private Label errorSamStepLabel;
	private Label errorMessageLabel;

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
        label.setText(Messages.samTabResultsComposite_waitingOnInput);

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
        label.setText(Messages.samTabResultsComposite_instructions);

        Composite scratchPadComposite = new Composite(calculationsReady, SWT.NONE);
        formData = new FormData();
        formData.top = new FormAttachment(label);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(100);
        scratchPadComposite.setLayoutData(formData);
        scratchPadComposite.setLayout(new FormLayout());

        scratchPadTableInput = new ScratchPadTable(scratchPadComposite, false);
        formData = new FormData();
        formData.top = new FormAttachment(0, 5);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(50);
        scratchPadTableInput.setLayoutData(formData);
        scratchPadTableInput.setCanSaveTableLayout(false);

        scratchPadTableOutput = new ScratchPadTable(scratchPadComposite, false);
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
		errorTitle.setText(Messages.samTabResultsComposite_errorTitle);
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
		repStepLabel.setText(Messages.samTabResultsComposite_errorRepStepLabel);
		repStepLabel.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		errorSamStepLabel = new Label(errorSubComposite, SWT.WRAP);
		errorSamStepLabel.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		Label messageLabel = new Label(errorSubComposite, SWT.NONE);
		messageLabel.setText(Messages.samTabResultsComposite_errorMessageLabel);
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

	void updateData(CalculatedSample calculatedSample, SamStep samStep) {
		if (calculatedSample == null) {
			stackLayout.topControl = waitingOnInput;

		} else if (calculatedSample.getErrorMessage() == null) {
			TableLayout inputTableLayout = new TableLayout();
			inputTableLayout.setFormattingOn(true);
			int count=0;
			TreeSet<String> columnNames = new TreeSet<String>(samStep.getInputs().values());

			String[] inputs = new String[columnNames.size()];
			int[] widths = new int[columnNames.size()];

			for (String value : calculatedSample.getColumnOrdering().getOrdering()) {
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

			scratchPadTableInput.setScratchPad(calculatedSample.getSampleScratchPad(), calculatedSample.getColumnOrdering(), calculatedSample.getFormatLookup(), inputTableLayout);

			TableLayout outputTableLayout = new TableLayout();
			outputTableLayout.setFormattingOn(true);
			count=0;
			columnNames = new TreeSet<String>(samStep.getOutputs().values());
			
			String[] outputs = new String[columnNames.size()];
			widths = new int[columnNames.size()];

			for (String value : calculatedSample.getColumnOrdering().getOrdering()) {
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

			scratchPadTableOutput.setScratchPad(calculatedSample.getSampleScratchPad(), calculatedSample.getColumnOrdering(), calculatedSample.getFormatLookup(), outputTableLayout);

			stackLayout.topControl = calculationsReady;

		} else {
			if (calculatedSample.getErrorSamStep() == null) {
				errorSamStepLabel.setText(Messages.samTabResultsComposite_emptyRepStepName);
			} else {
				errorSamStepLabel.setText(calculatedSample.getErrorSamStep());
			}

			errorMessageLabel.setText(calculatedSample.getErrorMessage());
			errorMessageLabel.getParent().layout();

			stackLayout.topControl = calculationsError;
		}

		layout();
	}
}
