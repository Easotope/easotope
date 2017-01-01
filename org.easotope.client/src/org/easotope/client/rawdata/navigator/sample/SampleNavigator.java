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

package org.easotope.client.rawdata.navigator.sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.PostConstruct;

import org.easotope.client.Messages;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.rawdata.navigator.PartManager;
import org.easotope.client.rawdata.project.ProjectPart;
import org.easotope.client.rawdata.replicate.SampleReplicatePart;
import org.easotope.client.rawdata.sample.SamplePart;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
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
import org.easotope.shared.rawdata.cache.input.replicatelist.InputCacheReplicateListGetListener;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateList;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateListItem;
import org.easotope.shared.rawdata.cache.input.samplelist.InputCacheSampleListGetListener;
import org.easotope.shared.rawdata.cache.input.samplelist.SampleList;
import org.easotope.shared.rawdata.cache.input.samplelist.SampleListItem;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;

public class SampleNavigator extends EasotopePart implements UserCacheUserListGetListener, InputCacheProjectListGetListener, InputCacheSampleListGetListener, InputCacheReplicateListGetListener, LoginInfoCacheLoginInfoGetListener {
	private Action createProject;
	private Action editProject;
	private Action createSample;
	private Action editSample;
	private Action createReplicate;
	private Action editReplicate;

	private TreeViewer treeViewer;

	private static final String ROOT = "root";
	private static final String USER_PREFIX = "u";
	private static final String PROJECT_PREFIX = "p";
	private static final String SAMPLE_PREFIX = "s";
	private static final String REPLICATE_PREFIX = "r";
	private HashMap<String,TreeElement> treeElementLookup = new HashMap<String,TreeElement>();
	private HashMap<Integer,String> waitingOnCommandIds = new HashMap<Integer,String>();
	private HashSet<String> waitingOnTreeElements = new HashSet<String>();

