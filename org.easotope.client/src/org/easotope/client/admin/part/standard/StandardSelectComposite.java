/*
 * Copyright © 2016-2020 by Devon Bowen.
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

package org.easotope.client.admin.part.standard;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.nameselect.SelectComposite;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standardlist.StandardCacheStandardListGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardList;
import org.easotope.shared.admin.cache.standard.standardlist.StandardListItem;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.widgets.Composite;

public class StandardSelectComposite extends SelectComposite implements StandardCacheStandardListGetListener {
	private boolean firstSelection = true;

	protected StandardSelectComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style, Messages.standardAdminPart_standardSelectCompositeLabel, true, StandardPart.SELECTION_STANDARD_LIST, StandardPart.SELECTION_STANDARD_ID);
		StandardCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		StandardCache.getInstance().removeListener(this);
	}

	@Override
	protected boolean canThisUserAdd() {
		return LoginInfoCache.getInstance().getPermissions().isCanEditStandards();
	}

	@Override
	protected boolean hasSelectionChanged(HashMap<String,Object> selection) {
		boolean result = firstSelection;
		firstSelection = false;
		return result;
	}

	@Override
	protected int requestListGetFromCache(HashMap<String,Object> selection) {
		return StandardCache.getInstance().standardListGet(this);
	}

	@Override
	protected String formatListItem(Object listItem) {
		return listItem.toString();
	}

	@Override
	protected int compareListItems(Object thisOne, Object thatOne) {
		return ((StandardListItem) thisOne).getName().compareTo(((StandardListItem) thatOne).getName());
	}

	@Override
	public void standardListGetCompleted(int commandId, StandardList standardList) {
		setList(commandId, standardList);
	}

	@Override
	public void standardListUpdated(int commandId, StandardList standardList) {
		updateList(standardList);
	}

	@Override
	public void standardListGetError(int commandId, String message) {
		setError(message);
	}
}
