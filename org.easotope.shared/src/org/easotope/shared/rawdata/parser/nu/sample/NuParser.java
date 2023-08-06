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

package org.easotope.shared.rawdata.parser.nu.sample;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.Vendor;
import org.easotope.shared.rawdata.parser.MapBuilder;
import org.easotope.shared.rawdata.parser.Parser;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;

public class NuParser extends Parser {
	enum State { DEFAULT, READ_CHANNELS, READ_ZEROS, LOOKING_FOR_DATA, READ_REF_DATA, READ_SAM_DATA };

	private DataPatterns dataPatterns = new DataPatterns(false);
	private Long oldDate = null;
	private Long newDate = null;
	private Boolean isLidi2 = false;
	private String sourceWeightString = null;
	private Double sampleWeight = null;
	private String sampleName = null;
	private ArrayList<CycleMeasurements> cycleMeasurementsList = new ArrayList<CycleMeasurements>();
	private TimeZone usedTimeZone = null;
	ArrayList<Double[]> zeros = new ArrayList<Double[]>();

	public NuParser(boolean historicMode, String assumedTimeZone) {
		super(historicMode, assumedTimeZone);
	}

	@Override
	public void parseFile(ByteArrayInputStream byteArrayInputStream) {
		int numBlocks = 0;
		int numChannels = 0;

		State state = State.DEFAULT;
		BufferedReader reader = new BufferedReader(new InputStreamReader(byteArrayInputStream));
		String line = null;

		try {
			line = reader.readLine();
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, NuParser.class, "Exception reading Nu file.", e);
			cycleMeasurementsList = null;
			return;
		}

		int currentCycle = 0;
		int currentChannel = 0;
		int cycleLength = -1;
		Long cycleTimestamp = 0L;
		int zeroMeasurementLength = -1;

