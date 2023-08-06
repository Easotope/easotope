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

import java.util.TreeSet;

import org.eclipse.swt.graphics.GC;

public abstract class DrawableObject {
	private boolean influencesAutoscale = true;
	private String[] tooltip = null;
	private TreeSet<SortableMenuItem> menuItems = new TreeSet<SortableMenuItem>();

	public abstract double getMaxX();
	public abstract double getMinX();
	public abstract double getMaxY();
	public abstract double getMinY();
	public abstract long squareOfDistanceFrom(int pixelX, int pixelY);
	public abstract void draw(GC gc, int sizeX, int sizeY, CoordinateTransform coordinateTransform);

	public void addMenuItem(MenuItemListener listener) {
		menuItems.add(new SortableMenuItem(listener));
	}

	TreeSet<SortableMenuItem> getMenuItems() {
		return menuItems;
	}

	public boolean isInfluencesAutoscale() {
		return influencesAutoscale;
	}

	public void setInfluencesAutoscale(boolean influencesAutoscale) {
		this.influencesAutoscale = influencesAutoscale;
	}

	public String[] getTooltip(int pixelX, int pixelY) {
		return tooltip;
	}

	public void setTooltip(String[] tooltip) {
		this.tooltip = tooltip;
	}
}
