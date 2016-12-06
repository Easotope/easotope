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

package org.easotope.shared.rawdata.parser.thermo.scn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.parser.MapBuilder;
import org.easotope.shared.rawdata.parser.thermo.RollingBuffer;
import org.easotope.shared.rawdata.parser.thermo.StateMachine;
import org.easotope.shared.rawdata.parser.thermo.Util;

public class ScnStateMachine implements StateMachine {
	private final int INTEGER_SIZE = Integer.SIZE / Byte.SIZE;
	private final int DOUBLE_SIZE = Double.SIZE / Byte.SIZE;
	private final int FLOAT_SIZE = Float.SIZE / Byte.SIZE;

	private Pattern massPattern = Pattern.compile("Mass\\s+([\\d\\.]+)\\s+.*");
	private Pattern cupPattern = Pattern.compile("Cup\\s+([\\d]+)");

	private boolean historicMode;
	private int numberOfCups = -1;
	private ArrayList<double[]> measurements = new ArrayList<double[]>();
	private int massCounter = -1;
	private ArrayList<String> masses = new ArrayList<String>();
	private long javaDate = Long.MIN_VALUE;
	private float fromVoltage = Float.NaN;
	private float toVoltage = Float.NaN;

	private enum State {
		NONE,
		WAITING_FOR_CUPS_EMPTY1,
		WAITING_FOR_CUPS_EMPTY2,
		WAITING_FOR_CUPS_EMPTY3,
		WAITING_FOR_DATA,
		READING_MASSES
	};

	private State state = State.NONE;

	private Integer cbinaryPattern[] = {
			0x72, 0x79, 0x02, 0x00, 0x00, 0x00
	};

	private Integer cPlotRangePattern[] = {
			0x67, 0x65
	};

	private Integer dataPattern[] = {
			0x02, 0x00, 0x00, 0x00
	};

	ScnStateMachine(boolean historicMode) {
		this.historicMode = historicMode;
	}

	public void foundString(String string, RollingBuffer buffer) {
		byte[] intArray = new byte[INTEGER_SIZE];
		byte[] doubleArray = new byte[DOUBLE_SIZE];
		byte[] floatArray = new byte[FLOAT_SIZE];

		if (state == State.NONE && "CScanStorage".equals(string)) {
			state = State.WAITING_FOR_CUPS_EMPTY1;

		} else if (state == State.WAITING_FOR_CUPS_EMPTY1 && string.isEmpty()) {
			state = State.WAITING_FOR_CUPS_EMPTY2;

		} else if (state == State.WAITING_FOR_CUPS_EMPTY2 && string.isEmpty()) {
			state = State.WAITING_FOR_CUPS_EMPTY3;

		} else if (state == State.WAITING_FOR_CUPS_EMPTY3 && string.isEmpty()) {
			int offset = 4;

			intArray[0] = Util.toByte(buffer.get(offset + 3));
			intArray[1] = Util.toByte(buffer.get(offset + 2));
			intArray[2] = Util.toByte(buffer.get(offset + 1));
			intArray[3] = Util.toByte(buffer.get(offset + 0));
			numberOfCups = Util.toInteger(intArray);

			state = State.NONE;

		} else if (state == State.NONE && "CBinary".equals(string) && buffer.startsWith(cbinaryPattern) && numberOfCups != -1) {
			state = State.WAITING_FOR_DATA;

		} else if (state == State.WAITING_FOR_DATA && string.isEmpty() && buffer.startsWith(dataPattern)) {
			final int RECORD_LENGTH = INTEGER_SIZE + (numberOfCups * DOUBLE_SIZE);

			intArray[0] = Util.toByte(buffer.get(7));
			intArray[1] = Util.toByte(buffer.get(6));
			intArray[2] = Util.toByte(buffer.get(5));
			intArray[3] = Util.toByte(buffer.get(4));

			int firstBlockLength = Util.toInteger(intArray);
			int numberOfLines = firstBlockLength / RECORD_LENGTH;

			for (int cup=0; cup<numberOfCups; cup++) {
				measurements.add(new double[numberOfLines]);
			}

			for (int line=0; line<numberOfLines; line++) {
				for (int cup=0; cup<numberOfCups; cup++) {
					int offset = INTEGER_SIZE * 2 + line * RECORD_LENGTH + INTEGER_SIZE + cup * DOUBLE_SIZE;

					doubleArray[0] = Util.toByte(buffer.get(offset + 7));
					doubleArray[1] = Util.toByte(buffer.get(offset + 6));
					doubleArray[2] = Util.toByte(buffer.get(offset + 5));
					doubleArray[3] = Util.toByte(buffer.get(offset + 4));
					doubleArray[4] = Util.toByte(buffer.get(offset + 3));
					doubleArray[5] = Util.toByte(buffer.get(offset + 2));
					doubleArray[6] = Util.toByte(buffer.get(offset + 1));
					doubleArray[7] = Util.toByte(buffer.get(offset + 0));

					measurements.get(cup)[line] = Util.toDouble(doubleArray);
				}
			}

			state = State.NONE;

		} else if (state == State.NONE && "CPlotRange".equals(string) && buffer.startsWith(cPlotRangePattern) && numberOfCups != -1) {
			int offset = 2;
			floatArray[0] = Util.toByte(buffer.get(offset + 3));
			floatArray[1] = Util.toByte(buffer.get(offset + 2));
			floatArray[2] = Util.toByte(buffer.get(offset + 1));
			floatArray[3] = Util.toByte(buffer.get(offset + 0));

			fromVoltage = Util.toFloat(floatArray);

			offset = 6;
			floatArray[0] = Util.toByte(buffer.get(offset + 3));
			floatArray[1] = Util.toByte(buffer.get(offset + 2));
			floatArray[2] = Util.toByte(buffer.get(offset + 1));
			floatArray[3] = Util.toByte(buffer.get(offset + 0));

			toVoltage = Util.toFloat(floatArray);

			state = State.READING_MASSES;
			massCounter = numberOfCups;

		} else if (state == State.READING_MASSES) {
			Matcher massPatternMatcher = massPattern.matcher(string);
			Matcher cupPatternMatcher = cupPattern.matcher(string);

			if (massPatternMatcher.matches()) {
				masses.add(massPatternMatcher.group(1));

				if (--massCounter == 0) {
					intArray[0] = Util.toByte(buffer.get(3));
					intArray[1] = Util.toByte(buffer.get(2));
					intArray[2] = Util.toByte(buffer.get(1));
					intArray[3] = Util.toByte(buffer.get(0));

					javaDate = ((long) Util.toInteger(intArray)) * 1000;

					state = State.NONE;
				}
			}

			if (cupPatternMatcher.matches()) {
				if (--massCounter == 0) {
					intArray[0] = Util.toByte(buffer.get(3));
					intArray[1] = Util.toByte(buffer.get(2));
					intArray[2] = Util.toByte(buffer.get(1));
					intArray[3] = Util.toByte(buffer.get(0));

					javaDate = ((long) Util.toInteger(intArray)) * 1000;

					state = State.NONE;
				}
			}
		}
	}