		while (line != null) {
			if (state == State.DEFAULT) {
				final String SAMPLE_NAME = "\"Sample_Name\",";
				final String SAMPLE_WEIGHT = "\"Sample_Weight\",";
				final String STARTING_ANALYSIS_AT = "\"Started analysis at";
				final String UTC = "\"UTC FileTimeLow\"";
				final String NUM_BLOCKS = "\"Num Blocks\",";
				final String ZERO_DATA = "Zero Data";
				final String INDIVIDUAL_DATA = "Individual Data";
				final String LIDI_FORMAT = "\"LIDI data format";

				if (line.startsWith(STARTING_ANALYSIS_AT)) {
					oldDate = DateParser.startedAnalysisTimeToJavaTime(line, assumedTimeZone);

				} else if (line.startsWith(UTC)) {
					newDate = DateParser.fileTimeLowHighToJavaTime(line);

				} else if (line.startsWith(SAMPLE_NAME)) {
					String newSourceName = line.substring(SAMPLE_NAME.length()).trim();

					if (newSourceName.startsWith("\"") && newSourceName.endsWith("\"")) {
						newSourceName = newSourceName.substring(1, newSourceName.length()-1).trim();
					}

					if (sampleName != null && !sampleName.equals(newSourceName)) {
						Log.getInstance().log(Level.INFO, NuParser.class, "Conflicting source names. old=" + sampleName + " new=" + newSourceName);
						cycleMeasurementsList = null;
						return;
					}

					sampleName = newSourceName;

				} else if (line.startsWith(SAMPLE_WEIGHT) && !line.endsWith("-999")) {
					String newSourceWeightString = line.substring(SAMPLE_WEIGHT.length()).trim();

					if (sourceWeightString != null && !sourceWeightString.equals(newSourceWeightString)) {
						Log.getInstance().log(Level.INFO, NuParser.class, "Conflicting source weights. line=" + line + " old=" + sourceWeightString + " new=" + newSourceWeightString);
						cycleMeasurementsList = null;
						return;
					}

					if (sourceWeightString == null) {
						sourceWeightString = newSourceWeightString;

						try {
							sampleWeight = Double.parseDouble(sourceWeightString);
						} catch (NumberFormatException e) {
							Log.getInstance().log(Level.INFO, NuParser.class, "Could not parse source weight from line: " + line, e);
						}
					}

				} else if (line.startsWith(NUM_BLOCKS)) {
					String numBlocksString = line.substring(NUM_BLOCKS.length());

					try {
						numBlocks = Integer.parseInt(numBlocksString);
					} catch (NumberFormatException e) {
						Log.getInstance().log(Level.INFO, NuParser.class, "Could not parse number of blocks from line: " + line, e);
						cycleMeasurementsList = null;
						return;
					}

					if (isLidi2 && numBlocks != 1) {
						Log.getInstance().log(Level.INFO, NuParser.class, "LIDI2 files must only contain 1 block but found " + numBlocks + " blocks.");
						cycleMeasurementsList = null;
						return;
					}
					
					state = State.READ_CHANNELS;

				} else if (line.startsWith(ZERO_DATA)) {
					state = State.READ_ZEROS;

				} else if (line.startsWith(INDIVIDUAL_DATA)) {
					if (numBlocks == 0) {
						Log.getInstance().log(Level.INFO, NuParser.class, "Data starts before numBlocks is defined.");
						cycleMeasurementsList = null;
						return;
					} else {
						cycleMeasurementsList.add(new CycleMeasurements(isLidi2));
						currentCycle = 0;
						currentChannel = 0;
						cycleLength = -1;
						zeroMeasurementLength = -1;
						state = State.LOOKING_FOR_DATA;
					}

				} else if (line.startsWith(LIDI_FORMAT) && line.contains("#TRUE#")) {
					isLidi2 = true;
					dataPatterns = new DataPatterns(true);

					if (numBlocks != 0 && numBlocks != 1) {
						Log.getInstance().log(Level.INFO, NuParser.class, "LIDI2 files must only contain 1 block but found " + numBlocks + " blocks.");
						cycleMeasurementsList = null;
						return;
					}
				}

			} else if (state == State.READ_CHANNELS) {
				if ("0".equals(line.trim())) {
					// ignore

				} else if ("-1".equals(line.trim())) {
					numChannels++;

				} else {
					state = State.DEFAULT;
				}

			} else if (state == State.READ_ZEROS) {
				ArrayList<Double> tmpList = new ArrayList<Double>();
				String[] numbers = line.split(",");

				for (int i=0; i<numbers.length-1; i++) {
					try {
						tmpList.add(Double.parseDouble(numbers[i]));
					} catch (NumberFormatException e) {
						Log.getInstance().log(Level.INFO, NuParser.class, "Could not parse zero value: " + numbers[i], e);
						cycleMeasurementsList = null;
						return;
					}
				}

				zeros.add(tmpList.toArray(new Double[tmpList.size()]));
				state = State.DEFAULT;

			} else if (state == State.LOOKING_FOR_DATA) {
				final String CYCLE_LENGTH = "Cycle_Length";
				final String ZERO_MEASUREMENT_LENGTH = "Zero_Measurement_Length";
				final String INDIVIDUAL_DATA = "Individual Data";

				if (line.startsWith(CYCLE_LENGTH)) {
					try {
						cycleLength = Integer.parseInt(line.substring(CYCLE_LENGTH.length()).trim());
					} catch (NumberFormatException e) {
						// ignore
					}

				} else if (line.startsWith(ZERO_MEASUREMENT_LENGTH)) {
					try {
						zeroMeasurementLength = Integer.parseInt(line.substring(ZERO_MEASUREMENT_LENGTH.length()).trim());
					} catch (NumberFormatException e) {
						// ignore
					}

				} else if (dataPatterns.matchesRefPattern(line)) {
					if (numChannels == 0) {
						Log.getInstance().log(Level.INFO, NuParser.class, "Data started without numChannels.");
						cycleMeasurementsList = null;
						return;
					}

					if (cycleLength != zeroMeasurementLength) {
						Log.getInstance().log(Level.INFO, NuParser.class, "Cycle length " + cycleLength + " and zero data length " + zeroMeasurementLength + " are not compatible.");
						return;
					}

					if (isLidi2) {
						cycleTimestamp = DateParser.lidiCycleHeaderTimeToJavaTime(line, assumedTimeZone);

						if (cycleTimestamp == null) {
							cycleMeasurementsList = null;
							return;
						}
						
						currentCycle++;
					}

					state = State.READ_REF_DATA;

				} else if (dataPatterns.matchesSamPattern(line)) {
					if (numChannels == 0) {
						Log.getInstance().log(Level.INFO, NuParser.class, "Data started without numChannels.");
						cycleMeasurementsList = null;
						return;
					}

					if (cycleLength != zeroMeasurementLength) {
						Log.getInstance().log(Level.INFO, NuParser.class, "Cycle length " + cycleLength + " and zero data length " + zeroMeasurementLength + " are not compatible.");
						return;
					}

					if (isLidi2) {
						cycleTimestamp = DateParser.lidiCycleHeaderTimeToJavaTime(line, assumedTimeZone);

						if (cycleTimestamp == null) {
							cycleMeasurementsList = null;
							return;
						}
					}

					currentCycle++;
					state = State.READ_SAM_DATA;

				} else if (dataPatterns.matchesEndPattern(line)) {
					state = State.DEFAULT;

				} else if (line.startsWith(INDIVIDUAL_DATA)) {
					cycleMeasurementsList.add(new CycleMeasurements(isLidi2));
					currentCycle = 0;
					currentChannel = 0;
					cycleLength = -1;
					zeroMeasurementLength = -1;
				}

			} else if (state == State.READ_REF_DATA || state == State.READ_SAM_DATA) {
				double sum = 0.0d;
				int count = 0;

				String[] numbers = line.split("\\s+");

				for (int i=0; i<numbers.length; i++) {
					String number = numbers[i];

					if (!isLidi2 && i == 0) {
						if (!"0.000000E+00".equals(number)) {
							Log.getInstance().log(Level.INFO, NuParser.class, "First number is not zero: " + number);
							cycleMeasurementsList = null;
							return;
						}

					} else {
						try {
							sum += Double.parseDouble(number);
							count++;
						} catch (NumberFormatException e) {
							Log.getInstance().log(Level.INFO, NuParser.class, "Exception parsing double: " + number, e);
							cycleMeasurementsList = null;
							return;
						}
					}
				}

				double average = sum / count;

				int currentBlockNum = cycleMeasurementsList.size()-1;
				Double[] zerosForBlock = null;

				if (numBlocks == zeros.size()) {
					zerosForBlock = zeros.get(currentBlockNum);
				} else if (zeros.size() != 0) {
					zerosForBlock = zeros.get(0);
				}

				if (zerosForBlock != null && zerosForBlock.length > currentChannel && zerosForBlock[currentChannel] != null) {
					average -= zerosForBlock[currentChannel];
				}

				InputParameter parameter = (state == State.READ_REF_DATA) ? InputParameter.Channel0_Ref : InputParameter.Channel0_Sample;
				parameter = InputParameter.values()[parameter.ordinal() + currentChannel];

				cycleMeasurementsList.get(currentBlockNum).addCycle(parameter, currentCycle, average, cycleTimestamp);

				if (++currentChannel == numChannels) {
					currentChannel = 0;
					state = State.LOOKING_FOR_DATA;
				}
			}

			try {
				line = reader.readLine();
			} catch (IOException e) {
				Log.getInstance().log(Level.INFO, NuParser.class, "Exception reading Nu file.", e);
				cycleMeasurementsList = null;
				return;
			}
		}

