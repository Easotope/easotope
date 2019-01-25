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

package org.easotope.shared.analysis.cache.corrinterval.corrintervallist;

import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class CorrIntervalListItem {
	private long date;
	private int[] selectedDataAnalysis;
	private int batchDelimiter;
	private Integer[] channelToMzX10;

	public CorrIntervalListItem(long date, int[] selectedDataAnalysis, int batchDelimiter, Integer[] channelToMzX10) {
		this.date = date;
		this.selectedDataAnalysis = selectedDataAnalysis;
		this.batchDelimiter = batchDelimiter;
		this.channelToMzX10 = channelToMzX10;
	}

	public long getDate() {
		return date;
	}

	public int[] getSelectedDataAnalysis() {
		return selectedDataAnalysis;
	}

	public int getBatchDelimiter() {
		return batchDelimiter;
	}

	public Integer[] getChannelToMZX10() {
		return channelToMzX10;
	}

	@Override
	public String toString() {
		String currentTimeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
		boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
		return DateFormat.format(date, currentTimeZone, showTimeZone, false);
	}
}
