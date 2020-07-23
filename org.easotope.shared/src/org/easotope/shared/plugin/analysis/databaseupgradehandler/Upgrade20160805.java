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

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.ScanFileInputV0;
import org.easotope.shared.rawdata.tables.old.AcquisitionInput;
import org.easotope.shared.rawdata.tables.old.ScanFileParsedV0;
import org.easotope.shared.rawdata.tables.old.ScanFileParsedV1;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class Upgrade20160805 extends DatabaseUpgrade {
	@Override
	public int appliesToVersion() {
		return 20160805;
	}

	@Override
	public int resultsInVersion() {
		return 20161009;
	}

	@Override
	public boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource) {		
		try {
			Dao<Preferences,Integer> preferencesDao = DaoManager.createDao(connectionSource, Preferences.class);
			preferencesDao.executeRaw("ALTER TABLE " + Preferences.TABLE_NAME + " ADD COLUMN " + Preferences.LEADINGEXPONENT_FIELD_NAME + " TINYINT DEFAULT 1;");
			preferencesDao.executeRaw("ALTER TABLE " + Preferences.TABLE_NAME + " ADD COLUMN " + Preferences.FORCEEXPONENT_FIELD_NAME + " TINYINT DEFAULT 0;");

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160805.class, "Error while adding LEADINGEXPONENT or FORCEEXPONENT to Preferences table.", e);
			return false;
		}

		try {
			buildScanFileInputs(connectionSource);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160805.class, "Error while building ScanFileInput table.", e);
			return false;
		}

		rebuildScanFileParsed = true;

		try {
			Dao<AcquisitionInputV0,Integer> acquisitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInputV0.class);
			acquisitionInputDao.executeRaw("ALTER TABLE " + AcquisitionInput.TABLE_NAME + " ADD COLUMN " + AcquisitionInputV0.ACQUISITION_PARSED_ID_FIELD_NAME + " INT DEFAULT -1;");
			acquisitionInputDao.executeRaw("ALTER TABLE " + AcquisitionInput.TABLE_NAME + " ADD COLUMN " + AcquisitionInputV0.ASSUMED_TIME_ZONE_FIELD_NAME + " VARCHAR(255) DEFAULT NULL;");
			acquisitionInputDao.executeRaw("ALTER TABLE " + AcquisitionInput.TABLE_NAME + " ADD COLUMN " + AcquisitionInputV0.ACQUISITION_NUMBER_FIELD_NAME + " INT DEFAULT 0;");

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while modifying AcquisitionInput table.", e);
			return false;
		}

		rebuildAcquisitionsParsed = true;

		return true;
	}

	private void buildScanFileInputs(ConnectionSource connectionSource) throws Exception {
		TableUtils.createTable(connectionSource, ScanFileInputV0.class);
		Dao<ScanFileInputV0,Integer> scanFileInputV0Dao = DaoManager.createDao(connectionSource, ScanFileInputV0.class);

		try {
			Dao<ScanFileParsedV1,Integer> scanFileParsedV1Dao = DaoManager.createDao(connectionSource, ScanFileParsedV1.class);

			for (ScanFileParsedV1 scanFileParsed : scanFileParsedV1Dao) {
				ScanFileInputV0 scanFileInput = new ScanFileInputV0();
				scanFileInput.setRawFileId(scanFileParsed.getRawFileId());
				scanFileInput.setScanId(scanFileParsed.getScanId());
				scanFileInput.setScanFileParsedId(-1);
				scanFileInputV0Dao.create(scanFileInput);
			}

			return;

		} catch (Exception e) {
			// ignore
		}

		try {
			Dao<ScanFileParsedV0,Integer> scanFileParsedV0Dao = DaoManager.createDao(connectionSource, ScanFileParsedV0.class);

			for (ScanFileParsedV0 scanFileParsed : scanFileParsedV0Dao) {
				ScanFileInputV0 scanFileInput = new ScanFileInputV0();
				scanFileInput.setRawFileId(scanFileParsed.getRawFileId());
				scanFileInput.setScanId(scanFileParsed.getScanId());
				scanFileInput.setScanFileParsedId(-1);
				scanFileInputV0Dao.create(scanFileInput);
			}

			return;

		} catch (Exception e) {
			// ignore
		}
	}
}
