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

package org.easotope.client.analysis.part.analysis.sample.samsteps;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.sample.SampleAnalysisPart;
import org.easotope.client.analysis.part.analysis.sampleselector.UserProjSampleSelection;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.framework.commands.Command;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.Reflection;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.samanalysis.AnalysisCacheSamAnalysisGetListener;
import org.easotope.shared.analysis.cache.calculated.CalculatedCache;
import org.easotope.shared.analysis.cache.calculated.samples.CalculatedCacheCalculatedSampleGetListener;
import org.easotope.shared.analysis.cache.calculated.samples.CalculatedSample;
import org.easotope.shared.analysis.execute.AnalysisCompiled;
import org.easotope.shared.analysis.step.StepController;
import org.easotope.shared.analysis.tables.SamAnalysis;
import org.easotope.shared.analysis.tables.SamStep;
import org.easotope.shared.core.PotentialGraphicsMethodsShared;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class SamStepComposite extends ChainedComposite implements AnalysisCacheSamAnalysisGetListener, CalculatedCacheCalculatedSampleGetListener {
	private static final int NO_POSITION = -1;

	private int currentSampleId = DatabaseConstants.EMPTY_DB_ID;
	private int currentSamAnalysisId = DatabaseConstants.EMPTY_DB_ID;
	private int currentSamStepPosition = NO_POSITION;

	private AnalysisCompiled analysisCompiled = null;
	private int waitingForSamAnalysisGetCommandId = Command.UNDEFINED_ID;
	private CalculatedSample calculatedSample = null;
	private int waitingForSampleGetCommandId = Command.UNDEFINED_ID;

	private TabDocsComposite documentationComposite;
	private TabIOComposite ioComposite;
	private TabParametersComposite parameterComposite;
	private TabGraphicsComposite graphicsComposite;
	private TabDependenciesComposite dependenciesComposite;
	private TabResultsComposite resultsComposite;

	public SamStepComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		CTabFolder cTabFolder = new CTabFolder(this, SWT.BORDER);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		cTabFolder.setLayoutData(formData);

		CTabItem cTabItem = new CTabItem(cTabFolder, SWT.NONE);
 		cTabItem.setText(Messages.samStepComposite_documentationTabLabel);
 		documentationComposite = new TabDocsComposite(getChainedPart(), cTabFolder, SWT.NONE);
 		cTabItem.setControl(documentationComposite);

 		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
 		cTabItem.setText(Messages.samStepComposite_ioTabLabel);
 		ioComposite = new TabIOComposite(getChainedPart(), cTabFolder, SWT.NONE);
 		cTabItem.setControl(ioComposite);
 
 		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
 		cTabItem.setText(Messages.samStepComposite_parametersTabLabel);
 		parameterComposite = new TabParametersComposite(getChainedPart(), cTabFolder, SWT.NONE);
 		cTabItem.setControl(parameterComposite);

 		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
 		cTabItem.setText(Messages.samStepComposite_graphicsTabLabel);
 		graphicsComposite = new TabGraphicsComposite(getChainedPart(), cTabFolder, SWT.NONE);
 		cTabItem.setControl(graphicsComposite);

 		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
 		cTabItem.setText(Messages.samStepComposite_dependenciesTabLabel);
 		dependenciesComposite = new TabDependenciesComposite(getChainedPart(), cTabFolder, SWT.NONE);
 		cTabItem.setControl(dependenciesComposite);
 		
 		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
 		cTabItem.setText(Messages.samStepComposite_resultsTabLabel);
 		resultsComposite = new TabResultsComposite(getChainedPart(), cTabFolder, SWT.NONE);
 		cTabItem.setControl(resultsComposite);

 		cTabFolder.setSelection(0);
		setVisible(false);

		AnalysisCache.getInstance().addListener(this);
		CalculatedCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		AnalysisCache.getInstance().removeListener(this);
		CalculatedCache.getInstance().removeListener(this);
	}

	@Override
	public boolean isWaiting() {
		return waitingForSampleGetCommandId != Command.UNDEFINED_ID || waitingForSamAnalysisGetCommandId != Command.UNDEFINED_ID;
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

		UserProjSampleSelection userProjSampleSelection = (UserProjSampleSelection) selection.get(SampleAnalysisPart.SELECTION_USER_PROJ_SAMPLE_IDS);
		Integer samAnalysisId = (Integer) selection.get(SampleAnalysisPart.SELECTION_SAM_ANALYSIS_ID);

		int sampleId = DatabaseConstants.EMPTY_DB_ID;

		if (userProjSampleSelection != null) {
			TreeSet<Integer> sampleIds = userProjSampleSelection.getSampleIds();

			if (sampleIds.size() != 0) {
				sampleId = sampleIds.toArray(new Integer[sampleIds.size()])[0];
			}
		}

		if (samAnalysisId == null) {
			samAnalysisId = DatabaseConstants.EMPTY_DB_ID;
		}

		if (currentSamAnalysisId != samAnalysisId) {
			analysisCompiled = null;

			if (samAnalysisId != DatabaseConstants.EMPTY_DB_ID) {
				waitingForSamAnalysisGetCommandId = Command.UNDEFINED_ID;
				waitingForSamAnalysisGetCommandId = AnalysisCache.getInstance().samAnalysisGet(samAnalysisId, this);
			}
		}

		if (currentSampleId != sampleId || currentSamAnalysisId != samAnalysisId) {
			calculatedSample = null;

			if (sampleId != DatabaseConstants.EMPTY_DB_ID && samAnalysisId != DatabaseConstants.EMPTY_DB_ID) {
				waitingForSampleGetCommandId = Command.UNDEFINED_ID;
				waitingForSampleGetCommandId = CalculatedCache.getInstance().calculatedSampleGet(sampleId, samAnalysisId, this);
			}
		}

		currentSampleId = sampleId;
		currentSamAnalysisId = samAnalysisId;

		Integer samStepPosition = (Integer) selection.get(SampleAnalysisPart.SELECTION_SAMSTEP_POSITION);

		if (samStepPosition == null) {
			samStepPosition = NO_POSITION;
		}

		if (currentSamStepPosition != samStepPosition) {
			currentSamStepPosition = samStepPosition;
		}

		updateWidgets();
	}

	private void updateWidgets() {
		if (currentSampleId == DatabaseConstants.EMPTY_DB_ID || currentSamAnalysisId == DatabaseConstants.EMPTY_DB_ID || currentSamStepPosition == NO_POSITION || analysisCompiled == null) {
			setVisible(false);
			return;
		}

		setVisible(true);

		SamStep samStep = (SamStep) analysisCompiled.getSteps()[currentSamStepPosition];
		StepController controller = (StepController) Reflection.createObject(samStep.getClazz());

		try {
			documentationComposite.setUrl(controller.getDocumentationPath());
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.samStepComposite_errorDocumentation, samStep.getId()), e);
			PotentialGraphicsMethodsShared.reportErrorToUser(getDisplay(), e);
		}

		try {
			ioComposite.setInputs(samStep, controller);
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.samStepComposite_errorIO, samStep.getId()), e);
			PotentialGraphicsMethodsShared.reportErrorToUser(getDisplay(), e);
		}

		try {
			parameterComposite.setCompositeClass(controller.getParameterComposite());
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.samStepComposite_errorParameters, samStep.getId()), e);
			PotentialGraphicsMethodsShared.reportErrorToUser(getDisplay(), e);
		}

		try {
			graphicsComposite.setCompositeClass(controller.getGraphicComposite());
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.samStepComposite_errorGraphicsComposite, samStep.getId()), e);
			PotentialGraphicsMethodsShared.reportErrorToUser(getDisplay(), e);
		}

		try {
			dependenciesComposite.updateData(calculatedSample, currentSamStepPosition);
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.samStepComposite_errorDependencies, samStep.getId()), e);
			PotentialGraphicsMethodsShared.reportErrorToUser(getDisplay(), e);
		}

		try {
			resultsComposite.updateData(calculatedSample, samStep);
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.samStepComposite_errorResults, samStep.getId()), e);
			PotentialGraphicsMethodsShared.reportErrorToUser(getDisplay(), e);
		}
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	@Override
	public void samAnalysisGetCompleted(int commandId, SamAnalysis samAnalysis, List<SamStep> samSteps) {
		if (waitingForSamAnalysisGetCommandId == commandId) {
			waitingForSamAnalysisGetCommandId = Command.UNDEFINED_ID;
			getChainedPart().setCursor();
			analysisCompiled = new AnalysisCompiled(samAnalysis, samSteps);

			if (commandId != Command.UNDEFINED_ID) {
				updateWidgets();
			}
		}
	}

	@Override
	public void samAnalysisGetError(int commandId, String message) {
		if (waitingForSamAnalysisGetCommandId == commandId) {
			waitingForSamAnalysisGetCommandId = Command.UNDEFINED_ID;
			getChainedPart().setCursor();
			getChainedPart().raiseError(message);
		}
	}

	@Override
	public void calculatedSampleGetCompleted(int commandId, CalculatedSample calculatedSample) {
		if (waitingForSampleGetCommandId == commandId) {
			waitingForSampleGetCommandId = Command.UNDEFINED_ID;
			getChainedPart().setCursor();
			this.calculatedSample = calculatedSample;

			if (commandId != Command.UNDEFINED_ID) {
				updateWidgets();
			}
		}
	}

	@Override
	public void calculatedSampleUpdated(int commandId, CalculatedSample calculatedSample) {
		if (this.calculatedSample != null && this.calculatedSample.getSampleId() == calculatedSample.getSampleId() && this.calculatedSample.getSampleAnalysisId() == calculatedSample.getSampleAnalysisId()) {
			this.calculatedSample = calculatedSample;
			updateWidgets();
		}
	}

	@Override
	public void calculatedSampleGetError(int commandId, String message) {
		if (waitingForSampleGetCommandId == commandId) {
			waitingForSampleGetCommandId = Command.UNDEFINED_ID;
			getChainedPart().setCursor();
			getChainedPart().raiseError(message);
		}
	}
}
