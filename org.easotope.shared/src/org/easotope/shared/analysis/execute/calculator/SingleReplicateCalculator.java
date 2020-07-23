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

package org.easotope.shared.analysis.execute.calculator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.shared.Messages;
import org.easotope.shared.analysis.execute.AnalysisCalculator;
import org.easotope.shared.analysis.execute.AnalysisWithParameters;
import org.easotope.shared.analysis.execute.CalculationError;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyManagerListener;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.Pad.Status;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.rawdata.tables.ReplicateV1;

public class SingleReplicateCalculator extends AnalysisCalculator implements DependencyManagerListener {
	private enum State { ValidateNumbers, FetchDependency, Execute, IncrementNumbers, Terminating, Finished };

	private AnalysisWithParameters dataAnalysisWithParameters;
	private ScratchPad<ReplicatePad> scratchPad;
	private ReplicatePad[] replicatePadArray;

	private State state = State.ValidateNumbers;
	private boolean noLongerValid = false;
	private int padToCalculateIndex = 0;
	private int currentNodeNumber = 0;
	private ArrayList<CalculationError> calculationErrors = new ArrayList<CalculationError>();
	private ArrayList<DependencyManager> dependencyManagers = new ArrayList<DependencyManager>();
	private List<CorrIntervalError> corrIntervalErrors = null;

	public SingleReplicateCalculator(AnalysisWithParameters dataAnalysis, ScratchPad<ReplicatePad> scratchPad, ReplicatePad calculateReplicatePad, List<CorrIntervalError> corrIntervalErrors) {
		this.dataAnalysisWithParameters = dataAnalysis;
		this.scratchPad = scratchPad;
		this.corrIntervalErrors = corrIntervalErrors;

		calculateReplicatePad.setValue(Pad.ANALYSIS, dataAnalysis.getAnalysis().getName());
		calculateReplicatePad.setValue(Pad.ANALYSIS_STATUS, Status.NONE);

		if (!calculateReplicatePad.hasAllColumns(dataAnalysis.getRequiredInputColumns())) {
			HashSet<String> requiredInputs = new HashSet<String>(dataAnalysis.getRequiredInputColumns());

			for (String column : calculateReplicatePad.getAllColumns()) {
				requiredInputs.remove(column);
			}

			for (String input : requiredInputs) {
				calculationErrors.add(new CalculationError(calculateReplicatePad, null, Messages.singleReplicateCalculator_missingInput + input));
			}

			Status status = Status.NONE;

			if (calculationErrors.size() != 0) {
				status = Status.ERROR;
			} else if (corrIntervalErrors.size() != 0) {
				status = Status.WARNING;
			} else {
				status = Status.OK;
			}

			state = State.Finished;
			calculateReplicatePad.setValue(Pad.ANALYSIS_STATUS, status);
			
			return;
		}

		ArrayList<String> errors = dataAnalysis.getErrors();

		if (errors != null && errors.size() != 0) {
			for (String message : errors) {
				calculationErrors.add(new CalculationError(calculateReplicatePad, null, message));
			}

			Status status = Status.NONE;

			if (calculationErrors.size() != 0) {
				status = Status.ERROR;
			} else if (corrIntervalErrors.size() != 0) {
				status = Status.WARNING;
			} else {
				status = Status.OK;
			}

			state = State.Finished;
			calculateReplicatePad.setValue(Pad.ANALYSIS_STATUS, status);

			return;
		}

		ArrayList<ReplicatePad> scratchPadChildren = scratchPad.getChildren();
		replicatePadArray = scratchPadChildren.toArray(new ReplicatePad[scratchPadChildren.size()]);

		for (int i=0; i<replicatePadArray.length; i++) {
			if (replicatePadArray[i] == calculateReplicatePad) {
				padToCalculateIndex = i;
			}
		}
	}

	@Override
	public void dispose() {
		for (DependencyManager dependencyManager : dependencyManagers) {
			if (dependencyManager != null) {
				dependencyManager.dispose();
			}
		}

		calculationErrors.clear();
		dependencyManagers.clear();
		listeners.clear();
	}

