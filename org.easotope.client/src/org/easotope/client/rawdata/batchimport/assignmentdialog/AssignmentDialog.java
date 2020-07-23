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

package org.easotope.client.rawdata.batchimport.assignmentdialog;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standardlist.StandardCacheStandardListGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardList;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.userlist.UserCacheUserListGetListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.projectlist.InputCacheProjectListGetListener;
import org.easotope.shared.rawdata.cache.input.projectlist.ProjectList;
import org.easotope.shared.rawdata.cache.input.samplelist.InputCacheSampleListGetListener;
import org.easotope.shared.rawdata.cache.input.samplelist.SampleList;
import org.easotope.shared.rawdata.cache.sourcelist.sourcelist.SourceListItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class AssignmentDialog extends Dialog implements UserCacheUserListGetListener, InputCacheProjectListGetListener, InputCacheSampleListGetListener, StandardCacheStandardListGetListener {
	private Shell shell;

	private Button sampleSelection;
	private Button standardSelection;
	private Composite inputComposite;

	private Composite sampleComposite;
	private SortedCombo user;
	private int lastSelectedUserId;
	private SortedCombo project;
	private int lastSelectedProjectId;
	private SortedCombo sample;

	private Composite standardComposite;
	private SortedCombo standard;

	private SourceListItem sourceListItem;
	private Button submit;

	public AssignmentDialog(Shell shell, int style) {
		super(shell, style);
		setText(Messages.assignmentDialog_title);
	}

	public AssignmentDialog(Shell shell) {
		this(shell, SWT.NONE);
	}

	public void open(SourceListItem sourceListItem) {
		Display display = getDisplay();

		shell = new Shell(display, SWT.TITLE | SWT.BORDER | SWT.ON_TOP | SWT.APPLICATION_MODAL | SWT.CLOSE);
		shell.setLayout(new FillLayout());
		shell.setText(Messages.assignmentDialog_title);

		Composite composite = new Composite(shell, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		composite.setLayout(formLayout);

		sampleSelection = new Button(composite, SWT.RADIO);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		sampleSelection.setLayoutData(formData);
		sampleSelection.setText(Messages.assignmentDialog_sample);
		sampleSelection.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				((StackLayout) inputComposite.getLayout()).topControl = sampleComposite;
				inputComposite.layout();
			}
		});

		standardSelection = new Button(composite, SWT.RADIO);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(sampleSelection);
		standardSelection.setLayoutData(formData);
		standardSelection.setText(Messages.assignmentDialog_standard);
		standardSelection.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				((StackLayout) inputComposite.getLayout()).topControl = standardComposite;
				inputComposite.layout();
			}
		});

		submit = new Button(composite, SWT.PUSH);
		formData = new FormData();
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		submit.setLayoutData(formData);
		submit.setText(Messages.assignmentDialog_select);
		submit.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (sampleSelection.getSelection()) {
					int userId = user.getSelectedInteger();
					String userName = user.getItem(user.getSelectionIndex());
					int projectId = project.getSelectedInteger();
					String projectName = project.getItem(project.getSelectionIndex());
					int sampleId = sample.getSelectedInteger();
					String sampleName = sample.getItem(sample.getSelectionIndex());

					AssignmentDialog.this.sourceListItem = new SourceListItem(userId, userName, projectId, projectName, sampleId, sampleName);

				} else {
					int standardId = standard.getSelectedInteger();
					String standardName = standard.getItem(standard.getSelectionIndex());

					AssignmentDialog.this.sourceListItem = new SourceListItem(standardId, standardName);
				}
			}
		});

		inputComposite = new Composite(composite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(sampleSelection, GuiConstants.INTER_WIDGET_GAP);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(submit, -GuiConstants.INTER_WIDGET_GAP);
		inputComposite.setLayoutData(formData);
		inputComposite.setLayout(new StackLayout());

		sampleComposite = new Composite(inputComposite, SWT.NONE);
		sampleComposite.setLayout(new FormLayout());

		user = new SortedCombo(sampleComposite, SWT.READ_ONLY);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		user.setLayoutData(formData);
		user.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (user.getSelectedInteger() != lastSelectedUserId) {
					project.setPossibilities(new HashMap<Integer,String>());
					project.selectInteger(DatabaseConstants.EMPTY_DB_ID);
					lastSelectedProjectId = DatabaseConstants.EMPTY_DB_ID;

					sample.setPossibilities(new HashMap<Integer,String>());
					sample.selectInteger(DatabaseConstants.EMPTY_DB_ID);

					InputCache.getInstance().projectListGet(AssignmentDialog.this, user.getSelectedInteger());
				}
			}
		});

		project = new SortedCombo(sampleComposite, SWT.READ_ONLY);
		formData = new FormData();
		formData.top = new FormAttachment(user);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		project.setLayoutData(formData);
		project.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				if (project.getSelectedInteger() != lastSelectedProjectId) {
					sample.setPossibilities(new HashMap<Integer,String>());
					sample.selectInteger(DatabaseConstants.EMPTY_DB_ID);
					InputCache.getInstance().sampleListGet(AssignmentDialog.this, project.getSelectedInteger());
				}
			}
		});

		sample = new SortedCombo(sampleComposite, SWT.READ_ONLY);
		formData = new FormData();
		formData.top = new FormAttachment(project);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		sample.setLayoutData(formData);

		standardComposite = new Composite(inputComposite, SWT.NONE);
		standardComposite.setLayout(new FormLayout());

		standard = new SortedCombo(standardComposite, SWT.READ_ONLY);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		standard.setLayoutData(formData);

		if (sourceListItem.getSampleId() != DatabaseConstants.EMPTY_DB_ID) {
			sampleSelection.setSelection(true);

			((StackLayout) inputComposite.getLayout()).topControl = sampleComposite;
			inputComposite.layout();

			HashMap<Integer,String> userPossibilities = new HashMap<Integer,String>();
			userPossibilities.put(sourceListItem.getUserId(), sourceListItem.getUserName());
			user.setPossibilities(userPossibilities);
			user.selectInteger(sourceListItem.getUserId());
			lastSelectedUserId = sourceListItem.getUserId();

			HashMap<Integer,String> projectPossibilities = new HashMap<Integer,String>();
			projectPossibilities.put(sourceListItem.getProjectId(), sourceListItem.getProjectName());
			project.setPossibilities(projectPossibilities);
			project.selectInteger(sourceListItem.getProjectId());
			lastSelectedProjectId = sourceListItem.getProjectId();
			InputCache.getInstance().projectListGet(this, sourceListItem.getUserId());

			HashMap<Integer,String> samplePossibilities = new HashMap<Integer,String>();
			samplePossibilities.put(sourceListItem.getSampleId(), sourceListItem.getSourceName());
			sample.setPossibilities(samplePossibilities);
			sample.selectInteger(sourceListItem.getSampleId());
			InputCache.getInstance().sampleListGet(this, sourceListItem.getProjectId());

		} else {
			standardSelection.setSelection(true);

			HashMap<Integer,String> standardPossibilities = new HashMap<Integer,String>();
			standardPossibilities.put(sourceListItem.getStandardId(), sourceListItem.getSourceName());
			standard.setPossibilities(standardPossibilities);
			standard.selectInteger(sourceListItem.getStandardId());

			((StackLayout) inputComposite.getLayout()).topControl = standardComposite;
			inputComposite.layout();
		}

		UserCache.getInstance().userListGet(this);
		StandardCache.getInstance().standardListGet(this);

		shell.setSize(shell.computeSize(500, SWT.DEFAULT));
		Rectangle bounds = display.getBounds();
		Point size = shell.getSize();
		shell.setLocation((bounds.width - size.x) / 2, (bounds.height - size.y) / 2);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	@Override
	public Display getDisplay() {
		return getParent().getDisplay();
	}

	@Override
	public boolean stillCallabled() {
		return !shell.isDisposed();
	}

	public SourceListItem getSourceListItem() {
		return sourceListItem;
	}

	public void newSampleList(SampleList sampleList) {
		if (project.getSelectedInteger() == sampleList.getProjectId()) {
			sample.setPossibilities(sampleList);

			if (sample.getSelectedInteger() == DatabaseConstants.EMPTY_DB_ID && sampleList.size() == 1) {
				int sampleId = sampleList.keySet().iterator().next();
				sample.selectInteger(sampleId);
			}
		}
	}

	@Override
	public void sampleListGetCompleted(int commandId, SampleList sampleList) {
		newSampleList(sampleList);
	}

	@Override
	public void sampleListUpdated(int commandId, SampleList sampleList) {
		newSampleList(sampleList);
	}

	@Override
	public void sampleListGetError(int commandId, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sampleListDeleted(int projectId) {
		// TODO Auto-generated method stub
		
	}

	public void newProjectList(ProjectList projectList) {
		if (user.getSelectedInteger() == projectList.getUserId()) {
			project.setPossibilities(projectList);

			if (project.getSelectedInteger() == DatabaseConstants.EMPTY_DB_ID && projectList.size() == 1) {
				int projectId = projectList.keySet().iterator().next();
				project.selectInteger(projectId);
			}
		}
	}

	@Override
	public void projectListGetCompleted(int commandId, ProjectList projectList) {
		newProjectList(projectList);
	}

	@Override
	public void projectListUpdated(int commandId, ProjectList projectList) {
		newProjectList(projectList);
	}

	@Override
	public void projectListGetError(int commandId, String message) {

	}

	@Override
	public void userListGetCompleted(int commandId, UserList userList) {
		user.setPossibilities(userList);
	}

	@Override
	public void userListUpdated(int commandId, UserList userList) {
		user.setPossibilities(userList);
	}

	@Override
	public void userListGetError(int commandId, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void standardListGetCompleted(int commandId, StandardList standardList) {
		standard.setPossibilities(standardList);
	}

	@Override
	public void standardListUpdated(int commandId, StandardList standardList) {
		standard.setPossibilities(standardList);
	}

	@Override
	public void standardListGetError(int commandId, String message) {
		// TODO Auto-generated method stub
		
	}
}
