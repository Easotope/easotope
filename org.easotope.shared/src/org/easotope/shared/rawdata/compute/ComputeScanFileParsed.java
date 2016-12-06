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

package org.easotope.shared.rawdata.compute;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Vector;

import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.shared.Messages;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.InputParameterType;
import org.easotope.shared.rawdata.parser.AutoParser;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;

public class ComputeScanFileParsed {
	public static ScanFileParsedV2 compute(RawFile rawFile, byte[] fileBytes, boolean historicMode) {
		AutoParser parser = new AutoParser(fileBytes, rawFile.getOriginalName(), historicMode, null);
		String error = parser.getError();

		if (error != null) {
			throw new RuntimeException(error);
		}

		HashMap<InputParameter,Object> map = parser.getMap().get(0);
		Long date = (Long) map.get(InputParameter.Java_Date);

		if (date == null) {
			error = MessageFormat.format(Messages.computeScanFileParsed_fileHasNoDateText, new Object[] { new File(rawFile.getOriginalName()).getName() });
			throw new RuntimeException(error);
		}

		Double scanFromVoltage = (Double) map.get(InputParameter.ScanFromVoltage);
		Double scanToVoltage = (Double) map.get(InputParameter.ScanToVoltage);

		if (scanFromVoltage == null || scanToVoltage == null) {
			error = MessageFormat.format(Messages.computeScanFileParsed_fileHasNoFromToVoltages, new Object[] { new File(rawFile.getOriginalName()).getName() });
			throw new RuntimeException(error);
		}

		HashMap<InputParameter,Double[]> measurements = new HashMap<InputParameter,Double[]>();
		Integer[] channelToMzX10 = null;

		for (InputParameter parameter : map.keySet()) {
			Object obj = map.get(parameter);

			if (parameter == InputParameter.ChannelToMZX10) {
				Vector<?> vector = (Vector<?>) obj;
				channelToMzX10 = new Integer[vector.size()];

				for (int i=0; i<vector.size(); i++) {
					if (vector.get(i) instanceof Integer) {
						channelToMzX10[i] = (Integer) vector.get(i);
					}
				}

			} else if (parameter.getInputParameterType() == InputParameterType.Scan) {
				Vector<?> vector = (Vector<?>) obj;

				int numValues = vector.size();
				Double[] array = new Double[numValues];

				for (int i=0; i<numValues; i++) {
					if (vector.get(i) instanceof Double) {
						array[i] = (Double) vector.get(i);
					}
				}

				measurements.put(parameter, array);
			}
		}

		ScanFileParsedV2 scanFileParsed = new ScanFileParsedV2();
		scanFileParsed.setDate(date);
		scanFileParsed.setMeasurements(measurements);
		scanFileParsed.setFromVoltage(scanFromVoltage);
		scanFileParsed.setToVoltage(scanToVoltage);
		scanFileParsed.setChannelToMzX10(channelToMzX10);

		return scanFileParsed;
	}
}
