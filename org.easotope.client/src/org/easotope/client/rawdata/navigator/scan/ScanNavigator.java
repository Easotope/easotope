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

package org.easotope.client.rawdata.navigator.scan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.rawdata.navigator.PartManager;
import org.easotope.client.rawdata.scan.ScanPart;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecCacheMassSpecListGetListener;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecList;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.userlist.UserCacheUserListGetListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.scanlist.InputCacheScanListGetListener;
import org.easotope.shared.rawdata.cache.input.scanlist.ScanList;
import org.eclipse.swt.SWT;
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

public class ScanNavigator extends EasotopePart implements UserCacheUserListGetListener, MassSpecCacheMassSpecListGetListener, InputCacheScanListGetListener, LoginInfoCacheLoginInfoGetListener {
	private SortedCombo massSpecs;
	private Button addReplicate;
	private Table scans;

	private int waitingForUserNames = Command.UNDEFINED_ID;
	private int waitingForMassSpecs = Command.UNDEFINED_ID;
	private int waitingForScans = Command.UNDEFINED_ID;

	private int currentlySelectedMassSpecId = -1;

	private UserList userIdToUserName = null;
	private ScanList scanList = null;
	private HashMap<Integer,Integer> indexToId = new HashMap<Integer,Integer>();

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
		addReplicate.setText(Messages.scanNavigator_addScanButton);
		addReplicate.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				addScan();
			}
		});

		scans = new Table(getParent(), SWT.BORDER | SWT.SINGLE);
		formData = new FormData();
		formData.top = new FormAttachment(controlComposite, 5);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		scans.setLayoutData(formData);
		scans.addListener(SWT.MouseDoubleClick, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				editScan();
			}
		});

		waitingForUserNames = UserCache.getInstance().userListGet(this);
		waitingForMassSpecs = MassSpecCache.getInstance().massSpecListGet(this);

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

		scanList = null;
		scans.removeAll();
		indexToId.clear();

		waitingForScans = InputCache.getInstance().scanListGet(this, massSpecId);
		setCursor();
	}

	protected void addScan() {
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(ScanPart.INPUTURI_PARAM_MASS_SPEC, String.valueOf(massSpecs.getSelectedInteger()));

		PartManager.openPart(this, ScanPart.ELEMENTID_BASE, ScanPart.class.getName(), parameters, false);
	}

	private void editScan() {
		int index = scans.getSelectionIndex();

		if (index == -1) {
			return;
		}

		int id = indexToId.get(index);

		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(ScanPart.INPUTURI_PARAM_SCAN, String.valueOf(id));

		PartManager.openPart(this, ScanPart.ELEMENTID_BASE, ScanPart.class.getName(), parameters, true);
	}

	private void setScanList(ScanList scanList) {
		this.scanList = scanList;
		refreshScans();
	}

	private void refreshScans() {
		if (scanList == null) {
			return;
		}

		int selectedId = scans.getSelectionIndex() != -1 ? indexToId.get(scans.getSelectionIndex()) : -1;

		ArrayList<KeyDate> sortedList = new ArrayList<KeyDate>();

		for (Integer key : scanList.keySet()) {
			Long date = scanList.get(key).getDate();
			sortedList.add(new KeyDate(key, date));
		}

		Collections.sort(sortedList);

		scans.setRedraw(false);

		scans.removeAll();
		indexToId.clear();

		int newSelectedIndex = -1;
		String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
		boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();

		for (KeyDate keyDate : sortedList) {
			int id = keyDate.getKey();
			long date = keyDate.getDate();
			int userId = scanList.get(id).getUserId();
			int index = scans.getItemCount();

			String text = DateFormat.format(date, timeZone, showTimeZone, false);
			String username = null;
			
			if (userIdToUserName != null) {
				username = userIdToUserName.get(userId).toString();
			}

			if (username == null) {
				username = String.valueOf(username);
			}

			text += " - " + username;

			TableItem item = new TableItem(scans, SWT.NONE);
			item.setText(text);

			indexToId.put(index, id);

			if (id == selectedId) {
				newSelectedIndex = index; 
			}
		}

		if (newSelectedIndex != -1) {
			scans.select(newSelectedIndex);
			scans.showSelection();
		}

		scans.setRedraw(true);
	}

	public void setCursor() {
		if (waitingForUserNames != Command.UNDEFINED_ID || waitingForMassSpecs != Command.UNDEFINED_ID || waitingForScans != Command.UNDEFINED_ID) {
			getParent().setCursor(getParent().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
		} else {
			getParent().setCursor(null);
		}
	}

	@Override
	public void userListGetCompleted(int commandId, final UserList userList) {
		userIdToUserName = userList;
		refreshScans();
		waitingForUserNames = Command.UNDEFINED_ID;
		setCursor();
	}

	@Override
	public void userListUpdated(int commandId, final UserList userList) {
		userIdToUserName = userList;
		refreshScans();
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

	@Override
	public void scanListGetCompleted(final int commandId, ScanList scanList) {
		if (waitingForScans != commandId) {
			return;
		}

		setScanList(scanList);

		waitingForScans = Command.UNDEFINED_ID;
		setCursor();
	}

	@Override
	public void scanListUpdated(int commandId, ScanList scanList) {
		if (this.scanList.getMassSpecId() == scanList.getMassSpecId()) {
			setScanList(scanList);
		}
	}

	@Override
	public void scanListGetError(final int commandId, String message) {
		if (waitingForScans != commandId) {
			return;
		}

		raiseError(message);

		waitingForScans = Command.UNDEFINED_ID;
		setCursor();
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
		refreshScans();
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
