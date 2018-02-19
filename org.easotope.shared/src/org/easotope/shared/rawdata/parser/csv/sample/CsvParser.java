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

package org.easotope.shared.rawdata.parser.csv.sample;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.Vendor;
import org.easotope.shared.rawdata.parser.MapBuilder;
import org.easotope.shared.rawdata.parser.Parser;

public class CsvParser extends Parser {
	private static HashMap<String, InputParameter> aliases = new HashMap<String, InputParameter>();
	private MapBuilder mapBuilder = new MapBuilder();

	public CsvParser(boolean historicMode, String assumedTimeZone) {
		super(historicMode, assumedTimeZone);
	}
	
	public void parseFile(ByteArrayInputStream inputStream) {
		try {
			Properties properties = new Properties();
			properties.load(inputStream);
			
			for (String property : properties.stringPropertyNames()) {
				String value = properties.getProperty(property);

				Pattern arrayPattern = Pattern.compile("^\\s*(.*)\\[(\\d+)\\]\\s*$");
				Matcher matcher = arrayPattern.matcher(property);

				if (matcher.matches()) {
					String key = matcher.group(1);
					InputParameter parameter;
					int index = Integer.valueOf(matcher.group(2));
					
					if (aliases.containsKey(key)) {
						parameter = aliases.get(key);
					} else {
						parameter = InputParameter.valueOf(key);
					}

					if (parameter != null) {
						mapBuilder.put(parameter, index, value);
					}
					
				} else {
					String key = property.trim();
					InputParameter parameter;
					
					if (aliases.containsKey(key)) {
						parameter = aliases.get(key);
					} else {
						parameter = InputParameter.valueOf(key);
					}

					if (parameter != null) {
						mapBuilder.put(parameter, value);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			mapBuilder = null;
			return;
		}
	}

	@Override
	public ArrayList<HashMap<InputParameter,Object>> getResultsList() {
		if (mapBuilder == null) {
			return null;
		}

		ArrayList<HashMap<InputParameter,Object>> result = new ArrayList<HashMap<InputParameter,Object>>();
		result.add(mapBuilder.getMap());

		return result;
	}

	@Override
	public String getAssumedTimeZone() {
		return null;
	}

	@Override
	public String getDescription() {
		return "Comma Separated Values";
	}

	@Override
	public Vendor getVendor() {
		return Vendor.Unknown;
	}

	static {
		fileExtensions = new String[] { "csv" };
	}
}
