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

import java.util.ArrayList;

public class DrawableObjects {
	private static int MAX_ALLOWABLE_SQUARE_OF_DISTANCE = 25;

	public static DrawableObject getClosestDrawableObject(int mouseX, int mouseY, ArrayList<DrawableObject> drawables) {
		long squareOfDistanceToClosestDrawable = Long.MAX_VALUE;
		DrawableObject closestDrawable = null;

		for (DrawableObject drawable : drawables) {
			long squareOfDistanceFromThis = drawable.squareOfDistanceFrom(mouseX, mouseY);

			if (squareOfDistanceFromThis <= MAX_ALLOWABLE_SQUARE_OF_DISTANCE && squareOfDistanceFromThis < squareOfDistanceToClosestDrawable) {
				closestDrawable = drawable;
				squareOfDistanceToClosestDrawable = squareOfDistanceFromThis;
			}
		}

		return closestDrawable;
	}
}
