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

package org.easotope.client.analysis.part.analysis.sample.table;

import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.sample.SampleAnalysisPart;
import org.easotope.client.analysis.part.analysis.sampleselector.UserProjSampleSelection;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.scratchpadtable.RepAnalysisChoice;
import org.easotope.client.core.scratchpadtable.ScratchPadTable;
import org.easotope.client.core.scratchpadtable.ScratchPadTableListener;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.AnalysisCacheRepAnalysisListGetListener;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.RepAnalysisList;
import org.easotope.shared.analysis.cache.calculated.CalculatedCache;
import org.easotope.shared.analysis.cache.calculated.repanalysischoice.CalculatedCacheRepAnalysisChoiceSaveListener;
import org.easotope.shared.analysis.cache.calculated.samples.CalculatedCacheCalculatedSampleGetListener;
import org.easotope.shared.analysis.cache.calculated.samples.CalculatedSample;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.tables.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;


public class TableComposite extends EditorComposite implements AnalysisCacheRepAnalysisListGetListener, CalculatedCacheCalculatedSampleGetListener, CalculatedCacheRepAnalysisChoiceSaveListener {
	private final String TABLE_LAYOUT_CONTEXT = "SAMPLE RESULTS";

	private static final String CALCULATED_SAMPLE_GET = "CALCULATED_SAMPLE_GET";
	private static final String REP_ANALYSIS_CHOICE_SAVE = "REP_ANALYSIS_IDS_SAVE";

	private StackLayout stackLayout;
	private ErrorComposite errorComposite;
	private ScratchPadTable scratchPadTable;

	RepAnalysisList repAnalysisList = null;

	public TableComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		stackLayout = new StackLayout();
		setLayout(stackLayout);

		errorComposite = new ErrorComposite(this, SWT.NONE);
		scratchPadTable = new ScratchPadTable(this, true, false);
		scratchPadTable.addListener(new ScratchPadTableListener() {
			@Override
			public void tableModified() {
				widgetStatusChanged();
			}

			@Override
			public void tableLayoutNeedsSaving(TableLayout tableLayout) {
				tableLayout.setUserId(LoginInfoCache.getInstance().getUser().getId());
				tableLayout.setContext(TABLE_LAYOUT_CONTEXT);
				tableLayout.setDataAnalysisId(getCurrentCalculatedSample().getSampleAnalysisId());

				LoginInfoCache.getInstance().saveTableLayout(tableLayout);
			}
		});

		stackLayout.topControl = scratchPadTable;
		layout();

