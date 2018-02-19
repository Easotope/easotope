/*
 * Copyright © 2016-2018 by Devon Bowen.
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
import org.easotope.shared.core.scratchpad.AccumulatorStdErr;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.math.BrentsMethod;

public class Calculator extends SamStepCalculator {
	public static final String INPUT_LABEL_ACID_TEMP = "Acid Temperature";
	public static final String INPUT_LABEL_ACID_FRACTIONATION_FACTOR = "Acid Fractionation Factor";
	public static final String INPUT_LABEL_Δ47 = "Δ47";

	public static final String OUTPUT_LABEL_Δ47_KLUGE = "Δ47 Adjusted for Kluge";
	public static final String OUTPUT_LABEL_Δ47_KLUGE_SD = "Δ47 Adjusted for Kluge SD";
	public static final String OUTPUT_LABEL_Δ47_KLUGE_SE = "Δ47 Adjusted for Kluge SE";
	public static final String OUTPUT_LABEL_KLUGE_MINUS_SE = "Kluge (Δ47-SE)";
	public static final String OUTPUT_LABEL_KLUGE = "Kluge (Δ47)";
	public static final String OUTPUT_LABEL_KLUGE_PLUS_SE = "Kluge (Δ47+SE)";

	public static final String OUTPUT_LABEL_Δ47_PASSEY = "Δ47 Adjusted for Passey";
	public static final String OUTPUT_LABEL_Δ47_PASSEY_SD = "Δ47 Adjusted for Passey SD";
	public static final String OUTPUT_LABEL_Δ47_PASSEY_SE = "Δ47 Adjusted for Passey SE";
	public static final String OUTPUT_LABEL_PASSEY_MINUS_SE = "Passey (Δ47-SE)";
	public static final String OUTPUT_LABEL_PASSEY = "Passey (Δ47)";
	public static final String OUTPUT_LABEL_PASSEY_PLUS_SE = "Passey (Δ47+SE)";

	public static final String OUTPUT_LABEL_Δ47_HENKES = "Δ47 Adjusted for Henkes";
	public static final String OUTPUT_LABEL_Δ47_HENKES_SD = "Δ47 Adjusted for Henkes SD";
	public static final String OUTPUT_LABEL_Δ47_HENKES_SE = "Δ47 Adjusted for Henkes SE";
	public static final String OUTPUT_LABEL_HENKES_MINUS_SE = "Henkes (Δ47-SE)";
	public static final String OUTPUT_LABEL_HENKES = "Henkes (Δ47)";
	public static final String OUTPUT_LABEL_HENKES_PLUS_SE = "Henkes (Δ47+SE)";

	public static final String OUTPUT_LABEL_Δ47_GOSH = "Δ47 Adjusted for Gosh";
	public static final String OUTPUT_LABEL_Δ47_GOSH_SD = "Δ47 Adjusted for Gosh SD";
	public static final String OUTPUT_LABEL_Δ47_GOSH_SE = "Δ47 Adjusted for Gosh SE";
	public static final String OUTPUT_LABEL_GOSH_MINUS_SE = "Gosh (Δ47-SE)";
	public static final String OUTPUT_LABEL_GOSH = "Gosh (Δ47)";
	public static final String OUTPUT_LABEL_GOSH_PLUS_SE = "Gosh (Δ47+SE)";

	public static final String OUTPUT_LABEL_Δ47_DENNIS = "Δ47 Adjusted for Dennis";
	public static final String OUTPUT_LABEL_Δ47_DENNIS_SD = "Δ47 Adjusted for Dennis SD";
	public static final String OUTPUT_LABEL_Δ47_DENNIS_SE = "Δ47 Adjusted for Dennis SE";
	public static final String OUTPUT_LABEL_DENNIS_MINUS_SE = "Dennis (Δ47-SE)";
	public static final String OUTPUT_LABEL_DENNIS = "Dennis (Δ47)";
	public static final String OUTPUT_LABEL_DENNIS_PLUS_SE = "Dennis (Δ47+SE)";

	public static final String OUTPUT_LABEL_Δ47_ZARUUR = "Δ47 Adjusted for Zaruur";
	public static final String OUTPUT_LABEL_Δ47_ZARUUR_SD = "Δ47 Adjusted for Zaruur SD";
	public static final String OUTPUT_LABEL_Δ47_ZARUUR_SE = "Δ47 Adjusted for Zaruur SE";
	public static final String OUTPUT_LABEL_ZARUUR_MINUS_SE = "Zaruur (Δ47-SE)";
	public static final String OUTPUT_LABEL_ZARUUR = "Zaruur (Δ47)";
	public static final String OUTPUT_LABEL_ZARUUR_PLUS_SE = "Zaruur (Δ47+SE)";

	public Calculator(SamStep samStep) {
		super(samStep);
	}

	@Override
	public DependencyManager getDependencyManager(SamplePad samplePad) {
		return new Dependencies();
	}

	@SuppressWarnings("unused")
	@Override
	public void calculate(SamplePad samplePad, DependencyManager dependencyManager) {
		Dependencies dependencies = (Dependencies) dependencyManager;

		boolean atLeastOneConstantChanged = false;
		atLeastOneConstantChanged = atLeastOneConstantChanged || !DoubleTools.essentiallyEqual(dependencies.getGamma().getValue(), dependencies.getGamma().getEnumeration().getDefaultValue());
		atLeastOneConstantChanged = atLeastOneConstantChanged || !DoubleTools.essentiallyEqual(dependencies.getR13VPDB().getValue(), dependencies.getR13VPDB().getEnumeration().getDefaultValue());
		atLeastOneConstantChanged = atLeastOneConstantChanged || !DoubleTools.essentiallyEqual(dependencies.getR17VSMOW().getValue(), dependencies.getR17VSMOW().getEnumeration().getDefaultValue());
		atLeastOneConstantChanged = atLeastOneConstantChanged || !DoubleTools.essentiallyEqual(dependencies.getR18VSMOW().getValue(), dependencies.getR18VSMOW().getEnumeration().getDefaultValue());
		atLeastOneConstantChanged = atLeastOneConstantChanged || !DoubleTools.essentiallyEqual(dependencies.getD18OVPDBVSMOW().getValue(), dependencies.getD18OVPDBVSMOW().getEnumeration().getDefaultValue());

		TempCalibrations[] tempCalibrations = { new Kluge(), new Passey(), new Henkes(), new Gosh(), new Dennis(), new Zaruur() };

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
				samplePad.setValue(labelToColumnName(tempCalibration.getTempMinusSELabel()), "UNDEFINED");
				samplePad.setValue(labelToColumnName(tempCalibration.getTempLabel()), "UNDEFINED");
				samplePad.setValue(labelToColumnName(tempCalibration.getTempPlusSELabel()), "UNDEFINED");
				continue;
			}

			samplePad.setAccumulator(labelToColumnName(tempCalibration.getD47AdjustedLabel()), labelToColumnName(tempCalibration.getD47AdjustedSDLabel()), labelToColumnName(tempCalibration.getD47AdjustedSELabel()), false);

			Accumulator accumulator = (Accumulator) samplePad.getValue(labelToColumnName(tempCalibration.getD47AdjustedLabel()));
			double[] meanStdDevSampleAndStdError = accumulator.getMeanStdDevSampleAndStdError();

			double mean = meanStdDevSampleAndStdError[0];
			double stdError = meanStdDevSampleAndStdError[2];

			AccumulatorStdErr accumulatorStdErr = (AccumulatorStdErr) samplePad.getValue(labelToColumnName(tempCalibration.getD47AdjustedSELabel()));

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

	private class Kluge extends TempCalibrations {
		//Kluge et al, 2015
		//AFF = 0.069
		//calibration range 25 - 250 degrees C
		//D = 0.98 * (((-3.407*10^9) / (T^4)) + ((2.365*10^7) / (T^3)) + ((-2.607*10^3) / (T^2)) + ((-5.880) / (T))) + 0.293

		Kluge() {
			atOfEquation = 90.0;
			affOfEquation = 0.069;
			d47AdjustedLabel = OUTPUT_LABEL_Δ47_KLUGE;
			d47AdjustedSdLabel = OUTPUT_LABEL_Δ47_KLUGE_SD;
			d47AdjustedSeLabel = OUTPUT_LABEL_Δ47_KLUGE_SE;
			tempMinusSeLabel = OUTPUT_LABEL_KLUGE_MINUS_SE;
			tempLabel = OUTPUT_LABEL_KLUGE;
			tempPlusSeLabel = OUTPUT_LABEL_KLUGE_PLUS_SE;
			lowValue = 195.51;
			highValue = 2944.2;
		}

		public TempEquation getTempEquation(double d47) {
			return new TempEquation(d47) {
				@Override
				public double solve(double t) {
					return 0.98 * ((-3.407e9 / Math.pow(t, 4)) + (2.365e7 / Math.pow(t, 3)) + (-2.607e3 / Math.pow(t, 2)) + (-5.880 / t)) + 0.293 - this.d47;
				}
			};
		}
	}

	private class Passey extends TempCalibrations {
		//Passey and Henkes, 2012
		//AFF = 0.081
		//D = ((-3.407*10^9) / (T^4)) + ((2.365*10^7) / (T^3)) + ((-2.607*10^3) / (T^2)) + ((-5.880) / (T)) + 0.280

		Passey() {
			atOfEquation = 90.0;
			affOfEquation = 0.081;
			d47AdjustedLabel = OUTPUT_LABEL_Δ47_PASSEY;
			d47AdjustedSdLabel = OUTPUT_LABEL_Δ47_PASSEY_SD;
			d47AdjustedSeLabel = OUTPUT_LABEL_Δ47_PASSEY_SE;
			tempMinusSeLabel = OUTPUT_LABEL_PASSEY_MINUS_SE;
			tempLabel = OUTPUT_LABEL_PASSEY;
			tempPlusSeLabel = OUTPUT_LABEL_PASSEY_PLUS_SE;
			lowValue = 195.51;
			highValue = 2944.2;
		}

		public TempEquation getTempEquation(double d47) {
			return new TempEquation(d47) {
				@Override
				public double solve(double t) {
					return (-3.407e9 / Math.pow(t, 4)) + (2.365e7 / Math.pow(t, 3)) + (-2.607e3 / Math.pow(t, 2)) + (-5.880 / t) + 0.280 - this.d47;
				}
			};
		}
	}

	private class Henkes extends TempCalibrations {
		//Henkes et al, 2013
		//AFF = 0.081
		//D = ((0.0327*10^6) / (T^2)) + 0.3286

		Henkes() {
			atOfEquation = 90.0;
			affOfEquation = 0.081;
			d47AdjustedLabel = OUTPUT_LABEL_Δ47_HENKES;
			d47AdjustedSdLabel = OUTPUT_LABEL_Δ47_HENKES_SD;
			d47AdjustedSeLabel = OUTPUT_LABEL_Δ47_HENKES_SE;
			tempMinusSeLabel = OUTPUT_LABEL_HENKES_MINUS_SE;
			tempLabel = OUTPUT_LABEL_HENKES;
			tempPlusSeLabel = OUTPUT_LABEL_HENKES_PLUS_SE;
			lowValue = 1;
			highValue = 10000;
		}

		public TempEquation getTempEquation(double d47) {
			return new TempEquation(d47) {
				@Override
				public double solve(double t) {
					return (0.0327e6 / Math.pow(t, 2)) + 0.3286 - this.d47;
				}
			};
		}
	}

	private class Gosh extends TempCalibrations {
		//Gosh et al 2006 calibration as recalibrated for the CDES scale in Dennis et al 2011
		//AFF = 0.000 (25 degrees C acidification)
		//calibration range 1 - 50 degrees C
		//WARNING: ORIGINAL HAD +- VALUES THAT HAVE BEEN REMOVED
		//D = ((0.0636*10^6) / (T^2)) - 0.0047

		Gosh() {
			atOfEquation = 25.0;
			affOfEquation = 0.000;
			d47AdjustedLabel = OUTPUT_LABEL_Δ47_GOSH;
			d47AdjustedSdLabel = OUTPUT_LABEL_Δ47_GOSH_SD;
			d47AdjustedSeLabel = OUTPUT_LABEL_Δ47_GOSH_SE;
			tempMinusSeLabel = OUTPUT_LABEL_GOSH_MINUS_SE;
			tempLabel = OUTPUT_LABEL_GOSH;
			tempPlusSeLabel = OUTPUT_LABEL_GOSH_PLUS_SE;
			lowValue = 1;
			highValue = 10000;
		}

		public TempEquation getTempEquation(double d47) {
			return new TempEquation(d47) {
				@Override
				public double solve(double t) {
					return (0.0636e6 / Math.pow(t, 2)) - 0.0047 - this.d47;
				}
			};
		}
	}

	private class Dennis extends TempCalibrations {
		//Dennis and Schrag 2010 calibration as recalibrated for the CDES scale in Dennis et al 2011
		//AFF = 0.069
		//calibration range 7.5 - 77 degrees C
		//WARNING: ORIGINAL HAD +- VALUES THAT HAVE BEEN REMOVED
		//D = ((0.0362*10^6) / (T^2)) + 0.2920

		Dennis() {
			atOfEquation = 90.0;
			affOfEquation = 0.069;
			d47AdjustedLabel = OUTPUT_LABEL_Δ47_DENNIS;
			d47AdjustedSdLabel = OUTPUT_LABEL_Δ47_DENNIS_SD;
			d47AdjustedSeLabel = OUTPUT_LABEL_Δ47_DENNIS_SE;
			tempMinusSeLabel = OUTPUT_LABEL_DENNIS_MINUS_SE;
			tempLabel = OUTPUT_LABEL_DENNIS;
			tempPlusSeLabel = OUTPUT_LABEL_DENNIS_PLUS_SE;
			lowValue = 1;
			highValue = 10000;
		}

		public TempEquation getTempEquation(double d47) {
			return new TempEquation(d47) {
				@Override
				public double solve(double t) {
					return (0.0362e6 / Math.pow(t, 2)) + 0.2920 - this.d47;
				}
			};
		}
	}
	
	private class Zaruur extends TempCalibrations {
		//Zaruur et al 2013
		//AFF = 0.000 (25 degrees C acidification)
		//WARNING: ORIGINAL HAD +- VALUES THAT HAVE BEEN REMOVED
		//D = ((0.0526*10^6) / (T^2)) + 0.0520

		Zaruur() {
			atOfEquation = 25.0;
			affOfEquation = 0.000;
			d47AdjustedLabel = OUTPUT_LABEL_Δ47_ZARUUR;
			d47AdjustedSdLabel = OUTPUT_LABEL_Δ47_ZARUUR_SD;
			d47AdjustedSeLabel = OUTPUT_LABEL_Δ47_ZARUUR_SE;
			tempMinusSeLabel = OUTPUT_LABEL_ZARUUR_MINUS_SE;
			tempLabel = OUTPUT_LABEL_ZARUUR;
			tempPlusSeLabel = OUTPUT_LABEL_ZARUUR_PLUS_SE;
			lowValue = 1;
			highValue = 10000;
		}

		public TempEquation getTempEquation(double d47) {
			return new TempEquation(d47) {
				@Override
				public double solve(double t) {
					return (0.0526e6 / Math.pow(t, 2)) + 0.0520 - this.d47;
				}
			};
		}
	}
}
