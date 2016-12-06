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

package org.easotope.client.admin.part.sampletype;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.VButton;
import org.easotope.client.core.widgets.VText;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.admin.cache.sampletype.SampleTypeCache;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.AcidTempList;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.SampleTypeCacheAcidTempListGetListener;
import org.easotope.shared.admin.cache.sampletype.sampletype.SampleTypeCacheSampleTypeGetListener;
import org.easotope.shared.admin.cache.sampletype.sampletype.SampleTypeCacheSampleTypeSaveListener;
import org.easotope.shared.admin.cache.sampletype.sampletypelist.SampleTypeList;
import org.easotope.shared.admin.cache.sampletype.sampletypelist.SampleTypeListItem;
import org.easotope.shared.admin.tables.SampleType;
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

public class SampleTypeComposite extends EditorComposite implements SampleTypeCacheSampleTypeGetListener, SampleTypeCacheSampleTypeSaveListener, SampleTypeCacheAcidTempListGetListener {
	private static final String ACID_TEMP_LIST_GET = "ACID_TEMP_LIST_GET";
	private static final String SAMPLE_TYPE_GET = "SAMPLE_TYPE_GET";
	private static final String SAMPLE_TYPE_SAVE = "SAMPLE_TYPE_SAVE";

 	private Label id;
	private VText name;
	private Canvas nameError;
	private VText description;
	private VButton hasAcidTemps;
	private SortedCombo defaultAcidTemp;

	private SampleTypeList lastSampleTypeList = null;
	private HashSet<String> sampleTypeNames = new HashSet<String>();
	
	protected SampleTypeComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		final Image errorImage = Icons.getError(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.sampleTypeComposite_sampleTypeIdLabel);

		id = new Label(this, SWT.NONE);

		Label sampleTypeNameLabel = new Label(this, SWT.NONE);
		sampleTypeNameLabel.setText(Messages.sampleTypeComposite_sampleTypeNameLabel);

		Composite sampleTypeNameComposite = new Composite(this, SWT.NONE);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		sampleTypeNameComposite.setLayout(gridLayout);

