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

package org.easotope.shared.analysis.repstep.co2.ethpbl;

import java.util.HashMap;

import org.easotope.shared.Messages;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ReplicatePad.ReplicateType;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.InputParameterType;

public class Calculator extends RepStepCalculator {
	public static final String INPUT_LABEL_DISABLED = "Disabled";
	public static final String INPUT_LABEL_OFF_PEAK = "Off Peak";
	public static final String INPUT_LABEL_V44_REF = "V44 Reference";
	public static final String INPUT_LABEL_V44_SAMPLE = "V44 Sample";
	public static final String INPUT_LABEL_V44_BACKGROUND = "V44 Background";
	public static final String INPUT_LABEL_V44_ALGORITHM = "V44 Correction Algorithm";
	public static final String INPUT_LABEL_V44_REFMZX10 = "V44 Correction Reference m/z";
	public static final String INPUT_LABEL_V44_X2COEFF = "V44 Scan Regression X2 Coefficient";
	public static final String INPUT_LABEL_V44_X1COEFF = "V44 Scan Regression X1 Coefficient";
	public static final String INPUT_LABEL_V44_X0COEFF = "V44 Scan Regression X0 Coefficient";
	public static final String INPUT_LABEL_V44_FACTOR = "V44 Correction factor";
	public static final String INPUT_LABEL_V45_REF = "V45 Reference";
	public static final String INPUT_LABEL_V45_SAMPLE = "V45 Sample";
	public static final String INPUT_LABEL_V45_BACKGROUND = "V45 Background";
	public static final String INPUT_LABEL_V45_ALGORITHM = "V45 Correction Algorithm";
	public static final String INPUT_LABEL_V45_REFMZX10 = "V45 Correction Reference m/z";
	public static final String INPUT_LABEL_V45_X2COEFF = "V45 Scan Regression X2 Coefficient";
	public static final String INPUT_LABEL_V45_X1COEFF = "V45 Scan Regression X1 Coefficient";
	public static final String INPUT_LABEL_V45_X0COEFF = "V45 Scan Regression X0 Coefficient";
	public static final String INPUT_LABEL_V45_FACTOR = "V45 Correction factor";
	public static final String INPUT_LABEL_V46_REF = "V46 Reference";
	public static final String INPUT_LABEL_V46_SAMPLE = "V46 Sample";
	public static final String INPUT_LABEL_V46_BACKGROUND = "V46 Background";
	public static final String INPUT_LABEL_V46_ALGORITHM = "V46 Correction Algorithm";
	public static final String INPUT_LABEL_V46_REFMZX10 = "V46 Correction Reference m/z";
	public static final String INPUT_LABEL_V46_X2COEFF = "V46 Scan Regression X2 Coefficient";
	public static final String INPUT_LABEL_V46_X1COEFF = "V46 Scan Regression X1 Coefficient";
	public static final String INPUT_LABEL_V46_X0COEFF = "V46 Scan Regression X0 Coefficient";
	public static final String INPUT_LABEL_V46_FACTOR = "V46 Correction factor";
	public static final String INPUT_LABEL_V47_REF = "V47 Reference";
	public static final String INPUT_LABEL_V47_SAMPLE = "V47 Sample";
	public static final String INPUT_LABEL_V47_BACKGROUND = "V47 Background";
	public static final String INPUT_LABEL_V47_ALGORITHM = "V47 Correction Algorithm";
	public static final String INPUT_LABEL_V47_REFMZX10 = "V47 Correction Reference m/z";
	public static final String INPUT_LABEL_V47_X2COEFF = "V47 Scan Regression X2 Coefficient";
	public static final String INPUT_LABEL_V47_X1COEFF = "V47 Scan Regression X1 Coefficient";
	public static final String INPUT_LABEL_V47_X0COEFF = "V47 Scan Regression X0 Coefficient";
	public static final String INPUT_LABEL_V47_FACTOR = "V47 Correction factor";
	public static final String INPUT_LABEL_V48_REF = "V48 Reference";
	public static final String INPUT_LABEL_V48_SAMPLE = "V48 Sample";
	public static final String INPUT_LABEL_V48_BACKGROUND = "V48 Background";
	public static final String INPUT_LABEL_V48_ALGORITHM = "V48 Correction Algorithm";
	public static final String INPUT_LABEL_V48_REFMZX10 = "V48 Correction Reference m/z";
	public static final String INPUT_LABEL_V48_X2COEFF = "V48 Scan Regression X2 Coefficient";
	public static final String INPUT_LABEL_V48_X1COEFF = "V48 Scan Regression X1 Coefficient";
	public static final String INPUT_LABEL_V48_X0COEFF = "V48 Scan Regression X0 Coefficient";
	public static final String INPUT_LABEL_V48_FACTOR = "V48 Correction factor";
	public static final String INPUT_LABEL_V49_REF = "V49 Reference";
	public static final String INPUT_LABEL_V49_SAMPLE = "V49 Sample";
	public static final String INPUT_LABEL_V49_BACKGROUND = "V49 Background";
	public static final String INPUT_LABEL_V49_ALGORITHM = "V49 Correction Algorithm";
	public static final String INPUT_LABEL_V49_REFMZX10 = "V49 Correction Reference m/z";
	public static final String INPUT_LABEL_V49_X2COEFF = "V49 Scan Regression X2 Coefficient";
	public static final String INPUT_LABEL_V49_X1COEFF = "V49 Scan Regression X1 Coefficient";
	public static final String INPUT_LABEL_V49_X0COEFF = "V49 Scan Regression X0 Coefficient";
	public static final String INPUT_LABEL_V49_FACTOR = "V49 Correction factor";

