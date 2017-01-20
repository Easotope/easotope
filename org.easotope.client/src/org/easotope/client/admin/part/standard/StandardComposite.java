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

package org.easotope.client.admin.part.standard;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.NumericValueList;
import org.easotope.client.core.widgets.NumericValueListListener;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.VTableCombo;
import org.easotope.client.core.widgets.VText;
import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.StandardParameter;
import org.easotope.shared.admin.cache.sampletype.SampleTypeCache;
import org.easotope.shared.admin.cache.sampletype.sampletypelist.SampleTypeCacheSampleTypeListGetListener;
import org.easotope.shared.admin.cache.sampletype.sampletypelist.SampleTypeList;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standard.StandardCacheStandardGetListener;
import org.easotope.shared.admin.cache.standard.standard.StandardCacheStandardSaveListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardList;
import org.easotope.shared.admin.cache.standard.standardlist.StandardListItem;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;

public class StandardComposite extends EditorComposite implements SampleTypeCacheSampleTypeListGetListener, StandardCacheStandardGetListener, StandardCacheStandardSaveListener {
	private static final String SAMPLE_TYPE_LIST_GET = "SAMPLE_TYPE_LIST_GET";
	private static final String STANDARD_GET = "STANDARD_GET";
	private static final String STANDARD_SAVE = "STANDARD_SAVE";

 	private Label id;
	private VText name;
	private Canvas nameError;
	private VTableCombo color;
	private VTableCombo shape;
	private VText description;
	private SortedCombo sampleType;
	private Canvas sampleTypeError;
	private NumericValueList values;

	private StandardList lastStandardList = null;
	private HashSet<String> standardNames = new HashSet<String>();
	
	protected StandardComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		final Image errorImage = Icons.getError(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.standardComposite_standardIdLabel);

		id = new Label(this, SWT.NONE);

		Label standardNameLabel = new Label(this, SWT.NONE);
		standardNameLabel.setText(Messages.standardComposite_standardNameLabel);

