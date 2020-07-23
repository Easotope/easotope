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

package org.easotope.shared.analysis.repstep.co2.d47etfpbl;

import org.easotope.shared.Messages;
import org.easotope.shared.admin.AcidTempParameter;
import org.easotope.shared.admin.StandardParameter;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.co2.d47etfpbl.Calculator;
import org.easotope.shared.analysis.repstep.co2.d47etfpbl.dependencies.Dependencies;
import org.easotope.shared.analysis.repstep.superclass.etfpbl.VolatileKeys;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.ReplicatePad;

public class Calculator extends org.easotope.shared.analysis.repstep.superclass.etfpbl.Calculator {
	public static final String INPUT_LABEL = "Δ47";
	public static final String INPUT_LABEL_STANDARD_CORRECTOR = "Standard δ47";
	public static final String INPUT_LABEL_STANDARD_CORRECTEE = "Standard Δ47";
	public static final String INPUT_LABEL_STANDARD_ACID_TEMP = "Standard Acid Temperature";
	public static final String OUTPUT_LABEL_ETF_SLOPE = "ETF Slope";
	public static final String OUTPUT_LABEL_ETF_INTERCEPT = "ETF Intercept";
	public static final String OUTPUT_LABEL = "Δ47 CDES";

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	@Override
	public VolatileKeys getVolatiles() {
		return new VolatileKeys(Calculator.class.getName());
	}

	@Override
	public String getInputLabel() {
		return INPUT_LABEL;
	}

	@Override
	public String getInputStandardCorrectorLabel() {
		return INPUT_LABEL_STANDARD_CORRECTOR;
	}

	@Override
	public String getInputStandardCorrecteeLabel() {
		return INPUT_LABEL_STANDARD_CORRECTEE;
	}

	@Override
	public String getInputStandardAcidTempLabel() {
		return INPUT_LABEL_STANDARD_ACID_TEMP;
	}

	@Override
	public String getOutputEtfSlopeLabel() {
		return OUTPUT_LABEL_ETF_SLOPE;
	}

	@Override
	public String getOutputEtfInterceptLabel() {
		return OUTPUT_LABEL_ETF_INTERCEPT;
	}

	@Override
	public String getOutputLabel() {
		return OUTPUT_LABEL;
	}
	
	@Override
	public DependencyManager getDependencyManager(ReplicatePad[] replicatePads, int standardNumber) {
		return new Dependencies();
	}

	@Override
	public String getStandardNotConfiguredLabel() {
		return Messages.d47etfPblCalc_noKnownD47;
	}

	@Override
	public int getStandardParameter() {
		return StandardParameter.Δ47.ordinal();
	}

	@Override
	public int getAcidTempParameter() {
		return AcidTempParameter.Δ47.ordinal();
	}
}
