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

package org.easotope.client.admin.part.massspec;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.VText;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.admin.cache.massspec.massspec.MassSpecCacheMassSpecGetListener;
import org.easotope.shared.admin.cache.massspec.massspec.MassSpecCacheMassSpecSaveListener;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecList;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecListItem;
import org.easotope.shared.admin.tables.MassSpec;
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

public class MassSpecComposite extends EditorComposite implements MassSpecCacheMassSpecGetListener, MassSpecCacheMassSpecSaveListener {
	private static final String MASS_SPEC_GET = "MASS_SPEC_GET";
	private static final String MASS_SPEC_SAVE = "MASS_SPEC_SAVE";

	private Label id;
	private VText name;
	private Canvas nameError;
	private VText description;

	private MassSpecList lastMassSpecList = null;
	private HashSet<String> massSpecNames = new HashSet<String>();
	
	protected MassSpecComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		final Image errorImage = Icons.getError(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.massSpecComposite_massSpecIdLabel);

		id = new Label(this, SWT.NONE);

		Label massSpecNameLabel = new Label(this, SWT.NONE);
		massSpecNameLabel.setText(Messages.massSpecComposite_massSpecNameLabel);

		Composite massSpecNameComposite = new Composite(this, SWT.NONE);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		massSpecNameComposite.setLayout(gridLayout);

		name = new VText(massSpecNameComposite, SWT.BORDER);
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

		nameError = new Canvas(massSpecNameComposite, SWT.NONE);
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
		descriptionLabel.setText(Messages.massSpecComposite_descriptionLabel);

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

		MassSpecCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		MassSpecCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private MassSpec getCurrentMassSpec() {
		return (MassSpec) getCurrentObject();
	}

	@Override
	protected void setCurrentFieldValues() {
		MassSpec currentMassSpec = getCurrentMassSpec();
		
		id.setText(String.valueOf(currentMassSpec.getId()));
		name.setText(currentMassSpec.getName());
		description.setText(currentMassSpec.getDescription());
	}

	@Override
	protected void setDefaultFieldValues() {
		id.setText(Messages.massSpecComposite_newMassSpecId);
		name.setText("");
		description.setText("");
	}

	@Override
	public void enableWidgets() {
		boolean canEditMassSpecs = LoginInfoCache.getInstance().getPermissions().isCanEditMassSpecs();
		
		name.setEnabled(canEditMassSpecs);

		if (!canEditMassSpecs) {
			name.revert();
		}

		description.setEnabled(canEditMassSpecs);

		if (!canEditMassSpecs) {
			description.revert();
		}
	}

	@Override
	public void disableWidgets() {
		name.setEnabled(false);
		description.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || name.hasChangedIfTrimmed();
		isDirty = isDirty || description.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		boolean nameErrorSet = false;

		if (name.getText().trim().isEmpty()) {
			nameError.setToolTipText(Messages.massSpecComposite_massSpecNameEmpty);
			nameErrorSet = true;
		}
		
		if (name.hasChangedIfTrimmed() && massSpecNames.contains(name.getText().trim())) {
			nameError.setToolTipText(Messages.massSpecComposite_massSpecNameNotUnique);
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
		MassSpecList currentMassSpecList = (MassSpecList) selection.get(MassSpecPart.SELECTION_MASS_SPEC_LIST);

		if (currentMassSpecList != lastMassSpecList) {
			lastMassSpecList = currentMassSpecList;
			massSpecNames.clear();

			if (currentMassSpecList != null) {
				for (Integer id : currentMassSpecList.keySet()) {
					MassSpecListItem massSpecListItem = currentMassSpecList.get(id);
					massSpecNames.add(massSpecListItem.toString());
				}
			}
		}

		MassSpec massSpec = getCurrentMassSpec();
		Integer selectedMassSpecId = (Integer) selection.get(MassSpecPart.SELECTION_MASS_SPEC_ID);

		if (massSpec == null && selectedMassSpecId == null) {
			return false;
		}

		if ((massSpec == null && selectedMassSpecId != null) || (massSpec != null && selectedMassSpecId == null)) {
			return true;
		}

		return massSpec.getId() != selectedMassSpecId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer massSpecId = (Integer) selection.get(MassSpecPart.SELECTION_MASS_SPEC_ID);

		if (massSpecId != null) {
			int commandId = MassSpecCache.getInstance().massSpecGet(massSpecId, this);
			waitingFor(MASS_SPEC_GET, commandId);
			return true;
		}

		return false;
	}

	@Override
	protected void requestSave() {
		MassSpec oldMassSpec = getCurrentMassSpec();
		MassSpec newMassSpec = new MassSpec();

		if (oldMassSpec != null) {
			newMassSpec.setId(oldMassSpec.getId());
		}

		newMassSpec.setName(name.getText().trim());
		newMassSpec.setDescription(description.getText());

		int commandId = MassSpecCache.getInstance().massSpecSave(newMassSpec, this);
		waitingFor(MASS_SPEC_SAVE, commandId);
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
	public void massSpecGetCompleted(int commandId, MassSpec massSpec) {
		newObject(MASS_SPEC_GET, massSpec);
	}

	@Override
	public void massSpecUpdated(int commandId, MassSpec massSpec) {
		updateObject(massSpec, Messages.massSpec_massSpecHasBeenUpdated);
	}

	@Override
	public void massSpecGetError(int commandId, String message) {
		raiseGetError(MASS_SPEC_GET, message);		
	}

	@Override
	public void massSpecSaveCompleted(int commandId, MassSpec massSpec) {
		saveComplete(MASS_SPEC_SAVE, massSpec);
	}

	@Override
	public void massSpecSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(MASS_SPEC_SAVE, message);
	}
}
