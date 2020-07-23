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

package org.easotope.shared.analysis.samstep.co2.clumpaverage;

import org.easotope.shared.analysis.execute.SamStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.tables.SamStep;
import org.easotope.shared.core.scratchpad.SamplePad;

public class Calculator extends SamStepCalculator {
//	public static final String INPUT_LABEL_δ47 = "δ47";
//	public static final String INPUT_LABEL_δ48 = "δ48";
//	public static final String INPUT_LABEL_δ49 = "δ49";
	public static final String INPUT_LABEL_Δ47 = "Δ47";
//	public static final String INPUT_LABEL_Δ48 = "Δ48";
//	public static final String INPUT_LABEL_Δ49 = "Δ49";

//	public static final String OUTPUT_LABEL_δ47 = "δ47";
//	public static final String OUTPUT_LABEL_δ47_SD = "δ47 Standard Deviation";
//	public static final String OUTPUT_LABEL_δ47_SE = "δ47 Standard Error";
//	public static final String OUTPUT_LABEL_δ48 = "δ48";
//	public static final String OUTPUT_LABEL_δ48_SD = "δ48 Standard Deviation";
//	public static final String OUTPUT_LABEL_δ48_SE = "δ48 Standard Error";
//	public static final String OUTPUT_LABEL_δ49 = "δ49";
//	public static final String OUTPUT_LABEL_δ49_SD = "δ49 Standard Deviation";
//	public static final String OUTPUT_LABEL_δ49_SE = "δ49 Standard Error";
	public static final String OUTPUT_LABEL_Δ47 = "Δ47";
	public static final String OUTPUT_LABEL_Δ47_SD = "Δ47 Standard Deviation";
	public static final String OUTPUT_LABEL_Δ47_SE = "Δ47 Standard Error";
	public static final String OUTPUT_LABEL_Δ47_CI = "Δ47 Confidence Interval";
//	public static final String OUTPUT_LABEL_Δ48 = "Δ48";
//	public static final String OUTPUT_LABEL_Δ48_SD = "Δ48 Standard Deviation";
//	public static final String OUTPUT_LABEL_Δ48_SE = "Δ48 Standard Error";
//	public static final String OUTPUT_LABEL_Δ49 = "Δ47";
//	public static final String OUTPUT_LABEL_Δ49_SD = "Δ49 Standard Deviation";
//	public static final String OUTPUT_LABEL_Δ49_SE = "Δ49 Standard Error";

	public Calculator(SamStep samStep) {
		super(samStep);
	}

	@Override
	public DependencyManager getDependencyManager(SamplePad samplePad) {
		return null;
	}

	@Override
	public void calculate(SamplePad samplePad, DependencyManager dependencyManager) {
		samplePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_Δ47), labelToColumnName(OUTPUT_LABEL_Δ47_SD), labelToColumnName(OUTPUT_LABEL_Δ47_SE),labelToColumnName(OUTPUT_LABEL_Δ47_CI), false);
	}
}
