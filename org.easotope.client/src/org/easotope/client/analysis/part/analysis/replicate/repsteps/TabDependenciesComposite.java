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

package org.easotope.client.analysis.part.analysis.replicate.repsteps;

import java.util.HashMap;
import java.util.TreeSet;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.shared.analysis.execute.CalculationError;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.analysis.tables.RepStep;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TabDependenciesComposite extends EasotopeComposite {
	private RunCalculator runCalculator;

	private StackLayout stackLayout;

	private Composite waitingOnInput;
	private Composite waitingOnCalculations;
	private Composite calculationsReady;
	private Table table;
	private Composite noDependencies;
	private Composite calculationsError;
	private Label errorRepStep;
	private Label errorMessage;

	public TabDependenciesComposite(EasotopePart easotopePart, Composite parent, int style) {
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
        label.setText(Messages.repTabDependenciesComposite_waitingOnInput);

        waitingOnCalculations = new Composite(this, SWT.NONE);
        waitingOnCalculations.setLayout(new GridLayout());

        label = new Label(waitingOnCalculations, SWT.NONE);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        label.setLayoutData(gridData);
        label.setText(Messages.repTabDependenciesComposite_waitingOnCalculations);

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
        label.setText(Messages.repTabDependenciesComposite_instructions);

        table = new Table(calculationsReady, SWT.BORDER);
        formData = new FormData();
        formData.top = new FormAttachment(label, GuiConstants.INTER_WIDGET_GAP);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(0, 400);
        formData.bottom = new FormAttachment(100);
        table.setLayoutData(formData);
		table.setLinesVisible(true);

		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(Messages.repTabDependenciesComposite_column1);
		column.setWidth(200);

		column = new TableColumn(table, SWT.NONE);
		column.setText(Messages.repTabDependenciesComposite_column2);
		column.setWidth(2000);

		table.setHeaderVisible(true);
        
        calculationsError = new Composite(this, SWT.NONE);
        calculationsError.setLayout(new FormLayout());

		Label errorTitle = new Label(calculationsError, SWT.WRAP);
		formData = new FormData();
		formData.top = new FormAttachment(45);
		formData.left = new FormAttachment(20);
		formData.right = new FormAttachment(80);
		errorTitle.setLayoutData(formData);
		errorTitle.setText(Messages.repTabDependenciesComposite_errorTitle);
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
		repStepLabel.setText(Messages.repTabDependenciesComposite_errorRepStepLabel);
		repStepLabel.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		errorRepStep = new Label(errorSubComposite, SWT.WRAP);
		errorRepStep.setForeground(ColorCache.getColor(getDisplay(), ColorCache.RED));

		Label messageLabel = new Label(errorSubComposite, SWT.NONE);
		messageLabel.setText(Messages.repTabDependenciesComposite_errorMessageLabel);
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

        noDependencies = new Composite(this, SWT.NONE);
        noDependencies.setLayout(new GridLayout());

        label = new Label(noDependencies, SWT.NONE);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        label.setLayoutData(gridData);
        label.setText(Messages.repTabDependenciesComposite_noDependencies);

		stackLayout.topControl = calculationsReady;
		layout();
	}

	@Override
	protected void handleDispose() {
		
	}

	void setRunCalculator(RunCalculator runCalculator) {
		this.runCalculator = runCalculator;
	}

	public void updateData(RepStep repStep) {
		if (runCalculator.isWaitingOnSelection() || runCalculator.isLoading()) {
			stackLayout.topControl = waitingOnInput;

		} else if (runCalculator.isCalculating()) {
			stackLayout.topControl = waitingOnCalculations;

		} else if (runCalculator.isSuccessful()) {
			for (TableItem tableItem : table.getItems()) {
				tableItem.dispose();
			}

			DependencyManager dependencyManager = runCalculator.getDependencyManager(repStep.getPosition());

			if (dependencyManager == null) {
				stackLayout.topControl = noDependencies;

			} else {
				HashMap<String,String> allPrintableValues = new HashMap<String,String>();

				for (DependencyPlugin dependencyPlugin : dependencyManager.getDependencyPlugins()) {
					HashMap<String,String> printableValues = dependencyPlugin.getPrintableValues(dependencyManager);
					
					if (printableValues != null) {
						allPrintableValues.putAll(printableValues);
					}
				}

				TreeSet<String> sortedKeys = new TreeSet<String>(allPrintableValues.keySet());

				for (String key : sortedKeys) {
					TableItem tableItem = new TableItem(table, SWT.NONE);
					tableItem.setText(0, key);
					tableItem.setText(1, allPrintableValues.get(key));
				}

				if (allPrintableValues.size() == 0) {
					stackLayout.topControl = noDependencies;
				} else {
					stackLayout.topControl = calculationsReady;
				}
			}

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