		name = new VText(sampleTypeNameComposite, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		name.setLayoutData(gridData);
		name.setTextLimit(GuiConstants.SHORT_TEXT_INPUT_LIMIT);
		name.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});
		name.addListener(SWT.Verify, new LoggingAdaptor() {
			public void loggingHandleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!Character.isLetterOrDigit(chars[i]) && chars[i] != '_' && chars[i] != '-' && chars[i] != ' ') {
						e.doit = false;
						return;
					}
				}
			}
		});

		nameError = new Canvas(sampleTypeNameComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		nameError.setLayoutData(gridData);

		nameError.setVisible(false);
		nameError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Label descriptionLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		descriptionLabel.setLayoutData(gridData);
		descriptionLabel.setText(Messages.sampleTypeComposite_descriptionLabel);

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

		Label hasAcidTempsLabel = new Label(this, SWT.NONE);
		hasAcidTempsLabel.setText(Messages.sampleTypeComposite_hasAcidTempsLabel);

		hasAcidTemps = new VButton(this, SWT.CHECK);
		hasAcidTemps.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label defaultAcidTempLabel = new Label(this, SWT.NONE);
		defaultAcidTempLabel.setText(Messages.sampleTypeComposite_defaultAcidTempLabel);

		defaultAcidTemp = new SortedCombo(this, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		defaultAcidTemp.setLayoutData(gridData);
		defaultAcidTemp.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
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

	private SampleType getCurrentSampleType() {
		return (SampleType) getCurrentObject();
	}

	@Override
	protected void setCurrentFieldValues() {
		SampleType currentSampleType = getCurrentSampleType();

		id.setText(String.valueOf(currentSampleType.getId()));
		name.setText(currentSampleType.getName());
		description.setText(currentSampleType.getDescription());
		hasAcidTemps.setSelection(currentSampleType.getHasAcidTemps());

		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();
		possibilities.put(DatabaseConstants.EMPTY_DB_ID, "");
		defaultAcidTemp.setPossibilities(possibilities);
		defaultAcidTemp.selectInteger(currentSampleType.getDefaultAcidTemp());

		cancelWaitingFor(ACID_TEMP_LIST_GET);
		int commandId = SampleTypeCache.getInstance().acidTempListGet(currentSampleType.getId(), this);
		waitingFor(ACID_TEMP_LIST_GET, commandId);
		
		propogateSelection(SampleTypePart.SELECTION_SAMPLE_TYPE_HAS_ACID_TEMPS, currentSampleType.getHasAcidTemps());
	}

	@Override
	protected void setDefaultFieldValues() {
		id.setText(Messages.sampleTypeComposite_newSampleTypeId);
		name.setText("");
		description.setText("");
		hasAcidTemps.setSelection(false);

		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();
		possibilities.put(DatabaseConstants.EMPTY_DB_ID, "");
		defaultAcidTemp.setPossibilities(possibilities);
		defaultAcidTemp.selectInteger(DatabaseConstants.EMPTY_DB_ID);

		cancelWaitingFor(ACID_TEMP_LIST_GET);
		propogateSelection(SampleTypePart.SELECTION_SAMPLE_TYPE_HAS_ACID_TEMPS, false);
	}

	@Override
	public void enableWidgets() {
		boolean canEditSampleTypes = LoginInfoCache.getInstance().getPermissions().isCanEditSampleTypes();
		
		name.setEnabled(canEditSampleTypes);

		if (!canEditSampleTypes) {
			name.revert();
		}

		description.setEnabled(canEditSampleTypes);

		if (!canEditSampleTypes) {
			description.revert();
		}

		hasAcidTemps.setEnabled(canEditSampleTypes && getCurrentSampleType() == null);
		
		if (!canEditSampleTypes) {
			hasAcidTemps.revert();
		}

		defaultAcidTemp.setEnabled(canEditSampleTypes && (getCurrentSampleType() != null && getCurrentSampleType().getHasAcidTemps()));

		if (!canEditSampleTypes) {
			defaultAcidTemp.revert();
		}
	}

	@Override
	public void disableWidgets() {
		name.setEnabled(false);
		description.setEnabled(false);
		hasAcidTemps.setEnabled(false);
		defaultAcidTemp.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || name.hasChangedIfTrimmed();
		isDirty = isDirty || description.hasChanged();
		isDirty = isDirty || hasAcidTemps.hasChanged();
		isDirty = isDirty || defaultAcidTemp.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		boolean nameErrorSet = false;

		if (name.getText().trim().isEmpty()) {
			nameError.setToolTipText(Messages.sampleTypeComposite_sampleTypeNameEmpty);
			nameErrorSet = true;
		}

		if (name.hasChangedIfTrimmed() && sampleTypeNames.contains(name.getText().trim())) {
			nameError.setToolTipText(Messages.sampleTypeComposite_sampleTypeNameNotUnique);
			nameErrorSet = true;
		}

		if (nameErrorSet != nameError.getVisible()) {
			nameError.setVisible(nameErrorSet);
			layoutNeeded();
		}

		return nameErrorSet;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		SampleTypeList currentSampleTypeList = (SampleTypeList) selection.get(SampleTypePart.SELECTION_SAMPLE_TYPE_LIST);

		if (currentSampleTypeList != lastSampleTypeList) {
			lastSampleTypeList = currentSampleTypeList;
			sampleTypeNames.clear();

			if (currentSampleTypeList != null) {
				for (Integer id : currentSampleTypeList.keySet()) {
					SampleTypeListItem sampleTypeListItem = currentSampleTypeList.get(id);
					sampleTypeNames.add(sampleTypeListItem.toString());
				}
			}
		}

		SampleType sampleType = getCurrentSampleType();
		Integer selectedSampleTypeId = (Integer) selection.get(SampleTypePart.SELECTION_SAMPLE_TYPE_ID);

		if (sampleType == null && selectedSampleTypeId == null) {
			return false;
		}

		if ((sampleType == null && selectedSampleTypeId != null) || (sampleType != null && selectedSampleTypeId == null)) {
			return true;
		}

		return sampleType.getId() != selectedSampleTypeId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer sampleTypeId = (Integer) selection.get(SampleTypePart.SELECTION_SAMPLE_TYPE_ID);

		if (sampleTypeId != null) {
			int commandId = SampleTypeCache.getInstance().sampleTypeGet(sampleTypeId, this);
			waitingFor(SAMPLE_TYPE_GET, commandId);
			return true;
		}

		return false;
	}

	@Override
	protected void requestSave() {
		SampleType oldSampleType = getCurrentSampleType();
		SampleType newSampleType = new SampleType();

		if (oldSampleType != null) {
			newSampleType.setId(oldSampleType.getId());
		}

		newSampleType.setName(name.getText().trim());
		newSampleType.setDescription(description.getText());
		newSampleType.setHasAcidTemps(hasAcidTemps.getSelection());
		newSampleType.setDefaultAcidTemp(defaultAcidTemp.getSelectedInteger());

		int commandId = SampleTypeCache.getInstance().sampleTypeSave(newSampleType, this);
		waitingFor(SAMPLE_TYPE_SAVE, commandId);
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
	public void sampleTypeGetCompleted(int commandId, SampleType sampleType) {
		newObject(SAMPLE_TYPE_GET, sampleType);
	}

	@Override
	public void sampleTypeUpdated(int commandId, SampleType sampleType) {
		updateObject(sampleType, Messages.sampleType_sampleTypeHasBeenUpdated);
	}

	@Override
	public void sampleTypeGetError(int commandId, String message) {
		raiseGetError(SAMPLE_TYPE_GET, message);		
	}

	@Override
	public void sampleTypeSaveCompleted(int commandId, SampleType sampleType) {
		saveComplete(SAMPLE_TYPE_SAVE, sampleType);
	}

	@Override
	public void sampleTypeSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(SAMPLE_TYPE_SAVE, message);
	}

	private void newAcidTempList(AcidTempList acidTempList) {
		int sampleTypeId = acidTempList.getSampleTypeId();

		if (getCurrentSampleType() != null && sampleTypeId == getCurrentSampleType().getId()) {
			HashMap<Integer,String> possibilitiesAsStrings = new HashMap<Integer,String>();

			possibilitiesAsStrings.put(DatabaseConstants.EMPTY_DB_ID, "");

			for (Integer key : acidTempList.keySet()) {
				possibilitiesAsStrings.put(key, acidTempList.get(key).toString());
			}

			defaultAcidTemp.setPossibilities(possibilitiesAsStrings);
			cancelWaitingFor(ACID_TEMP_LIST_GET);
		}
	}

	@Override
	public void acidTempListGetCompleted(int commandId, AcidTempList acidTempList) {
		newAcidTempList(acidTempList);
	}

	@Override
	public void acidTempListUpdated(int commandId, AcidTempList acidTempList) {
		newAcidTempList(acidTempList);
	}

	@Override
	public void acidTempListGetError(int commandId, String message) {
		raiseGetError(ACID_TEMP_LIST_GET, message);		
	}
}
