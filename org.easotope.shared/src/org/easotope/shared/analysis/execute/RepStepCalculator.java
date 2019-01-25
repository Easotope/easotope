/*
 * Copyright © 2016-2019 by Devon Bowen.
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

package org.easotope.shared.analysis.execute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.easotope.framework.core.global.OptionsInfo;
import org.easotope.shared.analysis.execute.calculator.AnalysisConstants;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.exception.EasotopeStepException;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ReplicatePad.ReplicateType;

public abstract class RepStepCalculator extends StepCalculator {
	public enum WindowType { CorrInterval, Batch, Window };

	public final static String PARAMETER_WINDOW_TYPE = "PARAMETER_WINDOW_TYPE";
	public final static String PARAMETER_MIN_NUM_STANDARDS_BEFORE_AFTER = "PARAMETER_MIN_NUM_STANDARDS_BEFORE_AFTER";
	public final static String PARAMETER_STANDARD_IDS = "PARAMETER_STANDARD_IDS";

	public abstract DependencyManager getDependencyManager(ReplicatePad[] replicatePads, int standardNumber);
	public abstract void calculate(ReplicatePad[] replicatePads, int padNumber, DependencyManager dependencyManager) throws EasotopeStepException;

	protected RepStepCalculator(RepStep repStep) {
		super(repStep);
	}

	public boolean appliesToContext() {
		return ((RepStep) getStep()).isApplyToContext();
	}

	public boolean appliesToResults() {
		return ((RepStep) getStep()).isApplyToResults();
	}

	protected StandardReplicatePads getStandardReplicatePads(ReplicatePad[] replicatePads, int targetPadNumber, WindowType windowType, int minNumStandardsBeforeAfter, StandardVerifier standardVerifier) {
		StandardReplicatePads resultReplicates = new StandardReplicatePads(standardVerifier);

		switch (windowType) {
			case Batch:
				// NOT IMPLEMENTED YET
				break;

			case CorrInterval:
				for (int i=0; i<replicatePads.length; i++) {
					ReplicatePad replicatePad = replicatePads[i];

					if (!OptionsInfo.getInstance().getOptions().isIncludeStds() && i == targetPadNumber) {
						continue;
					}

					resultReplicates.addReplicatePad(replicatePad);
				}

				break;

			case Window:
				int cursorBefore = targetPadNumber - 1;

				while (cursorBefore >= 0 && resultReplicates.getUsable().size() < minNumStandardsBeforeAfter) {
					ReplicatePad replicatePad = replicatePads[cursorBefore];
					resultReplicates.addReplicatePad(replicatePad);
					cursorBefore--;
				}

				int cursorAfter = targetPadNumber + 1;
				int numFoundBefore = resultReplicates.getUsable().size();

				while (cursorAfter < replicatePads.length && resultReplicates.getUsable().size() < minNumStandardsBeforeAfter + numFoundBefore) {
					ReplicatePad replicatePad = replicatePads[cursorAfter];
					resultReplicates.addReplicatePad(replicatePad);
					cursorAfter++;
				}

				break;
		}

		return resultReplicates;
	}

	public static void removeStandardIds(RepStepParams repStepParams, HashSet<Integer> standardIds) {
		int[] oldStdIds = (int[]) repStepParams.getParameters().get(PARAMETER_STANDARD_IDS);

		if (oldStdIds == null) {
			return;
		}

		ArrayList<Integer> newStdIds = new ArrayList<Integer>();

		for (int id : oldStdIds) {
			if (standardIds.contains(id)) {
				newStdIds.add(id);
			}
		}

		int count = 0;
		int[] result = new int[newStdIds.size()];

		for (int id : newStdIds) {
			result[count++] = id;
		}

		repStepParams.getParameters().put(PARAMETER_STANDARD_IDS, result);
	}

	public abstract class StandardVerifier {
		public abstract Set<Integer> getPotentialStandardIds();
		public abstract boolean replicatePadIsAcceptable(ReplicatePad replicatePad);
	}

	public class SimpleStandardVerifier extends StandardVerifier {
		private Set<Integer> potentialStandardIds;

		public SimpleStandardVerifier(Set<Integer> potentialStandardIds) {
			this.potentialStandardIds = potentialStandardIds;
		}

		public SimpleStandardVerifier(int[] standardIds) {
			potentialStandardIds = new HashSet<Integer>();

			for (int i : standardIds) {
				potentialStandardIds.add(i);
			}
		}

		@Override
		public Set<Integer> getPotentialStandardIds() {
			return potentialStandardIds;
		}

		@Override
		public boolean replicatePadIsAcceptable(ReplicatePad replicatePad) {
			return true;
		}
	}

	public class StandardReplicatePads {
		private StandardVerifier standardVerifier = null;
		private HashSet<ReplicatePad> usable = new HashSet<ReplicatePad>();
		private HashSet<ReplicatePad> disabled = new HashSet<ReplicatePad>();
		private HashSet<ReplicatePad> failed = new HashSet<ReplicatePad>();

		public StandardReplicatePads(StandardVerifier standardVerifier) {
			this.standardVerifier = standardVerifier;
		}

		public boolean addReplicatePad(ReplicatePad replicatePad) {
			if (replicatePad.getReplicateType() != ReplicateType.STANDARD_RUN || !standardVerifier.getPotentialStandardIds().contains(replicatePad.getSourceId())) {
				return false;
			}

			Boolean hasErrors = (Boolean) replicatePad.getVolatileData(AnalysisConstants.VOLATILE_DATA_HAS_ERRORS);

			if (hasErrors != null && hasErrors == true) {
				return false;
			}
			
			if (standardVerifier.replicatePadIsAcceptable(replicatePad)) {
				Boolean disabledFlag = (Boolean) replicatePad.getValue(ReplicatePad.DISABLED);

				if (disabledFlag != null && disabledFlag) {
					disabled.add(replicatePad);
				} else {
					usable.add(replicatePad);
					return true;
				}

			} else {
				failed.add(replicatePad);
			}
			
			return false;
		}

		public HashSet<ReplicatePad> getUsable() {
			return usable;
		}

		public HashSet<ReplicatePad> getDisabled() {
			return disabled;
		}

		public HashSet<ReplicatePad> getFailed() {
			return failed;
		}
	}
}
