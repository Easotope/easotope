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

package org.easotope.shared.core.scratchpad;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

abstract class DatedPad extends Pad {
	private long date;

	DatedPad(Pad parent, long date) {
		super(parent);
		this.date = date;
		Collections.sort(parent.children);
	}

	DatedPad(Pad parent, DatedPad oldPad) {
		super(parent, oldPad);
		date = oldPad.date;
	}

	DatedPad(Pad parent, ObjectInput input, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		super(parent, input, allProperties, propertyToIndex);
		date = input.readLong();
	}

	public long getDate() {
		return date;
	}

	@Override
	public String getPrintableIdentifier() {
		String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
		boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
		return DateFormat.format(date, timeZone, showTimeZone, false);
	}

	@Override
	void writeExternal(ObjectOutput output, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		super.writeExternal(output, allProperties, propertyToIndex);
		output.writeLong(date);
	}

	@Override
	public int compareTo(Pad that) {
		if (that instanceof DatedPad) {
			long thisDate = this.date;
			long thatDate = ((DatedPad) that).date;

			if (thisDate == thatDate) {
				String thisAnalysis = (String) this.getValue(Pad.ANALYSIS);
				String thatAnalysis = (String) that.getValue(Pad.ANALYSIS);

				if (thisAnalysis == null) {
					thisAnalysis = "";
				}

				if (thatAnalysis == null) {
					thatAnalysis = "";
				}

				return thisAnalysis.compareTo(thatAnalysis);
			}

			return ((Long) thisDate).compareTo(thatDate);
		}

		return 0;
	}
}
