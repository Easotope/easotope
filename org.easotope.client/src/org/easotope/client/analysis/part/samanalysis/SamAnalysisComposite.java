/*
 * Copyright © 2016-2020 by Devon Bowen.
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

package org.easotope.client.analysis.part.samanalysis;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.framework.core.util.Reflection;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.AnalysisCacheRepAnalysisListGetListener;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.RepAnalysisList;
import org.easotope.shared.analysis.cache.analysis.samanalysis.AnalysisCacheSamAnalysisGetListener;
import org.easotope.shared.analysis.execute.AnalysisCompiled;
import org.easotope.shared.analysis.step.StepController;
import org.easotope.shared.analysis.tables.SamAnalysis;
import org.easotope.shared.analysis.tables.SamStep;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class SamAnalysisComposite extends EditorComposite implements AnalysisCacheRepAnalysisListGetListener, AnalysisCacheSamAnalysisGetListener {
	private static final String SAM_ANALYSIS_GET = "SAM_ANALYSIS_GET";
	private static final String REP_ANALYSIS_LIST_GET = "REP_ANALYSIS_LIST_GET";

 	private Label id;
	private Label name;
	private Label description;
	private Table samSteps;
	private Table requiredInputs;
	private Table generatedOutputs;
	private Table validRepAnalyses;

	private RepAnalysisList currentRepAnalysisList = null;

	protected SamAnalysisComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.samAnalysisComposite_samAnalysisIdLabel);

		id = new Label(this, SWT.NONE);

		Label massSpecNameLabel = new Label(this, SWT.NONE);
		massSpecNameLabel.setText(Messages.samAnalysisComposite_samAnalysisNameLabel);

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
		descriptionLabel.setText(Messages.samAnalysisComposite_descriptionLabel);

		description = new Label(this, SWT.WRAP);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		description.setLayoutData(gridData);

		Label samStepLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		samStepLabel.setLayoutData(gridData);
		samStepLabel.setText(Messages.samAnalysisComposite_samStepLabel);

		samSteps = new Table(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		samSteps.setLayoutData(gridData);
		samSteps.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				samSteps.deselectAll();
			}
		});
		
		Label requiredInputsLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		requiredInputsLabel.setLayoutData(gridData);
		requiredInputsLabel.setText(Messages.samAnalysisComposite_requiredInputsLabel);

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
		generatedOutputsLabel.setText(Messages.samAnalysisComposite_generatedOutputsLabel);

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

		Label validRepAnalysesLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		validRepAnalysesLabel.setLayoutData(gridData);
		validRepAnalysesLabel.setText(Messages.samAnalysisComposite_validRepAnalysesLabel);

		validRepAnalyses = new Table(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		validRepAnalyses.setLayoutData(gridData);
		validRepAnalyses.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				validRepAnalyses.deselectAll();
			}
		});

		int commandId = AnalysisCache.getInstance().repAnalysisListGet(this);
		waitingFor(REP_ANALYSIS_LIST_GET, commandId);
	}

	@Override
	protected void handleDispose() {
		super.handleDispose();
	}

	private SamAnalysis getCurrentSamAnalysis() {
		Object[] currentObject = (Object[]) getCurrentObject();
		return currentObject == null ? null : (SamAnalysis) currentObject[0];
	}

	private List<SamStep> getCurrentSamSteps() {
		Object[] currentObject = (Object[]) getCurrentObject();
		@SuppressWarnings("unchecked")
		List<SamStep> currentSamSteps = currentObject == null ? null : (List<SamStep>) currentObject[1];
		return currentSamSteps;
	}

	@Override
	protected void setCurrentFieldValues() {
		SamAnalysis currentSamAnalysis = getCurrentSamAnalysis();
		List<SamStep> currentSamStepList = getCurrentSamSteps();

		id.setText(String.valueOf(currentSamAnalysis.getId()));
		name.setText(currentSamAnalysis.getName());
		description.setText(currentSamAnalysis.getDescription());

		samSteps.removeAll();
		requiredInputs.removeAll();
		generatedOutputs.removeAll();
		validRepAnalyses.removeAll();

		if (currentSamAnalysis != null && currentSamStepList != null) {
			AnalysisCompiled dataAnalysisCompiled = new AnalysisCompiled(currentSamAnalysis, currentSamStepList);

			for (SamStep samStep : currentSamStepList) {
				StepController samStepController = (StepController) Reflection.createObject(samStep.getClazz());
				new TableItem(samSteps, SWT.NONE).setText(samStepController.getStepName());
			}

			for (String requiredInputColumn : dataAnalysisCompiled.getRequiredInputColumns()) {
				new TableItem(requiredInputs, SWT.NONE).setText(requiredInputColumn);
			}

			for (String generatedOutputColumn : dataAnalysisCompiled.getGeneratedOutputColumns()) {
				new TableItem(generatedOutputs, SWT.NONE).setText(generatedOutputColumn);
			}
			
			TreeSet<String> sortedStrings = new TreeSet<String>();

			for (int repAnalysisId : currentSamAnalysis.getRepAnalyses()) {
				sortedStrings.add(currentRepAnalysisList.get(repAnalysisId).getName());
			}
			
			for (String string : sortedStrings) {
				new TableItem(validRepAnalyses, SWT.NONE).setText(string);
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
		SamAnalysis samAnalysis = getCurrentSamAnalysis();
		Integer selectedDataAnalysisId = (Integer) selection.get(SamAnalysisPart.SELECTION_SAM_ANALYSIS_ID);

		if (samAnalysis == null && selectedDataAnalysisId == null) {
			return false;
		}

		if ((samAnalysis == null && selectedDataAnalysisId != null) || (samAnalysis != null && selectedDataAnalysisId == null)) {
			return true;
		}

		return samAnalysis.getId() != selectedDataAnalysisId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer samAnalysisId = (Integer) selection.get(SamAnalysisPart.SELECTION_SAM_ANALYSIS_ID);

		if (samAnalysisId != null) {
			int commandId = AnalysisCache.getInstance().samAnalysisGet(samAnalysisId, this);
			waitingFor(SAM_ANALYSIS_GET, commandId);
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
	public void samAnalysisGetCompleted(int commandId, SamAnalysis samAnalysis, List<SamStep> samSteps) {
		newObject(SAM_ANALYSIS_GET, new Object[] { samAnalysis, samSteps });
	}

	@Override
	public void samAnalysisGetError(int commandId, String message) {
		raiseGetError(SAM_ANALYSIS_GET, message);		
	}

	@Override
	public void repAnalysisListGetCompleted(int commandId, RepAnalysisList repAnalysisList) {
		currentRepAnalysisList = repAnalysisList;
		doneWaitingFor(REP_ANALYSIS_LIST_GET);
	}

	@Override
	public void repAnalysisListGetError(int commandId, String message) {
		raiseGetError(REP_ANALYSIS_LIST_GET, message);		
	}
}
