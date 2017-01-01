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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateList;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateListItem;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class ReplicateListGet extends Command {
	private static final long serialVersionUID = 1L;

	private boolean getSamples = true;
	private int sampleId = DatabaseConstants.EMPTY_DB_ID;
	private int massSpecId = DatabaseConstants.EMPTY_DB_ID;
	private int userId = DatabaseConstants.EMPTY_DB_ID;

	private ReplicateList replicateList = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);

		if (permissions.isCanEditAllReplicates()) {
			return true;
		}

		if (sampleId != DatabaseConstants.EMPTY_DB_ID) {
			Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
			Sample sample = sampleDao.queryForId(sampleId);
			User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
			return sample.getUserId() == user.id;
		}

		//TODO don't allow an empty user if we are asking for samples

		return true;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
		QueryBuilder<ReplicateV1,Integer> queryBuilder = replicateDao.queryBuilder();

		Where<ReplicateV1,Integer> where = queryBuilder.where();
		boolean needsAnd = false;

		if (getSamples) {
			where = where.ne(ReplicateV1.SAMPLEID_FIELD_NAME, DatabaseConstants.EMPTY_DB_ID);
		} else {
			where = where.eq(ReplicateV1.SAMPLEID_FIELD_NAME, DatabaseConstants.EMPTY_DB_ID);
		}

		needsAnd = true;

		if (sampleId != DatabaseConstants.EMPTY_DB_ID) {
			if (needsAnd) {
				where = where.and();
			}

			where = where.eq(ReplicateV1.SAMPLEID_FIELD_NAME, sampleId);
			needsAnd = true;
		}

		if (massSpecId != DatabaseConstants.EMPTY_DB_ID) {
			if (needsAnd) {
				where = where.and();
			}

			where = where.eq(ReplicateV1.MASSSPECID_FIELD_NAME, massSpecId);
			needsAnd = true;
		}

		if (userId != DatabaseConstants.EMPTY_DB_ID) {
			if (needsAnd) {
				where = where.and();
			}

			where = where.eq(ReplicateV1.USERID_FIELD_NAME, userId);
			needsAnd = true;
		}

		replicateList = new ReplicateList(getSamples, sampleId, massSpecId, userId);

		PreparedQuery<ReplicateV1> preparedQuery = queryBuilder.prepare();
		List<ReplicateV1> results = replicateDao.query(preparedQuery);

		HashMap<Integer,String> sampleIdToSampleName = new HashMap<Integer,String>();
		Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);

		for (ReplicateV1 replicate : results) {
			String sampleName = null;

			if (replicate.getSampleId() != DatabaseConstants.EMPTY_DATE) {
				sampleName = sampleIdToSampleName.get(replicate.getSampleId());

				if (sampleName == null) {
					Sample sample = sampleDao.queryForId(replicate.getSampleId());
					
					if (sample != null) {
						sampleName = sample.getName();
						sampleIdToSampleName.put(replicate.getSampleId(), sampleName);
					}
				}
			}

			replicateList.put(replicate.getId(), new ReplicateListItem(replicate.getDate(), replicate.getUserId(), replicate.getStandardId(), replicate.getSampleId(), sampleName, replicate.isDisabled()));
		}
	}
	
	public boolean isGetSamples() {
		return getSamples;
	}

	public void setGetSamples(boolean getSamples) {
		this.getSamples = getSamples;
	}

	public int getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}

	public int getMassSpecId() {
		return massSpecId;
	}

	public void setMassSpecId(int massSpecId) {
		this.massSpecId = massSpecId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public ReplicateList getReplicateList() {
		return replicateList;
	}

	public void setReplicateList(ReplicateList replicateList) {
		this.replicateList = replicateList;
	}
}
