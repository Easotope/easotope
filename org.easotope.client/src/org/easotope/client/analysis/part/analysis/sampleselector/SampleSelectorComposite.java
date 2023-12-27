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

package org.easotope.client.analysis.part.analysis.sampleselector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.userlist.UserCacheUserListGetListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.admin.cache.user.userlist.UserListItem;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.projectlist.InputCacheProjectListGetListener;
import org.easotope.shared.rawdata.cache.input.projectlist.ProjectList;
import org.easotope.shared.rawdata.cache.input.projectlist.ProjectListItem;
import org.easotope.shared.rawdata.cache.input.samplelist.InputCacheSampleListGetListener;
import org.easotope.shared.rawdata.cache.input.samplelist.SampleList;
import org.easotope.shared.rawdata.cache.input.samplelist.SampleListItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;


public class SampleSelectorComposite extends ChainedComposite implements UserCacheUserListGetListener, InputCacheProjectListGetListener, InputCacheSampleListGetListener, LoginInfoCacheLoginInfoGetListener {
	private TreeViewer treeViewer;

	private static final String ROOT = "root";
	private static final String USER_PREFIX = "u";
	private static final String PROJECT_PREFIX = "p";
	private static final String SAMPLE_PREFIX = "s";
	private HashMap<String,TreeElement> treeElementLookup = new HashMap<String,TreeElement>();
	private HashMap<Integer,String> waitingOnCommandIds = new HashMap<Integer,String>();
	private HashSet<String> waitingOnTreeElements = new HashSet<String>();

