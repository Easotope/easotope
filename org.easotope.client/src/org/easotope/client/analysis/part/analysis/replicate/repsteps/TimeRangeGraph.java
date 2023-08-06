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

package org.easotope.client.analysis.part.analysis.replicate.repsteps;

import java.util.Vector;

import org.easotope.client.core.ColorCache;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.drawables.XRangeSelection;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.widgets.Composite;

public class TimeRangeGraph extends Graph {
	private boolean xRangeEnabled = false;
	private Double startTime;
	private XRangeSelection xRangeSelection;

	private Vector<TimeRangeSelectorListener> listeners = new Vector<TimeRangeSelectorListener>();

	public TimeRangeGraph(Composite parent, int style) {
		super(parent, style);
	}

	public void setXRangeEnabled(boolean xRangeEnabled) {
		this.xRangeEnabled = xRangeEnabled;
		clearAndRedraw();
	}

	public void setTimeRange(Double startTime, Double endTime) {
		removeDrawableObject(xRangeSelection);

		if (startTime != null) {
			xRangeSelection = new XRangeSelection(startTime, endTime, ColorCache.getColor(getDisplay(), ColorCache.LIGHT_GREY));
			addDrawableObjectFirst(xRangeSelection);
		}

		this.startTime = null;
		xRangeSelection = null;

		clearAndRedraw();
	}

	public double getStartTime() {
		if (xRangeSelection == null) {
			return Double.NaN;
		}

		double x1 = xRangeSelection.getX1();
		double x2 = xRangeSelection.getX2();

		if (Double.isNaN(x1) || Double.isNaN(x2)) {
			return Double.NaN;
		}
		
		return Math.min(x1, x2);
	}

	public double getEndTime() {
		if (xRangeSelection == null) {
			return Double.NaN;
		}

		double x1 = xRangeSelection.getX1();
		double x2 = xRangeSelection.getX2();

		if (Double.isNaN(x1) || Double.isNaN(x2)) {
			return Double.NaN;
		}

		return Math.max(x1, x2);
	}

	@Override
	protected void leftMouseDown(MouseEvent e) {
		if (!xRangeEnabled) {
			return;
		}

		startTime = getCoordinateTransform().pixelToCoordinate(e.x, 0)[0];
		removeDrawableObject(xRangeSelection);
		xRangeSelection = null;
		
		broadcastTimeRangeSelectionStarted();
	}

	@Override
	protected void mouseMoved(MouseEvent e) {
		if (startTime == null) {
			return;
		}

		Double endTime = getCoordinateTransform().pixelToCoordinate(e.x, 0)[0];
		removeDrawableObject(xRangeSelection);
		xRangeSelection = new XRangeSelection(startTime, endTime, ColorCache.getColor(getDisplay(), ColorCache.LIGHT_GREY));
	}

	@Override
	protected void leftMouseUp(MouseEvent e) {
		if (startTime == null ) {
			return;
		}

		double endTime = getCoordinateTransform().pixelToCoordinate(e.x, 0)[0];

		if (startTime != endTime) {
			xRangeSelection = new XRangeSelection(startTime, endTime, ColorCache.getColor(getDisplay(), ColorCache.LIGHT_GREY));
		}

		startTime = null;
		broadcastTimeRangeSelectionComplete();
	}

	@Override
	protected void paintControl(PaintEvent e) {
		if (xRangeSelection != null) {
			removeDrawableObject(xRangeSelection);
		}

		if (xRangeEnabled && xRangeSelection != null) {
			addDrawableObjectFirst(xRangeSelection);
		}

		super.paintControl(e);
	}

	private void broadcastTimeRangeSelectionStarted() {
		for (TimeRangeSelectorListener listener : listeners) {
			listener.timeRangeSelectionStarted();
		}
	}

	private void broadcastTimeRangeSelectionComplete() {
		for (TimeRangeSelectorListener listener : listeners) {
			listener.timeRangeSelectionComplete();
		}
	}

	public void addListener(TimeRangeSelectorListener listener) {
		listeners.add(listener);
	}
}
