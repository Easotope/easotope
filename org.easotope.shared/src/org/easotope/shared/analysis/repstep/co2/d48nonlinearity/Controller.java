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

package org.easotope.shared.analysis.repstep.co2.d48nonlinearity;

import org.easotope.shared.Messages;
import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.StandardParameter;
import org.easotope.shared.analysis.repstep.RepInputDescription;
import org.easotope.shared.analysis.repstep.RepOutputDescription;
import org.easotope.shared.analysis.step.InputDescription;
import org.easotope.shared.analysis.step.OutputDescription;

public class Controller extends org.easotope.shared.analysis.repstep.superclass.nonlinearity.Controller {
	@Override
	public String getStepName() {
		return Messages.repStepD48Nonlinearity_name;
	}

	@Override
	public String getShortDocumentation() {
		return Messages.repStepD48Nonlinearity_shortDocumentation;
	}

	@Override
	public String getDocumentationPath() {
		return Messages.repStepD48Nonlinearity_documentationPath;
	}

	@Override
	public String getStepCalculatorClassName() {
		return Calculator.class.getName();
	}

	@Override
	public String getGraphicComposite() {
		return "org.easotope.client.analysis.repstep.co2.d48nonlinearity.GraphicComposite";
	}

	@Override
	public InputDescription[] getInputDescription() {
		return new InputDescription[] {
			new RepInputDescription(Calculator.INPUT_LABEL_SAMPLE_δ48, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_SAMPLE_Δ48, true, false),
			new RepInputDescription(Calculator.INPUT_LABEL_STANDARD_δ48, true, true),
			new RepInputDescription(Calculator.INPUT_LABEL_STANDARD_Δ48, true, true)
		};
	}

	@Override
	public OutputDescription[] getOutputDescription() {
		return new OutputDescription[] {
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ48_δ48_SLOPE, null, null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ48_δ48_INTERCEPTS, null, null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_Δ48, "0.000", StandardParameter.Δ48, IsotopicScale.CDES, false)
		};
	}
}
