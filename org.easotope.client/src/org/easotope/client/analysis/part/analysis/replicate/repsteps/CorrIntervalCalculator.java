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

import java.util.List;
import java.util.Vector;

import org.easotope.client.core.part.ChainedPart;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.repanalysis.AnalysisCacheRepAnalysisGetListener;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrintervalcomp.CorrIntervalCacheCorrIntervalCompGetListener;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.CorrIntervalScratchPad;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.eclipse.swt.widgets.Display;

public class CorrIntervalCalculator implements AnalysisCacheRepAnalysisGetListener, CorrIntervalCacheCorrIntervalCompGetListener {
	private ChainedPart chainedPart;

	private int selectedCorrIntervalId = DatabaseConstants.EMPTY_DB_ID;
	private int selectedDataAnalysisId = DatabaseConstants.EMPTY_DB_ID;

	private int waitingForCorrIntervalCompCommandId = Command.UNDEFINED_ID;
	private List<RepStepParams> loadedRepStepParams = null;
	private ScratchPad<ReplicatePad> loadedCorrIntervalScratchPad = null;
	private List<CorrIntervalError> loadedCorrIntervalErrors = null;

	private int waitingForDataAnalysisCommandId = Command.UNDEFINED_ID;
	private RepAnalysis loadedRepAnalysis = null;
	private List<RepStep> loadedRepSteps = null;

	private Vector<CorrIntervalCalculatorListener> listeners = new Vector<CorrIntervalCalculatorListener>();

	CorrIntervalCalculator(ChainedPart chainedPart) {
		this.chainedPart = chainedPart;
		
		AnalysisCache.getInstance().addListener(this);
		CorrIntervalCache.getInstance().addListener(this);
	}

	void dispose() {
		waitingForCorrIntervalCompCommandId = Command.UNDEFINED_ID;
		waitingForDataAnalysisCommandId = Command.UNDEFINED_ID;
		
		AnalysisCache.getInstance().removeListener(this);
		CorrIntervalCache.getInstance().removeListener(this);
	}

	boolean hasSelection() {
		return selectedCorrIntervalId != DatabaseConstants.EMPTY_DB_ID && selectedDataAnalysisId != DatabaseConstants.EMPTY_DB_ID;
	}

	boolean isLoading() {
		return waitingForDataAnalysisCommandId != Command.UNDEFINED_ID || waitingForCorrIntervalCompCommandId != Command.UNDEFINED_ID;
	}

	boolean isReady() {
		return hasSelection() && !isLoading();
	}

	int getCorrIntervalId() {
		return selectedCorrIntervalId;
	}

	RepAnalysis getRepAnalysis() {
		return loadedRepAnalysis;
	}

	List<RepStep> getRepSteps() {
		return loadedRepSteps;
	}

	List<RepStepParams> getRepStepParams() {
		return loadedRepStepParams;
	}

	ScratchPad<ReplicatePad> getCorrIntervalScratchPad() {
		return loadedCorrIntervalScratchPad;
	}

	public List<CorrIntervalError> getCorrIntervalErrors() {
		return loadedCorrIntervalErrors;
	}

	boolean setSelection(int dataAnalysisId, int corrIntervalId) {
		boolean selectionChanged = false;

		if (corrIntervalId != selectedCorrIntervalId) {
			selectedCorrIntervalId = corrIntervalId;

			waitingForCorrIntervalCompCommandId = Command.UNDEFINED_ID;
			loadedRepStepParams = null;
			loadedCorrIntervalScratchPad = null;
			loadedCorrIntervalErrors= null;

			selectionChanged = true;
		}

		if (dataAnalysisId != selectedDataAnalysisId) {
			selectedDataAnalysisId = dataAnalysisId;

			waitingForCorrIntervalCompCommandId = Command.UNDEFINED_ID;
			loadedRepStepParams = null;
			loadedCorrIntervalScratchPad = null;
			loadedCorrIntervalErrors = null;

			waitingForDataAnalysisCommandId = Command.UNDEFINED_ID;
			loadedRepAnalysis = null;
			loadedRepSteps = null;

			if (selectedDataAnalysisId != DatabaseConstants.EMPTY_DB_ID) {
				waitingForDataAnalysisCommandId = AnalysisCache.getInstance().repAnalysisGet(selectedDataAnalysisId, this);
			}

			selectionChanged = true;
		}

		if (selectionChanged && selectedDataAnalysisId != DatabaseConstants.EMPTY_DB_ID && selectedCorrIntervalId != DatabaseConstants.EMPTY_DB_ID) {
			waitingForCorrIntervalCompCommandId = CorrIntervalCache.getInstance().corrIntervalCompGet(selectedCorrIntervalId, selectedDataAnalysisId, this);
		}

		chainedPart.setCursor();

		return selectionChanged;
	}

