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
import org.easotope.shared.rawdata.tables.old.ScanV2;
import org.easotope.shared.rawdata.tables.old.ScanV1;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class Upgrade20160704 extends DatabaseUpgrade {
	@Override
	public int appliesToVersion() {
		return 20160704;
	}

	@Override
	public int resultsInVersion() {
		return 20160805;
	}

	@Override
	public boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource, int originalServerVersion) {
		try {
			Dao<Preferences,Integer> scanV2Dao = DaoManager.createDao(connectionSource, Preferences.class);
			scanV2Dao.executeRaw("ALTER TABLE " + Preferences.TABLE_NAME + " ADD COLUMN " + Preferences.PREVIOUSBESTSERVER_FIELD_NAME + " INT DEFAULT 0;");

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while adding PREVIOUSBESTSERVER to Preferences table.", e);
			return false;
		}

		try {
			Dao<ScanV2,Integer> scanV2Dao = DaoManager.createDao(connectionSource, ScanV2.class);
			scanV2Dao.executeRaw("ALTER TABLE " + ScanV1.TABLE_NAME + " ADD COLUMN " + ScanV2.DEGREE_OF_FIT + " BLOB;");
			scanV2Dao.executeRaw("ALTER TABLE " + ScanV1.TABLE_NAME + " ADD COLUMN " + ScanV2.X2COEFF + " BLOB;");
			scanV2Dao.executeRaw("ALTER TABLE " + ScanV1.TABLE_NAME + " ADD COLUMN " + ScanV2.REFERENCE_CHANNEL + " BLOB;");
			scanV2Dao.executeRaw("ALTER TABLE " + ScanV1.TABLE_NAME + " ADD COLUMN " + ScanV2.ON_PEAK_X1_FIELD_NAME + " DOUBLE;");
			scanV2Dao.executeRaw("ALTER TABLE " + ScanV1.TABLE_NAME + " ADD COLUMN " + ScanV2.ON_PEAK_X2_FIELD_NAME + " DOUBLE;");
			scanV2Dao.executeRaw("ALTER TABLE " + ScanV1.TABLE_NAME + " RENAME TO " + ScanV2.TABLE_NAME);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while adding DEGREE_OF_FIT, X2, and BASIS_CHANNELS to Scan table.", e);
			return false;
		}

		try {
			Dao<ScanV2,Integer> scanV2Dao = DaoManager.createDao(connectionSource, ScanV2.class);

			for (ScanV2 scanV2 : scanV2Dao.queryForAll()) {
				scanV2.setOnPeakX1(Double.NaN);
				scanV2.setOnPeakX2(Double.NaN);

				Double[] slopes = scanV2.getSlope();
				int numOfTabs = slopes.length;

				if (slopes != null) {
					Double[] x2 = new Double[slopes.length];

					for (int i=0; i<x2.length; i++) {
						x2[i] = 0.0d;
					}

					scanV2.setX2Coeff(x2);
				}

				Integer[] channelToMzX10 = scanV2.getChannelToMzX10();

				if (channelToMzX10 != null) {
					int channel440 = -1;

					for (int channelNum=0; channelNum<channelToMzX10.length; channelNum++) {
						if (channelToMzX10[channelNum] != null && channelToMzX10[channelNum] == 440) {
							channel440 = channelNum;
						}
					}

					if (channel440 != -1) {
						Integer[] referenceChannel = new Integer[numOfTabs];

						for (int i=0; i<referenceChannel.length; i++) {
							referenceChannel[i] = channel440;
						}

						scanV2.setReferenceChannel(referenceChannel);
					}

					int[] degreeOfFit = new int[numOfTabs];

					for (int i=0; i<degreeOfFit.length; i++) {
						degreeOfFit[i] = 1;
					}

					scanV2.setDegreeOfFit(degreeOfFit);
				}

				scanV2Dao.update(scanV2);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while setting DEGREE_OF_FIT, X2, and BASIS_CHANNELS in Scan table.", e);
			return false;
		}

		rebuildAnalyses = true;
		return true;
	}
}
