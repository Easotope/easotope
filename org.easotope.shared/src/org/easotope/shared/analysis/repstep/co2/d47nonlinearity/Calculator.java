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

package org.easotope.shared.analysis.repstep.co2.d47nonlinearity;

import org.easotope.shared.analysis.repstep.superclass.nonlinearity.VolatileKeys;
import org.easotope.shared.analysis.tables.RepStep;

public class Calculator extends org.easotope.shared.analysis.repstep.superclass.nonlinearity.Calculator {
	public static final String INPUT_LABEL_SAMPLE_δ47 = "Sample δ47";
	public static final String INPUT_LABEL_SAMPLE_Δ47 = "Sample Δ47";
	public static final String INPUT_LABEL_STANDARD_δ47 = "Standard δ47";
	public static String INPUT_LABEL_STANDARD_Δ47 = "Standard Δ47";
	public static String OUTPUT_LABEL_Δ47_δ47_SLOPE = "Δ47 Nonlinearity Slope";
	public static String OUTPUT_LABEL_Δ47_δ47_INTERCEPTS = "Δ47 Nonlinearity Intercepts";
	public static String OUTPUT_LABEL_Δ47 = "Δ47 Nonlinearity Corrected";

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	@Override
	public VolatileKeys getVolatiles() {
		return new VolatileKeys(Calculator.class.getName());
	}

	@Override
	public String getInputLabelSampleCorrector() {
		return INPUT_LABEL_SAMPLE_δ47;
	}

	@Override
	public String getInputLabelSampleCorrectee() {
		return INPUT_LABEL_SAMPLE_Δ47;
	}

	@Override
	public String getInputLabelStandardCorrector() {
		return INPUT_LABEL_STANDARD_δ47;
	}

	@Override
	public String getInputLabelStandardCorrectee() {
		return INPUT_LABEL_STANDARD_Δ47;
	}

	@Override
	public String getOutputLabelSlope() {
		return OUTPUT_LABEL_Δ47_δ47_SLOPE;
	}

	@Override
	public String getOutputLabelIntercepts() {
		return OUTPUT_LABEL_Δ47_δ47_INTERCEPTS;
	}

	@Override
	public String getOutputLabel() {
		return OUTPUT_LABEL_Δ47;
	}
}
