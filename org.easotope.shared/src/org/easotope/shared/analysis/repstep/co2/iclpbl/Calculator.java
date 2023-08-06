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

package org.easotope.shared.analysis.repstep.co2.iclpbl;

import java.util.ArrayList;

import org.easotope.shared.Messages;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.math.LinearRegression;

public class Calculator extends RepStepCalculator {
	public static final String INPUT_LABEL_DISABLED = "Disabled";
	public static final String INPUT_LABEL_OFF_PEAK = "Off Peak";
	public static final String INPUT_LABEL_V44_REF = "V44 Reference";
	public static final String INPUT_LABEL_V44_SAMPLE = "V44 Sample";
	public static final String INPUT_LABEL_V44_BACKGROUND = "V44 Background";
	public static final String INPUT_LABEL_V45_REF = "V45 Reference";
	public static final String INPUT_LABEL_V45_SAMPLE = "V45 Sample";
	public static final String INPUT_LABEL_V45_BACKGROUND = "V45 Background";
	public static final String INPUT_LABEL_V46_REF = "V46 Reference";
	public static final String INPUT_LABEL_V46_SAMPLE = "V46 Sample";
	public static final String INPUT_LABEL_V46_BACKGROUND = "V46 Background";
	public static final String INPUT_LABEL_V47_REF = "V47 Reference";
	public static final String INPUT_LABEL_V47_SAMPLE = "V47 Sample";
	public static final String INPUT_LABEL_V47_BACKGROUND = "V47 Background";
	public static final String INPUT_LABEL_V48_REF = "V48 Reference";
	public static final String INPUT_LABEL_V48_SAMPLE = "V48 Sample";
	public static final String INPUT_LABEL_V48_BACKGROUND = "V48 Background";
	public static final String INPUT_LABEL_V49_REF = "V49 Reference";
	public static final String INPUT_LABEL_V49_SAMPLE = "V49 Sample";
	public static final String INPUT_LABEL_V49_BACKGROUND = "V49 Background";

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

	private String[] inputLabelRefs = new String[] { INPUT_LABEL_V44_REF, INPUT_LABEL_V45_REF, INPUT_LABEL_V46_REF, INPUT_LABEL_V47_REF, INPUT_LABEL_V48_REF, INPUT_LABEL_V49_REF };
	private String[] outputLabelRefs = new String[] { OUTPUT_LABEL_V44_REF_PBL, OUTPUT_LABEL_V45_REF_PBL, OUTPUT_LABEL_V46_REF_PBL, OUTPUT_LABEL_V47_REF_PBL, OUTPUT_LABEL_V48_REF_PBL, OUTPUT_LABEL_V49_REF_PBL };
	private String[] inputLabelSamples = new String[] { INPUT_LABEL_V44_SAMPLE, INPUT_LABEL_V45_SAMPLE, INPUT_LABEL_V46_SAMPLE, INPUT_LABEL_V47_SAMPLE, INPUT_LABEL_V48_SAMPLE, INPUT_LABEL_V49_SAMPLE };
	private String[] outputLabelSamples = new String[] { OUTPUT_LABEL_V44_SAMPLE_PBL, OUTPUT_LABEL_V45_SAMPLE_PBL, OUTPUT_LABEL_V46_SAMPLE_PBL, OUTPUT_LABEL_V47_SAMPLE_PBL, OUTPUT_LABEL_V48_SAMPLE_PBL, OUTPUT_LABEL_V49_SAMPLE_PBL };
	private String[] inputLabelBackgrounds = new String[] { INPUT_LABEL_V44_BACKGROUND, INPUT_LABEL_V45_BACKGROUND, INPUT_LABEL_V46_BACKGROUND, INPUT_LABEL_V47_BACKGROUND, INPUT_LABEL_V48_BACKGROUND, INPUT_LABEL_V49_BACKGROUND };

	public static String getVolatileDataLinearRegressionKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_LINEAR_REGRESSION";
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
		ArrayList<AcquisitionPad> acquisitions = replicatePads[padNumber].getChildren();

		boolean atLeastOneRegression = false;

		for (AcquisitionPad acquisitionPad : acquisitions) {
			if (!isTrue(acquisitionPad, INPUT_LABEL_DISABLED)) {
				atLeastOneRegression = generateRegression(acquisitionPad) || atLeastOneRegression;
			}
		}

		if (!atLeastOneRegression) {
			throw new RuntimeException(Messages.co2PblCalculator_insufficientOffPeakData);
		}

		int acquisitionCount = 1;
		for (AcquisitionPad acquisitionPad : acquisitions) {
			LinearRegression[] closestRegressions = findClosestRegressions(acquisitions, acquisitionCount);

			if (!isTrue(acquisitionPad, INPUT_LABEL_DISABLED)) {
				calculateAcquisition(acquisitionPad, acquisitionCount, closestRegressions);
			}

			acquisitionCount++;
		}
	}

	private boolean generateRegression(AcquisitionPad acquisitionPad) {
		LinearRegression[] linearRegressions = new LinearRegression[inputLabelBackgrounds.length];

		for (int i=0; i<linearRegressions.length; i++) {
			linearRegressions[i] = new LinearRegression();
		}

		int nextNextCyclePadIndex = 0;
		ArrayList<CyclePad> cyclePads = acquisitionPad.getChildren();

		CyclePad previousCyclePad = null;
		CyclePad thisCyclePad = cyclePads.size() > nextNextCyclePadIndex ? cyclePads.get(nextNextCyclePadIndex++) : null;
		CyclePad nextCyclePad = cyclePads.size() > nextNextCyclePadIndex ? cyclePads.get(nextNextCyclePadIndex++) : null;

		while (thisCyclePad != null) {
			if (isTrue(thisCyclePad, INPUT_LABEL_OFF_PEAK)) {
				addCycleToRegression(linearRegressions, acquisitionPad, previousCyclePad, thisCyclePad, nextCyclePad);
			}

			previousCyclePad = thisCyclePad;
			thisCyclePad = nextCyclePad;
			nextCyclePad = cyclePads.size() > nextNextCyclePadIndex ? cyclePads.get(nextNextCyclePadIndex++) : null;
		}

		for (LinearRegression linearRegression : linearRegressions) {
			if (linearRegression.isInvalid()) {
				return false;
			}
		}

		acquisitionPad.setVolatileData(getVolatileDataLinearRegressionKey(), linearRegressions);
		return true;
	}

	private void addCycleToRegression(LinearRegression[] linearRegression, AcquisitionPad acquisitionPad, CyclePad previousCyclePad, CyclePad thisCyclePad, CyclePad nextCyclePad) {
		Double interpolatedSample44 = interpolatePrevNext(INPUT_LABEL_V44_SAMPLE, previousCyclePad, nextCyclePad);

		if (interpolatedSample44 != null) {
			Double v44Background = (Double) acquisitionPad.getValue(labelToColumnName(INPUT_LABEL_V44_BACKGROUND));

			if (v44Background != null) {
				interpolatedSample44 += v44Background;
			}

			for (int i=0; i<linearRegression.length; i++) {
				Double sample = (Double) thisCyclePad.getValue(labelToColumnName(inputLabelSamples[i]));

				if (sample != null) {
					linearRegression[i].addCoordinate(interpolatedSample44, sample);
				}
			}
		}

		Double interpolatedRef44 = interpolatePrevNext(INPUT_LABEL_V44_REF, previousCyclePad, nextCyclePad);

		if (interpolatedRef44 != null) {
			Double v44Background = (Double) acquisitionPad.getValue(labelToColumnName(INPUT_LABEL_V44_BACKGROUND));

			if (v44Background != null) {
				interpolatedRef44 += v44Background;
			}

			for (int i=0; i<linearRegression.length; i++) {
				Double sample = (Double) thisCyclePad.getValue(labelToColumnName(inputLabelRefs[i]));

				if (sample != null) {
					linearRegression[i].addCoordinate(interpolatedRef44, sample);
				}
			}
		}
	}

	private Double interpolatePrevNext(String label, CyclePad previousCyclePad, CyclePad nextCyclePad) {
		int numFound = 0;
		double accumulator = 0.0;

		if (previousCyclePad != null) {
			if (!isTrue(previousCyclePad, INPUT_LABEL_OFF_PEAK)) {
				Double value = (Double) previousCyclePad.getValue(labelToColumnName(label));

				if (value != null) {
					accumulator += value;
					numFound++;
				}
			}
		}

		if (nextCyclePad != null) {
			if (!isTrue(nextCyclePad, INPUT_LABEL_OFF_PEAK)) {
				Double value = (Double) nextCyclePad.getValue(labelToColumnName(label));
	
				if (value != null) {
					accumulator += value;
					numFound++;
				}
			}
		}

		if (numFound == 0) {
			return null;
		}

		return accumulator / numFound;
	}

	private LinearRegression[] findClosestRegressions(ArrayList<AcquisitionPad> acquisitions, int desiredAcquisitionCount) {
		LinearRegression[] result = null;
		int distanceToResult = Integer.MAX_VALUE;

		int thisAcquisitionCount = 1;
		for (AcquisitionPad acquisitionPad : acquisitions) {
			LinearRegression[] thisLinearRegression = (LinearRegression[]) acquisitionPad.getVolatileData(getVolatileDataLinearRegressionKey());

			if (thisLinearRegression != null && Math.abs(thisAcquisitionCount - desiredAcquisitionCount) < distanceToResult) {
				result = thisLinearRegression;
				distanceToResult = Math.abs(thisAcquisitionCount - desiredAcquisitionCount);
			}

			thisAcquisitionCount++;
		}

		return result;
	}

	private void calculateAcquisition(AcquisitionPad acquisitionPad, int acquisitionNumber, LinearRegression[] closestRegression) {
		Double background44 = (Double) acquisitionPad.getValue(labelToColumnName(INPUT_LABEL_V44_BACKGROUND));

		for (CyclePad cyclePad : acquisitionPad.getChildren()) {
			if (!isTrue(cyclePad, INPUT_LABEL_OFF_PEAK)) {
				// first with sample data

				Double sample44 = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V44_SAMPLE));

				if (sample44 != null) {
					if (background44 != null) {
						sample44 += background44;
					}

					for (int i=0; i<inputLabelSamples.length; i++) {
						Double value = (Double) cyclePad.getValue(labelToColumnName(inputLabelSamples[i]));

						if (value != null) {
							Double background = (Double) acquisitionPad.getValue(labelToColumnName(inputLabelBackgrounds[i]));

							if (background != null) {
								value += background;
							}

							double newValue = value - (sample44 * closestRegression[i].getSlope() + closestRegression[i].getIntercept());
							cyclePad.setValue(labelToColumnName(outputLabelSamples[i]), newValue);
						}
					}
				}

				// then with reference data

				Double ref44 = (Double) cyclePad.getValue(labelToColumnName(INPUT_LABEL_V44_REF));

				if (ref44 != null) {
					if (background44 != null) {
						ref44 += background44;
					}

					for (int i=0; i<inputLabelRefs.length; i++) {
						Double value = (Double) cyclePad.getValue(labelToColumnName(inputLabelRefs[i]));

						if (value != null) {
							Double background = (Double) acquisitionPad.getValue(labelToColumnName(inputLabelBackgrounds[i]));

							if (background != null) {
								value += background;
							}

							double newValue = value - (ref44 * closestRegression[i].getSlope() + closestRegression[i].getIntercept());
							cyclePad.setValue(labelToColumnName(outputLabelRefs[i]), newValue);
						}
					}
				}
			}
		}
	}
}
