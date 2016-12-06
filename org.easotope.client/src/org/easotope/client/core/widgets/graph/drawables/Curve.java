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

package org.easotope.client.core.widgets.graph.drawables;

import java.util.ArrayList;

import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.widgets.graph.CoordinateTransform;
import org.easotope.client.core.widgets.graph.DrawableObject;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public class Curve extends DrawableObject {
	private double[] x;
	private double[] y;
	private Color color;

	private boolean addXToTooltip = false;
	private boolean addYToTooltip = false;
	private boolean withLines = false;

	private int[] mostRecentScreenPoint;

	int cachedX;
	int cachedY;
	int cachedIndex;

	private double maxX = Double.NEGATIVE_INFINITY;
	private double minX = Double.POSITIVE_INFINITY;
	private double maxY = Double.NEGATIVE_INFINITY;
	private double minY = Double.POSITIVE_INFINITY;

	public Curve(double[] x, double[] y, Color color, String[] tooltip) {
		this.x = x;
		this.y = y;

		mostRecentScreenPoint = new int[x.length * 2];

		for (double thisX : x) {
			if (thisX > maxX) {
				maxX = thisX;
			}

			if (thisX < minX) {
				minX = thisX;
			}
		}

		for (double thisY : y) {
			if (thisY > maxY) {
				maxY = thisY;
			}

			if (thisY < minY) {
				minY = thisY;
			}
		}

		this.color = color;
		setTooltip(tooltip);
	}

	public boolean isAddXToTooltip() {
		return addXToTooltip;
	}

	public void setAddXToTooltip(boolean addXToTooltip) {
		this.addXToTooltip = addXToTooltip;
	}

	public boolean isAddYToTooltip() {
		return addYToTooltip;
	}

	public void setAddYToTooltip(boolean addYToTooltip) {
		this.addYToTooltip = addYToTooltip;
	}

	public boolean isWithLines() {
		return withLines;
	}

	public void setWithLines(boolean withLines) {
		this.withLines = withLines;
	}

	@Override
	public double getMaxX() {
		return maxX;
	}

	@Override
	public double getMinX() {
		return minX;
	}

	@Override
	public double getMaxY() {
		return maxY;
	}

	@Override
	public double getMinY() {
		return minY;
	}

	@Override
	public long squareOfDistanceFrom(int pixelX, int pixelY) {
		long minSquareOfDistance = Long.MAX_VALUE;
		int minSquareOfDistanceIndex = -1;

		for (int i=0; i<x.length; i++) {
			long xDistance = (mostRecentScreenPoint[i*2] - pixelX);
			long yDistance = (mostRecentScreenPoint[i*2+1] - pixelY);
			long thisSquareOfDistance = (xDistance * xDistance) + (yDistance * yDistance);

			if (thisSquareOfDistance < minSquareOfDistance) {
				minSquareOfDistance = thisSquareOfDistance;
				minSquareOfDistanceIndex = i;
			}
		}

		cachedX = pixelX;
		cachedY = pixelY;
		cachedIndex = minSquareOfDistanceIndex;

		return minSquareOfDistance;
	}

	@Override
	public String[] getTooltip(int pixelX, int pixelY) {
		if (cachedX != pixelX || cachedY != pixelY) {
			squareOfDistanceFrom(pixelX, pixelY);
		}

		if (cachedIndex == -1) {
			return null;
		}

		ArrayList<String> newTooltip = new ArrayList<String>();

		if (super.getTooltip(pixelX, pixelY) != null) {
			for (String string : super.getTooltip(pixelX, pixelY)) {
				newTooltip.add(string);
			}
		}
		
		if (addXToTooltip) {
			newTooltip.add(String.format(GuiConstants.DOUBLE_FORMAT, x[cachedIndex]));
		}

		if (addYToTooltip) {
			newTooltip.add(String.format(GuiConstants.DOUBLE_FORMAT, y[cachedIndex]));
		}

		if (newTooltip.size() == 0) {
			return null;
		}

		return (newTooltip.size() != 0) ? newTooltip.toArray(new String[newTooltip.size()]) : null;
	}

	@Override
	public void draw(GC gc, int sizeX, int sizeY, CoordinateTransform coordinateTransform) {
		gc.setForeground(color);

		for (int i=0; i<x.length; i++) {
			double[] pixelValues = coordinateTransform.coordinateToPixel(x[i], y[i]);
			mostRecentScreenPoint[i*2] = (int) Math.floor(pixelValues[0]);
			mostRecentScreenPoint[i*2+1] = (int) Math.floor(pixelValues[1]);
		}

		if (withLines) {
			gc.drawPolyline(mostRecentScreenPoint);

		} else {
			for (int i=0; i<x.length; i++) {
				gc.drawPoint(mostRecentScreenPoint[i*2], mostRecentScreenPoint[i*2+1]);
			}
		}
	}
}