	@Override
	public void execute() {
		while (state != State.Terminating && state != State.Finished) {
			RepStepCalculator currentNode;

			switch (state) {
				case ValidateNumbers:
					Log.getInstance().log(Level.DEBUG, this, "State: ValidateNumbers " + padToCalculateIndex + " " + currentNodeNumber);

					if (currentNodeNumber >= dataAnalysisWithParameters.getStepCalculators().length) {
						state = State.Terminating;

					} else if (replicatePadArray[padToCalculateIndex].getVolatileData(AnalysisConstants.VOLATILE_DATA_HAS_ERRORS) != null) {
						state = State.IncrementNumbers;

					} else if (!((RepStepCalculator) dataAnalysisWithParameters.getStepCalculators()[currentNodeNumber]).appliesToResults()) {
						dependencyManagers.add(null);
						state = State.IncrementNumbers;

					} else {
						state = State.FetchDependency;
					}

					break;

				case FetchDependency:
					Log.getInstance().log(Level.DEBUG, this, "State: FetchDependency");

					currentNode = (RepStepCalculator) dataAnalysisWithParameters.getStepCalculators()[currentNodeNumber];
					DependencyManager dependencyManager = currentNode.getDependencyManager(replicatePadArray, padToCalculateIndex);

					state = State.Execute;
					dependencyManagers.add(dependencyManager);

					if (dependencyManager != null) {
						dependencyManager.execute((ReplicateV1) replicatePadArray[padToCalculateIndex].getVolatileData(AnalysisConstants.VOLATILE_DATA_REPLICATE), currentNode);
						dependencyManager.addListener(this);

						if (!dependencyManager.allDependenciesAreLoaded()) {
							Log.getInstance().log(Level.DEBUG, this, "waiting for dependencies");
							return;
						}
					}

					break;

				case Execute:
					Log.getInstance().log(Level.DEBUG, this, "State: Execute");
					DependencyManager currentDependencyManager = dependencyManagers.get(dependencyManagers.size()-1);

					if (currentDependencyManager != null && !currentDependencyManager.allDependenciesAreLoaded()) {
						Log.getInstance().log(Level.DEBUG, this, "dependencies are not loaded");
						return;
					}

					ReplicatePad currentReplicatePad = replicatePadArray[padToCalculateIndex];
					currentNode = (RepStepCalculator) dataAnalysisWithParameters.getStepCalculators()[currentNodeNumber];

					if (currentDependencyManager != null && !currentDependencyManager.allDependenciesAreValid()) {
						Log.getInstance().log(Level.DEBUG, this, "dependencies are not valid");

						for (DependencyPlugin plugin : currentDependencyManager.getDependencyPlugins()) {
							if (plugin.getState() == DependencyPlugin.PluginState.ERROR) {
								CalculationError calculationError = new CalculationError(currentReplicatePad, dataAnalysisWithParameters.getStepControllers()[currentNodeNumber], plugin.getErrorMessage());
								calculationErrors.add(calculationError);
								currentReplicatePad.setVolatileData(AnalysisConstants.VOLATILE_DATA_HAS_ERRORS, true);
							}
						}

					} else {
						Log.getInstance().log(Level.DEBUG, this, "executing node " + dataAnalysisWithParameters.getStepControllers()[currentNodeNumber].getStepName());

						try {							
							currentNode.calculate(replicatePadArray, padToCalculateIndex, currentDependencyManager);

						} catch (Exception e) {
							Log.getInstance().log(Level.INFO, this, "error executing node " + dataAnalysisWithParameters.getStepControllers()[currentNodeNumber].getStepName(), e);

							String message = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
							CalculationError calculationError = new CalculationError(currentReplicatePad, dataAnalysisWithParameters.getStepControllers()[currentNodeNumber], message);
							calculationErrors.add(calculationError);
							currentReplicatePad.setVolatileData(AnalysisConstants.VOLATILE_DATA_HAS_ERRORS, true);
						}
					}
					
					state = State.IncrementNumbers;
					break;

				case IncrementNumbers:
					Log.getInstance().log(Level.DEBUG, this, "State: IncrementNumbers");

					currentNodeNumber++;
					state = State.ValidateNumbers;

					break;

				default:
					assert(false);
					break;
			}
		}

		if (state == State.Terminating) {
			Status status = Status.NONE;

			if (calculationErrors.size() != 0) {
				status = Status.ERROR;
			} else if (corrIntervalErrors.size() != 0) {
				status = Status.WARNING;
			} else {
				status = Status.OK;
			}

			state = State.Finished;
			scratchPad.getChild(padToCalculateIndex).setValue(Pad.ANALYSIS_STATUS, status);
			broadcastStatusChanged(this);
		}

		Log.getInstance().log(Level.DEBUG, this, "finished executing");
	}

	@Override
	public ScratchPad<ReplicatePad> getScratchPad() {
		return scratchPad;
	}

	@Override
	public boolean isFinished() {
		return state == State.Finished;
	}

	@Override
	public boolean isNoLongerValid() {
		return noLongerValid;
	}

	@Override
	public ArrayList<CalculationError> getErrors() {
		return calculationErrors;
	}
	
	@Override
	public void dependenciesReady(DependencyManager dependencyManager) {
		execute();
	}

	@Override
	public void dependenciesNoLongerValid(DependencyManager dependencyManager) {
		if (state == State.Finished && noLongerValid == false) {
			noLongerValid = true;
			broadcastStatusChanged(this);
		}
	}

	public AnalysisWithParameters getDataAnalysisWithParameters() {
		return dataAnalysisWithParameters;
	}

	public ArrayList<DependencyManager> getDependencyManagers() {
		return dependencyManagers;
	}
}
