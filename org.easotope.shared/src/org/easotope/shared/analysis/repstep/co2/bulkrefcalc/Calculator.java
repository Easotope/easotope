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

package org.easotope.shared.analysis.repstep.co2.bulkrefcalc;

import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.shared.Messages;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.math.Polynomial;
import org.easotope.shared.math.PolynomialRegression;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2.DataFormat;

public class Calculator extends RepStepCalculator {
	public static final String PARAMETER_ALLOW_UNAVERAGED_REF_VALUES = "PARAMETER_ALLOW_UNAVERAGED_REF_VALUES";
	public static final boolean DEFAULT_ALLOW_UNAVERAGED_REF_VALUES = false;

	public static final String INPUT_LABEL_DISABLED = "Disabled";
	public static final String INPUT_LABEL_OFF_PEAK = "Off Peak";

	public static final String INPUT_BASIS_REF = "V44 Ref Basis";
	public static final String INPUT_BASIS_SAM = "V44 Sam Basis";

	public static final String INPUT_LABEL_V45_REF = "V45 Reference";
	public static final String INPUT_LABEL_V46_REF = "V46 Reference";

	public static final String OUTPUT_LABEL_V45_V44_REF_CALC = "V45/V44 Interp Ref";
	public static final String OUTPUT_LABEL_V46_V44_REF_CALC = "V46/V44 Interp Ref";

	public static String getVolatileDataLidi2BasisMz() {
		return Calculator.class.getName() + "VOLATILE_DATA_LIDI2_BASIS_MZ";
	}

	public static String getVolatileDataLidi2RefVals1() {
		return Calculator.class.getName() + "VOLATILE_DATA_LIDI2_REF_VALS_1";
	}

	public static String getVolatileDataLidi2RefPolynomial1() {
		return Calculator.class.getName() + "VOLATILE_DATA_LIDI2_POLYNOMIAL_1";
	}
	
	public static String getVolatileDataLidi2RefVals2() {
		return Calculator.class.getName() + "VOLATILE_DATA_LIDI2_REF_VALS_2";
	}

	public static String getVolatileDataLidi2RefPolynomial2() {
		return Calculator.class.getName() + "VOLATILE_DATA_LIDI2_POLYNOMIAL_2";
	}

	public static String getVolatileDataLidi2RefValsInterp() {
		return Calculator.class.getName() + "VOLATILE_DATA_LIDI2_REF_VALS_INTERP";
	}

	public static String getVolatileDataLidi2RefPolynomialInterp() {
		return Calculator.class.getName() + "VOLATILE_DATA_LIDI2_POLYNOMIAL_INTERP";
	}

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	@Override
	public DependencyManager getDependencyManager(ReplicatePad[] replicatePads, int standardNumber) {
		return null;
	}

	@Override
	public void calculate(ReplicatePad[] replicatePads, int padNumber, DependencyManager dependencyManager) {
		calculateReplicate(replicatePads[padNumber]);
	}

	private void calculateReplicate(ReplicatePad replicatePad) {
		for (AcquisitionPad acquisitionPad : replicatePad.getChildren()) {
			if (!isTrue(acquisitionPad, INPUT_LABEL_DISABLED)) {
				calculateAcquisition(acquisitionPad);
			}
		}
	}

	private void calculateAcquisition(AcquisitionPad acquisitionPad) {
		DataFormat dataFormat = acquisitionPad.getDataFormat();

		switch (dataFormat) {
			case DUALINLET:
				calculateDualInletAcquisition(acquisitionPad);
				break;

			case LIDI2:
				calculateLidi2Acquisition(acquisitionPad);
				break;

			default:
				break;
		}
	}
	
	//////////////
	// DUAL INLET
	//////////////
	
	private void calculateDualInletAcquisition(AcquisitionPad acquisitionPad) {
		CyclePad previousCyclePad = null;

		for (CyclePad cyclePad : acquisitionPad.getChildren()) {
			if (!isTrue(cyclePad, INPUT_LABEL_DISABLED) && !isTrue(cyclePad, INPUT_LABEL_OFF_PEAK)) {
				calculateCycle(cyclePad, previousCyclePad);
			}

			previousCyclePad = cyclePad;
		}
	}

	private void calculateCycle(CyclePad cyclePad, CyclePad previousCyclePad) {
	    final Double cycleV44Ref = getRef(cyclePad, previousCyclePad, labelToColumnName(INPUT_BASIS_REF));

	    if (cycleV44Ref == null) {
	    	return;
	    }
	    
		for (MzRatioLabels labels : getMzRatioLabels()) {
		    final Double cycleThisRef = getRef(cyclePad, previousCyclePad, labelToColumnName(labels.getInputRefLabel()));
		    cyclePad.setValue(labelToColumnName(labels.getOutputRefLabel()), cycleThisRef/cycleV44Ref);
		}
	}

