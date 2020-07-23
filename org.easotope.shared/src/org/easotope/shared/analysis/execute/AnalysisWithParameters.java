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

package org.easotope.shared.analysis.execute;

import java.util.List;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.Reflection;
import org.easotope.shared.Messages;
import org.easotope.shared.analysis.step.StepController;
import org.easotope.shared.analysis.tables.Analysis;
import org.easotope.shared.analysis.tables.Step;
import org.easotope.shared.analysis.tables.StepParams;

public class AnalysisWithParameters extends AnalysisCompiled {
	private StepParams[] stepParams;
	private StepCalculator[] stepCalculator;

	public AnalysisWithParameters(Analysis analysis, List<? extends Step> stepList, List<? extends StepParams> parametersList) {
		super(analysis, stepList);

		int maxStepParamPosition = -1;

		for (StepParams thisParams : parametersList) {
			maxStepParamPosition = Math.max(maxStepParamPosition, thisParams.getPosition());
		}

		stepParams = new StepParams[maxStepParamPosition + 1];

		outer:
		for (int i=0; i<getSteps().length; i++) {
			for (StepParams thisParams : parametersList) {
				if (thisParams.getPosition() == i) {
					stepParams[i] = thisParams;
					continue outer;
				}
			}
		}

		StepController[] stepControllers = getStepControllers();
		stepCalculator = new StepCalculator[stepControllers.length];

		for (int i=0; i<stepControllers.length; i++) {
			StepController stepController = stepControllers[i];

			if (stepController != null) {
				try {
					stepCalculator[i] = (StepCalculator) Reflection.createObject(stepController.getStepCalculatorClassName(), step[i]);
					stepCalculator[i].setParameters(stepParams.length > i ? stepParams[i] : null);

				} catch (Exception e) {
					String message = Messages.dataAnalysisWithParameters_couldntCreateStepCalculator + stepController.getStepCalculatorClassName();
					Log.getInstance().log(Level.INFO, this, message, e);
					addError(message);
				}
			}
		}
	}

	public StepParams[] getStepParams() {
		return stepParams;
	}

	public StepCalculator[] getStepCalculators() {
		return stepCalculator;
	}
}
