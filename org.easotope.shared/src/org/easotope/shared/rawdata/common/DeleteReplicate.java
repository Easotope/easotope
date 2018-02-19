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

package org.easotope.shared.rawdata.common;

import java.sql.SQLException;

import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;
import org.easotope.shared.rawdata.tables.ReplicateV1;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class DeleteReplicate {
	public static ReplicateV1 deleteReplicate(ConnectionSource connectionSource, RawFileManager rawFileManager, int replicateId) throws SQLException {
		Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);

		ReplicateV1 replicate = replicateDao.queryForId(replicateId);

		if (replicate == null) {
			return null;
		}

		return deleteReplicate(connectionSource, rawFileManager, replicate);
	}

	public static ReplicateV1 deleteReplicate(ConnectionSource connectionSource, RawFileManager rawFileManager, ReplicateV1 replicate) throws SQLException {
		Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
		replicateDao.deleteById(replicate.getId());

		Dao<AcquisitionInputV0,Integer> acquisitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInputV0.class);
		Dao<AcquisitionParsedV2,Integer> acquisitionParsedDao = DaoManager.createDao(connectionSource, AcquisitionParsedV2.class);
		Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);

		for (AcquisitionInputV0 acquisitionInput : acquisitionInputDao.queryForEq(AcquisitionInputV0.REPLICATEID_FIELD_NAME, replicate.getId())) {
			acquisitionInputDao.deleteById(acquisitionInput.getId());
			acquisitionParsedDao.deleteById(acquisitionInput.getAcquisitionParsedId());

			if (acquisitionInputDao.queryForEq(AcquisitionInputV0.RAWFILEID_FIELD_NAME, acquisitionInput.getRawFileId()).size() == 0) {
				RawFile rawFile = rawFileDao.queryForId(acquisitionInput.getRawFileId());

				if (rawFile != null) {
					rawFileManager.deleteRawFile(rawFile.getDatabaseName());
					rawFileDao.deleteById(rawFile.getId());
				}				
			}
		}

		return replicate;
	}
}
