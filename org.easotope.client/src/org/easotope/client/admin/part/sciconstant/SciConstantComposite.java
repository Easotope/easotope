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

package org.easotope.client.admin.part.sciconstant;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.VDouble;
import org.easotope.client.core.widgets.VText;
import org.easotope.shared.admin.cache.sciconstant.SciConstantCache;
import org.easotope.shared.admin.cache.sciconstant.sciconstant.SciConstantCacheSciConstantGetListener;
import org.easotope.shared.admin.cache.sciconstant.sciconstant.SciConstantCacheSciConstantSaveListener;
import org.easotope.shared.admin.tables.SciConstant;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class SciConstantComposite extends EditorComposite implements SciConstantCacheSciConstantGetListener, SciConstantCacheSciConstantSaveListener {
	private static final String SCI_CONSTANT_GET = "SCI_CONSTANT_GET";
	private static final String SCI_CONSTANT_SAVE = "SCI_CONSTANT_SAVE";

	private Label id;
	private Label name;
	private Label description;
	private Label defaultValue;
	private Label defaultReference;
	private VDouble value;
	private Canvas valueError;
	private Button resetButton;
	private VText reference;
	
	protected SciConstantComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		final Image errorImage = Icons.getError(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.sciConstantComposite_sciConstantIdLabel);

		id = new Label(this, SWT.NONE);

		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText(Messages.sciConstantComposite_sciConstantNameLabel);

		name = new Label(this, SWT.NONE);

		Label descriptionLabel = new Label(this, SWT.NONE);
		descriptionLabel.setText(Messages.sciConstantComposite_sciConstantDescriptionLabel);

		description = new Label(this, SWT.NONE);

		Label defaultValueLabel = new Label(this, SWT.NONE);
		defaultValueLabel.setText(Messages.sciConstantComposite_sciConstantDefaultValueLabel);

		defaultValue = new Label(this, SWT.NONE);

		Label defaultReferenceLabel = new Label(this, SWT.NONE);
		defaultReferenceLabel.setText(Messages.sciConstantComposite_sciConstantDefaultReferenceLabel);

		defaultReference = new Label(this, SWT.WRAP);

		Label valueLabel = new Label(this, SWT.NONE);
		valueLabel.setText(Messages.sciConstantComposite_valueLabel);

		Composite valueComposite = new Composite(this, SWT.NONE);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		valueComposite.setLayout(gridLayout);

		value = new VDouble(valueComposite, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		value.setLayoutData(gridData);
		value.setTextLimit(GuiConstants.SHORT_TEXT_INPUT_LIMIT);
		value.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		valueError = new Canvas(valueComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		valueError.setLayoutData(gridData);
		valueError.setVisible(false);
		valueError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Label referenceLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		referenceLabel.setLayoutData(gridData);
		referenceLabel.setText(Messages.sciConstantComposite_referenceLabel);

		reference = new VText(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		reference.setLayoutData(gridData);
		reference.setTextLimit(GuiConstants.LONG_TEXT_INPUT_LIMIT);
		reference.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		Label resetLabel = new Label(this, SWT.NONE);
		resetLabel.setText("");

		resetButton = new Button(this, SWT.PUSH);
		resetButton.setText(Messages.sciConstantComposite_resetButton);
		resetButton.setEnabled(false);
		resetButton.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				SciConstant sciConstant = getCurrentSciConstant();
				value.setNumberButLeaveRevertValue(sciConstant.getEnumeration().getDefaultValue());
				reference.setTextButLeaveRevertValue(sciConstant.getEnumeration().getDefaultReference());
				widgetStatusChanged();
			}
		});

		SciConstantCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		SciConstantCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private SciConstant getCurrentSciConstant() {
		return (SciConstant) getCurrentObject();
	}

	@Override
	protected void setCurrentFieldValues() {
		SciConstant currentSciConstant = getCurrentSciConstant();

		id.setText(String.valueOf(currentSciConstant.getId()));
		name.setText(currentSciConstant.getEnumeration().toString());
		description.setText(currentSciConstant.getEnumeration().getDescription());
		String defaultValueAsString = String.valueOf(currentSciConstant.getEnumeration().getDefaultValue());
		defaultValue.setText(defaultValueAsString);
		defaultReference.setText(currentSciConstant.getEnumeration().getDefaultReference());
		value.setNumber(currentSciConstant.getValue());
		reference.setText(currentSciConstant.getReference());
	}

	@Override
	protected void setDefaultFieldValues() {
		assert(false);
	}

	@Override
	public void enableWidgets() {
		boolean canEditConstants = LoginInfoCache.getInstance().getPermissions().isCanEditConstants();

		value.setEnabled(canEditConstants);

		if (!canEditConstants) {
			value.revert();
		}

		reference.setEnabled(canEditConstants);

		if (!canEditConstants) {
			reference.revert();
		}
	}

	@Override
	public void disableWidgets() {
		value.setEnabled(false);
		reference.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || value.hasChanged();
		isDirty = isDirty || reference.hasChangedIfTrimmed();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		boolean valueErrorSet = false;
		Double number = value.getNumber();

		if (number == null) {
			valueError.setToolTipText(Messages.sciConstantComposite_valueEmpty);
			valueErrorSet = true;
		}

		if (number != null && (Double.isInfinite(number) || Double.isNaN(number))) {
			valueError.setToolTipText(Messages.sciConstantComposite_valueMalformed);
			valueErrorSet = true;
		}

		if (valueErrorSet != valueError.getVisible()) {
			valueError.setVisible(valueErrorSet);
			layoutNeeded();
		}

		SciConstant currentSciConstant = getCurrentSciConstant();

		if (valueErrorSet || number != currentSciConstant.getEnumeration().getDefaultValue() || !reference.getText().trim().equals(defaultReference.getText().trim())) {
			defaultValue.setForeground(ColorCache.getColor(defaultValue.getDisplay(), ColorCache.RED));
			defaultReference.setForeground(ColorCache.getColor(defaultValue.getDisplay(), ColorCache.RED));
			resetButton.setEnabled(LoginInfoCache.getInstance().getPermissions().isCanEditConstants());
		} else {
			defaultValue.setForeground(ColorCache.getColor(defaultValue.getDisplay(), ColorCache.BLACK));
			defaultReference.setForeground(ColorCache.getColor(defaultValue.getDisplay(), ColorCache.BLACK));
			resetButton.setEnabled(false);
		}

		return valueErrorSet;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		SciConstant sciConstant = getCurrentSciConstant();
		Integer selectedSciConstantId = (Integer) selection.get(SciConstantPart.SELECTION_SCI_CONSTANT_ID);

		if (sciConstant == null && selectedSciConstantId == null) {
			return false;
		}

		if ((sciConstant == null && selectedSciConstantId != null) || (sciConstant != null && selectedSciConstantId == null)) {
			return true;
		}

		return sciConstant.getId() != selectedSciConstantId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer sciConstantId = (Integer) selection.get(SciConstantPart.SELECTION_SCI_CONSTANT_ID);

		if (sciConstantId != null) {
			int commandId = SciConstantCache.getInstance().sciConstantGet(sciConstantId, this);
			waitingFor(SCI_CONSTANT_GET, commandId);
			return true;
		}

		return false;
	}

	@Override
	protected void requestSave() {
		SciConstant oldSciConstant = getCurrentSciConstant();
		SciConstant newSciConstant = new SciConstant();

		newSciConstant.setId(oldSciConstant.getId());
		newSciConstant.setEnumeration(oldSciConstant.getEnumeration());
		newSciConstant.setValue(value.getNumber());
		newSciConstant.setReference(reference.getText());

		int commandId = SciConstantCache.getInstance().sciConstantSave(newSciConstant, this);
		waitingFor(SCI_CONSTANT_SAVE, commandId);
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
	public void sciConstantGetCompleted(int commandId, SciConstant sciConstant) {
		newObject(SCI_CONSTANT_GET, sciConstant);
	}

	@Override
	public void sciConstantUpdated(int commandId, SciConstant sciConstant) {
		updateObject(sciConstant, Messages.sciConstant_sciConstantHasBeenUpdated);
	}

	@Override
	public void sciConstantGetError(int commandId, String message) {
		raiseGetError(SCI_CONSTANT_GET, message);		
	}

	@Override
	public void sciConstantSaveCompleted(int commandId, SciConstant sciConstant) {
		saveComplete(SCI_CONSTANT_SAVE, sciConstant);
	}

	@Override
	public void sciConstantSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(SCI_CONSTANT_SAVE, message);
	}
}
