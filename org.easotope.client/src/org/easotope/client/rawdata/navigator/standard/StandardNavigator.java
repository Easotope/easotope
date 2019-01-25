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

package org.easotope.client.rawdata.navigator.standard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.rawdata.navigator.PartManager;
import org.easotope.client.rawdata.replicate.StandardReplicatePart;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecCacheMassSpecListGetListener;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecList;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standard.StandardCacheStandardGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardCacheStandardListGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardList;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.userlist.UserCacheUserListGetListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.replicatelist.InputCacheReplicateListGetListener;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class StandardNavigator extends EasotopePart implements UserCacheUserListGetListener, MassSpecCacheMassSpecListGetListener, StandardCacheStandardListGetListener, StandardCacheStandardGetListener, InputCacheReplicateListGetListener, LoginInfoCacheLoginInfoGetListener {
	private SortedCombo massSpecs;
	private Button addReplicate;
	private SortedCombo standardFilter;
	private Table replicates;

	private int waitingForUserNames = Command.UNDEFINED_ID;
	private int waitingForMassSpecs = Command.UNDEFINED_ID;
	private int waitingForStandards = Command.UNDEFINED_ID;
	private int waitingForReplicates = Command.UNDEFINED_ID;

	private int currentlySelectedMassSpecId = -1;

	private boolean alreadyGettingIcon = false;
	private UserList userIdToUserName = null;
	private ReplicateList replicateList = null;
	private HashMap<Integer,Integer> indexToId = new HashMap<Integer,Integer>();

	private HashMap<Integer,Image> imageCache = new HashMap<Integer,Image>();

	@PostConstruct
	public void postConstruct() {
		FormLayout formLayout = new FormLayout();
		formLayout.marginHeight = 5;
		formLayout.marginWidth = 5;
		getParent().setLayout(formLayout);

		Composite controlComposite = new Composite(getParent(), SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		controlComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 2;
		controlComposite.setLayout(gridLayout);

		massSpecs = new SortedCombo(controlComposite, SWT.READ_ONLY);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		massSpecs.setLayoutData(gridData);
		massSpecs.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				massSpecChanged();
			}
		});

		addReplicate = new Button(controlComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		addReplicate.setLayoutData(gridData);
		addReplicate.setText(Messages.standardNavigator_addReplicateButton);
		addReplicate.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				addReplicate();
			}
		});

		standardFilter = new SortedCombo(getParent(), SWT.READ_ONLY);
		formData = new FormData();
		formData.top = new FormAttachment(controlComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(0, GuiConstants.MEDIUM_COMBO_INPUT_WIDTH);
		standardFilter.setLayoutData(formData);
		standardFilter.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				refreshReplicates();
			}
		});

		replicates = new Table(getParent(), SWT.BORDER | SWT.SINGLE);
		formData = new FormData();
		formData.top = new FormAttachment(standardFilter, 5);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		replicates.setLayoutData(formData);
		replicates.addListener(SWT.MouseDoubleClick, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				editReplicate();
			}
		});

		imageCache.put(DatabaseConstants.EMPTY_DB_ID, new Image(getParent().getDisplay(), 9, 7));

		waitingForUserNames = UserCache.getInstance().userListGet(this);
		waitingForMassSpecs = MassSpecCache.getInstance().massSpecListGet(this);
		waitingForStandards = StandardCache.getInstance().standardListGet(this);

		setCursor();

		UserCache.getInstance().addListener(this);
		MassSpecCache.getInstance().addListener(this);
		StandardCache.getInstance().addListener(this);
		InputCache.getInstance().addListener(this);
		LoginInfoCache.getInstance().addListener(this);
	}

	@PreDestroy
	public void preDestroy() {
		UserCache.getInstance().removeListener(this);
		MassSpecCache.getInstance().removeListener(this);
		StandardCache.getInstance().removeListener(this);
		InputCache.getInstance().removeListener(this);
		LoginInfoCache.getInstance().removeListener(this);
	}

	public void massSpecChanged() {
		int massSpecId = massSpecs.getSelectedInteger();

		if (massSpecId == currentlySelectedMassSpecId) {
			return;
		}

		currentlySelectedMassSpecId = massSpecId;

		replicateList = null;
		replicates.removeAll();
		indexToId.clear();

		waitingForReplicates = InputCache.getInstance().replicateListGet(this, false, DatabaseConstants.EMPTY_DB_ID, massSpecId, DatabaseConstants.EMPTY_DB_ID);
		setCursor();
	}

	protected void addReplicate() {
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(StandardReplicatePart.INPUTURI_PARAM_MASS_SPEC, String.valueOf(massSpecs.getSelectedInteger()));
		
		PartManager.openPart(this, StandardReplicatePart.ELEMENTID_BASE, StandardReplicatePart.class.getName(), parameters, false);
	}

	private void editReplicate() {
		int index = replicates.getSelectionIndex();

		if (index == -1) {
			return;
		}

		int id = indexToId.get(index);

		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(StandardReplicatePart.INPUTURI_PARAM_REPLICATE, String.valueOf(id));
		
		PartManager.openPart(this, StandardReplicatePart.ELEMENTID_BASE, StandardReplicatePart.class.getName(), parameters, true);
	}

	private void setReplicateList(ReplicateList replicateList) {
		this.replicateList = replicateList;
		refreshReplicates();
	}

	private void refreshReplicates() {
		if (replicateList == null) {
			return;
		}

		int selectedId = replicates.getSelectionIndex() != -1 ? indexToId.get(replicates.getSelectionIndex()) : -1;

		ArrayList<KeyDate> sortedList = new ArrayList<KeyDate>();

		for (Integer key : replicateList.keySet()) {
			Long date = replicateList.get(key).getDate();
			sortedList.add(new KeyDate(key, date));
		}

		Collections.sort(sortedList);

		replicates.setRedraw(false);

		replicates.removeAll();
		indexToId.clear();

		int newSelectedIndex = -1;
		int standardFilterId = standardFilter.getSelectedInteger();
		String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
		boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();

		for (KeyDate keyDate : sortedList) {
			int id = keyDate.getKey();

			if (standardFilterId != -1 && standardFilterId != DatabaseConstants.EMPTY_DB_ID && replicateList.get(id).getStandardId() != standardFilterId) {
				continue;
			}

			long date = keyDate.getDate();
			int userId = replicateList.get(id).getUserId();
			int index = replicates.getItemCount();

			String text = DateFormat.format(date, timeZone, showTimeZone, false);
			String username = null;
			
			if (userIdToUserName != null) {
				username = userIdToUserName.get(userId).toString();
			}

			if (username == null) {
				username = String.valueOf(username);
			}

			text += " - " + username;

			TableItem item = new TableItem(replicates, SWT.NONE);
			item.setImage(getIcon(replicateList.get(id).getStandardId()));
			item.setText(text);

			indexToId.put(index, id);

			if (id == selectedId) {
				newSelectedIndex = index; 
			}
		}

		if (newSelectedIndex != -1) {
			replicates.select(newSelectedIndex);
			replicates.showSelection();
		}

		replicates.setRedraw(true);
	}

	private Image getIcon(int standardId) {		
		if (imageCache.containsKey(standardId)) {
			if (imageCache.get(standardId) != null) {
				return imageCache.get(standardId);
			} else {
				return imageCache.get(DatabaseConstants.EMPTY_DB_ID);
			}
		}

		alreadyGettingIcon = true;
		StandardCache.getInstance().standardGet(standardId, this);
		alreadyGettingIcon = false;

		if (imageCache.containsKey(standardId)) {
			if (imageCache.get(standardId) != null) {
				return imageCache.get(standardId);
			} else {
				return imageCache.get(DatabaseConstants.EMPTY_DB_ID);
			}
		}

		imageCache.put(standardId, null);
		return imageCache.get(DatabaseConstants.EMPTY_DB_ID);
	}

	public void setCursor() {
		if (waitingForUserNames != Command.UNDEFINED_ID || waitingForMassSpecs != Command.UNDEFINED_ID || waitingForReplicates != Command.UNDEFINED_ID || waitingForStandards != Command.UNDEFINED_ID) {
			getParent().setCursor(getParent().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		} else {
			getParent().setCursor(null);
		}
	}

	@Override
	public void userListGetCompleted(int commandId, final UserList userList) {
		userIdToUserName = userList;
		refreshReplicates();
		waitingForUserNames = Command.UNDEFINED_ID;
		setCursor();
	}

	@Override
	public void userListUpdated(int commandId, final UserList userList) {
		userIdToUserName = userList;
		refreshReplicates();
		waitingForUserNames = Command.UNDEFINED_ID;
		setCursor();
	}

	@Override
	public void userListGetError(int commandId, final String message) {
		raiseError(message);
		waitingForUserNames = Command.UNDEFINED_ID;
		setCursor();
	}

	@Override
	public void massSpecListGetCompleted(int commandId, final MassSpecList massSpecList) {
		massSpecs.setPossibilities(massSpecList);

		waitingForMassSpecs = Command.UNDEFINED_ID;
		setCursor();

		if (massSpecs.getItemCount() != 0) {
			massSpecs.select(0);
			massSpecChanged();
		}
	}

	@Override
	public void massSpecListUpdated(int commandId, final MassSpecList massSpecList) {
		massSpecs.setPossibilities(massSpecList);
		waitingForMassSpecs = Command.UNDEFINED_ID;
		setCursor();
	}

	@Override
	public void massSpecListGetError(int commandId, final String message) {
		raiseError(message);
		waitingForMassSpecs = Command.UNDEFINED_ID;
		setCursor();
	}

	private void newStandardList(StandardList standardList) {
		HashMap<Integer,String> possibilitiesAsStrings = new HashMap<Integer,String>();
		possibilitiesAsStrings.put(DatabaseConstants.EMPTY_DB_ID, "");
		
		for (Integer key : standardList.keySet()) {
			possibilitiesAsStrings.put(key, standardList.get(key).toString());
		}

		standardFilter.setPossibilities(possibilitiesAsStrings);
		
		waitingForStandards = Command.UNDEFINED_ID;
		setCursor();
	}
	
	@Override
	public void standardListGetCompleted(int commandId, StandardList standardList) {
		newStandardList(standardList);
	}

	@Override
	public void standardListUpdated(int commandId, StandardList standardList) {
		newStandardList(standardList);
	}

	@Override
	public void standardListGetError(int commandId, String message) {
		raiseError(message);
		waitingForStandards = Command.UNDEFINED_ID;
		setCursor();
	}

	@Override
	public void standardGetCompleted(int commandId, final Standard standard) {
		if (imageCache.containsKey(standard.getId()) && imageCache.get(standard.getId()) != null) {
			imageCache.get(standard.getId()).dispose();
		}

	    Image src = new Image(getParent().getDisplay(), 9, 7);        
	    ImageData imageData = src.getImageData();
	    imageData.transparentPixel = imageData.getPixel(0, 0);
	    src.dispose();
	    Image image = new Image(getParent().getDisplay(), imageData);

		imageCache.put(standard.getId(), image);

		GC gc = new GC(image);
		new PointDesign(getParent().getDisplay(), standard.getColorId(), PointStyle.values()[standard.getShapeId()]).draw(gc, 3, 3, false);
		gc.dispose();
		
		if (!alreadyGettingIcon) {
			refreshReplicates();
		}
	}

	@Override
	public void standardUpdated(int commandId, final Standard standard) {
		if (imageCache.containsKey(standard.getId()) && imageCache.get(standard.getId()) != null) {
			imageCache.get(standard.getId()).dispose();
		}

		Image src = new Image(getParent().getDisplay(), 9, 7);
	    ImageData imageData = src.getImageData();
	    imageData.transparentPixel = imageData.getPixel(0, 0);
	    src.dispose();
	    Image image = new Image(getParent().getDisplay(), imageData);

		imageCache.put(standard.getId(), image);

		GC gc = new GC(image);
		new PointDesign(getParent().getDisplay(), standard.getColorId(), PointStyle.values()[standard.getShapeId()]).draw(gc, 3, 3, false);
		gc.dispose();
		
		refreshReplicates();
	}

	@Override
	public void standardGetError(int commandId, String message) {
		raiseError(message);
	}

	@Override
	public void replicateListGetCompleted(final int commandId, final ReplicateList replicateList) {
		if (waitingForReplicates != commandId) {
			return;
		}

		setReplicateList(replicateList);

		waitingForReplicates = Command.UNDEFINED_ID;
		setCursor();
	}

	@Override
	public void replicateListUpdated(int commandId, ReplicateList replicateList) {
		if (!replicateList.isGetSamples() && replicateList.getSampleId() == DatabaseConstants.EMPTY_DB_ID && replicateList.getMassSpecId() == massSpecs.getSelectedInteger() && replicateList.getUserId() == DatabaseConstants.EMPTY_DB_ID) {
			setReplicateList(replicateList);
		}
	}

	@Override
	public void replicateListGetError(final int commandId, final String message) {
		if (waitingForReplicates != commandId) {
			return;
		}

		raiseError(message);

		waitingForReplicates = Command.UNDEFINED_ID;
		setCursor();
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
		refreshReplicates();
	}

	private class KeyDate implements Comparable<KeyDate> {
		private Integer key;
		private Long date;

		KeyDate(Integer key, Long date) {
			this.key = key;
			this.date = date;
		}

		public Integer getKey() {
			return key;
		}

		public Long getDate() {
			return date;
		}

		@Override
		public int compareTo(KeyDate that) {
			return that.date.compareTo(this.date);
		}
	}

	@Override
	public Display getDisplay() {
		return getParent().getDisplay();
	}

	@Override
	public boolean stillCallabled() {
		return getParent() != null && !getParent().isDisposed();
	}
}
