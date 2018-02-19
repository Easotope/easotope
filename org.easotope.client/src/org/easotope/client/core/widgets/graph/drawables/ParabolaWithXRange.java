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

import org.easotope.client.core.widgets.graph.CoordinateTransform;
import org.easotope.client.core.widgets.graph.DrawableObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class ParabolaWithXRange extends DrawableObject {
	private static final int PIXELS_BETWEEN_POINTS = 10;

	private double x2Coeff;
	private double x1Coeff;
	private double x0Coeff;
	private double xWithslope0;
	private double yAtXWithSlope0;
	private double x1;
	private double x2;
	private double yAtX1;
	private double yAtX2;
	private Color color;
	private int lineStyle;

	public ParabolaWithXRange(double x2Coeff, double x1Coeff, double x0Coeff, double x1, double x2, Color color, String[] tooltip) {
		init(x2Coeff, x1Coeff, x0Coeff, x1, x2, color, SWT.NONE, tooltip);
	}

	public ParabolaWithXRange(double x2Coeff, double x1Coeff, double x0Coeff, double x1, double x2, Color color, int lineStyle, String[] tooltip) {
		init(x2Coeff, x1Coeff, x0Coeff, x1, x2, color, lineStyle, tooltip);
	}

	public void init(double x2Coeff, double x1Coeff, double x0Coeff, double x1, double x2, Color color, int lineStyle, String[] tooltip) {
		this.x2Coeff = x2Coeff;
		this.x1Coeff = x1Coeff;
		this.x0Coeff = x0Coeff;
		this.xWithslope0 = -x1Coeff / (x2Coeff * 2);
		this.yAtXWithSlope0 = x2Coeff * xWithslope0 * xWithslope0 + x1Coeff * xWithslope0 + x0Coeff;

		if (x1 < x2) {
			this.x1 = x1;
			this.x2 = x2;
		} else {
			this.x1 = x2;
			this.x2 = x1;
		}

		this.yAtX1 = x2Coeff * x1 * x1 + x1Coeff * x1 + x0Coeff;
		this.yAtX2 = x2Coeff * x2 * x2 + x1Coeff * x2 + x0Coeff;
		this.color = color;
		this.lineStyle = lineStyle;
		setTooltip(tooltip);
	}

	@Override
	public double getMaxX() {
		return x2;
	}

	@Override
	public double getMinX() {
		return x1;
	}

	@Override
	public double getMaxY() {
		double maxY = (yAtX1 > yAtX2) ? yAtX1 : yAtX2;

		if (!Double.isNaN(yAtXWithSlope0) && xWithslope0 > x1 && xWithslope0 < x2) {
			maxY = (maxY > yAtXWithSlope0) ? maxY : yAtXWithSlope0;
		}

		return maxY;
	}

	@Override
	public double getMinY() {
		double minY = (yAtX1 < yAtX2) ? yAtX1 : yAtX2;

		if (!Double.isNaN(yAtXWithSlope0) && xWithslope0 > x1 && xWithslope0 < x2) {
			minY = (minY < yAtXWithSlope0) ? minY : yAtXWithSlope0;
		}

		return minY;
	}

	@Override
	public long squareOfDistanceFrom(int pixelX, int pixelY) {
		return Long.MAX_VALUE;
	}

	@Override
	public void draw(GC gc, int sizeX, int sizeY, CoordinateTransform coordinateTransform) {
		gc.setForeground(color);
		gc.setAntialias(SWT.ON);

		int oldLineStyle = SWT.NONE;

		if (lineStyle != SWT.NONE) {
			oldLineStyle = gc.getLineStyle();
			gc.setLineStyle(lineStyle);
		}

		double[] pixelValues = coordinateTransform.coordinateToPixel(x1, yAtX1);
		int previousPixelX = (int) Math.floor(pixelValues[0]);
		int previousPixelY = (int) Math.floor(pixelValues[1]);

		double[] coordinateValues = coordinateTransform.pixelToCoordinate(previousPixelX+PIXELS_BETWEEN_POINTS, previousPixelY);
		double coordinateStep = coordinateValues[0] - x1;

		double currentCoordinateX = x1 + coordinateStep;

		while (currentCoordinateX <= x2) {
			double currentCoordinateY = x2Coeff * currentCoordinateX * currentCoordinateX + x1Coeff * currentCoordinateX + x0Coeff;

			pixelValues = coordinateTransform.coordinateToPixel(currentCoordinateX, currentCoordinateY);
			int currentPixelX = (int) Math.floor(pixelValues[0]);
			int currentPixelY = (int) Math.floor(pixelValues[1]);

			gc.drawLine(previousPixelX, previousPixelY, currentPixelX, currentPixelY);

			previousPixelX = currentPixelX;
			previousPixelY = currentPixelY;

			currentCoordinateX += coordinateStep;
		}

		if (currentCoordinateX != x2) {
			pixelValues = coordinateTransform.coordinateToPixel(x2, yAtX2);
			int finalPixelX = (int) Math.floor(pixelValues[0]);
			int finalPixelY = (int) Math.floor(pixelValues[1]);

			gc.drawLine(previousPixelX, previousPixelY, finalPixelX, finalPixelY);
		}

		if (lineStyle != SWT.NONE) {
			gc.setLineStyle(oldLineStyle);
		}
	}
}
