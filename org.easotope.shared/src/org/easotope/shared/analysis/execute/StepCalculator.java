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

package org.easotope.shared.analysis.execute;

import org.easotope.shared.analysis.tables.Step;
import org.easotope.shared.analysis.tables.StepParams;
import org.easotope.shared.core.scratchpad.Accumulator;
import org.easotope.shared.core.scratchpad.Pad;

public class StepCalculator {
	private Step step;
	private StepParams stepParams;

	protected StepCalculator(Step step) {
		this.step = step;
	}

	protected Step getStep() {
		return step;
	}

	public void setParameters(StepParams stepParams) {
		this.stepParams = stepParams;
	}

	public Object getParameter(String key) {
		if (stepParams == null || stepParams.getParameters() == null) {
			return null;
		}

		return stepParams.getParameters().get(key);
	}

	protected String labelToColumnName(String label) {
		if (step.getInputs().containsKey(label)) {
			return step.getInputs().get(label);
		}

		return step.getOutputs().get(label);
	}

	protected boolean isTrue(Pad pad, String inputLabel) {
		Boolean value = (Boolean) pad.getValue(labelToColumnName(inputLabel));
		return value != null && value == true;
	}

	protected Double getDouble(Pad pad, String inputLabel) {
		Object value = (Object) pad.getValue(labelToColumnName(inputLabel));
		Double result = null;

		if (value instanceof Double) {
			result = (Double) value;

		} else if (value instanceof Accumulator) {
			result = ((Accumulator) value).getAccumulatedValues()[0];

		} else if (value instanceof Integer) {
			result = (double) ((Integer) value);
		}

		return result;
	}
}
