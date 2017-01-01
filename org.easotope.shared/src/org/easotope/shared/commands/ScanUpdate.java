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

package org.easotope.shared.commands;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcByTime;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.ScanFile;
import org.easotope.shared.rawdata.compute.ComputeScanFileParsed;
import org.easotope.shared.rawdata.events.ScanUpdated;
import org.easotope.shared.rawdata.tables.ScanFileInputV0;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;
import org.easotope.shared.rawdata.tables.ScanV2;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class ScanUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private ScanV2 scan;
	private ArrayList<ScanFile> scanFiles;
	private String name;

	@Override
	public String getName() {
		return (name != null) ? name : getClass().getSimpleName() + "(id=" + scan.getId() + ", date=" + scan.getDate() + ")";
	}

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		return true;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<ScanFileInputV0,Integer> scanFileInputDao = DaoManager.createDao(connectionSource, ScanFileInputV0.class);
		Dao<ScanFileParsedV2,Integer> scanFileParsedDao = DaoManager.createDao(connectionSource, ScanFileParsedV2.class);
		Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);

		String duplicates = getDuplicates(rawFileManager, rawFileDao, scanFileInputDao, scanFileParsedDao);

		if (!duplicates.isEmpty()) {
			String message = MessageFormat.format(Messages.scanUpdate_duplicateScan, duplicates);
			setStatus(Command.Status.EXECUTION_ERROR, message);
			return;
		}

		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		int userId = permissions.isCanEditAllReplicates() ? scan.getUserId() : user.id;
		scan.setUserId(userId);

		Dao<ScanV2,Integer> scanDao = DaoManager.createDao(connectionSource, ScanV2.class);
		CorrIntervalsNeedRecalcByTime corrIntervalsNeedRecalc = new CorrIntervalsNeedRecalcByTime();
		addEvent(corrIntervalsNeedRecalc);

		if (scan.getId() == DatabaseConstants.EMPTY_DB_ID) {
			scanDao.create(scan);

		} else {
			ScanV2 oldScan = scanDao.queryForId(scan.getId());

			if (oldScan == null) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.scanUpdate_doesNotExist, new Object[] { scan.getId() });
				return;
			}

			corrIntervalsNeedRecalc.addTime(oldScan.getMassSpecId(), oldScan.getDate());
			scanDao.update(scan);
		}

		corrIntervalsNeedRecalc.addTime(scan.getMassSpecId(), scan.getDate());

		for (ScanFile scanFile : scanFiles) {
			if (scanFile.getFileBytes() != null) {
				byte[] bytes = scanFile.getFileBytes();
				String filename = rawFileManager.writeRawFile(bytes);

				RawFile rawFile = scanFile.getRawFile();
				rawFile.setUserId(userId);
				rawFile.setDatabaseName(filename);
				rawFileDao.create(rawFile);

				ScanFileParsedV2 scanFileParsed = ComputeScanFileParsed.compute(rawFile, bytes, false);
				scanFileParsedDao.create(scanFileParsed);

				ScanFileInputV0 scanFileInput = new ScanFileInputV0();
				scanFileInput.setRawFileId(rawFile.getId());
				scanFileInput.setScanId(scan.getId());
				scanFileInput.setScanFileParsedId(scanFileParsed.getId());
				scanFileInputDao.create(scanFileInput);

				scanFile.setScanFileParsed(scanFileParsed);
				scanFile.setFileBytes(null);
			}
		}

		addEvent(new ScanUpdated(scan.getId(), scan.getDate(), scan.getMassSpecId(), scan.getUserId()));

		name = getClass().getSimpleName() + "(id=" + scan.getId() + ", date=" + scan.getDate() + ")";
		scan = null;
		scanFiles = null;
	}

	private String getDuplicates(RawFileManager rawFileManager, Dao<RawFile,Integer> rawFileDao, Dao<ScanFileInputV0,Integer> scanFileInputDao, Dao<ScanFileParsedV2,Integer> scanFileParsedDao) throws Exception {
		String result = "";

		nextScanFile:
		for (ScanFile scanFile : scanFiles) {
			if (scanFile.getFileBytes() != null) {
				List<ScanFileParsedV2> scanFileParseds = scanFileParsedDao.queryForEq(ScanFileParsedV2.DATE_FIELD_NAME, scanFile.getScanFileParsed().getDate());

				if (scanFileParseds != null) {
					for (ScanFileParsedV2 scanFileParsed : scanFileParseds) {
						List<ScanFileInputV0> scanFileInputs = scanFileInputDao.queryForEq(ScanFileInputV0.SCAN_FILE_PARSED_ID_FIELD_NAME, scanFileParsed.getId());
						ScanFileInputV0 scanFileInput = scanFileInputs.get(0);

						RawFile rawFile = rawFileDao.queryForId(scanFileInput.getRawFileId());
						byte[] oldFileBytes = rawFileManager.readRawFile(rawFile.getDatabaseName());

						if (Arrays.equals(scanFile.getFileBytes(), oldFileBytes)) {
							result += result.isEmpty() ? rawFile.getOriginalName() : ", " + rawFile.getOriginalName();
							continue nextScanFile;
						}
					}
				}
			}
		}

		return result;
	}

	public void setScan(ScanV2 scan) {
		this.scan = scan;
	}

	public void setScanFiles(ArrayList<ScanFile> scanFiles) {
		this.scanFiles = scanFiles;
	}
}
