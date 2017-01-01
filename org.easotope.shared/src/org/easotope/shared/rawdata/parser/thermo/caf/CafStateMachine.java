/*
 * Copyright © 2016-2017 by Devon Bowen.
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

package org.easotope.shared.rawdata.parser.thermo.caf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.parser.MapBuilder;
import org.easotope.shared.rawdata.parser.thermo.RollingBuffer;
import org.easotope.shared.rawdata.parser.thermo.StateMachine;
import org.easotope.shared.rawdata.parser.thermo.Util;

public class CafStateMachine implements StateMachine {
	final int INTEGER_SIZE = Integer.SIZE / Byte.SIZE;
	final int DOUBLE_SIZE = Double.SIZE / Byte.SIZE;

	private Pattern massPattern = Pattern.compile("Int\\.(\\d+).*", Pattern.DOTALL);
	private Pattern backgroundPattern = Pattern.compile(".*?(\\-?\\d+\\.\\d+) mV.*");

	private enum State { NONE, WAITING_FOR_STANDARD, WAITING_FOR_SAMPLE, WAITING_FOR_CYCLE1, WAITING_FOR_CYCLE2, WAITING_FOR_CYCLE3, WAITING_FOR_CYCLE4 };
	private State state = State.NONE;

	private MapBuilder mapBuilder = new MapBuilder();
	private enum MeasuredObjectType { REFERENCE_GAS, SAMPLE, BACKGROUND };
	private MeasuredObjectType measuredObjectType;
	private int totalExpected;
	private int currentNumber;
	private HashSet<Integer> knownMasses = new HashSet<Integer>();
	private String background;

	private Integer datePattern[] = {
			0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, null, null, null, null
	};

	private Integer voltagePattern[] = {
			0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, null, null, null, null, 0x00, 0x00, 0x00, 0x00, null, 0x00, 0x00, 0x00, null, 0x00, 0x00, 0x00
	};

	private Integer rawDataBlock[] = {
			0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, null, null, null, null
	};

	CafStateMachine(boolean historicMode) {
		//this.historicMode = historicMode;
	}

	public void foundString(String string, RollingBuffer buffer) {
		Matcher massPatternMatcher = massPattern.matcher(string);

		if ("Date".equals(string) && buffer.startsWith(datePattern)) {
			byte[] intArray = new byte[INTEGER_SIZE];

			intArray[0] = Util.toByte(buffer.get(11));
			intArray[1] = Util.toByte(buffer.get(10));
			intArray[2] = Util.toByte(buffer.get(9));
			intArray[3] = Util.toByte(buffer.get(8));

			mapBuilder.put(InputParameter.Java_Date, new Long(((long) Util.toInteger(intArray)) * 1000));

		} else if ("DualInlet RawData Standard Block".equals(string) && buffer.startsWith(rawDataBlock)) {
			measuredObjectType = MeasuredObjectType.REFERENCE_GAS;
			totalExpected = buffer.get(8);
			currentNumber = 0;
			state = State.WAITING_FOR_CYCLE1;
			
		} else if ("DualInlet RawData Sample Block".equals(string) && buffer.startsWith(rawDataBlock)) {
			measuredObjectType = MeasuredObjectType.SAMPLE;
			totalExpected = buffer.get(8);
			currentNumber = 0;
			state = State.WAITING_FOR_CYCLE1;

		} else if (string.trim().isEmpty() && state == State.WAITING_FOR_CYCLE1) {
			state = State.WAITING_FOR_CYCLE2;

		} else if (string.trim().isEmpty() && state == State.WAITING_FOR_CYCLE2) {
			state = State.WAITING_FOR_CYCLE3;

		} else if (string.trim().isEmpty() && state == State.WAITING_FOR_CYCLE3) {
			state = State.WAITING_FOR_CYCLE4;

		} else if (string.trim().isEmpty() && state == State.WAITING_FOR_CYCLE4 && buffer.startsWith(voltagePattern)) {
			int numberOfMeasurements = buffer.get(20);

			for (int currentMeasuredObject=0; currentMeasuredObject < numberOfMeasurements; currentMeasuredObject++) {
				byte[] doubleArray = new byte[DOUBLE_SIZE];

				doubleArray[0] = Util.toByte(buffer.get(voltagePattern.length + currentMeasuredObject*DOUBLE_SIZE + 7));
				doubleArray[1] = Util.toByte(buffer.get(voltagePattern.length + currentMeasuredObject*DOUBLE_SIZE + 6));
				doubleArray[2] = Util.toByte(buffer.get(voltagePattern.length + currentMeasuredObject*DOUBLE_SIZE + 5));
				doubleArray[3] = Util.toByte(buffer.get(voltagePattern.length + currentMeasuredObject*DOUBLE_SIZE + 4));
				doubleArray[4] = Util.toByte(buffer.get(voltagePattern.length + currentMeasuredObject*DOUBLE_SIZE + 3));
				doubleArray[5] = Util.toByte(buffer.get(voltagePattern.length + currentMeasuredObject*DOUBLE_SIZE + 2));
				doubleArray[6] = Util.toByte(buffer.get(voltagePattern.length + currentMeasuredObject*DOUBLE_SIZE + 1));
				doubleArray[7] = Util.toByte(buffer.get(voltagePattern.length + currentMeasuredObject*DOUBLE_SIZE + 0));

				InputParameter inputParameter = findInputParameter(measuredObjectType, currentMeasuredObject);

				if (inputParameter == null) {
					new RuntimeException("File contains too many channels.");
				}

				mapBuilder.put(inputParameter, currentNumber+1, Util.toDouble(doubleArray));
			}

			currentNumber++;

			if (currentNumber < totalExpected) {
				state = State.WAITING_FOR_CYCLE1;

			} else {
				if (measuredObjectType == MeasuredObjectType.SAMPLE) {
					measuredObjectType = MeasuredObjectType.REFERENCE_GAS;
					totalExpected = 1;
					currentNumber = -1;
					state = State.WAITING_FOR_CYCLE1;

				} else {
					state = State.NONE;
				}
			}

		} else if (massPatternMatcher.matches()) {
			int mass = (int) Math.round(Double.parseDouble(massPatternMatcher.group(1)));

			if (!knownMasses.contains(mass)) {
				mapBuilder.put(InputParameter.ChannelToMZX10, knownMasses.size(), mass);
				knownMasses.add(mass);
			}

		} else if (string.trim().startsWith("Background:") && !string.endsWith("..")) {
			background = string.trim();
		}
	}

	private InputParameter findInputParameter(MeasuredObjectType measuredObjectType, int number) {
		if (number > 9) {
			return null;
		}

		switch (measuredObjectType) {
			case BACKGROUND:
				return InputParameter.values()[InputParameter.Channel0_Background.ordinal() + number];

			case REFERENCE_GAS:
				return InputParameter.values()[InputParameter.Channel0_Ref.ordinal() + number];

			case SAMPLE:
				return InputParameter.values()[InputParameter.Channel0_Sample.ordinal() + number];
		}

		return null;
	}

	public HashMap<InputParameter, Object> getMap() {
		if (background != null) {
			String[] backgrounds = background.split(",");

			int counter = 0;	
			for (String background : backgrounds) {
				Matcher backgroundPatternMatcher = backgroundPattern.matcher(background);

				if (backgroundPatternMatcher.matches()) {
					InputParameter inputParameter = findInputParameter(MeasuredObjectType.BACKGROUND, counter++);

					if (inputParameter != null) {
						String backgroundValue = backgroundPatternMatcher.group(1);
						mapBuilder.put(inputParameter, backgroundValue);
					}
				}
			}

			if (counter != totalExpected) {
				new RuntimeException("wrong number of background numbers loaded: " + counter);
			}
		}

		return mapBuilder.getMap();
	}
}
