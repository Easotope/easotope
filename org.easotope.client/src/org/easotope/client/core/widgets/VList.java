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

package org.easotope.client.core.widgets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

public class VList extends List {
	private int oldIndex = -1;

	public VList(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void select(int index) {
		// if caller is swt, then don't do our special stuff
		if (new Throwable().getStackTrace()[1].getClassName().startsWith("org.eclipse.swt")) {
			super.select(index);
			return;
		}

		oldIndex = index;
		super.select(index);
	}

	public boolean hasChanged() {
		return oldIndex != this.getSelectionIndex();
	}

	public void revert() {
		select(oldIndex);
	}

	@Override
	protected void checkSubclass() {
		// remove check to allow subclassing
	}
}
