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

package org.easotope.client.admin.part.massspec;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.DateTimeLabel;
import org.easotope.client.core.widgets.NumericValueList;
import org.easotope.client.core.widgets.NumericValueListListener;
import org.easotope.client.core.widgets.VDateTime;
import org.easotope.client.core.widgets.VDateTimeListener;
import org.easotope.client.core.widgets.VText;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.RefGasParameter;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.admin.cache.massspec.refgas.MassSpecCacheRefGasGetListener;
import org.easotope.shared.admin.cache.massspec.refgas.MassSpecCacheRefGasSaveListener;
import org.easotope.shared.admin.cache.massspec.refgaslist.RefGasList;
import org.easotope.shared.admin.cache.massspec.refgaslist.RefGasListItem;
import org.easotope.shared.admin.tables.RefGas;
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

public class RefGasComposite extends EditorComposite implements MassSpecCacheRefGasGetListener, MassSpecCacheRefGasSaveListener {
	private static final String REF_GAS_GET = "REF_GAS_GET";
	private static final String REF_GAS_SAVE = "REF_GAS_SAVE";

	private Label id;
	private VDateTime validFrom;
	private Canvas validFromError;
	private DateTimeLabel validTo;
	private VText description;
	private NumericValueList values;

	private RefGasList lastRefGasList = null;
	private HashSet<Long> refGasDates = new HashSet<Long>();
	
	protected RefGasComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		final Image errorImage = Icons.getError(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.refGasComposite_idLabel);

		id = new Label(this, SWT.NONE);

		Label validFromLabel = new Label(this, SWT.NONE);
		validFromLabel.setText(Messages.refGasComposite_validFromLabel);

		Composite validFromComposite = new Composite(this, SWT.NONE);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		validFromComposite.setLayout(gridLayout);

		validFrom = new VDateTime(validFromComposite, SWT.CHECK);
		validFrom.addSelectionListener(new VDateTimeListener() {
			@Override
			public void widgetSelected() {
				widgetStatusChanged();
			}
		});

