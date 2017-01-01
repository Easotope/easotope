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

package org.easotope.shared.rawdata.parser.thermo.did;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.parser.MapBuilder;
import org.easotope.shared.rawdata.parser.thermo.RollingBuffer;
import org.easotope.shared.rawdata.parser.thermo.StateMachine;
import org.easotope.shared.rawdata.parser.thermo.Util;

public class DidStateMachine implements StateMachine {
	final int INTEGER_SIZE = Integer.SIZE / Byte.SIZE;
	final int DOUBLE_SIZE = Double.SIZE / Byte.SIZE;

	private Pattern standardPattern = Pattern.compile("Standard (\\d+)");
	private Pattern samplePattern = Pattern.compile("Sample (\\d+)");
	private Pattern backgroundPattern = Pattern.compile(".*?(\\-?\\d+\\.\\d+) mV.*");

	private enum State { NONE, WAITING_FOR_STANDARD, WAITING_FOR_SAMPLE, WAITING_FOR_CYCLE, WAITING_FOR_MASS, WAITING_FOR_SECOND_MASS, WAITING_FOR_LINE_INFO };
	private State state = State.NONE;

	private MapBuilder mapBuilder = new MapBuilder();
	private enum MeasuredObjectType { REFERENCE_GAS, SAMPLE, BACKGROUND };
	private MeasuredObjectType measuredObjectType;
	private int totalExpected;
	private int currentNumber;
	private String background;
	private boolean gotWeight;
	private boolean gotId1;
	private boolean gotId2;
	private boolean gotRun;
	private int seqInfoCounter;
	private String lastSeqInfoString;

	private Integer sequenceInfoPattern[] = {
			0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, null, null, null, null
	};

	private Integer datePattern[] = {
			0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, null, null, null, null
	};

	private Integer voltagePattern[] = {
			0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, null, null, null, null, 0x00, 0x00, 0x00, 0x00, null, 0x00, 0x00, 0x00, null, 0x00, 0x00, 0x00
	};

	private Integer rawDataBlock[] = {
			0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, null, null, null, null
	};

	private Integer massIndex[] = {
			0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, null, 0x00, 0x00, 0x00
	};

	DidStateMachine(boolean historicMode) {
		//this.historicMode = historicMode;
	}

	public void foundString(String string, RollingBuffer buffer) {
		Matcher standardPatternMatcher = standardPattern.matcher(string);
		Matcher samplePatternMatcher = samplePattern.matcher(string);

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
			state = State.WAITING_FOR_STANDARD;

		} else if (state == State.WAITING_FOR_STANDARD && standardPatternMatcher.matches()) {
			int foundCycle = Integer.valueOf(standardPatternMatcher.group(1));

			if (foundCycle != currentNumber) {
				state = State.NONE;
			}

			state = State.WAITING_FOR_CYCLE;
			
		} else if ("DualInlet RawData Sample Block".equals(string) && buffer.startsWith(rawDataBlock)) {
			measuredObjectType = MeasuredObjectType.SAMPLE;
			totalExpected = buffer.get(8);
			currentNumber = 0;
			state = State.WAITING_FOR_SAMPLE;

		} else if (state == State.WAITING_FOR_SAMPLE && samplePatternMatcher.matches()) {
			int foundCycle = Integer.valueOf(samplePatternMatcher.group(1));
			
			if (foundCycle != currentNumber) {
				state = State.NONE;
			}

			state = State.WAITING_FOR_CYCLE;
			
		} else if ("Standard Pre".equals(string) && buffer.startsWith(rawDataBlock)) {
			measuredObjectType = MeasuredObjectType.REFERENCE_GAS;
			totalExpected = 0;
			currentNumber = -1;
			state = State.WAITING_FOR_CYCLE;

		} else if (state == State.WAITING_FOR_CYCLE && string.isEmpty() && buffer.startsWith(voltagePattern)) {
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
				state = (measuredObjectType == MeasuredObjectType.REFERENCE_GAS) ? State.WAITING_FOR_STANDARD : State.WAITING_FOR_SAMPLE;
			} else {
				state = State.NONE;
			}

		} else if ("Index to Mass".equals(string) && buffer.startsWith(massIndex)) {
			totalExpected = buffer.get(8);
			currentNumber = 0;
			state = State.WAITING_FOR_MASS;

		} else if (state == State.WAITING_FOR_MASS) {
			mapBuilder.put(InputParameter.ChannelToMZX10, currentNumber, (int) Math.round(Double.parseDouble(string) * 10.0));
			currentNumber++;
			state = (currentNumber == totalExpected) ? State.NONE : State.WAITING_FOR_SECOND_MASS;

		} else if (state == State.WAITING_FOR_SECOND_MASS) {
			state = State.WAITING_FOR_MASS;

		} else if (string.startsWith("Background:") && !string.endsWith("..")) {
			background = string;

		} else if (string.trim().startsWith("Sequence Info") && buffer.startsWith(sequenceInfoPattern)) {
			byte[] intArray = new byte[INTEGER_SIZE];

			intArray[0] = Util.toByte(buffer.get(11));
			intArray[1] = Util.toByte(buffer.get(10));
			intArray[2] = Util.toByte(buffer.get(9));
			intArray[3] = Util.toByte(buffer.get(8));

			seqInfoCounter = Util.toInteger(intArray) * 2;
			state = State.WAITING_FOR_LINE_INFO;

		} else if (state == State.WAITING_FOR_LINE_INFO) {
			if (string.trim().startsWith("CData")) {
				// ignore

			} else if (string.trim().startsWith("Weight") && lastSeqInfoString != null) {
				double weight = Double.NaN;

				try {
					weight = Double.parseDouble(lastSeqInfoString.trim());
				} catch (NumberFormatException e) {
					// ignore
				}

				if (!Double.isNaN(weight)) {
					gotWeight = true;
					mapBuilder.put(InputParameter.Sample_Weight, weight);
				}

			} else if (string.trim().equals("Identifier 1") && lastSeqInfoString != null) {
				gotId1 = true;
				mapBuilder.put(InputParameter.Identifier_1, lastSeqInfoString.trim());

			} else if (string.trim().equals("Identifier 2") && lastSeqInfoString != null) {
				gotId2 = true;
				mapBuilder.put(InputParameter.Identifier_2, lastSeqInfoString.trim());

			} else if (string.trim().equals("Preparation") && lastSeqInfoString != null) {
				gotRun = true;
				mapBuilder.put(InputParameter.Run, lastSeqInfoString.trim());

			} else {
				lastSeqInfoString = string;
			}

			if ((gotWeight && gotId1 && gotId2 && gotRun) || --seqInfoCounter == 0 || string.trim().startsWith("CDualInletBlockData")) {
				state = State.NONE;
			}
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
