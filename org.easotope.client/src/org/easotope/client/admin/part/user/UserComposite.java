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

package org.easotope.client.admin.part.user;

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
import org.easotope.client.core.widgets.VButton;
import org.easotope.client.core.widgets.VText;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.user.UserCacheUserGetListener;
import org.easotope.shared.admin.cache.user.user.UserCacheUserSaveListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.admin.cache.user.userlist.UserListItem;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
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

public class UserComposite extends EditorComposite implements UserCacheUserGetListener, UserCacheUserSaveListener {
	private static final String USER_GET = "USER_GET";
	private static final String USER_SAVE = "USER_GET";

 	private Label userId;
	private VText username;
	private Canvas usernameError;
	private VText fullName;
	private VText password;
	private Canvas passwordError;
	private VButton isDisabled;
	private VButton isAdmin;
	private VButton canEditMassSpecs;
	private VButton canEditCorrIntervals;
	private VButton canEditSampleTypes;
	private VButton canEditStandards;
	private VButton canEditConstants;
	private VButton canEditAllInput;
	private VButton canDeleteAll;
	private VButton canDeleteOwn;

	private UserList lastUserList = null;
	private HashSet<String> userNames = new HashSet<String>();
	
	protected UserComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		final Image errorImage = Icons.getError(parent.getDisplay());

		Label userIdLabel = new Label(this, SWT.NONE);
		userIdLabel.setText(Messages.userComposite_userIdLabel);

		userId = new Label(this, SWT.NONE);

		Label usernameLabel = new Label(this, SWT.NONE);
		usernameLabel.setText(Messages.userComposite_usernameLabel);

		Composite usernameComposite = new Composite(this, SWT.NONE);
		usernameComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		usernameComposite.setLayout(gridLayout);

