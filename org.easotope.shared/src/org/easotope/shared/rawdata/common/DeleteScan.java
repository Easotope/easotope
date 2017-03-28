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

package org.easotope.shared.rawdata.common;

import java.sql.SQLException;

import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.rawdata.tables.ScanFileInputV0;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;
import org.easotope.shared.rawdata.tables.ScanV2;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class DeleteScan {
	public static ScanV2 deleteScan(ConnectionSource connectionSource, RawFileManager rawFileManager, int scanId) throws SQLException {
		Dao<ScanV2,Integer> scanDao = DaoManager.createDao(connectionSource, ScanV2.class);

		ScanV2 scan = scanDao.queryForId(scanId);

		if (scan == null) {
			return null;
		}

		return deleteScan(connectionSource, rawFileManager, scan);
	}

	public static ScanV2 deleteScan(ConnectionSource connectionSource, RawFileManager rawFileManager, ScanV2 scan) throws SQLException {
		Dao<ScanV2,Integer> scanDao = DaoManager.createDao(connectionSource, ScanV2.class);
		scanDao.deleteById(scan.getId());

		Dao<ScanFileInputV0,Integer> scanFileInputDao = DaoManager.createDao(connectionSource, ScanFileInputV0.class);
		Dao<ScanFileParsedV2,Integer> scanFileParsedDao = DaoManager.createDao(connectionSource, ScanFileParsedV2.class);
		Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);

		for (ScanFileInputV0 scanFileInput : scanFileInputDao.queryForEq(ScanFileInputV0.SCANID_FIELD_NAME, scan.getId())) {
			scanFileParsedDao.deleteById(scanFileInput.getScanFileParsedId());
			scanFileInputDao.deleteById(scanFileInput.getId());

			if (scanFileInputDao.queryForEq(ScanFileInputV0.RAWFILEID_FIELD_NAME, scanFileInput.getRawFileId()).size() == 0) {
				RawFile rawFile = rawFileDao.queryForId(scanFileInput.getRawFileId());
				rawFileManager.deleteRawFile(rawFile.getDatabaseName());
				rawFileDao.deleteById(rawFile.getId());
			}
		}

		return scan;
	}
}
