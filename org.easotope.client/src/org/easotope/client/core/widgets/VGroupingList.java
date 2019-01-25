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

package org.easotope.client.core.widgets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class VGroupingList extends Composite {
	private static final String ID = "ID";
	private static final String SPINNER = "SPINNER";

	private boolean isEnabled = true;
	private Table table = null;
	private boolean possibilitiesHaveBeenSet = false;
    private HashMap<Integer,Integer> originalSpinnerValues = null;
	private Vector<VGroupingListListener> listeners = new Vector<VGroupingListListener>();

	public VGroupingList(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());

		table = new Table(this, SWT.SINGLE);
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setWidth(40);
		column = new TableColumn(table, SWT.NONE);
		column.setWidth(150);
		table.addSelectionListener(new LoggingSelectionAdaptor() {
			@Override
			public void loggingWidgetSelected(SelectionEvent e) {
				table.deselectAll();
			}
		});
	}

	public void setEnabled(boolean isEnabled) {		
		if (this.isEnabled == isEnabled) {
			return;
		}

		for (TableItem tableItem : table.getItems()) {
			Spinner spinner = (Spinner) tableItem.getData(SPINNER);
			spinner.setEnabled(isEnabled);
		}

		this.isEnabled = isEnabled;
	}

	public void setPossibilities(HashMap<String,Integer> possibilities) {
		HashMap<Integer,Integer> oldGroups = getGroups();

		for (TableItem tableItem : table.getItems()) {
			Spinner spinner = (Spinner) tableItem.getData(SPINNER);
			spinner.dispose();
		}

		table.removeAll();

		if (possibilities == null) {
			possibilitiesHaveBeenSet = false;
			return;
		}

		ArrayList<String> keys = new ArrayList<String>(possibilities.keySet());
		Collections.sort(keys);

		for (String key : keys) {
			int id = possibilities.get(key);

			TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(1, key);

			TableEditor editor = new TableEditor(table);
			Spinner spinner = new Spinner(table, SWT.CHECK);
			spinner.setMinimum(0);
			spinner.setMaximum(99);
			spinner.addSelectionListener(new LoggingSelectionAdaptor() {
				@Override
				public void loggingWidgetSelected(SelectionEvent e) {
					for (VGroupingListListener listener : listeners) {
						listener.spinnersChanged();
					}
				}
			});
			spinner.setEnabled(isEnabled);
			editor.setEditor(spinner, tableItem, 0);
			editor.grabHorizontal = true;
			editor.layout();

			tableItem.setData(ID, id);
			tableItem.setData(SPINNER, spinner);

			if (!possibilitiesHaveBeenSet && originalSpinnerValues != null) {
				for (Integer tmpId : originalSpinnerValues.keySet()) {
					if (tmpId == id) {
						spinner.setSelection(originalSpinnerValues.get(tmpId));
					}
				}
			}

			if (oldGroups.containsKey(id)) {
				spinner.setSelection(oldGroups.get(id));
			}
		}

		possibilitiesHaveBeenSet = true;
	}

	public void setGroups(HashMap<Integer,Integer> idToGroup) {
		for (TableItem tableItem : table.getItems()) {
			int id = (Integer) tableItem.getData(ID);
			Spinner spinner = (Spinner) tableItem.getData(SPINNER);
			spinner.setSelection(idToGroup.containsKey(id) ? idToGroup.get(id) : 0);
		}

		originalSpinnerValues = new HashMap<Integer,Integer>(idToGroup);
	}

	public HashMap<Integer,Integer> getGroups() {
		HashMap<Integer,Integer> groups = new HashMap<Integer,Integer>();

		for (TableItem tableItem : table.getItems()) {
			int id = (Integer) tableItem.getData(ID);
			Spinner spinner = (Spinner) tableItem.getData(SPINNER);

			if (spinner.getSelection() != 0) {
				groups.put(id, spinner.getSelection());
			}
		}

		return groups;
	}

	public boolean hasChanged() {
		if (originalSpinnerValues == null || !possibilitiesHaveBeenSet) {
			return false;
		}

		for (TableItem tableItem : table.getItems()) {
			int id = (Integer) tableItem.getData(ID);
			Spinner spinner = (Spinner) tableItem.getData(SPINNER);

			int originalValue = originalSpinnerValues.containsKey(id) ? originalSpinnerValues.get(id) : 0;

			if (spinner.getSelection() != originalValue) {
				return true;
			}
		}

		return false;
	}

	public void revert() {
		if (originalSpinnerValues == null) {
			setGroups(new HashMap<Integer,Integer>());
		} else {
			setGroups(originalSpinnerValues);
		}
	}

	public void addListener(VGroupingListListener listener) {
		listeners.add(listener);
	}

	public void removeListener(VGroupingListListener listener) {
		listeners.remove(listener);
	}
}