		Composite standardNameComposite = new Composite(this, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		standardNameComposite.setLayout(gridLayout);

		name = new VText(standardNameComposite, SWT.BORDER);
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

		nameError = new Canvas(standardNameComposite, SWT.NONE);
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

		Label designLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		designLabel.setLayoutData(gridData);
		designLabel.setText(Messages.standardComposite_graphicDesign);

		Composite graphicComposite = new Composite(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		graphicComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		graphicComposite.setLayout(gridLayout);

		color = new VTableCombo(graphicComposite, SWT.BORDER | SWT.READ_ONLY);
		color.setLayoutData(new GridData(35, SWT.DEFAULT));
		color.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		for (int i=0; i<ColorCache.getNumberOfColorsInPalette(); i++) {			
			Image image = new Image(parent.getDisplay(), 26, 20);

			GC gc = new GC(image);
			gc.setBackground(ColorCache.getColorFromPalette(getDisplay(), i));
			gc.fillRectangle(0, 0, image.getBounds().width, image.getBounds().height);
			gc.dispose();

			TableItem item = new TableItem(color.getTable(), SWT.NONE);
			item.setImage(image);
		}

		shape = new VTableCombo(graphicComposite, SWT.BORDER | SWT.READ_ONLY);
		shape.setLayoutData(new GridData(35, SWT.DEFAULT));
		shape.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		for (PointStyle pointStyle : PointStyle.values()) {
			if (pointStyle == PointStyle.R || pointStyle == PointStyle.S) {
				continue;
			}

			Image image = new Image(parent.getDisplay(), 26, 20);

			GC gc = new GC(image);
			new PointDesign(parent.getDisplay(), ColorCache.getColor(getDisplay(), ColorCache.BLACK), pointStyle).draw(gc, image.getBounds().width / 2, image.getBounds().height / 2, false);
			gc.dispose();

			TableItem item = new TableItem(shape.getTable(), SWT.NONE);
			item.setImage(image);
		}

		Label descriptionLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		descriptionLabel.setLayoutData(gridData);
		descriptionLabel.setText(Messages.standardComposite_descriptionLabel);

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

		Label sampleTypeLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		sampleTypeLabel.setLayoutData(gridData);

		sampleTypeLabel.setText(Messages.standardComposite_sampleTypeLabel);

		Composite sampleTypeComposite = new Composite(this, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		sampleTypeComposite.setLayout(gridLayout);

		sampleType = new SortedCombo(sampleTypeComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		sampleType.setLayoutData(gridData);
		sampleType.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		sampleTypeError = new Canvas(sampleTypeComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		sampleTypeError.setLayoutData(gridData);
		sampleTypeError.setVisible(false);
		sampleTypeError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});
		
		Label valuesLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		valuesLabel.setLayoutData(gridData);
		valuesLabel.setText(Messages.standardComposite_valuesLabel);

		HashMap<Integer,String> possibilities = new HashMap<Integer,String>();

		for (StandardParameter standardParameter : StandardParameter.values()) {
			if (standardParameter != StandardParameter.NONE) {
				possibilities.put(standardParameter.ordinal(), standardParameter.name());
			}
		}

		HashMap<Integer,HashMap<Integer,String>> descriptionPossibilities = new HashMap<Integer,HashMap<Integer,String>>();

		HashMap<Integer,String> temp = new HashMap<Integer,String>();
		temp.put(IsotopicScale.VPDB.ordinal(), IsotopicScale.VPDB.toString());
		descriptionPossibilities.put(StandardParameter.δ13C.ordinal(), temp);

		temp = new HashMap<Integer,String>();
		temp.put(IsotopicScale.VPDB.ordinal(), IsotopicScale.VPDB.toString());
		temp.put(IsotopicScale.VSMOW.ordinal(), IsotopicScale.VSMOW.toString());
		descriptionPossibilities.put(StandardParameter.δ18O.ordinal(), temp);

		temp = new HashMap<Integer,String>();
		temp.put(IsotopicScale.CDES.ordinal(), IsotopicScale.CDES.toString());
		descriptionPossibilities.put(StandardParameter.Δ47.ordinal(), temp);

		values = new NumericValueList(this, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		values.setLayoutData(gridData);
		values.setValueName(Messages.standardComposite_valueName);
		values.setDescriptionName(Messages.standardComposite_descriptionName);
		values.setLabelPossibilities(possibilities);
		values.setDescriptionPossibilities(descriptionPossibilities);
		values.setValues(null);
		values.addSelectionListener(new NumericValueListListener() {
			@Override
			public void widgetSelected() {
				widgetStatusChanged();
			}
		});

		int commandId = SampleTypeCache.getInstance().sampleTypeListGet(this);
		waitingFor(SAMPLE_TYPE_LIST_GET, commandId);

		SampleTypeCache.getInstance().addListener(this);
		StandardCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		SampleTypeCache.getInstance().removeListener(this);
		StandardCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private Standard getCurrentStandard() {
		return (Standard) getCurrentObject();
	}

	@Override
	protected void setCurrentFieldValues() {
		Standard currentStandard = getCurrentStandard();

		id.setText(String.valueOf(currentStandard.getId()));
		name.setText(currentStandard.getName());
		color.select(currentStandard.getColorId());
		shape.select(currentStandard.getShapeId());
		description.setText(currentStandard.getDescription());
		sampleType.selectInteger(currentStandard.getSampleTypeId());
		values.setValues(currentStandard.getValues());
	}

	@Override
	protected void setDefaultFieldValues() {
		id.setText(Messages.standardComposite_newStandardId);
		name.setText("");
		color.select(0);
		shape.select(0);
		description.setText("");
		sampleType.selectInteger(-1);
		values.setValues(null);
	}

	@Override
	public void enableWidgets() {
		boolean canEditStandards = LoginInfoCache.getInstance().getPermissions().isCanEditStandards();
		
		name.setEnabled(canEditStandards);

		if (!canEditStandards) {
			name.revert();
		}

		color.setEnabled(canEditStandards);

		if (!canEditStandards) {
			color.revert();
		}

		shape.setEnabled(canEditStandards);

		if (!canEditStandards) {
			shape.revert();
		}

		description.setEnabled(canEditStandards);

		if (!canEditStandards) {
			description.revert();
		}

		sampleType.setEnabled(canEditStandards && getCurrentStandard() == null);

		if (!canEditStandards) {
			sampleType.revert();
		}

		values.setEnabled(canEditStandards);

		if (!canEditStandards) {
			values.revert();
		}
	}

	@Override
	public void disableWidgets() {
		name.setEnabled(false);
		color.setEnabled(false);
		shape.setEnabled(false);
		description.setEnabled(false);
		sampleType.setEnabled(false);
		values.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || name.hasChangedIfTrimmed();
		isDirty = isDirty || color.hasChanged();
		isDirty = isDirty || shape.hasChanged();
		isDirty = isDirty || description.hasChanged();
		isDirty = isDirty || sampleType.hasChanged();
		isDirty = isDirty || values.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		boolean nameErrorSet = false;

		if (name.getText().trim().isEmpty()) {
			nameError.setToolTipText(Messages.standardComposite_standardNameEmpty);
			nameErrorSet = true;
		}

		if (name.hasChangedIfTrimmed() && standardNames.contains(name.getText().trim())) {
			nameError.setToolTipText(Messages.standardComposite_standardNameNotUnique);
			nameErrorSet = true;
		}

		if (nameErrorSet != nameError.getVisible()) {
			nameError.setVisible(nameErrorSet);
			layoutNeeded();
		}

		boolean sampleTypeErrorSet = false;

		if (sampleType.getSelectedInteger() == -1) {
			sampleTypeError.setToolTipText(Messages.standardComposite_sampleTypeEmpty);
			sampleTypeErrorSet = true;
		}

		if (sampleTypeErrorSet != sampleTypeError.getVisible()) {
			sampleTypeError.setVisible(sampleTypeErrorSet);
			layoutNeeded();
		}

		return nameErrorSet || sampleTypeErrorSet || values.hasError();
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		StandardList currentStandardList = (StandardList) selection.get(StandardPart.SELECTION_STANDARD_LIST);

		if (currentStandardList != lastStandardList) {
			lastStandardList = currentStandardList;
			standardNames.clear();

			if (currentStandardList != null) {
				for (Integer id : currentStandardList.keySet()) {
					StandardListItem standardListItem = currentStandardList.get(id);
					standardNames.add(standardListItem.toString());
				}
			}
		}

		Standard standard = getCurrentStandard();
		Integer selectedStandardId = (Integer) selection.get(StandardPart.SELECTION_STANDARD_ID);

		if (standard == null && selectedStandardId == null) {
			return false;
		}

		if ((standard == null && selectedStandardId != null) || (standard != null && selectedStandardId == null)) {
			return true;
		}

		return standard.getId() != selectedStandardId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer standardId = (Integer) selection.get(StandardPart.SELECTION_STANDARD_ID);

		if (standardId != null) {
			int commandId = StandardCache.getInstance().standardGet(standardId, this);
			waitingFor(STANDARD_GET, commandId);
			return true;
		}

		return false;
	}

	@Override
	protected void requestSave(boolean isResend) {
		Standard oldStandard = getCurrentStandard();
		Standard newStandard = new Standard();

		if (oldStandard != null) {
			newStandard.setId(oldStandard.getId());
		}

		newStandard.setName(name.getText().trim());
		newStandard.setColorId(color.getSelectionIndex());
		newStandard.setShapeId(shape.getSelectionIndex());
		newStandard.setDescription(description.getText());
		newStandard.setSampleTypeId(sampleType.getSelectedInteger());
		newStandard.setValues(values.getValues());

		int commandId = StandardCache.getInstance().standardSave(newStandard, this);
		waitingFor(STANDARD_SAVE, commandId);
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
	public void sampleTypeListGetCompleted(int commandId, final SampleTypeList sampleTypeList) {
		sampleType.setPossibilities(sampleTypeList);
		cancelWaitingFor(SAMPLE_TYPE_LIST_GET);
	}

	@Override
	public void sampleTypeListUpdated(int commandId, final SampleTypeList sampleTypeList) {
		sampleType.setPossibilities(sampleTypeList);
		cancelWaitingFor(SAMPLE_TYPE_LIST_GET);
	}

	@Override
	public void sampleTypeListGetError(int commandId, final String message) {
		raiseGetError(SAMPLE_TYPE_LIST_GET, message);
	}

	@Override
	public void standardGetCompleted(int commandId, Standard standard) {
		newObject(STANDARD_GET, standard);
	}

	@Override
	public void standardUpdated(int commandId, Standard standard) {
		updateObject(standard, Messages.standardAdminPart_standardHasBeenUpdated);
	}

	@Override
	public void standardGetError(int commandId, String message) {
		raiseGetError(STANDARD_GET, message);		
	}

	@Override
	public void standardSaveCompleted(int commandId, Standard standard) {
		saveComplete(STANDARD_SAVE, standard);
	}

	@Override
	public void standardSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(STANDARD_SAVE, message);
	}
}
