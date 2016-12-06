/*
 * Copyright © 2016 by Devon Bowen.
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

package org.easotope.client.core.widgets.checktree;

import java.util.HashSet;

import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class CheckTree<T> extends Composite {
    private Tree tree = null;
    private HashSet<T> originalValues;

	public CheckTree(Composite parent, int style) {
		super(parent, style);
		setLayout(new FillLayout());

		tree = new Tree(this, SWT.CHECK);
		tree.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				TreeItem treeItem = ((TreeItem) event.item);

				if (treeItem == null || !treeItem.getChecked()) {
					return;
				}

				for (TreeItem child : treeItem.getItems()) {
					child.setChecked(true);
				}

				TreeItem parent = treeItem.getParentItem();

				if (parent != null) {
					boolean allChildrenAreChecked = true;
					
					for (TreeItem item : parent.getItems()) {
						if (!item.getChecked()) {
							allChildrenAreChecked = false;
						}
					}

					parent.setChecked(allChildrenAreChecked);
				}
			}
		});

	    final TreeItem item1 = new TreeItem(tree, SWT.NONE);
	    item1.setText("Carrara Marble");
	    item1.setChecked(true);

	    final TreeItem ti1 = new TreeItem(item1, SWT.NONE);
	    ti1.setText("δ13C");
	    ti1.setChecked(true);
	    final TreeItem ti2 = new TreeItem(item1, SWT.NONE);
	    ti2.setText("δ18O");
	    ti2.setChecked(true);
	    final TreeItem ti3 = new TreeItem(item1, SWT.NONE);
	    ti3.setText("Δ47");
	    ti3.setChecked(true);

	    TreeItem item = new TreeItem(tree, SWT.NONE);
	    item.setText("EG25");
	    TreeItem one = new TreeItem(item, SWT.NONE);
	    one.setText("Δ47");
	    
	    item = new TreeItem(tree, SWT.NONE);
	    item.setText("EG50");
	    one = new TreeItem(item, SWT.NONE);
	    one.setText("Δ47");

	    item = new TreeItem(tree, SWT.NONE);
	    item.setText("EG80");
	    one = new TreeItem(item, SWT.NONE);
	    one.setText("Δ47");
	    
	    item = new TreeItem(tree, SWT.NONE);
	    item.setText("ETH3 carbonate");
	    item.setChecked(true);

	    one = new TreeItem(item, SWT.NONE);
	    one.setText("δ13C");
	    one.setChecked(true);
	    TreeItem two = new TreeItem(item, SWT.NONE);
	    two.setText("δ18O");
	    two.setChecked(true);
	    TreeItem three = new TreeItem(item, SWT.NONE);
	    three.setText("Δ47");
	    three.setChecked(true);

	    item = new TreeItem(tree, SWT.NONE);
	    item.setText("Heated Gas");
	    item.setChecked(true);
	    one = new TreeItem(item, SWT.NONE);
	    one.setText("Δ47");
	    one.setChecked(true);
	}

	public void setNodes(HashSet<CheckTreeNode<T>> nodes) {
		HashSet<T> currentlySelectedValues = getSelectedValues();

		tree.clearAll(true);
		buildTree(null);

		setSelectedValuesInTreeItems(currentlySelectedValues, tree.getItems());
	}

	private void buildTree(T parentId) {
		
	}

	public void setSelectedValues(HashSet<T> values) {
		originalValues = values;
		setSelectedValuesInTreeItems(values, tree.getItems());
	}

	private void setSelectedValuesInTreeItems(HashSet<T> values, TreeItem[] treeItems) {
		for (TreeItem treeItem : treeItems) {
			@SuppressWarnings("unchecked")
			T id = (T) treeItem.getData();
			treeItem.setChecked(values.contains(id));
			setSelectedValuesInTreeItems(values, treeItem.getItems());
		}
	}

	public HashSet<T> getSelectedValues() {
		HashSet<T> result = new HashSet<T>();
		getSelectedValuesFromTreeItems(tree.getItems(), result);
		return result;
	}

	private void getSelectedValuesFromTreeItems(TreeItem[] treeItems, HashSet<T> result) {
		for (TreeItem treeItem : treeItems) {
			if (treeItem.getChecked()) {
				@SuppressWarnings("unchecked")
				T id = (T) treeItem.getData();
				result.add(id);
				getSelectedValuesFromTreeItems(treeItem.getItems(), result);
			}
		}
	}

	public boolean hasChanged() {
		HashSet<T> currentlySelectedValues = getSelectedValues();

		if (currentlySelectedValues.size() != originalValues.size()) {
			return true;
		}
		
		for (T t : currentlySelectedValues) {
			if (!originalValues.contains(t)) {
				return true;
			}
		}

		return false;
	}

	public void revert() {
		setSelectedValuesInTreeItems(originalValues, tree.getItems());
	}
}
