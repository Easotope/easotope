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

package org.easotope.client.analysis.part.repanalysis;

import java.util.HashMap;
import java.util.List;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.framework.core.util.Reflection;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.repanalysis.AnalysisCacheRepAnalysisGetListener;
import org.easotope.shared.analysis.execute.AnalysisCompiled;
import org.easotope.shared.analysis.step.StepController;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class RepAnalysisComposite extends EditorComposite implements AnalysisCacheRepAnalysisGetListener {
	private static final String REP_ANALYSIS_GET = "REP_ANALYSIS_GET";

 	private Label id;
	private Label name;
	private Label description;
	private Table repSteps;
	private Table requiredInputs;
	private Table generatedOutputs;

	protected RepAnalysisComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.repAnalysisComposite_repAnalysisIdLabel);

		id = new Label(this, SWT.NONE);

		Label massSpecNameLabel = new Label(this, SWT.NONE);
		massSpecNameLabel.setText(Messages.repAnalysisComposite_repAnalysisNameLabel);

		Composite standardNameComposite = new Composite(this, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		standardNameComposite.setLayout(gridLayout);

		name = new Label(standardNameComposite, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		name.setLayoutData(gridData);
		name.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		Label descriptionLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		descriptionLabel.setLayoutData(gridData);
		descriptionLabel.setText(Messages.repAnalysisComposite_descriptionLabel);

		description = new Label(this, SWT.WRAP);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		description.setLayoutData(gridData);

		Label repStepLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		repStepLabel.setLayoutData(gridData);
		repStepLabel.setText(Messages.repAnalysisComposite_repStepLabel);

		repSteps = new Table(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		repSteps.setLayoutData(gridData);
		repSteps.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				repSteps.deselectAll();
			}
		});
		
		Label requiredInputsLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		requiredInputsLabel.setLayoutData(gridData);
		requiredInputsLabel.setText(Messages.repAnalysisComposite_requiredInputsLabel);

		requiredInputs = new Table(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		requiredInputs.setLayoutData(gridData);
		requiredInputs.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				requiredInputs.deselectAll();
			}
		});

		Label generatedOutputsLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		generatedOutputsLabel.setLayoutData(gridData);
		generatedOutputsLabel.setText(Messages.repAnalysisComposite_generatedOutputsLabel);

		generatedOutputs = new Table(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		generatedOutputs.setLayoutData(gridData);
		generatedOutputs.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				generatedOutputs.deselectAll();
			}
		});
	}

	@Override
	protected void handleDispose() {
		super.handleDispose();
	}

	private RepAnalysis getCurrentRepAnalysis() {
		Object[] currentObject = (Object[]) getCurrentObject();
		return currentObject == null ? null : (RepAnalysis) currentObject[0];
	}

	private List<RepStep> getCurrentRepSteps() {
		Object[] currentObject = (Object[]) getCurrentObject();
		@SuppressWarnings("unchecked")
		List<RepStep> currentRepSteps = currentObject == null ? null : (List<RepStep>) currentObject[1];
		return currentRepSteps;
	}

	@Override
	protected void setCurrentFieldValues() {
		RepAnalysis currentRepAnalysis = getCurrentRepAnalysis();
		List<RepStep> currentRepStepList = getCurrentRepSteps();

		id.setText(String.valueOf(currentRepAnalysis.getId()));
		name.setText(currentRepAnalysis.getName());
		description.setText(currentRepAnalysis.getDescription());

		repSteps.removeAll();
		requiredInputs.removeAll();
		generatedOutputs.removeAll();

		if (currentRepAnalysis != null && currentRepStepList != null) {
			AnalysisCompiled dataAnalysisCompiled = new AnalysisCompiled(currentRepAnalysis, currentRepStepList);

			for (RepStep repStep : currentRepStepList) {
				StepController repStepController = (StepController) Reflection.createObject(repStep.getClazz());
				new TableItem(repSteps, SWT.NONE).setText(repStepController.getStepName());
			}

			for (String requiredInputColumn : dataAnalysisCompiled.getRequiredInputColumns()) {
				new TableItem(requiredInputs, SWT.NONE).setText(requiredInputColumn);
			}

			for (String generatedOutputColumn : dataAnalysisCompiled.getGeneratedOutputColumns()) {
				if (generatedOutputColumn != null) {
				new TableItem(generatedOutputs, SWT.NONE).setText(generatedOutputColumn);
				}
			}
		}
	}

	@Override
	protected void setDefaultFieldValues() {
		
	}

	@Override
	public void enableWidgets() {

	}

	@Override
	public void disableWidgets() {

	}

	@Override
	protected boolean isDirty() {
		return false;
	}

	@Override
	protected boolean hasError() {
		return false;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		RepAnalysis dataAnalysis = getCurrentRepAnalysis();
		Integer selectedDataAnalysisId = (Integer) selection.get(RepAnalysisPart.SELECTION_REP_ANALYSIS_ID);

		if (dataAnalysis == null && selectedDataAnalysisId == null) {
			return false;
		}

		if ((dataAnalysis == null && selectedDataAnalysisId != null) || (dataAnalysis != null && selectedDataAnalysisId == null)) {
			return true;
		}

		return dataAnalysis.getId() != selectedDataAnalysisId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer dataAnalysisId = (Integer) selection.get(RepAnalysisPart.SELECTION_REP_ANALYSIS_ID);

		if (dataAnalysisId != null) {
			int commandId = AnalysisCache.getInstance().repAnalysisGet(dataAnalysisId, this);
			waitingFor(REP_ANALYSIS_GET, commandId);
			return true;
		}

		return false;
	}

	@Override
	protected void requestSave(boolean isResend) {
		assert(false);
	}

	@Override
	protected boolean canDelete() {
		return false;
	}

	@Override
	protected boolean requestDelete() {
		return false;
	}

	@Override
	public void repAnalysisGetCompleted(int commandId, RepAnalysis repAnalysis, List<RepStep> repSteps) {
		newObject(REP_ANALYSIS_GET, new Object[] { repAnalysis, repSteps });
	}

	@Override
	public void repAnalysisGetError(int commandId, String message) {
		raiseGetError(REP_ANALYSIS_GET, message);		
	}
}
