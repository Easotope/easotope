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
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.cache.input.projectlist.ProjectList;
import org.easotope.shared.rawdata.cache.input.projectlist.ProjectListItem;
import org.easotope.shared.rawdata.tables.Project;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;

public class ProjectListGet extends Command {
	private static final long serialVersionUID = 1L;

	private int userId = DatabaseConstants.EMPTY_DB_ID;
	private ProjectList projectList = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);

		if (permissions.isCanEditAllReplicates()) {
			return true;
		}

		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		return userId == user.id;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		projectList = new ProjectList(userId);

		Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);

		GenericRawResults<Project> rawResults = projectDao.queryRaw(
	         "select * from " + Project.TABLE_NAME + " WHERE " + Project.USER_ID_FIELD_NAME + " = " + userId + " AND " + 
	         "EXISTS (select * from " + Sample.TABLE_NAME + " where " + Project.TABLE_NAME + "." + Project.ID_FIELD_NAME + " = " + Sample.TABLE_NAME + "." + Sample.PROJECT_ID_FIELD_NAME + ")",
	         projectDao.getRawRowMapper());

		for (Project project : rawResults) {
			projectList.put(project.getId(), new ProjectListItem(project, true));
		}

		rawResults = projectDao.queryRaw(
			"select * from " + Project.TABLE_NAME + " WHERE " + Project.USER_ID_FIELD_NAME + " = " + userId + " AND " + 
			"NOT EXISTS (select * from " + Sample.TABLE_NAME + " where " + Project.TABLE_NAME + "." + Project.ID_FIELD_NAME + " = " + Sample.TABLE_NAME + "." + Sample.PROJECT_ID_FIELD_NAME + ")",
			projectDao.getRawRowMapper());

		for (Project project : rawResults) {
			projectList.put(project.getId(), new ProjectListItem(project, false));
		}
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public ProjectList getProjectList() {
		return projectList;
	}

	public void setProjectList(ProjectList projectList) {
		this.projectList = projectList;
	}
}