	public void addListener(CorrIntervalCalculatorListener listener) {
		listeners.add(listener);
	}

	public void removeListener(CorrIntervalCalculatorListener listener) {
		listeners.remove(listener);
	}

	@Override
	public Display getDisplay() {
		return chainedPart.getParent().getDisplay();
	}

	@Override
	public boolean stillCallabled() {
		return !chainedPart.getParent().isDisposed();
	}

	private void newDataAnalysis(RepAnalysis repAnalysis, List<RepStep> repSteps, boolean notifyListeners) {
		loadedRepAnalysis = repAnalysis;
		loadedRepSteps = repSteps;

		if (notifyListeners) {
			for (CorrIntervalCalculatorListener listener : listeners) {
				listener.corrIntervalCalculatorStatusChanged();
			}
		}
	}

	@Override
	public void repAnalysisGetCompleted(int commandId, RepAnalysis repAnalysis, List<RepStep> repSteps) {
		if (commandId == waitingForDataAnalysisCommandId) {
			waitingForDataAnalysisCommandId = Command.UNDEFINED_ID;
			chainedPart.setCursor();
			newDataAnalysis(repAnalysis, repSteps, commandId != Command.UNDEFINED_ID);
		}
	}

	@Override
	public void repAnalysisGetError(int commandId, String message) {
		if (commandId == waitingForDataAnalysisCommandId) {
			chainedPart.raiseError(message);
			waitingForDataAnalysisCommandId = Command.UNDEFINED_ID;
			chainedPart.setCursor();
		}
	}

	private void newCorrIntervalComp(List<RepStepParams> repStepParams, CorrIntervalScratchPad corrIntervalScratchPad, boolean notifyListeners, List<CorrIntervalError> corrIntervalErrors) {
		loadedRepStepParams = repStepParams;
		loadedCorrIntervalScratchPad = corrIntervalScratchPad.getScratchPad();
		loadedCorrIntervalErrors = corrIntervalErrors;

		if (notifyListeners) {
			for (CorrIntervalCalculatorListener listener : listeners) {
				listener.corrIntervalCalculatorStatusChanged();
			}
		}
	}

	@Override
	public void corrIntervalCompGetCompleted(int commandId, int corrIntervalId, int dataAnalysisTypeId, List<RepStepParams> repStepParams, CorrIntervalScratchPad corrIntervalScratchPad, List<CorrIntervalError> errors) {
		if (commandId == waitingForCorrIntervalCompCommandId) {
			waitingForCorrIntervalCompCommandId = Command.UNDEFINED_ID;
			chainedPart.setCursor();
			newCorrIntervalComp(repStepParams, corrIntervalScratchPad, commandId != Command.UNDEFINED_ID, errors);
		}
	}

	@Override
	public void corrIntervalCompUpdated(int commandId, int corrIntervalId, int dataAnalysisTypeId, List<RepStepParams> repStepParams, CorrIntervalScratchPad corrIntervalScratchPad, List<CorrIntervalError> errors) {
		if (waitingForCorrIntervalCompCommandId == Command.UNDEFINED_ID && selectedCorrIntervalId == corrIntervalId) {
			newCorrIntervalComp(repStepParams, corrIntervalScratchPad, true, errors);
		}
	}

	@Override
	public void corrIntervalCompGetError(int commandId, String message) {
		if (commandId == waitingForCorrIntervalCompCommandId) {
			chainedPart.raiseError(message);
			waitingForCorrIntervalCompCommandId = Command.UNDEFINED_ID;
			chainedPart.setCursor();
		}
	}
}
