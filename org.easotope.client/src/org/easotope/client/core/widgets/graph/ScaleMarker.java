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

package org.easotope.client.core.widgets.graph;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;

class ScaleMarker {
	private boolean isValid;
	private boolean isNegative;
	private int exponent;
	private StringBuffer mantissa;

	ScaleMarker(boolean isNegative, StringBuffer mantissa, int exponent) {
		this.isValid = true;
		this.isNegative = isNegative;
		this.mantissa = mantissa;
		this.exponent = exponent;
	}

	ScaleMarker(double doubleValue, char decimalSeparator, char minusSign, String exponentSeparator) {
		if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
			isValid = false;
			return;
		}

		DecimalFormat decimalFormat = new DecimalFormat("0.##################E0");
		String original = decimalFormat.format(doubleValue);

		isValid = true;
		isNegative = (original.charAt(0) == minusSign);

		int eIndex = original.lastIndexOf(exponentSeparator);
		int eDigits = eIndex + exponentSeparator.length();
		boolean eIsNegative = false;

		if (original.charAt(eDigits) == minusSign) {
			eIsNegative = true;
			eDigits++;
		}

		for (int i=eDigits; i<original.length(); i++) {
			exponent *= 10;
			exponent += original.charAt(i) - '0';
		}

		if (eIsNegative) {
			exponent = -exponent;
		}

		mantissa = new StringBuffer();
		mantissa.append(original.charAt(isNegative ? 1 : 0));

		int indexAfterFirst = isNegative ? 2 : 1;

		if (original.charAt(indexAfterFirst) == decimalSeparator) {
			indexAfterFirst++;
		}

		exponent++;

		if (indexAfterFirst != eIndex) {
			mantissa.append(original.substring(indexAfterFirst, eIndex));
		}

		if (mantissa.length() == 1 && mantissa.charAt(0) == '0') {
			isNegative = false;
		}
	}

	public boolean isValid() {
		return isValid;
	}

	public boolean isNegative() {
		return isNegative;
	}

	public int getExponent() {
		return exponent;
	}

	public int getMantissaLength() {
		return mantissa.length();
	}

	public StringBuffer getMantissa() {
		return mantissa;
	}

	public void increaseExponentTo(int newExponent) {
		assert(newExponent >= exponent);

		if (exponent == newExponent) {
			return;
		}

		char[] chars = new char[newExponent - exponent];
		Arrays.fill(chars, '0');

		mantissa.insert(0, chars);
		exponent = newExponent;
	}

	public void increaseMantissaLength(int newMantissaLength) {
		assert(newMantissaLength >= mantissa.length());
		
		if (newMantissaLength == mantissa.length()) {
			return;
		}

		char[] chars = new char[newMantissaLength - mantissa.length()];
		Arrays.fill(chars, '0');
		mantissa.append(chars);
	}

	public BigDecimal getBigDecimal() {
		return new BigDecimal((isNegative ? "-0." : "0.") + mantissa.toString() + "E" + exponent);
	}

//	@Override
//	public String toString() {
//		boolean isZero = true;
//
//		for (int i=0; i<mantissa.length(); i++) {
//			if (mantissa.charAt(i) != '0') {
//				isZero = false;
//				break;
//			}
//		}
//
//		if (isZero) {
//			return "0" + decimalSeparator + "0";
//		}
//
//		StringBuffer answer = new StringBuffer();
//
//		if (isNegative) {
//			answer.append(minusSign);
//		}
//
//		answer.append(mantissa.charAt(0));
//		answer.append(decimalSeparator);
//
//		if (mantissa.length() != 1) {
//			answer.append(mantissa.substring(1));
//		} else {
//			answer.append('0');
//		}
//
//		if (exponent-1 != 0) {
//			answer.append(exponentSeparator);
//			answer.append(Integer.toString(exponent-1));
//		}
//
//		return answer.toString();
//	}
}
