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

package org.easotope.shared.rawdata.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.rawdata.tables.Project;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class DeleteProject {
	public static HashMap<Integer,ArrayList<ReplicateV1>> deleteProject(ConnectionSource connectionSource, RawFileManager rawFileManager, int projectId) throws SQLException {
		Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);

		Project project = projectDao.queryForId(projectId);

		if (project == null) {
			return null;
		}

		return deleteProject(connectionSource, rawFileManager, project);
	}

	public static HashMap<Integer,ArrayList<ReplicateV1>> deleteProject(ConnectionSource connectionSource, RawFileManager rawFileManager, Project project) throws SQLException {
		Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
		HashMap<Integer,ArrayList<ReplicateV1>> result = new HashMap<Integer,ArrayList<ReplicateV1>>();

		for (Sample sample : sampleDao.queryForEq(Sample.PROJECT_ID_FIELD_NAME, project.getId())) {
			ArrayList<ReplicateV1> replicates = DeleteSample.deleteSample(connectionSource, rawFileManager, sample.getId());
			result.put(sample.getId(), replicates);
		}

		Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);
		projectDao.delete(project);

		return result;
	}
}
