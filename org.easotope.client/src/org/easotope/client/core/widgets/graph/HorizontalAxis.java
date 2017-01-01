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

package org.easotope.client.core.widgets.graph;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;

import org.easotope.client.core.ColorCache;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;


public class HorizontalAxis {
	private GraphSettings graphSettings;
	private char decimalSeparator;
	private char minusSign;
	private String exponentSeparator;

	HorizontalAxis(GraphSettings graphSettings) {
		this.graphSettings = graphSettings;

		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();

		decimalSeparator = decimalFormatSymbols.getDecimalSeparator();
		minusSign = decimalFormatSymbols.getMinusSign();
		exponentSeparator = decimalFormatSymbols.getExponentSeparator();
	}

	int getHeight(GC gc) {
		Font oldFont = gc.getFont();
		int height = 0;
		String label = graphSettings.getHorizontalAxisShowLabel() ? graphSettings.getHorizontalAxisLabel() : null;

		if (label != null) {
			gc.setFont(graphSettings.getHorizontalAxisLabelFont());
			height += gc.getFontMetrics().getHeight();
			height += graphSettings.getHorizontalAxisPaddingAboveLabel();
		}

		if (graphSettings.getHorizontalAxisShowScale()) {
			gc.setFont(graphSettings.getHorizontalAxisScaleFont());
			height += gc.getFontMetrics().getHeight();
			height += graphSettings.getHorizontalAxisPaddingAboveScale();
		}

		if (height != 0) {
			height += graphSettings.getHorizontalAxisPaddingAboveBase();
		}

		gc.setFont(oldFont);
		return height;
	}

	void draw(GC gc, int sizeX, int sizeY, int firstUsableX, CoordinateTransform coordinateTransform) {
		Font oldFont = gc.getFont();
		Color oldForeground = gc.getForeground();

		int y = sizeY;
		String label = graphSettings.getHorizontalAxisShowLabel() ? graphSettings.getHorizontalAxisLabel() : null;

		if (label != null || graphSettings.getHorizontalAxisShowScale()) {
			y -= graphSettings.getHorizontalAxisPaddingAboveBase();
		}

		if (label != null) {
			gc.setFont(graphSettings.getHorizontalAxisLabelFont());

			Point point = gc.textExtent(label);
			int x = (sizeX - point.x + 1) / 2;
			y -= point.y;

			gc.drawText(label, x, y, true);

			y -= graphSettings.getHorizontalAxisPaddingAboveLabel();
		}

		if (graphSettings.getHorizontalAxisShowScale()) {
			gc.setFont(graphSettings.getHorizontalAxisScaleFont());

			y -= gc.getFontMetrics().getHeight();

			double min = coordinateTransform.pixelToCoordinate(firstUsableX, 0)[0];
			double max = coordinateTransform.pixelToCoordinate(sizeX-1, 0)[0];
	
			Scale scale = new Scale(min, max, decimalSeparator, minusSign, exponentSeparator);

			for (BigDecimal scaleMarker : scale.getScaleMarkers()) {
				int x = (int) Math.floor(coordinateTransform.coordinateToPixel(scaleMarker.doubleValue(), 0.0d)[0]);

				if (x < firstUsableX || x > sizeX-1) {
					continue;
				}

				gc.setForeground(ColorCache.getColor(graphSettings.getDisplay(), ColorCache.LIGHT_GREY));
				gc.drawLine(x, 0, x, y - graphSettings.getHorizontalAxisPaddingAboveScale());

				String toString = scaleMarker.toString();
				toString = toString.replace('.', decimalSeparator);
				toString = toString.replace('-', minusSign);
				toString = toString.replaceAll("E", exponentSeparator);

				String plainString = scaleMarker.toPlainString();
				plainString = plainString.replace('.', decimalSeparator);
				plainString = plainString.replace('-', minusSign);

				String string = toString.length() > plainString.length() ? plainString : toString;

				Point point = gc.textExtent(string);
				gc.setForeground(ColorCache.getColor(graphSettings.getDisplay(), ColorCache.BLACK));
				gc.drawText(string, x - point.x / 2, y, true);
			}
		}

		gc.setForeground(oldForeground);
		gc.setFont(oldFont);
	}
}