		username = new VText(usernameComposite, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		username.setLayoutData(gridData);
		username.setTextLimit(GuiConstants.SHORT_TEXT_INPUT_LIMIT);
		username.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});
		username.addListener(SWT.Verify, new LoggingAdaptor() {
			public void loggingHandleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!Character.isLetterOrDigit(chars[i]) && chars[i] != '_' && chars[i] != '-') {
						e.doit = false;
						return;
					}
				}
			}
		});

		usernameError = new Canvas(usernameComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		usernameError.setLayoutData(gridData);
		usernameError.setVisible(false);
		usernameError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Label fullNameLabel = new Label(this, SWT.NONE);
		fullNameLabel.setText(Messages.userComposite_fullNameLabel);

		fullName = new VText(this, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		fullName.setLayoutData(gridData);
		fullName.setTextLimit(GuiConstants.LONG_TEXT_INPUT_LIMIT);
		fullName.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		Label passwordLabel = new Label(this, SWT.NONE);
		passwordLabel.setText(Messages.userComposite_passwordLabel);

		Composite passwordComposite = new Composite(this, SWT.NONE);
		passwordComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		passwordComposite.setLayout(gridLayout);

		password = new VText(passwordComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		password.setLayoutData(gridData);
		password.setEchoChar('*');
		password.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		passwordError = new Canvas(passwordComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		passwordError.setLayoutData(gridData);
		passwordError.setVisible(false);
		passwordError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Label isDisabledLabel = new Label(this, SWT.NONE);
		isDisabledLabel.setText(Messages.userComposite_isDisabledLabel);

		isDisabled = new VButton(this, SWT.CHECK);
		isDisabled.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label isAdminLabel = new Label(this, SWT.NONE);
		isAdminLabel.setText(Messages.userComposite_isAdminLabel);

		isAdmin = new VButton(this, SWT.CHECK);
		isAdmin.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label canEditMassSpecsLabel = new Label(this, SWT.NONE);
		canEditMassSpecsLabel.setText(Messages.userComposite_canEditMassSpecsLabel);

		canEditMassSpecs = new VButton(this, SWT.CHECK);
		canEditMassSpecs.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label canEditCorrectionsLabel = new Label(this, SWT.NONE);
		canEditCorrectionsLabel.setText(Messages.userComposite_canEditCorrIntervalsLabel);

		canEditCorrIntervals = new VButton(this, SWT.CHECK);
		canEditCorrIntervals.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label canEditSampleTypesLabel = new Label(this, SWT.NONE);
		canEditSampleTypesLabel.setText(Messages.userComposite_canEditSampleTypesLabel);

		canEditSampleTypes = new VButton(this, SWT.CHECK);
		canEditSampleTypes.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label canEditStandardsLabel = new Label(this, SWT.NONE);
		canEditStandardsLabel.setText(Messages.userComposite_canEditStandardsLabel);

		canEditStandards = new VButton(this, SWT.CHECK);
		canEditStandards.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label canEditConstantsLabel = new Label(this, SWT.NONE);
		canEditConstantsLabel.setText(Messages.userComposite_canEditConstantsLabel);

		canEditConstants = new VButton(this, SWT.CHECK);
		canEditConstants.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label canEditAllInputLabel = new Label(this, SWT.NONE);
		canEditAllInputLabel.setText(Messages.userComposite_canEditAllInputLabel);

		canEditAllInput = new VButton(this, SWT.CHECK);
		canEditAllInput.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label canDeleteAllLabel = new Label(this, SWT.NONE);
		canDeleteAllLabel.setText(Messages.userComposite_canDeleteAllLabel);

		canDeleteAll = new VButton(this, SWT.CHECK);
		canDeleteAll.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		Label canDeleteOwnLabel = new Label(this, SWT.NONE);
		canDeleteOwnLabel.setText(Messages.userComposite_canDeleteOwnLabel);

		canDeleteOwn = new VButton(this, SWT.CHECK);
		canDeleteOwn.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});

		UserCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		UserCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private User getCurrentUser() {
		Object[] currentObject = (Object[]) getCurrentObject();
		return currentObject == null ? null : (User) currentObject[0];
	}

	private Permissions getCurrentPermissions() {
		Object[] currentObject = (Object[]) getCurrentObject();
		return currentObject == null ? null : (Permissions) currentObject[1];
	}

	@Override
	protected void setCurrentFieldValues() {
		User currentUser = getCurrentUser();
		Permissions currentPermissions = getCurrentPermissions();

		userId.setText(String.valueOf(currentUser.getId()));
		username.setText(currentUser.getUsername());
		fullName.setText(currentUser.getFullName());
		password.setText("");
		isDisabled.setSelection(currentUser.getIsDisabled());
		isAdmin.setSelection(currentUser.getIsAdmin());
		canEditMassSpecs.setSelection(currentPermissions.isCanEditMassSpecs());
		canEditCorrIntervals.setSelection(currentPermissions.isCanEditCorrIntervals());
		canEditSampleTypes.setSelection(currentPermissions.isCanEditSampleTypes());
		canEditStandards.setSelection(currentPermissions.isCanEditStandards());
		canEditConstants.setSelection(currentPermissions.isCanEditConstants());
		canEditAllInput.setSelection(currentPermissions.isCanEditAllReplicates());
		canDeleteAll.setSelection(currentPermissions.isCanDeleteAll());
		canDeleteOwn.setSelection(currentPermissions.isCanDeleteOwn());
	}

	@Override
	protected void setDefaultFieldValues() {
		userId.setText(Messages.userComposite_newUserId);
		username.setText("");
		fullName.setText("");
		password.setText("");
		isDisabled.setSelection(false);
		isAdmin.setSelection(false);
		canEditMassSpecs.setSelection(false);
		canEditCorrIntervals.setSelection(false);
		canEditSampleTypes.setSelection(false);
		canEditStandards.setSelection(false);
		canEditConstants.setSelection(false);
		canEditAllInput.setSelection(false);
		canDeleteAll.setSelection(false);
		canDeleteOwn.setSelection(false);
	}

	@Override
	public void enableWidgets() {
		User currentUser = getCurrentUser();
		boolean hasAdminPermissions = LoginInfoCache.getInstance().getUser().getIsAdmin();

		username.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));

		if (!hasAdminPermissions) {
			username.revert();
		}

		fullName.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));

		if (!hasAdminPermissions) {
			fullName.revert();
		}

		password.setEnabled(hasAdminPermissions);

		isDisabled.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));
		
		if (!hasAdminPermissions) {
			isDisabled.revert();
		}

		isAdmin.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));
		
		if (!hasAdminPermissions) {
			isAdmin.revert();
		}

		canEditMassSpecs.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));
		
		if (!hasAdminPermissions) {
			canEditMassSpecs.revert();
		}

		canEditCorrIntervals.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));
		
		if (!hasAdminPermissions) {
			canEditCorrIntervals.revert();
		}

		canEditSampleTypes.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));
		
		if (!hasAdminPermissions) {
			canEditSampleTypes.revert();
		}
		
		canEditStandards.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));
		
		if (!hasAdminPermissions) {
			canEditStandards.revert();
		}

		canEditConstants.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));

		if (!hasAdminPermissions) {
			canEditConstants.revert();
		}

		canEditAllInput.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));
		
		if (!hasAdminPermissions) {
			canEditAllInput.revert();
		}
		
		canDeleteAll.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));
		
		if (!hasAdminPermissions) {
			canDeleteAll.revert();
		}

		canDeleteOwn.setEnabled(hasAdminPermissions && (currentUser == null || currentUser.getId() != 1));

		if (!hasAdminPermissions) {
			canDeleteOwn.revert();
		}
	}

	@Override
	public void disableWidgets() {
		username.setEnabled(false);
		fullName.setEnabled(false);
		password.setEnabled(false);
		isDisabled.setEnabled(false);
		isAdmin.setEnabled(false);
		canEditMassSpecs.setEnabled(false);
		canEditCorrIntervals.setEnabled(false);
		canEditSampleTypes.setEnabled(false);
		canEditStandards.setEnabled(false);
		canEditConstants.setEnabled(false);
		canEditAllInput.setEnabled(false);
		canDeleteAll.setEnabled(false);
		canDeleteOwn.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		if (isAdmin.getSelection()) {
			canEditMassSpecs.setSelectionButLeaveRevertValue(true);
			canEditCorrIntervals.setSelectionButLeaveRevertValue(true);
			canEditSampleTypes.setSelectionButLeaveRevertValue(true);
			canEditStandards.setSelectionButLeaveRevertValue(true);
			canEditConstants.setSelectionButLeaveRevertValue(true);
			canEditAllInput.setSelectionButLeaveRevertValue(true);
			canDeleteAll.setSelectionButLeaveRevertValue(true);
			canDeleteOwn.setSelectionButLeaveRevertValue(true);
		}

		boolean isDirty = false;

		isDirty = isDirty || username.hasChangedIfTrimmed();
		isDirty = isDirty || fullName.hasChangedIfTrimmed();
		isDirty = isDirty || password.hasChanged();
		isDirty = isDirty || isDisabled.hasChanged();
		isDirty = isDirty || isAdmin.hasChanged();
		isDirty = isDirty || canEditMassSpecs.hasChanged();
		isDirty = isDirty || canEditCorrIntervals.hasChanged();
		isDirty = isDirty || canEditSampleTypes.hasChanged();
		isDirty = isDirty || canEditStandards.hasChanged();
		isDirty = isDirty || canEditConstants.hasChanged();
		isDirty = isDirty || canEditAllInput.hasChanged();
		isDirty = isDirty || canDeleteAll.hasChanged();
		isDirty = isDirty || canDeleteOwn.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		boolean usernameErrorSet = false;
		boolean passwordErrorSet = false;

		if (username.getText().trim().isEmpty()) {
			usernameError.setToolTipText(Messages.userComposite_usernameEmpty);
			usernameErrorSet = true;
		}

		if (username.hasChangedIfTrimmed() && userNames.contains(username.getText().trim())) {
			usernameError.setToolTipText(Messages.userComposite_usernameNotUnique);
			usernameErrorSet = true;
		}

		if (usernameErrorSet != usernameError.getVisible()) {
			usernameError.setVisible(usernameErrorSet);
			layoutNeeded();
		}

		if (userId.getText().equals(Messages.userComposite_newUserId) && password.getText().trim().isEmpty()) {
			passwordError.setToolTipText(Messages.userComposite_passwordEmpty);
			passwordErrorSet = true;
		}

		if (passwordErrorSet != passwordError.getVisible()) {
			passwordError.setVisible(passwordErrorSet);
			layoutNeeded();
		}

		return usernameErrorSet || passwordErrorSet;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		UserList currentUserList = (UserList) selection.get(UserPart.SELECTION_USER_LIST);

		if (currentUserList != lastUserList) {
			lastUserList = currentUserList;
			userNames.clear();

			if (lastUserList != null) {
				for (Integer id : lastUserList.keySet()) {
					UserListItem userListItem = lastUserList.get(id);
					userNames.add(userListItem.getName());
				}
			}
		}

		User user = getCurrentUser();
		Integer selectedUserId = (Integer) selection.get(UserPart.SELECTION_USER_ID);

		if (user == null && selectedUserId == null) {
			return false;
		}

		if ((user == null && selectedUserId != null) || (user != null && selectedUserId == null)) {
			return true;
		}

		return user.getId() != selectedUserId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer userId = (Integer) selection.get(UserPart.SELECTION_USER_ID);

		if (userId != null) {
			int commandId = UserCache.getInstance().userGet(userId, this);
			waitingFor(USER_GET, commandId);
			return true;
		}

		return false;
	}

	@Override
	protected void requestSave() {
		User oldUser = getCurrentUser();
		User newUser = new User();

		if (oldUser != null) {
			newUser.setId(oldUser.getId());
		}

		newUser.setUsername(username.getText().trim());
		newUser.setFullName(fullName.getText().trim());

		if (password.getText().isEmpty()) {
			newUser.passwordHash = oldUser.passwordHash;
			newUser.passwordSalt = oldUser.passwordSalt;
		} else {
			newUser.setPassword(password.getText());
		}

		newUser.setIsDisabled(isDisabled.getSelection());
		newUser.setIsAdmin(isAdmin.getSelection());

		Permissions oldPermissions = getCurrentPermissions();
		Permissions newPermissions = new Permissions();

		if (oldPermissions != null) {
			newPermissions.id = oldPermissions.getId();
		}

		if (oldUser != null) {
			newPermissions.setUserId(oldUser.getId());
		}

		newPermissions.setCanEditMassSpecs(canEditMassSpecs.getSelection());
		newPermissions.setCanEditCorrIntervals(canEditCorrIntervals.getSelection());
		newPermissions.setCanEditSampleTypes(canEditSampleTypes.getSelection());
		newPermissions.setCanEditStandards(canEditStandards.getSelection());
		newPermissions.setCanEditConstants(canEditConstants.getSelection());
		newPermissions.setCanEditAllReplicates(canEditAllInput.getSelection());
		newPermissions.setCanDeleteAll(canDeleteAll.getSelection());
		newPermissions.setCanDeleteOwn(canDeleteOwn.getSelection());

		int commandId = UserCache.getInstance().userSave(newUser, newPermissions, null, this);
		waitingFor(USER_SAVE, commandId);
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
	public void userGetCompleted(int commandId, User user, Permissions permissions, Preferences preferences) {
		newObject(USER_GET, new Object[] { user, permissions, preferences });
	}

	@Override
	public void userUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
		updateObject(new Object[] { user, permissions, preferences }, Messages.userAdminPart_userHasBeenUpdated);
	}

	@Override
	public void userGetError(int commandId, String message) {
		raiseGetError(USER_GET, message);		
	}

	@Override
	public void userSaveCompleted(int commandId, User user, Permissions permissions, Preferences preferences) {
		saveComplete(USER_SAVE, new Object[] { user, permissions, preferences });
	}

	@Override
	public void userSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(USER_SAVE, message);
	}
}
