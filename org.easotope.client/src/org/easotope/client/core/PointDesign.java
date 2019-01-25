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

package org.easotope.client.core;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;

public class PointDesign {
	static private PointStyle[] pointStyleOrder = new PointStyle[] {
		PointStyle.FilledSquare,
		PointStyle.FilledCircle,
		PointStyle.FilledTriangle,
		PointStyle.FilledInvertedTriangle,
		PointStyle.FilledDiamond,
		PointStyle.HorizontalLines,
		PointStyle.VerticalLines,
		PointStyle.Plus,
		PointStyle.X,
		PointStyle.Square,
		PointStyle.Circle,
		PointStyle.Triangle,
		PointStyle.InvertedTriangle,
		PointStyle.Diamond
	};

	private Display display;
	private Color color;
	private PointStyle pointStyle;

	public PointDesign(Display display, int paletteColor, PointStyle pointStyle) {
		this.display = display;
		this.color = ColorCache.getColorFromPalette(display, paletteColor);
		this.pointStyle = pointStyle;
	}

	public PointDesign(Display display, Color color, PointStyle pointStyle) {
		this.display = display;
		this.color = color;
		this.pointStyle = pointStyle;
	}

	public Color getColor() {
		return color;
	}

	public Color getMarkedColor() {
		return ColorCache.getColor(display, new int[] { color.getRed() / 2 + 128, color.getGreen() / 2 + 128, color.getBlue() / 2 + 128 });
	}

	public PointStyle getPointStyle() {
		return pointStyle;
	}

	public void draw(GC gc, int x, int y, boolean isMarked) {
		final int size = 3;
		Color drawColor = isMarked ? getMarkedColor() : color;

		switch (pointStyle) {
			case Square:
				gc.setAntialias(SWT.OFF);
				gc.setForeground(drawColor);
				gc.drawPolygon(new int[] { x-size, y-size, x+size, y-size, x+size, y+size, x-size, y+size });
				break;

			case FilledSquare:
				gc.setAntialias(SWT.OFF);
				gc.setForeground(drawColor);
				gc.drawPolygon(new int[] { x-size, y-size, x+size, y-size, x+size, y+size, x-size, y+size });
				gc.setBackground(drawColor);
				gc.fillPolygon(new int[] { x-size, y-size, x+size, y-size, x+size, y+size, x-size, y+size });
				break;

			case Circle:
				gc.setAntialias(SWT.ON);
				gc.setForeground(drawColor);
				gc.drawOval(x-size, y-size, size*2, size*2);
				break;

			case FilledCircle:
				gc.setAntialias(SWT.ON);
				gc.setForeground(drawColor);
				gc.drawOval(x-size, y-size, size*2, size*2);
				gc.setBackground(drawColor);
				gc.fillOval(x-size, y-size, size*2, size*2);
				break;

			case Triangle:
				gc.setAntialias(SWT.ON);
				gc.setForeground(drawColor);
				gc.drawPolygon(new int[] { x, y-size, x+size, y+size, x-size, y+size });
				break;

			case FilledTriangle:
				gc.setAntialias(SWT.ON);
				gc.setForeground(drawColor);
				gc.drawPolygon(new int[] { x, y-size, x+size, y+size, x-size, y+size });
				gc.setBackground(drawColor);
				gc.fillPolygon(new int[] { x, y-size, x+size, y+size, x-size, y+size });
				break;

			case InvertedTriangle:
				gc.setAntialias(SWT.ON);
				gc.setForeground(drawColor);
				gc.drawPolygon(new int[] { x, y+size, x+size, y-size, x-size, y-size });
				break;

			case FilledInvertedTriangle:
				gc.setAntialias(SWT.ON);
				gc.setForeground(drawColor);
				gc.drawPolygon(new int[] { x, y+size, x+size, y-size, x-size, y-size });
				gc.setBackground(drawColor);
				gc.fillPolygon(new int[] { x, y+size, x+size, y-size, x-size, y-size });
				break;

			case Diamond:
				gc.setAntialias(SWT.ON);
				gc.setForeground(drawColor);
				gc.drawPolygon(new int[] { x, y-size, x+size, y, x, y+size, x-size, y });
				break;

			case FilledDiamond:
				gc.setAntialias(SWT.ON);
				gc.setForeground(drawColor);
				gc.drawPolygon(new int[] { x, y-size, x+size, y, x, y+size, x-size, y });
				gc.setBackground(drawColor);
				gc.fillPolygon(new int[] { x, y-size, x+size, y, x, y+size, x-size, y });
				break;

			case Plus:
				gc.setAntialias(SWT.OFF);
				gc.setForeground(drawColor);
				gc.drawLine(x-size, y, x+size, y);
				gc.drawLine(x, y-size, x, y+size);
				break;

			case HorizontalLines:
				gc.setAntialias(SWT.OFF);
				gc.setForeground(drawColor);
				gc.drawLine(x-size, y-size, x+size, y-size);
				gc.drawLine(x-size, y, x+size, y);
				gc.drawLine(x-size, y+size, x+size, y+size);
				break;

			case VerticalLines:
				gc.setAntialias(SWT.OFF);
				gc.setForeground(drawColor);
				gc.drawLine(x-size, y-size, x-size, y+size);
				gc.drawLine(x, y-size, x, y+size);
				gc.drawLine(x+size, y-size, x+size, y+size);
				break;

			case X:
				gc.setAntialias(SWT.OFF);
				gc.setForeground(drawColor);
				gc.drawLine(x-size, y-size, x+size, y+size);
				gc.drawLine(x+size, y-size, x-size, y+size);
				break;

			case R:
				gc.setAntialias(SWT.OFF);
				gc.setForeground(drawColor);
				gc.drawPolygon(new int[] { x-size, y+size, x-size, y-size, x+size, y-size, x+size, y, x-size, y } );
				gc.drawLine(x, y, x+size, y+size);
				break;

			case S:
				gc.setAntialias(SWT.OFF);
				gc.setForeground(drawColor);
				gc.drawLine(x+size, y-size, x-size, y-size);
				gc.drawLine(x-size, y-size, x-size, y);
				gc.drawLine(x-size, y, x+size, y);
				gc.drawLine(x+size, y, x+size, y+size);
				gc.drawLine(x+size, y+size, x-size, y+size);
				break;

			default:
				break;
		}

		gc.setAntialias(SWT.ON);
	}

	static public PointDesign getByIndex(Display display, int index) {
		PointStyle pointStyle = pointStyleOrder[index % pointStyleOrder.length];
		Color color = ColorCache.getColorFromPalette(display, index);
		return new PointDesign(display, color, pointStyle);
	}
}
