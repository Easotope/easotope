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

package org.easotope.client.core.part;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;

public abstract class ChainedPart extends EasotopePart {
	abstract protected boolean closeOnSave();

	private HashMap<String,Object> selection = new HashMap<String,Object>();
	private int lastChainedCompositeIndex = -1;
	private HashMap<Integer,ChainedComposite> indexToChainedComposites = new HashMap<Integer,ChainedComposite>();
	private HashSet<Integer> chainedCompositeNeedsSelection = null;

	int registerComposite(ChainedComposite chainedComposite) {
		lastChainedCompositeIndex++;

		indexToChainedComposites.put(lastChainedCompositeIndex, chainedComposite);

		if (chainedCompositeNeedsSelection != null) {
			chainedCompositeNeedsSelection.add(lastChainedCompositeIndex);
		}

		return lastChainedCompositeIndex;
	}

	void deregisterComposite(ChainedComposite chainedComposite) {
		int index = chainedComposite.getIndex();
		indexToChainedComposites.remove(index);
	}

	public void setSelection(int index, String key, Object value) {
		if (key == null) {
			return;
		}

		HashMap<String,Object> hashMap = new HashMap<String,Object>();
		hashMap.put(key, value);
		setSelection(index, hashMap);
	}

	public void setSelection(int callersIndex, HashMap<String,Object> selection) {
		HashMap<String,Object> oldSelectionCopy = new HashMap<String,Object>(this.selection);
		this.selection.putAll(selection);
		boolean selectionChanged = false;

		if (oldSelectionCopy.size() != this.selection.size()) {
			selectionChanged = true;

		} else {	
			for (String key : this.selection.keySet()) {
				if (this.selection.get(key) != oldSelectionCopy.get(key)) {
					selectionChanged = true;
					break;
				}
			}
		}

		if (!selectionChanged) {
			return;
		}

		if (chainedCompositeNeedsSelection == null) {
			// this is not a recursive call

			chainedCompositeNeedsSelection = new HashSet<Integer>();
			chainedCompositeNeedsSelection.addAll(indexToChainedComposites.keySet());
			chainedCompositeNeedsSelection.remove(callersIndex);

			while (!chainedCompositeNeedsSelection.isEmpty()) {
				Integer nextIndex = (Integer) chainedCompositeNeedsSelection.toArray()[0];
				chainedCompositeNeedsSelection.remove(nextIndex);
				ChainedComposite chainedComposite = indexToChainedComposites.get(nextIndex);

				if (chainedComposite != null) {
					chainedComposite.receiveSelection();
				}
			}

			chainedCompositeNeedsSelection = null;

		} else {
			// this is a recursive call

			for (Integer index : indexToChainedComposites.keySet()) {
				if (index != callersIndex) {
					chainedCompositeNeedsSelection.add(callersIndex);
				}
			}
		}
	}

	public HashMap<String,Object> getSelection() {
		return selection;
	}

	public void setCanRevert() {
		boolean oneCanBeReverted = false;

		for (ChainedComposite chainedComposite : indexToChainedComposites.values()) {
			if (chainedComposite != null && chainedComposite.canRevert()) {
				oneCanBeReverted = true;
				break;
			}
		}

		setCanRevert(oneCanBeReverted);

		for (ChainedComposite chainedComposite : indexToChainedComposites.values()) {
			if (chainedComposite == null) {
				continue;
			}

			if (oneCanBeReverted) {
				if (!chainedComposite.canRevert()) {
					chainedComposite.setWidgetsDisabled();
				}
			} else {
				chainedComposite.setWidgetsEnabled();
			}
		}
	}

	@Override
	public void revert() {
		for (ChainedComposite chainedComposite : indexToChainedComposites.values()) {
			if (chainedComposite != null && chainedComposite.canRevert()) {
				chainedComposite.revert();
			}
		}
	}

	public void setCanPersist() {
		for (ChainedComposite chainedComposite : indexToChainedComposites.values()) {
			if (chainedComposite != null && chainedComposite.canPersist()) {
				setCanPersist(true);
				return;
			}
		}

		setCanPersist(false);
	}

	@Override
	public void persist() {
		for (ChainedComposite chainedComposite : indexToChainedComposites.values()) {
			if (chainedComposite != null && chainedComposite.canPersist()) {
				chainedComposite.persist();
			}
		}
	}

	public void setCanDelete() {
		// TODO delete currently only works with ChainedParts that have only one ChainedComposite
		if (indexToChainedComposites.size() != 1) {
			return;
		}

		for (ChainedComposite chainedComposite : indexToChainedComposites.values()) {
			if (chainedComposite != null && chainedComposite.canDelete()) {
				setCanDelete(true);
				return;
			}
		}

		setCanDelete(false);
	}

	@Override
	public void delete() {
		// TODO delete currently only works with ChainedParts that have only one ChainedComposite
		if (indexToChainedComposites.size() != 1) {
			return;
		}

		for (ChainedComposite chainedComposite : indexToChainedComposites.values()) {
			if (chainedComposite != null && chainedComposite.canDelete()) {
				chainedComposite.delete();
			}
		}
	}

	public void setCursor() {
		if (getParent().isDisposed()) {
			return;
		}

		for (Integer index : indexToChainedComposites.keySet()) {
			ChainedComposite chainedComposite = indexToChainedComposites.get(index);

			if (chainedComposite != null && chainedComposite.isWaiting()) {
				getParent().setCursor(getParent().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
				return;
			}
		}

		getParent().setCursor(null);
	}

	public void closePart() {
		getPart().getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
		getPartService().hidePart(getPart());
	}

	public boolean closePartIfRequested() {
		if (closeOnSave()) {
			closePart();
			return true;
		}

		return false;
	}
}