		validFromError = new Canvas(validFromComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		validFromError.setLayoutData(gridData);

		validFromError.setVisible(false);
		validFromError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Label validToLabel = new Label(this, SWT.NONE);
		validToLabel.setText(Messages.refGasComposite_validToLabel);

		validTo = new DateTimeLabel(this, SWT.NONE);
		validTo.setDefaultText(Messages.refGasComposite_newRefGasValidTo);

		Label descriptionLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		descriptionLabel.setLayoutData(gridData);
		descriptionLabel.setText(Messages.refGasComposite_descriptionLabel);

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
		valuesLabel.setText(Messages.refGasComposite_valuesLabel);

		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();

		for (RefGasParameter refGasParameter : RefGasParameter.values()) {
			if (refGasParameter != RefGasParameter.NONE) {
				possibilities.put(refGasParameter.ordinal(), refGasParameter.name());
			}
		}

		HashMap<Integer,HashMap<Integer,String>> descriptionPossibilities = new HashMap<Integer,HashMap<Integer,String>>();

		HashMap<Integer,String> temp = new HashMap<Integer,String>();
		temp.put(IsotopicScale.VPDB.ordinal(), IsotopicScale.VPDB.toString());
		descriptionPossibilities.put(RefGasParameter.δ13C.ordinal(), temp);

		temp = new HashMap<Integer,String>();
		temp.put(IsotopicScale.VPDB.ordinal(), IsotopicScale.VPDB.toString());
		temp.put(IsotopicScale.VSMOW.ordinal(), IsotopicScale.VSMOW.toString());
		descriptionPossibilities.put(RefGasParameter.δ18O.ordinal(), temp);

		values = new NumericValueList(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		values.setLayoutData(gridData);
		values.setValueName(Messages.refGasComposite_valueName);
		values.setDescriptionName(Messages.refGasComposite_descriptionName);
		values.setLabelPossibilities(possibilities);
		values.setDescriptionPossibilities(descriptionPossibilities);
		values.setValues(null);
		values.addSelectionListener(new NumericValueListListener() {
			@Override
			public void widgetSelected() {
				widgetStatusChanged();
			}
		});

		MassSpecCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		MassSpecCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private RefGas getCurrentRefGas() {
		return (RefGas) getCurrentObject();
	}

	@Override
	protected void setCurrentFieldValues() {
		RefGas currentRefGas = getCurrentRefGas();
		
		id.setText(String.valueOf(currentRefGas.getId()));
		validFrom.setDate(currentRefGas.getValidFrom());

		if (currentRefGas.getValidUntil() == DatabaseConstants.MAX_DATE) {
			validTo.unsetDate();
		} else {
			validTo.setDate(currentRefGas.getValidUntil());
		}

		description.setText(currentRefGas.getDescription());
		values.setValues(currentRefGas.getValues());
	}

	@Override
	protected void setDefaultFieldValues() {
		id.setText(Messages.refGasComposite_newRefGasId);
		validFrom.setDate(new Date().getTime());
		validTo.unsetDate();
		description.setText("");
		values.setValues(null);
	}

	@Override
	public void enableWidgets() {
		boolean canEditMassSpecs = LoginInfoCache.getInstance().getPermissions().isCanEditMassSpecs();
		
		validFrom.setEnabled(canEditMassSpecs);

		if (!canEditMassSpecs) {
			validFrom.revert();
		}

		description.setEnabled(canEditMassSpecs);

		if (!canEditMassSpecs) {
			description.revert();
		}

		values.setEnabled(canEditMassSpecs);

		if (!canEditMassSpecs) {
			values.revert();
		}
	}

	@Override
	public void disableWidgets() {
		validFrom.setEnabled(false);
		description.setEnabled(false);
		values.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || validFrom.hasChanged();
		isDirty = isDirty || description.hasChanged();
		isDirty = isDirty || values.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		boolean dateErrorSet = false;

		if (validFrom.hasChanged() && refGasDates.contains(validFrom.getDate())) {
			validFromError.setToolTipText(Messages.refGasComposite_validFromDateNotUnique);
			dateErrorSet = true;
		}

		if (dateErrorSet != validFromError.getVisible()) {
			validFromError.setVisible(dateErrorSet);
			layoutNeeded();
		}

		return dateErrorSet || values.hasError();
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		RefGasList currentRefGasList = (RefGasList) selection.get(MassSpecPart.SELECTION_REF_GAS_LIST);

		if (currentRefGasList != lastRefGasList) {
			lastRefGasList = currentRefGasList;
			refGasDates.clear();

			if (currentRefGasList != null) {
				for (Integer id : currentRefGasList.keySet()) {
					RefGasListItem refGasListItem = currentRefGasList.get(id);
					refGasDates.add(refGasListItem.getValidFrom());
				}
			}
		}

		RefGas refGas = getCurrentRefGas();
		Integer selectedRefGasId = (Integer) selection.get(MassSpecPart.SELECTION_REF_GAS_ID);

		if (refGas == null && selectedRefGasId == null) {
			return false;
		}

		if ((refGas == null && selectedRefGasId != null) || (refGas != null && selectedRefGasId == null)) {
			return true;
		}

		return refGas.getId() != selectedRefGasId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer refGasId = (Integer) selection.get(MassSpecPart.SELECTION_REF_GAS_ID);

		if (refGasId != null) {
			int commandId = MassSpecCache.getInstance().refGasGet(refGasId, this);
			waitingFor(REF_GAS_GET, commandId);
			return true;
		}

		return false;
	}

	@Override
	protected void requestSave(boolean isResend) {
		RefGas oldRefGas = getCurrentRefGas();
		RefGas newRefGas = new RefGas();

		if (oldRefGas != null) {
			newRefGas.id = oldRefGas.getId();
		}

		newRefGas.setMassSpecId(lastRefGasList.getMassSpecId());
		newRefGas.setValidFrom(validFrom.getDate());
		newRefGas.setDescription(description.getText());
		newRefGas.setValues(values.getValues());

		int commandId = MassSpecCache.getInstance().refGasSave(newRefGas, this);
		waitingFor(REF_GAS_SAVE, commandId);
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
	public void refGasGetCompleted(int commandId, RefGas refGas) {
		newObject(REF_GAS_GET, refGas);
	}

	@Override
	public void refGasUpdated(int commandId, RefGas refGas) {
		updateObject(refGas, Messages.massSpec_refGasHasBeenUpdated);
	}

	@Override
	public void refGasGetError(int commandId, String message) {
		raiseGetError(REF_GAS_GET, message);		
	}

	@Override
	public void refGasSaveCompleted(int commandId, RefGas refGas) {
		saveComplete(REF_GAS_SAVE, refGas);
	}

	@Override
	public void refGasSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(REF_GAS_SAVE, message);
	}
}
