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

package org.easotope.shared.analysis.execute.calculator;

import java.util.ArrayList;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.shared.analysis.execute.AnalysisCalculator;
import org.easotope.shared.analysis.execute.AnalysisWithParameters;
import org.easotope.shared.analysis.execute.CalculationError;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyManagerListener;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.Pad.Status;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ReplicatePad.ReplicateType;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.rawdata.tables.ReplicateV1;

public class AllStandardsCalculator extends AnalysisCalculator implements DependencyManagerListener {
	private enum State { ValidateNumbers, FetchDependency, Execute, IncrementNumbers, Terminating, Finished };

	private AnalysisWithParameters dataAnalysis;
	private ScratchPad<ReplicatePad> scratchPad;
	private ReplicatePad[] replicatePadArray;

	private State state = State.ValidateNumbers;
	private boolean dependenciesNoLongerValid = false;
	private int currentScratchPadItem = 0;
	private int currentRepStepNumber = 0;
	private ArrayList<CalculationError> calculationErrors = new ArrayList<CalculationError>();
	private ArrayList<DependencyManager> dependencyManagers = new ArrayList<DependencyManager>();

	public AllStandardsCalculator(AnalysisWithParameters dataAnalysis, ScratchPad<ReplicatePad> scratchPad) {
		this.dataAnalysis = dataAnalysis;
		this.scratchPad = scratchPad;

		Log.getInstance().log(Level.DEBUG, this, "State: started executing");

		ArrayList<ReplicatePad> scratchPadChildren = scratchPad.getChildren();
		replicatePadArray = scratchPadChildren.toArray(new ReplicatePad[scratchPadChildren.size()]);

		for (ReplicatePad replicatePad : replicatePadArray) {
			replicatePad.setValue(Pad.ANALYSIS, dataAnalysis.getAnalysis().getName());
			replicatePad.setValue(Pad.ANALYSIS_STATUS, Status.OK);
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

		state = State.Finished;
	}

	@Override
	public void execute() {
		while (state != State.Terminating && state != State.Finished) {
			RepStepCalculator currentNode;

			switch (state) {
				case ValidateNumbers:
					Log.getInstance().log(Level.DEBUG, this, "State: ValidateNumbers " + currentScratchPadItem + " " + currentRepStepNumber);

					if (currentRepStepNumber >= dataAnalysis.getStepCalculators().length || currentScratchPadItem >= replicatePadArray.length) {
						state = State.Terminating;

					} else if (replicatePadArray[currentScratchPadItem].getVolatileData(AnalysisConstants.VOLATILE_DATA_HAS_ERRORS) != null) {
						state = State.IncrementNumbers;

					} else if (replicatePadArray[currentScratchPadItem].getReplicateType() != ReplicateType.STANDARD_RUN) {
						state = State.IncrementNumbers;

					} else if (!((RepStepCalculator) dataAnalysis.getStepCalculators()[currentRepStepNumber]).appliesToContext()) {
						dependencyManagers.add(0, null);
						state = State.IncrementNumbers;

					} else {
						state = State.FetchDependency;
					}

					break;

				case FetchDependency:
					Log.getInstance().log(Level.DEBUG, this, "State: FetchDependency");

					currentNode = (RepStepCalculator) dataAnalysis.getStepCalculators()[currentRepStepNumber];
					DependencyManager dependencyManager = currentNode.getDependencyManager(replicatePadArray, currentScratchPadItem);

					state = State.Execute;
					dependencyManagers.add(0, dependencyManager);

					if (dependencyManager != null) {
						dependencyManager.execute((ReplicateV1) replicatePadArray[currentScratchPadItem].getVolatileData(AnalysisConstants.VOLATILE_DATA_REPLICATE), currentNode);

						if (!dependencyManager.allDependenciesAreLoaded()) {
							dependencyManager.addListener(this);
							Log.getInstance().log(Level.DEBUG, this, "waiting for dependencies");
							return;
						}
					}

					break;

				case Execute:
					Log.getInstance().log(Level.DEBUG, this, "State: Execute");
					DependencyManager currentDependencyManager = dependencyManagers.get(0);

					if (currentDependencyManager != null && !currentDependencyManager.allDependenciesAreLoaded()) {
						Log.getInstance().log(Level.DEBUG, this, "dependencies are not loaded");
						return;
					}

					ReplicatePad currentReplicatePad = replicatePadArray[currentScratchPadItem];
					currentNode = (RepStepCalculator) dataAnalysis.getStepCalculators()[currentRepStepNumber];

					if (currentDependencyManager != null && !currentDependencyManager.allDependenciesAreValid()) {
						Log.getInstance().log(Level.DEBUG, this, "dependencies are not valid");

						for (DependencyPlugin plugin : currentDependencyManager.getDependencyPlugins()) {
							if (plugin.getState() == DependencyPlugin.PluginState.ERROR) {
								CalculationError calculationError = new CalculationError(currentReplicatePad, dataAnalysis.getStepControllers()[currentRepStepNumber], plugin.getErrorMessage());
								calculationErrors.add(calculationError);
								currentReplicatePad.setVolatileData(AnalysisConstants.VOLATILE_DATA_HAS_ERRORS, true);
							}
						}

					} else {
						Log.getInstance().log(Level.DEBUG, this, "executing node " + dataAnalysis.getStepControllers()[currentRepStepNumber].getStepName());

						try {
							currentNode.calculate(replicatePadArray, currentScratchPadItem, currentDependencyManager);

						} catch (Exception e) {
							ReplicatePad replicatepad = replicatePadArray[currentScratchPadItem];
							String date = DateFormat.format(replicatepad.getDate(), "GMT", true, true);
							Log.getInstance().log(Level.INFO, this, "error executing node " + dataAnalysis.getStepControllers()[currentRepStepNumber].getStepName() + " with replicate timestamp " + date, e);

							String message = e.getMessage() != null ? e.getMessage() : e.getClass().getName();
							CalculationError calculationError = new CalculationError(currentReplicatePad, dataAnalysis.getStepControllers()[currentRepStepNumber], message);
							calculationErrors.add(calculationError);
							currentReplicatePad.setVolatileData(AnalysisConstants.VOLATILE_DATA_HAS_ERRORS, true);
						}
					}
					
					state = State.IncrementNumbers;
					break;

				case IncrementNumbers:
					Log.getInstance().log(Level.DEBUG, this, "State: IncrementNumbers");

					currentScratchPadItem++;

					if (currentScratchPadItem >= scratchPad.getChildren().size()) {
						currentScratchPadItem = 0;
						currentRepStepNumber++;
					}

					state = State.ValidateNumbers;
					break;

				default:
					assert(false);
					break;
			}
		}

		if (state == State.Terminating) {
			state = State.Finished;
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
		return dependenciesNoLongerValid;
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
		if (state == State.Finished && dependenciesNoLongerValid == false) {
			dependenciesNoLongerValid = true;
			broadcastStatusChanged(this);
		}
	}
}
