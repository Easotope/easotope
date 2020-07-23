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

package org.easotope.client.admin.part.sampletype;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.nameselect.SelectComposite;
import org.easotope.shared.admin.cache.sampletype.SampleTypeCache;
import org.easotope.shared.admin.cache.sampletype.sampletypelist.SampleTypeCacheSampleTypeListGetListener;
import org.easotope.shared.admin.cache.sampletype.sampletypelist.SampleTypeList;
import org.easotope.shared.admin.cache.sampletype.sampletypelist.SampleTypeListItem;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.widgets.Composite;

public class SampleTypeSelectComposite extends SelectComposite implements SampleTypeCacheSampleTypeListGetListener {
	private boolean firstSelection = true;

	protected SampleTypeSelectComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style, Messages.sampleType_sampleTypeSelectCompositeLabel, true, SampleTypePart.SELECTION_SAMPLE_TYPE_LIST, SampleTypePart.SELECTION_SAMPLE_TYPE_ID);
		SampleTypeCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		SampleTypeCache.getInstance().removeListener(this);
	}

	@Override
	protected boolean canThisUserAdd() {
		return LoginInfoCache.getInstance().getPermissions().isCanEditSampleTypes();
	}

	@Override
	protected boolean hasSelectionChanged(HashMap<String,Object> selection) {
		boolean result = firstSelection;
		firstSelection = false;
		return result;
	}

	@Override
	protected int requestListGetFromCache(HashMap<String,Object> selection) {
		return SampleTypeCache.getInstance().sampleTypeListGet(this);
	}

	@Override
	protected String formatListItem(Object listItem) {
		return listItem.toString();
	}

	@Override
	protected int compareListItems(Object thisOne, Object thatOne) {
		return ((SampleTypeListItem) thisOne).getName().compareTo(((SampleTypeListItem) thatOne).getName());
	}

	@Override
	public void sampleTypeListGetCompleted(int commandId, SampleTypeList sampleTypeList) {
		setList(commandId, sampleTypeList);
	}

	@Override
	public void sampleTypeListUpdated(int commandId, SampleTypeList sampleTypeList) {
		updateList(sampleTypeList);
	}

	@Override
	public void sampleTypeListGetError(int commandId, String message) {
		setError(message);
	}
}
