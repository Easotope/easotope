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

package org.easotope.shared.commands;

import java.util.HashMap;
import java.util.Hashtable;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.cache.sourcelist.sourcelist.SourceList;
import org.easotope.shared.rawdata.cache.sourcelist.sourcelist.SourceListItem;
import org.easotope.shared.rawdata.tables.Project;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class SourceListGet extends Command {
	private static final long serialVersionUID = 1L;

	private SourceList sourceList;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		return true;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		sourceList = new SourceList();

		Dao<Standard,Integer> standardDao = DaoManager.createDao(connectionSource, Standard.class);

		for (Standard standard : standardDao.queryForAll()) {
			sourceList.add(new SourceListItem(standard.getId(), standard.getName()));
		}

		Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
		Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);
		Dao<User,Integer> userDao = DaoManager.createDao(connectionSource, User.class);

		HashMap<Integer,String> projectIdToName = new HashMap<Integer,String>();
		HashMap<Integer,Integer> projectIdToUserId = new HashMap<Integer,Integer>();
		HashMap<Integer,String> userIdToName = new HashMap<Integer,String>();

		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		User callingUser = (User) authenticationObjects.get(AuthenticationKeys.USER);

		for (Sample sample : sampleDao.queryForAll()) {
			int projectId = sample.getProjectId();
			String projectName;

			if (!projectIdToName.containsKey(projectId)) {
				Project project = projectDao.queryForId(projectId);
				projectIdToName.put(projectId, project.getName());
				projectIdToUserId.put(projectId, project.getUserId());
			}

			projectName = projectIdToName.get(projectId);
			int projectUserId = projectIdToUserId.get(projectId);

			if (permissions.isCanEditAllReplicates() || callingUser.getId() == projectUserId) {
				if (!userIdToName.containsKey(projectUserId)) {
					User user = userDao.queryForId(projectUserId);
					userIdToName.put(projectUserId, user.getUsername());
				}

				int userId = projectUserId;
				String userName = userIdToName.get(projectUserId);

				sourceList.add(new SourceListItem(userId, userName, projectId, projectName, sample.getId(), sample.getName()));
			}
		}
	}

	public SourceList getSourceList() {
		return sourceList;
	}
}
