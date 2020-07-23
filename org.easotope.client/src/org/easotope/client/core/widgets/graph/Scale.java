/*
 * Copyright © 2016-2020 by Devon Bowen.
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
import java.util.ArrayList;

public class Scale {
	private ArrayList<BigDecimal> scaleMarkers = new ArrayList<BigDecimal>();
	private double min;
	private double max;

	Scale(double min, double max, char decimalSeparator, char minusSign, String exponentSeparator) {
		this.min = min;
		this.max = max;

		ScaleMarker scaleMarkerMin = new ScaleMarker(min, decimalSeparator, minusSign, exponentSeparator);
		ScaleMarker scaleMarkerMax = new ScaleMarker(max, decimalSeparator, minusSign, exponentSeparator);

		if (!scaleMarkerMin.isValid() || !scaleMarkerMax.isValid()) {
			return;
		}

		int commonExponent = Math.max(scaleMarkerMin.getExponent(), scaleMarkerMax.getExponent());
		scaleMarkerMin.increaseExponentTo(commonExponent);
		scaleMarkerMax.increaseExponentTo(commonExponent);

		int commonMantissaLength = Math.max(scaleMarkerMin.getMantissaLength(), scaleMarkerMax.getMantissaLength());
		scaleMarkerMin.increaseMantissaLength(commonMantissaLength);
		scaleMarkerMax.increaseMantissaLength(commonMantissaLength);

		StringBuffer mantissaLower = scaleMarkerMin.getMantissa();
		StringBuffer mantissaUpper = scaleMarkerMax.getMantissa();

		int firstDiffChar = 0;

		if (!scaleMarkerMin.isNegative() || scaleMarkerMax.isNegative()) {
			while (firstDiffChar<mantissaLower.length() && mantissaLower.charAt(firstDiffChar) == mantissaUpper.charAt(firstDiffChar)) {
				firstDiffChar++;
			}

			if (firstDiffChar == mantissaLower.length()) {
				return;
			}
		}

		StringBuffer mantissa = new StringBuffer();

		for (int i=0; i<firstDiffChar; i++) {
			mantissa.append('0');
		}

		mantissa.append('1');

		// this can be made much more efficient by adding firstDiffChar to the exponent or some such

		ScaleMarker incrementScaleMarker = new ScaleMarker(false, mantissa, commonExponent);
		BigDecimal increment = incrementScaleMarker.getBigDecimal().multiply(new BigDecimal(2));

		StringBuffer mantissaCopy = new StringBuffer(mantissaLower);

		if (mantissaCopy.length() > firstDiffChar+1) {
			mantissaCopy.setLength(firstDiffChar+1);
		}

		while (mantissaCopy.length() < firstDiffChar+1) {
			mantissaCopy.append('0');
		}

		ScaleMarker startScaleMarker = new ScaleMarker(scaleMarkerMin.isNegative(), mantissaCopy, commonExponent);
		BigDecimal start = startScaleMarker.getBigDecimal().subtract(increment);

		scaleMarkers.clear();

		while (scaleMarkers.size() < 3) {
			scaleMarkers.clear();
			increment = increment.divide(new BigDecimal(2));

			for (BigDecimal current = start; current.doubleValue() < max; current = current.add(increment)) {
				if (current.doubleValue() < min) {
					continue;
				}

				scaleMarkers.add(current);
			}
		}
	}

	ArrayList<BigDecimal> getScaleMarkers() {
		return scaleMarkers;
	}

	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
}
