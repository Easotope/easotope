/*
 * Copyright © 2016 by Devon Bowen.
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

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.core.tables.TableLayout;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class Upgrade20160306 extends DatabaseUpgrade {
	@Override
	public int appliesToVersion() {
		return 20160306;
	}

	@Override
	public int resultsInVersion() {
		return 20160531;
	}

	@Override
	public boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource) {
		try {
			Dao<TableLayout,Integer> tableLayoutDao = DaoManager.createDao(connectionSource, TableLayout.class);

			DeleteBuilder<TableLayout,Integer> deleteBuilder = tableLayoutDao.deleteBuilder();
			Where<TableLayout,Integer> where = deleteBuilder.where();
			where = where.eq(TableLayout.CONTEXT_FIELD_NAME, "SAMPLE RESULTS");
			PreparedDelete<TableLayout> preparedDelete = deleteBuilder.prepare();
			tableLayoutDao.delete(preparedDelete);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160306.class, "Error while removing sample analysis table layouts", e);
			return false;
		}
		
		try {
			Dao<Preferences,Integer> preferencesDao = DaoManager.createDao(connectionSource, Preferences.class);
			preferencesDao.executeRaw("ALTER TABLE " + Preferences.TABLE_NAME + " ADD COLUMN " + Preferences.SHOWTIMEZONE_FIELD_NAME + " TINYINT DEFAULT 1 NOT NULL;");

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160306.class, "Error while adding CANEDITCONSTANTS to Permissions table.", e);
			return false;
		}

		try {
			Dao<Permissions,Integer> permissionsDao = DaoManager.createDao(connectionSource, Permissions.class);
			permissionsDao.executeRaw("ALTER TABLE " + Permissions.TABLE_NAME + " ADD COLUMN " + Permissions.CANEDITCONSTANTS_FIELD_NAME + " TINYINT DEFAULT 0 NOT NULL;");

			Dao<User,Integer> userDao = DaoManager.createDao(connectionSource, User.class);

			for (User user : userDao.queryForEq(User.ISADMIN_FIELD_NAME, true)) {
				Permissions permissions = permissionsDao.queryForId(user.getId());
				permissions.setCanEditConstants(true);
				permissionsDao.update(permissions);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160306.class, "Error while adding CANEDITCONSTANTS to Permissions table.", e);
			return false;
		}

		try {
			int analysisId = 1;		// clumped analysis
			Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);
			
			DeleteBuilder<RepStepParams,Integer> deleteBuilder = repStepParamsDao.deleteBuilder();
			Where<RepStepParams,Integer> where = deleteBuilder.where();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
			where = where.and();
			where = where.eq(RepStepParams.POSITION_FIELD_NAME, 0);
			PreparedDelete<RepStepParams> preparedDelete = deleteBuilder.prepare();
			repStepParamsDao.delete(preparedDelete);

			deleteBuilder = repStepParamsDao.deleteBuilder();
			where = deleteBuilder.where();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
			where = where.and();
			where = where.eq(RepStepParams.POSITION_FIELD_NAME, 8);
			preparedDelete = deleteBuilder.prepare();
			repStepParamsDao.delete(preparedDelete);

			QueryBuilder<RepStepParams,Integer> queryBuilder = repStepParamsDao.queryBuilder();
			where = queryBuilder.where();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
			where = where.and();
			where = where.gt(RepStepParams.POSITION_FIELD_NAME, 0);
			where = where.and();
			where = where.lt(RepStepParams.POSITION_FIELD_NAME, 8);
			PreparedQuery<RepStepParams> preparedQuery = queryBuilder.prepare();

			for (RepStepParams repStepParams : repStepParamsDao.query(preparedQuery)) {
				repStepParams.setPosition(repStepParams.getPosition() - 1);
				repStepParamsDao.update(repStepParams);
			}

			queryBuilder = repStepParamsDao.queryBuilder();
			where = queryBuilder.where();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
			where = where.and();
			where = where.gt(RepStepParams.POSITION_FIELD_NAME, 8);
			preparedQuery = queryBuilder.prepare();

			for (RepStepParams repStepParams : repStepParamsDao.query(preparedQuery)) {
				repStepParams.setPosition(repStepParams.getPosition() - 2);
				repStepParamsDao.update(repStepParams);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160306.class, "Error while shifting clumped analysis RepStepParams", e);
			return false;
		}

		try {
			int analysisId = 2;		// icl analysis
			Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);
			
			DeleteBuilder<RepStepParams,Integer> deleteBuilder = repStepParamsDao.deleteBuilder();
			Where<RepStepParams,Integer> where = deleteBuilder.where();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
			where = where.and();
			where = where.eq(RepStepParams.POSITION_FIELD_NAME, 7);
			PreparedDelete<RepStepParams> preparedDelete = deleteBuilder.prepare();
			repStepParamsDao.delete(preparedDelete);

			QueryBuilder<RepStepParams,Integer> queryBuilder = repStepParamsDao.queryBuilder();
			where = queryBuilder.where();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
			where = where.and();
			where = where.gt(RepStepParams.POSITION_FIELD_NAME, 7);
			PreparedQuery<RepStepParams> preparedQuery = queryBuilder.prepare();

			for (RepStepParams repStepParams : repStepParamsDao.query(preparedQuery)) {
				repStepParams.setPosition(repStepParams.getPosition() - 1);
				repStepParamsDao.update(repStepParams);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160306.class, "Error while shifting icl analysis RepStepParams", e);
			return false;
		}

		try {
			int analysisId = 3;		// eth analysis
			Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);
			
			DeleteBuilder<RepStepParams,Integer> deleteBuilder = repStepParamsDao.deleteBuilder();
			Where<RepStepParams,Integer> where = deleteBuilder.where();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
			where = where.and();
			where = where.eq(RepStepParams.POSITION_FIELD_NAME, 7);
			PreparedDelete<RepStepParams> preparedDelete = deleteBuilder.prepare();
			repStepParamsDao.delete(preparedDelete);

			QueryBuilder<RepStepParams,Integer> queryBuilder = repStepParamsDao.queryBuilder();
			where = queryBuilder.where();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, analysisId);
			where = where.and();
			where = where.gt(RepStepParams.POSITION_FIELD_NAME, 7);
			PreparedQuery<RepStepParams> preparedQuery = queryBuilder.prepare();

			for (RepStepParams repStepParams : repStepParamsDao.query(preparedQuery)) {
				repStepParams.setPosition(repStepParams.getPosition() - 1);
				repStepParamsDao.update(repStepParams);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160306.class, "Error while shifting icl analysis RepStepParams", e);
			return false;
		}

		try {
			Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);

			for (RepStepParams repStepParams : repStepParamsDao.queryForAll()) {
				repStepParams.setPosition(repStepParams.getPosition() + 1);
				repStepParamsDao.update(repStepParams);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160306.class, "Error while shifting all analysis RepStepParams", e);
			return false;
		}

		rebuildAnalyses = true;

		return true;
	}
}
