/*
 * Copyright © 2016 by Devon Bowen.
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

package org.easotope.shared.analysis.repstep.co2.co2calc;

import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.RefGasParameter;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.co2.co2calc.dependencies.Dependencies;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.NumericValue;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.math.BrentsMethod;

public class Calculator extends RepStepCalculator {
	public static final String PARAMETER_ALLOW_UNAVERAGED_REF_VALUES = "PARAMETER_ALLOW_UNAVERAGED_REF_VALUES";
	public static final boolean DEFAULT_ALLOW_UNAVERAGED_REF_VALUES = false;

	public static final String INPUT_LABEL_DISABLED = "Disabled";
	public static final String INPUT_LABEL_OFF_PEAK = "Off Peak";
	public static final String INPUT_LABEL_V44_REF = "V44 Reference";
	public static final String INPUT_LABEL_V44_SAMPLE = "V44 Sample";
	public static final String INPUT_LABEL_V45_REF = "V45 Reference";
	public static final String INPUT_LABEL_V45_SAMPLE = "V45 Sample";
	public static final String INPUT_LABEL_V46_REF = "V46 Reference";
	public static final String INPUT_LABEL_V46_SAMPLE = "V46 Sample";

	public static final String OUTPUT_LABEL_δ13C = "δ¹³C VPDB";
	public static final String OUTPUT_LABEL_δ13C_SD = "δ¹³C VPDB Standard Deviation";
	public static final String OUTPUT_LABEL_δ13C_SE = "δ¹³C VPDB Standard Error";
	public static final String OUTPUT_LABEL_δ18O_VPDB = "δ¹⁸O VPDB";
	public static final String OUTPUT_LABEL_δ18O_VPDB_SD = "δ¹⁸O VPDB Standard Deviation";
	public static final String OUTPUT_LABEL_δ18O_VPDB_SE = "δ¹⁸O VPDB Standard Error";
	public static final String OUTPUT_LABEL_δ18O_VSMOW = "δ¹⁸O VSMOW";
	public static final String OUTPUT_LABEL_δ18O_VSMOW_SD = "δ¹⁸O VSMOW Standard Deviation";
	public static final String OUTPUT_LABEL_δ18O_VSMOW_SE = "δ¹⁸O VSMOW Standard Error";

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	public boolean getAllowUnaveragedRefValues() {
		Boolean parameter = (Boolean) getParameter(Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES);
		return parameter == null ? DEFAULT_ALLOW_UNAVERAGED_REF_VALUES : parameter;
	}

	@Override
	public DependencyManager getDependencyManager(ReplicatePad[] replicatePads, int standardNumber) {
		return new Dependencies();
	}

	@Override
	public void calculate(ReplicatePad[] replicatePads, int padNumber, DependencyManager dependencyManager) {
		Dependencies dependencies = (Dependencies) dependencyManager;

		// δ13CRef - the nominal carbon isotope composition of your reference gas vs VPDB
		// δ18ORef - the nominal oxygen isotope composition of your reference gas vs VSMOW

		final double δ13C_Ref = dependencies.getReferenceGas().getValues().get(RefGasParameter.δ13C.ordinal()).getValue();

		NumericValue numericValue = dependencies.getReferenceGas().getValues().get(RefGasParameter.δ18O.ordinal());
		double δ18O_Ref = numericValue.getValue();

		if (numericValue.getDescription() == IsotopicScale.VPDB.ordinal()) {
			double δ18O_VPDB_VSMOW = dependencies.getδ18O_VPDB_VSMOW();
			δ18O_Ref = (1.0 + δ18O_VPDB_VSMOW / 1000.0) * δ18O_Ref + δ18O_VPDB_VSMOW;
		}

		ReplicatePad replicate = replicatePads[padNumber];
		calculateReplicate(replicate, dependencies, δ13C_Ref, δ18O_Ref);
	}

	private void calculateReplicate(ReplicatePad replicatePad, Dependencies dependencies, double δ13C_Ref, double δ18O_Ref) {
		for (AcquisitionPad acquisitionPad : replicatePad.getChildren()) {
			if (!isTrue(acquisitionPad, INPUT_LABEL_DISABLED)) {
				calculateAcquisition(acquisitionPad, dependencies, δ13C_Ref, δ18O_Ref);
			}
		}

		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ13C), labelToColumnName(OUTPUT_LABEL_δ13C_SD), labelToColumnName(OUTPUT_LABEL_δ13C_SE), false);
		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ18O_VPDB), labelToColumnName(OUTPUT_LABEL_δ18O_VPDB_SD), labelToColumnName(OUTPUT_LABEL_δ18O_VPDB_SE), false);
		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ18O_VSMOW), labelToColumnName(OUTPUT_LABEL_δ18O_VSMOW_SD), labelToColumnName(OUTPUT_LABEL_δ18O_VSMOW_SE), false);
	}

	private void calculateAcquisition(AcquisitionPad acquisitionPad, Dependencies dependencies, double δ13C_Ref, double δ18O_Ref) {
		CyclePad previousCyclePad = null;

		for (CyclePad cyclePad : acquisitionPad.getChildren()) {
			if (!isTrue(cyclePad, INPUT_LABEL_DISABLED) && !isTrue(cyclePad, INPUT_LABEL_OFF_PEAK)) {
				calculateCycle(cyclePad, previousCyclePad, dependencies, δ13C_Ref, δ18O_Ref);
			}

			previousCyclePad = cyclePad;
		}

		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ13C), labelToColumnName(OUTPUT_LABEL_δ13C_SD), labelToColumnName(OUTPUT_LABEL_δ13C_SE), false);
		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ18O_VPDB), labelToColumnName(OUTPUT_LABEL_δ18O_VPDB_SD), labelToColumnName(OUTPUT_LABEL_δ18O_VPDB_SE), false);
		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ18O_VSMOW), labelToColumnName(OUTPUT_LABEL_δ18O_VSMOW_SD), labelToColumnName(OUTPUT_LABEL_δ18O_VSMOW_SE), false);
	}

	private void calculateCycle(CyclePad cyclePad, CyclePad previousCyclePad, Dependencies dependencies, double δ13C_Ref, double δ18O_Ref) {
		// V44_Ref ... V46_Ref for your reference gas
	    // V44_Sample ... V46_Sample for your sample gas
	    // ...based on voltage measurements from your dual-inlet spectrometer
	    // Ideally, these voltages reflect abundance ratios

	    final Double cycle_V44_Ref = getRef(cyclePad, previousCyclePad, labelToColumnName(INPUT_LABEL_V44_REF));
	    final Double cycle_V44_Sample = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V44_SAMPLE));

	    final Double cycle_V45_Ref = getRef(cyclePad, previousCyclePad, labelToColumnName(INPUT_LABEL_V45_REF));
	    final Double cycle_V45_Sample = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V45_SAMPLE));

	    final Double cycle_V46_Ref = getRef(cyclePad, previousCyclePad, labelToColumnName(INPUT_LABEL_V46_REF));	    
	    final Double cycle_V46_Sample = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V46_SAMPLE));

	    if (cycle_V44_Ref == null || cycle_V44_Sample == null ||
	    		cycle_V45_Ref == null || cycle_V45_Sample == null ||
	    		cycle_V46_Ref == null || cycle_V46_Sample == null) {

	    		return;
	    }

		// (1) Compute the isotopologue composition of your reference gas
		// (1.a) Compute abundance ratios of 13C/12C, 17O/16O and 18O/16O

	    final double R13_Ref = dependencies.getR13_VPDB() * (1 + δ13C_Ref / 1000);
	    final double R18_Ref = dependencies.getR18_VSMOW() * (1 + δ18O_Ref / 1000);
	    final double R17_Ref = dependencies.getR17_VSMOW() * Math.pow(R18_Ref / dependencies.getR18_VSMOW(), dependencies.getλ());

	    // (1.b) Compute abundances of 12C, 13C, 16O, 17O and 18O

	    final double C12_Ref = 1 / (1 + R13_Ref);
	    final double C13_Ref = R13_Ref / (1 + R13_Ref);
	    final double C16_Ref = 1 / (1 + R17_Ref + R18_Ref);
	    final double C17_Ref = R17_Ref / (1 + R17_Ref + R18_Ref);
	    final double C18_Ref = R18_Ref / (1 + R17_Ref + R18_Ref);

	    // (1.c) Compute abundances of isotopologues with masses 44 to 46
	    // ...by making the assumption that your reference gas is stochastic (this is usually not true, but we correct for that later in the process).

	    // Mass 44
	    final double C12_16_16_Ref = C12_Ref * C16_Ref * C16_Ref;

	    // Mass 45
	    final double C13_16_16_Ref = C13_Ref * C16_Ref * C16_Ref;
	    final double C12_17_16_Ref = C12_Ref * C17_Ref * C16_Ref * 2;

	    // Mass 46
	    final double C12_18_16_Ref = C12_Ref * C18_Ref * C16_Ref * 2;
	    final double C13_17_16_Ref = C13_Ref * C17_Ref * C16_Ref * 2;
	    final double C12_17_17_Ref = C12_Ref * C17_Ref * C17_Ref;
	    
	    // (1.d) Compute binned abundances of isotopologues grouped by mass

	    final double C44_Ref = C12_16_16_Ref;
	    final double C45_Ref = C13_16_16_Ref + C12_17_16_Ref;
	    final double C46_Ref = C12_18_16_Ref + C13_17_16_Ref + C12_17_17_Ref;

	    // (2) Compute the composition of your sample gas
	    // (2.a) Measure the abundance ratios of your sample gas for masses 45 to 46

	    final double R45_Ref = C45_Ref / C44_Ref;
	    final double R46_Ref = C46_Ref / C44_Ref;

	    final double R45_Sample = R45_Ref * cycle_V45_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V45_Ref;
	    final double R46_Sample = R46_Ref * cycle_V46_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V46_Ref;
	    
	    // (2.b) Compute the bulk composition of your sample gas
	    // One way to do that is to define
	    
	    final double K = dependencies.getR17_VSMOW() * Math.pow(dependencies.getR18_VSMOW(), -dependencies.getλ());

		// You can then compute R18-Sample by numerically solving the following equation:
		//
		// –3K^2 × R18-Sample^2λ + 2K × R45-Sample × R18-Sample^λ + 2 R18-Sample - R46-Sample = 0;
		//
		// (Assonov & Brenninkmeijer, 2003)

	    Function function = new Function(K, dependencies.getλ(), R45_Sample, R46_Sample);
	    double R18_Sample = BrentsMethod.solve(function, 0, 1, BrentsMethod.TOLERANCE);

	    // R17-Sample and R13-Sample may then be directly calculated:

	    final double R17_Sample = K * Math.pow(R18_Sample, dependencies.getλ());
	    double R13_Sample = R45_Sample - 2 * R17_Sample;

	    	// (3) Compute δ values

	    double δ18O = (R18_Sample - dependencies.getR18_VSMOW()) / dependencies.getR18_VSMOW() * 1000;
		cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_δ18O_VSMOW), δ18O);

		// convert to VPDB
		double δ18O_VPDB_VSMOW = dependencies.getδ18O_VPDB_VSMOW();
		double δ18O_pdb = (δ18O - δ18O_VPDB_VSMOW) / (1.0 + δ18O_VPDB_VSMOW / 1000.0);
		cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_δ18O_VPDB), δ18O_pdb);

	    double δ13C = (R13_Sample - dependencies.getR13_VPDB()) / dependencies.getR13_VPDB() * 1000;
	    cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_δ13C), δ13C);
	}

	private Double getRef(CyclePad cyclePad, CyclePad previousCyclePad, String columnName) {
		boolean previousOffPeak = false;
		Double previousValue = null;

		if (previousCyclePad != null) {
			previousOffPeak = isTrue(previousCyclePad, INPUT_LABEL_OFF_PEAK);
			previousValue = (Double) previousCyclePad.getValue(columnName);
		}

		Double thisValue = (Double) cyclePad.getValue(columnName);

		if (!previousOffPeak && previousValue != null && thisValue != null) {
			return (previousValue + thisValue) / 2;
		}

		return getAllowUnaveragedRefValues() ? (Double) cyclePad.getValue(columnName) : null;
	}

	private class Function implements BrentsMethod.Function {
		private double K;
		private double λ;
		private double R45_Sample;
		private double R46_Sample;

		private Function(double K, double λ, double R45_Sample, double R46_Sample) {
			this.K = K;
			this.λ = λ;
			this.R45_Sample = R45_Sample;
			this.R46_Sample = R46_Sample;
		}

		public double solve(double R18_Sample) {
			double answer = -3*K*K * Math.pow(R18_Sample, 2*λ);
			answer += 2*K * R45_Sample * Math.pow(R18_Sample, λ);
			answer += 2 * R18_Sample;
			answer -= R46_Sample;
			return answer;
		}
	}
}
