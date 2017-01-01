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

package org.easotope.shared.rawdata.parser.nu.sample;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;

public class DateParser {
	private static BigInteger INTEGER_OVERFLOW = BigInteger.ONE.shiftLeft(32);
	private static BigInteger LONG_MAX_VALUE = new BigInteger(String.valueOf(Long.MAX_VALUE));
	private static BigInteger INTEGER_10000000 = new BigInteger("10000000");
	private static BigInteger INTEGER_11644473600 = new BigInteger("11644473600");
	private static BigInteger INTEGER_1000 = new BigInteger("1000");

	public static Long startedAnalysisTimeToJavaTime(String string, String assumedTimeZone) {
		// sample input:
		// "Started analysis at 12:51:06 AM on the Tuesday, August 16, 2016"

		Pattern myPattern = Pattern.compile("^\"Started analysis at (\\d{1,2}):(\\d{1,2}):(\\d{1,2})\\s(\\w{2})\\son\\sthe\\s\\w+,\\s(\\w+)\\s(\\d{1,2}),\\s(\\d{4})\"");
		Matcher matcher = myPattern.matcher(string);

		if (!matcher.matches()) {
			Log.getInstance().log(Level.INFO, DateParser.class, "Nu Instruments date line does not match regex: " + string);
			return null;
		}

		String hours = matcher.group(1);
		String minutes = matcher.group(2);
		String seconds = matcher.group(3);
		String amPm = matcher.group(4);
		String monthWord = matcher.group(5);
		String monthDay = matcher.group(6);
		String year = matcher.group(7);
		String dateAsString = monthWord + " " + monthDay + " " + year + " " + hours + ":" + minutes + ":" + seconds + " " + amPm;

		SimpleDateFormat formatter = new SimpleDateFormat("MMM d yyyy hh:mm:ss a", Locale.ENGLISH);
		formatter.setTimeZone(getTimeZone(assumedTimeZone));
		Date date = null;

		try {
			date = formatter.parse(dateAsString);
		} catch (ParseException e) {
			Log.getInstance().log(Level.INFO, DateParser.class, "Date built from Nu Instruments date line cannot be parsed. Original: " + string + " Built:" + dateAsString);
			return null;
		}

		return date.getTime();
	}

	public static TimeZone getTimeZone(String assumedTimeZone) {
		return (assumedTimeZone != null) ? TimeZone.getTimeZone(assumedTimeZone) : TimeZone.getDefault();
	}

	public static Long fileTimeLowHighToJavaTime(String string) {
		// sample input:
		// "UTC FileTimeLow",1304653696," UTC FileTimeHigh",30544724

		Pattern myPattern = Pattern.compile("^\"UTC FileTimeLow\",(-?\\d+),\" UTC FileTimeHigh\",(-?\\d+)");
		Matcher matcher = myPattern.matcher(string);

		if (!matcher.matches()) {
			Log.getInstance().log(Level.INFO, DateParser.class, "Nu Instruments date line does not match regex: " + string);
			return null;
		}

		String signedLowInteger = matcher.group(1);
		String signedHighInteger = matcher.group(2);

		try {
			return windowsFileTimeToJavaTime(signedLowInteger, signedHighInteger);
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, DateParser.class, "Problem building JavaTime from Nu Instruments date line: " + string, e);
			return null;
		}
	}

	private static Long windowsFileTimeToJavaTime(String signedLowInteger, String signedHighInteger) {
		BigInteger lowBigInt = new BigInteger(signedLowInteger);
		BigInteger highBigInt = new BigInteger(signedHighInteger);
		
		if (lowBigInt.compareTo(INTEGER_OVERFLOW) >= 0) {
			Log.getInstance().log(Level.INFO, DateParser.class, "Value from Nu Instruments date line is too big for a 32-bit integer: " + signedLowInteger);
			return null;
		}

		if (lowBigInt.compareTo(BigInteger.ZERO) < 0) {
			lowBigInt = lowBigInt.add(INTEGER_OVERFLOW);
		}

		if (highBigInt.compareTo(INTEGER_OVERFLOW) >= 0) {
			Log.getInstance().log(Level.INFO, DateParser.class, "Value from Nu Instruments date line is too big for a 32-bit integer: " + signedHighInteger);
			return null;
		}

		if (highBigInt.compareTo(BigInteger.ZERO) < 0) {
			highBigInt = highBigInt.add(INTEGER_OVERFLOW);
		}

		BigInteger filetime = highBigInt.shiftLeft(32).or(lowBigInt);
		BigInteger unixTime = filetime.divide(INTEGER_10000000).subtract(INTEGER_11644473600);
		BigInteger javaTime = unixTime.multiply(INTEGER_1000);

		if (javaTime.compareTo(LONG_MAX_VALUE) > 0) {
			Log.getInstance().log(Level.INFO, DateParser.class, "Value built from Nu Instruments date line is too big for a 64-bit long: " + javaTime);
			return null;
		}

		return javaTime.longValue();
	}
}
