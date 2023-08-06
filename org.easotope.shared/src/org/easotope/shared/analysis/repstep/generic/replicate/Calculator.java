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

package org.easotope.shared.analysis.repstep.generic.replicate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.generic.replicate.dependencies.Dependencies;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.PadDate;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.rawdata.InputParameter;

public class Calculator extends RepStepCalculator {
    private static Pattern weightPattern = Pattern.compile("([\\d\\.]*) \\| (\\d*)");

	public static final String OUTPUT_LABEL_MASS_SPEC = "Mass Spectrometer";
	public static final String OUTPUT_LABEL_SOURCE_NAME = "Easotope Name of Source Material";
	public static final String OUTPUT_LABEL_SAMPLE_TYPE = "Sample Type";
	public static final String OUTPUT_LABEL_CORR_INTERVAL = "Corr Interval Timestamp";
	public static final String OUTPUT_LABEL_ACID_TEMP = "Acid Temperature";
	public static final String OUTPUT_LABEL_ACQUISITIONS = "Num Acqusitions";
	public static final String OUTPUT_LABEL_ENABLED_ACQUISITIONS = "Num Enabled Acqusitions";
	public static final String OUTPUT_LABEL_MZ44_FIRST_REFGAS_CYCLE = "MZ44 First Ref Gas Cycle";
	public static final String OUTPUT_LABEL_MZ44_FIRST_SAMPLE_CYCLE = "MZ44 First Sample Cycle";

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	@Override
	public DependencyManager getDependencyManager(ReplicatePad[] replicatePads, int standardNumber) {
		return new Dependencies();
	}

	@Override
	public void calculate(ReplicatePad[] replicatePads, int padNumber, DependencyManager dependencyManager) {
		Dependencies dependencies = (Dependencies) dependencyManager;
		ReplicatePad replicatePad = replicatePads[padNumber];
		ArrayList<AcquisitionPad> acquisitions = replicatePad.getChildren();

		replicatePad.setValue(labelToColumnName(OUTPUT_LABEL_MASS_SPEC), dependencies.getMassSpec());
		replicatePad.setValue(labelToColumnName(OUTPUT_LABEL_SOURCE_NAME), dependencies.getSource());
		replicatePad.setValue(labelToColumnName(OUTPUT_LABEL_SAMPLE_TYPE), dependencies.getSampleType());

    	Long corrIntervalTimeStamp = dependencies.getCorrIntervalTimeStamp();
    	replicatePad.setValue(labelToColumnName(OUTPUT_LABEL_CORR_INTERVAL), (corrIntervalTimeStamp == null) ? null : new PadDate(corrIntervalTimeStamp));

    	replicatePad.setValue(labelToColumnName(OUTPUT_LABEL_ACID_TEMP), dependencies.getAcidTemperature());

    	int enabledAcqusitions = 0;

    	for (AcquisitionPad acquisitionPad : acquisitions) {
    		if (!((Boolean) acquisitionPad.getValue(Pad.DISABLED))) {
    			enabledAcqusitions++;
    		}
    	}

    	replicatePad.setValue(labelToColumnName(OUTPUT_LABEL_ACQUISITIONS), acquisitions.size());
    	replicatePad.setValue(labelToColumnName(OUTPUT_LABEL_ENABLED_ACQUISITIONS), enabledAcqusitions);

    	if (acquisitions.size() != 0) {
    		ArrayList<CyclePad> firstAcqusitionCycles = acquisitions.get(0).getChildren();

    		if (firstAcqusitionCycles.size() > 0) {
    			CyclePad firstCycle = firstAcqusitionCycles.get(0);
    			Object mz44FirstRefGasCycle = firstCycle.getValue("V44_Ref");
    			replicatePad.setValue(labelToColumnName(OUTPUT_LABEL_MZ44_FIRST_REFGAS_CYCLE), mz44FirstRefGasCycle);
    		}

    		if (firstAcqusitionCycles.size() > 1) {
    			CyclePad secondCycle = firstAcqusitionCycles.get(1);
    			Object mz44FirstSampleCycle = secondCycle.getValue("V44_Sample");
    			replicatePad.setValue(labelToColumnName(OUTPUT_LABEL_MZ44_FIRST_SAMPLE_CYCLE), mz44FirstSampleCycle);
    		}
    	}

    	InputParameter[] inputParameters = {
    		InputParameter.Identifier_1,
    		InputParameter.Identifier_2,
    		InputParameter.Run,
    		InputParameter.Sample_Name
    	};

    	for (InputParameter inputParameter : inputParameters) {
    		HashSet<String> found = new HashSet<String>();

        	for (AcquisitionPad acquisitionPad : acquisitions) {
        		found.add((String) acquisitionPad.getValue(inputParameter.toString()));
        	}

        	if (found.size() == 1) {
        		String common = found.toArray(new String[1])[0];

        		if (common != null) {
        			replicatePad.setValue(inputParameter.toString(), common);
        		}
        	}
    	}

		HashSet<String> weights = new HashSet<String>();

    	for (AcquisitionPad acquisitionPad : acquisitions) {
    		Object obj = acquisitionPad.getValue(InputParameter.Sample_Weight.toString());

    		if (obj instanceof String) {
    			weights.add((String) obj);
    		} else {
    			weights.add(null);
    		}
    	}

    	if (weights.size() == 1) {
    		String common = weights.toArray(new String[1])[0];

    		 if (common != null) {
        		 Matcher matcher = weightPattern.matcher(common);

        		 if (matcher.matches()) {
        			 double weight = Double.parseDouble(matcher.group(1));
        			 int divisor = Integer.parseInt(matcher.group(2));

        			 if (divisor == acquisitions.size()) {
        				 replicatePad.setValue(InputParameter.Sample_Weight.toString(), weight);
        			 } else {
        				 String temp = weight + " | " + acquisitions.size() + "/" + divisor;
        				 replicatePad.setValue(InputParameter.Sample_Weight.toString(), temp);
        			 }
        		 }
    		}
    	}
	}
}
