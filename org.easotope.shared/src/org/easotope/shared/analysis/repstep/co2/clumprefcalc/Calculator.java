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

package org.easotope.shared.analysis.repstep.co2.clumprefcalc;

import org.easotope.shared.analysis.tables.RepStep;

public class Calculator extends org.easotope.shared.analysis.repstep.co2.bulkrefcalc.Calculator {
	public static final String INPUT_LABEL_V47_REF = "V47 Reference";
	public static final String INPUT_LABEL_V48_REF = "V48 Reference";
	public static final String INPUT_LABEL_V49_REF = "V49 Reference";

	public static final String OUTPUT_LABEL_V47_V44_REF_CALC = "V47/V44 Interp Ref";
	public static final String OUTPUT_LABEL_V48_V44_REF_CALC = "V48/V44 Interp Ref";
	public static final String OUTPUT_LABEL_V49_V44_REF_CALC = "V49/V44 Interp Ref";

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	@Override
	protected MzRatioLabels[] getMzRatioLabels() {
		return new MzRatioLabels[] {
			new MzRatioLabels("45", INPUT_LABEL_V45_REF, OUTPUT_LABEL_V45_V44_REF_CALC),
			new MzRatioLabels("46", INPUT_LABEL_V46_REF, OUTPUT_LABEL_V46_V44_REF_CALC),
			new MzRatioLabels("47", INPUT_LABEL_V47_REF, OUTPUT_LABEL_V47_V44_REF_CALC),
			new MzRatioLabels("48", INPUT_LABEL_V48_REF, OUTPUT_LABEL_V48_V44_REF_CALC),
			new MzRatioLabels("49", INPUT_LABEL_V49_REF, OUTPUT_LABEL_V49_V44_REF_CALC)
		};
	}
}
