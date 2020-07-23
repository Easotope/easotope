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

package org.easotope.client.analysis.part.analysis.replicate.repsteps;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.easotope.client.core.part.ChainedPart;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.admin.tables.SampleType;
import org.easotope.shared.analysis.execute.AnalysisCalculator;
import org.easotope.shared.analysis.execute.AnalysisCalculatorListener;
import org.easotope.shared.analysis.execute.AnalysisWithParameters;
import org.easotope.shared.analysis.execute.CalculationError;
import org.easotope.shared.analysis.execute.calculator.AnalysisConstants;
import org.easotope.shared.analysis.execute.calculator.SingleReplicateCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.RawDataHelper;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.replicate.InputCacheReplicateGetListener;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.eclipse.swt.widgets.Display;

public class RunCalculator implements InputCacheReplicateGetListener, AnalysisCalculatorListener {
	public final String VOLATILE_DATA_CALCULATION_PAD = "VOLATILE_DATA_CALCULATION_PAD"; 

	private ChainedPart chainedPart;

	private int currentlySelectedReplicateId = DatabaseConstants.EMPTY_DB_ID;
	private int waitingForReplicateCommandId = Command.UNDEFINED_ID;
	private ReplicateV1 currentReplicate = null;
	private ScratchPad<ReplicatePad> currentReplicateScratchPad = null;

	private int currentCorrIntervalId = DatabaseConstants.EMPTY_DB_ID;
	private RepAnalysis currentRepAnalysis = null;
	private List<RepStep> currentRepSteps = null;
	private List<RepStepParams> currentRepStepParams = null;
	private ScratchPad<ReplicatePad> currentCorrIntervalScratchPadCopy = null;
	private List<CorrIntervalError> currentCorrIntervalErrors = null;

	private SingleReplicateCalculator singleRunCalculator = null;
	private ScratchPad<ReplicatePad> replicatesTemporarilyRemovedFromCurrentCorrIntervalScratchPad = new ScratchPad<ReplicatePad>();

	private Vector<RunCalculatorListener> listeners = new Vector<RunCalculatorListener>();

	RunCalculator(ChainedPart chainedPart) {
		this.chainedPart = chainedPart;
		InputCache.getInstance().addListener(this);
	}

	public void dispose() {
		InputCache.getInstance().removeListener(this);
	}

	boolean isWaitingOnSelection() {
		return currentlySelectedReplicateId == DatabaseConstants.EMPTY_DB_ID || currentCorrIntervalId == DatabaseConstants.EMPTY_DB_ID;
	}

	boolean isLoading() {
		return waitingForReplicateCommandId != Command.UNDEFINED_ID;
	}

	boolean isCalculating() {
		return singleRunCalculator != null && !singleRunCalculator.isFinished();
	}

	boolean isFinished() {
		return singleRunCalculator != null && singleRunCalculator.isFinished();
	}

	boolean isSuccessful() {
		return isFinished() && getErrors() == null;
	}

	ArrayList<CalculationError> getErrors() {
		return singleRunCalculator == null || singleRunCalculator.getErrors() == null || singleRunCalculator.getErrors().size() == 0 ? null : singleRunCalculator.getErrors();
	}

	ScratchPad<ReplicatePad> getResultsScratchPad() {
		if (currentCorrIntervalScratchPadCopy != null) {
			ScratchPad<ReplicatePad> result = new ScratchPad<ReplicatePad>();	
			ReplicatePad pad = (ReplicatePad) currentCorrIntervalScratchPadCopy.getVolatileData(VOLATILE_DATA_CALCULATION_PAD);

			if (pad != null) {
				new ReplicatePad(result, pad);
				return result;
			}
		}

		return null;
	}

	public AnalysisWithParameters getDataAnalysisWithParameters() {
		if (singleRunCalculator == null) {
			return null;
		}

		return singleRunCalculator.getDataAnalysisWithParameters();
	}

