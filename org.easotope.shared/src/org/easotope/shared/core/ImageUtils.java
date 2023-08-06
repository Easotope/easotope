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

package org.easotope.shared.core;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;

public class ImageUtils {
	/**
	 * Given ImageData for an image with a white background and antialiased black
	 * text, this function modifies the ImageData so that previously white pixels
	 * become transparent and non-white pixels become partially transparent according
	 * to the amount of white they contain. The antialiased text will also be
	 * changed from black to a new color provided by the caller. This function is
	 * necessary because SWT does not know how to render antialiased text with an
	 * alpha channel.
	 * 
	 * @param imageData	image data with white background and antialiased black text
	 * @param newColor	new color to be used to render the antialiased text.
	 */
	public static void whiteToAlphaWithNewColor(ImageData imageData, Color newColor) {
		whiteToAlphaWithNewColor(imageData, newColor, 1);
	}

	public static void whiteToAlphaWithNewColor(ImageData imageData, Color newColor, float intensity) {
		int width = imageData.width;
		int height = imageData.height;

		int redMask = imageData.palette.redMask;
		int redShift = Math.abs(imageData.palette.redShift);
	    int newColorPixel = imageData.palette.getPixel(newColor.getRGB());

		byte[] alpha = new byte[height * width];
		imageData.alphaData = alpha;

	    int[] lineData = new int[width];

		for (int y=0; y<height; y++) {
		    imageData.getPixels(0, y, width, lineData, 0);

			for (int x=0; x<width; x++) {
				int red = (lineData[x] & redMask) >> redShift;
				alpha[y*width+x] = (byte) ((255 - red) * intensity);
				lineData[x] = newColorPixel;
			}

			imageData.setPixels(0, y, width, lineData, 0);
	    }
	}

	public static ImageData rotateImageLeft(ImageData source) {
		ImageData destination = new ImageData(source.height, source.width, source.depth, source.palette);

		for (int sx = 0; sx < source.width; sx++) {
			for (int sy = 0; sy < source.height; sy++) {
				int dx = sy;
				int dy = source.width - sx - 1;
				destination.setPixel(dx, dy, source.getPixel(sx, sy));
			}
		}

		return destination;
	}
}
