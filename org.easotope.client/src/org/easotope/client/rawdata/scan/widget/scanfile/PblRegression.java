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

package org.easotope.client.rawdata.scan.widget.scanfile;

import java.text.MessageFormat;
import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.drawables.LineWithEnds;
import org.easotope.client.core.widgets.graph.drawables.ParabolaWithXRange;
import org.easotope.client.core.widgets.graph.drawables.Point;
import org.easotope.shared.math.LinearRegression;
import org.easotope.shared.math.PolynomialFitter;
import org.easotope.shared.math.PolynomialRegression;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class PblRegression extends Composite {
	private Graph graph;
	private Composite equationComposite;
	private Label equation;
	private Label r2;

	private double x2Coeff = Double.NaN;
	private double x1Coeff = Double.NaN;
	private double x0Coeff = Double.NaN;

	private int degree = -1;
	private Integer referenceChannel = null;
	private HashMap<Integer,double[]> allReferenceChannelOnPeakValues;
	private double[] intensityAverages;

	public PblRegression(Composite parent, int style) {
		super(parent, style);

		FormLayout formLayout = new FormLayout();
		setLayout(formLayout);

		equationComposite = new Composite(this, SWT.NONE);
		FormData formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		equationComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 0;
		gridLayout.makeColumnsEqualWidth = true;
		equationComposite.setLayout(gridLayout);

		equation = new Label(equationComposite, SWT.NONE);

		r2 = new Label(equationComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		gridData.grabExcessHorizontalSpace = true;
		r2.setLayoutData(gridData);

		graph = new Graph(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(equationComposite);
		graph.setLayoutData(formData);
		graph.setVerticalAxisShowLabel(true);
		graph.setVerticalAxisShowValues(true);
		graph.setHorizontalAxisShowValues(true);
		graph.setHorizontalAxisShowLabel(true);
	}

	public void setGraphLabel(String label) {
		graph.setVerticalAxisLabel(MessageFormat.format(Messages.pblRegression_verticalLabel, label));
	}

	public void setDegreeOfFit(int degreeOfFit) {
		this.degree = degreeOfFit;
		renderGraph();
	}

	public void setReferenceChannel(Integer referenceChannel, String referenceChannelLabel) {
		this.referenceChannel = referenceChannel;
		graph.setHorizontalAxisLabel(MessageFormat.format(Messages.pblRegression_horizontalLabel, referenceChannelLabel));
		renderGraph();
	}

	public void setAllReferenceChannelOnPeakValues(HashMap<Integer,double[]> allReferenceChannelOnPeakValues) {
		this.allReferenceChannelOnPeakValues = allReferenceChannelOnPeakValues;
		renderGraph();
	}

	public void setIntensityAverages(double[] intensityAverages) {
		this.intensityAverages = intensityAverages;
		renderGraph();
	}

	public boolean isValid() {
		return graph.getDrawableObjects().size() != 0;
	}

	public double getX2Coeff() {
		return x2Coeff;
	}

	public double getX1Coeff() {
		return x1Coeff;
	}
	
	public double getX0Coeff() {
		return x0Coeff;
	}

	private void renderGraph() {
		x2Coeff = Double.NaN;
		x1Coeff = Double.NaN;
		x0Coeff = Double.NaN;

		graph.removeAllDrawableObjects();
		equation.setText("");
		r2.setText("");

		if (referenceChannel == null) {
			equationComposite.layout();
			return;
		}

		double[] referenceChannelOnPeakValues = allReferenceChannelOnPeakValues.get(referenceChannel); 

		if (degree == -1 || referenceChannelOnPeakValues == null || intensityAverages == null || referenceChannelOnPeakValues.length != intensityAverages.length || referenceChannelOnPeakValues.length < 2) {
			equationComposite.layout();
			return;
		}

		PolynomialFitter fitter = (degree == 1) ? new LinearRegression() : new PolynomialRegression(degree);

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;

		for (int i=0; i<referenceChannelOnPeakValues.length; i++) {
			if (Double.isNaN(intensityAverages[i])) {
				continue;
			}

			if (referenceChannelOnPeakValues[i] > maxX) {
				maxX = referenceChannelOnPeakValues[i];
			}

			if (referenceChannelOnPeakValues[i] < minX) {
				minX = referenceChannelOnPeakValues[i];
			}

			PointDesign pointDesign = new PointDesign(getDisplay(), ColorCache.getColorFromPalette(getDisplay(), i), PointStyle.FilledDiamond);
			Point point = new Point(referenceChannelOnPeakValues[i], intensityAverages[i], pointDesign);
			point.setTooltip(new String[] { String.format(GuiConstants.DOUBLE_FORMAT, referenceChannelOnPeakValues[i]), String.format(GuiConstants.DOUBLE_FORMAT, intensityAverages[i]) });
			graph.addDrawableObjectLast(point);

			fitter.addCoordinate(referenceChannelOnPeakValues[i], intensityAverages[i]);
		}

		if (fitter.isInvalid()) {
			graph.removeAllDrawableObjects();
			equationComposite.layout();
			return;
		}

		double[] coefficients = fitter.getCoefficients();

		x2Coeff = (degree == 1) ? 0.0d : coefficients[2];
		x1Coeff = coefficients[1];
		x0Coeff = coefficients[0];

		if (degree == 1) {
			double minY = x1Coeff * minX + x0Coeff;
			double maxY = x1Coeff * maxX + x0Coeff;

			LineWithEnds lineWithEnds = new LineWithEnds(minX, minY, maxX, maxY, ColorCache.getColor(getDisplay(), ColorCache.BLACK), null);
			graph.addDrawableObjectFirst(lineWithEnds);

		} else {
			ParabolaWithXRange parabolaWithXRange = new ParabolaWithXRange(x2Coeff, x1Coeff, x0Coeff, minX, maxX, ColorCache.getColor(getDisplay(), ColorCache.BLACK), null);
			graph.addDrawableObjectFirst(parabolaWithXRange);
		}

		graph.autoScale();

		String equationString = Messages.pblRegression_yLabel;

		if (degree == 2) {
			equationString += String.format(GuiConstants.DOUBLE_FORMAT, x2Coeff) + Messages.pblRegression_x2Label;
		}
			
		equationString += String.format(GuiConstants.DOUBLE_FORMAT, x1Coeff) + Messages.pblRegression_x1Label + String.format(GuiConstants.DOUBLE_FORMAT, x0Coeff);
		equation.setText(equationString);

		String r2String = (degree == 2) ? "" : Messages.pblRegression_r2Label + String.format(GuiConstants.DOUBLE_FORMAT, ((LinearRegression) fitter).getR2());
		r2.setText(r2String);

		equationComposite.layout();
	}
}
