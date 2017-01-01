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

package org.easotope.client.core.widgets.graph;

import java.util.ArrayList;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

public class MenuManager {
	private Display display;
	private Shell shell;

	private TreeSet<SortableMenuItem> defaultMenuItems = new TreeSet<SortableMenuItem>();
	private TreeSet<SortableMenuItem> graphMenuItems = new TreeSet<SortableMenuItem>();

	public MenuManager(Display display, Shell shell) {
		this.display = display;
		this.shell = shell;
	}

	void addDefaultMenuItem(MenuItemListener listener) {
		defaultMenuItems.add(new SortableMenuItem(listener));
	}

	void addGraphMenuItem(MenuItemListener listener) {
		graphMenuItems.add(new SortableMenuItem(listener));
	}

	protected void raiseMenu(int x, int y, int displayX, int displayY, ArrayList<DrawableObject> drawables) {
		Menu menu = new Menu(shell, SWT.POP_UP);

		for (SortableMenuItem sortableMenuItem : defaultMenuItems) {
			MenuItem item = new MenuItem(menu, SWT.NONE);
			item.setText(sortableMenuItem.getName());
			item.addListener(SWT.Selection, sortableMenuItem.getMenuItemListener());
		}

		if (graphMenuItems.size() != 0) {
			new MenuItem(menu, SWT.SEPARATOR);
	
			for (SortableMenuItem sortableMenuItem : graphMenuItems) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setText(sortableMenuItem.getName());
				item.addListener(SWT.Selection, sortableMenuItem.getMenuItemListener());
			}
		}

		DrawableObject closestDrawableObject = DrawableObjects.getClosestDrawableObject(x, y, drawables);

		if (closestDrawableObject != null) {
			TreeSet<SortableMenuItem> drawableObjectMenuItems = closestDrawableObject.getMenuItems();

			if (drawableObjectMenuItems != null && drawableObjectMenuItems.size() != 0) {
				new MenuItem(menu, SWT.SEPARATOR);

				for (SortableMenuItem sortableMenuItem : drawableObjectMenuItems) {
					MenuItem item = new MenuItem(menu, SWT.NONE);
					item.setText(sortableMenuItem.getName());
					item.addListener(SWT.Selection, sortableMenuItem.getMenuItemListener());
				}
			}
		}

		menu.setLocation(displayX, displayY);
		menu.setVisible(true);

		while (!menu.isDisposed() && menu.isVisible()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		menu.dispose();
	}
}
