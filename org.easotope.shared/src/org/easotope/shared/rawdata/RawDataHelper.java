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

package org.easotope.shared.rawdata;

import java.util.HashMap;
import java.util.List;

import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ReplicatePad.ReplicateType;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;
import org.easotope.shared.rawdata.tables.ReplicateV1;

public class RawDataHelper {
	private static HashMap<Integer,InputParameter> mzX10ToBackground = new HashMap<Integer,InputParameter>();
	private static HashMap<Integer,InputParameter> mzX10ToReference = new HashMap<Integer,InputParameter>();
	private static HashMap<Integer,InputParameter> mzX10ToSample = new HashMap<Integer,InputParameter>();

	public static int addReplicateToScratchPad(ScratchPad<ReplicatePad> scratchPad, ReplicateV1 replicate, List<Acquisition> acquisitions) {
		ReplicateType replicateType = (replicate.getStandardId() == DatabaseConstants.EMPTY_DB_ID) ? ReplicateType.SAMPLE_RUN : ReplicateType.STANDARD_RUN;
		ReplicatePad replicatePad = new ReplicatePad(scratchPad, replicate.getDate(), replicateType);
		replicatePad.setSourceId((replicate.getStandardId() == DatabaseConstants.EMPTY_DB_ID) ? replicate.getSampleId() : replicate.getStandardId());
		replicatePad.setValue(Pad.DISABLED, replicate.isDisabled());

		for (Acquisition acquisition : acquisitions) {
			AcquisitionInputV0 acquisitionInput = acquisition.getAcquisitionInput();
			AcquisitionParsedV2 acquisitionParsed = acquisition.getAcquisitionParsed();

			AcquisitionPad acquisitionPad = new AcquisitionPad(replicatePad, acquisitionParsed.getDate());
			acquisitionPad.setValue(Pad.DISABLED, acquisitionInput.isDisabled());

			HashMap<InputParameter,Double> backgrounds = new HashMap<InputParameter,Double>();

			for (InputParameter inputParameter : acquisitionParsed.getBackgrounds().keySet()) {
				int channel = inputParameter.ordinal() - InputParameter.Channel0_Background.ordinal();
				Integer[] channelToMzX10 = replicate.getChannelToMzX10();

				if (channelToMzX10 != null && channelToMzX10.length > channel && channelToMzX10[channel] != null) {
					InputParameter newInputParameter = mzX10ToBackground.get(channelToMzX10[channel]);
					Double object = acquisitionParsed.getBackgrounds().get(inputParameter);
					backgrounds.put(newInputParameter, object);
				}
			}

			for (InputParameter inputParameter : backgrounds.keySet()) {
				Double value = backgrounds.get(inputParameter);
				acquisitionPad.setValue(inputParameter.toString(), value);
			}

			for (int j=0; j<acquisitionParsed.getNumCycles(); j++) {
				CyclePad cyclePad = new CyclePad(acquisitionPad, j+1);

				if (j < acquisitionInput.getDisabledCycles().length) {
					cyclePad.setValue(Pad.DISABLED, acquisitionInput.getDisabledCycles()[j]);

					if (acquisitionInput.getOffPeakCycles() != null) {
						cyclePad.setValue(Pad.OFF_PEAK, acquisitionInput.getOffPeakCycles()[j]);
					}
				}

				HashMap<InputParameter,Double[]> measurements = new HashMap<InputParameter,Double[]>();

				for (InputParameter inputParameter : acquisitionParsed.getMeasurements().keySet()) {
					InputParameter newInputParameter = null;
					Integer[] channelToMzX10 = replicate.getChannelToMzX10();

					if (inputParameter.getInputParameterType() == InputParameterType.RefMeasurements) {
						int channel = inputParameter.ordinal() - InputParameter.Channel0_Ref.ordinal();

						if (channelToMzX10 != null && channelToMzX10.length > channel && channelToMzX10[channel] != null) {
							newInputParameter = mzX10ToReference.get(channelToMzX10[channel]);
						}

					} else {
						int channel = inputParameter.ordinal() - InputParameter.Channel0_Sample.ordinal();

						if (channelToMzX10 != null && channelToMzX10.length > channel && channelToMzX10[channel] != null) {
							newInputParameter = mzX10ToSample.get(channelToMzX10[channel]);
						}
					}

					if (newInputParameter != null) {
						Double[] object = acquisitionParsed.getMeasurements().get(inputParameter);
						measurements.put(newInputParameter, object);
					}
				}

				for (InputParameter inputParameter : measurements.keySet()) {
					Double[] values = measurements.get(inputParameter);

					if (j < values.length && values[j] != null) {
						cyclePad.setValue(inputParameter.toString(), values[j]);
					}
				}
			}
		}

		return scratchPad.getChildren().indexOf(replicatePad);
	}
	
	static {
		for (InputParameter inputParameter : InputParameter.values()) {
			if (inputParameter.getMzX10() != null) {
				switch (inputParameter.getInputParameterType()) {
					case Background:
						mzX10ToBackground.put(inputParameter.getMzX10(), inputParameter);
						break;
						
					case RefMeasurements:
						mzX10ToReference.put(inputParameter.getMzX10(), inputParameter);
						break;
						
					case SampleMeasurements:
						mzX10ToSample.put(inputParameter.getMzX10(), inputParameter);
						break;
						
					default:
						break;
				}
			}
		}
	}
}
