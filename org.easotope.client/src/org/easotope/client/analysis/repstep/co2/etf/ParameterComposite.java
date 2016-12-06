/*
 * Copyright © 2016 by Devon Bowen.
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

package org.easotope.client.analysis.repstep.co2.etf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepParamComposite;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.VButton;
import org.easotope.client.core.widgets.VSelectList;
import org.easotope.client.core.widgets.VSelectListListener;
import org.easotope.client.core.widgets.VSpinner;
import org.easotope.client.core.widgets.VText;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standardlist.StandardCacheStandardListGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardList;
import org.easotope.shared.analysis.execute.RepStepCalculator.WindowType;
import org.easotope.shared.analysis.repstep.co2.etf.Calculator;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class ParameterComposite extends RepStepParamComposite implements StandardCacheStandardListGetListener {
	private VText acidTemperature;
	private Canvas acidTemperatureError;
	private SortedCombo windowType;
	private VSpinner minNumStandardsBeforeAfter;
	private VSelectList standards;
	private VButton averageStandardsFirst;

	public ParameterComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		final Image errorImage = Icons.getError(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label temperatureLabel = new Label(this, SWT.NONE);
		temperatureLabel.setText(Messages.etfParameterComposite_acidTempLabel);

		Composite temperatureComposite = new Composite(this, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		temperatureComposite.setLayout(gridLayout);

		acidTemperature = new VText(temperatureComposite, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		acidTemperature.setLayoutData(gridData);
		acidTemperature.setTextLimit(GuiConstants.SHORT_TEXT_INPUT_LIMIT);
		acidTemperature.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		acidTemperatureError = new Canvas(temperatureComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		acidTemperatureError.setLayoutData(gridData);
		acidTemperatureError.setVisible(false);
		acidTemperatureError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Label windowTypeLabel = new Label(this, SWT.NONE);
		windowTypeLabel.setText(Messages.etfParameterComposite_windowType);

		windowType = new SortedCombo(this, SWT.READ_ONLY);
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
		possibilities.put(WindowType.CorrInterval.ordinal(), Messages.etfParameterComposite_corrInterval);
		possibilities.put(WindowType.Window.ordinal(), Messages.etfParameterComposite_window);
		windowType.setPossibilities(possibilities);

		Label newStandardsBeforeAndAfterLabel = new Label(this, SWT.NONE);
		newStandardsBeforeAndAfterLabel.setText(Messages.etfParameterComposite_newStandardsBeforeAfter);

		minNumStandardsBeforeAfter = new VSpinner(this, SWT.BORDER);
		minNumStandardsBeforeAfter.setMinimum(0);
		minNumStandardsBeforeAfter.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label standardsLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		standardsLabel.setLayoutData(gridData);
		standardsLabel.setText(Messages.etfParameterComposite_standards);

		standards = new VSelectList(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		standards.setLayoutData(gridData);
		standards.setSelectionEnabled(false);
		standards.addListener(new VSelectListListener() {
			@Override
			public void checkBoxesChanged() {
				widgetStatusChanged();
			}

			@Override
			public void selectionChanged() {
				// ignore
			}
		});

		Label averageLabel = new Label(this, SWT.NONE);
		averageLabel.setText(Messages.etfParameterComposite_averageStandardsFirst);

		averageStandardsFirst = new VButton(this, SWT.CHECK);
		averageStandardsFirst.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
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

		Double acidTemp = null;

		try {
			acidTemp = Double.valueOf(acidTemperature.getText());
		} catch (NumberFormatException e) {
			acidTemp = null;
		}

		parameters.put(Calculator.PARAMETER_ACID_TEMPERATURE, acidTemp);
		parameters.put(Calculator.PARAMETER_WINDOW_TYPE, windowType.getSelectedInteger());
		parameters.put(Calculator.PARAMETER_MIN_NUM_STANDARDS_BEFORE_AFTER, minNumStandardsBeforeAfter.getSelection());

		List<Integer> selectedIds = standards.getCheckboxSelection();
		int[] ids = new int[selectedIds.size()];

		for (int i=0; i<ids.length; i++) {
			ids[i] = selectedIds.get(i);
		}

		parameters.put(Calculator.PARAMETER_STANDARD_IDS, ids);
		parameters.put(Calculator.PARAMETER_AVERAGE_STANDARDS_FIRST, averageStandardsFirst.getSelection());

		return parameters;
	}

	@Override
	protected void setCurrentFieldValues() {
		Double doubleNum = (Double) getParameter(Calculator.PARAMETER_ACID_TEMPERATURE);
		doubleNum = (doubleNum == null) ? Calculator.DEFAULT_ACID_TEMPERATURE : doubleNum;

		acidTemperature.setText(String.valueOf(doubleNum));

		Integer num = (Integer) getParameter(Calculator.PARAMETER_WINDOW_TYPE);
		num = (num == null) ? Calculator.DEFAULT_WINDOW_TYPE : num;

		windowType.selectInteger(num);

		num = (Integer) getParameter(Calculator.PARAMETER_MIN_NUM_STANDARDS_BEFORE_AFTER);
		num = (num == null) ? Calculator.DEFAULT_MIN_NUM_STANDARDS_BEFORE_AFTER : num;

		minNumStandardsBeforeAfter.setSelection(num);
		minNumStandardsBeforeAfter.setEnabled(windowType.getSelectedInteger() == WindowType.Window.ordinal());

		int[] ids = (int[]) getParameter(Calculator.PARAMETER_STANDARD_IDS);
		ids = (ids == null) ? Calculator.DEFAULT_STANDARD_IDS : ids;

		ArrayList<Integer> selectedIds = new ArrayList<Integer>();

		for (int id : ids) {
			selectedIds.add(id);
		}

		standards.setCheckboxSelection(selectedIds);

		Boolean booleanValue = (Boolean) getParameter(Calculator.PARAMETER_AVERAGE_STANDARDS_FIRST);
		booleanValue = (booleanValue == null) ? Calculator.DEFAULT_AVERAGE_STANDARDS_FIRST : booleanValue;

		averageStandardsFirst.setSelection(booleanValue);
	}

	@Override
	public void enableWidgets() {
		boolean hasAdminPermissions = LoginInfoCache.getInstance().getPermissions().isCanEditCorrIntervals();

		acidTemperature.setEnabled(hasAdminPermissions);
		windowType.setEnabled(hasAdminPermissions);
		minNumStandardsBeforeAfter.setEnabled(hasAdminPermissions && windowType.getSelectedInteger() == WindowType.Window.ordinal());
		standards.setEnabled(hasAdminPermissions);

		if (!hasAdminPermissions) {
			acidTemperature.revert();
			minNumStandardsBeforeAfter.revert();
			standards.revert();
		}

		averageStandardsFirst.setEnabled(hasAdminPermissions);
	}

	@Override
	public void disableWidgets() {
		acidTemperature.setEnabled(false);
		windowType.setEnabled(false);
		minNumStandardsBeforeAfter.setEnabled(false);
		standards.setEnabled(false);
		averageStandardsFirst.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		try {
			double newTemperature = Double.valueOf(acidTemperature.getText());
			double oldTemperature = Double.valueOf(acidTemperature.getOldText());
			isDirty = isDirty || newTemperature != oldTemperature;

		} catch (NumberFormatException e) {
			isDirty = isDirty || acidTemperature.hasChangedIfTrimmed();
		}

		isDirty = isDirty || windowType.hasChanged();
		isDirty = isDirty || minNumStandardsBeforeAfter.hasChanged();
		isDirty = isDirty || standards.hasChanged();
		isDirty = isDirty || averageStandardsFirst.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		boolean newTemperatureParseError = true;

		try {
			Double.valueOf(acidTemperature.getText());
			newTemperatureParseError = false;
		} catch (NumberFormatException e) {
			// ignore
		}

		boolean tempertureErrorSet = false;

		if (newTemperatureParseError) {
			acidTemperatureError.setToolTipText(Messages.etfParameterComposite_temperatureMustBeValidNumber);
			tempertureErrorSet = true;
		}

		if (acidTemperature.getText().trim().isEmpty()) {
			acidTemperatureError.setToolTipText(Messages.etfParameterComposite_temperatureRequired);
			tempertureErrorSet = true;
		}

		if (tempertureErrorSet != acidTemperatureError.getVisible()) {
			acidTemperatureError.setVisible(tempertureErrorSet);
			layoutNeeded();
		}

		return tempertureErrorSet;
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
