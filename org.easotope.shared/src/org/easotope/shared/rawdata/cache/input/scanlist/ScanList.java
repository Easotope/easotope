/*
 * Copyright © 2016-2023 by Devon Bowen.
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

package org.easotope.shared.rawdata.cache.input.scanlist;

import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.core.cache.CacheList;

public class ScanList extends CacheList<ScanListItem> {
	private static final long serialVersionUID = 1L;

	private int massSpecId = DatabaseConstants.EMPTY_DB_ID;

	public ScanList() { }

	public ScanList(int massSpecId) {
		this.massSpecId = massSpecId;
	}

	public ScanList(ScanList that) {
		this.massSpecId = that.massSpecId;
	}
	
	public int getMassSpecId() {
		return massSpecId;
	}
}
