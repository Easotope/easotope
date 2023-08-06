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

package org.easotope.shared.analysis.repstep.superclass.acidfrac;

import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.superclass.acidfrac.dependencies.Dependencies;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.ReplicatePad;

public abstract class Calculator extends RepStepCalculator {
	public abstract String getInputLabel();
	public abstract String getOutputLabelAff();
	public abstract String getOutputLabel();

	protected abstract double getNewValue(double value, double factor);

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	@Override
	public final void calculate(ReplicatePad[] replicatePads, int padNumber, DependencyManager dependencyManager) {
		Double factor = null;
		Double value = getDouble(replicatePads[padNumber], getInputLabel());

		if (value != null) {
			Dependencies dependencies = (Dependencies) dependencyManager;
			factor = dependencies.getα();

			if (factor != null) {
				value = getNewValue(value, factor);
			}
		}

		replicatePads[padNumber].setValue(labelToColumnName(getOutputLabelAff()), factor);
	    replicatePads[padNumber].setValue(labelToColumnName(getOutputLabel()), value);
	}
}
