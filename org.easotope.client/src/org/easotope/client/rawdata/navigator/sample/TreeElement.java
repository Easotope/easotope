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

package org.easotope.client.rawdata.navigator.sample;

import java.util.ArrayList;

import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class TreeElement {
	protected int id;
	protected TreeElement parent;
	protected String name;
	protected long date = Long.MIN_VALUE;
	protected boolean hasChildren;
	protected ArrayList<TreeElement> children;

	public TreeElement(int id, TreeElement parent) {
		this.id = id;
		this.parent = parent;
	}

	int getId() {
		return id;
	}

	TreeElement getParent() {
		return parent;
	}

	void setParent(TreeElement parent) {
		this.parent = parent;
	}

	boolean setNameDateAndHasChildren(String name, long date, boolean hasChildren) {
		boolean stateChanged = false;

		stateChanged = stateChanged || (name == null && this.name != null);
		stateChanged = stateChanged || (name != null && !name.equals(this.name));
		stateChanged = stateChanged || date != this.date;
		stateChanged = stateChanged || hasChildren != this.hasChildren;

		this.name = name;
		this.date = date;
		this.hasChildren = hasChildren;

		if (!hasChildren) {
			children = null;
		}

		return stateChanged;
	}

	boolean hasChildren() {
		return hasChildren;
	}

	boolean setHasChildren(boolean hasChildren) {
		boolean stateChanged = this.hasChildren != hasChildren;
		this.hasChildren = hasChildren;
		return stateChanged;
	}

	boolean addChildIfChildrenAreLoaded(TreeElement treeElement) {
		boolean stateChanged = false;

		if (children != null) {
			//TODO do I need to remove children with this element id?? probably not

			if (!children.contains(treeElement)) {
				children.add(treeElement);
				stateChanged = true;
			}
		}

		return stateChanged;
	}

	void removeChild(TreeElement treeElement) {
		if (children == null) {
			return;
		}

		children.remove(treeElement);
	}

	void setChildren(ArrayList<TreeElement> children) {
		this.children = children;
	}

	ArrayList<TreeElement> getChildren(SampleNavigator sampleNavigator) {
		return children;
	}

	@Override
	public String toString() {
		String result = name;

		if (result == null) {
			String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			result = DateFormat.format(date, timeZone, showTimeZone, false);
		}

		return result;
	}
}
