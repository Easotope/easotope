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

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.tables.Options;
import org.easotope.framework.dbcore.tables.Options.OverviewResolution;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.rawdata.tables.ScanV3;
import org.easotope.shared.rawdata.tables.old.ScanV2;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class Upgrade20170328 extends DatabaseUpgrade {
	@Override
	public int appliesToVersion() {
		return 20170328;
	}

	@Override
	public int resultsInVersion() {
		return 20180218;
	}

	@Override
	public boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource, int originalServerVersion) {
		Log.getInstance().log(Level.INFO, Upgrade20170328.class, "Creating options table");

		try {
			TableUtils.createTable(connectionSource, Options.class);

			Dao<Options,Integer> optionsDao = DaoManager.createDao(connectionSource, Options.class);
			Options options = new Options();
			options.setOverviewResolution(OverviewResolution.REPLICATE);
			options.setIncludeStds(false);
			options.setConfidenceLevel(90.0);
			optionsDao.create(options);

		} catch (SQLException e) {
			Log.getInstance().log(Level.INFO, Upgrade20170328.class, "Problem encountered while creating options table", e);
			return false;
		}

		Log.getInstance().log(Level.INFO, Upgrade20170328.class, "Compressing raw files");

		if (!rawFileManager.upgrade20170328()) {
			Log.getInstance().log(Level.INFO, Upgrade20170328.class, "Problem encountered while compressing raw files");
			return false;
		}

		try {
			Dao<ScanV2,Integer> scanV2Dao = DaoManager.createDao(connectionSource, ScanV2.class);
			scanV2Dao.executeRaw("ALTER TABLE " + ScanV2.TABLE_NAME + " ADD COLUMN " + ScanV3.ALGORITHM + " BLOB;");
			scanV2Dao.executeRaw("ALTER TABLE " + ScanV2.TABLE_NAME + " ADD COLUMN " + ScanV3.REFERENCE_CHANNEL2 + " BLOB;");
			scanV2Dao.executeRaw("ALTER TABLE " + ScanV2.TABLE_NAME + " ADD COLUMN " + ScanV3.FACTOR2 + " BLOB;");
			scanV2Dao.executeRaw("ALTER TABLE " + ScanV2.TABLE_NAME + " RENAME TO " + ScanV3.TABLE_NAME);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20170328.class, "Error while adding ALGORITHM, REFERENCE_CHANNEL2, and FACTOR2 to Scan table.", e);
			return false;
		}

		try {
			Dao<ScanV3,Integer> scanV3Dao = DaoManager.createDao(connectionSource, ScanV3.class);

			for (ScanV3 scanV3 : scanV3Dao.queryForAll()) {
				int maxArraySize = 0;
				
				if (scanV3.getLeftBackgroundX1() != null) {
					maxArraySize = Math.max(maxArraySize, scanV3.getLeftBackgroundX1().length);
				}
				
				if (scanV3.getLeftBackgroundX2() != null) {
					maxArraySize = Math.max(maxArraySize, scanV3.getLeftBackgroundX2().length);
				}
				
				if (scanV3.getRightBackgroundX1() != null) {
					maxArraySize = Math.max(maxArraySize, scanV3.getRightBackgroundX1().length);
				}

				if (scanV3.getRightBackgroundX2() != null) {
					maxArraySize = Math.max(maxArraySize, scanV3.getRightBackgroundX2().length);
				}

				if (scanV3.getDegreeOfFit() != null) {
					maxArraySize = Math.max(maxArraySize, scanV3.getDegreeOfFit().length);
				}

				if (scanV3.getX2Coeff() != null) {
					maxArraySize = Math.max(maxArraySize, scanV3.getX2Coeff().length);
				}

				if (scanV3.getSlope() != null) {
					maxArraySize = Math.max(maxArraySize, scanV3.getSlope().length);
				}

				if (scanV3.getIntercept() != null) {
					maxArraySize = Math.max(maxArraySize, scanV3.getIntercept().length);
				}

				if (scanV3.getReferenceChannel() != null) {
					maxArraySize = Math.max(maxArraySize, scanV3.getReferenceChannel().length);
				}

				int[] algorithm = new int[maxArraySize];

				for (int i=0; i<maxArraySize; i++) {
					if (scanV3.getSlope() != null && scanV3.getSlope()[i] != null && !Double.isNaN(scanV3.getSlope()[i]) && scanV3.getIntercept() != null && scanV3.getIntercept()[i] != null && !Double.isNaN(scanV3.getIntercept()[i])) {
						algorithm[i] = 1;
					} else {
						algorithm[i] = 0;
					}
				}

				scanV3.setAlgorithm(algorithm);

				Integer[] referenceChannel2 = new Integer[maxArraySize];
				scanV3.setReferenceChannel2(referenceChannel2);

				Double[] factor2 = new Double[maxArraySize];

				for (int i=0; i<maxArraySize; i++) {
					factor2[i] = -1.0;
				}

				scanV3.setFactor2(factor2);

				scanV3Dao.update(scanV3);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20170328.class, "Error while setting ALGORITHM, REFERENCE_CHANNEL2, and FACTOR2 in Scan table.", e);
			return false;
		}

		rebuildScanFileParsed = true;
		rebuildAcquisitionsParsed = true;
		rebuildAnalyses = true;

		return true;
	}
}
