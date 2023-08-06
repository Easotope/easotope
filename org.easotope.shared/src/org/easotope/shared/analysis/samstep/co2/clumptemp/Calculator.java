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

package org.easotope.shared.analysis.samstep.co2.clumptemp;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.shared.analysis.execute.SamStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.samstep.co2.clumptemp.dependencies.Dependencies;
import org.easotope.shared.analysis.tables.SamStep;
import org.easotope.shared.core.DoubleTools;
import org.easotope.shared.core.scratchpad.Accumulator;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.math.BrentsMethod;

public class Calculator extends SamStepCalculator {
	public static final String INPUT_LABEL_ACID_TEMP = "Acid Temperature";
	public static final String INPUT_LABEL_ACID_FRACTIONATION_FACTOR = "Acid Fractionation Factor";
	public static final String INPUT_LABEL_Δ47 = "Δ47";

	public static final String OUTPUT_LABEL_Δ47_ANDERSON = "Δ47 Adjusted for Anderson";
	public static final String OUTPUT_LABEL_Δ47_ANDERSON_SD = "Δ47 Adjusted for Anderson SD";
	public static final String OUTPUT_LABEL_Δ47_ANDERSON_SE = "Δ47 Adjusted for Anderson SE";
	public static final String OUTPUT_LABEL_Δ47_ANDERSON_CI = "Δ47 Adjusted for Anderson CI";
	public static final String OUTPUT_LABEL_ANDERSON_MINUS_SE = "Anderson (Δ47-SE)";
	public static final String OUTPUT_LABEL_ANDERSON = "Anderson (Δ47)";
	public static final String OUTPUT_LABEL_ANDERSON_PLUS_SE = "Anderson (Δ47+SE)";

	public Calculator(SamStep samStep) {
		super(samStep);
	}

	@Override
	public DependencyManager getDependencyManager(SamplePad samplePad) {
		return new Dependencies();
	}