		CalculatedCache.getInstance().addListener(this);
		AnalysisCache.getInstance().repAnalysisListGet(this);
	}

	@Override
	protected void handleDispose() {
		CalculatedCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private CalculatedSample getCurrentCalculatedSample() {
		return (CalculatedSample) getCurrentObject();
	}

	@SuppressWarnings("unused")
	@Override
	protected void setCurrentFieldValues() {
		CalculatedSample calculatedSample = getCurrentCalculatedSample();

		//TODO fix this???
		if (false) { // calculatedSample.getErrorMessage() != null) {
			errorComposite.setErrorMessage(calculatedSample.getErrorSamStep(), calculatedSample.getErrorMessage());
			stackLayout.topControl = errorComposite;

		} else {
			ColumnOrdering columnOrdering = calculatedSample.getColumnOrdering();
			FormatLookup formatLookup = calculatedSample.getFormatLookup();
			TableLayout tableLayout = LoginInfoCache.getInstance().getTableLayout(calculatedSample.getSampleAnalysisId(), TABLE_LAYOUT_CONTEXT);
			
			ArrayList<ArrayList<RepAnalysisChoice>> repAnalysisChoices = new ArrayList<ArrayList<RepAnalysisChoice>>();
			int[][] potentialRepAnalyses = calculatedSample.getPotentialRepAnalyses();
			
			if (potentialRepAnalyses != null) {
				for (int[] choices : potentialRepAnalyses) {
					ArrayList<RepAnalysisChoice> choicesAsStrings = new ArrayList<RepAnalysisChoice>();
					repAnalysisChoices.add(choicesAsStrings);

					for (int repAnalysisId : choices) {
						String repAnalysisName = repAnalysisList == null || repAnalysisList.get(repAnalysisId) == null ? String.valueOf(repAnalysisId) : repAnalysisList.get(repAnalysisId).getName();
						choicesAsStrings.add(new RepAnalysisChoice(repAnalysisId, repAnalysisName));
					}
				}
			}

			scratchPadTable.setScratchPad(calculatedSample.getSampleScratchPad(), columnOrdering, formatLookup, tableLayout, calculatedSample.getReplicateIds(), repAnalysisChoices);
			stackLayout.topControl = scratchPadTable;
		}
	}

	@Override
	protected void setDefaultFieldValues() {
		// ignore
	}

	@Override
	public void enableWidgets() {
		scratchPadTable.setEnabled(true);
	}

	@Override
	public void disableWidgets() {
		scratchPadTable.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		return scratchPadTable.isDirty();
	}

	@Override
	protected boolean hasError() {
		return false;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String, Object> selection) {		
		UserProjSampleSelection userProjSampleSelection = (UserProjSampleSelection) selection.get(SampleAnalysisPart.SELECTION_USER_PROJ_SAMPLE_IDS);
		Integer selectedSampleAnalysisId = (Integer) selection.get(SampleAnalysisPart.SELECTION_SAM_ANALYSIS_ID);

		Integer selectedSampleId = null;

		if (userProjSampleSelection != null && !userProjSampleSelection.getSampleIds().isEmpty()) {
			selectedSampleId = userProjSampleSelection.getSampleIds().toArray(new Integer[userProjSampleSelection.getSampleIds().size()])[0];
		}

		CalculatedSample calculatedSample = getCurrentCalculatedSample();

		if (calculatedSample == null && selectedSampleId == null && selectedSampleAnalysisId == null) {
			return false;
		}

		if (calculatedSample != null && selectedSampleId != null && selectedSampleId == calculatedSample.getSampleId() && selectedSampleAnalysisId != null && selectedSampleAnalysisId == calculatedSample.getSampleAnalysisId()) {
			return false;
		}

		return true;
	}

	@Override
	protected boolean processSelection(HashMap<String, Object> selection) {
		UserProjSampleSelection userProjSampleSelection = (UserProjSampleSelection) selection.get(SampleAnalysisPart.SELECTION_USER_PROJ_SAMPLE_IDS);
		Integer sampleAnalysisId = (Integer) selection.get(SampleAnalysisPart.SELECTION_SAM_ANALYSIS_ID);

		Integer sampleId = null;

		if (userProjSampleSelection != null && !userProjSampleSelection.getSampleIds().isEmpty()) {
			sampleId = userProjSampleSelection.getSampleIds().toArray(new Integer[userProjSampleSelection.getSampleIds().size()])[0];
		}

		if (sampleId != null && sampleAnalysisId != null) {
			int commandId = CalculatedCache.getInstance().calculatedSampleGet(sampleId, sampleAnalysisId, this);
			waitingFor(CALCULATED_SAMPLE_GET, commandId);
			return true;
		}

		return false;
	}

	@Override
	protected void requestSave(boolean isResend) {
		HashMap<Integer,Integer> replicateIdToRepAnalysisIds = new HashMap<Integer,Integer>();
		HashMap<Integer,RepAnalysisChoice> changes = scratchPadTable.getReplicateIdToRepAnalysisChoice();
		
		for (Integer key : changes.keySet()) {
			replicateIdToRepAnalysisIds.put(key, changes.get(key).getRepAnalysisId());
		}

		int commandId = CalculatedCache.getInstance().repAnalysisChoiceSave(getCurrentCalculatedSample().getSampleId(), getCurrentCalculatedSample().getSampleAnalysisId(), replicateIdToRepAnalysisIds, this);
		waitingFor(REP_ANALYSIS_CHOICE_SAVE, commandId);
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
	public void repAnalysisListGetCompleted(int commandId, RepAnalysisList repAnalysisList) {
		this.repAnalysisList = repAnalysisList;
	}

	@Override
	public void repAnalysisListGetError(int commandId, String message) {
		getChainedPart().raiseError(message);
	}

	@Override
	public void calculatedSampleGetCompleted(int commandId, CalculatedSample calculatedSample) {
		newObject(CALCULATED_SAMPLE_GET, calculatedSample);
	}

	@Override
	public void calculatedSampleUpdated(int commandId, CalculatedSample calculatedSample) {
		updateObject(calculatedSample, Messages.calculatedSampleTableComposite_calculatedSampleHasBeenUpdated);
	}

	@Override
	public void calculatedSampleGetError(int commandId, String message) {
		raiseGetError(CALCULATED_SAMPLE_GET, message);		
	}

	@Override
	public void repAnalysisChoiceSaveCompleted(int commandId) {
		saveComplete(REP_ANALYSIS_CHOICE_SAVE, getCurrentObject());
	}

	@Override
	public void repAnalysisChoiceSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(REP_ANALYSIS_CHOICE_SAVE, message);
	}

	@Override
	protected boolean newIsReplacementForOld(Object oldObject, Object newObject) {
		if (oldObject == null || newObject == null) {
			return false;
		}

		CalculatedSample oldCalculatedSample = (CalculatedSample) oldObject;
		CalculatedSample newCalculatedSample = (CalculatedSample) newObject;

		return oldCalculatedSample.getSampleId() == newCalculatedSample.getSampleId() && oldCalculatedSample.getSampleAnalysisId() == newCalculatedSample.getSampleAnalysisId();
	}
}
