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

package org.easotope.client.analysis.repstep.superclass.drift;

import java.util.HashSet;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepGraphicComposite;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.MenuItemListener;
import org.easotope.client.core.widgets.graph.drawables.LineWithEnds;
import org.easotope.client.core.widgets.graph.drawables.LineWithoutEnds;
import org.easotope.client.core.widgets.graph.drawables.Point;
import org.easotope.client.rawdata.navigator.PartManager;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.repstep.superclass.drift.Calculator.DriftPoint;
import org.easotope.shared.analysis.repstep.superclass.drift.VolatileKeys;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.math.LinearRegression;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public abstract class GraphicComposite extends RepStepGraphicComposite {
	private StackLayout stackLayout;

	private Composite stretchedComposite;
	private Label slope;
	private Label intercept;
	private Label stretchedOffset;
	private Graph stretchedGraph;

	private Composite unstretchedComposite;
	private Label unstretchedOffset;
	private Graph unstretchedGraph;

	public abstract VolatileKeys getVolatileKeys();
	public abstract String getHorizontalLabel();
	public abstract String getVerticalLabel();

	public GraphicComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
		setLayout(stackLayout = new StackLayout());

		stretchedComposite = new Composite(this, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		stretchedComposite.setLayout(formLayout);

		Label label = new Label(stretchedComposite, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		label.setLayoutData(formData);
		label.setText(Messages.co2SuperDriftGraphicComposite_slope);

		slope = new Label(stretchedComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(label);
		slope.setLayoutData(formData);

		Label label2 = new Label(stretchedComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		label2.setLayoutData(formData);
		label2.setText(Messages.co2SuperDriftGraphicComposite_intercept);

		intercept = new Label(stretchedComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(label2);
		intercept.setLayoutData(formData);

		label = new Label(stretchedComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label2);
		formData.left = new FormAttachment(0);
		label.setLayoutData(formData);
		label.setText(Messages.co2SuperDriftGraphicComposite_offset);

		stretchedOffset = new Label(stretchedComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label2);
		formData.left = new FormAttachment(label);
		stretchedOffset.setLayoutData(formData);

		stretchedGraph = new Graph(stretchedComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		stretchedGraph.setLayoutData(formData);
		stretchedGraph.setUnitsAreEqual(true);
		stretchedGraph.setHorizontalAxisLabel(getHorizontalLabel());
		stretchedGraph.setHorizontalAxisShowLabel(true);
		stretchedGraph.setHorizontalAxisShowValues(true);
		stretchedGraph.setVerticalAxisLabel(getVerticalLabel());
		stretchedGraph.setVerticalAxisShowLabel(true);
		stretchedGraph.setVerticalAxisShowValues(true);

		unstretchedComposite = new Composite(this, SWT.NONE);
		formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		unstretchedComposite.setLayout(formLayout);

		label = new Label(unstretchedComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		label.setLayoutData(formData);
		label.setText(Messages.co2SuperDriftGraphicComposite_offset);

		unstretchedOffset = new Label(unstretchedComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(label);
		unstretchedOffset.setLayoutData(formData);

		unstretchedGraph = new Graph(unstretchedComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		unstretchedGraph.setLayoutData(formData);
		unstretchedGraph.setUnitsAreEqual(true);
		unstretchedGraph.setHorizontalAxisLabel(getHorizontalLabel());
		unstretchedGraph.setHorizontalAxisShowLabel(true);
		unstretchedGraph.setHorizontalAxisShowValues(true);
		unstretchedGraph.setVerticalAxisLabel(getVerticalLabel());
		unstretchedGraph.setVerticalAxisShowLabel(true);
		unstretchedGraph.setVerticalAxisShowValues(true);

		stackLayout.topControl = stretchedComposite;
		layout();
    }

	@Override
	protected void handleDispose() {

	}

	@Override
	public void newCalculation(ScratchPad<ReplicatePad> scratchPad) {
		ReplicatePad replicatePad = scratchPad.getChildren().get(0);

		@SuppressWarnings("unchecked")
		HashSet<DriftPoint> driftPoints = (HashSet<DriftPoint>) replicatePad.getVolatileData(getVolatileKeys().getVolatileDataDriftPointsKey());
		LinearRegression linearRegression = (LinearRegression) replicatePad.getVolatileData(getVolatileKeys().getVolatileDataLinearRegressionKey());
		Double offset = (Double) replicatePad.getVolatileData(getVolatileKeys().getVolatileDataOffsetKey());

		if (linearRegression != null) {
			newStretchedData(driftPoints, linearRegression, offset);
		} else {
			newUnstretchedData(driftPoints, offset);
		}
	}

	private void newStretchedData(HashSet<DriftPoint> driftPoints, LinearRegression linearRegression, Double offset) {
		slope.setText(String.valueOf(linearRegression.getSlope()));
		intercept.setText(String.valueOf(linearRegression.getIntercept()));
		stretchedOffset.setText(String.valueOf(offset));

		stretchedGraph.removeAllDrawableObjects();

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;

		for (DriftPoint driftPoint : driftPoints) {
			int replicateId = driftPoint.getReplicateId();
			Standard standard = driftPoint.getStandard();
			double measuredValue = driftPoint.getMeasuredValue();
			double expectedValue = driftPoint.getExpectedValue();
			long date = driftPoint.getDate();
			boolean disabled = driftPoint.getDisabled();

			minX = Math.min(minX, measuredValue);
			maxX = Math.max(maxX, measuredValue);

			String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			String timestamp = DateFormat.format(date, timeZone, showTimeZone, false);
			String[] tooltip = null;
			PointDesign pointDesign = null;
			boolean influencesAutoscale = true;

			if (standard != null) {
				Color color;

				if (disabled) {
					tooltip = new String[] { standard.getName() + " " + Messages.co2SuperDriftGraphicComposite_disabled, timestamp, Messages.co2SuperDriftGraphicComposite_measured + String.valueOf(measuredValue), Messages.co2SuperDriftGraphicComposite_expected + String.valueOf(expectedValue) };
					color = ColorCache.getFadedColorFromPalette(getDisplay(), standard.getColorId());
					influencesAutoscale = false;				
				} else {
					tooltip = new String[] { standard.getName(), timestamp, Messages.co2SuperDriftGraphicComposite_measured + String.valueOf(measuredValue), Messages.co2SuperDriftGraphicComposite_expected + String.valueOf(expectedValue) };
					color = ColorCache.getColorFromPalette(getDisplay(), standard.getColorId());
				}

				pointDesign = new PointDesign(getDisplay(), color, PointStyle.values()[standard.getShapeId()]);

			} else {
				tooltip = new String[] { timestamp, Messages.co2SuperDriftGraphicComposite_measured + String.valueOf(measuredValue), Messages.co2SuperDriftGraphicComposite_afterCorrection + String.valueOf(measuredValue+offset) };
				pointDesign = new PointDesign(getDisplay(), ColorCache.getColor(getDisplay(), ColorCache.BLACK), PointStyle.X);

				stretchedGraph.addDrawableObjectLast(new LineWithEnds(measuredValue, expectedValue, measuredValue+offset, expectedValue, ColorCache.getColor(getDisplay(), ColorCache.BLACK), SWT.LINE_DOT, null));
			}

			Point point = new Point(measuredValue, expectedValue, pointDesign);
			point.setTooltip(tooltip);
			point.setInfluencesAutoscale(influencesAutoscale);
			if (standard != null) {
				point.addMenuItem(new MenuItemListener() {
					@Override
					public void handleEvent(Event event) {
						PartManager.showRawDataPerspective(getEasotopePart());
						PartManager.openStandardReplicate(getEasotopePart(), replicateId);
					}

					@Override
					public String getName() {
						return Messages.co2SuperDriftGraphicComposite_openReplicate;
					}
				});
				if (LoginInfoCache.getInstance().getPermissions().isCanEditAllReplicates()) {
					point.addMenuItem(new MenuItemListener() {
						@Override
						public void handleEvent(Event event) {
							InputCache.getInstance().replicateDisabledStatusUpdate(replicateId, !disabled);
						}
		
						@Override
						public String getName() {
							return disabled ? Messages.co2SuperDriftGraphicComposite_enableReplicate : Messages.co2SuperDriftGraphicComposite_disableReplicate;
						}
					});
				}
			}

			stretchedGraph.addDrawableObjectFirst(point);
		}

		double minY = linearRegression.getSlope() * minX + linearRegression.getIntercept();
		double maxY = linearRegression.getSlope() * maxX + linearRegression.getIntercept();

		stretchedGraph.addDrawableObjectFirst(new LineWithEnds(minX, minY, maxX, maxY, ColorCache.getColor(getDisplay(), ColorCache.BLACK), null));
		stretchedGraph.autoScale();

		stretchedComposite.layout();
		stackLayout.topControl = stretchedComposite;
		layout();
	}

	private void newUnstretchedData(HashSet<DriftPoint> driftPoints, Double offset) {
		unstretchedOffset.setText(String.valueOf(offset));
		unstretchedGraph.removeAllDrawableObjects();

		long earliestTimeInSeconds = Long.MAX_VALUE;

		for (DriftPoint run : driftPoints) {
			earliestTimeInSeconds = Math.min(earliestTimeInSeconds, run.getDate() / 1000);
		}

		for (DriftPoint driftPoint : driftPoints) {
			int replicateId = driftPoint.getReplicateId();
			Standard standard = driftPoint.getStandard();
			double measuredValue = driftPoint.getMeasuredValue();
			double expectedValue = driftPoint.getExpectedValue();
			long date = driftPoint.getDate();
			boolean disabled = driftPoint.getDisabled();

			String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			String timestamp = DateFormat.format(date, timeZone, showTimeZone, false);
			String[] tooltip = null;
			PointDesign pointDesign = null;
			boolean influencesAutoscale = true;

			if (standard != null) {
				Color color;

				if (disabled) {
					tooltip = new String[] { standard.getName() + " " + Messages.co2SuperDriftGraphicComposite_disabled, timestamp, Messages.co2SuperDriftGraphicComposite_measured + String.valueOf(measuredValue), Messages.co2SuperDriftGraphicComposite_expected + String.valueOf(expectedValue) };
					color = ColorCache.getFadedColorFromPalette(getDisplay(), standard.getColorId());
					influencesAutoscale = false;				
				} else {
					tooltip = new String[] { standard.getName(), timestamp, Messages.co2SuperDriftGraphicComposite_measured + String.valueOf(measuredValue), Messages.co2SuperDriftGraphicComposite_expected + String.valueOf(expectedValue) };
					color = ColorCache.getColorFromPalette(getDisplay(), standard.getColorId());
					unstretchedGraph.addDrawableObjectFirst(new LineWithEnds(expectedValue, expectedValue, measuredValue, expectedValue, ColorCache.getColor(getDisplay(), ColorCache.BLACK), SWT.LINE_DOT, null));
				}

				pointDesign = new PointDesign(getDisplay(), color, PointStyle.values()[standard.getShapeId()]);

			} else {
				tooltip = new String[] { timestamp, Messages.co2SuperDriftGraphicComposite_measured + String.valueOf(measuredValue), Messages.co2SuperDriftGraphicComposite_afterCorrection + String.valueOf(expectedValue) };
				pointDesign = new PointDesign(getDisplay(), ColorCache.getColor(getDisplay(), ColorCache.BLACK), PointStyle.X);
				unstretchedGraph.addDrawableObjectFirst(new LineWithEnds(measuredValue, expectedValue, expectedValue, expectedValue, ColorCache.getColor(getDisplay(), ColorCache.BLACK), SWT.LINE_DOT, null));
			}

			Point point = new Point(measuredValue, expectedValue, pointDesign);
			point.setTooltip(tooltip);
			point.setInfluencesAutoscale(influencesAutoscale);
			if (standard != null) {
				point.addMenuItem(new MenuItemListener() {
					@Override
					public void handleEvent(Event event) {
						PartManager.showRawDataPerspective(getEasotopePart());
						PartManager.openStandardReplicate(getEasotopePart(), replicateId);
					}

					@Override
					public String getName() {
						return Messages.co2SuperDriftGraphicComposite_openReplicate;
					}
				});
				point.addMenuItem(new MenuItemListener() {
					@Override
					public void handleEvent(Event event) {
						InputCache.getInstance().replicateDisabledStatusUpdate(replicateId, !disabled);
					}
	
					@Override
					public String getName() {
						return disabled ? Messages.co2SuperDriftGraphicComposite_enableReplicate : Messages.co2SuperDriftGraphicComposite_disableReplicate;
					}
				});
			}
			
			unstretchedGraph.addDrawableObjectLast(point);
		}

		unstretchedGraph.addDrawableObjectFirst(new LineWithoutEnds(1.0d, 0.0d, ColorCache.getColor(getDisplay(), ColorCache.BLACK), null));
		unstretchedGraph.autoScale();

		unstretchedComposite.layout();
		stackLayout.topControl = unstretchedComposite;
		layout();
	}
}
