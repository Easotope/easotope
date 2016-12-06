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

package org.easotope.shared.analysis.repstep.generic.replicate;

import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.generic.replicate.dependencies.Dependencies;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.PadDate;
import org.easotope.shared.core.scratchpad.ReplicatePad;

public class Calculator extends RepStepCalculator {
	public static final String OUTPUT_LABEL_MASS_SPEC = "Mass Spectrometer";
	public static final String OUTPUT_LABEL_SOURCE_NAME = "Source Material Name";
	public static final String OUTPUT_LABEL_SAMPLE_TYPE = "Sample Type";
	public static final String OUTPUT_LABEL_CORR_INTERVAL = "Corr Interval Timestamp";
	public static final String OUTPUT_LABEL_ACID_TEMP = "Acid Temperature";

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

    		replicatePads[padNumber].setValue(labelToColumnName(OUTPUT_LABEL_MASS_SPEC), dependencies.getMassSpec());
    		replicatePads[padNumber].setValue(labelToColumnName(OUTPUT_LABEL_SOURCE_NAME), dependencies.getSource());
    		replicatePads[padNumber].setValue(labelToColumnName(OUTPUT_LABEL_SAMPLE_TYPE), dependencies.getSampleType());

    		Long corrIntervalTimeStamp = dependencies.getCorrIntervalTimeStamp();
    		replicatePads[padNumber].setValue(labelToColumnName(OUTPUT_LABEL_CORR_INTERVAL), (corrIntervalTimeStamp == null) ? null : new PadDate(corrIntervalTimeStamp));

    		replicatePads[padNumber].setValue(labelToColumnName(OUTPUT_LABEL_ACID_TEMP), dependencies.getAcidTemperature());
	}
}
