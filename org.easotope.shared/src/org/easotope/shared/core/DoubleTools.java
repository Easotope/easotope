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

package org.easotope.shared.core;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;

public class DoubleTools {
	private static char superMinus = '⁻';
	private static char superDigits[] = { '⁰', '¹', '²', '³', '⁴', '⁵', '⁶', '⁷', '⁸', '⁹' };
	private static HashMap<Character,Character> superCharToNormalChar = new HashMap<Character,Character>();

	public static String format(double number, String format) {
		DecimalFormat decimalFormat = new DecimalFormat(format);
		decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
		return decimalFormat.format(number);
	}

	public static String format(double number, boolean leadingExponent, boolean forceExponent) {
		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();

		if (Double.isNaN(number)) {
			return decimalFormatSymbols.getNaN();
		}

		if (Double.isInfinite(number)) {
			return (number > 0) ? decimalFormatSymbols.getInfinity() : decimalFormatSymbols.getMinusSign() + decimalFormatSymbols.getInfinity();
		}

		String string = null;

		if (forceExponent) {
			string = new DecimalFormat("0.##########################E0").format(number);
		} else {
			string = String.valueOf(number);
		}

		if (leadingExponent) {
			String e = decimalFormatSymbols.getExponentSeparator();
			int eIndex = string.lastIndexOf(e);

			if (eIndex != -1) {
				StringBuffer buf = new StringBuffer();

				for (int i=eIndex+e.length(); i<string.length(); i++) {
					char ch = string.charAt(i);

					if (ch == decimalFormatSymbols.getMinusSign()) {
						buf.append(superMinus);
					} else {
						buf.append(superDigits[ch - '0']);
					}
				}

				buf.append(' ');
				buf.append(string.substring(0, eIndex));

				string = buf.toString();
			}
		}

		return string;
	}

	public static String removeLeadingExponent(String string) {
		if (string == null || string.length() == 0) {
			return string;
		}

		int index = 0;
		Character normalChar = superCharToNormalChar.get(string.charAt(index));
		int spaceIndex = string.indexOf(' ');

		if (normalChar == null || spaceIndex == -1) {
			return string;
		}

		StringBuffer buf = new StringBuffer(string.substring(spaceIndex + 1));
		buf.append(new DecimalFormatSymbols().getExponentSeparator());
		buf.append(normalChar);

		while (++index < spaceIndex) {
			normalChar = superCharToNormalChar.get(string.charAt(index));
			buf.append((normalChar != null) ? normalChar : string.charAt(index));
		}

		return buf.toString();
	}

	static {
		superCharToNormalChar.put(superMinus, '-');

		for (int i=0; i<superDigits.length; i++) {
			superCharToNormalChar.put(superDigits[i], Character.forDigit(i, 10));
		}
	}
}
