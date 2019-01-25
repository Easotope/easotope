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

package org.easotope.client.core.widgets.graph.drawables;

import org.easotope.client.core.widgets.graph.CoordinateTransform;
import org.easotope.client.core.widgets.graph.DrawableObject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class Axes extends DrawableObject {
	private Color color;
	private int lineStyle;
	private boolean originIsVisible;

	public Axes(Color color, int lineStyle, boolean originIsVisible) {
		this.color = color;
		this.lineStyle = lineStyle;
		this.originIsVisible = originIsVisible;
	}

	@Override
	public double getMaxX() {
		return originIsVisible ? 0 : Double.NaN;
	}

	@Override
	public double getMinX() {
		return originIsVisible ? 0 : Double.NaN;
	}

	@Override
	public double getMaxY() {
		return originIsVisible ? 0 : Double.NaN;
	}

	@Override
	public double getMinY() {
		return originIsVisible ? 0 : Double.NaN;
	}

	@Override
	public long squareOfDistanceFrom(int pixelX, int pixelY) {
		return Integer.MAX_VALUE;
	}

	@Override
	public void draw(GC gc, int sizeX, int sizeY, CoordinateTransform coordinateTransform) {
		double[] pixelValues = coordinateTransform.coordinateToPixel(0, 0);
		int x = (int) Math.floor(pixelValues[0]);
		int y = (int) Math.floor(pixelValues[1]);

		gc.setForeground(color);
		gc.setAntialias(SWT.ON);

		int oldLineStyle = SWT.NONE;

		if (lineStyle != SWT.NONE) {
			oldLineStyle = gc.getLineStyle();
			gc.setLineStyle(lineStyle);
		}

		gc.drawLine(0, y, sizeX-1, y);
		gc.drawLine(x, 0, x, sizeY-1);

		if (lineStyle != SWT.NONE) {
			gc.setLineStyle(oldLineStyle);
		}
	}
}
