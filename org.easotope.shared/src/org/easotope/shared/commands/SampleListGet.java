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

import java.util.Hashtable;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.rawdata.cache.input.samplelist.SampleList;
import org.easotope.shared.rawdata.cache.input.samplelist.SampleListItem;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;

public class SampleListGet extends Command {
	private static final long serialVersionUID = 1L;

	private int projectId = DatabaseConstants.EMPTY_DB_ID;
	private SampleList sampleList = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		// TODO this is wrong - should read project and verify userid
		return true;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		sampleList = new SampleList();

		Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);

		GenericRawResults<Sample> rawResults = sampleDao.queryRaw(
	         "select id from " + Sample.TABLE_NAME + " WHERE " + Sample.PROJECT_ID_FIELD_NAME + " = " + projectId + " AND " + 
	         "EXISTS (select id from " + ReplicateV1.TABLE_NAME + " where " + Sample.TABLE_NAME + "." + Sample.ID_FIELD_NAME + " = " + ReplicateV1.TABLE_NAME + "." + ReplicateV1.SAMPLEID_FIELD_NAME + ")",
	         sampleDao.getRawRowMapper());

		for (Sample sample : rawResults) {
			sampleList.put(sample.getId(), new SampleListItem(sampleDao.queryForId(sample.getId()), true));
		}

		rawResults = sampleDao.queryRaw(
			"select id from " + Sample.TABLE_NAME + " WHERE " + Sample.PROJECT_ID_FIELD_NAME + " = " + projectId + " AND " + 
			"NOT EXISTS (select id from " + ReplicateV1.TABLE_NAME + " where " + Sample.TABLE_NAME + "." + Sample.ID_FIELD_NAME + " = " + ReplicateV1.TABLE_NAME + "." + ReplicateV1.SAMPLEID_FIELD_NAME + ")",
			sampleDao.getRawRowMapper());

		for (Sample sample : rawResults) {
			sampleList.put(sample.getId(), new SampleListItem(sampleDao.queryForId(sample.getId()), true));
		}
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public SampleList getSampleList() {
		return sampleList;
	}

	public void setSampleList(SampleList sampleList) {
		this.sampleList = sampleList;
	}
}
