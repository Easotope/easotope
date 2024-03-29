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

package org.easotope.shared.analysis.repstep.co2.clumpcalc;

import java.util.HashSet;

import org.easotope.shared.Messages;
import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.StandardParameter;
import org.easotope.shared.analysis.repstep.RepInputDescription;
import org.easotope.shared.analysis.repstep.RepOutputDescription;
import org.easotope.shared.analysis.step.InputDescription;
import org.easotope.shared.analysis.step.OutputDescription;
import org.easotope.shared.analysis.step.RepStepController;
import org.easotope.shared.analysis.tables.RepStepParams;

public class Controller extends RepStepController {
	@Override
	public String getStepName() {
		return Messages.repStepCO2Clump_name;
	}

	@Override
	public String getShortDocumentation() {
		return Messages.repStepCO2Clump_shortDocumentation;
	}

	@Override
	public String getDocumentationPath() {
		return Messages.repStepCO2Clump_documentationPath;
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
			new RepInputDescription(Calculator.INPUT_LABEL_DISABLED, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_OFF_PEAK, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V44_SAMPLE, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V45_V44_INTERP_REF, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V45_SAMPLE, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V46_V44_INTERP_REF, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V46_SAMPLE, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V47_V44_INTERP_REF, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V47_SAMPLE, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V48_V44_INTERP_REF, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V48_SAMPLE, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V49_V44_INTERP_REF, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_V49_SAMPLE, true, false)
		};
	}

	@Override
	public OutputDescription[] getOutputDescription() {
		return new OutputDescription[] {
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ13C, "0.00", StandardParameter.δ13C, IsotopicScale.VPDB, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ13C_SD, "0.00", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ13C_SE, "0.00", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ13C_CI, "0.00", null, null, false),

			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VPDB, "0.00", StandardParameter.δ18O, IsotopicScale.VPDB, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VPDB_SD, "0.00", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VPDB_SE, "0.00", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VPDB_CI, "0.00", null, null, false),

			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VSMOW, "0.00", StandardParameter.δ18O, IsotopicScale.VSMOW, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VSMOW_SD, "0.00", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VSMOW_SE, "0.00", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ18O_VSMOW_CI, "0.00", null, null, false),

			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ45, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ45_SD, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ45_SE, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ45_CI, "0.000", null, null, false),
			
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ46, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ46_SD, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ46_SE, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ46_CI, "0.000", null, null, false),

			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ47, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ47_SD, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ47_SE, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ47_CI, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ47, "0.000", StandardParameter.Δ47, IsotopicScale.CDES, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ47_SD, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ47_SE, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ47_CI, "0.000", null, null, false),

			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ48, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ48_SD, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ48_SE, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ48_CI, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ48, "0.000", StandardParameter.Δ48, IsotopicScale.CDES, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ48_SD, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ48_SE, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ48_CI, "0.000", null, null, false),

			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ49, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ49_SD, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ49_SE, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_δ49_CI, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ49, "0.000", null, null, true),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ49_SD, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ49_SE, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ49_CI, "0.000", null, null, false),
			
			new RepOutputDescription(Calculator.OUTPUT_LABEL_49_PARAM, "0.000", null, null, true),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_49_PARAM_SD, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_49_PARAM_SE, "0.000", null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_49_PARAM_CI, "0.000", null, null, false)
		};
	}

	@Override
	public void removeStandardIds(RepStepParams repStepParams, HashSet<Integer> standardIds) {

	}
}
