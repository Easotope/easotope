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

import org.easotope.shared.analysis.step.StepController;
import org.easotope.shared.core.scratchpad.Pad;

public class CalculationError {
	private Pad pad;
	private StepController stepController;
	private String errorMessage;

	public CalculationError(Pad pad, StepController stepController, String errorMessage) {
		this.pad = pad;
		this.stepController = stepController;
		this.errorMessage = errorMessage;
	}

	public Pad getPad() {
		return pad;
	}

	public StepController getStepController() {
		return stepController;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	@Override
	public String toString() {
		return "CalculationError(" + pad.getValue(Pad.ID) + ", " + stepController.getStepName() + ", \"" + errorMessage + "\")";
	}
}
