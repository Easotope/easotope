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

package org.easotope.client.analysis.repstep.co2.bulkrefcalc;

import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepGraphicComposite;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.graph.DrawableObject;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.drawables.ParabolaWithXRange;
import org.easotope.client.core.widgets.graph.drawables.Point;
import org.easotope.shared.analysis.repstep.co2.bulkrefcalc.Calculator;
import org.easotope.shared.analysis.repstep.co2.bulkrefcalc.Calculator.Lidi2Point;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.math.Polynomial;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2.DataFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class GraphicComposite extends RepStepGraphicComposite {
	private SortedCombo selectAcquisition;
	Label selectMzLabel;
	private SortedCombo selectMz;
	private Composite visibilityComposite;
	private Button ref1Points;
	private Button ref1Curve;
	private Button ref2Points;
	private Button ref2Curve;
	private Button interpPoints;
	private Button interpCurve;
	Composite graphComposite;
	StackLayout graphCompositeLayout;
	Composite dualInputComposite;
	private Graph graph;

	private ArrayList<GraphData> graphDataByAcquisitionNum;

	public GraphicComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
		
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Composite selectAcqComposite = new Composite(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		selectAcqComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.marginWidth = 0;
		selectAcqComposite.setLayout(gridLayout);

		Label selectAcqLabel = new Label(selectAcqComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		selectAcqLabel.setLayoutData(gridData);
		selectAcqLabel.setText(Messages.bulkRefCalcGraphicComposite_acquisitionSelectLabel);

		selectAcquisition = new SortedCombo(selectAcqComposite, SWT.READ_ONLY);
		selectAcquisition.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				updateGraph(true);
			}
		});
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		selectAcquisition.setLayoutData(gridData);

		selectMzLabel = new Label(selectAcqComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		selectMzLabel.setLayoutData(gridData);
		selectMzLabel.setText(Messages.bulkRefCalcGraphicComposite_mzSelectLabel);

		selectMz = new SortedCombo(selectAcqComposite, SWT.READ_ONLY);
		selectMz.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				updateGraph(true);
			}
		});
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		selectMz.setLayoutData(gridData);

		visibilityComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		visibilityComposite.setLayoutData(formData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 9;
		gridLayout.marginWidth = 0;
		visibilityComposite.setLayout(gridLayout);

		Label ref1Label = new Label(visibilityComposite, SWT.WRAP);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		ref1Label.setLayoutData(gridData);
		ref1Label.setText(Messages.bulkRefCalcGraphicComposite_ref1Label);

		ref1Points = new Button(visibilityComposite, SWT.CHECK);
		ref1Points.setSelection(true);
		ref1Points.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				updateGraph(false);
			}
		});
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		ref1Points.setLayoutData(gridData);

		ref1Curve = new Button(visibilityComposite, SWT.CHECK);
		ref1Curve.setSelection(true);
		ref1Curve.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				updateGraph(false);
			}
		});
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		ref1Curve.setLayoutData(gridData);

		Label ref2Label = new Label(visibilityComposite, SWT.WRAP);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		ref2Label.setLayoutData(gridData);
		ref2Label.setText(Messages.bulkRefCalcGraphicComposite_ref2Label);

		ref2Points = new Button(visibilityComposite, SWT.CHECK);
		ref2Points.setSelection(true);
		ref2Points.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				updateGraph(false);
			}
		});
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		ref2Points.setLayoutData(gridData);

		ref2Curve = new Button(visibilityComposite, SWT.CHECK);
		ref2Curve.setSelection(true);
		ref2Curve.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				updateGraph(false);
			}
		});
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		ref2Curve.setLayoutData(gridData);

		Label interpLabel = new Label(visibilityComposite, SWT.WRAP);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		interpLabel.setLayoutData(gridData);
		interpLabel.setText(Messages.bulkRefCalcGraphicComposite_interpolatedLabel);

		interpPoints = new Button(visibilityComposite, SWT.CHECK);
		interpPoints.setSelection(true);
		interpPoints.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				updateGraph(false);
			}
		});
		formData = new FormData();
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		interpPoints.setLayoutData(gridData);

		interpCurve = new Button(visibilityComposite, SWT.CHECK);
		interpCurve.setSelection(true);
		interpCurve.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				updateGraph(false);
			}
		});
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		interpCurve.setLayoutData(gridData);
		
		graphComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(selectAcqComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		graphComposite.setLayoutData(formData);
		graphCompositeLayout = new StackLayout();
		graphComposite.setLayout(graphCompositeLayout);

		dualInputComposite = new Composite(graphComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(selectAcqComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		dualInputComposite.setLayoutData(formData);
		dualInputComposite.setLayout(new GridLayout());
		
		Label label = new Label(dualInputComposite, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = SWT.CENTER;
		label.setLayoutData(gridData);
		label.setText(Messages.bulkRefCalcGraphicComposite_dualInletLabel);

		graph = new Graph(graphComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(selectAcqComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		graph.setLayoutData(formData);
		graph.setUnitsAreEqual(false);
		graph.setVerticalAxisLabel("vertical");
		graph.setVerticalAxisShowLabel(true);
		graph.setVerticalAxisShowValues(true);
		graph.setHorizontalAxisLabel("horizontal");
		graph.setHorizontalAxisShowLabel(true);
		graph.setHorizontalAxisShowValues(true);
		
		graphCompositeLayout.topControl = graph;
    }

	@Override
	protected void handleDispose() {

	}

	public void setLidi2Mode(boolean yes) {
		selectMzLabel.setVisible(yes);
		selectMz.setVisible(yes);
		visibilityComposite.setVisible(yes);
		graphCompositeLayout.topControl = yes ? graph : dualInputComposite;
		graphComposite.layout();
	}
	
	public void updateGraph(boolean autoScale) {
		int selectedAcquisition = selectAcquisition.getSelectedInteger();

		if (selectedAcquisition == -1) {
			setLidi2Mode(false);
			return;
		}

		GraphData graphData = graphDataByAcquisitionNum.get(selectedAcquisition);

		if (graphData == null) {
			setLidi2Mode(false);
			return;
		}

		setLidi2Mode(true);

		String selectedMz = selectMz.getSelectedString();

		ArrayList<String> mzList = graphData.getMzList();
		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();
		for (int i=0; i<mzList.size(); i++) {
			possibilities.put(i, mzList.get(i));
		}

		selectMz.setPossibilities(possibilities);

		if (mzList.contains(selectedMz)) {
			selectMz.selectString(selectedMz);
		} else {
			selectMz.selectInteger(0);
		}

		selectedMz = selectMz.getSelectedString();
		String basisMz = graphData.getBasisMz();
		ArrayList<Lidi2Point> refVals1 = graphData.getRefVals1().get(selectedMz);
		Polynomial polynomial1 = graphData.getPolynomial1().get(selectedMz);
		ArrayList<Lidi2Point> refVals2 = graphData.getRefVals2().get(selectedMz);
		Polynomial polynomial2 = graphData.getPolynomial2().get(selectedMz);
		Polynomial polynomialInterp = graphData.getPolynomialInterp().get(selectedMz);
		ArrayList<Lidi2Point> valsInterp = graphData.getLidi2RefValsInterp().get(selectedMz);

		// THROUGHOUT THIS FUNCTION VERIFY IT WILL WORK EVEN IF NO DATA WAS RECEIVED

		graph.removeAllDrawableObjects();
		graph.setVerticalAxisLabel(String.format(Messages.bulkRefCalcGraphicComposite_verticalGraphLabel, selectedMz, basisMz));
		graph.setHorizontalAxisLabel(String.format(Messages.bulkRefCalcGraphicComposite_horizontalGraphLabel, basisMz));

		double minX = Double.MAX_VALUE;
		double maxX = -Double.MIN_VALUE;

		for (Lidi2Point point : refVals1) {
			minX = Math.min(point.getX(), minX);
			maxX = Math.max(point.getX(), maxX);
		}

		for (Lidi2Point point : refVals2) {
			minX = Math.min(point.getX(), minX);
			maxX = Math.max(point.getX(), maxX);
		}
		
		double fivePercent = (maxX - minX) * 0.05;
		minX -= fivePercent;
		maxX += fivePercent;

		double[] coefficients = polynomial1.getCoefficients();
		Color block1Color = ColorCache.getColorFromPalette(getDisplay(), 0);
		PointDesign block1PointDesign = new PointDesign(getDisplay(), block1Color, PointStyle.FilledCircle);
		
		Color block1ColorDisabled = ColorCache.getFadedColorFromPalette(getDisplay(), 0);
		PointDesign block1PointDesignDisabled = new PointDesign(getDisplay(), block1ColorDisabled, PointStyle.FilledCircle);

		if (ref1Curve.getSelection()) {
			graph.addDrawableObjectFirst(new ParabolaWithXRange(coefficients, minX, maxX, block1Color, null));
		}

		if (ref1Points.getSelection()) {
			for (Lidi2Point point : refVals1) {
				DrawableObject drawable = new Point(point.getX(), point.getY(), point.getDisabled() ? block1PointDesignDisabled : block1PointDesign);
				drawable.setTooltip(new String[] { "Block 1 Measured Reference" + (point.getDisabled() ? " (disabled)" : ""), basisMz + " = " + point.getX(), selectedMz + "/" + basisMz + " = " + point.getY() });
				graph.addDrawableObjectFirst(drawable);
			}
		}

		coefficients = polynomial2.getCoefficients();
		Color block2Color = ColorCache.getColorFromPalette(getDisplay(), 1);
		PointDesign block2PointDesign = new PointDesign(getDisplay(), block2Color, PointStyle.FilledCircle);
		
		Color block2ColorDisabled = ColorCache.getFadedColorFromPalette(getDisplay(), 1);
		PointDesign block2PointDesignDisabled = new PointDesign(getDisplay(), block2ColorDisabled, PointStyle.FilledCircle);

		if (ref2Curve.getSelection()) {
			graph.addDrawableObjectFirst(new ParabolaWithXRange(coefficients, minX, maxX, block2Color, null));
		}

		if (ref2Points.getSelection()) {
			for (Lidi2Point point : refVals2) {
				DrawableObject drawable = new Point(point.getX(), point.getY(), point.getDisabled() ? block2PointDesignDisabled : block2PointDesign);
				drawable.setTooltip(new String[] { "Block 2 Measured Reference" + (point.getDisabled() ? " (disabled)" : ""), basisMz + " = " + point.getX(), selectedMz + "/" + basisMz + " = " + point.getY() });
				graph.addDrawableObjectFirst(drawable);
			}
		}

		coefficients = polynomialInterp.getCoefficients();
		Color interpolatedColor = ColorCache.getColorFromPalette(getDisplay(), 2);
		PointDesign interpolatedPointDesign = new PointDesign(getDisplay(), interpolatedColor, PointStyle.FilledDiamond);

		Color interpolatedColorDisabled = ColorCache.getFadedColorFromPalette(getDisplay(), 2);
		PointDesign interpolatedPointDesignDisabled = new PointDesign(getDisplay(), interpolatedColorDisabled, PointStyle.FilledDiamond);

		if (interpCurve.getSelection()) {
			graph.addDrawableObjectFirst(new ParabolaWithXRange(coefficients, minX, maxX, interpolatedColor, null));
		}

		if (interpPoints.getSelection()) {
			for (Lidi2Point point : valsInterp) {
				DrawableObject drawable = new Point(point.getX(), point.getY(), point.getDisabled() ? interpolatedPointDesignDisabled : interpolatedPointDesign);
				drawable.setTooltip(new String[] { "Interpolated reference" + (point.getDisabled() ? " (disabled)" : ""), basisMz + " = " + point.getX(), selectedMz + "/" + basisMz + " = " + point.getY() });
				graph.addDrawableObjectFirst(drawable);
			}
		}

		if (autoScale) {
			graph.autoScale();
		}

		layout();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void newCalculation(ScratchPad<ReplicatePad> scratchPad) {
		ReplicatePad replicatePad = scratchPad.getChildren().get(0);

		int previousSelection = (selectAcquisition.getSelectedInteger() == -1) ? 0 : selectAcquisition.getSelectedInteger();
		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();

		graphDataByAcquisitionNum = new ArrayList<GraphData>();

		for (int i=0; i<replicatePad.getChildren().size(); i++) {
			possibilities.put(i, Integer.toString(i+1));

			AcquisitionPad acquisitionPad = replicatePad.getChildren().get(i);

			if (acquisitionPad.getDataFormat() == DataFormat.LIDI2) {
				GraphData graphData = new GraphData();

				graphData.setBasisMz((String) acquisitionPad.getVolatileData(Calculator.getVolatileDataLidi2BasisMz()));
				
				graphData.setRefVals1((HashMap<String,ArrayList<Lidi2Point>>) acquisitionPad.getVolatileData(Calculator.getVolatileDataLidi2RefVals1()));
				graphData.setPolynomial1((HashMap<String,Polynomial>) acquisitionPad.getVolatileData(Calculator.getVolatileDataLidi2RefPolynomial1()));
				graphData.setRefVals2((HashMap<String,ArrayList<Lidi2Point>>) acquisitionPad.getVolatileData(Calculator.getVolatileDataLidi2RefVals2()));
				graphData.setPolynomial2((HashMap<String,Polynomial>) acquisitionPad.getVolatileData(Calculator.getVolatileDataLidi2RefPolynomial2()));
				graphData.setLidi2RefValsInterp((HashMap<String,ArrayList<Lidi2Point>>) acquisitionPad.getVolatileData(Calculator.getVolatileDataLidi2RefValsInterp()));
				graphData.setPolynomialInterp((HashMap<String,Polynomial>) acquisitionPad.getVolatileData(Calculator.getVolatileDataLidi2RefPolynomialInterp()));

				graphDataByAcquisitionNum.add(graphData);

			} else {
				graphDataByAcquisitionNum.add(null);
			}
		}

		selectAcquisition.setPossibilities(possibilities);
		selectAcquisition.selectInteger(selectAcquisition.getItems().length <= previousSelection ? 0 : previousSelection);
		selectAcquisition.setEnabled(possibilities.size() > 1);

		updateGraph(true);
	}

	public class GraphData {
		private String basisMz;
		private HashMap<String,ArrayList<Lidi2Point>> refVals1;
		private HashMap<String,Polynomial> polynomial1;
		private HashMap<String,ArrayList<Lidi2Point>> refVals2;
		private HashMap<String,Polynomial> polynomial2;
		private HashMap<String,ArrayList<Lidi2Point>> lidi2RefValsInterp;
		private HashMap<String,Polynomial> polynomialInterp;

		public void setBasisMz(String basisMz) {
			this.basisMz = basisMz;
		}
		
		public String getBasisMz() {
			return basisMz;
		}
		
		public ArrayList<String> getMzList() {
			return new ArrayList<String>(refVals1.keySet());
		}

		public HashMap<String, ArrayList<Lidi2Point>> getRefVals1() {
			return refVals1;
		}
		
		public void setRefVals1(HashMap<String, ArrayList<Lidi2Point>> refVals1) {
			this.refVals1 = refVals1;
		}
		
		public HashMap<String, Polynomial> getPolynomial1() {
			return polynomial1;
		}
		
		public void setPolynomial1(HashMap<String, Polynomial> polynomial1) {
			this.polynomial1 = polynomial1;
		}
		
		public HashMap<String, ArrayList<Lidi2Point>> getRefVals2() {
			return refVals2;
		}
		
		public void setRefVals2(HashMap<String, ArrayList<Lidi2Point>> refVals2) {
			this.refVals2 = refVals2;
		}
		
		public HashMap<String, Polynomial> getPolynomial2() {
			return polynomial2;
		}
		
		public void setPolynomial2(HashMap<String, Polynomial> polynomial2) {
			this.polynomial2 = polynomial2;
		}
		
		public HashMap<String, ArrayList<Lidi2Point>> getLidi2RefValsInterp() {
			return lidi2RefValsInterp;
		}
		
		public void setLidi2RefValsInterp(HashMap<String, ArrayList<Lidi2Point>> lidi2RefValsInterp) {
			this.lidi2RefValsInterp = lidi2RefValsInterp;
		}
		
		public HashMap<String, Polynomial> getPolynomialInterp() {
			return polynomialInterp;
		}
		
		public void setPolynomialInterp(HashMap<String, Polynomial> polynomialInterp) {
			this.polynomialInterp = polynomialInterp;
		}
	}
}

