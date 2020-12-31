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

package org.easotope.shared.plugin.analysis.databaseupgradehandler;

import org.easotope.framework.dbcore.util.RawFileManager;

import com.j256.ormlite.support.ConnectionSource;

public class Upgrade20190125 extends DatabaseUpgrade {
	@Override
	public int appliesToVersion() {
		return 20190125;
	}

	@Override
	public int resultsInVersion() {
		return 20200723;
	}

	@Override
	public boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource, int originalServerVersion) {
//		// fix clumped analysis
//		
//		if (!shiftParametersBy(connectionSource, 1, 10, 3)) {
//			return false;
//		}
//
//		// fix icl analysis
//
//		if (!shiftParametersBy(connectionSource, 2, 10, 2)) {
//			return false;
//		}
//
//		// fix eth analysis
//
//		if (!shiftParametersBy(connectionSource, 3, 11, 3)) {
//			return false;
//		}

		rebuildAnalyses = true;
		return true;
	}

//	private boolean shiftParametersBy(ConnectionSource connectionSource, int analysisId, int position, int shiftBy) {
//		try {
//			Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);
//
//			QueryBuilder<RepStepParams,Integer> queryBuilder = repStepParamsDao.queryBuilder();
//			Where<RepStepParams, Integer> where = queryBuilder.where();
//			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
//			where = where.and();
//			where = where.ge(RepStepParams.POSITION_FIELD_NAME, position);
//			PreparedQuery<RepStepParams> preparedQuery = queryBuilder.prepare();
//
//			for (RepStepParams repStepParams : repStepParamsDao.query(preparedQuery)) {
//				repStepParams.setPosition(repStepParams.getPosition() + shiftBy);
//				repStepParamsDao.update(repStepParams);
//			}
//
//		} catch (Exception e) {
//			Log.getInstance().log(Level.INFO, Upgrade20190125.class, "Error while shifting analysis (id=" + analysisId + ") RepStepParams", e);
//			return false;
//		}
//		
//		return true;
//	}
}
