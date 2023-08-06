/*
 * Copyright © 2016-2023 by Devon Bowen.
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

package org.easotope.client.core;


public class Transform2D {
	private double t11;
	private double t12;
	private double t13;
	private double t21;
	private double t22;
	private double t23;
	private final double t31 = 0.0d;
	private final double t32 = 0.0d;
	private final double t33 = 1.0d;

	private boolean inverseIsSet;

	private double i11;
	private double i12;
	private double i13;
	private double i21;
	private double i22;
	private double i23;
	//private final double i31 = 0.0d;
	//private final double i32 = 0.0d;
	//private final double i33 = 1.0d;

	public Transform2D() {
		identity();
	}

	public Transform2D(Transform2D that) {
		t11 = that.t11;
		t12 = that.t12;
		t13 = that.t13;
		t21 = that.t21;
		t22 = that.t22;
		t23 = that.t23;

		inverseIsSet = false;
	}

	public void identity() {
		t11 = 1.0d;
		t12 = 0.0d;
		t13 = 0.0d;
		t21 = 0.0d;
		t22 = 1.0d;
		t23 = 0.0d;

		inverseIsSet = false;
	}

	public void scale(double scaleX, double scaleY) {
		t11 *= scaleX;
		t12 *= scaleY;
		t21 *= scaleX;
		t22 *= scaleY;

		inverseIsSet = false;
	}

	public void rotate(double radians) {
		double cosine = Math.cos(radians);		
		t11 *= cosine;
		t22 *= cosine;

		double sine = Math.sin(radians);
		t12 *= -sine;
		t21 *= sine;
	}

	public void translate(double offsetX, double offsetY) {
		t13 += t11 * offsetX + t12 * offsetY;
		t23 += t21 * offsetX + t22 * offsetY;

		inverseIsSet = false;
	}

	public void transform(double[] pointArray) {
		double tempX = t11 * pointArray[0] + t12 * pointArray[1] + t13;
		double tempY = t21 * pointArray[0] + t22 * pointArray[1] + t23;
		
		pointArray[0] = tempX;
		pointArray[1] = tempY;
	}

	public void inverseTransform(double[] pointArray) {
		if (!inverseIsSet) {
			double det = t11 * (t33 * t22 - t32 * t23) - t21 * (t33 * t12 - t32 * t13) + t31 * (t23 * t12 - t22 * t13);

			i11 = (t33 * t22 - t32 * t23) / det;
			i12 = (-(t33 * t12 - t32 * t13)) / det;
			i13 = (t23 * t12 - t22 * t13) / det;
			i21 = (-(t33 * t21 - t31 * t23)) / det;
			i22 = (t33 * t11 - t31 * t13) / det;
			i23 = (-(t23 * t11 - t21 * t13)) / det;
			//i31 = (t32 * t21 - t31 * t22) / det;
			//i32 = -(t32 * t11 - t31 * t12) / det;
			//i33 = (t22 * t11 - t21 * t12) / det;

			inverseIsSet = true;
		}

		double tempX = i11 * pointArray[0] + i12 * pointArray[1] + i13;
		double tempY = i21 * pointArray[0] + i22 * pointArray[1] + i23;
		
		pointArray[0] = tempX;
		pointArray[1] = tempY;
	}
}
