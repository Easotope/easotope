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

package org.easotope.client.admin.part.massspec;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.nameselect.SelectComposite;
import org.easotope.framework.commands.Command;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.admin.cache.massspec.refgaslist.MassSpecCacheRefGasListGetListener;
import org.easotope.shared.admin.cache.massspec.refgaslist.RefGasList;
import org.easotope.shared.admin.cache.massspec.refgaslist.RefGasListItem;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.widgets.Composite;

public class RefGasSelectComposite extends SelectComposite implements MassSpecCacheRefGasListGetListener {

	protected RefGasSelectComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style, Messages.massSpec_refGasSelectCompositeLabel, true, MassSpecPart.SELECTION_REF_GAS_LIST, MassSpecPart.SELECTION_REF_GAS_ID);		
		MassSpecCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		MassSpecCache.getInstance().removeListener(this);
	}

	@Override
	protected boolean canThisUserAdd() {
		return LoginInfoCache.getInstance().getPermissions().isCanEditMassSpecs();
	}

	@Override
	protected boolean hasSelectionChanged(HashMap<String,Object> selection) {
		RefGasList refGasList = (RefGasList) getList();
		Integer massSpecId = (Integer) selection.get(MassSpecPart.SELECTION_MASS_SPEC_ID);

		if (massSpecId == null && refGasList == null) {
			return false;
		}

		if ((massSpecId == null && refGasList != null) || (massSpecId != null && refGasList == null)) {
			return true;
		}

		return massSpecId != refGasList.getMassSpecId();
	}

	@Override
	protected int requestListGetFromCache(HashMap<String,Object> selection) {
		Integer massSpecId = (Integer) selection.get(MassSpecPart.SELECTION_MASS_SPEC_ID);

		if (massSpecId == null) {
			return Command.UNDEFINED_ID;
		} else {
			return MassSpecCache.getInstance().refGasListGet(massSpecId, this);
		}
	}

	@Override
	protected String formatListItem(Object listItem) {
		return listItem.toString();
	}

	@Override
	protected int compareListItems(Object thisOne, Object thatOne) {
		return -((Long) ((RefGasListItem) thisOne).getValidFrom()).compareTo(((RefGasListItem) thatOne).getValidFrom());
	}

	@Override
	public void refGasListGetCompleted(int commandId, RefGasList refGasList) {
		setList(commandId, refGasList);
	}

	@Override
	public void refGasListUpdated(int commandId, RefGasList refGasList) {
		RefGasList currentRefGasList = (RefGasList) getList();

		if (currentRefGasList != null && currentRefGasList.getMassSpecId() == refGasList.getMassSpecId()) {
			updateList(refGasList);
		}
	}

	@Override
	public void refGasListGetError(int commandId, String message) {
		setError(message);		
	}
}
