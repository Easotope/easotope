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

package org.easotope.client.analysis.repstep.co2.iclpbl;

import java.text.MessageFormat;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepGraphicComposite;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.drawables.LineWithEnds;
import org.easotope.client.core.widgets.graph.drawables.Point;
import org.easotope.shared.analysis.repstep.co2.iclpbl.Calculator;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.math.LinearRegression;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class GraphicComposite extends RepStepGraphicComposite {
	private TabFolder tabFolder;
	private String[] tabLabels = new String[] { "44", "45", "46", "47", "48", "49" };
	private Graph[] graph = new Graph[tabLabels.length];
	private Table[] table = new Table[tabLabels.length];

	public GraphicComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);

		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Label label = new Label(this, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		label.setLayoutData(formData);
		label.setText(Messages.co2IclPblGraphicComposite_instructions);

		tabFolder = new TabFolder(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		tabFolder.setLayoutData(formData);

		for (int i=0; i<tabLabels.length; i++) {
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
	 		Composite itemComposite = new Composite(tabFolder, SWT.NONE);
			formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
			formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
			formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
			formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
			itemComposite.setLayout(formLayout);
	 		tabItem.setControl(itemComposite);
			tabItem.setText(tabLabels[i]);

			table[i] = new Table(itemComposite, SWT.BORDER);
			formData = new FormData();
			formData.top = new FormAttachment(label);
			formData.left = new FormAttachment(100, -250);
			formData.right = new FormAttachment(100);
			formData.bottom = new FormAttachment(100);
			table[i].setLayoutData(formData);
			table[i].setLinesVisible(true);
			table[i].setHeaderVisible(true);
	
			TableColumn column = new TableColumn(table[i], SWT.NONE);
			column.setWidth(40);
			column.setText(Messages.co2IclPblGraphicComposite_acquisition);
	
			column = new TableColumn(table[i], SWT.NONE);
			column.setWidth(70);
			column.setText(Messages.co2IclPblGraphicComposite_slope);

			column = new TableColumn(table[i], SWT.NONE);
			column.setWidth(70);
			column.setText(Messages.co2IclPblGraphicComposite_intercept);

			column = new TableColumn(table[i], SWT.NONE);
			column.setWidth(70);
			column.setText(Messages.co2IclPblGraphicComposite_r2);

			graph[i] = new Graph(itemComposite, SWT.NONE);
			formData = new FormData();
			formData.top = new FormAttachment(label);
			formData.left = new FormAttachment(0);
			formData.right = new FormAttachment(table[i], -5, SWT.LEFT);
			formData.bottom = new FormAttachment(100);
			graph[i].setLayoutData(formData);
			graph[i].setBackground(ColorCache.getColor(getDisplay(), ColorCache.WHITE));
			graph[i].setVerticalAxisLabel(MessageFormat.format(Messages.co2IclPblGraphicComposite_yAxis, tabLabels[i]));
			graph[i].setVerticalAxisShowLabel(true);
			graph[i].setVerticalAxisShowValues(true);
			graph[i].setHorizontalAxisLabel(Messages.co2IclPblGraphicComposite_xAxis);
			graph[i].setHorizontalAxisShowLabel(true);
			graph[i].setHorizontalAxisShowValues(true);
		}
	}

	@Override
	protected void handleDispose() {

	}

	@Override
	public void newCalculation(ScratchPad<ReplicatePad> scratchPad) {
		for (int i=0; i<tabLabels.length; i++) {
			for (TableItem tableItem : table[i].getItems()) {
				if (!tableItem.getImage().isDisposed()) {
					tableItem.getImage().dispose();
				}
				tableItem.dispose();
			}

			graph[i].removeAllDrawableObjects();
			graph[i].setRedraw(false);
		}

		Image emptyImage = getEmptyImage();

		int acquisitionNumber=0;
		for (AcquisitionPad acquisitionPad : scratchPad.getChild(0).getChildren()) {
			LinearRegression[] linearRegressions = (LinearRegression[]) acquisitionPad.getVolatileData(Calculator.getVolatileDataLinearRegressionKey());
			
			PointDesign pointDesign = PointDesign.getByIndex(getDisplay(), acquisitionNumber);
			Image imageForAcquisition = imageForAcquisition(pointDesign);

			for (int i=0; i<tabLabels.length; i++) {
				TableItem tableItem = new TableItem(table[i], SWT.NONE);

				if (linearRegressions != null && !linearRegressions[i].isInvalid()) {
					tableItem.setText(0, String.valueOf(acquisitionNumber+1));
					tableItem.setImage(imageForAcquisition);

					tableItem.setText(1, String.format(GuiConstants.DOUBLE_FORMAT, linearRegressions[i].getSlope()));
					tableItem.setText(2, String.format(GuiConstants.DOUBLE_FORMAT, linearRegressions[i].getIntercept()));
					tableItem.setText(3, String.format(GuiConstants.DOUBLE_FORMAT, linearRegressions[i].getR2()));

					double minX = Double.POSITIVE_INFINITY;
					double maxX = Double.NEGATIVE_INFINITY;

					for (LinearRegression.Point regressionPoint : linearRegressions[i].getPoints()) {
						double x = regressionPoint.getX();

						if (x > maxX) {
							maxX = x;
						} else if (x < minX) {
							minX = x;
						}

						String[] tooltip = new String[] {
								String.valueOf(Messages.co2IclPblGraphicComposite_acquisition + ": " + (acquisitionNumber+1)),
								String.valueOf(Messages.co2IclPblGraphicComposite_xAxis + ": " + x),
								String.valueOf(Messages.co2IclPblGraphicComposite_yAxis + ": " + regressionPoint.getY())
						};

						Point point = new Point(x, regressionPoint.getY(), pointDesign);
						point.setTooltip(tooltip);
						graph[i].addDrawableObjectLast(point);
					}

					double yForMinX = linearRegressions[i].getSlope() * minX + linearRegressions[i].getIntercept();
					double yForMaxX = linearRegressions[i].getSlope() * maxX + linearRegressions[i].getIntercept();

					graph[i].addDrawableObjectFirst(new LineWithEnds(minX, yForMinX, maxX, yForMaxX, pointDesign.getColor(), null));

				} else {
					tableItem.setText(0, String.valueOf(acquisitionNumber+1));
					tableItem.setImage(emptyImage);
				}
			}

			acquisitionNumber++;
		}

		for (int i=0; i<tabLabels.length; i++) {
			graph[i].autoScale();
			graph[i].setRedraw(true);
		}
	}

	private Image imageForAcquisition(PointDesign pointDesign) {
		Image image = getEmptyImage();

		GC gc = new GC(image);
		pointDesign.draw(gc, 3, 3, false);
		gc.dispose();

		return image;
	}

	private Image getEmptyImage() {
	    Image src = new Image(getParent().getDisplay(), 9, 7);        
	    ImageData imageData = src.getImageData();
	    imageData.transparentPixel = imageData.getPixel(0, 0);
	    src.dispose();
	    Image image = new Image(getParent().getDisplay(), imageData);

		return image;
	}
}
