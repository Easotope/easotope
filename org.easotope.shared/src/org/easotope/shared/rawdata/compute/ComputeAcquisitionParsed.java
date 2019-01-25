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

package org.easotope.shared.rawdata.compute;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.shared.Messages;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.parser.AutoParser;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;

public class ComputeAcquisitionParsed {
	private ArrayList<AcquisitionParsedV2> maps = null;
	private String assumedTimeZone = null;

	public ComputeAcquisitionParsed(RawFile rawFile, byte[] fileBytes, boolean historicMode, String assumedTimeZone) {
		AutoParser parser = new AutoParser(fileBytes, rawFile.getOriginalName(), historicMode, assumedTimeZone);
		this.assumedTimeZone = parser.getAssumedTimeZone();

		String error = parser.getError();

		if (error != null) {
			throw new RuntimeException(error);
		}

		maps = new ArrayList<AcquisitionParsedV2>();

		for (HashMap<InputParameter,Object> map : parser.getMap()) {
			Long date = (Long) map.get(InputParameter.Java_Date);

			if (date == null) {
				error = MessageFormat.format(Messages.computeAcquisitionParsed_fileHasNoDateText, new Object[] { new File(rawFile.getOriginalName()).getName() });
				throw new RuntimeException(error);
			}

			int numCycles = -1;
			HashMap<InputParameter,Double[]> measurements = new HashMap<InputParameter,Double[]>();
			HashMap<InputParameter,Double> backgrounds = new HashMap<InputParameter,Double>();
			HashMap<InputParameter,Object> misc = new HashMap<InputParameter,Object>();
			Integer[] channelToMzX10 = null;

			for (InputParameter parameter : map.keySet()) {
				Object obj = map.get(parameter);

				if (parameter == InputParameter.Java_Date) {
					// ignore

				} else if (parameter == InputParameter.ChannelToMZX10) {
					Vector<?> vector = (Vector<?>) obj;
					channelToMzX10 = new Integer[vector.size()];
	
					for (int i=0; i<vector.size(); i++) {
						if (vector.get(i) instanceof Integer) {
							channelToMzX10[i] = (Integer) vector.get(i);
						}
					}
	
				} else if (parameter.getIsMeasurements()) {
					Vector<?> vector = (Vector<?>) obj;
	
					if (numCycles == -1) {
						numCycles = vector.size();
					} else {
						if (numCycles != vector.size()) {
							error = MessageFormat.format(Messages.computeAcquisitionParsed_fileHasUnequalCycles, new Object[] { new File(rawFile.getOriginalName()).getName() });
							throw new RuntimeException(error);
						}
					}

					Double[] array = new Double[numCycles];

					for (int i=0; i<numCycles; i++) {
						if (vector.get(i) instanceof Double) {
							array[i] = (Double) vector.get(i);
						}
					}

					measurements.put(parameter, array);

				} else if (parameter.getIsBackground()) {
					backgrounds.put(parameter, (Double) obj);

				} else {
					misc.put(parameter, obj);
				}
			}

			if (numCycles == -1) {
				error = MessageFormat.format(Messages.computeAcquisitionParsed_fileHasNoCycles, new Object[] { new File(rawFile.getOriginalName()).getName() });
				throw new RuntimeException(error);
			}

			AcquisitionParsedV2 acquisition = new AcquisitionParsedV2();
			acquisition.setDate(date);
			acquisition.setNumCycles(numCycles);
			acquisition.setMeasurements(measurements);
			acquisition.setBackgrounds(backgrounds);
			acquisition.setChannelToMzX10(channelToMzX10);
			acquisition.setMisc(misc);

			maps.add(acquisition);
		}
	}
	
	public ArrayList<AcquisitionParsedV2> getMaps() {
		return maps;
	}

	public String getAssumedTimeZone() {
		return assumedTimeZone;
	}
}
