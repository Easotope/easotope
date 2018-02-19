/*
 * Copyright © 2016-2018 by Devon Bowen.
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

package org.easotope.shared.rawdata.parser.properties.sample;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.InputParameterType;
import org.easotope.shared.rawdata.Vendor;
import org.easotope.shared.rawdata.parser.MapBuilder;
import org.easotope.shared.rawdata.parser.Parser;

public class PropertiesParser extends Parser {
	HashMap<InputParameter,Object> map = null;

	public PropertiesParser(boolean historicMode, String assumedTimeZone) {
		super(historicMode, assumedTimeZone);
	}

	public void parseFile(ByteArrayInputStream inputStream) {
		try {
			MapBuilder mapBuilder = new MapBuilder();
			TreeSet<Integer> seenMzX10s = new TreeSet<Integer>();

			Properties properties = new Properties();
			properties.load(inputStream);

			Pattern arrayPattern = Pattern.compile("^\\s*(.*)\\[(\\d+)\\]\\s*$");

			for (String property : properties.stringPropertyNames()) {
				String value = properties.getProperty(property);
				Matcher arrayPatternMatcher = arrayPattern.matcher(property);
				InputParameter parameter = null;

				if (arrayPatternMatcher.matches()) {
					String key = arrayPatternMatcher.group(1);
					parameter = InputParameter.valueOf(key);

					if (parameter == null) {
						new Exception("unknown key " + key);
					}

					int index = Integer.valueOf(arrayPatternMatcher.group(2));
					mapBuilder.put(parameter, index, value);

				} else {
					String key = property.trim();
					parameter = InputParameter.valueOf(key);

					if (parameter == null) {
						new Exception("unknown key " + key);
					}

					if (parameter == InputParameter.Java_Date) {
						boolean isNumber = true;
						String trimmed = value.trim();

						for (int i=0; i<trimmed.length(); i++) {
							if (trimmed.charAt(i) < '0' || trimmed.charAt(i) > '9') {
								isNumber = false;
								break;
							}
						}

						if (!isNumber) {
							try {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS zzz");
								value = ((Long) sdf.parse(value).getTime()).toString();

							} catch (ParseException e) {
								// ignore
							}
						}
					}

					mapBuilder.put(parameter, value);
				}

				InputParameterType inputParameterType = parameter.getInputParameterType();

				if (parameter.getMzX10() != null && (inputParameterType == InputParameterType.Background || inputParameterType == InputParameterType.SampleMeasurement || inputParameterType == InputParameterType.RefMeasurement || inputParameterType == InputParameterType.Scan)) {
					seenMzX10s.add(parameter.getMzX10());
				}
			}

			map = mapBuilder.getMap();

			if (!map.containsKey(InputParameter.ChannelToMZX10)) {
				HashMap<Integer,Integer> mzX10ToChannel = new HashMap<Integer,Integer>();

				int count = 0;
				for (Integer mzX10 : seenMzX10s) {
					mzX10ToChannel.put(mzX10, count++);
				}

				for (InputParameter inputParameter : new ArrayList<InputParameter>(map.keySet())) {
					InputParameter newInputParameter = null;
					Integer channel = mzX10ToChannel.get(inputParameter.getMzX10());
	
					if (channel != null) {
						switch (inputParameter.getInputParameterType()) {
							case RefMeasurement:
								newInputParameter = InputParameter.values()[InputParameter.Channel0_Ref.ordinal() + channel];
								break;
	
							case SampleMeasurement:
								newInputParameter = InputParameter.values()[InputParameter.Channel0_Sample.ordinal() + channel];
								break;
	
							case Background:
								newInputParameter = InputParameter.values()[InputParameter.Channel0_Background.ordinal() + channel];
								break;
	
							default:
								break;
						}
					}
	
					if (newInputParameter != null) {
						Object object = map.remove(inputParameter);
						map.put(newInputParameter, object);
					}
				}

				map.put(InputParameter.ChannelToMZX10, new Vector<Integer>(seenMzX10s));
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, this, "Error while parsing properties file", e);
			map = null;
			return;
		}
	}

	@Override
	public ArrayList<HashMap<InputParameter,Object>> getResultsList() {
		if (map == null) {
			return null;
		}

		ArrayList<HashMap<InputParameter,Object>> result = new ArrayList<HashMap<InputParameter,Object>>();
		result.add(map);

		return result;
	}
	
	@Override
	public String getAssumedTimeZone() {
		return null;
	}

	@Override
	public String getDescription() {
		return "Java Properties";
	}

	@Override
	public Vendor getVendor() {
		return Vendor.Unknown;
	}

	static {
		fileExtensions = new String[] { "properties" };
	}
}
