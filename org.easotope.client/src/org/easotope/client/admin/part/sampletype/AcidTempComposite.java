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

package org.easotope.client.admin.part.sampletype;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.NumericValueList;
import org.easotope.client.core.widgets.NumericValueListListener;
import org.easotope.client.core.widgets.VText;
import org.easotope.shared.admin.AcidTempParameter;
import org.easotope.shared.admin.cache.sampletype.SampleTypeCache;
import org.easotope.shared.admin.cache.sampletype.acidtemp.SampleTypeCacheAcidTempGetListener;
import org.easotope.shared.admin.cache.sampletype.acidtemp.SampleTypeCacheAcidTempSaveListener;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.AcidTempList;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.AcidTempListItem;
import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class AcidTempComposite extends EditorComposite implements SampleTypeCacheAcidTempGetListener, SampleTypeCacheAcidTempSaveListener {
	private static final String ACID_TEMP_GET = "ACID_TEMP_GET";
	private static final String ACID_TEMP_SAVE = "ACID_TEMP_SAVE";

	private Label id;
	private VText temperature;
	private Canvas temperatureError;
	private VText description;
	private NumericValueList values;

	private AcidTempList lastAcidTempList = null;
	private HashSet<Double> acidTempValues = new HashSet<Double>();
	private double mostRecentlySetTemperature = Double.NaN;

	protected AcidTempComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		final Image errorImage = Icons.getError(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.acidTempComposite_idLabel);

		id = new Label(this, SWT.NONE);

		Label temperatureLabel = new Label(this, SWT.NONE);
		temperatureLabel.setText(Messages.acidTempComposite_temperatureLabel);

		Composite temperatureComposite = new Composite(this, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		temperatureComposite.setLayout(gridLayout);

		temperature = new VText(temperatureComposite, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		temperature.setLayoutData(gridData);
		temperature.setTextLimit(GuiConstants.SHORT_TEXT_INPUT_LIMIT);
		temperature.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		temperatureError = new Canvas(temperatureComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		temperatureError.setLayoutData(gridData);
		temperatureError.setVisible(false);
		temperatureError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Label descriptionLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		descriptionLabel.setLayoutData(gridData);
		descriptionLabel.setText(Messages.acidTempComposite_descriptionLabel);

		description = new VText(this, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		description.setLayoutData(gridData);
		description.setTextLimit(GuiConstants.LONG_TEXT_INPUT_LIMIT);
		description.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		Label valuesLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		valuesLabel.setLayoutData(gridData);
		valuesLabel.setText(Messages.acidTempComposite_valuesLabel);

		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();

		for (AcidTempParameter acidTempParameter : AcidTempParameter.values()) {
			if (acidTempParameter != AcidTempParameter.NONE) {
				possibilities.put(acidTempParameter.ordinal(), acidTempParameter.name());
			}
		}

		values = new NumericValueList(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		values.setLayoutData(gridData);
		values.setValueName(Messages.acidTempComposite_valueName);
		values.setLabelPossibilities(possibilities);
		values.setWithReferences(true);
		values.setValues(null);
		values.addSelectionListener(new NumericValueListListener() {
			@Override
			public void widgetSelected() {
				widgetStatusChanged();
			}
		});

		SampleTypeCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		SampleTypeCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private AcidTemp getCurrentAcidTemp() {
		return (AcidTemp) getCurrentObject();
	}

	@Override
	protected void setCurrentFieldValues() {
		AcidTemp currentAcidTemp = getCurrentAcidTemp();

		id.setText(String.valueOf(currentAcidTemp.getId()));
		temperature.setText(String.valueOf(currentAcidTemp.getTemperature()));
		description.setText(currentAcidTemp.getDescription());
		values.setValues(currentAcidTemp.getValues());
		
		mostRecentlySetTemperature = currentAcidTemp.getTemperature();
	}

	@Override
	protected void setDefaultFieldValues() {
		id.setText(Messages.acidTempComposite_newAcidTempId);
		temperature.setText("");
		description.setText("");
		values.setValues(null);

		mostRecentlySetTemperature = Double.NaN;
	}

	@Override
	public void enableWidgets() {
		boolean canEditSampleTypes = LoginInfoCache.getInstance().getPermissions().isCanEditSampleTypes();

		temperature.setEnabled(canEditSampleTypes);
		description.setEnabled(canEditSampleTypes);
		values.setEnabled(canEditSampleTypes);

		if (!canEditSampleTypes) {
			temperature.revert();
			description.revert();
			values.revert();
		}
	}

	@Override
	public void disableWidgets() {
		temperature.setEnabled(false);
		description.setEnabled(false);
		values.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		try {
			AcidTemp currentAcidTemp = getCurrentAcidTemp();
			double newTemperature = Double.valueOf(temperature.getText());
			double oldTemperature = currentAcidTemp == null ? Double.NaN : getCurrentAcidTemp().getTemperature();
			isDirty = isDirty || currentAcidTemp == null || newTemperature != oldTemperature;

		} catch (NumberFormatException e) {
			isDirty = isDirty || temperature.hasChangedIfTrimmed();
		}

		isDirty = isDirty || description.hasChanged();
		isDirty = isDirty || values.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		double newTemperature = Double.NaN;
		boolean newTemperatureParseError = true;

		try {
			newTemperature = Double.valueOf(temperature.getText());
			newTemperatureParseError = false;
		} catch (NumberFormatException e) {
			// ignore
		}

		boolean tempertureErrorSet = false;

		if (newTemperatureParseError) {
			temperatureError.setToolTipText(Messages.acidTempComposite_temperatureMustBeValidNumber);
			tempertureErrorSet = true;

		} else if (newTemperature != mostRecentlySetTemperature && acidTempValues.contains(newTemperature)) {
			temperatureError.setToolTipText(Messages.acidTempComposite_temperatureNotUnique);
			tempertureErrorSet = true;
		}

		if (temperature.getText().trim().isEmpty()) {
			temperatureError.setToolTipText(Messages.acidTempComposite_temperatureRequired);
			tempertureErrorSet = true;
		}

		if (tempertureErrorSet != temperatureError.getVisible()) {
			temperatureError.setVisible(tempertureErrorSet);
			layoutNeeded();
		}
		
		return tempertureErrorSet || values.hasError();
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		AcidTempList currentAcidTempList = (AcidTempList) selection.get(SampleTypePart.SELECTION_ACID_TEMP_LIST);

		if (currentAcidTempList != lastAcidTempList) {
			lastAcidTempList = currentAcidTempList;
			acidTempValues.clear();

			if (currentAcidTempList != null) {
				for (Integer id : currentAcidTempList.keySet()) {
					AcidTempListItem acidTempListItem = currentAcidTempList.get(id);
					acidTempValues.add(acidTempListItem.getTemperature());
				}
			}
		}

		AcidTemp acidTemp = getCurrentAcidTemp();
		Integer selectedAcidTempId = (Integer) selection.get(SampleTypePart.SELECTION_ACID_TEMP_ID);

		if (acidTemp == null && selectedAcidTempId == null) {
			return false;
		}

		if ((acidTemp == null && selectedAcidTempId != null) || (acidTemp != null && selectedAcidTempId == null)) {
			return true;
		}

		return acidTemp.getId() != selectedAcidTempId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer acidTempId = (Integer) selection.get(SampleTypePart.SELECTION_ACID_TEMP_ID);

		if (acidTempId != null) {
			int commandId = SampleTypeCache.getInstance().acidTempGet(acidTempId, this);
			waitingFor(ACID_TEMP_GET, commandId);
			return true;
		}

		return false;
	}

	@Override
	protected void requestSave(boolean isResend) {
		AcidTemp oldAcidTemp = getCurrentAcidTemp();
		AcidTemp newAcidTemp = new AcidTemp();

		if (oldAcidTemp != null) {
			newAcidTemp.id = oldAcidTemp.getId();
		}

		newAcidTemp.setTemperature(Double.valueOf(temperature.getText()));
		newAcidTemp.setSampleTypeId(lastAcidTempList.getSampleTypeId());
		newAcidTemp.setDescription(description.getText());
		newAcidTemp.setValues(values.getValues());

		int commandId = SampleTypeCache.getInstance().acidTempSave(newAcidTemp, this);
		waitingFor(ACID_TEMP_SAVE, commandId);
	}

	@Override
	protected boolean canDelete() {
		return false;
	}

	@Override
	protected boolean requestDelete() {
		return false;
	}

	@Override
	public void acidTempGetCompleted(int commandId, AcidTemp acidTemp) {
		newObject(ACID_TEMP_GET, acidTemp);
	}

	@Override
	public void acidTempUpdated(int commandId, AcidTemp acidTemp) {
		updateObject(acidTemp, Messages.acidTempComposite_acidTempHasBeenUpdated);
	}

	@Override
	public void acidTempGetError(int commandId, String message) {
		raiseGetError(ACID_TEMP_GET, message);		
	}

	@Override
	public void acidTempSaveCompleted(int commandId, AcidTemp acidTemp) {
		saveComplete(ACID_TEMP_SAVE, acidTemp);
	}

	@Override
	public void acidTempSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(ACID_TEMP_SAVE, message);
	}
}
