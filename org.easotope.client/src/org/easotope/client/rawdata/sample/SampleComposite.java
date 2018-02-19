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

package org.easotope.client.rawdata.sample;

import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.VSelectList;
import org.easotope.client.core.widgets.VSelectListListener;
import org.easotope.client.core.widgets.VText;
import org.easotope.shared.admin.cache.sampletype.SampleTypeCache;
import org.easotope.shared.admin.cache.sampletype.sampletypelist.SampleTypeCacheSampleTypeListGetListener;
import org.easotope.shared.admin.cache.sampletype.sampletypelist.SampleTypeList;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.userlist.UserCacheUserListGetListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.samanalysislist.AnalysisCacheSamAnalysisListGetListener;
import org.easotope.shared.analysis.cache.analysis.samanalysislist.SamAnalysisList;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.projectlist.InputCacheProjectListGetListener;
import org.easotope.shared.rawdata.cache.input.projectlist.ProjectList;
import org.easotope.shared.rawdata.cache.input.sample.InputCacheSampleGetListener;
import org.easotope.shared.rawdata.cache.input.sample.InputCacheSampleSaveListener;
import org.easotope.shared.rawdata.tables.Sample;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class SampleComposite extends EditorComposite implements UserCacheUserListGetListener, InputCacheProjectListGetListener, InputCacheSampleGetListener, InputCacheSampleSaveListener, SampleTypeCacheSampleTypeListGetListener, AnalysisCacheSamAnalysisListGetListener {
	private final String USERLIST_COMMAND_ID = "USERLIST_COMMAND_ID";
	private final String PROJECTLIST_COMMAND_ID = "PROJECTLIST_COMMAND_ID";
	private final String SAMPLE_TYPE_LIST_COMMAND_ID = "SAMPLE_TYPE_LIST_COMMAND_ID";
	private final String SAMPLE_COMMAND_ID = "SAMPLE_COMMAND_ID";
	private final String SAMPLE_SAVE_COMMAND_ID = "SAMPLE_SAVE_COMMAND_ID";
	private final String SAM_ANALYSIS_LIST_COMMAND_ID = "SAM_ANALYSIS_LIST_COMMAND_ID";
	private final String WAITING_FOR_SAMPLE_DELETE = "WAITING_FOR_SAMPLE_DELETE";

	private Integer currentUserId = null;
	private Integer currentProjectId = null;
	private Integer currentSampleId = null;

	private Label id;
	private SortedCombo user;
	private Canvas userError;
	private SortedCombo project;
	private Canvas projectError;
	private VText name;
	private Canvas nameError;
	private VText description;
	private SortedCombo sampleType;
	private Canvas sampleTypeError;
	private VSelectList samAnalyses;

	SampleComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		final Image errorImage = Icons.getError(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.sampleComposite_idLabel);

		id = new Label(this, SWT.NONE);

		Label massSpecNameLabel = new Label(this, SWT.NONE);
		massSpecNameLabel.setText(Messages.sampleComposite_nameLabel);

		Composite sampleNameComposite = new Composite(this, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 6;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		sampleNameComposite.setLayout(gridLayout);

		user = new SortedCombo(sampleNameComposite, SWT.BORDER | SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		user.setLayoutData(gridData);
		user.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				int commandId = InputCache.getInstance().projectListGet(SampleComposite.this, user.getSelectedInteger());
				waitingFor(PROJECTLIST_COMMAND_ID, commandId);

				widgetStatusChanged();
			}
		});

		userError = new Canvas(sampleNameComposite, SWT.NONE);
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

		project = new SortedCombo(sampleNameComposite, SWT.BORDER | SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		project.setLayoutData(gridData);
		project.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		projectError = new Canvas(sampleNameComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		projectError.setLayoutData(gridData);
		projectError.setVisible(false);
		projectError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		name = new VText(sampleNameComposite, SWT.BORDER);
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

		nameError = new Canvas(sampleNameComposite, SWT.NONE);
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
		descriptionLabel.setText(Messages.sampleComposite_descriptionLabel);

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
		sampleTypeLabel.setText(Messages.sampleComposite_sampleTypeLabel);

		Composite sampleTypeComposite = new Composite(this, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		sampleTypeComposite.setLayout(gridLayout);

		sampleType = new SortedCombo(sampleTypeComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
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

		Label sampleAnalysesLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		sampleAnalysesLabel.setLayoutData(gridData);
		sampleAnalysesLabel.setText(Messages.sampleComposite_sampleAnalysesLabel);

		samAnalyses = new VSelectList(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		samAnalyses.setLayoutData(gridData);
		samAnalyses.setSelectionEnabled(false);
		samAnalyses.addListener(new VSelectListListener() {
			@Override
			public void checkBoxesChanged() {
				widgetStatusChanged();
			}

			@Override
			public void selectionChanged() {

			}
		});

		SampleTypeCache.getInstance().addListener(this);
		UserCache.getInstance().addListener(this);
		InputCache.getInstance().addListener(this);

		SampleTypeCache.getInstance().sampleTypeListGet(this);
		AnalysisCache.getInstance().samAnalysisListGet(this);

		int commandId = UserCache.getInstance().userListGet(this);
		waitingFor(USERLIST_COMMAND_ID, commandId);
	}

	@Override
	public void handleDispose() {
		SampleTypeCache.getInstance().removeListener(this);
		UserCache.getInstance().removeListener(this);
		InputCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private Sample getCurrentSample() {
		return (Sample) getCurrentObject();
	}

	@Override
	protected void setCurrentFieldValues() {
		Sample currentSample = getCurrentSample();

		getChainedPart().getPart().setLabel(Messages.sampleComposite_labelPrefix + currentSample.getName());

		id.setText(String.valueOf(currentSample.getId()));

		if (user.getSelectedInteger() != currentSample.getUserId()) {
			user.selectInteger(currentSample.getUserId());

			int commandId = InputCache.getInstance().projectListGet(this, currentSample.getUserId());
			waitingFor(PROJECTLIST_COMMAND_ID, commandId);
		}

		project.selectInteger(currentSample.getProjectId());
		name.setText(currentSample.getName());
		description.setText(currentSample.getDescription());
		sampleType.selectInteger(currentSample.getSampleTypeId());

		ArrayList<Integer> list = new ArrayList<Integer>();

		if (currentSample.getSamAnalyses() != null) {
			for (int i : currentSample.getSamAnalyses()) {
				list.add(i);
			}
		}

		samAnalyses.setCheckboxSelection(list);
	}

	@Override
	protected void setDefaultFieldValues() {
		getChainedPart().getPart().setLabel(Messages.editor_sampleTab);

		id.setText(Messages.sampleComposite_newId);

		int newUserId = (Integer) getSelection().get(SamplePart.SELECTION_USER_ID);

		if (user.getSelectedInteger() != newUserId) {
			user.selectInteger(newUserId);

			int commandId = InputCache.getInstance().projectListGet(this, newUserId);
			waitingFor(PROJECTLIST_COMMAND_ID, commandId);
		}

		int newProjectId = (Integer) getSelection().get(SamplePart.SELECTION_PROJECT_ID);

		project.selectInteger(newProjectId);
		name.setText("");
		description.setText("");
		sampleType.selectInteger(-1);
		samAnalyses.setCheckboxSelection(new ArrayList<Integer>());

		name.setFocus();
	}

	@Override
	public void enableWidgets() {
		Sample currentSample = getCurrentSample();

		boolean hasPermission = (currentSample == null) || currentSample.getUserId() == LoginInfoCache.getInstance().getUser().getId();
		hasPermission = hasPermission || LoginInfoCache.getInstance().getPermissions().isCanEditAllReplicates();

		user.setEnabled(hasPermission);

		if (!hasPermission) {
			user.revert();
		}

		project.setEnabled(hasPermission);

		if (!hasPermission) {
			project.revert();
		}

		name.setEnabled(hasPermission);

		if (!hasPermission) {
			name.revert();
		}

		description.setEnabled(hasPermission);

		if (!hasPermission) {
			description.revert();
		}

		sampleType.setEnabled(hasPermission && getCurrentSample() == null);

		if (!hasPermission) {
			sampleType.revert();
		}

		samAnalyses.setEnabled(hasPermission);

		if (!hasPermission) {
			samAnalyses.revert();
		}
	}

	@Override
	public void disableWidgets() {
		user.setEnabled(false);
		project.setEnabled(false);
		name.setEnabled(false);
		description.setEnabled(false);
		sampleType.setEnabled(false);
		samAnalyses.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean fieldsContainNewData = false;

		fieldsContainNewData = fieldsContainNewData || user.hasChanged();
		fieldsContainNewData = fieldsContainNewData || project.hasChanged();
		fieldsContainNewData = fieldsContainNewData || name.hasChangedIfTrimmed();
		fieldsContainNewData = fieldsContainNewData || description.hasChanged();
		fieldsContainNewData = fieldsContainNewData || sampleType.hasChanged();
		fieldsContainNewData = fieldsContainNewData || samAnalyses.hasChanged();

		return fieldsContainNewData;
	}

	@Override
	protected boolean hasError() {
		boolean userErrorIsSet = false;
		boolean projectErrorIsSet = false;
		boolean nameErrorIsSet = false;

		if (user.getSelectionIndex() == -1) {
			userError.setToolTipText(Messages.sampleComposite_userEmpty);

			if (!userError.getVisible()) {
				layoutNeeded();
			}

			userErrorIsSet = true;
		}

		if (project.getSelectionIndex() == -1) {
			projectError.setToolTipText(Messages.sampleComposite_projectEmpty);

			if (!projectError.getVisible()) {
				layoutNeeded();
			}

			projectErrorIsSet = true;
		}

		if (name.getText().trim().isEmpty()) {
			nameError.setToolTipText(Messages.sampleComposite_nameEmpty);

			if (!nameError.getVisible()) {
				layoutNeeded();
			}

			nameErrorIsSet = true;
		}

		boolean sampleTypeIsSet = false;
		
		if (sampleType.getSelectedInteger() == -1) {
			sampleTypeError.setToolTipText(Messages.sampleComposite_sampleTypeEmpty);

			if (!sampleTypeError.getVisible()) {
				layoutNeeded();
			}

			sampleTypeIsSet = true;
		}

		userError.setVisible(userErrorIsSet);
		projectError.setVisible(projectErrorIsSet);
		nameError.setVisible(nameErrorIsSet);
		sampleTypeError.setVisible(sampleTypeIsSet);

		return userErrorIsSet || projectErrorIsSet || nameErrorIsSet || sampleTypeIsSet;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String, Object> selection) {
		Integer userId = (Integer) getSelection().get(SamplePart.SELECTION_USER_ID);
		Integer projectId = (Integer) getSelection().get(SamplePart.SELECTION_PROJECT_ID);
		Integer sampleId = (Integer) getSelection().get(SamplePart.SELECTION_SAMPLE_ID);

		return userId != currentUserId || projectId != currentProjectId || sampleId != currentSampleId;
	}

	@Override
	protected boolean processSelection(HashMap<String, Object> selection) {
		Integer sampleId = (Integer) getSelection().get(SamplePart.SELECTION_SAMPLE_ID);

		if (sampleId != null) {
			int commandId = InputCache.getInstance().sampleGet(this, sampleId);
			waitingFor(SAMPLE_COMMAND_ID, commandId);
		}

		currentUserId = (Integer) getSelection().get(SamplePart.SELECTION_USER_ID);
		currentProjectId = (Integer) getSelection().get(SamplePart.SELECTION_PROJECT_ID);
		currentSampleId = sampleId;

		return true;
	}

	@Override
	protected void requestSave(boolean isResend) {
		Sample sample = new Sample();
		Sample currentSample = getCurrentSample();

		if (currentSample != null) {
			sample.setId(currentSample.getId());
		}

		sample.setUserId(user.getSelectedInteger());
		sample.setProjectId(project.getSelectedInteger());
		sample.setName(name.getText().trim());
		sample.setDescription(description.getText());
		sample.setSampleTypeId(sampleType.getSelectedInteger());

		int count = 0;
		int[] array = new int[samAnalyses.getCheckboxSelection().size()];

		for (Integer i : samAnalyses.getCheckboxSelection()) {
			array[count++] = i;
		}

		sample.setSamAnalyses(array);

		int commandId = InputCache.getInstance().sampleSave(this, sample);
		waitingFor(SAMPLE_SAVE_COMMAND_ID, commandId);
	}

	@Override
	protected boolean canDelete() {
		return false;

//		if (getChainedPart().canRevert()) {
//			return false;
//		}
//
//		Sample currentSample = getCurrentSample();
//
//		if (currentSample != null) {
//			if (LoginInfoCache.getInstance().getPermissions().isCanDeleteAll()) {
//				return true;
//			}
//
//			int currentUserId = LoginInfoCache.getInstance().getUser().getId();
//			boolean canDeleteOwn = LoginInfoCache.getInstance().getPermissions().isCanDeleteOwn();
//
//			if (canDeleteOwn && currentSample.getUserId() == currentUserId) {
//				return true;
//			}
//		}
//
//		return false;
	}

	@Override
	protected boolean requestDelete() {
		if (getChainedPart().raiseQuestion(Messages.sampleComposite_reallyDelete)) {
			int commandId = InputCache.getInstance().sampleDelete(getCurrentSample().getId(), this);
			waitingFor(WAITING_FOR_SAMPLE_DELETE, commandId);
			return true;
		}

		return false;
	}

	@Override
	public void sampleTypeListGetCompleted(int commandId, final SampleTypeList sampleTypeList) {
		sampleType.setPossibilities(sampleTypeList);
		doneWaitingFor(SAMPLE_TYPE_LIST_COMMAND_ID);
	}

	@Override
	public void sampleTypeListUpdated(int commandId, final SampleTypeList sampleTypeList) {
		sampleType.setPossibilities(sampleTypeList);
		doneWaitingFor(SAMPLE_TYPE_LIST_COMMAND_ID);
	}

	@Override
	public void sampleTypeListGetError(int commandId, final String message) {
		if (commandIdForKey(SAMPLE_TYPE_LIST_COMMAND_ID) == commandId) {
			raiseGetError(SAMPLE_TYPE_LIST_COMMAND_ID, message);
		}
	}

	@Override
	public void sampleGetCompleted(int commandId, Sample sample) {
		if (commandIdForKey(SAMPLE_COMMAND_ID) == commandId) {
			newObject(SAMPLE_COMMAND_ID, sample);
		}
	}

	@Override
	public void sampleUpdated(int commandId, Sample sample) {
		if (getCurrentSample() != null && getCurrentSample().getId() == sample.getId()) {
			updateObject(sample, Messages.sampleComposite_sampleHasBeenUpdated);
		}
	}

	@Override
	public void sampleDeleted(int sampleId) {
		if (getCurrentSample() != null && getCurrentSample().getId() == sampleId) {
			getChainedPart().raiseInfo(Messages.sampleComposite_sampleDeleted);
			getChainedPart().closePart();
		}
	}

	@Override
	public void sampleGetError(int commandId, String message) {
		if (commandIdForKey(SAMPLE_COMMAND_ID) == commandId) {
			raiseGetError(SAMPLE_COMMAND_ID, message);
		}
	}
	
	@Override
	public void sampleSaveCompleted(int commandId, Sample sample) {
		saveComplete(SAMPLE_SAVE_COMMAND_ID, sample);
	}

	@Override
	public void sampleSaveError(int commandId, String message) {
		if (commandIdForKey(SAMPLE_SAVE_COMMAND_ID) == commandId) {
			raiseGetError(SAMPLE_SAVE_COMMAND_ID, message);
		}
	}

	@Override
	public void sampleDeleteCompleted(int commandId) {
		if (commandIdForKey(WAITING_FOR_SAMPLE_DELETE) == commandId) {
			deleteComplete(WAITING_FOR_SAMPLE_DELETE);
		}
	}

	@Override
	public void sampleDeleteError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_SAMPLE_DELETE) == commandId) {
			raiseSaveOrDeleteError(WAITING_FOR_SAMPLE_DELETE, message);
		}
	}

	@Override
	public void samAnalysisListGetCompleted(int commandId, SamAnalysisList samAnalysisList) {
		HashMap<String,Integer> possibilities = new HashMap<String,Integer>();

		for (int key : samAnalysisList.keySet()) {
			String value = samAnalysisList.get(key).getName();
			possibilities.put(value, key);
		}

		samAnalyses.setPossibilities(possibilities);
		doneWaitingFor(SAM_ANALYSIS_LIST_COMMAND_ID);
	}

	@Override
	public void samAnalysisListGetError(int commandId, String message) {
		if (commandIdForKey(SAM_ANALYSIS_LIST_COMMAND_ID) == commandId) {
			raiseGetError(SAM_ANALYSIS_LIST_COMMAND_ID, message);
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

	@Override
	public void projectListGetCompleted(int commandId, ProjectList projectList) {
		if (projectList.getUserId() == user.getSelectedInteger()) {
			project.setPossibilities(projectList);
			doneWaitingFor(PROJECTLIST_COMMAND_ID);

			if (projectList.size() == 1) {
				project.selectInteger(projectList.keySet().toArray(new Integer[1])[0]);
			}

			widgetStatusChanged();
		}
	}

	@Override
	public void projectListUpdated(int commandId, ProjectList projectList) {
		if (projectList.getUserId() == user.getSelectedInteger()) {
			project.setPossibilities(projectList);
			doneWaitingFor(PROJECTLIST_COMMAND_ID);
			
			if (projectList.size() == 1) {
				project.selectInteger(projectList.keySet().toArray(new Integer[1])[0]);
			}

			widgetStatusChanged();
		}
	}

	@Override
	public void projectListGetError(int commandId, String message) {
		raiseGetError(PROJECTLIST_COMMAND_ID, message);
	}
}
