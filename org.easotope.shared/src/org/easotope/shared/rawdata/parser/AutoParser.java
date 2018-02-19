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

package org.easotope.shared.rawdata.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.Reflection;
import org.easotope.shared.Messages;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.parser.csv.sample.CsvParser;
import org.easotope.shared.rawdata.parser.nu.sample.NuParser;
import org.easotope.shared.rawdata.parser.properties.sample.PropertiesParser;
import org.easotope.shared.rawdata.parser.thermo.caf.ThermoCafParser;
import org.easotope.shared.rawdata.parser.thermo.did.ThermoDidParser;
import org.easotope.shared.rawdata.parser.thermo.scn.ThermoScnParser;

public class AutoParser {
	private static Class<?>[] parsers = { CsvParser.class, PropertiesParser.class, ThermoDidParser.class, ThermoCafParser.class, ThermoScnParser.class, NuParser.class };

	private static HashMap<String,Class<?>> fileExtensionMap = new HashMap<String,Class<?>>();
	private static HashMap<String,String> filterNameMap = new HashMap<String,String>();

	private static String[] fileExtensions;
	private static String[] filterNames;

	private Class<?> parserClass = null;
	private ArrayList<HashMap<InputParameter,Object>> map = null;
	private String usedAssumedTimeZone = null;
	private String error = null;

	public AutoParser(byte[] file, String filename, boolean historicMode, String assumedTimeZone) {
		Matcher matcher = Pattern.compile(".*\\.([^\\.]+)").matcher(filename);

		if (!matcher.matches()) {
			error = MessageFormat.format(Messages.autoParser_noFileExtension, new Object[] { filename });
			return;
		}

		String fileExtension = matcher.group(1).toLowerCase();
		parserClass = fileExtensionMap.get(fileExtension);

		if (parserClass == null) {
			error = MessageFormat.format(Messages.autoParser_unknownFileExtension, new Object[] { fileExtension });
			return;
		}

		Parser parser = constructObject(parserClass, historicMode, assumedTimeZone);
		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(file);

		try {
			parser.parseFile(byteArrayInputStream);
			map = parser.getResultsList();
			usedAssumedTimeZone = parser.getAssumedTimeZone();

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, this, e.getMessage(), e);
		}

		if (map == null) {
			error = MessageFormat.format(Messages.autoParser_fileNotValid, new Object[] { filename, parser.getDescription() });
		}

		try {
			byteArrayInputStream.close();
		} catch (IOException e) {
			// do nothing;
		}
	}

	public Class<?> getParserClass() {
		return parserClass;
	}

	public ArrayList<HashMap<InputParameter,Object>> getMap() {
		return map;
	}

	public String getAssumedTimeZone() {
		return usedAssumedTimeZone;
	}

	public String getError() {
		return error;
	}

	private static Parser constructObject(Class<?> clazz, boolean historicMode, String assumedTimeZone) {
		try {
			Object object = Reflection.createObject(clazz.getName(), historicMode, assumedTimeZone);
			return (Parser) object;
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, AutoParser.class, "Could not create parser " + clazz, e);
		}

		return null;
	}

	static {
		for (Class<?> parser : parsers) {
			Parser object = constructObject(parser, false, null);
			String[] fileExtensions = object.getFileExtensions();

			if (fileExtensions != null) {
				for (String fileExtension : fileExtensions) {
					fileExtensionMap.put(fileExtension.toLowerCase(), parser);
					filterNameMap.put(fileExtension.toLowerCase(), object.getDescription());
				}
			}
		}

		fileExtensions = fileExtensionMap.keySet().toArray(new String[] {});
		filterNames = new String[fileExtensions.length];
		
		for (int i=0; i<fileExtensions.length; i++) {
			filterNames[i] = filterNameMap.get(fileExtensions[i]);
		}
	}

	public static String[] getFileExtensions() {
		return fileExtensions;
	}

	public static String[] getFilterNames() {
		return filterNames;
	}
}
