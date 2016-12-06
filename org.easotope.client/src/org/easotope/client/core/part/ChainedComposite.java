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

package org.easotope.client.core.part;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.widgets.Composite;


public abstract class ChainedComposite extends EasotopeComposite {
	private ChainedPart chainedPart;
	private int index;
	private ArrayList<ChainedComposite> children = new ArrayList<ChainedComposite>();
	private boolean canRevert;
	private boolean canPersist;
	private boolean canDelete;

	/**
	 * Whether this composite is waiting for some data to be loaded from
	 * the database. This indicates that the part should show a "waiting"
	 * cursor.
	 * 
	 * @return true means it is waiting
	 */
	abstract public boolean isWaiting();

	/**
	 * Enable all widgets in this composite that the user has permission
	 * to modify. If the user does not have permission, the widgets will
	 * instead be disabled. If these newly disabled widgets have been
	 * modified since their original default values were set, the widget
	 * might need to be reverted.
	 */
	abstract protected void setWidgetsEnabled();

	/**
	 * Disable all widgets in this composite effectively freezing all input.
	 */
	abstract protected void setWidgetsDisabled();

	/**
	 * Called when the preceding ChainedComposite in this part issues a
	 * request to add a new record.
	 */
	abstract protected void receiveAddRequest();

	/**
	 * Called when the preceding ChainedComposite in this part cancels a
	 * request to add a new record.
	 */
	abstract protected void cancelAddRequest();

	/**
	 * Called when any ChainedComposite in this part sets a new selection.
	 */
	abstract protected void receiveSelection();

	protected ChainedComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);
		this.chainedPart = chainedPart;
		index = chainedPart.registerComposite(this);
	}

	protected void handleDispose() {
		chainedPart.deregisterComposite(this);
	}

	protected ChainedPart getChainedPart() {
		return chainedPart;
	}

	int getIndex() {
		return index;
	}

	protected void setCanRevert(boolean canRevert) {
		this.canRevert = canRevert;
		getChainedPart().setCanRevert();
	}

	boolean canRevert() {
		return canRevert;
	}

	void revert() {
		
	}

	protected void setCanPersist(boolean canPersist) {
		this.canPersist = canPersist;
		getChainedPart().setCanPersist();
	}

	boolean canPersist() {
		return canPersist;
	}

	void persist() {
		
	}

	protected void setCanDelete(boolean canDelete) {
		this.canDelete = canDelete;
		getChainedPart().setCanDelete();
	}

	boolean canDelete() {
		return canDelete;
	}

	void delete() {
		
	}

	public void addChild(ChainedComposite chainedComposite) {
		children.add(chainedComposite);
	}

	protected void propogateAddRequest() {
		for (ChainedComposite child : children) {
			child.receiveAddRequest();
		}
	}

	protected void rescindAddRequest() {
		for (ChainedComposite child : children) {
			child.cancelAddRequest();
		}
	}

	protected void propogateSelection(HashMap<String,Object> selection) {
		chainedPart.setSelection(index, selection);
	}

	protected void propogateSelection(String key, Object value) {
		chainedPart.setSelection(index, key, value);
	}
}
