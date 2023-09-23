/*
 * Copyright © 2019-2020 by Cédric John.
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

package org.carbonateresearch.shared.analysis.repstep.co2.ethpbl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
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

	public static final String PARAMETER_CORRECTION_TYPE = "PARAMETER_CORRECTION_TYPE";
	public static final String PARAMETER_MIN_NUM_SCANS_BEFORE_AFTER = "PARAMETER_MIN_NUM_SCANS_BEFORE_AFTER";
	public static final int DEFAULT_CORRECTION_TYPE = PBLCorrectionType.NearestScan.ordinal();
	public static final int DEFAULT_MIN_NUM_SCANS_BEFORE_AFTER = 2;

	public enum BackgroundPointType { ThisReplicate, BackgroundPoint, MovingAverage };
	public enum PBLCorrectionType { NearestScan, MovingAverage, Interpolate };

	private Hashtable<String,Object> largestCycle = new Hashtable<String,Object>();
	private HashMap<String,Double> largestBackground = new HashMap<String,Double>();

	private Hashtable<String, Object> getLargestCycle() {	
		return largestCycle;
	}

	private void setLargestCycle(CyclePad cyclePad, String largestLabel, HashMap<Integer,String> mz10ToMeasurement) {
		this.largestCycle.put("cycle pad", cyclePad);
		this.largestCycle.put("label", largestLabel);
		this.largestCycle.put("mz10ToMeasurment", mz10ToMeasurement);
	}

	private HashMap<String,Double> getLargestBackground() {
		return largestBackground;
	}

	private void setLargestBackground(Double largestBackground, int channel) {
		this.largestBackground.put(inputLabelBackgrounds[channel],largestBackground);
	}

	public static String getVolatileDataAllBackgroundsKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_ALL_BACKGROUNDS";
	}
	
	public static String getVolatileDataAverageBackgroundsKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_AVERAGE_BACKGROUNDS";
	}
	
	public static String getVolatileDataThisReplicateBackgroundsKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_REPLICATE_BACKGROUNDS";
	}
	
	public static String getVolatileDataMaximumCycle() {
		return Calculator.class.getName() + "VOLATILE_DATA_MAXIMUM_CYCLE";
	}

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	public PBLCorrectionType getCorrectionType() {
		Integer parameter = (Integer) getParameter(PARAMETER_CORRECTION_TYPE);
		return parameter == null ? PBLCorrectionType.values()[DEFAULT_CORRECTION_TYPE] : PBLCorrectionType.values()[parameter];
	}

	public int getMinNumScansBeforeAfter() {
		Integer parameter = (Integer) getParameter(PARAMETER_MIN_NUM_SCANS_BEFORE_AFTER);
		return parameter == null ? DEFAULT_MIN_NUM_SCANS_BEFORE_AFTER : parameter;
	}

	@Override
	public DependencyManager getDependencyManager(ReplicatePad[] replicatePads, int standardNumber) {
		return null;
	}

	@Override
	public void calculate(ReplicatePad[] replicatePads, int padNumber, DependencyManager dependencyManager) {	

		ArrayList<ReplicatePad> scanFiles = new ArrayList<ReplicatePad>();
		int nbScansSelected=0;
		ReplicatePad scanFileBefore = null;
		ReplicatePad scanFileAfter = null;

		for (int i=padNumber; i>=0; i--) {
			if (replicatePads[i].getReplicateType() == ReplicateType.SCAN && !isTrue(replicatePads[i], INPUT_LABEL_DISABLED)) {
				if (nbScansSelected==0 && getCorrectionType()==PBLCorrectionType.NearestScan) {
					scanFileBefore = replicatePads[i];
				}
				if (getCorrectionType()!=PBLCorrectionType.NearestScan) {
					scanFiles.add(replicatePads[i]);
				}
				nbScansSelected++;
				if (nbScansSelected==getMinNumScansBeforeAfter() || getCorrectionType()==PBLCorrectionType.NearestScan || getCorrectionType()==PBLCorrectionType.Interpolate) {
					break;
				}
			}
		}

		nbScansSelected=0;
		
		for (int i=padNumber; i<replicatePads.length; i++) {
			if (replicatePads[i].getReplicateType() == ReplicateType.SCAN && !isTrue(replicatePads[i], INPUT_LABEL_DISABLED)) {
				if (nbScansSelected==0 && getCorrectionType()==PBLCorrectionType.NearestScan) {
					scanFileAfter = replicatePads[i];					
				}
				if (getCorrectionType()!=PBLCorrectionType.NearestScan) {
					scanFiles.add(replicatePads[i]);
				}
				nbScansSelected++;
				if (nbScansSelected==getMinNumScansBeforeAfter() || getCorrectionType()==PBLCorrectionType.NearestScan || getCorrectionType()==PBLCorrectionType.Interpolate) {
					break;
				}
			}
		}

		// now determine whether to add the scan before or the scan after to the scan pad for the nearest scan algorithm
		if (getCorrectionType() == PBLCorrectionType.NearestScan) {
			if (scanFileBefore == null && scanFileAfter == null) {
				throw new RuntimeException(Messages.co2EthPblCalculator_noScanFileFound);
			}
			else if (scanFileBefore == null && scanFileAfter !=null) {
				scanFiles.add(scanFileAfter);
			} 
			else if (scanFileAfter == null && scanFileBefore != null){
				scanFiles.add(scanFileBefore);
			}
			else if (scanFileBefore != null && scanFileAfter !=null){
				
				if (Math.abs(scanFileBefore.getDate() - replicatePads[padNumber].getDate()) > Math.abs(scanFileAfter.getDate() - replicatePads[padNumber].getDate())) {
					scanFiles.add(scanFileAfter);
				} else {
					scanFiles.add(scanFileBefore);
				}
			}
		}
		
		Collections.sort(scanFiles);
		
		for (AcquisitionPad acquisitionPad : replicatePads[padNumber].getChildren()) {
			if (!isTrue(acquisitionPad, INPUT_LABEL_DISABLED)) {
				calculateAcquisition(acquisitionPad, scanFiles);
			}
		}
		
		// The correction is done for this channel, now calculate a background for this channel using the largest cycle; this is used for the graphics.
		
		CyclePad largestCyclePad=(CyclePad) getLargestCycle().get("cycle pad");
		String label=(String) getLargestCycle().get("label");
		@SuppressWarnings("unchecked")
		HashMap<Integer,String> mz10ToMeasurement = (HashMap<Integer,String>) getLargestCycle().get("mz10ToMeasurment");
		ArrayList<BackgroundPoint> backgroundPointThisRep = new ArrayList<BackgroundPoint>();
		
		for (int channel=0; channel<inputLabelX2Coeff.length; channel++) {
			Double backgroundThisRepThisChannel = calculateBackground(largestCyclePad,channel,scanFiles,getCorrectionType(),label,mz10ToMeasurement);
			backgroundPointThisRep.add(new BackgroundPoint(replicatePads[padNumber].getDate(), backgroundThisRepThisChannel, -1, -1, true,  BackgroundPointType.BackgroundPoint, replicatePads[padNumber].getReplicateId()));
		}

		// After all of the channels are cycled through, put the replicate background in the volatile and calculate the scan backgrounds for the volatiles
		replicatePads[padNumber].setVolatileData(getVolatileDataThisReplicateBackgroundsKey(), backgroundPointThisRep);
		calculateVolatileScanData(replicatePads,padNumber,dependencyManager);
	}

	private void calculateAcquisition(AcquisitionPad acquisitionPad, ArrayList<ReplicatePad> scanFileReplicatePad) {
		for (CyclePad cyclePad : acquisitionPad.getChildren()) {
			if (!isTrue(cyclePad, INPUT_LABEL_OFF_PEAK)) {
				adjustCycle(cyclePad, scanFileReplicatePad, inputLabelSamples, outputLabelSamples, mzX10ToSampleMeasurement);
				adjustCycle(cyclePad, scanFileReplicatePad, inputLabelRefs, outputLabelRefs, mzX10ToRefMeasurement);
			}
		}
	}

	private void adjustCycle(CyclePad cyclePad, ArrayList<ReplicatePad> scanFiles, String[] inputLabels, String[] outputLabels, HashMap<Integer,String> mzX10ToMeasurement) {
			
		for (int i=0; i<inputLabels.length; i++) { // cycles through the various channels of the data
			AcquisitionPad acquisitionPad = (AcquisitionPad) cyclePad.getParent();
			Double value = (Double) cyclePad.getValue(labelToColumnName(inputLabels[i])); // the value of channel i
			
			if (value == null) {
				break; // happens if we are dealing with the first sample cycles of each replicate; avoids a nullPointerException
			}
			
			double thisPBLbackground = calculateBackground(cyclePad, i, scanFiles, getCorrectionType(), inputLabels[i], mzX10ToMeasurement);
			
			// now check if we need to add this cycle to the largest background hashmap
			if (getLargestBackground().get(inputLabelBackgrounds[i])==null){ // first cycle, need to add the background and largest cycle
				setLargestBackground(thisPBLbackground,i); // we know that this is the first cycle as the array is empty
				setLargestCycle(cyclePad,inputLabels[0],mzX10ToMeasurement);
			} 
		
			CyclePad largestCyclePad=(CyclePad) getLargestCycle().get("cycle pad");
			String label=(String) getLargestCycle().get("label");
			Double largestCycle = (Double) largestCyclePad.getValue(labelToColumnName(label));
			
			if ((Double) cyclePad.getValue(labelToColumnName(inputLabels[0]))>=largestCycle) {			
				setLargestCycle(cyclePad,inputLabels[0],mzX10ToMeasurement); // sets the largest cycle from mass 44
				setLargestBackground(thisPBLbackground,i); //set the largest background on the current mass being cycled through.
			}
			
			Double background = (Double) acquisitionPad.getValue(labelToColumnName(inputLabelBackgrounds[i]));

			if (background != null) {
				value += background;
			}
			// set the new value for the aquisition
			double newValue = value - thisPBLbackground;
			
			cyclePad.setValue(labelToColumnName(outputLabels[i]), newValue);
		}	
	}
	
	
	private double calculateBackground(CyclePad cyclePad, int channel, ArrayList<ReplicatePad> scanFiles, PBLCorrectionType correctionType, String inputLabel,  HashMap<Integer,String> mzX10ToMeasurement){
		
		// This method calculates a background value given a cycle pad, a channel, some scanFiles and a PBL correction		
		ArrayList<Double> pblBackgroundValues = new ArrayList<Double>(); // A collection of background values
		AcquisitionPad acquisitionPad = (AcquisitionPad) cyclePad.getParent();
		ReplicatePad replicatePad = (ReplicatePad) acquisitionPad.getParent();
		
		for (ReplicatePad scanFileReplicatePad:scanFiles) { // Loop through the scans present in the scan pad			
		
			if ((Integer) scanFileReplicatePad.getValue(labelToColumnName(inputLabelAlgorithms[channel])) == 0) { // no PBL to be performed, simply return the raw value
				break;

			} else { // We do have a PBL background
				
				if ((Integer) scanFileReplicatePad.getValue(labelToColumnName(inputLabelAlgorithms[channel])) == 1) { // This is the algorithm used if no half-mass is available
					Integer refMzX10 = (Integer) scanFileReplicatePad.getValue(labelToColumnName(inputLabelRefMzX10s[channel]));
					Double x2Coeff = (Double) scanFileReplicatePad.getValue(labelToColumnName(inputLabelX2Coeff[channel]));
					Double x1Coeff = (Double) scanFileReplicatePad.getValue(labelToColumnName(inputLabelX1Coeff[channel]));
					Double x0Coeff = (Double) scanFileReplicatePad.getValue(labelToColumnName(inputLabelX0Coeff[channel]));
					Double refValue = (Double) cyclePad.getValue(mzX10ToMeasurement.get(refMzX10));
				
					if (x2Coeff == null || Double.isNaN(x2Coeff) || x1Coeff == null || Double.isNaN(x1Coeff) || x0Coeff == null || Double.isNaN(x0Coeff) || refValue == null) {
						break;

					} else {
						Double refBackground = (Double) acquisitionPad.getValue(mzX10ToBackground.get(refMzX10));

						if (refBackground != null) {
							refValue += refBackground;
						}

						pblBackgroundValues.add(Double.valueOf(x2Coeff * Math.pow(refValue,2) + x1Coeff * refValue + x0Coeff));		
					}
				
				} else if ((Integer) scanFileReplicatePad.getValue(labelToColumnName(inputLabelAlgorithms[channel])) == 2) { // Half mass are being used.
					Integer refMzX10 = (Integer) scanFileReplicatePad.getValue(labelToColumnName(inputLabelRefMzX10s[channel]));
					Double factor = (Double) scanFileReplicatePad.getValue(labelToColumnName(inputLabelFactors[channel]));
					Double refValue = (Double) cyclePad.getValue(mzX10ToMeasurement.get(refMzX10));
	
					if (factor == null || Double.isNaN(factor) || refValue == null) {
						//pblBackgroundValues.add(new Double(0)); // adding no PBL background. TODO: Could simply break as adding 0 is incorrect
						break;
	
					} else {
						Double refBackground = (Double) acquisitionPad.getValue(mzX10ToBackground.get(refMzX10));
	
						if (refBackground != null) {
							refValue += refBackground;
						}
	
						pblBackgroundValues.add(Double.valueOf(-1*factor * refValue));
					}
				}				
			}
		} // end of loop though background

		//Now we have an array of backgrounds that can be used to calculate the appropriate background value for the cycle, based on the user-selected algorithm
		double thisPBLbackground = 0.0;

		if (pblBackgroundValues.size()== 0){
			return 0; // happens if we have no background calculated for this mass
		}

		if (correctionType==PBLCorrectionType.NearestScan || (correctionType==PBLCorrectionType.Interpolate && pblBackgroundValues.size()==1) ){
			// In this case we should have only 1 scan and thus 1 background at index 0
			thisPBLbackground = (double) pblBackgroundValues.get(0);	
		}

		if (correctionType==PBLCorrectionType.MovingAverage ){
			// Simply average the values and subtract
			
			double backgroundSum = 0.0;
			
			for (Double bckValue: pblBackgroundValues){
				backgroundSum=backgroundSum+ (double) bckValue;
			}
			
			thisPBLbackground = (backgroundSum/pblBackgroundValues.size());		
		}
		
		if (correctionType == PBLCorrectionType.Interpolate && pblBackgroundValues.size() == 2) {
			// Do a time-weighted average of the two backgrounds and subtract
			
			long scan1Date = scanFiles.get(0).getDate();
			long scan2Date = scanFiles.get(1).getDate();
			long timeIntervalScans = scan2Date - scan1Date;	
			//long thisReplicateDate = acquisitionPad.getDate();
			long thisReplicateDate = replicatePad.getDate();
			long timeIntervalSample = thisReplicateDate - scan1Date;
			
			double scan1Background=pblBackgroundValues.get(0);
			double scan2Background=pblBackgroundValues.get(1);
			double differenceBackgroundScans = scan1Background-scan2Background;
			
			double slope = (double) (differenceBackgroundScans/timeIntervalScans);

			thisPBLbackground = scan1Background-slope*timeIntervalSample;
		}
		
		return thisPBLbackground;
	}

	private void calculateVolatileScanData (ReplicatePad[] replicatePads, int padNumber, DependencyManager dependencyManager) {
		/*
		 * The goal of this method is to calculate data points and smooth lines for the volatile data, to be used in the graphics Composite 
		 */
		
		ArrayList<ReplicatePad> scanPads = new ArrayList<ReplicatePad>();
		CyclePad largestCyclePad=(CyclePad) getLargestCycle().get("cycle pad");
		String label=(String) getLargestCycle().get("label");
		Double Mv44Ref = (Double) largestCyclePad.getValue(labelToColumnName(label));
		@SuppressWarnings("unchecked")
		HashMap<Integer,String> mz10ToMeasurement = (HashMap<Integer,String>) getLargestCycle().get("mz10ToMeasurment");
		HashMap<String, Object> backgrounds = new HashMap<String, Object>();
		HashMap<String, Object> MoAvBackgrounds = new HashMap<String, Object>();

		for (int channel=0; channel<inputLabelX2Coeff.length; channel++) {
			ArrayList<BackgroundPoint> backgroundPoints = new ArrayList<BackgroundPoint>();

			for (int currentScan=0; currentScan<replicatePads.length; currentScan++) {
				if (replicatePads[currentScan].getReplicateType()==ReplicateType.SCAN ) {
					ArrayList<ReplicatePad> scans = new ArrayList<ReplicatePad>();
					scans.add(replicatePads[currentScan]);
				
					Double backgroundThisScan = calculateBackground(largestCyclePad,channel,scans,PBLCorrectionType.NearestScan,label,mz10ToMeasurement);

					if (!isTrue(replicatePads[currentScan], INPUT_LABEL_DISABLED)) {
						if (channel == 0) { // only adds each scan once, on label 44 and only if not disabled.
							scanPads.add(replicatePads[currentScan]);
						}
						
						backgroundPoints.add(new BackgroundPoint(replicatePads[currentScan].getDate(), backgroundThisScan, -1, -1, false,  BackgroundPointType.BackgroundPoint, replicatePads[currentScan].getReplicateId()));
					} else {
						backgroundPoints.add(new BackgroundPoint(replicatePads[currentScan].getDate(), backgroundThisScan, -1, -1, true,  BackgroundPointType.BackgroundPoint, replicatePads[currentScan].getReplicateId()));
					}
				}
			}

			Collections.sort(backgroundPoints);
			backgrounds.put(inputLabelBackgrounds[channel], backgroundPoints);
		}

		for (int channel=0; channel<inputLabelX2Coeff.length; channel++) {
			ArrayList<BackgroundPoint> backgroundPointsMoAv = new ArrayList<BackgroundPoint>();
			int i=0;
	
			// This part of the code loops through all the scans in the correction interval, 
			// calculates a background at 14kv, and put the result in the volatile key. 
			
			long lastDate=replicatePads[0].getDate();
	
			for(ReplicatePad thisScan: scanPads){
			
				// Computing the moving average value of our scan if they are based on the nearest scan
			
				if (getCorrectionType() == PBLCorrectionType.NearestScan){
					ArrayList<ReplicatePad> scans = new ArrayList<ReplicatePad>();
					scans.add(thisScan);
					Double backgroundThisScan=calculateBackground(largestCyclePad,channel,scans,PBLCorrectionType.NearestScan,label,mz10ToMeasurement);
					
					backgroundPointsMoAv.add(new BackgroundPoint(lastDate, backgroundThisScan, -1, -1, false,  BackgroundPointType.MovingAverage, -1));
					
					if(scanPads.size()>i+1) {
						lastDate=thisScan.getDate()+(long) ((float)scanPads.get(i+1).getDate()-(float)thisScan.getDate())/2;
						backgroundPointsMoAv.add(new BackgroundPoint(lastDate, backgroundThisScan, -1, -1, false,BackgroundPointType.MovingAverage, -1));
					}
					else{
						backgroundPointsMoAv.add(new BackgroundPoint(replicatePads[replicatePads.length-1].getDate(), backgroundThisScan, -1, -1, false,BackgroundPointType.MovingAverage, -1));
					}

				} else if (getCorrectionType() == PBLCorrectionType.Interpolate) {
					/*
					 * In this case all we need to do is add a calculated background for each scan as our moving average, plus add a background before and after the first/last scan if relevant.
					 */
					
					ArrayList<ReplicatePad> scans = new ArrayList<ReplicatePad>();
					scans.add(thisScan);
					Double backgroundThisScan = calculateBackground(largestCyclePad,channel,scans,PBLCorrectionType.NearestScan,label,mz10ToMeasurement);

					if (thisScan == scanPads.get(0) && thisScan != replicatePads[0]) { // add a moving average point at position 0
						backgroundPointsMoAv.add(new BackgroundPoint(replicatePads[0].getDate(), backgroundThisScan, -1, -1, false,BackgroundPointType.MovingAverage, -1));
					}
					
					backgroundPointsMoAv.add(new BackgroundPoint(thisScan.getDate(), backgroundThisScan, -1, -1, false,BackgroundPointType.MovingAverage, -1));

					if (i+1 == scanPads.size() && thisScan != replicatePads[replicatePads.length-1]) { //last scan, extend the graphics to last replicate
						backgroundPointsMoAv.add(new BackgroundPoint(replicatePads[replicatePads.length-1].getDate(), backgroundThisScan, -1, -1, false,BackgroundPointType.MovingAverage, -1));
					}

				} else if (getCorrectionType()==PBLCorrectionType.MovingAverage){
					/* For this algorithm, we always calculate the background for the samples before the scans (background47), so keep in mind that the current
					 * scan (i) is included as a scan after the samples. Thus we need to set this background value to do a graph point both for the previous scan date, and
					 * we then use it with the current scan date (i.e. the background line will be between the last scan and this one).
					 */	
					int lowerLimits=0;
					int upperLimits=0;

					if (i<=(getMinNumScansBeforeAfter())) {
						lowerLimits=0;
					} else {
						lowerLimits=i-getMinNumScansBeforeAfter();
					}

					if (scanPads.size()>i+getMinNumScansBeforeAfter()-1) {
						upperLimits=i+getMinNumScansBeforeAfter()-1;
					} else {
						upperLimits=scanPads.size()-1;
					}			
				
					// set the polynomial parameters to 0
					ArrayList<ReplicatePad> scans = new ArrayList<ReplicatePad>();
						
					// Add the  scans required to the scans Array before calculating an average
				
					for (int k=lowerLimits; k<=upperLimits;k++) {
						scans.add(scanPads.get(k));
					}
				
					Double backgroundThisScan=calculateBackground(largestCyclePad,channel,scans,PBLCorrectionType.MovingAverage,label,mz10ToMeasurement);
				
					backgroundPointsMoAv.add(new BackgroundPoint(lastDate, backgroundThisScan, -1, -1, false,BackgroundPointType.MovingAverage, -1));
					backgroundPointsMoAv.add(new BackgroundPoint(thisScan.getDate(), backgroundThisScan, -1, -1, false,BackgroundPointType.MovingAverage, -1));

					if (scanPads.size()>i+1) {
						lastDate = thisScan.getDate();

					} else {
						ArrayList<ReplicatePad> lastScans = new ArrayList<ReplicatePad>();
						/*
						 * This is only executed at the end of the correction interval, to add an appropriate moving average
						 * for the background beyond the last scan, up to the last sample/standard in the CorrInt
						 */
						lowerLimits = ((upperLimits-lowerLimits+1) > getMinNumScansBeforeAfter()) ? lowerLimits+1 : lowerLimits;

						for (int k=lowerLimits; k<=upperLimits;k++) {
							lastScans.add(scanPads.get(k));
						}
						Double backgroundLastScans=calculateBackground(largestCyclePad,channel,lastScans,PBLCorrectionType.MovingAverage,label,mz10ToMeasurement);

						backgroundPointsMoAv.add(new BackgroundPoint(thisScan.getDate(), backgroundLastScans, -1, -1, false,BackgroundPointType.MovingAverage, -1));
						backgroundPointsMoAv.add(new BackgroundPoint(replicatePads[replicatePads.length-1].getDate(), backgroundLastScans, -1, -1, false,BackgroundPointType.MovingAverage, -1));
					}
				}
				
				i++;
			}

			Collections.sort(backgroundPointsMoAv);
			MoAvBackgrounds.put(inputLabelBackgrounds[channel], backgroundPointsMoAv);	

		} // end of channel, go to the next channel
		
		// write the background data to the volatile data
		replicatePads[padNumber].setVolatileData(getVolatileDataAllBackgroundsKey(), backgrounds); // all the activated Bkg points at 14V that the graphics controller can plot
		replicatePads[padNumber].setVolatileData(getVolatileDataAverageBackgroundsKey(), MoAvBackgrounds); // the points for the moving average at 14V
		replicatePads[padNumber].setVolatileData(getVolatileDataMaximumCycle(), " " + Math.round(Mv44Ref)); // the value of the 44 beam used on the axis label
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
	
	public class BackgroundPoint implements Comparable<BackgroundPoint> {
		private long date;
		private double background;
		private int colorId;
		private int shapeId;
		private boolean disabled;
		private BackgroundPointType type;
		private int scanId;

		public BackgroundPoint(long date, double background, int colorId, int shapeId, boolean disabled, BackgroundPointType type, int scanId) {
			this.date = date;
			this.background = background;
			this.colorId = colorId;
			this.shapeId = shapeId;
			this.disabled = disabled;
			this.type = type;
			this.scanId = scanId;
		}

		public long getDate() {
			return date;
		}

		public double getBackground() {
			return background;
		}

		public int getColorId() {
			return colorId;
		}

		public int getShapeId() {
			return shapeId;
		}

		public boolean getDisabled() {
			return disabled;
		}

		public BackgroundPointType getType() {
			return type;
		}
		
		public int getScanId() {
			return scanId;
		}

		// Allows the points to be sorted in ascending order based on their dates - needed to plot and do moving averages
		@Override
		public int compareTo(BackgroundPoint bkgToCompare) {
			return Long.compare(this.getDate(), bkgToCompare.getDate());
		}
	}
}
