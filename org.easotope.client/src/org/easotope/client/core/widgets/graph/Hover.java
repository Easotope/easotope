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

package org.easotope.client.core.widgets.graph;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class Hover {
	private static final int PADDING = 3;

	private int mouseX;
	private int mouseY;
	private String text = null;

	void set(int mouseX, int mouseY, ArrayList<DrawableObject> drawables) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;

		text = null;
		DrawableObject closestDrawable = DrawableObjects.getClosestDrawableObject(mouseX, mouseY, drawables);
		String[] tooltip = closestDrawable != null ? closestDrawable.getTooltip(mouseX, mouseY) : null;

		if (tooltip != null) {
			for (String string : tooltip) {
				string = (string == null) ? "" : string;
				text = (text == null) ? string : text + "\n" + string;
			}
		}
	}

	void reset() {
		text = null;
	}

	void drawHover(GC gc, int canvasSizeX, int canvasSizeY, GraphSettings graphSettings) {
		if (text == null) {
			return;
		}

		Font oldFont = gc.getFont();
		Color oldForeground = gc.getForeground();
		Color oldBackground = gc.getBackground();

		gc.setFont(graphSettings.getTooltipFont());
		gc.setForeground(graphSettings.getTooltipColor());
		gc.setBackground(graphSettings.getTooltipBackground());

		Point textExtent = gc.textExtent(text);

		int cornerX = 0;
		int cornerY = 0;

		if (canvasSizeX / 2 < mouseX) {
			cornerX = mouseX - textExtent.x - 2 * PADDING - 8;
		} else {
			cornerX = mouseX + 10;
		}

		if (canvasSizeY / 2 < mouseY) {
			cornerY = mouseY - textExtent.y - 2 * PADDING - 8;
		} else {
			cornerY = mouseY + 10;
		}

		gc.fillRectangle(cornerX, cornerY, textExtent.x + 2 * PADDING, textExtent.y + 2 * PADDING);
		gc.drawRectangle(cornerX, cornerY, textExtent.x + 2 * PADDING, textExtent.y + 2 * PADDING);
		gc.drawText(text, cornerX + PADDING, cornerY + PADDING);

		gc.setFont(oldFont);
		gc.setForeground(oldForeground);
		gc.setBackground(oldBackground);
	}
}
