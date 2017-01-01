/*
 * Copyright © 2016-2017 by Devon Bowen.
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
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.widgets.graph.DrawableObject;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.MenuItemListener;
import org.easotope.client.core.widgets.graph.drawables.Curve;
import org.easotope.client.core.widgets.graph.drawables.XRangeSelection;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.ScanFile;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

public class BackgroundSelector extends Graph {
	private InputParameter inputParameter;
	private boolean drawWithLines = true;
	private boolean shiftMode = false;
	private double originalX1;
	private double originalX2;
	private double peakX;
	private boolean widgetIsEnabled = true;

	private XRangeSelection leftXRangeSelection = null;
	private XRangeSelection rightXRangeSelection = null;
	private XRangeSelection currentXRangeSelection = null;

	private Vector<BackgroundSelectorListener> listeners = new Vector<BackgroundSelectorListener>();

	public BackgroundSelector(Composite parent, int style) {
		super(parent, style);

		leftXRangeSelection = new XRangeSelection(ColorCache.getColor(getDisplay(), ColorCache.GREY));
		addDrawableObjectFirst(leftXRangeSelection);

		rightXRangeSelection = new XRangeSelection(ColorCache.getColor(getDisplay(), ColorCache.GREY));
		addDrawableObjectFirst(rightXRangeSelection);

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
				return Messages.byMassWidget_toggleLinesMenuItem;
			}
		});
	}

	public void setInputParameter(InputParameter inputParameter) {
		this.inputParameter = inputParameter;
	}

	public void setScanFiles(TreeSet<ScanFile> scanFiles) {
		boolean initiallyEmpty = getDrawableObjects().size() == 2;

		for (DrawableObject drawableObject : new ArrayList<DrawableObject>(getDrawableObjects())) {
			if (drawableObject instanceof Curve) {
				removeDrawableObject(drawableObject);
			}
		}

		if (scanFiles.size() == 0) {
			leftXRangeSelection.setX1(Double.NaN);
			leftXRangeSelection.setX2(Double.NaN);

			rightXRangeSelection.setX1(Double.NaN);
			rightXRangeSelection.setX2(Double.NaN);

			return;
		}

		double maxY = Double.NEGATIVE_INFINITY;

		int count = 0;
		for (ScanFile scanFile : scanFiles) {
			ScanFileParsedV2 scanFileParsed = scanFile.getScanFileParsed();
			HashMap<InputParameter,Double[]> measurements = scanFileParsed.getMeasurements();
			Double[] data = measurements.get(inputParameter);

			if (data == null) {
				continue;
			}

			double[] x = new double[data.length];
			double[] y = new double[data.length];

			double m = (scanFileParsed.getToVoltage()-scanFileParsed.getFromVoltage()) / (data.length-1);
			double b = scanFileParsed.getFromVoltage();

			for (int i=0; i<data.length; i++) {
				x[i] = m * i + b;
				y[i] = data[i];

				if (maxY < data[i]) {
					maxY = data[i];
					peakX = x[i];
				}
			}

			Curve curve = new Curve(x, y, ColorCache.getColorFromPalette(getDisplay(), count), new String[] { scanFile.getRawFile().getOriginalName() });
			curve.setAddXToTooltip(true);
			curve.setAddYToTooltip(true);
			curve.setWithLines(drawWithLines);

			addDrawableObjectLast(curve);
			count++;
		}

		if (initiallyEmpty) {
			autoScale();
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		widgetIsEnabled = enabled;

		if (!widgetIsEnabled && currentXRangeSelection != null) {
			currentXRangeSelection = null;
			broadcastNewBackgroundSelection();
		}
	}

	@Override
	protected void leftMouseDown(MouseEvent e) {
		if (!widgetIsEnabled) {
			return;
		}

		double[] coordinate = getCoordinateTransform().pixelToCoordinate(e.x, e.y);

		if (coordinate[0] > peakX) {
			currentXRangeSelection = rightXRangeSelection;
		} else {
			currentXRangeSelection = leftXRangeSelection;
		}

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
			broadcastNewBackgroundSelection();
		}
	}

	public XRangeSelection getLeftXRangeSelection() {
		return leftXRangeSelection;
	}

	public XRangeSelection getRightXRangeSelection() {
		return rightXRangeSelection;
	}

	private void broadcastNewBackgroundSelection() {
		for (BackgroundSelectorListener listener : listeners) {
			listener.backgroundSelectionChanged();
		}
	}

	public void addListener(BackgroundSelectorListener listener) {
		listeners.add(listener);
	}
}
