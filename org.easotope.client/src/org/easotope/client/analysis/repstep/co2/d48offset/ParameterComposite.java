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

package org.easotope.client.analysis.repstep.co2.d48offset;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepParamComposite;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.VGroupingList;
import org.easotope.client.core.widgets.VGroupingListListener;
import org.easotope.client.core.widgets.VSpinner;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standardlist.StandardCacheStandardListGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardList;
import org.easotope.shared.analysis.execute.RepStepCalculator.WindowType;
import org.easotope.shared.analysis.repstep.co2.d48offset.Calculator;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ParameterComposite extends RepStepParamComposite implements StandardCacheStandardListGetListener {
	private SortedCombo windowType;
	private VSpinner minNumStandardsBeforeAfter;
	private VGroupingList standards;

	public ParameterComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label windowTypeLabel = new Label(this, SWT.NONE);
		windowTypeLabel.setText(Messages.co2D48OffsetParameterComposite_windowType);

		windowType = new SortedCombo(this, SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		windowType.setLayoutData(gridData);
		windowType.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {				
				if (windowType.getSelectedInteger() == WindowType.Window.ordinal()) {
					minNumStandardsBeforeAfter.setEnabled(true);
				} else {
					minNumStandardsBeforeAfter.setEnabled(false);
					minNumStandardsBeforeAfter.revert();
				}

				widgetStatusChanged();
			}
		});

		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();
		possibilities.put(WindowType.CorrInterval.ordinal(), Messages.co2D48OffsetParameterComposite_corrInterval);
		possibilities.put(WindowType.Window.ordinal(), Messages.co2D48OffsetParameterComposite_window);
		windowType.setPossibilities(possibilities);

		Label minNumStandardsBeforeAndAfterLabel = new Label(this, SWT.NONE);
		minNumStandardsBeforeAndAfterLabel.setText(Messages.co2D48OffsetParameterComposite_minNumStandardsBeforeAndAfter);

		minNumStandardsBeforeAfter = new VSpinner(this, SWT.BORDER);
		minNumStandardsBeforeAfter.setMinimum(0);
		minNumStandardsBeforeAfter.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label standardsLabel = new Label(this, SWT.NONE);
		standardsLabel.setText(Messages.co2D48OffsetParameterComposite_standard);

		standards = new VGroupingList(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		standards.setLayoutData(gridData);
		standards.addListener(new VGroupingListListener() {
			@Override
			public void spinnersChanged() {
				widgetStatusChanged();
			}
		});

		StandardCache.getInstance().addListener(this);
		StandardCache.getInstance().standardListGet(this);
    }

	@Override
	protected void handleDispose() {
		super.handleDispose();
		StandardCache.getInstance().removeListener(this);
	}

	@Override
	protected HashMap<String,Object> buildNewParameters() {
		HashMap<String,Object> parameters = new HashMap<String,Object>();

		parameters.put(Calculator.PARAMETER_WINDOW_TYPE, windowType.getSelectedInteger());
		parameters.put(Calculator.PARAMETER_MIN_NUM_STANDARDS_BEFORE_AFTER, minNumStandardsBeforeAfter.getSelection());
		parameters.put(Calculator.PARAMETER_STANDARD_IDS, standards.getGroups());

		return parameters;
	}

	@Override
	protected void setCurrentFieldValues() {
		Integer num = (Integer) getParameter(Calculator.PARAMETER_WINDOW_TYPE);
		num = (num == null) ? Calculator.DEFAULT_WINDOW_TYPE : num;

		windowType.selectInteger(num);

		num = (Integer) getParameter(Calculator.PARAMETER_MIN_NUM_STANDARDS_BEFORE_AFTER);
		num = (num == null) ? Calculator.DEFAULT_MIN_NUM_STANDARDS_BEFORE_AFTER : num;

		minNumStandardsBeforeAfter.setSelection(num);
		minNumStandardsBeforeAfter.setEnabled(windowType.getSelectedInteger() == WindowType.Window.ordinal());

		HashMap<Integer,Integer> groups = null;
		Object obj = getParameter(Calculator.PARAMETER_STANDARD_IDS);

		if (obj == null) {
			groups = new HashMap<Integer,Integer>();

		} else if (obj instanceof int[]) {
			int[] ids = (int[]) obj;
			groups = new HashMap<Integer,Integer>();

			for (int id : ids) {
				groups.put(id, 1);
			}

		} else {
			@SuppressWarnings("unchecked")
			HashMap<Integer,Integer> tmp = (HashMap<Integer,Integer>) obj;
			groups = tmp;
		}

		standards.setGroups(groups);
	}

	@Override
	public void enableWidgets() {
		boolean hasAdminPermissions = LoginInfoCache.getInstance().getPermissions().isCanEditCorrIntervals();

		windowType.setEnabled(hasAdminPermissions);
		minNumStandardsBeforeAfter.setEnabled(hasAdminPermissions && windowType.getSelectedInteger() == WindowType.Window.ordinal());
		standards.setEnabled(hasAdminPermissions);

		if (!hasAdminPermissions) {
			minNumStandardsBeforeAfter.revert();
			standards.revert();
		}
	}

	@Override
	public void disableWidgets() {
		windowType.setEnabled(false);
		minNumStandardsBeforeAfter.setEnabled(false);
		standards.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || windowType.hasChanged();
		isDirty = isDirty || minNumStandardsBeforeAfter.hasChanged();
		isDirty = isDirty || standards.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		return false;
	}

	private void setPossibilities(StandardList standardList) {
		HashMap<String,Integer> possibilities = new HashMap<String,Integer>();

		for (Integer id : standardList.keySet()) {
			possibilities.put(standardList.get(id).getName(), id);
		}

		standards.setPossibilities(possibilities);
	}

	@Override
	public void standardListGetCompleted(int commandId, StandardList standardList) {
		setPossibilities(standardList);
	}

	@Override
	public void standardListUpdated(int commandId, StandardList standardList) {
		setPossibilities(standardList);
	}

	@Override
	public void standardListGetError(int commandId, String message) {
		getChainedPart().raiseError(message);
	}
}
