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

package org.easotope.client.core.widgets.graph.drawables;

import org.easotope.client.core.PointDesign;
import org.easotope.client.core.widgets.graph.CoordinateTransform;
import org.easotope.client.core.widgets.graph.DrawableObject;
import org.eclipse.swt.graphics.GC;

public class Point extends DrawableObject {
	private double x;
	private double y;
	private PointDesign pointDesign;

	private int pixelX;
	private int pixelY;

	public Point(double x, double y, PointDesign pointDesign) {
		this.x = x;
		this.y = y;
		this.pointDesign = pointDesign;
	}

	public void setX(double x) {
		this.x = x;
	}

	@Override
	public double getMaxX() {
		return x;
	}

	@Override
	public double getMinX() {
		return x;
	}

	@Override
	public double getMaxY() {
		return y;
	}

	@Override
	public double getMinY() {
		return y;
	}

	@Override
	public long squareOfDistanceFrom(int pixelX, int pixelY) {
		long xDistance = (this.pixelX - pixelX);
		long yDistance = (this.pixelY - pixelY);

		return (xDistance * xDistance) + (yDistance * yDistance);
	}

	@Override
	public void draw(GC gc, int sizeX, int sizeY, CoordinateTransform coordinateTransform) {
		double[] pixelValues = coordinateTransform.coordinateToPixel(x, y);
		pixelX = (int) Math.floor(pixelValues[0]);
		pixelY = (int) Math.floor(pixelValues[1]);

		if (pointDesign != null) {
			pointDesign.draw(gc, pixelX, pixelY, false);
		}
	}
}
