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

package org.easotope.client.analysis.repstep.co2.nonlinearity;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepGraphicComposite;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.drawables.Axes;
import org.easotope.client.core.widgets.graph.drawables.LineWithEnds;
import org.easotope.client.core.widgets.graph.drawables.Point;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.repstep.co2.nonlinearity.Calculator;
import org.easotope.shared.analysis.repstep.co2.nonlinearity.Calculator.NonlinearityPoint;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.math.MultiLineCommonSlopeRegression;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class GraphicComposite extends RepStepGraphicComposite {
	private Label slope;
	private Label correction;
	private Graph graph;

	public GraphicComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Label label2 = new Label(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		label2.setLayoutData(formData);
		label2.setText(Messages.nonlinearityGraphicComposite_slope);

		slope = new Label(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(label2);
		slope.setLayoutData(formData);

		Label label = new Label(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label2);
		formData.left = new FormAttachment(0);
		label.setLayoutData(formData);
		label.setText(Messages.nonlinearityGraphicComposite_correction);

		correction = new Label(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label2);
		formData.left = new FormAttachment(label);
		correction.setLayoutData(formData);

		graph = new Graph(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		graph.setLayoutData(formData);
		graph.setUnitsAreEqual(true);
		graph.setVerticalAxisLabel(Messages.nonlinearityGraphicComposite_verticalLabel);
		graph.setVerticalAxisShowLabel(true);
		graph.setVerticalAxisShowValues(true);
		graph.setHorizontalAxisLabel(Messages.nonlinearityGraphicComposite_horizontalLabel);
		graph.setHorizontalAxisShowLabel(true);
		graph.setHorizontalAxisShowValues(true);
    }

	@Override
	protected void handleDispose() {

	}

	@Override
	public void newCalculation(ScratchPad<ReplicatePad> scratchPad) {
		ReplicatePad replicatePad = scratchPad.getChildren().get(0);

		MultiLineCommonSlopeRegression regression = (MultiLineCommonSlopeRegression) replicatePad.getVolatileData(Calculator.getVolatileDataLinearRegressionKey());

		if (regression == null || regression.getInvalid()) {
			slope.setText(String.valueOf(Double.NaN));
		} else {
			slope.setText(String.valueOf(regression.getSlope()));
		}

		Double addedCorrection = (Double) replicatePad.getVolatileData(Calculator.getVolatileDataNonlinearityCorrectionKey());

		if (addedCorrection == null) {
			addedCorrection = 0.0d;
		}

		correction.setText(String.valueOf(addedCorrection));

		@SuppressWarnings("unchecked")
		HashSet<NonlinearityPoint> nonlinearityPoints = (HashSet<NonlinearityPoint>) replicatePad.getVolatileData(Calculator.getVolatileDataNonlinearityPointsKey());

		graph.removeAllDrawableObjects();

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;

		for (NonlinearityPoint nonlinearityPoint : nonlinearityPoints) {
			Standard standard = nonlinearityPoint.getStandard();
			double δ47 = nonlinearityPoint.getδ47();
			double Δ47 = nonlinearityPoint.getΔ47();
			long date = nonlinearityPoint.getDate();

			if (!nonlinearityPoint.getDisabled()) {
				minX = Math.min(minX, δ47);
				maxX = Math.max(maxX, δ47);
			}

			String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			String timestamp = DateFormat.format(date, timeZone, showTimeZone, false);
			String[] tooltip;
			PointDesign pointDesign = null;

			if (standard != null) {
				Color color;
				boolean influencesAutoscale = true;

				if (nonlinearityPoint.getDisabled()) {
					color = ColorCache.getFadedColorFromPalette(getDisplay(), standard.getColorId());
					tooltip = new String[] { standard.getName() + " " + Messages.nonlinearityGraphicComposite_disabled, timestamp, Messages.nonlinearityGraphicComposite_d47 + String.valueOf(δ47), Messages.nonlinearityGraphicComposite_D47 + String.valueOf(Δ47) };
					influencesAutoscale = false;				
				} else {
					color = ColorCache.getColorFromPalette(getDisplay(), standard.getColorId());
					tooltip = new String[] { standard.getName(), timestamp, Messages.nonlinearityGraphicComposite_d47 + String.valueOf(δ47), Messages.nonlinearityGraphicComposite_D47 + String.valueOf(Δ47) };
				}

				pointDesign = new PointDesign(getDisplay(), color, PointStyle.values()[standard.getShapeId()]);
				Point point = new Point(δ47, Δ47, pointDesign);
				point.setTooltip(tooltip);
				point.setInfluencesAutoscale(influencesAutoscale);
				graph.addDrawableObjectLast(point);

			} else {
				tooltip = new String[] { timestamp, Messages.nonlinearityGraphicComposite_d47 + String.valueOf(δ47), Messages.nonlinearityGraphicComposite_D47_measured + String.valueOf(Δ47-addedCorrection), Messages.nonlinearityGraphicComposite_D47_corrected + String.valueOf(Δ47) };
				pointDesign = new PointDesign(getDisplay(), ColorCache.getColor(getDisplay(), ColorCache.BLACK), PointStyle.X);
				graph.addDrawableObjectFirst(new LineWithEnds(0, Δ47, δ47, Δ47-addedCorrection, ColorCache.getColor(getDisplay(), ColorCache.BLACK), SWT.LINE_DOT, null));

				Point point = new Point(0, Δ47, pointDesign);
				point.setTooltip(tooltip);
				graph.addDrawableObjectLast(point);

				point = new Point(δ47, Δ47-addedCorrection, null);
				graph.addDrawableObjectLast(point);
			}
		}

		if (regression != null && !regression.getInvalid()) {
			HashMap<Integer,Double> intercepts = regression.getIntercepts();

			for (int group : intercepts.keySet()) {
				double intercept = intercepts.get(group);
				double minY = regression.getSlope() * minX + intercept;
				double maxY = regression.getSlope() * maxX + intercept;
				graph.addDrawableObjectFirst(new LineWithEnds(minX, minY, maxX, maxY, ColorCache.getColor(getDisplay(), ColorCache.BLACK), null));
				Point point = new Point(0, intercept, null);
				String groupTooltip = Messages.nonlinearityGraphicComposite_group + group;
				String interceptTooltip = Messages.nonlinearityGraphicComposite_intercept + String.valueOf(intercept);
				point.setTooltip(new String[] { groupTooltip, interceptTooltip });
				graph.addDrawableObjectFirst(point);
			}
		}

		graph.addDrawableObjectFirst(new Axes(ColorCache.getColor(getDisplay(), ColorCache.BLACK), SWT.LINE_SOLID, true));
		graph.autoScale();

		layout();
	}
}
