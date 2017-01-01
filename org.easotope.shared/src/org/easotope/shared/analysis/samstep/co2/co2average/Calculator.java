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

package org.easotope.shared.analysis.samstep.co2.co2average;

import org.easotope.shared.analysis.execute.SamStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.tables.SamStep;
import org.easotope.shared.core.scratchpad.SamplePad;

public class Calculator extends SamStepCalculator {
	public static final String INPUT_LABEL_δ13C_VPDB = "δ13C VPDB";
	public static final String INPUT_LABEL_δ18O_VPDB = "δ18O VPDB";
	public static final String INPUT_LABEL_δ18O_VSMOW = "δ18O VSMOW";

	public static final String OUTPUT_LABEL_δ13C_VPDB = "δ13C VPDB";
	public static final String OUTPUT_LABEL_δ13C_VPDB_SD = "δ13C VPDB Standard Deviation";
	public static final String OUTPUT_LABEL_δ13C_VPDB_SE = "δ13C VPDB Standard Error";
	public static final String OUTPUT_LABEL_δ18O_VPDB = "δ18O VPDB";
	public static final String OUTPUT_LABEL_δ18O_VPDB_SD = "δ18O VPDB Standard Deviation";
	public static final String OUTPUT_LABEL_δ18O_VPDB_SE = "δ18O VPDB Standard Error";
	public static final String OUTPUT_LABEL_δ18O_VSMOW = "δ18O VSMOW";
	public static final String OUTPUT_LABEL_δ18O_VSMOW_SD = "δ18O VSMOW Standard Deviation";
	public static final String OUTPUT_LABEL_δ18O_VSMOW_SE = "δ18O VSMOW Standard Error";

	public Calculator(SamStep samStep) {
		super(samStep);
	}

	@Override
	public DependencyManager getDependencyManager(SamplePad samplePad) {
		return null;
	}

	@Override
	public void calculate(SamplePad samplePad, DependencyManager dependencyManager) {
		samplePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ13C_VPDB), labelToColumnName(OUTPUT_LABEL_δ13C_VPDB_SD), labelToColumnName(OUTPUT_LABEL_δ13C_VPDB_SE), false);
		samplePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ18O_VPDB), labelToColumnName(OUTPUT_LABEL_δ18O_VPDB_SD), labelToColumnName(OUTPUT_LABEL_δ18O_VPDB_SE), false);
		samplePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ18O_VSMOW), labelToColumnName(OUTPUT_LABEL_δ18O_VSMOW_SD), labelToColumnName(OUTPUT_LABEL_δ18O_VSMOW_SE), false);
	}
}
