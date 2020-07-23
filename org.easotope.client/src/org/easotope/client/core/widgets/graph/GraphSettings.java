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

package org.easotope.client.core.widgets.graph;

import org.easotope.client.core.ColorCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class GraphSettings {
	private Display display;

	private boolean unitsAreEqual;

	private Font tooltipFont;
	private Color tooltipColor;
	private Color tooltipBackground;

	private String horizontalAxisLabel;
	private boolean horizontalAxisShowLabel;
	private Font horizontalAxisLabelFont;
	private Color horizontalAxisLabelColor;
	private boolean horizontalAxisShowScale;
	private Font horizontalAxisScaleFont;
	private Color horizontalAxisScaleColor;
	private int horizontalAxisPaddingAboveScale;
	private int horizontalAxisPaddingAboveLabel;
	private int horizontalAxisPaddingAboveBase;

	private String verticalAxisLabel;
	private boolean verticalAxisShowLabel;
	private Font verticalAxisLabelFont;
	private Color verticalAxisLabelColor;
	private boolean verticalAxisShowScale;
	private Font verticalAxisScaleFont;
	private Color verticalAxisScaleColor;
	private int verticalAxisPaddingRightOfScale;
	private int verticalAxisPaddingRightOfLabel;
	private int verticalAxisPaddingRightOfBase;

	public GraphSettings(Display display) {
		this.display = display;

		unitsAreEqual = false;

		tooltipFont = new Font(display, "Arial", 12, SWT.NORMAL);
		tooltipColor = ColorCache.getColor(display, ColorCache.BLACK);
		tooltipBackground = ColorCache.getColor(display, ColorCache.WHITE);

		horizontalAxisLabel = null;
		horizontalAxisShowLabel = false;
		horizontalAxisLabelFont= new Font(display, "Arial", 12, SWT.NORMAL);
		horizontalAxisLabelColor = ColorCache.getColor(display, ColorCache.BLACK);
		horizontalAxisShowScale = false;
		horizontalAxisScaleFont= new Font(display, "Arial", 12, SWT.NORMAL);
		horizontalAxisScaleColor = ColorCache.getColor(display, ColorCache.BLACK);
		horizontalAxisPaddingAboveScale = 2;
		horizontalAxisPaddingAboveLabel = 2;
		horizontalAxisPaddingAboveBase = 2;

		verticalAxisLabel = null;
		verticalAxisShowLabel = false;
		verticalAxisLabelFont= new Font(display, "Arial", 12, SWT.NORMAL);
		verticalAxisLabelColor = ColorCache.getColor(display, ColorCache.BLACK);
		verticalAxisShowScale = false;
		verticalAxisScaleFont= new Font(display, "Arial", 12, SWT.NORMAL);
		verticalAxisScaleColor = ColorCache.getColor(display, ColorCache.BLACK);
		verticalAxisPaddingRightOfScale = 2;
		verticalAxisPaddingRightOfLabel = 2;
		verticalAxisPaddingRightOfBase = 2;
	}

	public void dispose() {
		tooltipFont.dispose();
		horizontalAxisLabelFont.dispose();
		horizontalAxisScaleFont.dispose();
	}
	
	public Display getDisplay() {
		return display;
	}

	public boolean getUnitsAreEqual() {
		return unitsAreEqual;
	}

	public void setUnitsAreEqual(boolean unitsAreEqual) {
		this.unitsAreEqual = unitsAreEqual;
	}

	public Font getTooltipFont() {
		return tooltipFont;
	}

	public Color getTooltipColor() {
		return tooltipColor;
	}

	public Color getTooltipBackground() {
		return tooltipBackground;
	}

	public String getHorizontalAxisLabel() {
		return horizontalAxisLabel;
	}

	public void setHorizontalAxisLabel(String horizontalAxisLabel) {
		this.horizontalAxisLabel = horizontalAxisLabel;
	}

	public boolean getHorizontalAxisShowLabel() {
		return horizontalAxisShowLabel;
	}

	public void setHorizontalAxisShowLabel(boolean horizontalAxisShowLabel) {
		this.horizontalAxisShowLabel = horizontalAxisShowLabel;
	}

	public Font getHorizontalAxisLabelFont() {
		return horizontalAxisLabelFont;
	}

	public Color getHorizontalAxisLabelColor() {
		return horizontalAxisLabelColor;
	}

	public boolean getHorizontalAxisShowScale() {
		return horizontalAxisShowScale;
	}

	public void setHorizontalAxisShowScale(boolean horizontalAxisShowScale) {
		this.horizontalAxisShowScale = horizontalAxisShowScale;
	}

	public Font getHorizontalAxisScaleFont() {
		return horizontalAxisScaleFont;
	}

	public Color getHorizontalAxisScaleColor() {
		return horizontalAxisScaleColor;
	}

	public int getHorizontalAxisPaddingAboveScale() {
		return horizontalAxisPaddingAboveScale;
	}

	public int getHorizontalAxisPaddingAboveLabel() {
		return horizontalAxisPaddingAboveLabel;
	}

	public int getHorizontalAxisPaddingAboveBase() {
		return horizontalAxisPaddingAboveBase;
	}

	public String getVerticalAxisLabel() {
		return verticalAxisLabel;
	}

	public void setVerticalAxisLabel(String verticalAxisLabel) {
		this.verticalAxisLabel = verticalAxisLabel;
	}

	public boolean getVerticalAxisShowLabel() {
		return verticalAxisShowLabel;
	}

	public void setVerticalAxisShowLabel(boolean verticalAxisShowLabel) {
		this.verticalAxisShowLabel = verticalAxisShowLabel;
	}

	public Font getVerticalAxisLabelFont() {
		return verticalAxisLabelFont;
	}

	public Color getVerticalAxisLabelColor() {
		return verticalAxisLabelColor;
	}

	public boolean getVerticalAxisShowScale() {
		return verticalAxisShowScale;
	}

	public void setVerticalAxisShowScale(boolean verticalAxisShowScale) {
		this.verticalAxisShowScale = verticalAxisShowScale;
	}

	public Font getVerticalAxisScaleFont() {
		return verticalAxisScaleFont;
	}

	public Color getVerticalAxisScaleColor() {
		return verticalAxisScaleColor;
	}

	public int getVerticalAxisPaddingRightOfScale() {
		return verticalAxisPaddingRightOfScale;
	}

	public int getVerticalAxisPaddingRightOfLabel() {
		return verticalAxisPaddingRightOfLabel;
	}

	public int getVerticalAxisPaddingRightOfBase() {
		return verticalAxisPaddingRightOfBase;
	}
}