	public DependencyManager getDependencyManager(int position) {
		if (singleRunCalculator == null || singleRunCalculator.getDependencyManagers() == null || singleRunCalculator.getDependencyManagers().size() <= position) {
			return null;
		}

		return singleRunCalculator.getDependencyManagers().get(position);
	}

	private boolean setReplicateId(int replicateId) {
		if (replicateId == currentlySelectedReplicateId) {
			return false;
		}

		currentlySelectedReplicateId = replicateId;
		waitingForReplicateCommandId = Command.UNDEFINED_ID;
		currentReplicate = null;
		currentReplicateScratchPad = null;

		if (currentlySelectedReplicateId != DatabaseConstants.EMPTY_DB_ID) {
			waitingForReplicateCommandId = InputCache.getInstance().replicateGet(currentlySelectedReplicateId, this);
			chainedPart.setCursor();
		}

		return true;
	}

	boolean setSelection(int replicateId) {
		if (setReplicateId(replicateId)) {
			startCalculationsIfReady();
			return true;
		}

		return false;
	}

	void setSelection(int corrIntervalId, RepAnalysis repAnalysis, List<RepStep> repSteps, List<RepStepParams> repStepParams, ScratchPad<ReplicatePad> corrIntervalScratchPad, List<CorrIntervalError> corrIntervalErrors) {
		currentCorrIntervalId = corrIntervalId;
		currentRepAnalysis = repAnalysis;
		currentRepSteps = repSteps;
		currentRepStepParams = repStepParams;
		currentCorrIntervalScratchPadCopy = corrIntervalScratchPad != null ? new ScratchPad<ReplicatePad>(corrIntervalScratchPad) : null;
		currentCorrIntervalErrors = corrIntervalErrors;

		replicatesTemporarilyRemovedFromCurrentCorrIntervalScratchPad.removeAllChildren();

		startCalculationsIfReady();
	}

	void setSelection(Integer replicateId, Integer corrIntervalId, RepAnalysis repAnalysis, List<RepStep> repSteps, List<RepStepParams> repStepParams, ScratchPad<ReplicatePad> corrIntervalScratchPad, List<CorrIntervalError> corrIntervalErrors) {
		setReplicateId(replicateId);
		setSelection(corrIntervalId, repAnalysis, repSteps, repStepParams, corrIntervalScratchPad, corrIntervalErrors);
	}

	private boolean startCalculationsIfReady() {
		if (singleRunCalculator != null) {
			singleRunCalculator.dispose();
			singleRunCalculator = null;
		}

		if (currentReplicate == null || currentReplicateScratchPad == null || currentRepAnalysis == null || currentRepSteps == null || currentRepStepParams == null || currentCorrIntervalScratchPadCopy == null) { 
			return false;
		}

		do {
			// restore currentCorrIntervalScratchPadCopy to its default state

			ReplicatePad oldCalcPad = (ReplicatePad) currentCorrIntervalScratchPadCopy.getVolatileData(VOLATILE_DATA_CALCULATION_PAD);

			if (oldCalcPad != null) {
				currentCorrIntervalScratchPadCopy.removeChild(oldCalcPad);
				currentCorrIntervalScratchPadCopy.setVolatileData(VOLATILE_DATA_CALCULATION_PAD, null);
			}

			for (ReplicatePad replicatePad : new ArrayList<ReplicatePad>(replicatesTemporarilyRemovedFromCurrentCorrIntervalScratchPad.getChildren())) {
				replicatePad.reassignToParent(currentCorrIntervalScratchPadCopy);
			}

			// if this is a standard, look for duplicate standards and temporarily remove them

			if (currentReplicate.getStandardId() != DatabaseConstants.EMPTY_DB_ID) {
				ArrayList<ReplicatePad> toBeRemoved = new ArrayList<ReplicatePad>();

				for (ReplicatePad potentialCollision : currentCorrIntervalScratchPadCopy.getChildren()) {
					if (currentReplicate.getDate() == potentialCollision.getDate()) {
						toBeRemoved.add(potentialCollision);
					}
				}

				for (ReplicatePad toRemove : toBeRemoved) {
					toRemove.reassignToParent(replicatesTemporarilyRemovedFromCurrentCorrIntervalScratchPad);
				}
			}

			// add copy of the replicate pad to be calculated to currentCorrIntervalScratchPad

			ScratchPad<ReplicatePad> replicatePad = new ScratchPad<ReplicatePad>(currentReplicateScratchPad);
			ReplicatePad newCalcPad = replicatePad.getChildren().get(0);
			newCalcPad.setVolatileData(AnalysisConstants.VOLATILE_DATA_REPLICATE, currentReplicate);
			newCalcPad.reassignToParent(currentCorrIntervalScratchPadCopy);
			currentCorrIntervalScratchPadCopy.setVolatileData(VOLATILE_DATA_CALCULATION_PAD, newCalcPad);

			// start the replicate analysis

			AnalysisWithParameters dataAnalysisWithParameters = new AnalysisWithParameters(currentRepAnalysis, currentRepSteps, currentRepStepParams);

			singleRunCalculator = new SingleReplicateCalculator(dataAnalysisWithParameters, currentCorrIntervalScratchPadCopy, newCalcPad, currentCorrIntervalErrors);
			singleRunCalculator.execute();

		} while (singleRunCalculator.isNoLongerValid());

		singleRunCalculator.addListener(this);

		return true;
	}