	@Override
	public void calculate(SamplePad samplePad, DependencyManager dependencyManager) {
		Dependencies dependencies = (Dependencies) dependencyManager;

		boolean atLeastOneConstantChanged = false;
		atLeastOneConstantChanged = atLeastOneConstantChanged || !DoubleTools.essentiallyEqual(dependencies.getGamma().getValue(), dependencies.getGamma().getEnumeration().getDefaultValue());
		atLeastOneConstantChanged = atLeastOneConstantChanged || !DoubleTools.essentiallyEqual(dependencies.getR13VPDB().getValue(), dependencies.getR13VPDB().getEnumeration().getDefaultValue());
		atLeastOneConstantChanged = atLeastOneConstantChanged || !DoubleTools.essentiallyEqual(dependencies.getR17VSMOW().getValue(), dependencies.getR17VSMOW().getEnumeration().getDefaultValue());
		atLeastOneConstantChanged = atLeastOneConstantChanged || !DoubleTools.essentiallyEqual(dependencies.getR18VSMOW().getValue(), dependencies.getR18VSMOW().getEnumeration().getDefaultValue());
		atLeastOneConstantChanged = atLeastOneConstantChanged || !DoubleTools.essentiallyEqual(dependencies.getD18OVPDBVSMOW().getValue(), dependencies.getD18OVPDBVSMOW().getEnumeration().getDefaultValue());

		// hack till I figure out what to do here
		atLeastOneConstantChanged = false;
				
		TempCalibrations[] tempCalibrations = { new Anderson() };

		for (TempCalibrations tempCalibration : tempCalibrations) {
			for (ReplicatePad replicatePad : samplePad.getChildren()) {
				if (atLeastOneConstantChanged) {
					replicatePad.setValue(labelToColumnName(tempCalibration.getD47AdjustedLabel()), "UNDEFINED");
					continue;
				}
				
				Double atReplicate = getDouble(replicatePad, INPUT_LABEL_ACID_TEMP);
				Double affReplicate = getDouble(replicatePad, INPUT_LABEL_ACID_FRACTIONATION_FACTOR);
				Double d47 = getDouble(replicatePad, INPUT_LABEL_Δ47);

				if (atReplicate == null || affReplicate == null || d47 == null) {
					continue;
				}

				// hack to handle inaccuracy of doubles
				if (atReplicate - tempCalibration.getATOfEquation() < 0.001) {
					atReplicate = tempCalibration.getATOfEquation();
				}

				double scalingFactor = atReplicate / tempCalibration.getATOfEquation();
				double affEquationScaled = tempCalibration.getAFFOfEquation() * scalingFactor;
				double diff = affEquationScaled - affReplicate;
				double d47Replicate = d47 + diff;

				replicatePad.setValue(labelToColumnName(tempCalibration.getD47AdjustedLabel()), d47Replicate);
			}

			if (atLeastOneConstantChanged) {
				samplePad.setValue(labelToColumnName(tempCalibration.getD47AdjustedLabel()), "UNDEFINED");
				samplePad.setValue(labelToColumnName(tempCalibration.getD47AdjustedSDLabel()), "UNDEFINED");
				samplePad.setValue(labelToColumnName(tempCalibration.getD47AdjustedSELabel()), "UNDEFINED");
				samplePad.setValue(labelToColumnName(tempCalibration.getD47AdjustedCILabel()), "UNDEFINED");
				samplePad.setValue(labelToColumnName(tempCalibration.getTempMinusSELabel()), "UNDEFINED");
				samplePad.setValue(labelToColumnName(tempCalibration.getTempLabel()), "UNDEFINED");
				samplePad.setValue(labelToColumnName(tempCalibration.getTempPlusSELabel()), "UNDEFINED");
				continue;
			}

			samplePad.setAccumulator(labelToColumnName(tempCalibration.getD47AdjustedLabel()), labelToColumnName(tempCalibration.getD47AdjustedSDLabel()), labelToColumnName(tempCalibration.getD47AdjustedSELabel()), labelToColumnName(tempCalibration.getD47AdjustedCILabel()), false);

			Accumulator accumulator = (Accumulator) samplePad.getValue(labelToColumnName(tempCalibration.getD47AdjustedLabel()));
			double[] meanStdDevSampleAndStdError = accumulator.getAccumulatedValues();

			double mean = meanStdDevSampleAndStdError[0];
			double stdError = meanStdDevSampleAndStdError[2];
			double answer = Double.NaN;

			if (Double.isNaN(stdError)) {
				samplePad.setValue(labelToColumnName(tempCalibration.getTempMinusSELabel()), Double.NaN);
				
			} else {
				try {
					answer = BrentsMethod.solve(tempCalibration.getTempEquation(mean-stdError), tempCalibration.getLowValue(), tempCalibration.getHighValue(), BrentsMethod.TOLERANCE);
				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, Calculator.class, "could not calculate temperature", e);
				}
	
				samplePad.setValue(labelToColumnName(tempCalibration.getTempMinusSELabel()), answer - dependencies.getKELVINCELCIUS());
			}
			
			try {
				answer = Double.NaN;
				answer = BrentsMethod.solve(tempCalibration.getTempEquation(mean), tempCalibration.getLowValue(), tempCalibration.getHighValue(), BrentsMethod.TOLERANCE);
			} catch (Exception e) {
				Log.getInstance().log(Level.INFO, Calculator.class, "could not calculate temperature", e);
			}

			samplePad.setValue(labelToColumnName(tempCalibration.getTempLabel()), answer - dependencies.getKELVINCELCIUS());

			if (Double.isNaN(stdError)) {
				samplePad.setValue(labelToColumnName(tempCalibration.getTempPlusSELabel()), Double.NaN);

			} else {
				try {
					answer = Double.NaN;
					answer = BrentsMethod.solve(tempCalibration.getTempEquation(mean+stdError), tempCalibration.getLowValue(), tempCalibration.getHighValue(), BrentsMethod.TOLERANCE);
				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, Calculator.class, "could not calculate temperature", e);
				}
	
				samplePad.setValue(labelToColumnName(tempCalibration.getTempPlusSELabel()), answer - dependencies.getKELVINCELCIUS());
			}
		}
	}

	abstract class TempCalibrations {
		protected double atOfEquation = Double.NaN;
		protected double affOfEquation = Double.NaN;
		protected String d47AdjustedLabel = null;
		protected String d47AdjustedSdLabel = null;
		protected String d47AdjustedSeLabel = null;
		protected String d47AdjustedCiLabel = null;
		protected String tempMinusSeLabel = null;
		protected String tempLabel = null;
		protected String tempPlusSeLabel = null;
		protected double lowValue = Double.NaN;
		protected double highValue = Double.NaN;

		public double getATOfEquation() {
			return atOfEquation;
		}
		
		public double getAFFOfEquation() {
			return affOfEquation;
		}

		public String getD47AdjustedLabel() {
			return d47AdjustedLabel;
		}

		public String getD47AdjustedSDLabel() {
			return d47AdjustedSdLabel;
		}

		public String getD47AdjustedSELabel() {
			return d47AdjustedSeLabel;
		}
		
		public String getD47AdjustedCILabel() {
			return d47AdjustedCiLabel;
		}

		public String getTempMinusSELabel() {
			return tempMinusSeLabel;
		}

		public String getTempLabel() {
			return tempLabel;
		}

		public String getTempPlusSELabel() {
			return tempPlusSeLabel;
		}

		public abstract TempEquation getTempEquation(double d47);

		public double getLowValue() {
			return lowValue;
		}

		public double getHighValue() {
			return highValue;
		}
	}

	public abstract class TempEquation implements BrentsMethod.Function {
		double d47;

		TempEquation(double d47) {
			this.d47 = d47;
		}
	}

	private class Anderson extends TempCalibrations {
		//Anderson et al 2021
		//WARNING: PUBLISHED VERSION HAS +- VALUES THAT HAVE BEEN REMOVED
		//D = 0.0391 * (10^6 / T^2) + 0.154

		Anderson() {
			atOfEquation = 25.0;
			affOfEquation = 0.000;
			d47AdjustedLabel = OUTPUT_LABEL_Δ47_ANDERSON;
			d47AdjustedSdLabel = OUTPUT_LABEL_Δ47_ANDERSON_SD;
			d47AdjustedSeLabel = OUTPUT_LABEL_Δ47_ANDERSON_SE;
			d47AdjustedCiLabel = OUTPUT_LABEL_Δ47_ANDERSON_CI;
			tempMinusSeLabel = OUTPUT_LABEL_ANDERSON_MINUS_SE;
			tempLabel = OUTPUT_LABEL_ANDERSON;
			tempPlusSeLabel = OUTPUT_LABEL_ANDERSON_PLUS_SE;
			lowValue = 1;
			highValue = 10000;
		}

		public TempEquation getTempEquation(double d47) {
			return new TempEquation(d47) {
				@Override
				public double solve(double t) {
					return (0.0391e6 / Math.pow(t, 2)) + 0.154 - this.d47;
				}
			};
		}
	}
}
