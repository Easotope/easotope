/*
 * Copyright © 2016-2018 by Devon Bowen.
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

package org.easotope.client.analysis.part.analysis.replicate.repstepselector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.replicate.ReplicateAnalysisPart;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.SortedList;
import org.easotope.framework.commands.Command;
import org.easotope.framework.core.util.Reflection;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.repanalysis.AnalysisCacheRepAnalysisGetListener;
import org.easotope.shared.analysis.step.StepController;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public class RepStepSelectorComposite extends ChainedComposite implements AnalysisCacheRepAnalysisGetListener {
	private List repSteps;

	private int currentlySelectedRepAnalysisId = DatabaseConstants.EMPTY_DB_ID;
	private RepAnalysis currentRepAnalysis = null;
	private int waitingForCommandId = Command.UNDEFINED_ID;
	private HashMap<Integer,Integer> indexToPosition = new HashMap<Integer,Integer>();

	public RepStepSelectorComposite(ChainedPart chainedPart, Composite parent, int style) {
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
		label.setText(Messages.repStepSelector_title);

		repSteps = new SortedList(this, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		repSteps.setLayoutData(formData);
		repSteps.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				propogateSelection(ReplicateAnalysisPart.SELECTION_REPSTEP_POSITION, (repSteps.getSelectionIndex() == -1) ? null : indexToPosition.get(repSteps.getSelectionIndex()));
			}
		});

		AnalysisCache.getInstance().addListener(this);

		setVisible(false);
	}

	@Override
	protected void handleDispose() {
		AnalysisCache.getInstance().removeListener(this);
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	@Override
	public boolean isWaiting() {
		return waitingForCommandId != Command.UNDEFINED_ID;
	}

	@Override
	protected void setWidgetsEnabled() {
		repSteps.setEnabled(true);
	}

	@Override
	protected void setWidgetsDisabled() {
		repSteps.setEnabled(false);
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
		Integer newDataAnalysisId = (Integer) getChainedPart().getSelection().get(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID);
		
		if (newDataAnalysisId == null) {
			newDataAnalysisId = DatabaseConstants.EMPTY_DB_ID;
		}

		if (newDataAnalysisId != currentlySelectedRepAnalysisId) {
			currentlySelectedRepAnalysisId = newDataAnalysisId;
			waitingForCommandId = Command.UNDEFINED_ID;
			currentRepAnalysis = null;

			if (currentlySelectedRepAnalysisId != DatabaseConstants.EMPTY_DB_ID) {
				waitingForCommandId = AnalysisCache.getInstance().repAnalysisGet(currentlySelectedRepAnalysisId, this);
			}

			getChainedPart().setCursor();

			if (currentRepAnalysis == null) {
				propogateSelection(ReplicateAnalysisPart.SELECTION_REPSTEP_POSITION, null);
			}

			setVisible(currentRepAnalysis != null);
		}
	}

	private void newRepAnalysis(RepAnalysis repAnalysis, java.util.List<RepStep> repStepList) {
		waitingForCommandId = Command.UNDEFINED_ID;
		getChainedPart().setCursor();

		currentRepAnalysis = repAnalysis;

		int newSelectedIndex = -1;
		String selectedRepStepName = repSteps.getSelectionIndex() != -1 ? repSteps.getItem(repSteps.getSelectionIndex()) : null;

		repSteps.removeAll();
		indexToPosition.clear();

		ArrayList<RepStep> sortedRepStepList = new ArrayList<RepStep>(repStepList);
		Collections.sort(sortedRepStepList, new RepStepComparator());

		int position = 0;
		for (RepStep repStep : sortedRepStepList) {
			if (repStep.isApplyToResults()) {
				indexToPosition.put(repSteps.getItemCount(), position);
				StepController controller = (StepController) Reflection.createObject(repStep.getClazz());

				if (controller.getStepName().equals(selectedRepStepName)) {
					newSelectedIndex = repSteps.getItemCount();
				}

				repSteps.add(controller.getStepName());
			}

			position++;
		}

		if (newSelectedIndex != -1) {
			repSteps.select(newSelectedIndex);
			propogateSelection(ReplicateAnalysisPart.SELECTION_REPSTEP_POSITION, indexToPosition.get(newSelectedIndex));
		} else {
			propogateSelection(ReplicateAnalysisPart.SELECTION_REPSTEP_POSITION, null);
		}

		layout();
		setVisible(true);
	}

	@Override
	public void repAnalysisGetCompleted(int commandId, RepAnalysis repAnalysis, java.util.List<RepStep> repSteps) {
		if (waitingForCommandId == commandId) {
			newRepAnalysis(repAnalysis, new ArrayList<RepStep>(repSteps));
		}
	}

	@Override
	public void repAnalysisGetError(int commandId, String message) {
		if (waitingForCommandId == commandId) {
			waitingForCommandId = DatabaseConstants.EMPTY_DB_ID;
			getChainedPart().setCursor();

			setVisible(false);
			propogateSelection(ReplicateAnalysisPart.SELECTION_REPSTEP_POSITION, null);

			getChainedPart().raiseError(message);
		}
	}

	private class RepStepComparator implements Comparator<RepStep> {
		@Override
		public int compare(RepStep arg0, RepStep arg1) {
			return ((Integer) arg0.getPosition()).compareTo(arg1.getPosition());
		}
	}
}
