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

package org.easotope.client.core.widgets.graph.drawables;

import org.easotope.client.core.widgets.graph.CoordinateTransform;
import org.easotope.client.core.widgets.graph.DrawableObject;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class XRangeSelection extends DrawableObject {
	double x1 = Double.NaN;
	double x2 = Double.NaN;
	Color color = null;

	public XRangeSelection(Color color) {
		this.color = color;
	}

	public XRangeSelection(double x1, double x2, Color color) {
		this.x1 = x1;
		this.x2 = x2;
		this.color = color;
	}

	public void setX1(double x1) {
		this.x1 = x1;
	}

	public double getX1() {
		return x1;
	}

	public void setX2(double x2) {
		this.x2 = x2;
	}
	
	public double getX2() {
		return x2;
	}

	@Override
	public double getMaxX() {
		return Math.max(x1, x2);
	}

	@Override
	public double getMinX() {
		return Math.min(x1, x2);
	}

	@Override
	public double getMaxY() {
		return Double.NaN;
	}

	@Override
	public double getMinY() {
		return Double.NaN;
	}

	@Override
	public long squareOfDistanceFrom(int pixelX, int pixelY) {
		return Long.MAX_VALUE;
	}

	@Override
	public void draw(GC gc, int sizeX, int sizeY, CoordinateTransform coordinateTransform) {
		if (Double.isNaN(x1) || Double.isNaN(x2) || x1 == x2) {
			return;
		}

		int pixelX1 = (int) Math.floor(coordinateTransform.coordinateToPixel(x1, 0)[0]);
		int pixelX2 = (int) Math.floor(coordinateTransform.coordinateToPixel(x2, 0)[0]);
		int width = Math.abs(pixelX2-pixelX1) + 1;

		gc.setBackground(color);
		gc.fillRectangle(Math.min(pixelX1, pixelX2), 0, width, sizeY);
	}
}
