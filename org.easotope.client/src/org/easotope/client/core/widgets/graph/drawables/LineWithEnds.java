/*
 * Copyright © 2016-2017 by Devon Bowen.
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

public class LineWithEnds extends DrawableObject {
	private double x1;
	private double y1;
	private double x2;
	private double y2;
	private Color color;
	private int lineStyle = SWT.NONE;

	public LineWithEnds(double x1, double y1, double x2, double y2, Color color, String[] tooltip) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.color = color;
		setTooltip(tooltip);
	}

	public LineWithEnds(double x1, double y1, double x2, double y2, Color color, int lineStyle, String[] tooltip) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		this.color = color;
		this.lineStyle = lineStyle;
		setTooltip(tooltip);
	}

	@Override
	public double getMaxX() {
		return (x1 > x2) ? x1 : x2;
	}

	@Override
	public double getMinX() {
		return (x1 < x2) ? x1 : x2;
	}

	@Override
	public double getMaxY() {
		return (y1 > y2) ? y1 : y2;
	}

	@Override
	public double getMinY() {
		return (y1 < y2) ? y1 : y2;
	}

	@Override
	public long squareOfDistanceFrom(int pixelX, int pixelY) {
		return Long.MAX_VALUE;
	}

	@Override
	public void draw(GC gc, int sizeX, int sizeY, CoordinateTransform coordinateTransform) {
		double[] pixelValues = coordinateTransform.coordinateToPixel(x1, y1);
		int x1 = (int) Math.floor(pixelValues[0]);
		int y1 = (int) Math.floor(pixelValues[1]);

		pixelValues = coordinateTransform.coordinateToPixel(x2, y2);
		int x2 = (int) Math.floor(pixelValues[0]);
		int y2 = (int) Math.floor(pixelValues[1]);

		gc.setForeground(color);
		gc.setAntialias(SWT.ON);

		int oldLineStyle = SWT.NONE;

		if (lineStyle != SWT.NONE) {
			oldLineStyle = gc.getLineStyle();
			gc.setLineStyle(lineStyle);
		}

		gc.drawLine(x1, y1, x2, y2);

		if (lineStyle != SWT.NONE) {
			gc.setLineStyle(oldLineStyle);
		}
	}
}