	public HashMap<InputParameter, Object> getMap() {
		if (state != State.NONE) {
			Log.getInstance().log(Level.INFO, "State machine ended with state " + state + ".");
			return null;
		}

		if (masses.size() != 0 && measurements.size() != masses.size()) {
			Log.getInstance().log(Level.INFO, "Number of measurements (" + measurements.size() + ") does not match masses (" + masses.size() + ").");
			return null;
		}

		if (measurements.size() == 0) {
			Log.getInstance().log(Level.INFO, "Number of measurements is zero.");
			return null;
		}

		if (javaDate == Long.MIN_VALUE) {
			Log.getInstance().log(Level.INFO, "No date found.");
			return null;
		}

		if (fromVoltage == Float.NaN || toVoltage == Float.NaN) {
			Log.getInstance().log(Level.INFO, "No from or to voltages found.");
			return null;
		}

		if (masses.size() > 9) {
			Log.getInstance().log(Level.INFO, "Number of masses is too big (" + masses.size() + ")");
			return null;
		}

		MapBuilder mapBuilder = new MapBuilder();
		boolean alreadyDidChannelMap = false;

		if (historicMode) {
			// special historic case for Alvaro's old mass spec at the ETH

			String[] alvaroMasses = new String[] { "32.67", "29.33", "30.00", "30.67", "36.00", "31.33", "37.34", "32.00" };

			if (masses.size() == alvaroMasses.length) {
				boolean isAlvaro = true;

				for (int i=0; i<alvaroMasses.length; i++) {
					if (!alvaroMasses[i].equals(masses.get(i))) {
						isAlvaro = false;
						break;
					}
				}

				if (isAlvaro) {
					mapBuilder.put(InputParameter.ChannelToMZX10, 0, 490);
					mapBuilder.put(InputParameter.ChannelToMZX10, 1, 440);
					mapBuilder.put(InputParameter.ChannelToMZX10, 2, 450);
					mapBuilder.put(InputParameter.ChannelToMZX10, 3, 460);
					//mapBuilder.put(InputParameter.ChannelToMZX10, 4, 0);
					mapBuilder.put(InputParameter.ChannelToMZX10, 5, 470);
					//mapBuilder.put(InputParameter.ChannelToMZX10, 6, 0);
					mapBuilder.put(InputParameter.ChannelToMZX10, 7, 480);

					alreadyDidChannelMap = true;
				}
			}
		}

		if (masses.size() != 0 && !alreadyDidChannelMap) {
			for (int i=0; i<masses.size(); i++) {
				int mz = (int) Math.round(Double.parseDouble(masses.get(i)) * 10.0);
				mapBuilder.put(InputParameter.ChannelToMZX10, i, mz);
			}
		}

		for (int i=0; i<measurements.size(); i++) {
			InputParameter inputParameter = InputParameter.values()[InputParameter.Channel0_Scan.ordinal() + i];
			double[] values = measurements.get(i);

			for (int j=0; j<values.length; j++) {
				mapBuilder.put(inputParameter, j, values[j]);
			}
		}

		mapBuilder.put(InputParameter.Java_Date, javaDate);
		mapBuilder.put(InputParameter.ScanFromVoltage, fromVoltage);
		mapBuilder.put(InputParameter.ScanToVoltage, toVoltage);

		return mapBuilder.getMap();
	}
}
