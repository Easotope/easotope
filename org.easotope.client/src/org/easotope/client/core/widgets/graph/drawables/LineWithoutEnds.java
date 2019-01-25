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

public class LineWithoutEnds extends DrawableObject {
	private double slope;
	private double intercept;
	private double autoscaleShowsPointAtX = Double.NaN;
	private double autoscaleShowsY = Double.NaN;
	private Color color;

	public LineWithoutEnds(double xIntercept, Color color, double autoscaleShowsPointAtX, double autoscaleShowsY, String[] tooltip) {
		slope = Double.NaN;
		intercept = xIntercept;
		this.autoscaleShowsPointAtX = autoscaleShowsPointAtX;
		this.autoscaleShowsY = autoscaleShowsY;
		this.color = color;
		setTooltip(tooltip);
	}

	public LineWithoutEnds(double slope, double intercept, Color color, String[] tooltip) {
		this.slope = slope;
		this.intercept = intercept;
		this.color = color;
		setTooltip(tooltip);
	}

	public LineWithoutEnds(double slope, double intercept, double autoscaleShowsPointAtX, double autoscaleShowsY, Color color, String[] tooltip) {
		this.slope = slope;
		this.intercept = intercept;
		this.autoscaleShowsPointAtX = autoscaleShowsPointAtX;
		this.autoscaleShowsY = autoscaleShowsY;
		this.color = color;
		setTooltip(tooltip);
	}

	@Override
	public double getMaxX() {
		return autoscaleShowsPointAtX;
	}

	@Override
	public double getMinX() {
		return autoscaleShowsPointAtX;
	}

	@Override
	public double getMaxY() {
		if (!Double.isNaN(autoscaleShowsY) && !Double.isNaN(autoscaleShowsPointAtX)) {
			return Math.max(autoscaleShowsY, slope * autoscaleShowsPointAtX + intercept);

		} else if (!Double.isNaN(autoscaleShowsPointAtX)) {
			return slope * autoscaleShowsPointAtX + intercept;

		} else if (!Double.isNaN(autoscaleShowsY)) {
			return autoscaleShowsY;	
		}

		return Double.NaN;
	}

	@Override
	public double getMinY() {
		if (!Double.isNaN(autoscaleShowsY) && !Double.isNaN(autoscaleShowsPointAtX)) {
			return Math.min(autoscaleShowsY, slope * autoscaleShowsPointAtX + intercept);

		} else if (!Double.isNaN(autoscaleShowsPointAtX)) {
			return slope * autoscaleShowsPointAtX + intercept;

		} else if (!Double.isNaN(autoscaleShowsY)) {
			return autoscaleShowsY;	
		}

		return Double.NaN;
	}

	@Override
	public long squareOfDistanceFrom(int pixelX, int pixelY) {
		return Long.MAX_VALUE;	// TODO this could be perpendicular distance to line
	}

	@Override
	public void draw(GC gc, int sizeX, int sizeY, CoordinateTransform coordinateTransform) {
		int x1 = 0;
		int y1 = 0;
		int x2 = 0;
		int y2 = 0;

		if (!Double.isNaN(slope)) {
			double[] pixelValues = coordinateTransform.pixelToCoordinate(0, 0);
			double leftX = pixelValues[0];
			double leftY = slope * leftX + intercept;
	
			pixelValues = coordinateTransform.pixelToCoordinate(sizeX-1, 0);
			double rightX = pixelValues[0];
			double rightY = slope * rightX + intercept;
	
			pixelValues = coordinateTransform.coordinateToPixel(leftX, leftY);
			x1 = (int) Math.floor(pixelValues[0]);
			y1 = (int) Math.floor(pixelValues[1]);
	
			pixelValues = coordinateTransform.coordinateToPixel(rightX, rightY);
			x2 = (int) Math.floor(pixelValues[0]);
			y2 = (int) Math.floor(pixelValues[1]);

		} else {
			double[] pixelValues = coordinateTransform.coordinateToPixel(intercept, 0);
			x1 = (int) Math.floor(pixelValues[0]);
			y1 = 0;

			x2 = x1;
			y2 = sizeY-1;
		}

		gc.setForeground(color);
		gc.setAntialias(SWT.ON);
		gc.drawLine(x1, y1, x2, y2);
	}
}