	public static final String OUTPUT_LABEL_V44_REF_PBL = "V44 Reference PBL Adjusted";
	public static final String OUTPUT_LABEL_V44_SAMPLE_PBL = "V44 Sample PBL Adjusted";
	public static final String OUTPUT_LABEL_V45_REF_PBL = "V45 Reference PBL Adjusted";
	public static final String OUTPUT_LABEL_V45_SAMPLE_PBL = "V45 Sample PBL Adjusted";
	public static final String OUTPUT_LABEL_V46_REF_PBL = "V46 Reference PBL Adjusted";
	public static final String OUTPUT_LABEL_V46_SAMPLE_PBL = "V46 Sample PBL Adjusted";
	public static final String OUTPUT_LABEL_V47_REF_PBL = "V47 Reference PBL Adjusted";
	public static final String OUTPUT_LABEL_V47_SAMPLE_PBL = "V47 Sample PBL Adjusted";
	public static final String OUTPUT_LABEL_V48_REF_PBL = "V48 Reference PBL Adjusted";
	public static final String OUTPUT_LABEL_V48_SAMPLE_PBL = "V48 Sample PBL Adjusted";
	public static final String OUTPUT_LABEL_V49_REF_PBL = "V49 Reference PBL Adjusted";
	public static final String OUTPUT_LABEL_V49_SAMPLE_PBL = "V49 Sample PBL Adjusted";

