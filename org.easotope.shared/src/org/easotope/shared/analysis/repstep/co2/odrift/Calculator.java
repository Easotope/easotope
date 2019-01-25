/*
 * Copyright © 2016-2019 by Devon Bowen.
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

package org.easotope.shared.analysis.repstep.co2.odrift;

import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.StandardParameter;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.co2.odrift.dependencies.Dependencies;
import org.easotope.shared.analysis.repstep.superclass.drift.VolatileKeys;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.NumericValue;
import org.easotope.shared.core.scratchpad.ReplicatePad;

public class Calculator extends org.easotope.shared.analysis.repstep.superclass.drift.Calculator {
	public static final String INPUT_LABEL_δ18O = "δ¹⁸O VPDB";
	public static final String OUTPUT_LABEL_δ18O = "δ¹⁸O VPDB Drift Corrected";

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	@Override
	public VolatileKeys getVolatiles() {
		return new VolatileKeys(Calculator.class.getName());
	}

	@Override
	public String getInputLabel() {
		return INPUT_LABEL_δ18O;
	}

	@Override
	public String getOutputLabel() {
		return OUTPUT_LABEL_δ18O;
	}

	@Override
	public StandardParameter getStandardParameter() {
		return StandardParameter.δ18O;
	}

	@Override
	public Double getExpectedStandardValue(Standard standard, DependencyManager dependencyManager) {
		NumericValue numericValue = standard.getValues().get(getStandardParameter().ordinal());

		if (numericValue == null) {
			return null;
		}

		double value = numericValue.getValue();

		switch (IsotopicScale.values()[numericValue.getDescription()]) {
			case VSMOW:
				Dependencies dependencies = (Dependencies) dependencyManager;
				double δ18O_VPDB_VSMOW = dependencies.getδ18O_VPDB_VSMOW();
				value = (value - δ18O_VPDB_VSMOW) / (1.0 + δ18O_VPDB_VSMOW / 1000.0);

			case VPDB:
				break;

			default:
				return null;
		}

		return value;
	}

	@Override
	public DependencyManager getDependencyManager(ReplicatePad[] replicatePads, int sampleNumber) {
		return new Dependencies();
	}
}
