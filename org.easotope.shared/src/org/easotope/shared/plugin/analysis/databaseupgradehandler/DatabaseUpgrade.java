/*
 * Copyright © 2016-2023 by Devon Bowen.
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

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.tables.Version;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.plugin.analysis.initializedhandler.AnalysesCreator;
import org.easotope.shared.rawdata.compute.ComputeAcquisitionParsed;
import org.easotope.shared.rawdata.compute.ComputeScanFileParsed;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.ScanFileInputV0;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;
import org.easotope.shared.rawdata.tables.ScanV3;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public abstract class DatabaseUpgrade {
	public abstract int appliesToVersion();
	public abstract int resultsInVersion();
	public abstract boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource, int originalServerVersion);

	public static boolean rebuildScanFileParsed = false;
	public static boolean rebuildAcquisitionsParsed = false;
	public static boolean rebuildAnalyses = false;

	public static void upgradeFromVersion(int originalServerVersion, boolean reparseAcquisitions, RawFileManager rawFileManager, ConnectionSource connectionSource) {
		int currentServerVersion = originalServerVersion;
		rebuildAcquisitionsParsed = rebuildAcquisitionsParsed || reparseAcquisitions;

		for (DatabaseUpgrade databaseUpgrade : list) {
			if (databaseUpgrade.appliesToVersion() == currentServerVersion) {
				Log.getInstance().log(Level.INFO, "Upgrading database from version " + databaseUpgrade.appliesToVersion());

				try {
					if (!databaseUpgrade.upgrade(rawFileManager, connectionSource, originalServerVersion)) {
						throw new Exception("Upgrade returned false.");
					}
				} catch (Exception e) {
					Log.getInstance().log(Level.TERMINAL, DatabaseUpgrade.class, "Database upgrade from version " + databaseUpgrade.appliesToVersion() + " to version " + databaseUpgrade.resultsInVersion() + " failed. Giving up.", e);
					return;
				}

				currentServerVersion = databaseUpgrade.resultsInVersion();

				try {
					Dao<Version,Integer> versionDao = DaoManager.createDao(connectionSource, Version.class);
					Version version = versionDao.queryForId(1);
					version.setLastServerVersion(currentServerVersion);
					versionDao.update(version);

				} catch (SQLException e) {
					Log.getInstance().log(Level.TERMINAL, DatabaseUpgrade.class, "Failed to update version table. Giving up.", e);
				}
			}
		}

		if (rebuildScanFileParsed) {
			if (!rebuildScanFileParsed(rawFileManager, connectionSource)) {
				Log.getInstance().log(Level.TERMINAL, DatabaseUpgrade.class, "Failed to reparse ScanFileParsed table. Giving up.");
			}
		}

		if (rebuildAcquisitionsParsed) {
			if (!rebuildAcquisitionsParsed(rawFileManager, connectionSource)) {
				Log.getInstance().log(Level.TERMINAL, DatabaseUpgrade.class, "Failed to reparse AcquisitionParsed table. Giving up.");
			}
		}

		if (rebuildAnalyses) {
			if (!AnalysesCreator.create(connectionSource)) {
				Log.getInstance().log(Level.TERMINAL, DatabaseUpgrade.class, "Failed to recreate analyses tables. Giving up.");
			}
		}
	}

	private static void removeAllOldTables(ConnectionSource connectionSource, Class<?> newestTableClass) throws Exception {
		Pattern pattern = Pattern.compile("^(.*)\\.(\\w+)(\\d+)$");
		Matcher matcher = pattern.matcher(newestTableClass.getName());

		if (!matcher.matches()) {
			throw new Exception("Couldn't parse class name " + newestTableClass.getName());
		}

		String newPackage = matcher.group(1) + ".";
		String oldPackage = newPackage + "old.";
		String classPrefix = matcher.group(2);
		int highestKnownVersion = Integer.parseInt(matcher.group(3));

		for (int i=0; i<highestKnownVersion; i++) {
			Class<?> clazz = classForName(oldPackage + classPrefix + i);

			if (clazz != null) {
				TableUtils.dropTable(connectionSource, clazz, true);
			}
		}

		TableUtils.dropTable(connectionSource, newestTableClass, true);
	}

	private static boolean rebuildScanFileParsed(RawFileManager rawFileManager, ConnectionSource connectionSource) {
		Log.getInstance().log(Level.INFO, DatabaseUpgrade.class, "Rescanning scan files...");

		try {
			removeAllOldTables(connectionSource, ScanFileParsedV2.class);
			TableUtils.createTable(connectionSource, ScanFileParsedV2.class);

			Dao<ScanV3,Integer> scanDao = DaoManager.createDao(connectionSource, ScanV3.class);
			Dao<ScanFileInputV0,Integer> scanFileInputDao = DaoManager.createDao(connectionSource, ScanFileInputV0.class);
			Dao<ScanFileParsedV2,Integer> scanFileParsedDao = DaoManager.createDao(connectionSource, ScanFileParsedV2.class);
			Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);

			CloseableIterator<ScanV3> scanIterator = scanDao.iterator();

			while (scanIterator.hasNext()) {
				ScanV3 scan = scanIterator.next();

				for (ScanFileInputV0 scanFileInput : scanFileInputDao.queryForEq(ScanFileInputV0.SCANID_FIELD_NAME, scan.getId())) {
					RawFile rawFile = rawFileDao.queryForId(scanFileInput.getRawFileId());
					byte[] fileBytes = rawFileManager.readRawFile(rawFile.getDatabaseName());

					ScanFileParsedV2 scanFileParsed = ComputeScanFileParsed.compute(rawFile, fileBytes, true);
					scanFileParsedDao.create(scanFileParsed);

					scanFileInput.setScanFileParsedId(scanFileParsed.getId());
					scanFileInputDao.update(scanFileInput);
				}
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, DatabaseUpgrade.class, "Error while reparsing scan tables.", e);
			return false;
		}
		
		return true;
	}

	private static boolean rebuildAcquisitionsParsed(RawFileManager rawFileManager, ConnectionSource connectionSource) {
		Log.getInstance().log(Level.INFO, DatabaseUpgrade.class, "Rescanning replicate files...");

		try {
			removeAllOldTables(connectionSource, AcquisitionParsedV2.class);
			TableUtils.createTable(connectionSource, AcquisitionParsedV2.class);

			Dao<ReplicateV1,Integer> replicateParsedDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
			Dao<AcquisitionInputV0,Integer> acquisitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInputV0.class);
			Dao<AcquisitionParsedV2,Integer> acquisitionParsedDao = DaoManager.createDao(connectionSource, AcquisitionParsedV2.class);
			Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);

			CloseableIterator<ReplicateV1> replicateIterator = replicateParsedDao.iterator();

			while (replicateIterator.hasNext()) {
				ReplicateV1 replicate = replicateIterator.next();

				for (AcquisitionInputV0 acquisitionInput : acquisitionInputDao.queryForEq(AcquisitionInputV0.REPLICATEID_FIELD_NAME, replicate.getId())) {
					RawFile rawFile = rawFileDao.queryForId(acquisitionInput.getRawFileId());
					byte[] fileBytes = rawFileManager.readRawFile(rawFile.getDatabaseName());

					ComputeAcquisitionParsed computeAcquisitionParsed = new ComputeAcquisitionParsed(rawFile, fileBytes, true, acquisitionInput.getAssumedTimeZone());
					AcquisitionParsedV2 acquisitionParsed = computeAcquisitionParsed.getMaps().get(acquisitionInput.getAcquisitionNumber());
					acquisitionParsedDao.create(acquisitionParsed);

					acquisitionInput.setAcquisitionParsedId(acquisitionParsed.getId());
					acquisitionInputDao.update(acquisitionInput);
				}

				replicateParsedDao.update(replicate);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, DatabaseUpgrade.class, "Error while reparsing acquisition tables.", e);
			return false;
		}

		return true;
	}

	private static Class<?> classForName(String className) {
		try {
			return Class.forName(className);
		} catch (Exception e) {
			// ignore
		}
		return null;
	}

	static DatabaseUpgrade[] list = {
		new Upgrade0(),
		new Upgrade20160105(),
		new Upgrade20160204(),
		new Upgrade20160218(),
		new Upgrade20160306(),
		new Upgrade20160531(),
		new Upgrade20160704(),
		new Upgrade20160805(),
		new Upgrade20161009(),
		new Upgrade20161129(),
		new Upgrade20170101(),
		new Upgrade20170120(),
		new Upgrade20170222(),
		new Upgrade20170328(),
		new Upgrade20180218(),
		new Upgrade20180724(),
		new Upgrade20190125(),
		new Upgrade20200723(),
		new Upgrade20201112(),
		new Upgrade20201231(),
		new Upgrade20230220()
	};
}
