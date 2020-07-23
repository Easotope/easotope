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

package org.easotope.shared.analysis.repstep.generic.replicate;

import java.util.HashSet;

import org.easotope.shared.Messages;
import org.easotope.shared.analysis.repstep.RepOutputDescription;
import org.easotope.shared.analysis.step.InputDescription;
import org.easotope.shared.analysis.step.OutputDescription;
import org.easotope.shared.analysis.step.RepStepController;
import org.easotope.shared.analysis.tables.RepStepParams;

public class Controller extends RepStepController {
	@Override
	public String getStepName() {
		return Messages.repStepGenericReplicate_name;
	}

	@Override
	public String getShortDocumentation() {
		return Messages.repStepGenericReplicate_shortDocumentation;
	}

	@Override
	public String getDocumentationPath() {
		return Messages.repStepGenericReplicate_documentationPath;
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

		};
	}

	@Override
	public OutputDescription[] getOutputDescription() {
		return new OutputDescription[] {
			new RepOutputDescription(Calculator.OUTPUT_LABEL_MASS_SPEC, null, null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_SOURCE_NAME, null, null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_SAMPLE_TYPE, null, null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_CORR_INTERVAL, null, null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_ACID_TEMP, null, null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_ACQUISITIONS, null, null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_ENABLED_ACQUISITIONS, null, null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_MZ44_FIRST_REFGAS_CYCLE, null, null, null, false),
			new RepOutputDescription(Calculator.OUTPUT_LABEL_MZ44_FIRST_SAMPLE_CYCLE, null, null, null, false)
		};
	}

	@Override
	public void removeStandardIds(RepStepParams repStepParams, HashSet<Integer> standardIds) {

	}
}
