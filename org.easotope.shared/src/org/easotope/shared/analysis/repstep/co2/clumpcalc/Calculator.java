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

package org.easotope.shared.analysis.repstep.co2.clumpcalc;

import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.RefGasParameter;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.co2.clumpcalc.dependencies.Dependencies;
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
	public static final String INPUT_LABEL_V47_REF = "V47 Reference";
	public static final String INPUT_LABEL_V47_SAMPLE = "V47 Sample";
	public static final String INPUT_LABEL_V48_REF = "V48 Reference";
	public static final String INPUT_LABEL_V48_SAMPLE = "V48 Sample";
	public static final String INPUT_LABEL_V49_REF = "V49 Reference";
	public static final String INPUT_LABEL_V49_SAMPLE = "V49 Sample";

	public static final String OUTPUT_LABEL_δ45 = "δ45 Working Gas";
	public static final String OUTPUT_LABEL_δ45_SD = "δ45 Working Gas Standard Deviation";
	public static final String OUTPUT_LABEL_δ45_SE = "δ45 Working Gas Standard Error";
	public static final String OUTPUT_LABEL_δ45_CI = "δ45 Working Gas Confidence Interval";
	public static final String OUTPUT_LABEL_δ46 = "δ46 Working Gas";
	public static final String OUTPUT_LABEL_δ46_SD = "δ46 Working Gas Standard Deviation";
	public static final String OUTPUT_LABEL_δ46_SE = "δ46 Working Gas Standard Error";
	public static final String OUTPUT_LABEL_δ46_CI = "δ46 Working Gas Confidence Interval";
	public static final String OUTPUT_LABEL_δ47 = "δ47 Working Gas";
	public static final String OUTPUT_LABEL_δ47_SD = "δ47 Working Gas Standard Deviation";
	public static final String OUTPUT_LABEL_δ47_SE = "δ47 Working Gas Standard Error";
	public static final String OUTPUT_LABEL_δ47_CI = "δ47 Working Gas Confidence Interval";
	public static final String OUTPUT_LABEL_Δ47 = "Δ47 Working Gas";
	public static final String OUTPUT_LABEL_Δ47_SD = "Δ47 Working Gas Standard Deviation";
	public static final String OUTPUT_LABEL_Δ47_SE = "Δ47 Working Gas Standard Error";
	public static final String OUTPUT_LABEL_Δ47_CI = "Δ47 Working Gas Confidence Interval";
	public static final String OUTPUT_LABEL_δ48 = "δ48 Working Gas";
	public static final String OUTPUT_LABEL_δ48_SD = "δ48 Working Gas Standard Deviation";
	public static final String OUTPUT_LABEL_δ48_SE = "δ48 Working Gas Standard Error";
	public static final String OUTPUT_LABEL_δ48_CI = "δ48 Working Gas Confidence Interval";
	public static final String OUTPUT_LABEL_Δ48 = "Δ48 Working Gas";
	public static final String OUTPUT_LABEL_Δ48_SD = "Δ48 Working Gas Standard Deviation";
	public static final String OUTPUT_LABEL_Δ48_SE = "Δ48 Working Gas Standard Error";
	public static final String OUTPUT_LABEL_Δ48_CI = "Δ48 Working Gas Confidence Interval";
	public static final String OUTPUT_LABEL_δ49 = "δ49 Working Gas";
	public static final String OUTPUT_LABEL_δ49_SD = "δ49 Working Gas Standard Deviation";
	public static final String OUTPUT_LABEL_δ49_SE = "δ49 Working Gas Standard Error";
	public static final String OUTPUT_LABEL_δ49_CI = "δ49 Working Gas Confidence Interval";
	public static final String OUTPUT_LABEL_Δ49 = "Δ49 Working Gas";
	public static final String OUTPUT_LABEL_Δ49_SD = "Δ49 Working Gas Standard Deviation";
	public static final String OUTPUT_LABEL_Δ49_SE = "Δ49 Working Gas Standard Error";
	public static final String OUTPUT_LABEL_Δ49_CI = "Δ49 Working Gas Confidence Interval";
	public static final String OUTPUT_LABEL_49_PARAM = "49 Parameter";
	public static final String OUTPUT_LABEL_49_PARAM_SD = "49 Parameter Standard Deviation";
	public static final String OUTPUT_LABEL_49_PARAM_SE = "49 Parameter Standard Error";
	public static final String OUTPUT_LABEL_49_PARAM_CI = "49 Parameter Confidence Interval";

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

		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ45), labelToColumnName(OUTPUT_LABEL_δ45_SD), labelToColumnName(OUTPUT_LABEL_δ45_SE), labelToColumnName(OUTPUT_LABEL_δ45_CI), false);
		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ46), labelToColumnName(OUTPUT_LABEL_δ46_SD), labelToColumnName(OUTPUT_LABEL_δ46_SE), labelToColumnName(OUTPUT_LABEL_δ46_CI), false);
		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ47), labelToColumnName(OUTPUT_LABEL_δ47_SD), labelToColumnName(OUTPUT_LABEL_δ47_SE), labelToColumnName(OUTPUT_LABEL_δ47_CI), false);
		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_Δ47), labelToColumnName(OUTPUT_LABEL_Δ47_SD), labelToColumnName(OUTPUT_LABEL_Δ47_SE), labelToColumnName(OUTPUT_LABEL_Δ47_CI), false);
		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ48), labelToColumnName(OUTPUT_LABEL_δ48_SD), labelToColumnName(OUTPUT_LABEL_δ48_SE), labelToColumnName(OUTPUT_LABEL_δ48_CI), false);
		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_Δ48), labelToColumnName(OUTPUT_LABEL_Δ48_SD), labelToColumnName(OUTPUT_LABEL_Δ48_SE), labelToColumnName(OUTPUT_LABEL_Δ48_CI), false);
		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ49), labelToColumnName(OUTPUT_LABEL_δ49_SD), labelToColumnName(OUTPUT_LABEL_δ49_SE), labelToColumnName(OUTPUT_LABEL_δ49_CI), false);
		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_Δ49), labelToColumnName(OUTPUT_LABEL_Δ49_SD), labelToColumnName(OUTPUT_LABEL_Δ49_SE), labelToColumnName(OUTPUT_LABEL_Δ49_CI), false);
		replicatePad.setAccumulator(labelToColumnName(OUTPUT_LABEL_49_PARAM), labelToColumnName(OUTPUT_LABEL_49_PARAM_SD), labelToColumnName(OUTPUT_LABEL_49_PARAM_SE),labelToColumnName(OUTPUT_LABEL_49_PARAM_CI), false);
	}

	private void calculateAcquisition(AcquisitionPad acquisitionPad, Dependencies dependencies, double δ13C_Ref, double δ18O_Ref) {
		CyclePad previousCyclePad = null;

		for (CyclePad cyclePad : acquisitionPad.getChildren()) {
			if (!isTrue(cyclePad, INPUT_LABEL_DISABLED) && !isTrue(cyclePad, INPUT_LABEL_OFF_PEAK)) {
				calculateCycle(cyclePad, previousCyclePad, dependencies, δ13C_Ref, δ18O_Ref);
			}

			previousCyclePad = cyclePad;
		}

		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ45), labelToColumnName(OUTPUT_LABEL_δ45_SD), labelToColumnName(OUTPUT_LABEL_δ45_SE), labelToColumnName(OUTPUT_LABEL_δ45_CI), false);
		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ46), labelToColumnName(OUTPUT_LABEL_δ46_SD), labelToColumnName(OUTPUT_LABEL_δ46_SE), labelToColumnName(OUTPUT_LABEL_δ46_CI), false);
		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ47), labelToColumnName(OUTPUT_LABEL_δ47_SD), labelToColumnName(OUTPUT_LABEL_δ47_SE), labelToColumnName(OUTPUT_LABEL_δ47_CI), false);
		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_Δ47), labelToColumnName(OUTPUT_LABEL_Δ47_SD), labelToColumnName(OUTPUT_LABEL_Δ47_SE), labelToColumnName(OUTPUT_LABEL_Δ47_CI), false);
		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ48), labelToColumnName(OUTPUT_LABEL_δ48_SD), labelToColumnName(OUTPUT_LABEL_δ48_SE), labelToColumnName(OUTPUT_LABEL_δ48_CI), false);
		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_Δ48), labelToColumnName(OUTPUT_LABEL_Δ48_SD), labelToColumnName(OUTPUT_LABEL_Δ48_SE), labelToColumnName(OUTPUT_LABEL_Δ48_CI), false);
		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_δ49), labelToColumnName(OUTPUT_LABEL_δ49_SD), labelToColumnName(OUTPUT_LABEL_δ49_SE), labelToColumnName(OUTPUT_LABEL_δ49_CI), false);
		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_Δ49), labelToColumnName(OUTPUT_LABEL_Δ49_SD), labelToColumnName(OUTPUT_LABEL_Δ49_SE), labelToColumnName(OUTPUT_LABEL_Δ49_CI), false);
		acquisitionPad.setAccumulator(labelToColumnName(OUTPUT_LABEL_49_PARAM), labelToColumnName(OUTPUT_LABEL_49_PARAM_SD), labelToColumnName(OUTPUT_LABEL_49_PARAM_SE),labelToColumnName(OUTPUT_LABEL_49_PARAM_CI), false);
	}

	private void calculateCycle(CyclePad cyclePad, CyclePad previousCyclePad, Dependencies dependencies, double δ13C_Ref, double δ18O_Ref) {
		// V44_Ref ... V49_Ref for your reference gas
	    // V44_Sample ... V49_Sample for your sample gas
	    // ...based on voltage measurements from your dual-inlet spectrometer
	    // Ideally, these voltages reflect abundance ratios

	    final Double cycle_V44_Ref = getRef(cyclePad, previousCyclePad, labelToColumnName(INPUT_LABEL_V44_REF));
	    final Double cycle_V44_Sample = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V44_SAMPLE));

	    final Double cycle_V45_Ref = getRef(cyclePad, previousCyclePad, labelToColumnName(INPUT_LABEL_V45_REF));
	    final Double cycle_V45_Sample = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V45_SAMPLE));

	    final Double cycle_V46_Ref = getRef(cyclePad, previousCyclePad, labelToColumnName(INPUT_LABEL_V46_REF));	    
	    final Double cycle_V46_Sample = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V46_SAMPLE));

	    final Double cycle_V47_Ref = getRef(cyclePad, previousCyclePad, labelToColumnName(INPUT_LABEL_V47_REF));
	    final Double cycle_V47_Sample = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V47_SAMPLE));
	    
	    final Double cycle_V48_Ref = getRef(cyclePad, previousCyclePad, labelToColumnName(INPUT_LABEL_V48_REF));	    
	    final Double cycle_V48_Sample = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V48_SAMPLE));

	    final Double cycle_V49_Ref = getRef(cyclePad, previousCyclePad, labelToColumnName(INPUT_LABEL_V49_REF));
	    final Double cycle_V49_Sample = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V49_SAMPLE));

	    if (cycle_V44_Ref == null || cycle_V44_Sample == null ||
	    		cycle_V45_Ref == null || cycle_V45_Sample == null ||
	    		cycle_V46_Ref == null || cycle_V46_Sample == null ||
	    		cycle_V47_Ref == null || cycle_V47_Sample == null ||
	    		cycle_V48_Ref == null || cycle_V48_Sample == null ||
	    		cycle_V49_Ref == null || cycle_V49_Sample == null) {

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

	    // (1.c) Compute abundances of isotopologues with masses 44 to 49
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

	    	// Mass 47
	    final double C13_18_16_Ref = C13_Ref * C18_Ref * C16_Ref * 2;
	    final double C13_17_17_Ref = C13_Ref * C17_Ref * C17_Ref;
	    final double C12_18_17_Ref = C12_Ref * C18_Ref * C17_Ref * 2;

	    // Mass 48
	    final double C13_18_17_Ref = C13_Ref * C18_Ref * C17_Ref * 2;
	    final double C12_18_18_Ref = C12_Ref * C18_Ref * C18_Ref;

	    // Mass 49
	    final double C13_18_18_Ref = C13_Ref * C18_Ref * C18_Ref;
	    
	    // (1.d) Compute binned abundances of isotopologues grouped by mass

	    final double C44_Ref = C12_16_16_Ref;
	    final double C45_Ref = C13_16_16_Ref + C12_17_16_Ref;
	    final double C46_Ref = C12_18_16_Ref + C13_17_16_Ref + C12_17_17_Ref;
	    final double C47_Ref = C13_18_16_Ref + C13_17_17_Ref + C12_18_17_Ref;
	    final double C48_Ref = C13_18_17_Ref + C12_18_18_Ref;
	    final double C49_Ref = C13_18_18_Ref;

	    // (2) Compute the composition of your sample gas
	    // (2.a) Measure the abundance ratios of your sample gas for masses 45 to 49

	    final double R45_Ref = C45_Ref / C44_Ref;
	    final double R46_Ref = C46_Ref / C44_Ref;
	    final double R47_Ref = C47_Ref / C44_Ref;
	    final double R48_Ref = C48_Ref / C44_Ref;
	    final double R49_Ref = C49_Ref / C44_Ref;

	    final double R45_Sample = R45_Ref * cycle_V45_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V45_Ref;
	    final double R46_Sample = R46_Ref * cycle_V46_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V46_Ref;
	    final double R47_Sample = R47_Ref * cycle_V47_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V47_Ref;
	    final double R48_Sample = R48_Ref * cycle_V48_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V48_Ref;
	    final double R49_Sample = R49_Ref * cycle_V49_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V49_Ref;

	    // And, using the conventional δ notation (relative to your reference gas): 

	    double δ45 = 1000 * (cycle_V45_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V45_Ref - 1);
	    cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_δ45), δ45);

	    double δ46 = 1000 * (cycle_V46_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V46_Ref - 1);
	    cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_δ46), δ46);

	    double δ47 = 1000 * (cycle_V47_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V47_Ref - 1);
	    cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_δ47), δ47);

	    double δ48 = 1000 * (cycle_V48_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V48_Ref - 1);
	    cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_δ48), δ48);

	    double δ49 = 1000 * (cycle_V49_Sample / cycle_V44_Sample * cycle_V44_Ref / cycle_V49_Ref - 1);
	    cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_δ49), δ49);
	    
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

	    // (3) Compute "raw" Δ values of your sample gas
	    // (3.a) "Scramble" your sample gas
	    // This means computing the abundance of each isotopologue of a gas with the same bulk composition as your sample, but in a stochastic state.
	    
	    final double C12_Sample = 1 / (1 + R13_Sample);
	    final double C13_Sample = R13_Sample / (1 + R13_Sample);
	    final double C16_Sample = 1 / (1 + R17_Sample + R18_Sample);
	    final double C17_Sample = R17_Sample / (1 + R17_Sample + R18_Sample);
	    final double C18_Sample = R18_Sample / (1 + R17_Sample + R18_Sample);
	    
	    // Mass 44
	    final double CS12_16_16_Sample = C12_Sample * C16_Sample * C16_Sample;

	    // Mass 45
	    final double CS13_16_16_Sample = C13_Sample * C16_Sample * C16_Sample;
	    	final double CS12_17_16_Sample = C12_Sample * C17_Sample * C16_Sample * 2;
	    	
	    // Mass 46
	    	final double CS12_18_16_Sample = C12_Sample * C18_Sample * C16_Sample * 2;
	    	final double CS13_17_16_Sample = C13_Sample * C17_Sample * C16_Sample * 2;
	    	final double CS12_17_17_Sample = C12_Sample * C17_Sample * C17_Sample;

	    //Mass 47
	    	final double CS13_18_16_Sample = C13_Sample * C18_Sample * C16_Sample * 2;
	    	final double CS13_17_17_Sample = C13_Sample * C17_Sample * C17_Sample;
	    	final double CS12_18_17_Sample = C12_Sample * C18_Sample * C17_Sample * 2;
	        
	    	// Mass 48
	    	final double CS13_18_17_Sample = C13_Sample * C18_Sample * C17_Sample * 2;
	    	final double CS12_18_18_Sample = C12_Sample * C18_Sample * C18_Sample;
	        
	    	// Mass 49
	    	final double CS13_18_18_Sample = C13_Sample * C18_Sample * C18_Sample;
	    	
	    	final double CS44_Sample = CS12_16_16_Sample;
	    	final double CS45_Sample = CS13_16_16_Sample + CS12_17_16_Sample;
	    	final double CS46_Sample = CS12_18_16_Sample + CS13_17_16_Sample + CS12_17_17_Sample;
	    	final double CS47_Sample = CS13_18_16_Sample + CS13_17_17_Sample + CS12_18_17_Sample;
	    	final double CS48_Sample = CS13_18_17_Sample + CS12_18_18_Sample;
	    	final double CS49_Sample = CS13_18_18_Sample;

	    // Ending up with the following "stochastic abundance ratios"

	    final double RS45_Sample = CS45_Sample / CS44_Sample;
	    final double RS46_Sample = CS46_Sample / CS44_Sample;
	    final double RS47_Sample = CS47_Sample / CS44_Sample;
	    	final double RS48_Sample = CS48_Sample / CS44_Sample;
	    	final double RS49_Sample = CS49_Sample / CS44_Sample;

	    	// (3.b) Compute raw Δ values
	    	// These Δ values are called "raw" because they have not yet been corrected for a number of analytical artifacts.
	    	// Most importantly, we have assumed that your reference gas is in a stochastic state, which is unlikely.
	    	// This is why raw Δ47 values are typically underestimated by roughly the actual Δ47 value of your reference gas.

	    	double Δ47 = 1000 * ((R47_Sample/RS47_Sample - 1) - (R46_Sample/RS46_Sample - 1) - (R45_Sample/RS45_Sample - 1));
		cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_Δ47), Δ47);
	    	
		double Δ48 = 1000 * ((R48_Sample/RS48_Sample - 1) - 2 * (R46_Sample/RS46_Sample - 1));
		cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_Δ48), Δ48);

	    	double Δ49 = 1000 * ((R49_Sample/RS49_Sample - 1) - 2 * (R46_Sample/RS46_Sample - 1) - (R45_Sample/RS45_Sample - 1));
	    	cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_Δ49), Δ49);

	    double param49 = (cycle_V49_Sample / cycle_V44_Sample - cycle_V49_Ref / cycle_V44_Ref) * 10000;
	    cyclePad.setValue(labelToColumnName(OUTPUT_LABEL_49_PARAM), param49);
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
