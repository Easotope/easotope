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

package org.easotope.shared.analysis.samstep.co2.clumpaverage;

import org.easotope.shared.Messages;
import org.easotope.shared.analysis.samstep.SamInputDescription;
import org.easotope.shared.analysis.samstep.SamOutputDescription;
import org.easotope.shared.analysis.step.InputDescription;
import org.easotope.shared.analysis.step.OutputDescription;
import org.easotope.shared.analysis.step.StepController;

public class Controller extends StepController {
	@Override
	public String getStepName() {
		return Messages.samStepClumpAverage_name;
	}

	@Override
	public String getShortDocumentation() {
		return Messages.samStepClumpAverage_shortDocumentation;
	}

	@Override
	public String getDocumentationPath() {
		return Messages.samStepClumpAverage_documentationPath;
	}

	@Override
	public String getStepCalculatorClassName() {
		return Calculator.class.getName();
	}

	@Override
	public String getParameterComposite() {
		return null;
	}

	@Override
	public String getGraphicComposite() {
		return null;
	}

	@Override
	public InputDescription[] getInputDescription() {
		return new InputDescription[] {
			new SamInputDescription(Calculator.INPUT_LABEL_Δ47, true)
		};
	}

	@Override
	public OutputDescription[] getOutputDescription() {
		return new OutputDescription[] {
			new SamOutputDescription(Calculator.OUTPUT_LABEL_Δ47, "0.000"),
			new SamOutputDescription(Calculator.OUTPUT_LABEL_Δ47_SD, "0.000"),
			new SamOutputDescription(Calculator.OUTPUT_LABEL_Δ47_SE, "0.000")
		};
	}
}