	@PostConstruct
	public void postConstruct() {
		treeViewer = new TreeViewer(getParent(), SWT.VIRTUAL);
		treeViewer.setContentProvider(new ContentProvider(this));
		treeViewer.setUseHashlookup(true);
		treeViewer.setComparator(new ViewerComparator());
		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection(); 
				Object selectedNode = thisSelection.getFirstElement(); 

				if (selectedNode instanceof ProjectElement) {
					ProjectElement projectElement = (ProjectElement) selectedNode;

					HashMap<String,String> parameters = new HashMap<String,String>();
					parameters.put(ProjectPart.INPUTURI_PARAM_PROJECT, String.valueOf(projectElement.getId()));

				    PartManager.openPart(SampleNavigator.this, ProjectPart.ELEMENTID_BASE, ProjectPart.class.getName(), parameters, true);

				} else if (selectedNode instanceof SampleElement) {
					SampleElement sampleElement = (SampleElement) selectedNode;

					HashMap<String,String> parameters = new HashMap<String,String>();
					parameters.put(SamplePart.INPUTURI_PARAM_SAMPLE, String.valueOf(sampleElement.getId()));

					PartManager.openPart(SampleNavigator.this, SamplePart.ELEMENTID_BASE, SamplePart.class.getName(), parameters, true);

				} else if (selectedNode instanceof ReplicateElement) {
					ReplicateElement replicateElement = (ReplicateElement) selectedNode;

					HashMap<String,String> parameters = new HashMap<String,String>();
					parameters.put(SampleReplicatePart.INPUTURI_PARAM_REPLICATE, String.valueOf(replicateElement.getId()));

					PartManager.openPart(SampleNavigator.this, SampleReplicatePart.ELEMENTID_BASE, SampleReplicatePart.class.getName(), parameters, true);
				}
			}
		});

		TreeElement root = new TreeElement(0, null);
		root.setNameDateAndHasChildren("root", 0, true);
		treeElementLookup.put(ROOT, root);
		treeViewer.setInput(root);

		createProject=new Action(){
			@Override public void run(){
				createProject();
			}
		};
		createProject.setText(Messages.sampleNavigator_addProject);

		editProject=new Action(){
			@Override public void run(){
				editProject();
			}
		};
		editProject.setText(Messages.sampleNavigator_editProject);

		createSample=new Action(){
			@Override public void run(){
				createSample();
			}
		};
		createSample.setText(Messages.sampleNavigator_addSample);

		editSample=new Action(){
			@Override public void run(){
				editSample();
			}
		};
		editSample.setText(Messages.sampleNavigator_editSample);

		createReplicate=new Action(){
			@Override public void run(){
				createReplicate();
			}
		};
		createReplicate.setText(Messages.sampleNavigator_addReplicate);

		editReplicate=new Action(){
			@Override public void run(){
				editReplicate();
			}
		};
		editReplicate.setText(Messages.sampleNavigator_editReplicate);

	    MenuManager menuMgr = new MenuManager();
	    menuMgr.setRemoveAllWhenShown(true);
	    menuMgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				fillContextMenu(manager);
			}
	    });
	    Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
	    treeViewer.getControl().setMenu(menu);

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

	void getReplicates(int sampleId) {
		int commandId = InputCache.getInstance().replicateListGet(this, true, sampleId, DatabaseConstants.EMPTY_DB_ID, DatabaseConstants.EMPTY_DB_ID);
	    addWaitingReminder(commandId, SAMPLE_PREFIX + sampleId);
	}

	private void fillContextMenu(IMenuManager manager) {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		TreeElement selectedElement = (TreeElement) selection.getFirstElement();

		if (selectedElement == null) {
			return;
		}

		if (selectedElement instanceof UserElement) {
			manager.add(createProject);

		} else if (selectedElement instanceof ProjectElement) {
			manager.add(editProject);
			manager.add(createSample);

		} else if (selectedElement instanceof SampleElement) {
			manager.add(editSample);
			manager.add(createReplicate);

		} else if (selectedElement instanceof ReplicateElement) {
			manager.add(editReplicate);
		}
	}

	private void createProject() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		UserElement userElement = (UserElement) selection.getFirstElement();

		if (userElement == null) {
			return;
		}

		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(ProjectPart.INPUTURI_PARAM_USER, String.valueOf(userElement.getId()));

	    PartManager.openPart(this, ProjectPart.ELEMENTID_BASE, ProjectPart.class.getName(), parameters, false);
	}

	private void editProject() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		ProjectElement projectElement = (ProjectElement) selection.getFirstElement();

		if (projectElement == null) {
			return;
		}

		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(ProjectPart.INPUTURI_PARAM_PROJECT, String.valueOf(projectElement.getId()));

		PartManager.openPart(this, ProjectPart.ELEMENTID_BASE, ProjectPart.class.getName(), parameters, true);
	}

	private void createSample() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		ProjectElement projectElement = (ProjectElement) selection.getFirstElement();

		if (projectElement == null) {
			return;
		}

		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(SamplePart.INPUTURI_PARAM_PROJECT, String.valueOf(projectElement.getId()));

		PartManager.openPart(this, SamplePart.ELEMENTID_BASE, SamplePart.class.getName(), parameters, false);
	}

	private void editSample() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		SampleElement sampleElement = (SampleElement) selection.getFirstElement();

		if (sampleElement == null) {
			return;
		}

		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(SamplePart.INPUTURI_PARAM_SAMPLE, String.valueOf(sampleElement.getId()));

		PartManager.openPart(this, SamplePart.ELEMENTID_BASE, SamplePart.class.getName(), parameters, true);
	}

	private void createReplicate() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		SampleElement sampleElement = (SampleElement) selection.getFirstElement();

		if (sampleElement == null) {
			return;
		}

		ProjectElement projectElement = sampleElement.getProjectElement();
		UserElement userElement = projectElement.getUserElement();
		
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(SampleReplicatePart.INPUTURI_PARAM_USER, String.valueOf(userElement.getId()));
		parameters.put(SampleReplicatePart.INPUTURI_PARAM_PROJECT, String.valueOf(projectElement.getId()));
		parameters.put(SampleReplicatePart.INPUTURI_PARAM_SAMPLE, String.valueOf(sampleElement.getId()));

		PartManager.openPart(this, SampleReplicatePart.ELEMENTID_BASE, SampleReplicatePart.class.getName(), parameters, false);
	}

	private void editReplicate() {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		ReplicateElement replicateElement = (ReplicateElement) selection.getFirstElement();

		if (replicateElement == null) {
			return;
		}

		SampleElement sampleElement = replicateElement.getSampleElement();
		ProjectElement projectElement = sampleElement.getProjectElement();
		UserElement userElement = projectElement.getUserElement();

		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(SampleReplicatePart.INPUTURI_PARAM_USER, String.valueOf(userElement.getId()));
		parameters.put(SampleReplicatePart.INPUTURI_PARAM_PROJECT, String.valueOf(projectElement.getId()));
		parameters.put(SampleReplicatePart.INPUTURI_PARAM_SAMPLE, String.valueOf(sampleElement.getId()));
		parameters.put(SampleReplicatePart.INPUTURI_PARAM_REPLICATE, String.valueOf(replicateElement.getId()));

		PartManager.openPart(this, SampleReplicatePart.ELEMENTID_BASE, SampleReplicatePart.class.getName(), parameters, true);
	}

	@Override
	public Display getDisplay() {
		return getParent().getDisplay();
	}

	@Override
	public boolean stillCallabled() {
		return getParent() != null && !getParent().isDisposed();
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
		raiseError(message);
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

			ProjectListItem projectListItem = projectList.get(projectId);
			project.setNameDateAndHasChildren(projectListItem.getProject().getName(), 0, projectListItem.hasChildren());
			children.add(project);
		}

		user.setChildren(children);

		treeViewer.refresh(user, true);
		removeWaitingReminder(commandId);
	}

	@Override
	public void projectListUpdated(int commandId, ProjectList projectList) {
		if (waitingOnTreeElements.contains(USER_PREFIX + projectList.getUserId())) {
			return;
		}

		TreeElement userElement = treeElementLookup.get(USER_PREFIX + projectList.getUserId());

		for (Integer projectId: projectList.keySet()) {
			ProjectListItem projectListItem = projectList.get(projectId);
			TreeElement projectElement = treeElementLookup.get(PROJECT_PREFIX + projectId);

			if (projectElement == null) {
				projectElement = new ProjectElement(projectId, (UserElement) userElement);

				userElement.setHasChildren(true);
				userElement.addChildIfChildrenAreLoaded(projectElement);
			}

			projectElement.setNameDateAndHasChildren(projectListItem.getProject().getName(), 0, projectListItem.hasChildren());
			treeElementLookup.put(PROJECT_PREFIX + projectId, projectElement);
		}

		treeViewer.refresh(userElement, true);
	}

	@Override
	public void projectListGetError(int commandId, String message) {
		raiseError(message);
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

            SampleListItem sampleListItem = sampleList.get(sampleId);
            sample.setNameDateAndHasChildren(sampleListItem.getSample().getName(), 0, sampleListItem.hasChildren());

			children.add(sample);
		}

		project.setChildren(children);

		treeViewer.refresh(project, true);
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

        for (Integer sampleId: sampleList.keySet()) {
        		SampleListItem sampleListItem = sampleList.get(sampleId);
        		TreeElement sampleElement = treeElementLookup.get(SAMPLE_PREFIX + sampleId);

        		if (sampleElement == null) {
        			sampleElement = new SampleElement(sampleId, (ProjectElement) projectElement);

        			projectElement.setHasChildren(true);
        			projectElement.addChildIfChildrenAreLoaded(sampleElement);
        		}

        		sampleElement.setNameDateAndHasChildren(sampleListItem.getSample().getName(), 0, sampleListItem.hasChildren());
        		treeElementLookup.put(SAMPLE_PREFIX + sampleId, sampleElement);
        }

        treeViewer.refresh(projectElement, true);
	}

	@Override
	public void sampleListGetError(int commandId, String message) {
		raiseError(message);
		removeWaitingReminder(commandId);
	}

	@Override
	public void replicateListGetCompleted(int commandId, ReplicateList replicateList) {
		int sampleId = replicateList.getSampleId();
		TreeElement sample = treeElementLookup.get(SAMPLE_PREFIX + sampleId);
		ArrayList<TreeElement> children = new ArrayList<TreeElement>();

		for (Integer replicateId : replicateList.keySet()) {
			TreeElement replicate = treeElementLookup.get(REPLICATE_PREFIX + replicateId);

			if (replicate == null) {
				replicate = new ReplicateElement(replicateId, (SampleElement) sample);
				treeElementLookup.put(REPLICATE_PREFIX + replicateId, replicate);
			}

            ReplicateListItem replicateListItem = replicateList.get(replicateId);
            replicate.setNameDateAndHasChildren(null, replicateListItem.getDate(), false);

			children.add(replicate);
		}

		sample.setChildren(children);

		treeViewer.refresh(sample, true);
		removeWaitingReminder(commandId);
	}

	@Override
	public void replicateListUpdated(int commandId, ReplicateList replicateList) {
		if (waitingOnTreeElements.contains(SAMPLE_PREFIX + replicateList.getSampleId())) {
			return;
		}

		if (!replicateList.isGetSamples() || replicateList.getSampleId() == DatabaseConstants.EMPTY_DB_ID || replicateList.getMassSpecId() != DatabaseConstants.EMPTY_DB_ID || replicateList.getUserId() != DatabaseConstants.EMPTY_DB_ID) {
			return;
		}

		TreeElement sampleElement = treeElementLookup.get(SAMPLE_PREFIX + replicateList.getSampleId());

		if (sampleElement == null) {
			return;
		}

		if (sampleElement.getChildren(this) == null) {
			sampleElement.setHasChildren(replicateList.size() != 0);
			treeViewer.refresh(sampleElement, true);
			return;
		}

		ArrayList<ReplicateElement> toBeRemoved = new ArrayList<ReplicateElement>();

		for (TreeElement treeElement : sampleElement.getChildren(this)) {
			ReplicateElement replicateElement = (ReplicateElement) treeElement;
			
			if (!replicateList.containsKey(replicateElement.getId())) {
				toBeRemoved.add(replicateElement);
			}
		}

		for (ReplicateElement replicateElement : toBeRemoved) {
			sampleElement.removeChild(replicateElement);

			ReplicateElement savedReplicateElement = (ReplicateElement) treeElementLookup.get(REPLICATE_PREFIX + replicateElement.getId());

			if (savedReplicateElement.getSampleElement() == sampleElement) {
				treeElementLookup.remove(REPLICATE_PREFIX + replicateElement.getId());
			}
		}

		HashSet<Integer> alreadyHasIds = new HashSet<Integer>();

		for (TreeElement treeElement : sampleElement.getChildren(this)) {
			alreadyHasIds.add(treeElement.getId());
		}

        for (Integer replicateId: replicateList.keySet()) {
        		if (alreadyHasIds.contains(replicateId)) {
        			continue;
        		}

        		ReplicateListItem replicateListItem = replicateList.get(replicateId);

        		ReplicateElement replicateElement = new ReplicateElement(replicateId, (SampleElement) sampleElement);
        		replicateElement.setNameDateAndHasChildren(null, replicateListItem.getDate(), false);

        		treeElementLookup.put(REPLICATE_PREFIX + replicateId, replicateElement);

    			sampleElement.setHasChildren(true);
    			sampleElement.addChildIfChildrenAreLoaded(replicateElement);
        }

		treeViewer.refresh(sampleElement, true);
	}

	@Override
	public void replicateListGetError(int commandId, String message) {
		raiseError(message);
		removeWaitingReminder(commandId);
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
	    if (!waitingOnTreeElements.contains(ROOT)) {
			int userListGetCommandId = UserCache.getInstance().userListGet(SampleNavigator.this);
		    	addWaitingReminder(userListGetCommandId, ROOT);
	    }
	}
}
