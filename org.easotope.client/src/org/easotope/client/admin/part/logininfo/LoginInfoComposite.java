/*
 * Copyright © 2016-2019 by Devon Bowen.
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

package org.easotope.client.admin.part.logininfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.VButton;
import org.easotope.client.core.widgets.VCombo;
import org.easotope.client.core.widgets.VText;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoSaveListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
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

public class LoginInfoComposite extends EditorComposite implements LoginInfoCacheLoginInfoSaveListener {
	private static final String LOGIN_INFO_SAVE = "LOGIN_INFO_SAVE";

	private Label id;
	private Label username;
	private Label fullName;
	private VText oldPassword;
	private Canvas oldPasswordError;
	private VText newPassword1;
	private Canvas newPassword1Error;
	private VText newPassword2;
	private Canvas newPassword2Error;
	private Button isAdmin;
	private Button canEditMassSpecs;
	private Button canEditCorrections;
	private Button canEditSampleTypes;
	private Button canEditStandards;
	private Button canEditConstants;
	private Button canEditAllInput;
	private Button canImportDuplicates;
	private Button canBatchImport;
	private Button canDeleteAll;
	private Button canDeleteOwn;
	private VCombo timeZone;
	private VButton checkForUpdates;
	private VButton showTimeZone;
	private VButton leadingExponent;
	private VButton forceExponent;

	HashMap<Integer,String> indexToTimeZoneId = new HashMap<Integer,String>();
	HashMap<String,Integer> timeZoneIdToIndex = new HashMap<String,Integer>();

	protected LoginInfoComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		final Image errorImage = Icons.getError(parent.getDisplay());

		Label label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_id);

		id = new Label(this, SWT.NONE);
		
		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_username);

		username = new Label(this, SWT.NONE);

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_fullName);

		fullName = new Label(this, SWT.NONE);

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_oldPassword);

		Composite oldPasswordComposite = new Composite(this, SWT.NONE);
		oldPasswordComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		oldPasswordComposite.setLayout(gridLayout);

		oldPassword = new VText(oldPasswordComposite, SWT.PASSWORD | SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		oldPassword.setLayoutData(gridData);
		oldPassword.setEchoChar('*');
		oldPassword.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		oldPasswordError = new Canvas(oldPasswordComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		oldPasswordError.setLayoutData(gridData);
		oldPasswordError.setVisible(false);
		oldPasswordError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_newPassword1);

		Composite newPassword1Composite = new Composite(this, SWT.NONE);
		newPassword1Composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		newPassword1Composite.setLayout(gridLayout);

		newPassword1 = new VText(newPassword1Composite, SWT.PASSWORD | SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		newPassword1.setLayoutData(gridData);
		newPassword1.setEchoChar('*');
		newPassword1.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		newPassword1Error = new Canvas(newPassword1Composite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		newPassword1Error.setLayoutData(gridData);
		newPassword1Error.setVisible(false);
		newPassword1Error.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_newPassword2);

		Composite newPassword2Composite = new Composite(this, SWT.NONE);
		newPassword2Composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		newPassword2Composite.setLayout(gridLayout);

		newPassword2 = new VText(newPassword2Composite, SWT.PASSWORD | SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		newPassword2.setLayoutData(gridData);
		newPassword2.setEchoChar('*');
		newPassword2.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		newPassword2Error = new Canvas(newPassword2Composite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		newPassword2Error.setLayoutData(gridData);
		newPassword2Error.setVisible(false);
		newPassword2Error.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_timeZone);

		timeZone = new VCombo(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		timeZone.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		Locale locale = new Locale(Messages.locale);
		ArrayList<IdAndDescription> allZones = new ArrayList<IdAndDescription>();

		for (String timeZoneId : TimeZone.getAvailableIDs()) {
			allZones.add(new IdAndDescription(timeZoneId, TimeZone.getTimeZone(timeZoneId).getDisplayName(locale)));
		}

		Collections.sort(allZones);

		indexToTimeZoneId.put(0, null);
		timeZoneIdToIndex.put(null, 0);
		timeZone.add(Messages.loginInfo_defaultTimeZone);

		for (int i=0; i<allZones.size(); i++) {
			IdAndDescription idAndDescription = allZones.get(i);
			indexToTimeZoneId.put(i+1, idAndDescription.id);
			timeZoneIdToIndex.put(idAndDescription.id, i+1);
			timeZone.add(idAndDescription.key);
		}

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_showTimeZone);

		showTimeZone = new VButton(this, SWT.CHECK);
		showTimeZone.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_leadingExponent);

		leadingExponent = new VButton(this, SWT.CHECK);
		leadingExponent.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_forceExponent);

		forceExponent = new VButton(this, SWT.CHECK);
		forceExponent.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_checkForUpdates);

		checkForUpdates = new VButton(this, SWT.CHECK);
		checkForUpdates.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_isAdmin);

		isAdmin = new Button(this, SWT.CHECK);
		isAdmin.setEnabled(false);
		
		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_canEditMassSpecs);

		canEditMassSpecs = new Button(this, SWT.CHECK);
		canEditMassSpecs.setEnabled(false);

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_canEditCorrections);

		canEditCorrections = new Button(this, SWT.CHECK);
		canEditCorrections.setEnabled(false);
		
		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_canEditSampleTypes);

		canEditSampleTypes = new Button(this, SWT.CHECK);
		canEditSampleTypes.setEnabled(false);
		
		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_canEditStandards);

		canEditStandards = new Button(this, SWT.CHECK);
		canEditStandards.setEnabled(false);

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_canEditConstants);

		canEditConstants = new Button(this, SWT.CHECK);
		canEditConstants.setEnabled(false);

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_canEditAllInput);

		canEditAllInput = new Button(this, SWT.CHECK);
		canEditAllInput.setEnabled(false);

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_canImportDuplicates);

		canImportDuplicates = new Button(this, SWT.CHECK);
		canImportDuplicates.setEnabled(false);

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_canBatchImport);

		canBatchImport = new Button(this, SWT.CHECK);
		canBatchImport.setEnabled(false);

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_canDeleteAll);

		canDeleteAll = new Button(this, SWT.CHECK);
		canDeleteAll.setEnabled(false);

		label = new Label(this, SWT.NONE);
		label.setText(Messages.loginInfo_canDeleteOwn);

		canDeleteOwn = new Button(this, SWT.CHECK);
		canDeleteOwn.setEnabled(false);
	}

	@Override
	protected void handleDispose() {
		super.handleDispose();
	}

	private User getCurrentUser() {
		return LoginInfoCache.getInstance().getUser();
	}

	private Permissions getCurrentPermissions() {
		return LoginInfoCache.getInstance().getPermissions();
	}

	private Preferences getCurrentPreferences() {
		return LoginInfoCache.getInstance().getPreferences();
	}

	@Override
	protected void setCurrentFieldValues() {
		setCurrentConstantFieldValues();

		oldPassword.setText("");
		newPassword1.setText("");
		newPassword2.setText("");

		Preferences currentPreferences = getCurrentPreferences();

		if (timeZoneIdToIndex.containsKey(currentPreferences.getTimeZoneId())) {
			timeZone.select(timeZoneIdToIndex.get(currentPreferences.getTimeZoneId()));
		} else {
			timeZone.select(0);
		}

		showTimeZone.setSelection(currentPreferences.getShowTimeZone());
		leadingExponent.setSelection(currentPreferences.getLeadingExponent());
		forceExponent.setSelection(currentPreferences.getForceExponent());
		checkForUpdates.setSelection(currentPreferences.getCheckForUpdates());
	}

	private void setCurrentConstantFieldValues() {
		User currentUser = getCurrentUser();
		Permissions currentPermissions = getCurrentPermissions();

		id.setText(String.valueOf(currentUser.getId()));
		username.setText(currentUser.getUsername());
		fullName.setText(currentUser.getFullName());
		isAdmin.setSelection(currentUser.getIsAdmin());
		canEditMassSpecs.setSelection(currentPermissions.isCanEditMassSpecs());
		canEditCorrections.setSelection(currentPermissions.isCanEditCorrIntervals());
		canEditSampleTypes.setSelection(currentPermissions.isCanEditSampleTypes());
		canEditStandards.setSelection(currentPermissions.isCanEditStandards());
		canEditConstants.setSelection(currentPermissions.isCanEditConstants());
		canEditAllInput.setSelection(currentPermissions.isCanEditAllReplicates());
		canImportDuplicates.setSelection(currentPermissions.isCanImportDuplicates());
		canBatchImport.setSelection(currentPermissions.isCanBatchImport());
		canDeleteAll.setSelection(currentPermissions.isCanDeleteAll());
		canDeleteOwn.setSelection(currentPermissions.isCanDeleteOwn());

		layout();
	}

	@Override
	protected void setDefaultFieldValues() {
		assert(false);
	}

	@Override
	public void enableWidgets() {
		oldPassword.setEnabled(true);
		newPassword1.setEnabled(true);
		newPassword2.setEnabled(true);
		timeZone.setEnabled(true);
		showTimeZone.setEnabled(true);
		leadingExponent.setEnabled(true);
		forceExponent.setEnabled(true);
		checkForUpdates.setEnabled(true);
	}

	@Override
	public void disableWidgets() {
		oldPassword.setEnabled(false);
		newPassword1.setEnabled(false);
		newPassword2.setEnabled(false);
		timeZone.setEnabled(false);
		showTimeZone.setEnabled(false);
		leadingExponent.setEnabled(false);
		forceExponent.setEnabled(false);
		checkForUpdates.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || oldPassword.hasChanged();
		isDirty = isDirty || newPassword1.hasChanged();
		isDirty = isDirty || newPassword2.hasChanged();
		isDirty = isDirty || timeZone.hasChanged();
		isDirty = isDirty || showTimeZone.hasChanged();
		isDirty = isDirty || leadingExponent.hasChanged();
		isDirty = isDirty || forceExponent.hasChanged();
		isDirty = isDirty || checkForUpdates.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		User currentUser = getCurrentUser();
		boolean oldPasswordErrorSet = false;
		boolean newPassword1ErrorSet = false;
		boolean newPassword2ErrorSet = false;

		if (oldPassword.getText().isEmpty() && (!newPassword1.getText().isEmpty() || !newPassword2.getText().isEmpty())) {
			oldPasswordError.setToolTipText(Messages.loginInfo_oldPasswordEmpty);
			oldPasswordErrorSet = true;
		}

		if (!oldPassword.getText().isEmpty() && !currentUser.passwordMatches(oldPassword.getText())) {
			oldPasswordError.setToolTipText(Messages.loginInfo_oldPasswordInvalid);
			oldPasswordErrorSet = true;
		}

		if (oldPasswordErrorSet != oldPasswordError.getVisible()) {
			oldPasswordError.setVisible(oldPasswordErrorSet);
			layoutNeeded();
		}

		if (!oldPassword.getText().isEmpty() && newPassword1.getText().isEmpty() && newPassword2.getText().isEmpty()) {
			newPassword1Error.setToolTipText(Messages.loginInfo_newPasswordEmpty);
			newPassword1ErrorSet = true;
		}

		if (newPassword1ErrorSet != newPassword1Error.getVisible()) {
			newPassword1Error.setVisible(newPassword1ErrorSet);
			layoutNeeded();
		}

		if ((!newPassword1.getText().isEmpty() || !newPassword2.getText().isEmpty()) && !newPassword1.getText().equals(newPassword2.getText())) {
			newPassword2Error.setToolTipText(Messages.loginInfo_newPasswordsDontMatch);
			newPassword2ErrorSet = true;
		}

		if (newPassword2ErrorSet != newPassword2Error.getVisible()) {
			newPassword2Error.setVisible(newPassword2ErrorSet);
			layoutNeeded();
		}

		return oldPasswordErrorSet || newPassword1ErrorSet || newPassword2ErrorSet;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		return true;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		newObject(null, new Object());
		return true;
	}

	@Override
	protected void requestSave(boolean isResend) {
		String password = newPassword1.getText();
		String tz = indexToTimeZoneId.get(timeZone.getSelectionIndex());
		boolean showtz = showTimeZone.getSelection();
		boolean leadingExp = leadingExponent.getSelection();
		boolean forceExp = forceExponent.getSelection();
		boolean updates = checkForUpdates.getSelection();

		int commandId = LoginInfoCache.getInstance().savePreferences(password, tz, updates, showtz, leadingExp, forceExp, this);
		waitingFor(LOGIN_INFO_SAVE, commandId);
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
	public void loginInfoSaveCompleted(int commandId, User user, Permissions permissions, Preferences preferences) {
		saveComplete(LOGIN_INFO_SAVE, new Object());		
	}

	@Override
	public void loginInfoSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(LOGIN_INFO_SAVE, message);
	}

	@Override
	protected boolean newIsReplacementForOld(Object oldObject, Object newObject) {
		return true;
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
		updateObject(new Object(), Messages.loginInfo_hasBeenUpdated);
		super.loginInfoUpdated(commandId, user, permissions, preferences);
	}

	class IdAndDescription implements Comparable<IdAndDescription> {
		String id;
		String description;
		String key;

		IdAndDescription(String id, String description) {
			this.id = id;
			this.description = description;
			key = this.id + " (" + this.description + ")";
		}

		@Override
		public int compareTo(IdAndDescription that) {
			return key.compareTo(that.key);
		}
	}
}
