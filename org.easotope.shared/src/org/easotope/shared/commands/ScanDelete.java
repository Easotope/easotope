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

package org.easotope.shared.commands;

import java.util.Hashtable;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcByTime;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.events.ScanDeleted;
import org.easotope.shared.rawdata.tables.ScanFileInputV0;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;
import org.easotope.shared.rawdata.tables.ScanV2;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class ScanDelete extends Command {
	private static final long serialVersionUID = 1L;

	private int scanId;
	private transient ScanV2 scan;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		Dao<ScanV2,Integer> scanDao = DaoManager.createDao(connectionSource, ScanV2.class);
		scan = scanDao.queryForId(scanId);

		if (scan == null) {
			return false;
		}

		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);

		return permissions.isCanDeleteAll() || (permissions.isCanDeleteOwn() && user.getId() == scan.getUserId());
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		Dao<ScanV2,Integer> scanDao = DaoManager.createDao(connectionSource, ScanV2.class);
		scanDao.deleteById(scanId);

		Dao<ScanFileInputV0,Integer> scanFileInputDao = DaoManager.createDao(connectionSource, ScanFileInputV0.class);
		Dao<ScanFileParsedV2,Integer> scanFileParsedDao = DaoManager.createDao(connectionSource, ScanFileParsedV2.class);
		Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);

		for (ScanFileInputV0 scanFileInput : scanFileInputDao.queryForEq(ScanFileInputV0.SCANID_FIELD_NAME, scanId)) {
			scanFileParsedDao.deleteById(scanFileInput.getScanFileParsedId());
			scanFileInputDao.deleteById(scanFileInput.getId());

			RawFile rawFile = rawFileDao.queryForId(scanFileInput.getRawFileId());
			rawFileManager.deleteRawFile(rawFile.getDatabaseName());
			rawFileDao.deleteById(rawFile.getId());
		}

		CorrIntervalsNeedRecalcByTime corrIntervalsNeedRecalcByTime = new CorrIntervalsNeedRecalcByTime();
		corrIntervalsNeedRecalcByTime.addTime(scan.getMassSpecId(), scan.getDate());
		addEvent(corrIntervalsNeedRecalcByTime);

		addEvent(new ScanDeleted(scanId));
	}

	public void setScanId(int scanId) {
		this.scanId = scanId;
	}
}