	public boolean getAllowUnaveragedRefValues() {
		Boolean parameter = (Boolean) getParameter(Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES);
		return parameter == null ? DEFAULT_ALLOW_UNAVERAGED_REF_VALUES : parameter;
	}

	protected Double getRef(CyclePad cyclePad, CyclePad previousCyclePad, String columnName) {
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

	//////////////
	// LIDI2
	//////////////
	
	private enum State { FIRST_REF, SAMPLES, SECOND_REF };

	private void calculateLidi2Acquisition(AcquisitionPad acquisitionPad) {
		acquisitionPad.setVolatileData(getVolatileDataLidi2BasisMz(), "44");
		
		HashMap<String,ArrayList<Lidi2Point>> lidi2RefVals1 = new HashMap<String,ArrayList<Lidi2Point>>();
		acquisitionPad.setVolatileData(getVolatileDataLidi2RefVals1(), lidi2RefVals1);

		HashMap<String,PolynomialRegression> polynomialRegression1 = new HashMap<String,PolynomialRegression>();
		HashMap<String,Polynomial> polynomial1 = new HashMap<String,Polynomial>();
		acquisitionPad.setVolatileData(getVolatileDataLidi2RefPolynomial1(), polynomial1);
		
		HashMap<String,ArrayList<Lidi2Point>> lidi2RefVals2 = new HashMap<String,ArrayList<Lidi2Point>>();
		acquisitionPad.setVolatileData(getVolatileDataLidi2RefVals2(), lidi2RefVals2);

		HashMap<String,PolynomialRegression> polynomialRegression2 = new HashMap<String,PolynomialRegression>();
		HashMap<String,Polynomial> polynomial2 = new HashMap<String,Polynomial>();
		acquisitionPad.setVolatileData(getVolatileDataLidi2RefPolynomial2(), polynomial2);

		HashMap<String,ArrayList<Lidi2Point>> lidi2RefValsInterp = new HashMap<String,ArrayList<Lidi2Point>>();
		acquisitionPad.setVolatileData(getVolatileDataLidi2RefValsInterp(), lidi2RefValsInterp);

		HashMap<String,Polynomial> polynomialInterp = new HashMap<String,Polynomial>();
		acquisitionPad.setVolatileData(getVolatileDataLidi2RefPolynomialInterp(), polynomialInterp);

		MzRatioLabels[] mzLabels = getMzRatioLabels();
		
		for (MzRatioLabels labels : mzLabels) {
			lidi2RefVals1.put(labels.getMz(), new ArrayList<Lidi2Point>());
			polynomialRegression1.put(labels.getMz(), new PolynomialRegression(2));
			lidi2RefVals2.put(labels.getMz(), new ArrayList<Lidi2Point>());
			polynomialRegression2.put(labels.getMz(), new PolynomialRegression(2));
			lidi2RefValsInterp.put(labels.getMz(), new ArrayList<Lidi2Point>());
		}

		ArrayList<CyclePad> allCycles = acquisitionPad.getChildren();
		State currentState = State.FIRST_REF;
		int sampleStartIndex = 0;
		int sampleEndIndex = 0;

		for (int currentCycleIndex=0; currentCycleIndex<allCycles.size(); currentCycleIndex++) {
			CyclePad cyclePad = allCycles.get(currentCycleIndex);
			
			if (isTrue(cyclePad, INPUT_LABEL_OFF_PEAK)) {
				continue;
			}

			int numEmpty = 0;
			Double cycleBasisRef = (Double) cyclePad.getValue(labelToColumnName(INPUT_BASIS_REF));

			switch (currentState) {
				case FIRST_REF:
					if (cycleBasisRef == null) {
						numEmpty++;
					}
					
					for (MzRatioLabels labels : mzLabels) {
						Double cycleRef = (Double) cyclePad.getValue(labelToColumnName(labels.getInputRefLabel()));
						boolean disabled = (Boolean) cyclePad.getValue(labelToColumnName(INPUT_LABEL_DISABLED));
						
						if (cycleRef == null) {
							numEmpty++;
						} else {
							if (!disabled) {
								polynomialRegression1.get(labels.getMz()).addCoordinate(cycleBasisRef, cycleRef/cycleBasisRef);
							}
							lidi2RefVals1.get(labels.getMz()).add(new Lidi2Point(cycleBasisRef, cycleRef/cycleBasisRef, disabled));
						}
					}

					if (numEmpty != 0) {
						if (numEmpty != mzLabels.length + 1) {
							throw new RuntimeException(String.format(Messages.co2RefCalculator_missingRefValues, currentCycleIndex, numEmpty, mzLabels.length));
						}
						
						currentState = State.SAMPLES;
						sampleStartIndex = currentCycleIndex;
					}
					
					break;

				case SAMPLES:
					if (cycleBasisRef == null) {
						numEmpty++;
					}

					for (MzRatioLabels labels : mzLabels) {
						Double cycleRef = (Double) cyclePad.getValue(labelToColumnName(labels.getInputRefLabel()));

						if (cycleRef == null) {
							numEmpty++;
						}
					}

					if (numEmpty == 0) {
						currentState = State.SECOND_REF;
						sampleEndIndex = currentCycleIndex - 1;

					} else {
						if (numEmpty != mzLabels.length + 1) {
							throw new RuntimeException(String.format(Messages.co2RefCalculator_missingRefValues, currentCycleIndex, numEmpty, mzLabels.length));
						}
					}

					break;

				case SECOND_REF:
					for (MzRatioLabels labels : mzLabels) {
						Double cycleRef = (Double) cyclePad.getValue(labelToColumnName(labels.getInputRefLabel()));						
						boolean disabled = (Boolean) cyclePad.getValue(labelToColumnName(INPUT_LABEL_DISABLED));

						if (cycleRef == null) {
							numEmpty++;
						} else {
							if (!disabled) {
								polynomialRegression2.get(labels.getMz()).addCoordinate(cycleBasisRef, cycleRef/cycleBasisRef);
							}
							lidi2RefVals2.get(labels.getMz()).add(new Lidi2Point(cycleBasisRef, cycleRef/cycleBasisRef, disabled));
						}

						if (numEmpty != 0) {
							if (numEmpty != mzLabels.length) {
								throw new RuntimeException(String.format(Messages.co2RefCalculator_missingRefValues, currentCycleIndex, numEmpty, mzLabels.length));
							}
							
							currentState = State.SAMPLES;
						}
					}

					break;
			}
		}

		for (MzRatioLabels labels : mzLabels) {
			PolynomialRegression regression1 = polynomialRegression1.get(labels.getMz());
			double [] coeff1 = regression1.getPolynomial().getCoefficients();
			
			PolynomialRegression regression2 = polynomialRegression2.get(labels.getMz());
			double[] coeff2 = regression2.getPolynomial().getCoefficients();
			
			double coeff3[] = new double[coeff1.length];

			for (int i=0; i<coeff3.length; i++) {
				coeff3[i] = (coeff1[i] + coeff2[i]) / 2; 
			}

			polynomial1.put(labels.getMz(), regression1.getPolynomial());
			polynomial2.put(labels.getMz(), regression2.getPolynomial());
			polynomialInterp.put(labels.getMz(), new Polynomial(coeff3));
		}

		for (int i=sampleStartIndex; i<=sampleEndIndex; i++) {
			CyclePad cyclePad = allCycles.get(i);
			
			if (isTrue(cyclePad, INPUT_LABEL_OFF_PEAK)) {
				continue;
			}

			Double cycleBasisSam = (Double) cyclePad.getValue(labelToColumnName(INPUT_BASIS_SAM));
			boolean disabled = (Boolean) cyclePad.getValue(labelToColumnName(INPUT_LABEL_DISABLED));

			for (MzRatioLabels labels : mzLabels) {
				Polynomial polynomial = polynomialInterp.get(labels.getMz());
				double refValInterp = polynomial.evaluate(cycleBasisSam);

				lidi2RefValsInterp.get(labels.getMz()).add(new Lidi2Point(cycleBasisSam, refValInterp, disabled));
				cyclePad.setValue(labelToColumnName(labels.getOutputRefLabel()), refValInterp);
			}
		}
	}

	protected MzRatioLabels[] getMzRatioLabels() {
		return new MzRatioLabels[] {
			new MzRatioLabels("45", INPUT_LABEL_V45_REF, OUTPUT_LABEL_V45_V44_REF_CALC),
			new MzRatioLabels("46", INPUT_LABEL_V46_REF, OUTPUT_LABEL_V46_V44_REF_CALC)
		};
	}
	
	public class MzRatioLabels {
		private String mz;
		private String inputRefLabel;
		private String outputRefLabel;

		public MzRatioLabels(String mzRatio, String inputLabel, String outputLabel) {
			this.mz = mzRatio;
			this.inputRefLabel = inputLabel;
			this.outputRefLabel = outputLabel;
		}

		public String getMz() {
			return mz;
		}

		public String getInputRefLabel() {
			return inputRefLabel;
		}

		public String getOutputRefLabel() {
			return outputRefLabel;
		}
	}
	
	public class Lidi2Point {
		private double x;
		private double y;
		private boolean disabled;
		
		public Lidi2Point(double x, double y, boolean disabled) {
			this.x = x;
			this.y = y;
			this.disabled = disabled;
		}
		
		public double getX() {
			return x;
		}
		
		public double getY() {
			return y;
		}
		
		public boolean getDisabled() {
			return disabled;
		}
	}
}
