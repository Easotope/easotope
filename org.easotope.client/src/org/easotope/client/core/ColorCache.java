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

package org.easotope.client.core;

import java.util.HashMap;

import org.easotope.framework.core.logging.Log;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Display;

public class ColorCache {
	private static final String DISPLAY_ID = "org.easotope.core.gui.ColorCache.DISPLAY_ID";

	public static final int[] BLACK = new int[] { 0x00, 0x00, 0x00 };
	public static final int[] WHITE = new int[] { 0xff, 0xff, 0xff };
	public static final int[] GREY = new int[] { 0xc0, 0xc0, 0xc0 };
	public static final int[] LIGHT_GREY = new int[] { 0xe0, 0xe0, 0xe0 };
	public static final int[] RED = new int[] { 0xff, 0x00, 0x00 };
	public static final int[] YELLOW = new int[] { 0xff, 0xff, 0x00 };
	public static final int[] WARNING_TEXT = new int[] { 0xfb, 0xc5, 0x0b };

	// Wong, B. (2011) Points of view: Color blindness. Nature Methods 8:441.
	private static int[][] standardPalette = {
		{ 213, 94, 0 },		// vermillion
		{ 0, 114, 178 },	// blue
		{ 204, 121, 167 },	// reddish purple
		{ 230, 159, 0 },	// orange
		{ 0, 158, 115 },	// bluish green
		{ 86, 180, 233 },	// sky blue
		{ 240, 228, 66 }	// yellow
	};

//	// old Easotope colors
//	private static int[][] standardPalette = {
//	{ 255, 1, 77 },
//	{ 63, 42, 253 },
//	{ 247, 0, 212 },
//	{ 252, 159, 6 },
//	{ 125, 125, 125 },
//	{ 5, 228, 236 },
//	{ 235, 235, 0 }
//};

//	// suggested deuteranopia color palette
//	private static int[][] standardPalette = {
//		{ 255, 204, 153 },
//		{ 204, 204, 255 },
//		{ 255, 102, 102 },
//		{ 139, 230, 2 },
//		{ 255, 0, 0 },
//		{ 255, 255, 0 },
//		{ 51, 51, 255 },
//		{ 153, 153, 153 },
//		{ 153, 0, 0 },
//		{ 51, 51, 51 },
//		{ 0, 0, 102 },
//		{ 120, 102, 75 }
//	};

	private static int displayCounter = 0;	// is this whole thing necessary?
	private static HashMap<Integer,Color> colors = new HashMap<Integer,Color>();

	public static int getNumberOfColorsInPalette() {
		return standardPalette.length;
	}

	public static Color getColorFromPalette(Device device, int index) {
		return getColor(device, standardPalette[index % ColorCache.getNumberOfColorsInPalette()]);
	}

	public static Color getFadedColorFromPalette(Device device, int index) {
		int[] originalColor = standardPalette[index % ColorCache.getNumberOfColorsInPalette()];
		return getColor(device, (originalColor[0] + (255*3)) / 4, (originalColor[1] + (255*3)) / 4, (originalColor[2] + (255*3)) / 4);
	}

	public static Color getColor(Device device, int[] color) {
		return getColor(device, color[0], color[1], color[2]);
	}

	private static Color getColor(Device device, int red, int green, int blue) {
		Integer displayId = (Integer) ((Display) device).getData(DISPLAY_ID);

		if (displayId == null) {
			displayId = displayCounter++;
			((Display) device).setData(DISPLAY_ID, displayId);
			Log.getInstance().log(Log.Level.INFO, ColorCache.class, "Assigning new display id " + displayId);
		}

		int key = (displayId << 24) | (red << 16) | (green << 8) | blue;

		if (colors.containsKey(key)) {
			return colors.get(key);
		}

		Color color = new Color(device, red, green, blue);
		colors.put(key, color);

		return color;
	}
}
