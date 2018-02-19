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

package org.easotope.shared.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.easotope.shared.Messages;

public class DateFormat {
	public static String format(long date, String zone, boolean withTimeZone, boolean withSeconds) {
		String format = "yyyy-MM-dd HH:mm";

		if (withSeconds) {
			format += ":ss";
		}

		if (withTimeZone) {
			format += " z";
		}

		SimpleDateFormat formatter = new SimpleDateFormat(format, new Locale(Messages.locale));
	    formatter.setTimeZone(zone == null ? TimeZone.getDefault() : TimeZone.getTimeZone(zone));
	    return formatter.format(new Date(date));
	}

//	public static Date parse(String string, String zone, boolean withSeconds) throws ParseException {
//		String format = "yyyy-MM-dd HH:mm";
//
//		if (withSeconds) {
//			format += ":ss";
//		}
//
//		SimpleDateFormat formatter = new SimpleDateFormat(format, new Locale(Messages.locale));
//	    formatter.setTimeZone(zone == null ? TimeZone.getDefault() : TimeZone.getTimeZone(zone));
//
//		return formatter.parse(string);
//	}
}
