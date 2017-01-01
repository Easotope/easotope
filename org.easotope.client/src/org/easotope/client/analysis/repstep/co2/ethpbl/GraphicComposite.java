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

package org.easotope.client.analysis.repstep.co2.ethpbl;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepGraphicComposite;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.DateTimeLabel;
import org.easotope.shared.analysis.repstep.co2.ethpbl.Calculator;
import org.easotope.shared.core.DoubleFormat;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.rawdata.InputParameter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class GraphicComposite extends RepStepGraphicComposite {
	private DateTimeLabel dateTimeLabel;
	private Label timeBetweenLabel;
	private Table table;

	public GraphicComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);

		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Label label1 = new Label(this, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		label1.setLayoutData(formData);
		label1.setText(Messages.co2EthMonitorComposite_scanFileDate);

		dateTimeLabel = new DateTimeLabel(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(label1);
		dateTimeLabel.setLayoutData(formData);

		Label label2 = new Label(this, SWT.WRAP);
		formData = new FormData();
		formData.top = new FormAttachment(label1);
		formData.left = new FormAttachment(0);
		label2.setLayoutData(formData);
		label2.setText(Messages.co2EthMonitorComposite_scanFileTimeBetween);

		timeBetweenLabel = new Label(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label1);
		formData.left = new FormAttachment(label2);
		timeBetweenLabel.setLayoutData(formData);

		table = new Table(this, SWT.BORDER);
		formData = new FormData();
		formData.top = new FormAttachment(label2, 5);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(0, 600);
		formData.bottom = new FormAttachment(label2, 300);
		table.setLayoutData(formData);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setWidth(50);
		column.setText(Messages.co2EthMonitorComposite_mass);

		column = new TableColumn(table, SWT.NONE);
		column.setWidth(180);
		column.setText(Messages.co2EthMonitorComposite_X2Coeff);

		column = new TableColumn(table, SWT.NONE);
		column.setWidth(180);
		column.setText(Messages.co2EthMonitorComposite_X1Coeff);

		column = new TableColumn(table, SWT.NONE);
		column.setWidth(180);
		column.setText(Messages.co2EthMonitorComposite_X0Coeff);

		Label label3 = new Label(this, SWT.WRAP);
		formData = new FormData();
		formData.top = new FormAttachment(table, 5);
		formData.left = new FormAttachment(0);
		label3.setLayoutData(formData);
		label3.setText(Messages.co2EthMonitorComposite_nanWarning);
	}

	@Override
	protected void handleDispose() {

	}

	@Override
	public void newCalculation(ScratchPad<ReplicatePad> scratchPad) {
		ReplicatePad replicatePad = scratchPad.getChildren().get(0);
		ReplicatePad scanFileReplicatePad = (ReplicatePad) replicatePad.getVolatileData(Calculator.getVolatileDataScanReplicatePadKey());

		if (scanFileReplicatePad == null) {
			return;
		}

		dateTimeLabel.setDate(scanFileReplicatePad.getDate());

		long timeBetween = Math.abs(replicatePad.getDate() - scanFileReplicatePad.getDate()) / 1000;
		String timeBetweenString = "";

		final long secondsInDay = 60 * 60 * 24;
		long days = timeBetween / secondsInDay;
		timeBetweenString += (days < 10) ? "0" + days : days;
		timeBetween %= secondsInDay;

		timeBetweenString += ":";

		final long secondsInHour = 60 * 60;
		long hours = timeBetween / secondsInHour;
		timeBetweenString += (hours < 10) ? "0" + hours : hours;
		timeBetween %= secondsInHour;

		timeBetweenString += ":";

		final long secondsInMinute = 60;
		long minutes = timeBetween / secondsInMinute;
		timeBetweenString += (minutes < 10) ? "0" + minutes : minutes;
		timeBetween %= secondsInMinute;

		timeBetweenString += ":";

		timeBetweenString += (timeBetween < 10) ? "0" + timeBetween : timeBetween;

		timeBetweenLabel.setText(timeBetweenString + " " + Messages.co2EthMonitorComposite_timeBetweenFormat);
		layout();

		table.removeAll();

		// TODO This is really wrong!!!!! It should dereference the INPUT_LABELs

		String[] labels = { "44", "45", "46", "47", "48", "49" };
		InputParameter[] x2Coeff = { InputParameter.V44_Scan_X2Coeff, InputParameter.V45_Scan_X2Coeff, InputParameter.V46_Scan_X2Coeff, InputParameter.V47_Scan_X2Coeff, InputParameter.V48_Scan_X2Coeff, InputParameter.V49_Scan_X2Coeff };
		InputParameter[] x1Coeff = { InputParameter.V44_Scan_Slope, InputParameter.V45_Scan_Slope, InputParameter.V46_Scan_Slope, InputParameter.V47_Scan_Slope, InputParameter.V48_Scan_Slope, InputParameter.V49_Scan_Slope };
		InputParameter[] x0Coeff = { InputParameter.V44_Scan_Intercept, InputParameter.V45_Scan_Intercept, InputParameter.V46_Scan_Intercept, InputParameter.V47_Scan_Intercept, InputParameter.V48_Scan_Intercept, InputParameter.V49_Scan_Intercept };

		for (int i=0; i<labels.length; i++) {		
			TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(0, labels[i]);
			Double value = (Double) scanFileReplicatePad.getValue(x2Coeff[i].toString());

			if (value != null) {
				tableItem.setText(1, DoubleFormat.formatWithoutExp(value));
			}

			value = (Double) scanFileReplicatePad.getValue(x1Coeff[i].toString());

			if (value != null) {
				tableItem.setText(2, DoubleFormat.formatWithoutExp(value));
			}

			value = (Double) scanFileReplicatePad.getValue(x0Coeff[i].toString());
	
			if (value != null) {
				tableItem.setText(3, DoubleFormat.formatWithoutExp(value));
			}
		}
	}
}
