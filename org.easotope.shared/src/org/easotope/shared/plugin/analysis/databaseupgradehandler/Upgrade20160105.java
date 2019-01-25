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

package org.easotope.shared.plugin.analysis.databaseupgradehandler;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.analysis.tables.old.CorrIntervalV0;
import org.easotope.shared.rawdata.compute.ComputeAcquisitionParsed;
import org.easotope.shared.rawdata.compute.ComputeScanFileParsed;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;
import org.easotope.shared.rawdata.tables.old.AcquisitionInput;
import org.easotope.shared.rawdata.tables.old.AcquisitionParsedV0;
import org.easotope.shared.rawdata.tables.old.ReplicateV0;
import org.easotope.shared.rawdata.tables.old.ScanFileParsedV0;
import org.easotope.shared.rawdata.tables.old.ScanV0;
import org.easotope.shared.rawdata.tables.old.ScanV1;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;

public class Upgrade20160105 extends DatabaseUpgrade {
	@Override
	public int appliesToVersion() {
		return 20160105;
	}

	@Override
	public int resultsInVersion() {
		return 20160218;
	}

	@Override
	public boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource) {
		try {
			Dao<ScanV0,Integer> scanV0Dao = DaoManager.createDao(connectionSource, ScanV0.class);
			scanV0Dao.executeRaw("ALTER TABLE " + ScanV0.TABLE_NAME + " DROP COLUMN " + ScanV0.INPUT_PARAMETERS + ";");
			scanV0Dao.executeRaw("ALTER TABLE " + ScanV0.TABLE_NAME + " ADD COLUMN " + ScanV1.CHANNEL_TO_MZX10_FIELD_NAME + " BLOB;");
			scanV0Dao.executeRaw("ALTER TABLE " + ScanV0.TABLE_NAME + " RENAME TO " + ScanV1.TABLE_NAME);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while adding CHANNELTOMZX10 to Scan table.", e);
			return false;
		}

		try {
			Dao<ScanV1,Integer> scanDao = DaoManager.createDao(connectionSource, ScanV1.class);
			Dao<ScanFileParsedV0,Integer> scanFileParsedDao = DaoManager.createDao(connectionSource, ScanFileParsedV0.class);
			Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);

			CloseableIterator<ScanV1> scanIterator = scanDao.iterator();

			while (scanIterator.hasNext()) {
				ScanV1 scan = scanIterator.next();

				GenericRawResults<String[]> rawResults = scanFileParsedDao.queryRaw("select " + ScanFileParsedV0.RAWFILEID_FIELD_NAME + " from " + ScanFileParsedV0.TABLE_NAME + " where " + ScanFileParsedV0.SCANID_FIELD_NAME + " = " + scan.getId());
				
				for (String[] result : rawResults.getResults()) {
					if (result == null || result.length == 0) {
						continue;
					}

					Integer rawFileId = null;

					try {
						rawFileId = Integer.parseInt(result[0]);
					} catch (NumberFormatException e) {
						// ignore
					}

					if (rawFileId == null) {
						continue;
					}

					RawFile rawFile = rawFileDao.queryForId(rawFileId);
					byte[] fileBytes = rawFileManager.readRawFile(rawFile.getDatabaseName());

					ScanFileParsedV2 newScanFileParsedV1 = ComputeScanFileParsed.compute(rawFile, fileBytes, true);

					if (scan.getChannelToMzX10() != null) {
						Integer[] scanArray = scan.getChannelToMzX10();
						Integer[] scanFileParsedArray = newScanFileParsedV1.getChannelToMzX10();

						if (scanFileParsedArray == null || scanFileParsedArray.length == 0) {
							Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Scan file parsed channel to mz is null or zero length");
							return false;
						}

						if (scanArray.length != scanFileParsedArray.length) {
							Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Scan channel to mz size " + scanArray.length + " does not equal scan file parsed size " + scanFileParsedArray.length);
							return false;
						}

						for (int i=0; i<scanArray.length; i++) {
							if (scanArray[i] != null && !scanArray[i].equals(scanFileParsedArray[i])) {
								Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Scan channel to mz entry " + i + " is " + scanArray[i] + " and does not equal scan file parsed " + scanFileParsedArray[i]);
								return false;
							}
						}

					} else {
						scan.setChannelToMzX10(newScanFileParsedV1.getChannelToMzX10());
					}

					rawResults = scanFileParsedDao.queryRaw("select " + ScanFileParsedV0.ID_FIELD_NAME + " from " + ScanFileParsedV0.TABLE_NAME + " where " + ScanFileParsedV0.RAWFILEID_FIELD_NAME + " = " + rawFile.getId());

					if (rawResults.getResults().size() != 1) {
						Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Wrong number of ScanFileParsed entries for raw file id " + rawFile.getId() + " - expected 1 but got " + rawResults.getResults().size());
						return false;
					}
				}

				scanDao.update(scan);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while reparsing scan tables.", e);
			return false;
		}

		rebuildScanFileParsed = true;

		try {
			Dao<CorrIntervalV0,Integer> corrIntervalV0Dao = DaoManager.createDao(connectionSource, CorrIntervalV0.class);
			corrIntervalV0Dao.executeRaw("ALTER TABLE " + CorrIntervalV0.TABLE_NAME + " ADD COLUMN " + CorrIntervalV1.CHANNEL_TO_MZX10_FIELD_NAME + " BLOB;");
			corrIntervalV0Dao.executeRaw("ALTER TABLE " + CorrIntervalV0.TABLE_NAME + " RENAME TO " + CorrIntervalV1.TABLE_NAME);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while adding CHANNELTOMZX10 to CorrInterval table.", e);
			return false;
		}

		try {
			Dao<ReplicateV0,Integer> replicateV0Dao = DaoManager.createDao(connectionSource, ReplicateV0.class);
			replicateV0Dao.executeRaw("ALTER TABLE " + ReplicateV0.TABLE_NAME + " ADD COLUMN " + ReplicateV1.CHANNEL_TO_MZX10_FIELD_NAME + " BLOB;");
			replicateV0Dao.executeRaw("ALTER TABLE " + ReplicateV0.TABLE_NAME + " RENAME TO " + ReplicateV1.TABLE_NAME);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while adding CHANNELTOMZX10 to Replicate table.", e);
			return false;
		}

		try {
			Dao<ReplicateV1,Integer> replicateParsedDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
			Dao<AcquisitionInput,Integer> acquisitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInput.class);
			Dao<AcquisitionParsedV0,Integer> acquisitionParsedDao = DaoManager.createDao(connectionSource, AcquisitionParsedV0.class);
			Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);

			CloseableIterator<ReplicateV1> replicateIterator = replicateParsedDao.iterator();

			while (replicateIterator.hasNext()) {
				ReplicateV1 replicate = replicateIterator.next();

				for (AcquisitionInput acquisitionInput : acquisitionInputDao.queryForEq(AcquisitionInput.REPLICATEID_FIELD_NAME, replicate.getId())) {
					RawFile rawFile = rawFileDao.queryForId(acquisitionInput.getRawFileId());
					byte[] fileBytes = rawFileManager.readRawFile(rawFile.getDatabaseName());

					ComputeAcquisitionParsed computeAcquisitionParsed = new ComputeAcquisitionParsed(rawFile, fileBytes, true, null);
					AcquisitionParsedV2 newAcquisitionParsed = computeAcquisitionParsed.getMaps().get(0);

					if (replicate.getChannelToMzX10() != null) {
						Integer[] replicateArray = replicate.getChannelToMzX10();
						Integer[] acquisitionArray = newAcquisitionParsed.getChannelToMzX10();

						if (acquisitionArray == null || acquisitionArray.length == 0) {
							Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Acquisition channel to mz is null or zero length");
							return false;
						}

						if (replicateArray.length != acquisitionArray.length) {
							Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Replicate channel to mz size " + replicateArray.length + " does not equal acquisition size " + acquisitionArray.length);
							return false;
						}

						for (int i=0; i<replicateArray.length; i++) {
							if (!replicateArray[i].equals(acquisitionArray[i])) {
								Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Replicate channel to mz entry " + i + " is " + replicateArray[i] + " and does not equal acquisition " + acquisitionArray[i]);
								return false;
							}
						}

					} else {
						replicate.setChannelToMzX10(newAcquisitionParsed.getChannelToMzX10());
					}

					GenericRawResults<String[]> rawResults = acquisitionParsedDao.queryRaw("select " + AcquisitionParsedV0.ID_FIELD_NAME + " from " + AcquisitionParsedV0.TABLE_NAME + " where " + AcquisitionParsedV0.RAWFILEID_FIELD_NAME + " = " + rawFile.getId());

					if (rawResults.getResults().size() != 1) {
						Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Wrong number of AcquisitionParsed entries for raw file id " + rawFile.getId() + " - expected 1 but got " + rawResults.getResults().size());
						return false;
					}
				}

				replicateParsedDao.update(replicate);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while reparsing acquisition tables.", e);
			return false;
		}

		rebuildAcquisitionsParsed = true;

		return true;
	}
}
