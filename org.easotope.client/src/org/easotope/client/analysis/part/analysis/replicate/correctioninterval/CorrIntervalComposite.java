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

package org.easotope.client.analysis.part.analysis.replicate.correctioninterval;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.replicate.ReplicateAnalysisPart;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.DateTimeLabel;
import org.easotope.client.core.widgets.SortedList;
import org.easotope.client.core.widgets.VText;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.AnalysisCacheRepAnalysisListGetListener;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.RepAnalysisList;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrinterval.CorrIntervalCacheCorrIntervalGetListener;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class CorrIntervalComposite extends ChainedComposite implements AnalysisCacheRepAnalysisListGetListener, CorrIntervalCacheCorrIntervalGetListener {
	private Label id;
	private DateTimeLabel validFrom;
	private DateTimeLabel validTo;
	private VText description;
	private SortedList dataAnalyses;

	private RepAnalysisList dataAnalysisList = null;

	private int currentlySelectedCorrIntervalId = DatabaseConstants.EMPTY_DB_ID;
	private int waitingForCommandId = Command.UNDEFINED_ID;
	private CorrIntervalV1 currentCorrInterval = null;

	public CorrIntervalComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.corrIntervalComposite_idLabel);

		id = new Label(this, SWT.NONE);

		Label validFromLabel = new Label(this, SWT.NONE);
		validFromLabel.setText(Messages.corrIntervalComposite_validFromLabel);

		validFrom = new DateTimeLabel(this, SWT.CHECK);
		validFrom.setDefaultText(Messages.corrIntervalComposite_newCorrIntervalValidFrom);

		Label validToLabel = new Label(this, SWT.NONE);
		validToLabel.setText(Messages.corrIntervalComposite_validToLabel);

		validTo = new DateTimeLabel(this, SWT.NONE);
		validTo.setDefaultText(Messages.corrIntervalComposite_newCorrIntervalValidTo);

		Label descriptionLabel = new Label(this, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		descriptionLabel.setLayoutData(gridData);
		descriptionLabel.setText(Messages.corrIntervalComposite_descriptionLabel);

		description = new VText(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		description.setLayoutData(gridData);
		description.setEnabled(false);

		Label dataAnalysisLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		dataAnalysisLabel.setLayoutData(gridData);
		dataAnalysisLabel.setText(Messages.corrIntervalComposite_dataAnalysisLabel);

		dataAnalyses = new SortedList(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		dataAnalyses.setLayoutData(gridData);
		dataAnalyses.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				propogateSelection(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID, (dataAnalyses.getSelectionIndex() == -1) ? null : dataAnalyses.getSelectedInteger());
			}
		});

		AnalysisCache.getInstance().repAnalysisListGet(this);

		AnalysisCache.getInstance().addListener(this);
		CorrIntervalCache.getInstance().addListener(this);

		setVisible(false);
	}

	@Override
	protected void handleDispose() {
		AnalysisCache.getInstance().removeListener(this);
		CorrIntervalCache.getInstance().removeListener(this);
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
		dataAnalyses.setEnabled(true);
	}

	@Override
	protected void setWidgetsDisabled() {
		dataAnalyses.setEnabled(false);
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
		Integer newCorrIntervalId = (Integer) getChainedPart().getSelection().get(ReplicateAnalysisPart.SELECTION_CORR_INTERVAL_ID);

		if (newCorrIntervalId == null) {
			newCorrIntervalId = DatabaseConstants.EMPTY_DB_ID;
		}

		if (newCorrIntervalId != currentlySelectedCorrIntervalId) {
			currentlySelectedCorrIntervalId = newCorrIntervalId;
			waitingForCommandId = Command.UNDEFINED_ID;
			currentCorrInterval = null;

			if (currentlySelectedCorrIntervalId != DatabaseConstants.EMPTY_DB_ID) {
				waitingForCommandId = CorrIntervalCache.getInstance().corrIntervalGet(currentlySelectedCorrIntervalId, this);
			}

			if (currentCorrInterval == null) {
				propogateSelection(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID, null);
			}

			getChainedPart().setCursor();
			setVisible(currentCorrInterval != null);
		}
	}

	private void newDataAnalysisList(RepAnalysisList dataAnalysisList) {
		this.dataAnalysisList = dataAnalysisList;
		refreshDataAnalyses();
	}

	private void refreshDataAnalyses() {
		if (currentCorrInterval != null) {
			HashMap<Integer,String> possibilities = new HashMap<Integer,String>();

			for (int dataAnalysisId : currentCorrInterval.getDataAnalysis()) {
				String name = (dataAnalysisList != null && dataAnalysisList.containsKey(dataAnalysisId)) ? dataAnalysisList.get(dataAnalysisId).getName() : String.valueOf(dataAnalysisId);
				possibilities.put(dataAnalysisId, name);
			}

			dataAnalyses.setPossibilities(possibilities);

			if (possibilities.size() == 1) {
				Integer id = (Integer) possibilities.keySet().toArray()[0];
				dataAnalyses.selectInteger(id);
				propogateSelection(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID, id);

			} else if (dataAnalyses.getSelectionIndex() == -1) {
				dataAnalyses.selectInteger(DatabaseConstants.EMPTY_DB_ID);
				propogateSelection(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID, null);

			} else {
				propogateSelection(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID, dataAnalyses.getSelectedInteger());
			}
		}
	}

	@Override
	public void repAnalysisListGetCompleted(int commandId, RepAnalysisList dataAnalysisList) {
		newDataAnalysisList(dataAnalysisList);
	}

	@Override
	public void repAnalysisListGetError(int commandId, String message) {
		getChainedPart().raiseError(message);
	}

	private void newCorrInterval(CorrIntervalV1 corrInterval) {
		waitingForCommandId = Command.UNDEFINED_ID;
		getChainedPart().setCursor();

		currentCorrInterval = corrInterval;

		id.setText(String.valueOf(currentCorrInterval.getId()));
		validFrom.setDate(currentCorrInterval.getValidFrom());
		validTo.setDate(currentCorrInterval.getValidUntil());
		description.setText(currentCorrInterval.getDescription());
		refreshDataAnalyses();
		
		layout();
		setVisible(true);
	}

	@Override
	public void corrIntervalGetCompleted(int commandId, CorrIntervalV1 corrInterval) {
		if (waitingForCommandId == commandId) {
			newCorrInterval(corrInterval);
		}
	}

	@Override
	public void corrIntervalUpdated(int commandId, CorrIntervalV1 corrInterval) {
		if (currentCorrInterval != null && corrInterval.getId() == currentCorrInterval.getId()) {
			newCorrInterval(corrInterval);
		}
	}

	@Override
	public void corrIntervalGetError(int commandId, String message) {
		if (waitingForCommandId == commandId) {
			waitingForCommandId = DatabaseConstants.EMPTY_DB_ID;
			getChainedPart().setCursor();

			setVisible(false);
			propogateSelection(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID, null);

			getChainedPart().raiseError(message);
		}
	}
}
