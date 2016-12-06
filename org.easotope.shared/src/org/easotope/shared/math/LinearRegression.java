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

import java.util.ArrayList;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class LinearRegression implements PolynomialFitter {
	private boolean minimizeHorizontalLeastSquares = false;
	private SimpleRegression regression = new SimpleRegression();
	private ArrayList<Point> pointList = new ArrayList<Point>();

	public LinearRegression() {
		this.minimizeHorizontalLeastSquares = false;
	}

	public LinearRegression(boolean minimizeHorizontalLeastSquares) {
		this.minimizeHorizontalLeastSquares = minimizeHorizontalLeastSquares;
	}

	public void addCoordinate(double x, double y) {
		if (minimizeHorizontalLeastSquares) {
			pointList.add(new Point(y, x));
			regression.addData(y, x);
		} else {
			pointList.add(new Point(x, y));
			regression.addData(x, y);
		}
	}

	public int getNumPoints() {
		return pointList.size();
	}

	public ArrayList<Point> getPoints() {
		return pointList;
	}

	public boolean isInvalid() {
		if (minimizeHorizontalLeastSquares && regression.getSlope() == 0.0d || Double.isNaN(regression.getSlope())) {
			return true;
		}

		return Double.isNaN(regression.getRSquare());
	}

	public double getSlope() {
		if (minimizeHorizontalLeastSquares) {
			return 1.0d / regression.getSlope();
		} else {
			return regression.getSlope();
		}
	}

	public double getIntercept() {
		if (minimizeHorizontalLeastSquares) {
			return -regression.getIntercept() / regression.getSlope();
		} else {
			return regression.getIntercept();
		}
	}

	public double[] getCoefficients() {
		return new double[] { getIntercept(), getSlope() };
	}

	public double getR2() {
		return regression.getRSquare();
	}

	public class Point {
		double x;
		double y;

		Point(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}
	}
}