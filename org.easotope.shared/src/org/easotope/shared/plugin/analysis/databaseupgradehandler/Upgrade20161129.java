/*
 * Copyright © 2016-2018 by Devon Bowen.
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
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.ReplicateV1;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class Upgrade20161129 extends DatabaseUpgrade {
	@Override
	public int appliesToVersion() {
		return 20161129;
	}

	@Override
	public int resultsInVersion() {
		return 20170101;
	}

	@Override
	public boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource) {
		try {
			Dao<Permissions,Integer> permissionsDao = DaoManager.createDao(connectionSource, Permissions.class);
			permissionsDao.executeRaw("ALTER TABLE " + Permissions.TABLE_NAME + " ADD COLUMN " + Permissions.CANBATCHIMPORT_FIELD_NAME + " TINYINT DEFAULT 0 NOT NULL;");

			Dao<User,Integer> userDao = DaoManager.createDao(connectionSource, User.class);

			for (User user : userDao.queryForEq(User.ISADMIN_FIELD_NAME, true)) {
				permissionsDao.executeRaw("UPDATE " + Permissions.TABLE_NAME + " SET " + Permissions.CANBATCHIMPORT_FIELD_NAME + "=1 WHERE " + Permissions.USERID_FIELD_NAME + "=" + user.getId());
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20161129.class, "Error while adding CANBATCHIMPORT to Permissions table.", e);
			return false;
		}

		try {
			Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
			Dao<AcquisitionInputV0,Integer> acqusitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInputV0.class);

			for (ReplicateV1 replicate : replicateDao.queryForAll()) {
				boolean allAcquisitionsDisabled = true;

				for (AcquisitionInputV0 acquisitionInput : acqusitionInputDao.queryForEq(AcquisitionInputV0.REPLICATEID_FIELD_NAME, replicate.getId())) {
					if (!acquisitionInput.isDisabled()) {
						allAcquisitionsDisabled = false;
					}
				}

				if (allAcquisitionsDisabled && !replicate.isDisabled()) {
					replicate.setDisabled(true);
					replicateDao.update(replicate);

					Log.getInstance().log(Level.INFO, Upgrade20161129.class, "Disabling replicate " + replicate.getId());
				}
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20161129.class, "Error while fixing replicate disabled flags.", e);
			return false;
		}
		
		rebuildScanFileParsed = true;
		rebuildAcquisitionsParsed = true;
		rebuildAnalyses = true;

		return true;
	}
}