		if (state != State.DEFAULT) {
			Log.getInstance().log(Level.INFO, NuParser.class, "File ended while not in default state. State was " + state);
			cycleMeasurementsList = null;
			return;
		}

		// DO SOME MORE SANITY CHECKS HERE
	}

	@Override
	public ArrayList<HashMap<InputParameter,Object>> getResultsList() {
		if (cycleMeasurementsList == null) {
			return null;
		}

		ArrayList<HashMap<InputParameter,Object>> result = new ArrayList<HashMap<InputParameter,Object>>();

		int currentBlockNum = 0;
		for (CycleMeasurements cycleMeasurements : cycleMeasurementsList) {
			MapBuilder mapBuilder = cycleMeasurements.getAsMapBuilder();
			HashMap<InputParameter,Object> map = mapBuilder.getMap();

			Double[] zerosForBlock = null;

			if (zeros.size() == cycleMeasurementsList.size()) {
				zerosForBlock = zeros.get(currentBlockNum);
			} else if (zeros.size() != 0) {
				zerosForBlock = zeros.get(0);
			}

			if (zerosForBlock != null) {
				for (int channel=0; channel<zerosForBlock.length; channel++) {
					Double d = zerosForBlock[channel];

					if (d != null) {
						map.put(InputParameter.values()[InputParameter.Channel0_Background.ordinal() + channel], d);
					}
				}
			}

			if (isLidi2) {
				map.put(InputParameter.Data_Format, AcquisitionParsedV2.DataFormat.LIDI2);
			}
			
			if (newDate != null) {
				map.put(InputParameter.Java_Date, newDate + (currentBlockNum * 1000));
			} else if (oldDate != null) {
				map.put(InputParameter.Java_Date, oldDate + (currentBlockNum * 1000));
				usedTimeZone = DateParser.getTimeZone(assumedTimeZone); 
			}

			if (sampleWeight != null) {
				map.put(InputParameter.Sample_Weight, sampleWeight + " | " + cycleMeasurementsList.size());
			}

			if (sampleName != null) {
				map.put(InputParameter.Sample_Name, sampleName);
			}

			result.add(map);
			currentBlockNum++;
		}

		return result;
	}

	@Override
	public String getAssumedTimeZone() {
		return (usedTimeZone == null) ? null : usedTimeZone.getID();
	}

	@Override
	public String getDescription() {
		return "Nu Instruments";
	}

	@Override
	public Vendor getVendor() {
		return Vendor.Nu;
	}

	static {
		fileExtensions = new String[] { "txt" };
	}
}
