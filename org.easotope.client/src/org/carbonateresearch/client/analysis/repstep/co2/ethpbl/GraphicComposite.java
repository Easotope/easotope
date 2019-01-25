/*
 * Copyright © 2019 by Cédric John.
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

package org.carbonateresearch.client.analysis.repstep.co2.ethpbl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.carbonateresearch.shared.analysis.repstep.co2.ethpbl.Calculator.BackgroundPoint;
import org.carbonateresearch.shared.analysis.repstep.co2.ethpbl.Calculator;
import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepGraphicComposite;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.MenuItemListener;
import org.easotope.client.core.widgets.graph.drawables.LineWithEnds;
import org.easotope.client.core.widgets.graph.drawables.LineWithoutEnds;
import org.easotope.client.core.widgets.graph.drawables.Point;
import org.easotope.client.rawdata.navigator.PartManager;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class GraphicComposite extends RepStepGraphicComposite {
	private Graph graph;
	private static final String[] inputLabelBackgrounds = new String[] { Calculator.INPUT_LABEL_V44_BACKGROUND, Calculator.INPUT_LABEL_V45_BACKGROUND, Calculator.INPUT_LABEL_V46_BACKGROUND, Calculator.INPUT_LABEL_V47_BACKGROUND, Calculator.INPUT_LABEL_V48_BACKGROUND, Calculator.INPUT_LABEL_V49_BACKGROUND };
	private SortedCombo selectChannel;
	private HashMap<String, Object> backgrounds = null;
	private HashMap<String, Object> MoAvBackgrounds = null;
	private ArrayList<BackgroundPoint> backgroundPointsThisReplicate= null;
	private String maximumCycle="MAXIMUM_CYCLE_AND_MASS";

	public GraphicComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Label selectChannelLabel = new Label(this, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		selectChannelLabel.setLayoutData(formData);
		selectChannelLabel.setText("Select the channel:");

		selectChannel = new SortedCombo(this, SWT.READ_ONLY);
		selectChannel.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				updateGraph();
			}
		});
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(selectChannelLabel);
		selectChannel.setLayoutData(formData);

		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();
		possibilities.put(0, inputLabelBackgrounds[0]);
		possibilities.put(1, inputLabelBackgrounds[1]);
		possibilities.put(2, inputLabelBackgrounds[2]);
		possibilities.put(3, inputLabelBackgrounds[3]);
		possibilities.put(4, inputLabelBackgrounds[4]);
		possibilities.put(5, inputLabelBackgrounds[5]);
		
		selectChannel.setPossibilities(possibilities);
		selectChannel.selectInteger(3);
		
		graph = new Graph(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(selectChannel);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		graph.setLayoutData(formData);
		graph.setUnitsAreEqual(false);
		graph.setVerticalAxisLabel("Backound (mv)");
		graph.setVerticalAxisShowLabel(true);
		graph.setVerticalAxisShowValues(true);
		graph.setHorizontalAxisLabel("Time stamp");
		graph.setHorizontalAxisShowLabel(true);
		graph.setHorizontalAxisShowValues(true);
    }

	@Override
	protected void handleDispose() {

	}

	public void updateGraph() {
		int selectedChannel = selectChannel.getSelectedInteger();
		
		@SuppressWarnings("unchecked")
		ArrayList<BackgroundPoint> backgroundPoints = (ArrayList<BackgroundPoint>) backgrounds.get(inputLabelBackgrounds[selectedChannel]);
		
		@SuppressWarnings("unchecked")
		ArrayList<BackgroundPoint> backgroundPointsMoAv = (ArrayList<BackgroundPoint>) MoAvBackgrounds.get(inputLabelBackgrounds[selectedChannel]);

		BackgroundPoint thisBackground=backgroundPointsThisReplicate.get(selectedChannel);
		
		Collections.sort(backgroundPoints);
		Collections.sort(backgroundPointsMoAv);
		
		graph.removeAllDrawableObjects();

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		
		graph.setVerticalAxisLabel("Background (mV) at m/z 44 of " + maximumCycle + " (mV)");
		
		BackgroundPoint previousBackground = null;
		
		// Adding the activated and deactivated background points
		for (BackgroundPoint backgroundPoint : backgroundPoints) {
			double background = backgroundPoint.getBackground();
			long date = backgroundPoint.getDate();

			int scanId = backgroundPoint.getScanId();
			boolean disabled = backgroundPoint.getDisabled();

			minX = Math.min(minX, date);
		    maxX = Math.max(maxX, date);
		    minY = Math.min(minY, background);
			maxY = Math.max(maxY, background);
	
			String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			String timestamp = DateFormat.format(date, timeZone, showTimeZone, false);
			String[] tooltip;
			
			PointDesign pointDesign = null;
			
			if (!disabled){
				pointDesign = new PointDesign(getDisplay(), ColorCache.getColor(getDisplay(), ColorCache.BLACK), PointStyle.S);
				tooltip = new String[] {"Enabled", timestamp, "Background: " + String.valueOf(background)};
			} else {
				pointDesign = new PointDesign(getDisplay(), ColorCache.getColor(getDisplay(), ColorCache.GREY), PointStyle.S);
				tooltip = new String[] {"Disabled", timestamp, "Background: " + String.valueOf(background)};
			}
			
			Point point = new Point(date, background, pointDesign);
			point.setTooltip(tooltip);
			
			graph.addDrawableObjectFirst(point);
			
			if (previousBackground!=null && !disabled) {
				graph.addDrawableObjectLast(new LineWithEnds(previousBackground.getDate(), previousBackground.getBackground(), date, background, ColorCache.getColor(getDisplay(), ColorCache.GREY), null));
				previousBackground=backgroundPoint;
			}
			
			if (!disabled) {
				previousBackground=backgroundPoint;
			}

			if (scanId != -1) {
				point.addMenuItem(new MenuItemListener() {
					@Override
					public void handleEvent(Event event) {
						PartManager.showRawDataPerspective(getEasotopePart());
						PartManager.openScanReplicate(getEasotopePart(), scanId);
					}

					@Override
					public String getName() {
						return Messages.co2EthPBL_openScan;
					}
				});
//				point.addMenuItem(new MenuItemListener() {
//					@Override
//					public void handleEvent(Event event) {
//						InputCache.getInstance().replicateDisabledStatusUpdate(scanId, !disabled);
//					}
//	
//					@Override
//					public String getName() {
//						return disabled ? Messages.co2EthPBL_enableScan : Messages.co2EthPBL_disableScan;
//					}
//				});
			}
		}
		
		previousBackground = null;
		
		// Adding the moving average of all backgrounds
		for (BackgroundPoint backgroundPoint : backgroundPointsMoAv) {
			double background = backgroundPoint.getBackground();
			long date = backgroundPoint.getDate();

			minX = Math.min(minX, date);
		    maxX = Math.max(maxX, date);
		    minY = Math.min(minY, background);
			maxY = Math.max(maxY, background);
			
			if (previousBackground!=null) {
				graph.addDrawableObjectLast(new LineWithEnds(previousBackground.getDate(), previousBackground.getBackground(), date, background, ColorCache.getColor(getDisplay(), ColorCache.RED), null));
			}

			previousBackground=backgroundPoint;
		}

		// Adding the graphics for this particular background value

		double background = thisBackground.getBackground();
		long date = thisBackground.getDate();

		minX = Math.min(minX, date);
	    maxX = Math.max(maxX, date);
	    minY = Math.min(minY, background);
		maxY = Math.max(maxY, background);

		String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
		boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
		String timestamp = DateFormat.format(date, timeZone, showTimeZone, false);
		String[] tooltip;

		tooltip = new String[] { timestamp, "Background: " + String.valueOf(background) };

		PointDesign pointDesign = null;

		pointDesign = new PointDesign(getDisplay(), ColorCache.getColor(getDisplay(), ColorCache.BLACK), PointStyle.X);
		Point point = new Point(date, background, pointDesign);
		
		point.setTooltip(tooltip);

		graph.addDrawableObjectLast(point);
		graph.addDrawableObjectLast(new LineWithoutEnds((double) date, ColorCache.getColor(getDisplay(), ColorCache.BLACK), (double) date, (double) date, null));
	
		graph.autoScale();

		layout();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void newCalculation(ScratchPad<ReplicatePad> scratchPad) {
		ReplicatePad replicatePad = scratchPad.getChildren().get(0);
		
		this.backgrounds = (HashMap<String,Object>) replicatePad.getVolatileData(Calculator.getVolatileDataAllBackgroundsKey());
		this.MoAvBackgrounds = (HashMap<String,Object>) replicatePad.getVolatileData(Calculator.getVolatileDataAverageBackgroundsKey());
		this.backgroundPointsThisReplicate = (ArrayList<BackgroundPoint>) replicatePad.getVolatileData(Calculator.getVolatileDataThisReplicateBackgroundsKey());
		this.maximumCycle = (String) replicatePad.getVolatileData(Calculator.getVolatileDataMaximumCycle());
		
		updateGraph();
	}
}

