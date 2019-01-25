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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class VSelectList extends Composite {
	private static final String ID = "ID";
	private static final String CHECKBOX = "CHECKBOX";
	private static final String INDEX = "INDEX";

	private boolean selectionEnabled = true;
	private boolean allowSelectOnlyIfChecked = false;
	private boolean checkboxesEnabled = true;
	private boolean possibilitiesHaveBeenSet = false;

	private Table table = null;
    private Integer[] originalCheckboxSelection = null;
    private TableItem mostRecentSelectedItem = null;
	private Vector<VSelectListListener> listeners = new Vector<VSelectListListener>();

	public VSelectList(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());

		table = new Table(this, SWT.SINGLE);
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setWidth(25);
		column = new TableColumn(table, SWT.NONE);
		column.setWidth(150);
		table.addSelectionListener(new LoggingSelectionAdaptor() {
			@Override
			public void loggingWidgetSelected(SelectionEvent e) {
				TableItem[] selectedItems = table.getSelection();

				if (selectedItems.length != 1) {
					if (mostRecentSelectedItem != null) {
						mostRecentSelectedItem = null;

						for (VSelectListListener listener : listeners) {
							listener.selectionChanged();
						}
					}

					return;
				}

				TableItem selectedItem = selectedItems[0];

				if (selectedItem == mostRecentSelectedItem) {
					return;
				}

				Button checkbox = (Button) selectedItem.getData(CHECKBOX);

				if (!selectionEnabled || (allowSelectOnlyIfChecked && (checkbox == null || !checkbox.getSelection()))) {
					selectedItem = null;
					table.deselectAll();
				}

				if (selectedItem != mostRecentSelectedItem) {
					mostRecentSelectedItem = selectedItem;

					for (VSelectListListener listener : listeners) {
						listener.selectionChanged();
					}
				}
			}
		});
	}

	public void setSelectionEnabled(boolean selectionEnabled) {
		if (this.selectionEnabled == selectionEnabled) {
			return;
		}

		this.selectionEnabled = selectionEnabled;

		if (!selectionEnabled) {
			clearEntrySelection();
		}
	}

	public void setAllowSelectOnlyIfChecked(boolean allowSelectOnlyIfChecked) {
		if (this.allowSelectOnlyIfChecked == allowSelectOnlyIfChecked) {
			return;
		}

		this.allowSelectOnlyIfChecked = allowSelectOnlyIfChecked;
		clearEntrySelection();
	}

	public void setEnabled(boolean enabled) {
		if (super.getEnabled() == enabled) {
			return;
		}

		for (TableItem tableItem : table.getItems()) {
			Button checkbox = (Button) tableItem.getData(CHECKBOX);
			checkbox.setEnabled(!enabled ? false : checkboxesEnabled);
		}

		super.setEnabled(enabled);
	}

	public void setCheckboxesEnabled(boolean checkboxesEnabled) {
		if (this.checkboxesEnabled == checkboxesEnabled) {
			return;
		}

		this.checkboxesEnabled = checkboxesEnabled;

		if (super.isEnabled()) {
			for (TableItem tableItem : table.getItems()) {
				Button checkbox = (Button) tableItem.getData(CHECKBOX);
				checkbox.setEnabled(checkboxesEnabled);
			}
		}
	}

	public void setPossibilities(HashMap<String,Integer> possibilities) {
		List<Integer> oldEntrySelection = getEntrySelection();
		List<Integer> oldCheckboxSelection = getCheckboxSelection();

		for (TableItem tableItem : table.getItems()) {
			Button checkbox = (Button) tableItem.getData(CHECKBOX);
			checkbox.dispose();
		}

		table.removeAll();
		
		if (possibilities == null) {
			possibilitiesHaveBeenSet = false;
			return;
		}

		int entrySelectIndex = -1;

		ArrayList<String> keys = new ArrayList<String>(possibilities.keySet());
		Collections.sort(keys);

		for (String key : keys) {
			int value = possibilities.get(key);

			TableItem tableItem = new TableItem(table, SWT.NONE);
			tableItem.setText(1, key);

			TableEditor editor = new TableEditor(table);
			Button checkbox = new Button(table, SWT.CHECK);
			checkbox.addSelectionListener(new LoggingSelectionAdaptor() {
				@Override
				public void loggingWidgetSelected(SelectionEvent e) {
					Button checkbox = (Button) e.widget;
					int index = (Integer) checkbox.getData(INDEX);

					if (!checkbox.getSelection() && allowSelectOnlyIfChecked && mostRecentSelectedItem != null && index == (Integer) mostRecentSelectedItem.getData(INDEX)) {
						mostRecentSelectedItem = null;
						table.deselectAll();

						for (VSelectListListener listener : listeners) {
							listener.selectionChanged();
						}
					}

					for (VSelectListListener listener : listeners) {
						listener.checkBoxesChanged();
					}
				}
			});
			checkbox.setData(INDEX, table.getItemCount()-1);
			checkbox.setEnabled(checkboxesEnabled);
			editor.setEditor(checkbox, tableItem, 0);
			editor.grabHorizontal = true;
			editor.layout();

			tableItem.setData(ID, value);
			tableItem.setData(CHECKBOX, checkbox);
			tableItem.setData(INDEX, table.getItemCount()-1);

			if (!possibilitiesHaveBeenSet && originalCheckboxSelection != null) {
				for (Integer i : originalCheckboxSelection) {
					if (i == value) {
						checkbox.setSelection(true);
					}
				}
			}

			if (oldCheckboxSelection.contains(value)) {
				checkbox.setSelection(true);
			}

			if (oldEntrySelection.contains(value)) {
				entrySelectIndex = table.getItemCount() - 1;
			}
		}

		if (entrySelectIndex != -1) {
			table.select(entrySelectIndex);
		}

		possibilitiesHaveBeenSet = true;
	}

	public void clearEntrySelection() {
		mostRecentSelectedItem = null;
		table.deselectAll();
	}

	public List<Integer> getEntrySelection() {
		ArrayList<Integer> selection = new ArrayList<Integer>();

		for (TableItem tableItem : table.getSelection()) {
			int value = (Integer) tableItem.getData(ID);
			selection.add(value);
		}

		return selection;
	}

	public void setCheckboxSelection(List<Integer> selection) {
		for (TableItem tableItem : table.getItems()) {
			int value = (Integer) tableItem.getData(ID);
			Button checkbox = (Button) tableItem.getData(CHECKBOX);
			checkbox.setSelection(selection.contains(value));
		}

		originalCheckboxSelection = selection.toArray(new Integer[selection.size()]);
		Arrays.sort(originalCheckboxSelection);
	}

	public List<Integer> getCheckboxSelection() {
		ArrayList<Integer> selection = new ArrayList<Integer>();

		for (TableItem tableItem : table.getItems()) {
			int value = (Integer) tableItem.getData(ID);
			Button checkbox = (Button) tableItem.getData(CHECKBOX);

			if (checkbox.getSelection()) {
				selection.add(value);
			}
		}

		return selection;
	}

	public boolean hasChanged() {
		if (originalCheckboxSelection == null || !possibilitiesHaveBeenSet) {
			return false;
		}

		List<Integer> currentSelection = getCheckboxSelection();

		if (originalCheckboxSelection.length != currentSelection.size()) {
			return true;
		}

		Collections.sort(currentSelection);

		int index = 0;
		for (Integer value : currentSelection) {
			if (value.intValue() != originalCheckboxSelection[index].intValue()) {
				return true;
			}

			index++;
		}

		return false;
	}

	public void revert() {
		if (originalCheckboxSelection == null) {
			setCheckboxSelection(new ArrayList<Integer>());
		} else {
			setCheckboxSelection(Arrays.asList(originalCheckboxSelection));
		}
	}

	public void addListener(VSelectListListener listener) {
		listeners.add(listener);
	}

	public void removeListener(VSelectListListener listener) {
		listeners.remove(listener);
	}
}
