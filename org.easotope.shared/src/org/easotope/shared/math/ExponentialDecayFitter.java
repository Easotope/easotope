/*
 * Copyright © 2016-2019 by Devon Bowen.
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
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class ExponentialDecayFitter {
	// Fits a, b, and c for the equation:
	//
	// y = a + b * Math.exp(c * x);
	//
	// Algorithm taken from pp 16-17 of document:
	// https://fr.scribd.com/doc/14674814/Regressions-et-equations-integrales
	//
	// See also:
	// http://stackoverflow.com/questions/3938042/fitting-exponential-decay-with-no-initial-guessing

	private ArrayList<Point> pointList = new ArrayList<Point>();
	private boolean needsCalculation = true;
	private double a = Double.NaN;
	private double b = Double.NaN;
	private double c = Double.NaN;

	public void addCoordinate(double x, double y) {
		pointList.add(new Point(x, y));
		needsCalculation = true;
	}

	public boolean isInvalid() {
		calculateIfNecessary();
		return a == Double.NaN;
	}

	public double getA() {
		calculateIfNecessary();
		return a;
	}

	public double getB() {
		calculateIfNecessary();
		return b;
	}

	public double getC() {
		calculateIfNecessary();
		return c;
	}

	public double getY(double x) {
		calculateIfNecessary();
		return a + b * Math.exp(c * x);
	}
	
	public double getDy(double x) {
		calculateIfNecessary();
		return b * c * Math.exp(c * x);
	}
	
	private void calculateIfNecessary() {
		if (!needsCalculation) {
			return;
		}

		a = b = c = Double.NaN;

		if (pointList.size() < 2) {
			return;
		}

		Point[] point = pointList.toArray(new Point[pointList.size()]);
		Arrays.sort(point, new PointComparator());
		
		double[] s = new double[point.length];
		s[0] = 0.0;

		for (int k=1; k<point.length; k++) {
			s[k] = s[k-1] + 0.5 * (point[k].y + point[k-1].y) * (point[k].x - point[k-1].x);
		}

		// calculate m

		RealMatrix m = MatrixUtils.createRealMatrix(2, 2);

		for (int k=0; k<point.length; k++) {
			m.setEntry(0, 0, m.getEntry(0, 0) + (point[k].x - point[0].x) * (point[k].x - point[0].x));
			m.setEntry(0, 1, m.getEntry(0, 1) + (point[k].x - point[0].x) * s[k]);
			m.setEntry(1, 1, m.getEntry(1, 1) + s[k] * s[k]);
		}

		m.setEntry(1, 0, m.getEntry(0, 1));

		// invert m

		try {
			m = MatrixUtils.inverse(m);
		} catch (Exception e) {
			return;
		}

		// calculate n

		RealMatrix n = MatrixUtils.createRealMatrix(2, 1);

		for (int k=0; k<point.length; k++) {
			n.setEntry(0, 0, n.getEntry(0, 0) + (point[k].y - point[0].y) * (point[k].x - point[0].x));
			n.setEntry(1, 0, n.getEntry(1, 0) + (point[k].y - point[0].y) * s[k]);
		}

		// calculate c

		c = m.multiply(n).getEntry(1, 0);

		// calculate m

		m = MatrixUtils.createRealMatrix(2, 2);
		m.setEntry(0, 0, point.length);

		for (int k=0; k<point.length; k++) {
			m.setEntry(0, 1, m.getEntry(0, 1) + Math.exp(c * point[k].x));
			m.setEntry(1, 1, m.getEntry(1, 1) + Math.exp(2.0 * c * point[k].x));
		}

		m.setEntry(1, 0, m.getEntry(0, 1));

		// invert m

		try {
			m = MatrixUtils.inverse(m);
		} catch (Exception e) {
			return;
		}
		
		// calculate n

		n = MatrixUtils.createRealMatrix(2, 1);

		for (int k=0; k<point.length; k++) {
			n.setEntry(0, 0, n.getEntry(0, 0) + point[k].y);
			n.setEntry(1, 0, n.getEntry(1, 0) + point[k].y * Math.exp(c * point[k].x));
		}

		// calculate a & b

		RealMatrix r = m.multiply(n);

		a = r.getEntry(0, 0);
		b = r.getEntry(1, 0);
	}

	public class Point {
		private double x;
		private double y;

		Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	public class PointComparator implements Comparator<Point> {
		@Override
		public int compare(Point o1, Point o2) {
			return Double.compare(o1.x, o2.x);
		}
	}

	public static void main(String[] args) {
		ExponentialDecayFitter ef = new ExponentialDecayFitter();

		ef.addCoordinate(0, 4.5);
		ef.addCoordinate(0.026, 4.42);
		ef.addCoordinate(0.052, 4.11);
		ef.addCoordinate(0.079, 4.01);
		ef.addCoordinate(0.105, 3.56);
		ef.addCoordinate(0.131, 3.32);
		ef.addCoordinate(0.157, 3.14);
		ef.addCoordinate(0.184, 3.32);
		ef.addCoordinate(0.211, 3.22);
		ef.addCoordinate(0.237, 2.99);
		ef.addCoordinate(0.263, 2.96);
		ef.addCoordinate(0.29, 2.54);
		ef.addCoordinate(0.315, 2.46);
		ef.addCoordinate(0.342, 2.44);
		ef.addCoordinate(0.369, 2.58);
		ef.addCoordinate(0.395, 2.42);
		ef.addCoordinate(0.422, 2.61);
		ef.addCoordinate(0.448, 2.41);
		ef.addCoordinate(0.473, 2.19);
		ef.addCoordinate(0.5, 2.52);

		// should print a = 1.896, b = 2.66, c = -3.882
		
		System.err.println("A = " + ef.getA());
		System.err.println("B = " + ef.getB());
		System.err.println("C = " + ef.getC());
	}
}
