/*
 * Copyright © 2016-2020 by Devon Bowen.
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

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingControlAdaptor;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.shared.analysis.step.InputDescription;
import org.easotope.shared.analysis.step.OutputDescription;
import org.easotope.shared.analysis.step.StepController;
import org.easotope.shared.analysis.tables.RepStep;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class TabIOComposite extends EasotopeComposite {
	private Table inputsTable;
	private Table outputsTable;

	TabIOComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
		FormLayout formLayout = new FormLayout();
 		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
 		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
 		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
 		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
 		setLayout(formLayout);

 		Label label = new Label(this, SWT.NONE);
 		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		label.setLayoutData(formData);
 		label.setText(Messages.repTabIoComposite_inputsLabel);
 
 		final Composite inputsTableComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(50, -3);
		formData.bottom = new FormAttachment(100);
		inputsTableComposite.setLayoutData(formData);

 		inputsTable = new Table(inputsTableComposite, SWT.BORDER);
		final TableColumn tableInputColumn1 = new TableColumn(inputsTable, SWT.NONE);
		tableInputColumn1.setText(Messages.repTabIoComposite_description);
		final TableColumn tableInputColumn2 = new TableColumn(inputsTable, SWT.NONE);
		tableInputColumn2.setText(Messages.repTabIoComposite_column);
		inputsTable.setHeaderVisible(true);
		inputsTable.setLinesVisible(true);
		inputsTable.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				inputsTable.deselectAll();
			}
		});

		inputsTableComposite.addControlListener(new LoggingControlAdaptor() {
			public void loggingControlResized(ControlEvent e) {
				Rectangle area = inputsTableComposite.getClientArea();
				Point preferredSize = inputsTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				int width = area.width - 2 * inputsTable.getBorderWidth() - 2; // final -2 is a unexplained fudge factor
				// TODO adjusting for the vertical bar doesn't work - at least on the mac
				if (preferredSize.y > area.height + inputsTable.getHeaderHeight()) {
					Point vBarSize = inputsTable.getVerticalBar().getSize();
					width -= vBarSize.x;
				}
				Point oldSize = inputsTable.getSize();
				if (oldSize.x > area.width) {
					tableInputColumn1.setWidth(width / 2);
					tableInputColumn2.setWidth(width - tableInputColumn1.getWidth());
					inputsTable.setSize(area.width, area.height);
				} else {
					inputsTable.setSize(area.width, area.height);
					tableInputColumn1.setWidth(width / 2);
					tableInputColumn2.setWidth(width - tableInputColumn1.getWidth());
				}
			}
		});

 		label = new Label(this, SWT.NONE);
 		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(50, 3);
		label.setLayoutData(formData);
 		label.setText(Messages.repTabIoComposite_outputsLabel);

 		final Composite outputsTableComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(50, 3);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		outputsTableComposite.setLayoutData(formData);

 		outputsTable = new Table(outputsTableComposite, SWT.BORDER);
		final TableColumn tableOutputColumn1 = new TableColumn(outputsTable, SWT.NONE);
		tableOutputColumn1.setText(Messages.repTabIoComposite_description);
		final TableColumn tableOutputColumn2 = new TableColumn(outputsTable, SWT.NONE);
		tableOutputColumn2.setText(Messages.repTabIoComposite_column);
		outputsTable.setHeaderVisible(true);
		outputsTable.setLinesVisible(true);
		outputsTable.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				outputsTable.deselectAll();
			}
		});

		outputsTableComposite.addControlListener(new LoggingControlAdaptor() {
			public void loggingControlResized(ControlEvent e) {
				Rectangle area = outputsTableComposite.getClientArea();
				Point preferredSize = outputsTable.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				int width = area.width - 2 * outputsTable.getBorderWidth() - 2; // final -2 is a unexplained fudge factor
				// TODO adjusting for the vertical bar doesn't work - at least on the mac
				if (preferredSize.y > area.height + outputsTable.getHeaderHeight()) {
					Point vBarSize = outputsTable.getVerticalBar().getSize();
					width -= vBarSize.x;
				}
				Point oldSize = outputsTable.getSize();
				if (oldSize.x > area.width) {
					tableOutputColumn1.setWidth(width/2);
					tableOutputColumn2.setWidth(width - tableOutputColumn1.getWidth());
					outputsTable.setSize(area.width, area.height);
				} else {
					outputsTable.setSize(area.width, area.height);
					tableOutputColumn1.setWidth(width/2);
					tableOutputColumn2.setWidth(width - tableOutputColumn1.getWidth());
				}
			}
		});
	}

	@Override
	protected void handleDispose() {
		// TODO Auto-generated method stub
		
	}

	void setInputs(RepStep repStep, StepController controller) {
		inputsTable.removeAll();
		HashMap<String,String> inputMap = repStep.getInputs();

		for (InputDescription inputDescription : controller.getInputDescription()) {
			String key = inputDescription.getLabel();
			TableItem tableItem = new TableItem(inputsTable, SWT.NONE);
			tableItem.setText(0, key);
			tableItem.setText(1, inputMap.get(key));
		}

		outputsTable.removeAll();
		HashMap<String,String> outputMap = repStep.getOutputs();

		for (OutputDescription outputDescription : controller.getOutputDescription()) {
			String key = outputDescription.getLabel();
			TableItem tableItem = new TableItem(outputsTable, SWT.NONE);
			tableItem.setText(0, key);
			tableItem.setText(1, outputMap.get(key));
		}
	}
}
