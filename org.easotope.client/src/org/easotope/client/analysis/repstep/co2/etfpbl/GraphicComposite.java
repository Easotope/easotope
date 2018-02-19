/*
 * Copyright © 2016-2018 by Devon Bowen.
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

package org.easotope.client.analysis.repstep.co2.etfpbl;

import java.util.HashSet;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepGraphicComposite;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.MenuItemListener;
import org.easotope.client.core.widgets.graph.drawables.Axes;
import org.easotope.client.core.widgets.graph.drawables.LineWithoutEnds;
import org.easotope.client.core.widgets.graph.drawables.Point;
import org.easotope.client.rawdata.navigator.PartManager;
import org.easotope.shared.analysis.repstep.co2.etfpbl.Calculator;
import org.easotope.shared.analysis.repstep.co2.etfpbl.Calculator.AverageLine;
import org.easotope.shared.analysis.repstep.co2.etfpbl.Calculator.GraphPoint;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.math.LinearRegression;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class GraphicComposite extends RepStepGraphicComposite {
	private Composite textComposite;
	private Label slope;
	private Label intercept;
	private Label r2;
	private Label offset;

	private Composite leftComposite;
	private Graph graph1;
	private FormData graph1FormData;

	private Composite rightComposite;
	private Graph graph2;
	private FormData graph2FormData;

	public GraphicComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
		setLayout(new FormLayout());

		textComposite = new Composite(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		textComposite.setLayoutData(formData);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		textComposite.setLayout(formLayout);

		Label slopeLabel = new Label(textComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(50);
		slopeLabel.setLayoutData(formData);
		slopeLabel.setText(Messages.co2Etf_slope);

		slope = new Label(textComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(slopeLabel);
		slope.setLayoutData(formData);

		Label interceptLabel = new Label(textComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(slopeLabel);
		formData.left = new FormAttachment(50);
		interceptLabel.setLayoutData(formData);
		interceptLabel.setText(Messages.co2Etf_intercept);

		intercept = new Label(textComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(slopeLabel);
		formData.left = new FormAttachment(interceptLabel);
		intercept.setLayoutData(formData);

		Label r2Label = new Label(textComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(interceptLabel);
		formData.left = new FormAttachment(50);
		r2Label.setLayoutData(formData);
		r2Label.setText(Messages.co2Etf_r2);

		r2 = new Label(textComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(interceptLabel);
		formData.left = new FormAttachment(r2Label);
		r2.setLayoutData(formData);

		Label offsetLabel = new Label(textComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(r2Label);
		formData.left = new FormAttachment(50);
		offsetLabel.setLayoutData(formData);
		offsetLabel.setText(Messages.co2Etf_offset);

		offset = new Label(textComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(r2Label);
		formData.left = new FormAttachment(offsetLabel);
		offset.setLayoutData(formData);

		leftComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(textComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(50);
		formData.bottom = new FormAttachment(100);
		leftComposite.setLayoutData(formData);
		formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		leftComposite.setLayout(formLayout);

		graph1 = new Graph(leftComposite, SWT.NONE);
		graph1FormData = new FormData();
		graph1FormData.top = new FormAttachment(0);
		graph1FormData.left = new FormAttachment(0);
		graph1FormData.right = new FormAttachment(100);
		graph1.setLayoutData(graph1FormData);
		graph1.setUnitsAreEqual(true);
		graph1.setVerticalAxisLabel(Messages.co2Etf_graph1VerticalLabel);
		graph1.setVerticalAxisShowLabel(true);
		graph1.setVerticalAxisShowValues(true);
		graph1.setHorizontalAxisLabel(Messages.co2Etf_graph1HorizontalLabel);
		graph1.setHorizontalAxisShowLabel(true);
		graph1.setHorizontalAxisShowValues(true);

		rightComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(textComposite);
		formData.left = new FormAttachment(50);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		rightComposite.setLayoutData(formData);
		formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = 0;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		rightComposite.setLayout(formLayout);

		graph2 = new Graph(rightComposite, SWT.NONE);
		graph2FormData = new FormData();
		graph2FormData.top = new FormAttachment(offsetLabel);
		graph2FormData.left = new FormAttachment(0);
		graph2FormData.right = new FormAttachment(100);
		graph2.setLayoutData(graph2FormData);
		graph2.setUnitsAreEqual(true);
		graph2.setVerticalAxisLabel(Messages.co2Etf_graph2VerticalLabel);
		graph2.setVerticalAxisShowLabel(true);
		graph2.setVerticalAxisShowValues(true);
		graph2.setHorizontalAxisLabel(Messages.co2Etf_graph2HorizontalLabel);
		graph2.setHorizontalAxisShowLabel(true);
		graph2.setHorizontalAxisShowValues(true);

		setGraphFormData();

		addListener(SWT.Resize, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				setGraphFormData();
				leftComposite.layout();
				rightComposite.layout();
			}
		});
    }

	@Override
	protected void handleDispose() {

	}

	private void setGraphFormData() {
		int usableWidth = (getSize().x / 2) - GuiConstants.FORM_LAYOUT_MARGIN;

		graph1FormData.bottom = new FormAttachment(0, usableWidth);
		graph2FormData.bottom = new FormAttachment(0, usableWidth);
	}

	@Override
	public void newCalculation(ScratchPad<ReplicatePad> scratchPad) {
		ReplicatePad replicatePad = scratchPad.getChildren().get(0);

		@SuppressWarnings("unchecked")
		HashSet<GraphPoint> standardGraphPoints = (HashSet<GraphPoint>) replicatePad.getVolatileData(Calculator.getVolatileDataStandardGraphPointsKey());
		@SuppressWarnings("unchecked")
		HashSet<AverageLine> averageLines = (HashSet<AverageLine>) replicatePad.getVolatileData(Calculator.getVolatileDataAverageLinesKey());
		@SuppressWarnings("unchecked")
		HashSet<GraphPoint> etfGraphPoints = (HashSet<GraphPoint>) replicatePad.getVolatileData(Calculator.getVolatileDataEtfGraphPointsKey());
		LinearRegression etfRegression = (LinearRegression) replicatePad.getVolatileData(Calculator.getVolatileDataEtfRegressionKey()); 

		graph1.removeAllDrawableObjects();

		for (GraphPoint graphPoint : standardGraphPoints) {
			int replicateId = graphPoint.getReplicateId();
			boolean disabled = graphPoint.getDisabled();
			String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			String timestamp = DateFormat.format(graphPoint.getDate(), timeZone, showTimeZone, false);

			Color color;
			String[] tooltip;
			boolean influencesAutoscale = true;

			if (graphPoint.getDisabled()) {
				color = ColorCache.getFadedColorFromPalette(getDisplay(), graphPoint.getColorId());
				tooltip = new String[] { graphPoint.getName() + " " + Messages.co2Etf_disabled, timestamp, Messages.co2Etf_d47 + String.valueOf(graphPoint.getX()), Messages.co2Etf_D47 + String.valueOf(graphPoint.getY()) };
				influencesAutoscale = false;
			} else {
				color = ColorCache.getColorFromPalette(getDisplay(), graphPoint.getColorId());
				tooltip = new String[] { graphPoint.getName(), timestamp, Messages.co2Etf_d47 + String.valueOf(graphPoint.getX()), Messages.co2Etf_D47 + String.valueOf(graphPoint.getY()) };
			}

			PointDesign pointDesign = new PointDesign(getDisplay(), color, PointStyle.values()[graphPoint.getShapeId()]);
			Point point = new Point(graphPoint.getX(), graphPoint.getY(), pointDesign);
			point.setTooltip(tooltip);
			point.setInfluencesAutoscale(influencesAutoscale);
			if (replicateId != -1) {
				point.addMenuItem(new MenuItemListener() {
					@Override
					public void handleEvent(Event event) {
						PartManager.showRawDataPerspective(getEasotopePart());
						PartManager.openStandardReplicate(getEasotopePart(), replicateId);
					}

					@Override
					public String getName() {
						return Messages.co2EtfPbl_openReplicate;
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
							return disabled ? Messages.co2EtfPbl_enableReplicate : Messages.co2EtfPbl_disableReplicate;
						}
					});
				}
			}

			graph1.addDrawableObjectFirst(point);
		}

		if (averageLines != null) {  
			for (AverageLine averageLine : averageLines) {
				double intercept = averageLine.getIntercept();
				Color color = ColorCache.getColorFromPalette(getDisplay(), averageLine.getColorId());
				graph1.addDrawableObjectFirst(new LineWithoutEnds(0, intercept, color, null));
				String[] tooltip = new String[] { Messages.co2Etf_intercept + String.valueOf(intercept) };
				Point point = new Point(0, intercept, null);
				point.setTooltip(tooltip);
				graph1.addDrawableObjectFirst(point);
			}
		}

		graph1.addDrawableObjectFirst(new Axes(ColorCache.getColor(getDisplay(), ColorCache.BLACK), SWT.LINE_SOLID, true));
		graph1.autoScale();

		graph2.removeAllDrawableObjects();

		GraphPoint samplePoint = null;

		for (GraphPoint etfPoint : etfGraphPoints) {
			int replicateId = etfPoint.getReplicateId();
			String name = etfPoint.getName();
			boolean disabled = etfPoint.getDisabled();
			PointDesign pointDesign = null;
			String[] tooltip = null;
			boolean influencesAutoscale = true;

			if (etfPoint.getName() == null) {
				samplePoint = etfPoint;
				pointDesign = new PointDesign(getDisplay(), ColorCache.getColor(getDisplay(), ColorCache.BLACK), PointStyle.X);
				tooltip = new String[] { Messages.co2Etf_sample, Messages.co2Etf_D47 + String.valueOf(etfPoint.getX()), Messages.co2Etf_D47CDES + String.valueOf(etfPoint.getY()) };

			} else {
				Color color;
				String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
				boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
				String timestamp = null;

				if (etfPoint.getDate() != -1) {
					timestamp = DateFormat.format(etfPoint.getDate(), timeZone, showTimeZone, false);
				}

				if (etfPoint.getDisabled()) {
					color = ColorCache.getFadedColorFromPalette(getDisplay(), etfPoint.getColorId());
					tooltip = new String[] { etfPoint.getName() + " " + Messages.co2Etf_disabled, timestamp, Messages.co2Etf_D47 + String.valueOf(etfPoint.getX()), Messages.co2Etf_D47CDES + String.valueOf(etfPoint.getY()) };
					influencesAutoscale = false;
				} else {
					color = ColorCache.getColorFromPalette(getDisplay(), etfPoint.getColorId());
					if (timestamp != null) {
						tooltip = new String[] { etfPoint.getName(), timestamp, Messages.co2Etf_D47 + String.valueOf(etfPoint.getX()), Messages.co2Etf_D47CDES + String.valueOf(etfPoint.getY()) };
					} else {
						tooltip = new String[] { etfPoint.getName(), Messages.co2Etf_D47 + String.valueOf(etfPoint.getX()), Messages.co2Etf_D47CDES + String.valueOf(etfPoint.getY()) };
					}
				}

				pointDesign = new PointDesign(getDisplay(), color, PointStyle.values()[etfPoint.getShapeId()]);
			}

			Point point = new Point(etfPoint.getX(), etfPoint.getY(), pointDesign);
			point.setTooltip(tooltip);
			point.setInfluencesAutoscale(influencesAutoscale);
			if (replicateId != -1 && name != null) {
				point.addMenuItem(new MenuItemListener() {
					@Override
					public void handleEvent(Event event) {
						PartManager.showRawDataPerspective(getEasotopePart());
						PartManager.openStandardReplicate(getEasotopePart(), replicateId);
					}

					@Override
					public String getName() {
						return Messages.co2EtfPbl_openReplicate;
					}
				});
				point.addMenuItem(new MenuItemListener() {
					@Override
					public void handleEvent(Event event) {
						InputCache.getInstance().replicateDisabledStatusUpdate(replicateId, !disabled);
					}
	
					@Override
					public String getName() {
						return disabled ? Messages.co2EtfPbl_enableReplicate : Messages.co2EtfPbl_disableReplicate;
					}
				});
			}

			graph2.addDrawableObjectFirst(point);
		}

		if (etfRegression != null) {
			graph2.addDrawableObjectFirst(new LineWithoutEnds(etfRegression.getSlope(), etfRegression.getIntercept(), ColorCache.getColor(getDisplay(), ColorCache.BLACK), null));

			Point point = new Point(0, etfRegression.getIntercept(), null);
			point.setTooltip(new String[] { Messages.co2Etf_intercept + String.valueOf(etfRegression.getIntercept()) });
			graph2.addDrawableObjectFirst(point);

			double xIntercept = (0 - etfRegression.getIntercept()) / etfRegression.getSlope();
			point = new Point(xIntercept, 0, null);
			point.setTooltip(new String[] { Messages.co2Etf_intercept + String.valueOf(xIntercept) });
			graph2.addDrawableObjectFirst(point);
		}

		graph2.addDrawableObjectFirst(new Axes(ColorCache.getColor(getDisplay(), ColorCache.BLACK), SWT.LINE_SOLID, true));
		graph2.autoScale();

		slope.setText(String.valueOf(etfRegression != null ? etfRegression.getSlope() : Double.NaN));
		intercept.setText(String.valueOf(etfRegression != null ? etfRegression.getIntercept() : Double.NaN));
		r2.setText(String.valueOf(etfRegression != null ? etfRegression.getR2() : Double.NaN));
		offset.setText(String.valueOf(samplePoint != null ? (samplePoint.getY() - samplePoint.getX()) : 0.0d));

		textComposite.layout();
	}
}
