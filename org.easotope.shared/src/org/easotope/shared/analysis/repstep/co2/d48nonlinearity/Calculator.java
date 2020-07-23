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

import org.easotope.shared.analysis.repstep.superclass.nonlinearity.VolatileKeys;
import org.easotope.shared.analysis.tables.RepStep;

public class Calculator extends org.easotope.shared.analysis.repstep.superclass.nonlinearity.Calculator {
	public static final String INPUT_LABEL_SAMPLE_δ48 = "Sample δ48";
	public static final String INPUT_LABEL_SAMPLE_Δ48 = "Sample Δ48";
	public static final String INPUT_LABEL_STANDARD_δ48 = "Standard δ48";
	public static String INPUT_LABEL_STANDARD_Δ48 = "Standard Δ48";
	public static String OUTPUT_LABEL_Δ48_δ48_SLOPE = "Δ48 Nonlinearity Slope";
	public static String OUTPUT_LABEL_Δ48_δ48_INTERCEPTS = "Δ48 Nonlinearity Intercepts";
	public static String OUTPUT_LABEL_Δ48 = "Δ48 Nonlinearity Corrected";

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	@Override
	public VolatileKeys getVolatiles() {
		return new VolatileKeys(Calculator.class.getName());
	}

	@Override
	public String getInputLabelSampleCorrector() {
		return INPUT_LABEL_SAMPLE_δ48;
	}

	@Override
	public String getInputLabelSampleCorrectee() {
		return INPUT_LABEL_SAMPLE_Δ48;
	}

	@Override
	public String getInputLabelStandardCorrector() {
		return INPUT_LABEL_STANDARD_δ48;
	}

	@Override
	public String getInputLabelStandardCorrectee() {
		return INPUT_LABEL_STANDARD_Δ48;
	}

	@Override
	public String getOutputLabelSlope() {
		return OUTPUT_LABEL_Δ48_δ48_SLOPE;
	}

	@Override
	public String getOutputLabelIntercepts() {
		return OUTPUT_LABEL_Δ48_δ48_INTERCEPTS;
	}

	@Override
	public String getOutputLabel() {
		return OUTPUT_LABEL_Δ48;
	}
}
