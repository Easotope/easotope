/*
 * Copyright © 2016 by Devon Bowen.
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

import org.easotope.client.core.Transform2D;


public class CoordinateTransform {
	private final double PAN_SPEED = 2;

	private double[] tempPoint = new double[] { 0.0d, 0.0d };

	private int canvasMaxX;
	private int canvasMaxY;

	private Transform2D mainTransform = null;

	private double magnification = Double.NaN;
	private int[] magnifyAroundMouse = new int[] { 0, 0 };
	private Transform2D magnificationTransform = null;

	public boolean isReady() {
		return mainTransform != null;
	}

	// This needs to be called before initX. X can't be initialized until we know Y because we don't know how wide the labels might be.
	void initY(int canvasSizeY, int pixelMinY, int pixelMaxY, double coordinateAtPixelMinY, double coordinateAtPixelMaxY) {
		mainTransform = new Transform2D();
		magnificationTransform = null;

		canvasMaxY = canvasSizeY - 1;

		double scaleY = ((double) pixelMaxY - pixelMinY) / (coordinateAtPixelMaxY - coordinateAtPixelMinY);

		mainTransform.scale(1.0d, scaleY);
		mainTransform.translate(0.0d, -coordinateAtPixelMinY);
		mainTransform.translate(0.0d, (pixelMinY + 0.5d) / scaleY);
	}

	// This needs to be called after initY. Only then to we know the pixelMinX value because we need to calculate label width.
	void initX(int canvasSizeX, int pixelMinX, int pixelMaxX, double coordinateAtPixelMinX, double coordinateAtPixelMaxX) {
		canvasMaxX = canvasSizeX - 1;

		double scaleX = ((double) pixelMaxX - pixelMinX) / (coordinateAtPixelMaxX - coordinateAtPixelMinX);

		mainTransform.scale(scaleX, 1.0d);
		mainTransform.translate(-coordinateAtPixelMinX, 0.0d);
		mainTransform.translate((pixelMinX + 0.5d) / scaleX, 0.0d);
	}

	void updateCanvasSize(int canvasSizeX, int canvasSizeY) {
		if (!isReady()) {
			return;
		}

		tempPoint[0] = 0.5d;
		tempPoint[1] = 0.5d;
		mainTransform.inverseTransform(tempPoint);
		double coordinateAtFirstPixelX = tempPoint[0];
		double coordinateAtFirstPixelY = tempPoint[1];

		tempPoint[0] = canvasMaxX + 0.5d;
		tempPoint[1] = canvasMaxY + 0.5d;
		mainTransform.inverseTransform(tempPoint);
		double coordinateAtLastPixelX = tempPoint[0];
		double coordinateAtLastPixelY = tempPoint[1];

		canvasMaxX = canvasSizeX - 1;
		canvasMaxY = canvasSizeY - 1;

		updateVisibleDataRange(coordinateAtFirstPixelX, coordinateAtLastPixelX, coordinateAtFirstPixelY, coordinateAtLastPixelY);
	}

	void updateVisibleDataRange(double coordinateAtFirstPixelX, double coordinateAtLastPixelX, double coordinateAtFirstPixelY, double coordinateAtLastPixelY) {
		double scaleX = (double) canvasMaxX / (coordinateAtLastPixelX - coordinateAtFirstPixelX);
		double scaleY = (double) canvasMaxY / (coordinateAtLastPixelY - coordinateAtFirstPixelY);

		mainTransform.identity();
		mainTransform.scale(scaleX, scaleY);
		mainTransform.translate(-coordinateAtFirstPixelX + 0.5d / scaleX, -coordinateAtFirstPixelY + 0.5d / scaleY);

		magnificationTransform = null;
	}

	void gestureBegin(int mouseX, int mouseY) {
		magnifyAroundMouse[0] = mouseX;
		magnifyAroundMouse[1] = mouseY;
	}

	public void gestureMagnify(double magnification) {
		this.magnification = magnification;
		magnificationTransform = null;
	}

	public void gesturePan(int xDirection, int yDirection) {
		if (xDirection == 0 && yDirection == 0) {
			return;
		}

		tempPoint[0] = 0.5d;
		tempPoint[1] = 0.5d;
		mainTransform.inverseTransform(tempPoint);
		double coordinateAtZeroPixelX = tempPoint[0];
		double coordinateAtZeroPixelY = tempPoint[1];

		tempPoint[0] = 1.5d;
		tempPoint[1] = 1.5d;
		mainTransform.inverseTransform(tempPoint);
		double coordinateAtOnePixelX = tempPoint[0];
		double coordinateAtOnePixelY = tempPoint[1];

		double x = (coordinateAtOnePixelX - coordinateAtZeroPixelX) * xDirection * PAN_SPEED;
		double y = (coordinateAtOnePixelY - coordinateAtZeroPixelY) * yDirection * PAN_SPEED;

		mainTransform.translate(x, y);
	}

	public void gestureEnd() {
		if (!Double.isNaN(magnification)) {
			magnifyTransform(mainTransform);
			magnification = Double.NaN;
		}
	}

	private void magnifyTransform(Transform2D transform) {
		tempPoint[0] = magnifyAroundMouse[0] + 0.5d;
		tempPoint[1] = magnifyAroundMouse[1] + 0.5d;
		transform.inverseTransform(tempPoint);

		transform.translate(tempPoint[0], tempPoint[1]);
		transform.scale(magnification, magnification);
		transform.translate(-tempPoint[0], -tempPoint[1]);
	}

	public double[] coordinateToPixel(double coordinateX, double coordinateY) {
		Transform2D currentTransform = getCurrentTransform();
		tempPoint[0] = coordinateX;
		tempPoint[1] = coordinateY;
		currentTransform.transform(tempPoint);
		return tempPoint;
	}

	public double[] pixelToCoordinate(int pixelX, int pixelY) {
		Transform2D currentTransform = getCurrentTransform();
		tempPoint[0] = pixelX + 0.5d;
		tempPoint[1] = pixelY + 0.5d;
		currentTransform.inverseTransform(tempPoint);
		return tempPoint;
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
