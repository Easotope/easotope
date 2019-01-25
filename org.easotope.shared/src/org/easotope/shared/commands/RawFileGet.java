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

package org.easotope.shared.commands;

import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.ScanFileInputV0;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class RawFileGet extends Command {
	private static final long serialVersionUID = 1L;

	private int rawFileId;

	private RawFile rawFile;
	private byte[] fileBytes;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);
		rawFile = rawFileDao.queryForId(rawFileId);

		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);

		if (rawFile.getUserId() != user.getId() && !permissions.isCanEditAllReplicates() && !fileIsStandardRun(connectionSource, rawFile) && !fileIsScanFile(connectionSource, rawFile)) {
			rawFile = null;
			return false;
		}

		return true;
	}

	private boolean fileIsStandardRun(ConnectionSource connectionSource, RawFile rawFile) throws Exception {
		Dao<AcquisitionInputV0,Integer> acquisitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInputV0.class);
		List<AcquisitionInputV0> foundAcquisitions = acquisitionInputDao.queryForEq(AcquisitionInputV0.RAWFILEID_FIELD_NAME, rawFile.getId());

		if (foundAcquisitions != null && foundAcquisitions.size() != 0) {
			AcquisitionInputV0 acquisitionInput = foundAcquisitions.get(0);

			Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
			ReplicateV1 replicate = replicateDao.queryForId(acquisitionInput.getReplicateId());

			return replicate != null && replicate.getStandardId() != DatabaseConstants.EMPTY_DB_ID;
		}

		return false;
	}

	private boolean fileIsScanFile(ConnectionSource connectionSource, RawFile rawFile) throws Exception {
		Dao<ScanFileInputV0,Integer> scanFileParsedDao = DaoManager.createDao(connectionSource, ScanFileInputV0.class);
		List<ScanFileInputV0> foundScanFiles = scanFileParsedDao.queryForEq(ScanFileInputV0.RAWFILEID_FIELD_NAME, rawFile.getId());

		return foundScanFiles != null && foundScanFiles.size() != 0;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		fileBytes = rawFileManager.readRawFile(rawFile.getDatabaseName());
	}

	public int getRawFileId() {
		return rawFileId;
	}

	public void setRawFileId(int rawFileId) {
		this.rawFileId = rawFileId;
	}

	public RawFile getRawFile() {
		return rawFile;
	}

	public byte[] getFileBytes() {
		return fileBytes;
	}
}
