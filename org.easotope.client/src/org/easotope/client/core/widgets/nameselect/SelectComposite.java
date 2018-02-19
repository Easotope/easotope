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

package org.easotope.client.core.widgets.nameselect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.adaptors.LoggingMouseAdaptor;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.core.cache.CacheList;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public abstract class SelectComposite extends ChainedComposite implements LoginInfoCacheLoginInfoGetListener {
	private Button addButton;
	private List list;

	private int waitingOnCommandId = Command.UNDEFINED_ID;
	private CacheList<?> cacheList = null;
	private HashMap<Integer,Integer> indexToId = new HashMap<Integer,Integer>();
	private HashMap<Integer,Integer> idToIndex = new HashMap<Integer,Integer>();

	private Integer selectedId = -1;
	private String listSelectionOutputKey;
	private String selectionOutputKey;
	private boolean widgetsAreEnabled = false;

	abstract protected boolean canThisUserAdd();
	abstract protected boolean hasSelectionChanged(HashMap<String,Object> selection);
	abstract protected int requestListGetFromCache(HashMap<String,Object> selection);
	abstract protected String formatListItem(Object listItem);
	abstract protected int compareListItems(Object thisOne, Object thatOne);

	public SelectComposite(ChainedPart chainedPart, Composite parent, int style, String displayLabel, boolean hasAddFunctionality, String listSelectionOutputKey, String selectionOutputKey) {
		super(chainedPart, parent, style);

		this.listSelectionOutputKey = listSelectionOutputKey;
		this.selectionOutputKey = selectionOutputKey;

		FormLayout formLayout= new FormLayout();
		formLayout.marginHeight = 5;
		formLayout.marginWidth = 5;
		this.setLayout(formLayout);

		Control lastControl = null;

		if (displayLabel != null) {
			Label label = new Label(this, SWT.PUSH);
			FormData formData = new FormData();
			formData.top = new FormAttachment(0);
			formData.left = new FormAttachment(0);
			label.setLayoutData(formData);
			label.setText(displayLabel);
			lastControl = label;
		}

		if (hasAddFunctionality) {
			addButton = new Button(this, SWT.PUSH);
			FormData formData = new FormData();
			if (lastControl == null) {
				formData.top = new FormAttachment(0);
			} else {
				formData.top = new FormAttachment(lastControl);
			}
			formData.left = new FormAttachment(0);
			addButton.setLayoutData(formData);
			addButton.setText(Messages.selectComposite_addUserButton);
			addButton.addSelectionListener(new LoggingSelectionAdaptor() {
				public void loggingWidgetSelected(SelectionEvent e) {
					addSelection();
				}
			});
			lastControl = addButton;
		}

		list = new List(this, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		FormData formData = new FormData();
		if (lastControl == null) {
			formData.top = new FormAttachment(0);
		} else {
			formData.top = new FormAttachment(lastControl);
		}
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		list.setLayoutData(formData);
		list.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				listSelection();
			}
		});
		list.addMouseListener(new LoggingMouseAdaptor() {
			@Override
			public void loggingMouseDoubleClick(MouseEvent e) {
				
			}

			@Override
			public void loggingMouseDown(MouseEvent e) {

			}

			@Override
			public void loggingMouseUp(MouseEvent e) {
				listMouseUp();
			}
		});

		setWidgetsDisabled();
		setVisible(false);

		LoginInfoCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		LoginInfoCache.getInstance().removeListener(this);
	}

	private void addSelection() {
		list.deselectAll();

		selectedId = null;
		propogateSelection(selectionOutputKey, selectedId);

		propogateAddRequest();
	}

	private void listSelection() {
		int selectedIndex = list.getSelectionIndex();
		selectedId = (selectedIndex == -1) ? null : indexToId.get(selectedIndex);
		propogateSelection(selectionOutputKey, selectedId);
	}

	private void listMouseUp() {
		int selectedIndex = list.getSelectionIndex();

		if (selectedIndex == -1) {
			selectedId = null;
			propogateSelection(selectionOutputKey, selectedId);
		}
	}

	@Override
	public boolean isWaiting() {
		return waitingOnCommandId != Command.UNDEFINED_ID;
	}

	@Override
	public void setWidgetsEnabled() {
		addButton.setEnabled(canThisUserAdd());
		list.setEnabled(true);
		widgetsAreEnabled = true;
	}

	@Override
	public void setWidgetsDisabled() {
		addButton.setEnabled(false);
		list.setEnabled(false);
		widgetsAreEnabled = false;
	}

	@Override
	public void receiveAddRequest() {
		assert(false);
	}

	@Override
	protected void cancelAddRequest() {
		assert(false);
	}

	@Override
	public void receiveSelection() {
		if (!hasSelectionChanged(getChainedPart().getSelection())) {
			return;
		}

		cacheList = null;
		list.removeAll();
		selectedId = null;
		propogateSelection(selectionOutputKey, null);
		rescindAddRequest();

		setWidgetsDisabled();
		setVisible(false);

		waitingOnCommandId = requestListGetFromCache(getChainedPart().getSelection());
		getChainedPart().setCursor();
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	protected CacheList<?> getList() {
		return cacheList;
	}

	protected void setList(int commandId, CacheList<?> cacheList) {
		if (commandId != Command.UNDEFINED_ID && commandId != waitingOnCommandId) {
			return;
		}

		waitingOnCommandId = Command.UNDEFINED_ID;
		getChainedPart().setCursor();

		updateList(cacheList);
		setWidgetsEnabled();
	}

	protected void updateList(CacheList<?> cacheList) {
		if (waitingOnCommandId != Command.UNDEFINED_ID) {
			return;
		}

		this.cacheList = cacheList;
		if (listSelectionOutputKey != null) {
			propogateSelection(listSelectionOutputKey, cacheList);
		}
		rebuildList();
	}

	protected void setError(String message) {
		updateList(null);
		getChainedPart().raiseError(message);
	}

	private void rebuildList() {
		if (cacheList == null) {
			indexToId.clear();
			idToIndex.clear();
			list.removeAll();

			selectedId = null;
			propogateSelection(selectionOutputKey, selectedId);

			setVisible(false);
			return;
		}

		// created sorted list of string/id pairs

		ArrayList<FormatAndSort> pairs = new ArrayList<FormatAndSort>();

		for (int id : cacheList.keySet()) {
			pairs.add(new FormatAndSort(id, cacheList.get(id)));
		}

		Collections.sort(pairs);

		// clear out old data structures

		indexToId.clear();
		idToIndex.clear();
		list.removeAll();

		// fill in new data structures

		int newSelectedIndex = -1;

		for (FormatAndSort formatAndSort : pairs) {
			indexToId.put(list.getItemCount(), formatAndSort.id);
			idToIndex.put(formatAndSort.id, list.getItemCount());

			if (selectedId == formatAndSort.id) {
				newSelectedIndex = list.getItemCount();
			}

			list.add(formatListItem(formatAndSort.object));
		}

		// reset previous selection

		if (newSelectedIndex != -1) {
			list.select(newSelectedIndex);
			list.showSelection();
		} else {
			selectedId = null;
			propogateSelection(selectionOutputKey, selectedId);
		}

		setVisible(true);
	}

	public void setSelectedId(int id) {
		selectedId = id;

		if (idToIndex.containsKey(id)) {
			list.select(idToIndex.get(id));
			list.showSelection();
		} else {
			list.deselectAll();
		}
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
		boolean thisUserCanAdd = canThisUserAdd();

		if (widgetsAreEnabled) {
			addButton.setEnabled(thisUserCanAdd);
		}

		if (!thisUserCanAdd && getChainedPart().getSelection().get(selectionOutputKey) == null) {
			rescindAddRequest();
		}
	}

	class FormatAndSort implements Comparable<FormatAndSort> {
		Integer id;
		Object object;

		FormatAndSort(Integer id, Object object) {
			this.id = id;
			this.object = object;
		}

		@Override
		public int compareTo(FormatAndSort that) {
			return compareListItems(this.object, that.object);
		}
	}
}