	public SampleSelectorComposite(ChainedPart chainedPart, Composite parent, int style, final boolean singleSamples, final String selectionKey) {
		super(chainedPart, parent, style);

		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Label label = new Label(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		label.setText(Messages.sampleSelectorComposite_title);

		Composite composite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		composite.setLayoutData(formData);
		composite.setLayout(new FillLayout());

		int flags = SWT.VIRTUAL | SWT.BORDER;

		if (!singleSamples) {
			flags = flags | SWT.MULTI;
		}

		treeViewer = new TreeViewer(composite, flags);
		treeViewer.setLabelProvider(new MyLabelProvider(ColorCache.getColor(getDisplay(), ColorCache.BLACK), ColorCache.getColor(getDisplay(), ColorCache.GREY), ColorCache.getColor(getDisplay(), ColorCache.WHITE)));
		treeViewer.setContentProvider(new ContentProvider(this));
		treeViewer.setUseHashlookup(true);
		treeViewer.setComparator(new ViewerComparator());
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();

				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					Iterator<?> iterator = structuredSelection.iterator();

					UserProjSampleSelection newSelection = new UserProjSampleSelection();

					while (iterator.hasNext()) {
						Object selectionObj = iterator.next();

						if (selectionObj instanceof UserElement) {
							TreeElement treeElement = (TreeElement) selectionObj;
							newSelection.addUser(treeElement.getId());

						} else if (selectionObj instanceof ProjectElement) {
							TreeElement treeElement = (TreeElement) selectionObj;
							newSelection.addProject(treeElement.getId());

						} else if (selectionObj instanceof SampleElement) {
							TreeElement treeElement = (TreeElement) selectionObj;
							newSelection.addSample(treeElement.getId());
						}
					}

					boolean hasNonSamples = !newSelection.getUserIds().isEmpty() || !newSelection.getProjectIds().isEmpty();

					if (singleSamples && hasNonSamples) {
						treeViewer.setSelection(null);
						propogateSelection(selectionKey, null);
					} else {
						propogateSelection(selectionKey, newSelection);
					}
				}
			}
		});

		TreeElement root = new TreeElement(0, null);
		root.setNameDateAndHasChildren("root", 0, true);
		treeElementLookup.put(ROOT, root);
		treeViewer.setInput(root);

	    UserCache.getInstance().addListener(this);
	    InputCache.getInstance().addListener(this);
	    LoginInfoCache.getInstance().addListener(this);

		int commandId = UserCache.getInstance().userListGet(this);
	    addWaitingReminder(commandId, ROOT);
	}

	private void addWaitingReminder(int commandId, String treeElement) {
		if (commandId == Command.UNDEFINED_ID) {
			return;
		}

		waitingOnCommandIds.put(commandId, treeElement);
		waitingOnTreeElements.add(treeElement);

		getParent().setCursor(getParent().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
	}

	private void removeWaitingReminder(int commandId) {
		if (commandId == Command.UNDEFINED_ID) {
			return;
		}

		waitingOnTreeElements.remove(waitingOnCommandIds.get(commandId));
		waitingOnCommandIds.remove(commandId);

		if (waitingOnCommandIds.size() == 0) {
			getParent().setCursor(null);
		}
	}

	void getProjects(int userId) {
		int commandId = InputCache.getInstance().projectListGet(this, userId);
	    addWaitingReminder(commandId, USER_PREFIX + userId);
	}

	void getSamples(int projectId) {
		int commandId = InputCache.getInstance().sampleListGet(this, projectId);
	    addWaitingReminder(commandId, PROJECT_PREFIX + projectId);
	}

	@Override
	public boolean isWaiting() {
		return !waitingOnCommandIds.isEmpty();
	}

	@Override
	protected void setWidgetsEnabled() {
		((MyLabelProvider) treeViewer.getLabelProvider()).setEnabled(true);
		treeViewer.refresh();
		this.setEnabled(true);
	}

	@Override
	protected void setWidgetsDisabled() {
		((MyLabelProvider) treeViewer.getLabelProvider()).setEnabled(false);
		treeViewer.refresh();
		this.setEnabled(false);
	}

	@Override
	protected void receiveAddRequest() {
		// ignore
	}

	@Override
	protected void cancelAddRequest() {
		// ignore
	}

	@Override
	protected void receiveSelection() {
		// ignore
	}

	@Override
	public Display getDisplay() {
		return super.getDisplay();
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	@Override
	public void userListGetCompleted(int commandId, UserList userList) {
		TreeElement root = treeElementLookup.get(ROOT);
		ArrayList<TreeElement> children = new ArrayList<TreeElement>();

		boolean canEditAllReplicates = LoginInfoCache.getInstance().getPermissions().isCanEditAllReplicates();
		int currentUserId = LoginInfoCache.getInstance().getUser().getId();

		for (Integer userId : userList.keySet()) {
			if (!canEditAllReplicates && userId != currentUserId) {
				continue;
			}

			UserListItem userListItem = userList.get(userId);
			TreeElement userElement = treeElementLookup.get(USER_PREFIX + userId);

			if (userElement == null) {
				userElement = new UserElement(userId, root);
				treeElementLookup.put(USER_PREFIX + userId, userElement);
			}

			userElement.setNameDateAndHasChildren(userListItem.getName(), 0, userListItem.hasChildren());
			children.add(userElement);
		}

		root.setChildren(children);

		treeViewer.refresh(root, true);
		removeWaitingReminder(commandId);
	}

	@Override
	public void userListUpdated(int commandId, UserList userList) {
		if (waitingOnTreeElements.contains(ROOT)) {
			return;
		}

		final TreeElement rootElement = (TreeElement) treeElementLookup.get(ROOT);

		boolean canEditAllReplicates = LoginInfoCache.getInstance().getPermissions().isCanEditAllReplicates();
		int currentUserId = LoginInfoCache.getInstance().getUser().getId();

		for (Integer userId : userList.keySet()) {
			if (!canEditAllReplicates && userId != currentUserId) {
				continue;
			}

			UserListItem userListItem = userList.get(userId);
			TreeElement userElement = (TreeElement) treeElementLookup.get(USER_PREFIX + userId);

			if (userElement == null) {
				userElement = new UserElement(userId, rootElement);

				rootElement.setHasChildren(true);
				rootElement.addChildIfChildrenAreLoaded(userElement);
			}

			userElement.setNameDateAndHasChildren(userListItem.getName(), 0, userListItem.hasChildren());
			treeElementLookup.put(USER_PREFIX + userId, userElement);
		}

		treeViewer.refresh(rootElement, true);
	}

	@Override
	public void userListGetError(int commandId, String message) {
		getChainedPart().raiseError(message);
		removeWaitingReminder(commandId);
	}

	@Override
	public void projectListGetCompleted(int commandId, ProjectList projectList) {
		final TreeElement user = treeElementLookup.get(USER_PREFIX + projectList.getUserId());
		ArrayList<TreeElement> children = new ArrayList<TreeElement>();

		for (Integer projectId : projectList.keySet()) {
			TreeElement project = treeElementLookup.get(PROJECT_PREFIX + projectId);

			if (project == null) {
				project = new ProjectElement(projectId, (UserElement) user);
				treeElementLookup.put(PROJECT_PREFIX + projectId, project);
			}

            project.setParent(user);
			ProjectListItem projectListItem = projectList.get(projectId);
			project.setNameDateAndHasChildren(projectListItem.getProject().getName(), 0, projectListItem.hasChildren());
			children.add(project);
		}

		user.setChildren(children);

		if (!treeViewer.isBusy()) {
			treeViewer.refresh(user, true);
		}

		removeWaitingReminder(commandId);
	}

	@Override
	public void projectListUpdated(int commandId, ProjectList projectList) {
		if (waitingOnTreeElements.contains(USER_PREFIX + projectList.getUserId())) {
			return;
		}

		TreeElement userElement = treeElementLookup.get(USER_PREFIX + projectList.getUserId());

		if (userElement == null) {
			return;
		}

		if (userElement.getChildren(this) == null) {
			userElement.setHasChildren(projectList.size() != 0);

			if (!treeViewer.isBusy()) {
				treeViewer.refresh(userElement, true);
			}

			return;
		}

		HashSet<TreeElement> refreshTreeElements = new HashSet<TreeElement>();
		refreshTreeElements.add(userElement);

		ArrayList<ProjectElement> toBeRemoved = new ArrayList<ProjectElement>();

		for (TreeElement treeElement : userElement.getChildren(this)) {
			ProjectElement projectElement = (ProjectElement) treeElement;

			if (!projectList.containsKey(projectElement.getId())) {
				toBeRemoved.add(projectElement);
			}
		}

		for (ProjectElement projectElement : toBeRemoved) {
			userElement.removeChild(projectElement);
			refreshTreeElements.add(userElement);

			if (projectElement.getParent() == userElement) {
				projectElement.setParent(null);
			}
		}

		for (Integer projectId: projectList.keySet()) {
			ProjectListItem projectListItem = projectList.get(projectId);

			TreeElement projectElement = treeElementLookup.get(PROJECT_PREFIX + projectId);

			if (projectElement == null) {
				projectElement = new ProjectElement(projectId, (UserElement) userElement);
				userElement.setHasChildren(true);
			}

			projectElement.setParent(userElement);
			userElement.addChildIfChildrenAreLoaded(projectElement);
			projectElement.setNameDateAndHasChildren(projectListItem.getProject().getName(), 0, projectListItem.hasChildren());
			treeElementLookup.put(PROJECT_PREFIX + projectId, projectElement);
		}

		if (!treeViewer.isBusy()) {
			for (TreeElement treeElement : refreshTreeElements) {
				treeViewer.refresh(treeElement, true);
			}
		}
	}

	@Override
	public void projectListGetError(int commandId, String message) {
		getChainedPart().raiseError(message);
		removeWaitingReminder(commandId);
	}

	@Override
	public void sampleListGetCompleted(int commandId, SampleList sampleList) {
		TreeElement project = treeElementLookup.get(PROJECT_PREFIX + sampleList.getProjectId());
		ArrayList<TreeElement> children = new ArrayList<TreeElement>();

		for (Integer sampleId : sampleList.keySet()) {
			TreeElement sample = treeElementLookup.get(SAMPLE_PREFIX + sampleId);

			if (sample == null) {
				sample = new SampleElement(sampleId, (ProjectElement) project);
				treeElementLookup.put(SAMPLE_PREFIX + sampleId, sample);
			}

            sample.setParent(project);
            SampleListItem sampleListItem = sampleList.get(sampleId);
            sample.setNameDateAndHasChildren(sampleListItem.getSample().getName(), 0, false);

			children.add(sample);
		}

		project.setChildren(children);

		if (!treeViewer.isBusy()) {
			treeViewer.refresh(project, true);
		}

		removeWaitingReminder(commandId);
	}

	@Override
	public void sampleListUpdated(int commandId, SampleList sampleList) {
		if (waitingOnTreeElements.contains(PROJECT_PREFIX + sampleList.getProjectId())) {
			return;
		}

		TreeElement projectElement = treeElementLookup.get(PROJECT_PREFIX + sampleList.getProjectId());

		if (projectElement == null) {
			return;
		}

		if (projectElement.getChildren(this) == null) {
			projectElement.setHasChildren(sampleList.size() != 0);

			if (!treeViewer.isBusy()) {
				treeViewer.refresh(projectElement, true);
			}

			return;
		}

		HashSet<TreeElement> refreshTreeElements = new HashSet<TreeElement>();
		refreshTreeElements.add(projectElement);

		ArrayList<SampleElement> toBeRemoved = new ArrayList<SampleElement>();

		for (TreeElement treeElement : projectElement.getChildren(this)) {
			SampleElement sampleElement = (SampleElement) treeElement;

			if (!sampleList.containsKey(sampleElement.getId())) {
				toBeRemoved.add(sampleElement);
			}
		}

		for (SampleElement sampleElement : toBeRemoved) {
			projectElement.removeChild(sampleElement);
			refreshTreeElements.add(projectElement);

			if (sampleElement.getParent() == projectElement) {
				sampleElement.setParent(null);
			}
		}

		for (Integer sampleId: sampleList.keySet()) {
			SampleListItem sampleListItem = sampleList.get(sampleId);

			TreeElement sampleElement = treeElementLookup.get(SAMPLE_PREFIX + sampleId);

			if (sampleElement == null) {
				sampleElement = new SampleElement(sampleId, (ProjectElement) projectElement);
				projectElement.setHasChildren(true);
			}

			sampleElement.setParent(projectElement);
			projectElement.addChildIfChildrenAreLoaded(sampleElement);
			sampleElement.setNameDateAndHasChildren(sampleListItem.getSample().getName(), 0, false);
			treeElementLookup.put(SAMPLE_PREFIX + sampleId, sampleElement);
		}

		if (!treeViewer.isBusy()) {
			for (TreeElement treeElement : refreshTreeElements) {
				treeViewer.refresh(treeElement, true);
			}
		}
	}

	@Override
	public void sampleListGetError(int commandId, String message) {
		getChainedPart().raiseError(message);
		removeWaitingReminder(commandId);
	}

	@Override
	public void sampleListDeleted(int projectId) {
		//TODO how to handle this?
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
	    if (!waitingOnTreeElements.contains(ROOT)) {
			int userListGetCommandId = UserCache.getInstance().userListGet(SampleSelectorComposite.this);
		    	addWaitingReminder(userListGetCommandId, ROOT);
	    }
	}
}
