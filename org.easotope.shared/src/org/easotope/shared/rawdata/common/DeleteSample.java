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
import java.util.ArrayList;

import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class DeleteSample {
	public static ArrayList<ReplicateV1> deleteSample(ConnectionSource connectionSource, RawFileManager rawFileManager, int sampleId) throws SQLException {
		Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);

		Sample sample = sampleDao.queryForId(sampleId);

		if (sample == null) {
			return null;
		}

		return deleteSample(connectionSource, rawFileManager, sample);
	}

	public static ArrayList<ReplicateV1> deleteSample(ConnectionSource connectionSource, RawFileManager rawFileManager, Sample sample) throws SQLException {
		Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
		ArrayList<ReplicateV1> result = new ArrayList<ReplicateV1>();

		for (ReplicateV1 replicate : replicateDao.queryForEq(ReplicateV1.SAMPLEID_FIELD_NAME, sample.getId())) {
			ReplicateV1 deletedReplicate = DeleteReplicate.deleteReplicate(connectionSource, rawFileManager, replicate.getId());
			result.add(deletedReplicate);
		}

		Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
		sampleDao.delete(sample);

		return result;
	}
}