	public void addListener(RunCalculatorListener listener) {
		listeners.add(listener);
	}

	public void removeListener(RunCalculatorListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void statusChanged(AnalysisCalculator analysisNetworkCalculator) {
		if (singleRunCalculator.isNoLongerValid()) {
			startCalculationsIfReady();
		}

		for (RunCalculatorListener listener : listeners) {
			listener.runCalculatorStatusChanged();
		}
	}

	@Override
	public Display getDisplay() {
		return chainedPart.getParent().getDisplay();
	}

	@Override
	public boolean stillCallabled() {
		return !chainedPart.getParent().isDisposed();
	}

	private void newReplicate(ReplicateV1 replicate, ArrayList<Acquisition> acquisitions, boolean startCalculations) {
		currentReplicate = replicate;

		currentReplicateScratchPad = new ScratchPad<ReplicatePad>();
		RawDataHelper.addReplicateToScratchPad(currentReplicateScratchPad, replicate, acquisitions);

		if (startCalculations && startCalculationsIfReady()) {
			for (RunCalculatorListener listener : listeners) {
				listener.runCalculatorStatusChanged();
			}
		}
	}

	@Override
	public void replicateGetCompleted(int commandId, ReplicateV1 replicate, ArrayList<Acquisition> acquisitions, Integer projectId, SampleType sampleType) {
		if (commandId == waitingForReplicateCommandId) {
			waitingForReplicateCommandId = Command.UNDEFINED_ID;
			newReplicate(replicate, acquisitions, commandId != Command.UNDEFINED_ID);
			chainedPart.setCursor();
		}
	}

	@Override
	public void replicateGetError(int commandId, String message) {
		if (commandId == waitingForReplicateCommandId) {
			waitingForReplicateCommandId = Command.UNDEFINED_ID;
			chainedPart.raiseError(message);
			chainedPart.setCursor();
		}
	}

	@Override
	public void replicateUpdated(int commandId, ReplicateV1 replicate, ArrayList<Acquisition> acquisitions, Integer projectId, SampleType sampleType) {
		if (currentReplicate != null && currentReplicate.getId() == replicate.getId()) {
			newReplicate(replicate, acquisitions, true);
		}
	}

	@Override
	public void replicateDeleted(int replicateId) {
		if (currentReplicate != null && currentReplicate.getId() == replicateId) {
			currentlySelectedReplicateId = DatabaseConstants.EMPTY_DB_ID;
			waitingForReplicateCommandId = Command.UNDEFINED_ID;
			currentReplicate = null;
			currentReplicateScratchPad = null;

			if (startCalculationsIfReady()) {
				for (RunCalculatorListener listener : listeners) {
					listener.runCalculatorStatusChanged();
				}
			}
		}
	}
}
