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

package org.easotope.client.analysis.part.analysis.replicate.timeselector;

import org.easotope.client.core.Transform2D;


public class CoordinateTransform {
	private final double PAN_SPEED = 2;

	private double[] tempPoint = new double[] { 0.0d, 0.0d };

	private int canvasMaxX;
	private int startTimeInMinutes;
	private int endTimeInMinutes;

	private Transform2D mainTransform = null;

	private double magnification = Double.NaN;
	private int magnifyAroundMouseX;
	private double minimumAllowableMagnification = Double.NaN;
	private Transform2D magnificationTransform = null;

	void reset() {
		mainTransform = null;
	}

	public boolean isReady() {
		return mainTransform != null;
	}

	void initTransform(int canvasSizeX, int startTimeInMinutes, int endTimeInMinutes, int timeInMinutesAtFirstPixel, int timeInMinutesAtLastPixel) {
		this.canvasMaxX = canvasSizeX - 1;
		this.startTimeInMinutes = startTimeInMinutes;
		this.endTimeInMinutes = endTimeInMinutes;

		mainTransform = new Transform2D();
		updateVisibleDataRange(timeInMinutesAtFirstPixel, timeInMinutesAtLastPixel);
	}

	void updateCanvasSizeX(int canvasSizeX) {
		tempPoint[0] = 0.5d;
		mainTransform.inverseTransform(tempPoint);
		double tempTimeInMinutesAtFirstPixel = tempPoint[0];

		tempPoint[0] = canvasMaxX + 0.5d;
		mainTransform.inverseTransform(tempPoint);
		double tempTimeInMinutesAtLastPixel = tempPoint[0];

		canvasMaxX = canvasSizeX - 1;

		updateVisibleDataRange(tempTimeInMinutesAtFirstPixel, tempTimeInMinutesAtLastPixel);
	}

	void updateDataRange(int startTimeInMinutes, int endTimeInMinutes) {
		if (startTimeInMinutes < this.startTimeInMinutes) {
			this.startTimeInMinutes = startTimeInMinutes;
		}

		if (endTimeInMinutes > this.endTimeInMinutes) {
			this.endTimeInMinutes = endTimeInMinutes;
		}
	}

	void updateVisibleDataRange(double timeInMinutesAtFirstPixel, double timeInMinutesAtLastPixel) {
		mainTransform.identity();

		double scaleX = (double) canvasMaxX / (timeInMinutesAtLastPixel - timeInMinutesAtFirstPixel);
		mainTransform.scale(scaleX, 1.0d);
		mainTransform.translate(-timeInMinutesAtFirstPixel + 0.5d / scaleX, 0);

		magnification = Double.NaN;
		magnificationTransform = null;
	}

	void gestureBegin(int mouseX) {
		magnifyAroundMouseX = mouseX;

		tempPoint[0] = 0.5d;
		mainTransform.inverseTransform(tempPoint);
		double startTimeX = tempPoint[0];

		tempPoint[0] = (double) canvasMaxX + 0.5d;
		mainTransform.inverseTransform(tempPoint);
		double endTimeX = tempPoint[0];

		//TODO there should probably be a maximum, too, that limits the scale to one minute per pixel
		minimumAllowableMagnification = (endTimeX - startTimeX) / (endTimeInMinutes - startTimeInMinutes);
	}

	public void gestureMagnify(double magnification) {
		this.magnification = Math.max(minimumAllowableMagnification, magnification);
		magnificationTransform = null;
	}

	public void gesturePan(int xDirection) {
		if (xDirection == 0) {
			return;
		}

		tempPoint[0] = 0.0d;
		mainTransform.inverseTransform(tempPoint);
		double timeInMinutesAtZeroPixel = tempPoint[0];

		tempPoint[0] = 1.0d;
		mainTransform.inverseTransform(tempPoint);
		double timeInMinutesAtOnePixel = tempPoint[0];

		mainTransform.translate((timeInMinutesAtOnePixel - timeInMinutesAtZeroPixel) * xDirection * PAN_SPEED, 0.0d);

		forceEndPoints(mainTransform);
	}

	public void gestureEnd() {
		if (!Double.isNaN(magnification)) {
			magnifyTransform(mainTransform);
			magnification = Double.NaN;
		}
	}

	private void magnifyTransform(Transform2D transform) {
		tempPoint[0] = magnifyAroundMouseX + 0.5d;
		transform.inverseTransform(tempPoint);

		transform.translate(tempPoint[0], 0.0d);
		transform.scale((double) magnification, 1.0d);
		transform.translate(-tempPoint[0], 0.0d);

		forceEndPoints(transform);
	}

	private void forceEndPoints(Transform2D transform) {
		tempPoint[0] = 0.5d;
		transform.inverseTransform(tempPoint);
		double tempTimeInMinutesAtFirstPixel = tempPoint[0];

		if (tempTimeInMinutesAtFirstPixel < startTimeInMinutes) {
			transform.translate(tempTimeInMinutesAtFirstPixel - startTimeInMinutes, 0);
		}

		tempPoint[0] = canvasMaxX + 0.5d;
		transform.inverseTransform(tempPoint);
		double tempTimeInMinutesAtLastPixel = tempPoint[0];

		if (tempTimeInMinutesAtLastPixel > endTimeInMinutes) {
			transform.translate(tempTimeInMinutesAtLastPixel - endTimeInMinutes, 0);
		}
	}

	public int timeInMinutesToPixelX(int timeInMinutes) {
		Transform2D currentTransform = getCurrentTransform();
		tempPoint[0] = (double) timeInMinutes;
		currentTransform.transform(tempPoint);
		return (int) tempPoint[0];
	}

	public int pixelXToTimeInMinutes(int pixelX) {
		Transform2D currentTransform = getCurrentTransform();
		tempPoint[0] = pixelX + 0.5d;
		currentTransform.inverseTransform(tempPoint);
		return (int) tempPoint[0];
	}

	private Transform2D getCurrentTransform() {
		if (!Double.isNaN(magnification)) {
			if (magnificationTransform == null) {
				magnificationTransform = new Transform2D(mainTransform);
				magnifyTransform(magnificationTransform);
			}

			return magnificationTransform;
		}

		return mainTransform;
	}
}
