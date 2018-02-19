/*
 * Copyright © 2016-2018 by Devon Bowen.
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

package org.easotope.client.rawdata.project;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.VText;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.userlist.UserCacheUserListGetListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.project.InputCacheProjectGetListener;
import org.easotope.shared.rawdata.cache.input.project.InputCacheProjectSaveListener;
import org.easotope.shared.rawdata.tables.Project;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class ProjectComposite extends EditorComposite implements UserCacheUserListGetListener, InputCacheProjectGetListener, InputCacheProjectSaveListener {
	private final String USERLIST_COMMAND_ID = "USERLIST_COMMAND_ID";
	private final String PROJECT_COMMAND_ID = "PROJECT_COMMAND_ID";
	private final String PROJECT_SAVE_COMMAND_ID = "PROJECT_SAVE_COMMAND_ID";
	private final String WAITING_FOR_PROJECT_DELETE = "WAITING_FOR_PROJECT_DELETE";

	private Integer currentUserId = null;
	private Integer currentProjectId = null;

	private Label id;
	private SortedCombo user;
	private Canvas userError;
	private VText name;
	private Canvas nameError;
	private VText description;

	ProjectComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		final Image errorImage = Icons.getError(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.projectComposite_idLabel);

		id = new Label(this, SWT.NONE);

		Label nameLabel = new Label(this, SWT.NONE);
		nameLabel.setText(Messages.projectComposite_nameLabel);

		Composite projectNameComposite = new Composite(this, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		projectNameComposite.setLayout(gridLayout);

		user = new SortedCombo(projectNameComposite, SWT.BORDER | SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		user.setLayoutData(gridData);
		user.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		userError = new Canvas(projectNameComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		userError.setLayoutData(gridData);
		userError.setVisible(false);
		userError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		name = new VText(projectNameComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		name.setLayoutData(gridData);
		name.setTextLimit(GuiConstants.LONG_TEXT_INPUT_LIMIT);
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

		nameError = new Canvas(projectNameComposite, SWT.NONE);
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
		descriptionLabel.setText(Messages.projectComposite_descriptionLabel);

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

		UserCache.getInstance().addListener(this);
		
		int commandId = UserCache.getInstance().userListGet(this);
		waitingFor(USERLIST_COMMAND_ID, commandId);
	}

	@Override
	public void handleDispose() {
		UserCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private Project getCurrentProject() {
		return (Project) getCurrentObject();
	}

	@Override
	protected void setCurrentFieldValues() {
		Project currentProject = getCurrentProject();

		getChainedPart().getPart().setLabel(Messages.projectComposite_labelPrefix + currentProject.getName());

		id.setText(String.valueOf(currentProject.getId()));
		user.selectInteger(currentProject.getUserId());
		name.setText(currentProject.getName());
		description.setText(currentProject.getDescription());
	}

	@Override
	protected void setDefaultFieldValues() {
		getChainedPart().getPart().setLabel(Messages.editor_projectTab);

		id.setText(Messages.projectComposite_newId);
		user.selectInteger((Integer) getSelection().get(ProjectPart.SELECTION_USER_ID));
		name.setText("");
		description.setText("");

		name.setFocus();
	}

	@Override
	public void enableWidgets() {
		Project currentProject = getCurrentProject();

		boolean hasPermission = (currentProject == null) || currentProject.getUserId() == LoginInfoCache.getInstance().getUser().getId();
		hasPermission = hasPermission || LoginInfoCache.getInstance().getPermissions().isCanEditAllReplicates();

		user.setEnabled(hasPermission);

		if (!hasPermission) {
			user.revert();
		}

		name.setEnabled(hasPermission);

		if (!hasPermission) {
			name.revert();
		}

		description.setEnabled(hasPermission);

		if (!hasPermission) {
			description.revert();
		}
	}

	@Override
	public void disableWidgets() {
		user.setEnabled(false);
		name.setEnabled(false);
		description.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean fieldsContainNewData = false;

		fieldsContainNewData = fieldsContainNewData || user.hasChanged();
		fieldsContainNewData = fieldsContainNewData || name.hasChangedIfTrimmed();
		fieldsContainNewData = fieldsContainNewData || description.hasChanged();

		return fieldsContainNewData;
	}

	@Override
	protected boolean hasError() {
		boolean nameErrorIsSet = false;

		if (name.getText().trim().isEmpty()) {
			nameError.setToolTipText(Messages.projectComposite_nameEmpty);

			if (!nameError.getVisible()) {
				layoutNeeded();
			}

			nameErrorIsSet = true;
		}

		nameError.setVisible(nameErrorIsSet);

		return nameErrorIsSet;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		Integer userId = (Integer) getSelection().get(ProjectPart.SELECTION_USER_ID);
		Integer projectId = (Integer) getSelection().get(ProjectPart.SELECTION_PROJECT_ID);

		return userId != currentUserId || projectId != currentProjectId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer projectId = (Integer) getSelection().get(ProjectPart.SELECTION_PROJECT_ID);

		if (projectId != null) {
			int commandId = InputCache.getInstance().projectGet(this, projectId);
			waitingFor(PROJECT_COMMAND_ID, commandId);
		}

		currentUserId = (Integer) getSelection().get(ProjectPart.SELECTION_USER_ID);
		currentProjectId = projectId;

		return true;
	}

	@Override
	protected void requestSave(boolean isResend) {
		Project project = new Project();
		Project currentProject = getCurrentProject();

		if (currentProject != null) {
			project.setId(currentProject.getId());
		}

		project.setUserId(user.getSelectedInteger());
		project.setName(name.getText().trim());
		project.setDescription(description.getText());

		int commandId = InputCache.getInstance().projectSave(this, project);
		waitingFor(PROJECT_SAVE_COMMAND_ID, commandId);
	}

	@Override
	protected boolean canDelete() {
		return false;

//		if (getChainedPart().canRevert()) {
//			return false;
//		}
//
//		Project currentProject = getCurrentProject();
//
//		if (currentProject != null) {
//			if (LoginInfoCache.getInstance().getPermissions().isCanDeleteAll()) {
//				return true;
//			}
//
//			int currentUserId = LoginInfoCache.getInstance().getUser().getId();
//			boolean canDeleteOwn = LoginInfoCache.getInstance().getPermissions().isCanDeleteOwn();
//
//			if (canDeleteOwn && currentProject.getUserId() == currentUserId) {
//				return true;
//			}
//		}
//
//		return false;
	}

	@Override
	protected boolean requestDelete() {
		if (getChainedPart().raiseQuestion(Messages.projectEditor_reallyDelete)) {
			int commandId = InputCache.getInstance().projectDelete(getCurrentProject().getId(), this);
			waitingFor(WAITING_FOR_PROJECT_DELETE, commandId);
			return true;
		}

		return false;
	}

	@Override
	public void projectGetCompleted(int commandId, Project project) {
		if (commandIdForKey(PROJECT_COMMAND_ID) == commandId) {
			newObject(PROJECT_COMMAND_ID, project);
		}
	}

	@Override
	public void projectUpdated(int commandId, Project project) {
		if (getCurrentProject() != null && getCurrentProject().getId() == project.getId()) {
			updateObject(project, Messages.projectEditor_projectHasBeenUpdated);
		}
	}

	@Override
	public void projectDeleted(int projectId) {
		if (getCurrentProject() != null && getCurrentProject().getId() == projectId) {
			getChainedPart().raiseInfo(Messages.projectEditor_projectDeleted);
			getChainedPart().closePart();
		}
	}

	@Override
	public void projectGetError(int commandId, String message) {
		if (commandIdForKey(PROJECT_COMMAND_ID) == commandId) {
			raiseGetError(PROJECT_COMMAND_ID, message);
		}
	}

	@Override
	public void projectSaveCompleted(int commandId, Project project) {
		saveComplete(PROJECT_SAVE_COMMAND_ID, project);
	}

	@Override
	public void projectSaveError(int commandId, String message) {
		if (commandIdForKey(PROJECT_SAVE_COMMAND_ID) == commandId) {
			raiseGetError(PROJECT_SAVE_COMMAND_ID, message);
		}
	}

	@Override
	public void projectDeleteCompleted(int commandId) {
		if (commandIdForKey(WAITING_FOR_PROJECT_DELETE) == commandId) {
			deleteComplete(WAITING_FOR_PROJECT_DELETE);
		}
	}

	@Override
	public void projectDeleteError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_PROJECT_DELETE) == commandId) {
			raiseSaveOrDeleteError(WAITING_FOR_PROJECT_DELETE, message);
		}
	}

	@Override
	public void userListGetCompleted(int commandId, UserList userList) {
		user.setPossibilities(userList);
		doneWaitingFor(USERLIST_COMMAND_ID);
	}

	@Override
	public void userListUpdated(int commandId, UserList userList) {
		user.setPossibilities(userList);
		doneWaitingFor(USERLIST_COMMAND_ID);
	}

	@Override
	public void userListGetError(int commandId, String message) {
		raiseGetError(USERLIST_COMMAND_ID, message);
	}
}