	private static final String[] inputLabelRefs = new String[] { INPUT_LABEL_V44_REF, INPUT_LABEL_V45_REF, INPUT_LABEL_V46_REF, INPUT_LABEL_V47_REF, INPUT_LABEL_V48_REF, INPUT_LABEL_V49_REF };
	private static final String[] outputLabelRefs = new String[] { OUTPUT_LABEL_V44_REF_PBL, OUTPUT_LABEL_V45_REF_PBL, OUTPUT_LABEL_V46_REF_PBL, OUTPUT_LABEL_V47_REF_PBL, OUTPUT_LABEL_V48_REF_PBL, OUTPUT_LABEL_V49_REF_PBL };
	private static final String[] inputLabelSamples = new String[] { INPUT_LABEL_V44_SAMPLE, INPUT_LABEL_V45_SAMPLE, INPUT_LABEL_V46_SAMPLE, INPUT_LABEL_V47_SAMPLE, INPUT_LABEL_V48_SAMPLE, INPUT_LABEL_V49_SAMPLE };
	private static final String[] outputLabelSamples = new String[] { OUTPUT_LABEL_V44_SAMPLE_PBL, OUTPUT_LABEL_V45_SAMPLE_PBL, OUTPUT_LABEL_V46_SAMPLE_PBL, OUTPUT_LABEL_V47_SAMPLE_PBL, OUTPUT_LABEL_V48_SAMPLE_PBL, OUTPUT_LABEL_V49_SAMPLE_PBL };
	private static final String[] inputLabelBackgrounds = new String[] { INPUT_LABEL_V44_BACKGROUND, INPUT_LABEL_V45_BACKGROUND, INPUT_LABEL_V46_BACKGROUND, INPUT_LABEL_V47_BACKGROUND, INPUT_LABEL_V48_BACKGROUND, INPUT_LABEL_V49_BACKGROUND };
	private static final String[] inputLabelAlgorithms = new String[] { INPUT_LABEL_V44_ALGORITHM, INPUT_LABEL_V45_ALGORITHM, INPUT_LABEL_V46_ALGORITHM, INPUT_LABEL_V47_ALGORITHM, INPUT_LABEL_V48_ALGORITHM, INPUT_LABEL_V49_ALGORITHM };
	private static final String[] inputLabelRefMzX10s = new String[] { INPUT_LABEL_V44_REFMZX10, INPUT_LABEL_V45_REFMZX10, INPUT_LABEL_V46_REFMZX10, INPUT_LABEL_V47_REFMZX10, INPUT_LABEL_V48_REFMZX10, INPUT_LABEL_V49_REFMZX10 };
	private static final String[] inputLabelX2Coeff = new String[] { INPUT_LABEL_V44_X2COEFF, INPUT_LABEL_V45_X2COEFF, INPUT_LABEL_V46_X2COEFF, INPUT_LABEL_V47_X2COEFF, INPUT_LABEL_V48_X2COEFF, INPUT_LABEL_V49_X2COEFF };
	private static final String[] inputLabelX1Coeff = new String[] { INPUT_LABEL_V44_X1COEFF, INPUT_LABEL_V45_X1COEFF, INPUT_LABEL_V46_X1COEFF, INPUT_LABEL_V47_X1COEFF, INPUT_LABEL_V48_X1COEFF, INPUT_LABEL_V49_X1COEFF };
	private static final String[] inputLabelX0Coeff = new String[] { INPUT_LABEL_V44_X0COEFF, INPUT_LABEL_V45_X0COEFF, INPUT_LABEL_V46_X0COEFF, INPUT_LABEL_V47_X0COEFF, INPUT_LABEL_V48_X0COEFF, INPUT_LABEL_V49_X0COEFF };
	private static final String[] inputLabelFactors = new String[] { INPUT_LABEL_V44_FACTOR, INPUT_LABEL_V45_FACTOR, INPUT_LABEL_V46_FACTOR, INPUT_LABEL_V47_FACTOR, INPUT_LABEL_V48_FACTOR, INPUT_LABEL_V49_FACTOR };

	private static HashMap<Integer,String> mzX10ToSampleMeasurement = new HashMap<Integer,String>();
	private static HashMap<Integer,String> mzX10ToRefMeasurement = new HashMap<Integer,String>();
	private static HashMap<Integer,String> mzX10ToBackground = new HashMap<Integer,String>();

	public static String getVolatileDataScanReplicatePadKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_SCAN_REPLICATE_PAD";
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
		ReplicatePad scanFileBefore = null;

		for (int i=padNumber; i>=0; i--) {
			if (replicatePads[i].getReplicateType() == ReplicateType.SCAN && !isTrue(replicatePads[i], INPUT_LABEL_DISABLED)) {
				scanFileBefore = replicatePads[i];
				break;
			}
		}

		ReplicatePad scanFileAfter = null;

		for (int i=padNumber; i<replicatePads.length; i++) {
			if (replicatePads[i].getReplicateType() == ReplicateType.SCAN && !isTrue(replicatePads[i], INPUT_LABEL_DISABLED)) {
				scanFileAfter = replicatePads[i];
				break;
			}
		}

		ReplicatePad scanFileReplicatePad = null;

		if (scanFileBefore == null && scanFileAfter == null) {
			throw new RuntimeException(Messages.co2EthPblCalculator_noScanFileFound);

		} else if (scanFileBefore == null) {
			scanFileReplicatePad = scanFileAfter;

		} else if (scanFileAfter == null) {
			scanFileReplicatePad = scanFileBefore;

		} else if (Math.abs(scanFileBefore.getDate()-replicatePads[padNumber].getDate()) > Math.abs(scanFileAfter.getDate()-replicatePads[padNumber].getDate())) {
			scanFileReplicatePad = scanFileAfter;

		} else {
			scanFileReplicatePad = scanFileBefore;
		}

		replicatePads[padNumber].setVolatileData(getVolatileDataScanReplicatePadKey(), scanFileReplicatePad);

