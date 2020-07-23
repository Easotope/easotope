/*
 * Copyright © 2019-2020 by Cédric John.
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

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepParamComposite;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.VSpinner;
import org.carbonateresearch.shared.analysis.repstep.co2.ethpbl.Calculator.PBLCorrectionType;

import java.util.HashMap;

import org.carbonateresearch.shared.analysis.repstep.co2.ethpbl.Calculator;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ParameterComposite extends RepStepParamComposite {
	private SortedCombo correctionType;
	private VSpinner minNumScansBeforeAfter;

	public ParameterComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label correctionTypeLabel = new Label(this, SWT.NONE);
		correctionTypeLabel.setText(Messages.ETHPBLComposite_CorrectionType);

		correctionType = new SortedCombo(this, SWT.READ_ONLY);
		correctionType.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {				
				if (correctionType.getSelectedInteger() == PBLCorrectionType.MovingAverage.ordinal()) {
					minNumScansBeforeAfter.setEnabled(true);
				} else {
					minNumScansBeforeAfter.setEnabled(false);
					minNumScansBeforeAfter.revert();
				}

				widgetStatusChanged();
			}
		});

		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();
		possibilities.put(PBLCorrectionType.NearestScan.ordinal(), Messages.ETHPBLCorrectionType_NearestScan);
		possibilities.put(PBLCorrectionType.MovingAverage.ordinal(), Messages.ETHPBLCorrectionType_AveragedScans);
		possibilities.put(PBLCorrectionType.Interpolate.ordinal(), Messages.ETHPBLCorrectionType_Interpolate);
		correctionType.setPossibilities(possibilities);

		Label minNumStandardsBeforeAndAfterLabel = new Label(this, SWT.NONE);
		minNumStandardsBeforeAndAfterLabel.setText(Messages.ETHPBLComposite_NbScansWindow);

		minNumScansBeforeAfter = new VSpinner(this, SWT.BORDER);
		minNumScansBeforeAfter.setMinimum(1);
		minNumScansBeforeAfter.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});
    }

	@Override
	protected HashMap<String,Object> buildNewParameters() {
		HashMap<String,Object> parameters = new HashMap<String,Object>();

		parameters.put(Calculator.PARAMETER_CORRECTION_TYPE, correctionType.getSelectedInteger());
		parameters.put(Calculator.PARAMETER_MIN_NUM_SCANS_BEFORE_AFTER, minNumScansBeforeAfter.getSelection());

		return parameters;
	}

	@Override
	protected void setCurrentFieldValues() {
		Integer num = (Integer) getParameter(Calculator.PARAMETER_CORRECTION_TYPE);
		num = (num == null) ? Calculator.DEFAULT_CORRECTION_TYPE : num;

		correctionType.selectInteger(num);

		num = (Integer) getParameter(Calculator.PARAMETER_MIN_NUM_SCANS_BEFORE_AFTER);
		num = (num == null) ? Calculator.DEFAULT_MIN_NUM_SCANS_BEFORE_AFTER : num;

		minNumScansBeforeAfter.setSelection(num);
		minNumScansBeforeAfter.setEnabled(correctionType.getSelectedInteger() == PBLCorrectionType.MovingAverage.ordinal());
	}

	@Override
	public void enableWidgets() {
		boolean canEditCorrIntervals = LoginInfoCache.getInstance().getPermissions().isCanEditCorrIntervals();

		correctionType.setEnabled(canEditCorrIntervals);
		minNumScansBeforeAfter.setEnabled(canEditCorrIntervals && correctionType.getSelectedInteger() == PBLCorrectionType.MovingAverage.ordinal());

		if (!canEditCorrIntervals) {
			minNumScansBeforeAfter.revert();
		}
	}

	@Override
	public void disableWidgets() {
		correctionType.setEnabled(false);
		minNumScansBeforeAfter.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || correctionType.hasChanged();
		isDirty = isDirty || minNumScansBeforeAfter.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		return false;
	}
}
