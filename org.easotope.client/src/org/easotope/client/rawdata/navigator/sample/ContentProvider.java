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

package org.easotope.client.rawdata.navigator.sample;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ContentProvider implements ITreeContentProvider {
	private static Object[] EMPTY_ARRAY = new Object[0];
	protected SampleNavigator sampleNavigator;

	ContentProvider(SampleNavigator sampleNavigator) {
		this.sampleNavigator = sampleNavigator;
	}

	@Override
	public void dispose() {
		// ignore
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// ignore
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement != null) {
			ArrayList<?> children = ((TreeElement) parentElement).getChildren(sampleNavigator);

			if (children != null) {
				return children.toArray();
			}
		}

		return EMPTY_ARRAY;
	}

	@Override
	public Object getParent(Object element) {
		return ((TreeElement) element).getParent();
	}

	@Override
	public boolean hasChildren(Object element) {
		return ((TreeElement) element).hasChildren();
	}
}
