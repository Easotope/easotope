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

package org.easotope.client.core.widgets.graph;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

import org.easotope.client.core.ColorCache;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;

public class VerticalAxis {
	private GraphSettings graphSettings;
	private char decimalSeparator;
	private char minusSign;
	private String exponentSeparator;

	private int sizeX;
	private int sizeY;
	private CoordinateTransform coordinateTransform;

	private int labelX;
	private int maxXExtent;
	private int scaleX;
	private int firstUsableX;
	private Scale scale;
	private ArrayList<String> scaleMarkerStrings = new ArrayList<String>();
	private ArrayList<Point> scaleMarkerStringExtents = new ArrayList<Point>();

	VerticalAxis(GraphSettings graphSettings) {
		this.graphSettings = graphSettings;

		DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();

		decimalSeparator = decimalFormatSymbols.getDecimalSeparator();
		minusSign = decimalFormatSymbols.getMinusSign();
		exponentSeparator = decimalFormatSymbols.getExponentSeparator();
	}

	void setParams(GC gc, int sizeX, int sizeY, int maxUsableY, CoordinateTransform coordinateTransform) {
		Font oldFont = gc.getFont();

		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.coordinateTransform = coordinateTransform;

		firstUsableX = 0;
		String label = graphSettings.getVerticalAxisShowLabel() ? graphSettings.getVerticalAxisLabel() : null;

		if (label != null || graphSettings.getVerticalAxisShowScale()) {
			firstUsableX += graphSettings.getVerticalAxisPaddingRightOfBase();
		}

		if (label != null) {
			labelX = firstUsableX;
			gc.setFont(graphSettings.getVerticalAxisLabelFont());
			firstUsableX += gc.getFontMetrics().getHeight();
			firstUsableX += graphSettings.getVerticalAxisPaddingRightOfLabel();
		}

		if (graphSettings.getVerticalAxisShowScale()) {
			double min = coordinateTransform.pixelToCoordinate(0, maxUsableY)[1];
			double max = coordinateTransform.pixelToCoordinate(0, 0)[1];

			scale = new Scale(min, max, decimalSeparator, minusSign, exponentSeparator);
			scaleX = firstUsableX;

			gc.setFont(graphSettings.getVerticalAxisScaleFont());

			maxXExtent = 0;
			scaleMarkerStrings.clear();
			scaleMarkerStringExtents.clear();

			ArrayList<String> toStrings = new ArrayList<String>();
			ArrayList<Point> toStringExtents = new ArrayList<Point>();

			ArrayList<String> plainStrings = new ArrayList<String>();
			ArrayList<Point> plainStringExtents = new ArrayList<Point>();

			for (BigDecimal scaleMarker : scale.getScaleMarkers()) {			
				String toString = scaleMarker.toString();
				toString = toString.replace('.', decimalSeparator);
				toString = toString.replace('-', minusSign);
				toString = toString.replaceAll("E", exponentSeparator);

				toStrings.add(toString);
				Point toStringExtent = gc.textExtent(toString);
				toStringExtents.add(toStringExtent);

				String plainString = scaleMarker.toPlainString();
				plainString = plainString.replace('.', decimalSeparator);
				plainString = plainString.replace('-', minusSign);

				plainStrings.add(plainString);
				Point plainStringExtent = gc.textExtent(plainString);
				plainStringExtents.add(plainStringExtent);
				
				maxXExtent = Math.max(maxXExtent, Math.min(toStringExtent.x, plainStringExtent.x));
			}

			for (int i=0; i<scale.getScaleMarkers().size(); i++) {
				if (plainStringExtents.get(i).x <= maxXExtent) {
					scaleMarkerStrings.add(plainStrings.get(i));
					scaleMarkerStringExtents.add(plainStringExtents.get(i));
				} else {
					scaleMarkerStrings.add(toStrings.get(i));
					scaleMarkerStringExtents.add(toStringExtents.get(i));
				}
			}

			firstUsableX += maxXExtent;
			firstUsableX += graphSettings.getVerticalAxisPaddingRightOfScale();
		}

		gc.setFont(oldFont);
	}

	int getWidth() {
		return firstUsableX;
	}

	void draw(GC gc) {
		Font oldFont = gc.getFont();

		String label = graphSettings.getVerticalAxisShowLabel() ? graphSettings.getVerticalAxisLabel() : null;

		if (label != null) {
			Transform oldTransform = new Transform(gc.getDevice());
			gc.getTransform(oldTransform);

			Transform newTransform = new Transform(gc.getDevice());
			newTransform.rotate(-90);
			gc.setTransform(newTransform);

			gc.setFont(graphSettings.getVerticalAxisLabelFont());
			Point point = gc.textExtent(label);

			int y = (sizeY + point.x) / 2;
			gc.drawText(label, -y, labelX, true);

			gc.setTransform(oldTransform);
		}

		if (graphSettings.getVerticalAxisShowScale()) {
			gc.setFont(graphSettings.getVerticalAxisScaleFont());

			ArrayList<BigDecimal> scaleMarkers = scale.getScaleMarkers();
			
			for (int i=0; i<scaleMarkers.size(); i++) {
				BigDecimal scaleMarker = scaleMarkers.get(i);
				String string = scaleMarkerStrings.get(i);
				Point stringExtent = scaleMarkerStringExtents.get(i);

				int y = (int) Math.floor(coordinateTransform.coordinateToPixel(0.0d, scaleMarker.doubleValue())[1]);

				gc.setForeground(ColorCache.getColor(graphSettings.getDisplay(), ColorCache.LIGHT_GREY));
				gc.drawLine(firstUsableX, y, sizeX-1, y);

				gc.setForeground(ColorCache.getColor(graphSettings.getDisplay(), ColorCache.BLACK));
				gc.drawText(string, scaleX + (maxXExtent - stringExtent.x), y - stringExtent.y / 2, true);
			}
		}

		gc.setFont(oldFont);
	}
}
