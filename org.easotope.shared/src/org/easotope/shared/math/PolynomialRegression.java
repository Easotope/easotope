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

package org.easotope.shared.math;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;

public class PolynomialRegression implements PolynomialFitter {
	private int degree;
	private PolynomialCurveFitter fitter;
	private WeightedObservedPoints points = new WeightedObservedPoints();
	private double[] coefficients;

	public PolynomialRegression(int degree) {
		this.degree = degree;
		this.fitter = PolynomialCurveFitter.create(degree);
	}

	public void addCoordinate(double x, double y) {
		coefficients = null;
		points.add(x, y);
	}

	public boolean isInvalid() {
		if (coefficients == null) {
			try {
				coefficients = fitter.fit(points.toList());
			} catch (Exception e) {
				Log.getInstance().log(Level.DEBUG, this, "error while fitting", e);
			}
		}

		if (coefficients == null || coefficients.length != degree+1) {
			return true;
		}

		for (double coefficient : coefficients) {
			if (Double.isNaN(coefficient)) {
				return true;
			}
		}

		return false;
	}

	public double[] getCoefficients() {
		if (isInvalid()) {
			return null;
		}

		return coefficients;
	}
}