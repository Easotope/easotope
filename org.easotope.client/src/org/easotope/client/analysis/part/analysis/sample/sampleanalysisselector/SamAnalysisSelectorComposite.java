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

package org.easotope.client.analysis.part.analysis.sample.sampleanalysisselector;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.sample.SampleAnalysisPart;
import org.easotope.client.analysis.part.analysis.sampleselector.UserProjSampleSelection;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.SortedList;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.samanalysislist.AnalysisCacheSamAnalysisListGetListener;
import org.easotope.shared.analysis.cache.analysis.samanalysislist.SamAnalysisList;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.sample.InputCacheSampleGetListener;
import org.easotope.shared.rawdata.tables.Sample;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;


public class SamAnalysisSelectorComposite extends ChainedComposite implements InputCacheSampleGetListener, AnalysisCacheSamAnalysisListGetListener {
	private SortedList samAnalyses;
	private int lastSelectedSamAnalysisId = DatabaseConstants.EMPTY_DB_ID;

	private SamAnalysisList samAnalysisList;
	private Sample currentSample = null;
	private int waitingForCommandId = Command.UNDEFINED_ID;
	private int currentlySelectedSampleId = DatabaseConstants.EMPTY_DB_ID;

	public SamAnalysisSelectorComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);
		
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Label label = new Label(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		label.setLayoutData(formData);
		label.setText(Messages.samAnalysisSelector_title);

		samAnalyses = new SortedList(this, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		samAnalyses.setLayoutData(formData);
		samAnalyses.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				lastSelectedSamAnalysisId = samAnalyses.getSelectedInteger();
				propogateSelection(SampleAnalysisPart.SELECTION_SAM_ANALYSIS_ID, samAnalyses.getSelectionCount() == 0 ? null : samAnalyses.getSelectedInteger());
			}
		});

		AnalysisCache.getInstance().samAnalysisListGet(this);
		AnalysisCache.getInstance().addListener(this);
		InputCache.getInstance().addListener(this);

		setVisible(false);
	}

	@Override
	protected void handleDispose() {
		AnalysisCache.getInstance().removeListener(this);
	}

	@Override
	public boolean isWaiting() {
		return waitingForCommandId != Command.UNDEFINED_ID;
	}

	@Override
	protected void setWidgetsEnabled() {
		samAnalyses.setEnabled(true);
	}

	@Override
	protected void setWidgetsDisabled() {
		samAnalyses.setEnabled(false);		
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
		Integer newSampleId = null;
		UserProjSampleSelection userProjSampleSelection = (UserProjSampleSelection) getChainedPart().getSelection().get(SampleAnalysisPart.SELECTION_USER_PROJ_SAMPLE_IDS);

		if (userProjSampleSelection != null && !userProjSampleSelection.getSampleIds().isEmpty()) {
			newSampleId = userProjSampleSelection.getSampleIds().toArray(new Integer[userProjSampleSelection.getSampleIds().size()])[0];
		}

		if (newSampleId == null) {
			newSampleId = DatabaseConstants.EMPTY_DB_ID;
		}

		if (newSampleId != currentlySelectedSampleId) {
			currentlySelectedSampleId = newSampleId;
			waitingForCommandId = Command.UNDEFINED_ID;
			currentSample = null;

			if (currentlySelectedSampleId != DatabaseConstants.EMPTY_DB_ID) {
				waitingForCommandId = InputCache.getInstance().sampleGet(this, currentlySelectedSampleId);
			}

			getChainedPart().setCursor();

			if (currentSample == null) {
				propogateSelection(SampleAnalysisPart.SELECTION_SAM_ANALYSIS_ID, null);
			}

			reloadList();
		}
	}

	private void reloadList() {
		if (currentSample == null) {
			setVisible(false);
			return;
		}

		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();

		if (currentSample.getSamAnalyses() != null) {
			for (int samAnalysisId : currentSample.getSamAnalyses()) {
				if (samAnalysisList == null) {
					possibilities.put(samAnalysisId, String.valueOf(samAnalysisId));
				} else {
					possibilities.put(samAnalysisId, samAnalysisList.get(samAnalysisId).getName());
				}
			}
		}

		samAnalyses.setPossibilities(possibilities);

		if (possibilities.size() == 1) {
			int integer = possibilities.keySet().toArray(new Integer[1])[0];
			samAnalyses.selectInteger(integer);

		} else if (possibilities.size() != 0 && lastSelectedSamAnalysisId != DatabaseConstants.EMPTY_DB_ID) {
			samAnalyses.selectInteger(lastSelectedSamAnalysisId);

		} else {
			samAnalyses.selectInteger(-1);
		}

		propogateSelection(SampleAnalysisPart.SELECTION_SAM_ANALYSIS_ID, samAnalyses.getSelectionCount() == 0 ? null : samAnalyses.getSelectedInteger());

		setVisible(true);
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	@Override
	public void samAnalysisListGetCompleted(int commandId, SamAnalysisList samAnalysisList) {
		this.samAnalysisList = samAnalysisList;
		reloadList();
	}

	@Override
	public void samAnalysisListGetError(int commandId, String message) {
		getChainedPart().raiseError(message);
	}

	@Override
	public void sampleGetCompleted(int commandId, Sample sample) {
		if (commandId == waitingForCommandId) {
			currentSample = sample;
			waitingForCommandId = Command.UNDEFINED_ID;
			getChainedPart().setCursor();
			reloadList();
		}
	}

	@Override
	public void sampleUpdated(int commandId, Sample sample) {
		if (currentSample != null && currentSample.getId() == sample.getId()) {
			currentSample = sample;
			reloadList();
		}
	}

	@Override
	public void sampleGetError(int commandId, String message) {
		if (commandId == waitingForCommandId) {
			currentSample = null;
			waitingForCommandId = Command.UNDEFINED_ID;
			getChainedPart().setCursor();
			reloadList();
			getChainedPart().raiseError(message);
		}
	}

	@Override
	public void sampleDeleted(int sampleId) {
		//TODO how to handle??
	}
}
