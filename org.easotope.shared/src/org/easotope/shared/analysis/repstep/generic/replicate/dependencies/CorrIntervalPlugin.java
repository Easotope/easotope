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

package org.easotope.shared.analysis.repstep.generic.replicate.dependencies;

import java.util.HashMap;

import org.easotope.framework.commands.Command;
import org.easotope.shared.Messages;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.tables.Preferences;

public class CorrIntervalPlugin extends DependencyPlugin {
	private CorrIntervalList corrIntervalList = null;
	private Long corrIntervalDate = null;

	CorrIntervalPlugin() {
		super("corr interval");
	}

	@Override
	public Object getObject() {
		return corrIntervalDate;
	}

	@Override
	public HashMap<String,String> getPrintableValues(DependencyManager dependencyManager) {
		HashMap<String,String> result = new HashMap<String,String>();

		String formatted = null;

		if (corrIntervalDate != null) {
			String timeZone = "GMT";
			boolean showTimeZone = true;

			Preferences preferences = LoginInfoCache.getInstance().getPreferences();

			if (preferences != null) {
				timeZone = preferences.getTimeZoneId();
				showTimeZone = preferences.getShowTimeZone();
			}

			formatted = DateFormat.format(corrIntervalDate, timeZone, showTimeZone, true);
		}

		result.put(Messages.corrIntervalPlugin_corrInterval, formatted == null ? "UNDEFINED" : formatted);
		return result;
	}

	@Override
	public int requestObject(DependencyManager dependencyManager) {
		int massSpecId = dependencyManager.getReplicate().getMassSpecId();
		return CorrIntervalCache.getInstance().corrIntervalListGet(massSpecId, dependencyManager);
	}

	@Override
	public int receivedObject(DependencyManager dependencyManager, Object object) {
		corrIntervalList = (CorrIntervalList) object;

		long replicateDate = dependencyManager.getReplicate().getDate();
		corrIntervalDate = getCorrIntervalDate(corrIntervalList, replicateDate);

		return Command.UNDEFINED_ID;
	}

	private Long getCorrIntervalDate(CorrIntervalList corrIntervalList, long replicateDate) {
		if (corrIntervalList == null) {
			return null;
		}

		Long bestDate = null;
		long distanceToBest = Long.MAX_VALUE;

		for (int corrIntervalId : corrIntervalList.keySet()) {
			long thisDate = corrIntervalList.get(corrIntervalId).getDate();
			
			if (thisDate < replicateDate) {
				long distanceToThis = replicateDate - thisDate;

				if (distanceToThis < distanceToBest) {
					bestDate = thisDate;
					distanceToBest = distanceToThis;
				}
			}
		}

		return bestDate;
	}

	@Override
	public boolean verifyCurrentObject(DependencyManager dependencyManager) {
		return corrIntervalList != null;
	}

	@Override
	public AbstractCache[] getCachesToListenTo() {
		return new AbstractCache[] { CorrIntervalCache.getInstance() };
	}

	@Override
	public boolean isNoLongerValid(DependencyManager dependencyManager, Object object) {
		if (object instanceof CorrIntervalList) {
			CorrIntervalList corrIntervalList = (CorrIntervalList) object;
			long replicateDate = dependencyManager.getReplicate().getDate();

			Long newCorrIntervalDate = getCorrIntervalDate(corrIntervalList, replicateDate);

			if (corrIntervalDate == null && newCorrIntervalDate != null) {
				return true;
			}

			if (corrIntervalDate != null && newCorrIntervalDate == null) {
				return true;
			}

			if (corrIntervalDate != null && (((long) corrIntervalDate) != ((long) newCorrIntervalDate))) {
				return true;
			}
		}

		return false;
	}
}
