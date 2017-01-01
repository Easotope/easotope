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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.replicate.ReplicateAnalysisPart;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.Reflection;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.analysis.execute.AnalysisCompiled;
import org.easotope.shared.analysis.step.StepController;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.UnexpectedException;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class RepStepComposite extends ChainedComposite implements RunCalculatorListener, CorrIntervalCalculatorListener {
	private Integer repStepPosition;
	private RepStep currentRepStep;

	private CorrIntervalCalculator corrIntervalCalculator;
	private RunCalculator runCalculator;

	private CTabFolder cTabFolder;
	private int tabToSelect = -1;

	private TabOffsetsComposite offsetsComposite;
	private TabDocsComposite documentationComposite;
	private TabIOComposite ioComposite;
	private TabParametersComposite parameterComposite;
	private TabGraphicsComposite graphicsComposite;
	private TabDependenciesComposite dependenciesComposite;
	private TabResultsComposite resultsComposite;

	public RepStepComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		cTabFolder = new CTabFolder(this, SWT.BORDER);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		cTabFolder.setLayoutData(formData);

 		CTabItem tabItem = new CTabItem(cTabFolder, SWT.NONE);
 		tabItem.setText(Messages.repStepComposite_offsetsLabel);
 		offsetsComposite = new TabOffsetsComposite(chainedPart, cTabFolder, SWT.NONE);
 		tabItem.setControl(offsetsComposite);

 		cTabFolder.setSelection(0);

 		corrIntervalCalculator = new CorrIntervalCalculator(chainedPart);
 		corrIntervalCalculator.addListener(this);
 
		runCalculator = new RunCalculator(chainedPart);
		runCalculator.addListener(this);

		setVisible(false);
	}

	@Override
	protected void handleDispose() {
		runCalculator.dispose();
		corrIntervalCalculator.dispose();
	}

	@Override
	public boolean isWaiting() {
		return corrIntervalCalculator.isLoading() || runCalculator.isLoading();
	}

	@Override
	protected void setWidgetsEnabled() {
		// ignore
	}

	@Override
	protected void setWidgetsDisabled() {
		// ignore
	}

	@Override
	protected void receiveAddRequest() {
		// ignore
	}

	@Override
	protected void cancelAddRequest() {
		// ignore
	}

	@Override
	protected void receiveSelection() {
		HashMap<String,Object> selection = getChainedPart().getSelection();

		Integer replicateId = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_REPLICATE_ID);
		Integer dataAnalysisId = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID);
		Integer corrIntervalId = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_CORR_INTERVAL_ID);

		if (replicateId == null) {
			replicateId = DatabaseConstants.EMPTY_DB_ID;
		}

		if (dataAnalysisId == null) {
			dataAnalysisId = DatabaseConstants.EMPTY_DB_ID;
		}

		if (corrIntervalId == null) {
			corrIntervalId = DatabaseConstants.EMPTY_DB_ID;
		}

		boolean dataChanged = corrIntervalCalculator.setSelection(dataAnalysisId, corrIntervalId);

		if (dataChanged) {
			RepAnalysis repAnalysis = corrIntervalCalculator.getRepAnalysis();
			List<RepStep> repSteps = corrIntervalCalculator.getRepSteps();
			List<RepStepParams> repStepParams = corrIntervalCalculator.getRepStepParams();
			ScratchPad<ReplicatePad> corrIntervalScratchPad = corrIntervalCalculator.getCorrIntervalScratchPad();
			List<CorrIntervalError> corrIntervalErrors = corrIntervalCalculator.getCorrIntervalErrors();

			runCalculator.setSelection(replicateId, corrIntervalId, repAnalysis, repSteps, repStepParams, corrIntervalScratchPad, corrIntervalErrors);

		} else {
			dataChanged = runCalculator.setSelection(replicateId);
		}

		Integer repStepPosition = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_REPSTEP_POSITION);
		boolean repStepPositionChanged = false;

		if (this.repStepPosition != repStepPosition) {
			this.repStepPosition = repStepPosition;
			repStepPositionChanged = true;
		}

		if (dataChanged || repStepPositionChanged) {
			updateWidgets(dataChanged);
		}
	}

	private void updateWidgets(boolean dataChanged) {
		if (!corrIntervalCalculator.isReady()) {
			setVisible(false);
			runCalculator.setSelection(DatabaseConstants.EMPTY_DB_ID, null, null, null, null, null);
			return;
		}

		setVisible(true);

		if (dataChanged) {
			try {
				AnalysisCompiled dataAnalysisCompiled = new AnalysisCompiled(corrIntervalCalculator.getRepAnalysis(), corrIntervalCalculator.getRepSteps());
				offsetsComposite.setCorrIntervalInfo(dataAnalysisCompiled, corrIntervalCalculator.getCorrIntervalScratchPad());

			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, this, Messages.repStepComposite_errorScratchPad, e);
				UnexpectedException.reportErrorToUser(getDisplay(), e);
				offsetsComposite.setCorrIntervalInfo(null, null);
			}
		}

		cTabFolder.setRedraw(false);

		List<RepStep> currentRepSteps = corrIntervalCalculator.getRepSteps();
		RepStep repStep = (currentRepSteps != null && repStepPosition != null && repStepPosition < currentRepSteps.size()) ? currentRepSteps.get(repStepPosition) : null;

		if (repStep == null) {
			currentRepStep = null;

			if (cTabFolder.getItemCount() != 1) {
				tabToSelect = cTabFolder.getSelectionIndex();
			}

			while (cTabFolder.getItemCount() != 1) {
				CTabItem cTabItem = cTabFolder.getItems()[cTabFolder.getItemCount()-1];
				cTabItem.getControl().dispose();
				cTabItem.dispose();
			}

			documentationComposite = null;
			ioComposite = null;
			parameterComposite = null;
			graphicsComposite = null;
			dependenciesComposite = null;
			resultsComposite = null;

			cTabFolder.setRedraw(true);

			return;
		}

		boolean needToUpdateData = dataChanged;

		if (cTabFolder.getItemCount() == 1) {
			CTabItem cTabItem = new CTabItem(cTabFolder, SWT.NONE);
	 		cTabItem.setText(Messages.repStepComposite_documentationTabLabel);
	 		documentationComposite = new TabDocsComposite(getChainedPart(), cTabFolder, SWT.NONE);
	 		cTabItem.setControl(documentationComposite);

	 		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
	 		cTabItem.setText(Messages.repStepComposite_ioTabLabel);
	 		ioComposite = new TabIOComposite(getChainedPart(), cTabFolder, SWT.NONE);
	 		cTabItem.setControl(ioComposite);
	 
	 		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
	 		cTabItem.setText(Messages.repStepComposite_parametersTabLabel);
	 		parameterComposite = new TabParametersComposite(getChainedPart(), cTabFolder, SWT.NONE);
	 		cTabItem.setControl(parameterComposite);

	 		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
	 		cTabItem.setText(Messages.repStepComposite_graphicsTabLabel);
	 		graphicsComposite = new TabGraphicsComposite(getChainedPart(), cTabFolder, SWT.NONE);
	 		graphicsComposite.setRunCalculator(runCalculator);
	 		cTabItem.setControl(graphicsComposite);

	 		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
	 		cTabItem.setText(Messages.repStepComposite_dependenciesTabLabel);
	 		dependenciesComposite = new TabDependenciesComposite(getChainedPart(), cTabFolder, SWT.NONE);
	 		dependenciesComposite.setRunCalculator(runCalculator);
	 		cTabItem.setControl(dependenciesComposite);

	 		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
	 		cTabItem.setText(Messages.repStepComposite_resultsTabLabel);
	 		resultsComposite = new TabResultsComposite(getChainedPart(), cTabFolder, SWT.NONE);
	 		resultsComposite.setRunCalculator(runCalculator);
	 		cTabItem.setControl(resultsComposite);

	 		if (tabToSelect != -1) {
	 			cTabFolder.setSelection(tabToSelect);
	 		}

	 		needToUpdateData = true;
		}

		if (currentRepStep == null || repStep.getId() != currentRepStep.getId()) {
			currentRepStep = repStep;
			StepController controller = (StepController) Reflection.createObject(repStep.getClazz());

			try {
				documentationComposite.setUrl(controller.getDocumentationPath());
			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.repStepComposite_errorDocumentation, repStep.getId()), e);
				UnexpectedException.reportErrorToUser(getDisplay(), e);
			}

			try {
				ioComposite.setInputs(repStep, controller);
			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.repStepComposite_errorIO, repStep.getId()), e);
				UnexpectedException.reportErrorToUser(getDisplay(), e);
			}

			try {
				parameterComposite.setCompositeClass(controller.getParameterComposite());
			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.repStepComposite_errorParameters, repStep.getId()), e);
				UnexpectedException.reportErrorToUser(getDisplay(), e);
			}

			try {
				graphicsComposite.setCompositeClass(controller.getGraphicComposite());
			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.repStepComposite_errorGraphicsComposite, repStep.getId()), e);
				UnexpectedException.reportErrorToUser(getDisplay(), e);
			}

			needToUpdateData = true;
		}

		if (needToUpdateData) {
			try {
				graphicsComposite.updateData();
			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.repStepComposite_errorGraphics, repStep.getId()), e);
				UnexpectedException.reportErrorToUser(getDisplay(), e);
			}

			try {
				dependenciesComposite.updateData(repStep);
			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.repStepComposite_errorDependencies, repStep.getId()), e);
				UnexpectedException.reportErrorToUser(getDisplay(), e);
			}

			try {
				resultsComposite.updateData(repStep);
			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.repStepComposite_errorResults, repStep.getId()), e);
				UnexpectedException.reportErrorToUser(getDisplay(), e);
			}
		}

		cTabFolder.setRedraw(true);
	}

	@Override
	public void runCalculatorStatusChanged() {
		updateWidgets(true);
	}

	@Override
	public void corrIntervalCalculatorStatusChanged() {
		int corrIntervalId = corrIntervalCalculator.getCorrIntervalId();
		RepAnalysis repAnalysis = corrIntervalCalculator.getRepAnalysis();
		List<RepStep> repSteps = corrIntervalCalculator.getRepSteps();
		List<RepStepParams> repStepParameters = corrIntervalCalculator.getRepStepParams();
		ScratchPad<ReplicatePad> corrIntervalScratchPad = corrIntervalCalculator.getCorrIntervalScratchPad();
		List<CorrIntervalError> corrIntervalErrors = corrIntervalCalculator.getCorrIntervalErrors();

		runCalculator.setSelection(corrIntervalId, repAnalysis, repSteps, repStepParameters, corrIntervalScratchPad, corrIntervalErrors);

		updateWidgets(true);
	}
}
