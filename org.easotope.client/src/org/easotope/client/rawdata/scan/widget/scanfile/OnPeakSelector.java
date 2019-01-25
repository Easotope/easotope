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

package org.easotope.client.rawdata.scan.widget.scanfile;

import java.util.ArrayList;
import java.util.Vector;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.widgets.graph.DrawableObject;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.MenuItemListener;
import org.easotope.client.core.widgets.graph.drawables.Curve;
import org.easotope.client.core.widgets.graph.drawables.XRangeSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class OnPeakSelector extends Graph {
	private boolean drawWithLines = true;
	private boolean shiftMode = false;
	private double originalX1;
	private double originalX2;
	private boolean widgetIsEnabled = true;

	private XRangeSelection xRangeSelection = null;
	private XRangeSelection currentXRangeSelection = null;

	private Vector<OnPeakSelectorListener> listeners = new Vector<OnPeakSelectorListener>();

	public OnPeakSelector(Composite parent, int style) {
		super(parent, style);

		xRangeSelection = new XRangeSelection(ColorCache.getColor(getDisplay(), ColorCache.GREY));
		addDrawableObjectFirst(xRangeSelection);

		addMenuItem(new MenuItemListener() {
			@Override
			public void handleEvent(Event event) {
				drawWithLines = !drawWithLines;

				for (DrawableObject drawableObject : getDrawableObjects()) {
					if (drawableObject instanceof Curve) {
						Curve curve = (Curve) drawableObject;
						curve.setWithLines(drawWithLines);
					}
				}

				clearAndRedraw();
			}

			@Override
			public String getName() {
				return Messages.byFileWidget_toggleLinesMenuItem;
			}
		});
	}

	@Override
	public void setEnabled(boolean enabled) {
		widgetIsEnabled = enabled;

		if (!widgetIsEnabled && currentXRangeSelection != null) {
			currentXRangeSelection = null;
			broadcastNewOnPeakSelection();
		}
	}

	@Override
	public void removeAllDrawableObjects() {
		for (DrawableObject drawableObject : new ArrayList<DrawableObject>(getDrawableObjects())) {
			if (drawableObject != xRangeSelection) {
				removeDrawableObject(drawableObject);
			}
		}
	}

	@Override
	protected void leftMouseDown(MouseEvent e) {
		if (!widgetIsEnabled) {
			return;
		}

		double[] coordinate = getCoordinateTransform().pixelToCoordinate(e.x, e.y);
		currentXRangeSelection = xRangeSelection;

		shiftMode = (e.stateMask & SWT.SHIFT) != 0 && (!Double.isNaN(currentXRangeSelection.getX1()) || !Double.isNaN(currentXRangeSelection.getX2()));

		if (shiftMode) {
			originalX1 = currentXRangeSelection.getX1();
			originalX2 = currentXRangeSelection.getX2();

			if (Double.isNaN(originalX1) || Math.abs(originalX1-coordinate[0]) < Math.abs(originalX2-coordinate[0])) {
				currentXRangeSelection.setX1(coordinate[0]);
			} else {
				currentXRangeSelection.setX2(coordinate[0]);
			}

		} else {
			currentXRangeSelection.setX1(coordinate[0]);
			currentXRangeSelection.setX2(Double.NaN);
		}

		clearAndRedraw();
	}

	@Override
	protected void mouseMoved(MouseEvent e) {
		if (currentXRangeSelection == null) {
			return;
		}

		double[] coordinate = getCoordinateTransform().pixelToCoordinate(e.x, e.y);

		if (shiftMode) {
			if (Math.abs(originalX1-coordinate[0]) < Math.abs(originalX2-coordinate[0])) {
				currentXRangeSelection.setX1(coordinate[0]);
				currentXRangeSelection.setX2(originalX2);
			} else {
				currentXRangeSelection.setX1(originalX1);
				currentXRangeSelection.setX2(coordinate[0]);
			}

		} else {
			currentXRangeSelection.setX2(coordinate[0]);
		}

		clearAndRedraw();
	}

	@Override
	protected void leftMouseUp(MouseEvent e) {
		if (currentXRangeSelection != null) {
			currentXRangeSelection = null;
			broadcastNewOnPeakSelection();
		}
	}

	public XRangeSelection getXRangeSelection() {
		return xRangeSelection;
	}

	private void broadcastNewOnPeakSelection() {
		for (OnPeakSelectorListener listener : listeners) {
			listener.onPeakSelectionChanged();
		}
	}

	public void addListener(OnPeakSelectorListener listener) {
		listeners.add(listener);
	}
}
