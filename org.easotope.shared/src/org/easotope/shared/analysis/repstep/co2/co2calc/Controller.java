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

package org.easotope.shared.analysis.repstep.co2.co2calc;

import org.easotope.shared.Messages;
import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.StandardParameter;
import org.easotope.shared.analysis.repstep.RepInputDescription;
import org.easotope.shared.analysis.repstep.RepOutputDescription;
import org.easotope.shared.analysis.step.InputDescription;
import org.easotope.shared.analysis.step.OutputDescription;
import org.easotope.shared.analysis.step.StepController;

public class Controller extends StepController {
	@Override
	public String getStepName() {
		return Messages.repStepCO2_name;
	}

	@Override
	public String getShortDocumentation() {
		return Messages.repStepCO2_shortDocumentation;
	}

	@Override
	public String getDocumentationPath() {
		return Messages.repStepCO2_documentationPath;
	}

	@Override
	public String getStepCalculatorClassName() {
		return Calculator.class.getName();
	}

	@Override
	public String getParameterComposite() {
		return "org.easotope.client.analysis.repstep.co2.co2calc.ParameterComposite";
	}

	@Override
	public String getGraphicComposite() {
		return null;
	}

	@Override
	public InputDescription[] getInputDescription() {
		return new InputDescription[] {
			new RepInputDescription(Calculator.INPUT_LABEL_DISABLED, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_OFF_PEAK, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V44_REF, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V44_SAMPLE, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V45_REF, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V45_SAMPLE, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V46_REF, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V46_SAMPLE, true, false)
		};
	}

	@Override
	public OutputDescription[] getOutputDescription() {
		return new OutputDescription[] {
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ13C, "0.00", StandardParameter.δ13C, IsotopicScale.VPDB),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ13C_SD, "0.00", null, null),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ13C_SE, "0.00", null, null),

			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VPDB, "0.00", StandardParameter.δ18O, IsotopicScale.VPDB),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VPDB_SD, "0.00", null, null),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VPDB_SE, "0.00", null, null),

			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VSMOW, "0.00", StandardParameter.δ18O, IsotopicScale.VSMOW),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VSMOW_SD, "0.00", null, null),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VSMOW_SE, "0.00", null, null)
		};
	}
}