		for (AcquisitionPad acquisitionPad : replicatePads[padNumber].getChildren()) {
			if (!isTrue(acquisitionPad, INPUT_LABEL_DISABLED)) {
				calculateAcquisition(acquisitionPad, scanFileReplicatePad);
			}
		}
	}

	private void calculateAcquisition(AcquisitionPad acquisitionPad, ReplicatePad scanFileReplicatePad) {
		for (CyclePad cyclePad : acquisitionPad.getChildren()) {
			if (!isTrue(cyclePad, INPUT_LABEL_OFF_PEAK)) {
				adjustCycle(cyclePad, scanFileReplicatePad, inputLabelSamples, outputLabelSamples, mzX10ToSampleMeasurement);
				adjustCycle(cyclePad, scanFileReplicatePad, inputLabelRefs, outputLabelRefs, mzX10ToRefMeasurement);
			}
		}
	}

	private void adjustCycle(CyclePad cyclePad, ReplicatePad scanFileReplicatePad, String[] inputLabels, String[] outputLabels, HashMap<Integer,String> mzX10ToMeasurement) {
		AcquisitionPad acquisitionPad = (AcquisitionPad) cyclePad.getParent();

		for (int i=0; i<inputLabels.length; i++) {
			Double value = (Double) cyclePad.getValue(labelToColumnName(inputLabels[i]));

			if (value != null) {
				int algorithm = (Integer) scanFileReplicatePad.getValue(labelToColumnName(inputLabelAlgorithms[i]));

				if (algorithm == 0) {
					cyclePad.setValue(labelToColumnName(outputLabels[i]), value);

				} else if (algorithm == 1) {
					Integer refMzX10 = (Integer) scanFileReplicatePad.getValue(labelToColumnName(inputLabelRefMzX10s[i]));
					Double x2Coeff = (Double) scanFileReplicatePad.getValue(labelToColumnName(inputLabelX2Coeff[i]));
					Double x1Coeff = (Double) scanFileReplicatePad.getValue(labelToColumnName(inputLabelX1Coeff[i]));
					Double x0Coeff = (Double) scanFileReplicatePad.getValue(labelToColumnName(inputLabelX0Coeff[i]));
					Double refValue = (Double) cyclePad.getValue(mzX10ToMeasurement.get(refMzX10));

					if (x2Coeff == null || Double.isNaN(x2Coeff) || x1Coeff == null || Double.isNaN(x1Coeff) || x0Coeff == null || Double.isNaN(x0Coeff) || refValue == null) {
						cyclePad.setValue(labelToColumnName(outputLabels[i]), value);

					} else {
						Double background = (Double) acquisitionPad.getValue(labelToColumnName(inputLabelBackgrounds[i]));

						if (background != null) {
							value += background;
						}

						Double refBackground = (Double) acquisitionPad.getValue(mzX10ToBackground.get(refMzX10));

						if (refBackground != null) {
							refValue += refBackground;
						}

						double newValue = value - (x2Coeff * refValue * refValue + x1Coeff * refValue + x0Coeff);
						cyclePad.setValue(labelToColumnName(outputLabels[i]), newValue);
					}

				} else if (algorithm == 2) {
					Integer refMzX10 = (Integer) scanFileReplicatePad.getValue(labelToColumnName(inputLabelRefMzX10s[i]));
					Double factor = (Double) scanFileReplicatePad.getValue(labelToColumnName(inputLabelFactors[i]));
					Double refValue = (Double) cyclePad.getValue(mzX10ToMeasurement.get(refMzX10));

					if (factor == null || Double.isNaN(factor) || refValue == null) {
						cyclePad.setValue(labelToColumnName(outputLabels[i]), value);

					} else {
						Double background = (Double) acquisitionPad.getValue(labelToColumnName(inputLabelBackgrounds[i]));

						if (background != null) {
							value += background;
						}

						Double refBackground = (Double) acquisitionPad.getValue(mzX10ToBackground.get(refMzX10));

						if (refBackground != null) {
							refValue += refBackground;
						}

						double newValue = value + factor * refValue;
						cyclePad.setValue(labelToColumnName(outputLabels[i]), newValue);
					}
				}
			}
		}
	}

	static {
		for (InputParameter inputParameter : InputParameter.values()) {
			if (inputParameter.getInputParameterType() == InputParameterType.SampleMeasurement) {
				mzX10ToSampleMeasurement.put(inputParameter.getMzX10(), inputParameter.toString());
			}

			if (inputParameter.getInputParameterType() == InputParameterType.RefMeasurement) {
				mzX10ToRefMeasurement.put(inputParameter.getMzX10(), inputParameter.toString());
			}

			if (inputParameter.getInputParameterType() == InputParameterType.Background) {
				mzX10ToBackground.put(inputParameter.getMzX10(), inputParameter.toString());
			}
		}
	}
}
