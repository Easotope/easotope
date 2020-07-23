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

package org.easotope.client.rawdata.batchimport.loader;

import org.easotope.client.rawdata.batchimport.BatchImportComposite;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalCacheCorrIntervalListGetListener;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
import org.eclipse.swt.widgets.Display;

public class CorrIntervalListLoader implements CorrIntervalCacheCorrIntervalListGetListener {
	private final String WAITING_FOR_CORR_INTERVAL_LIST = CorrIntervalListLoader.class.getName();

	private BatchImportComposite parent;

	public CorrIntervalListLoader(BatchImportComposite parent) {
		this.parent = parent;
		MassSpecCache.getInstance().addListener(this);
	}

	public void dispose() {
		MassSpecCache.getInstance().removeListener(this);
	}

	public void setMassSpecId(int massSpecId) {
		int commandId = CorrIntervalCache.getInstance().corrIntervalListGet(massSpecId, this);
		parent.waitingFor(WAITING_FOR_CORR_INTERVAL_LIST, commandId);
	}

	@Override
	public Display getDisplay() {
		return parent.getDisplay();
	}

	@Override
	public boolean stillCallabled() {
		return parent.stillCallabled();
	}

	@Override
	public void corrIntervalListGetCompleted(int commandId, CorrIntervalList corrIntervalList) {
		if (parent.commandIdForKey(WAITING_FOR_CORR_INTERVAL_LIST) == commandId) {
			parent.doneWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST);
			parent.newCorrIntervalList(corrIntervalList);
		}
	}

	@Override
	public void corrIntervalListUpdated(int commandId, CorrIntervalList corrIntervalList) {
		parent.doneWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST);
		parent.newCorrIntervalList(corrIntervalList);
	}

	@Override
	public void corrIntervalListGetError(int commandId, String message) {
		if (parent.commandIdForKey(WAITING_FOR_CORR_INTERVAL_LIST) == commandId) {
			parent.raiseGetError(WAITING_FOR_CORR_INTERVAL_LIST, message);
		}
	}
}
