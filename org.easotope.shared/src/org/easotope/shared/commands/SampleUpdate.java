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

package org.easotope.shared.commands;

import java.util.ArrayList;
import java.util.Hashtable;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.events.SampleUpdated;
import org.easotope.shared.rawdata.tables.Project;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class SampleUpdate extends Command {
	private static final long serialVersionUID = 1L;

	protected Sample sample;
	private transient Project project;

	@Override
	public String getName() {
		return getClass().getSimpleName() + "(id=" + sample.id + ", name=" + sample.getName() + ")";
	}

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);
		project = projectDao.queryForId(sample.getProjectId());

		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		return project != null && user.id == project.userId || permissions.isCanEditAllReplicates();
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
		Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);

		int oldUserId = DatabaseConstants.EMPTY_DB_ID;
		int oldProjectId = DatabaseConstants.EMPTY_DB_ID;
		boolean oldProjectHasChildren = false;
		ArrayList<Integer> reassignedReplicateIds = new ArrayList<Integer>();

		sample.setUserId(project.getUserId());

		if (sample.getId() == DatabaseConstants.EMPTY_DB_ID) {
			sampleDao.create(sample);

		} else {
			Sample oldSample = sampleDao.queryForId(sample.getId());

			if (oldSample == null) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.sampleUpdate_doesNotExist, new Object[] { sample.getId() } );
				return;
			}

			sampleDao.update(sample);

			oldUserId = oldSample.getUserId();
			oldProjectId = oldSample.getProjectId();

			QueryBuilder<Sample,Integer> queryBuilder = sampleDao.queryBuilder();
			Where<Sample,Integer> where = queryBuilder.where();
			where = where.eq(Sample.PROJECT_ID_FIELD_NAME, oldProjectId);

			Sample samples = sampleDao.queryForFirst(queryBuilder.prepare());
			oldProjectHasChildren = samples != null;

			if (oldProjectId != sample.getProjectId()) {
				for (ReplicateV1 replicate : replicateDao.queryForEq(ReplicateV1.SAMPLEID_FIELD_NAME, sample.getId())) {
					reassignedReplicateIds.add(replicate.getId());

					replicate.setUserId(project.getUserId());
					replicateDao.update(replicate);
				}
			}
		}

		QueryBuilder<ReplicateV1,Integer> queryBuilder = replicateDao.queryBuilder();
		Where<ReplicateV1,Integer> where = queryBuilder.where();
		where = where.eq(ReplicateV1.SAMPLEID_FIELD_NAME, sample.getId());

		ReplicateV1 replicate = replicateDao.queryForFirst(queryBuilder.prepare());
		boolean hasChildren = replicate != null;

		addEvent(new SampleUpdated(sample, hasChildren, oldProjectId, oldProjectHasChildren, oldUserId, reassignedReplicateIds));
	}

	public Sample getSample() {
		return sample;
	}

	public void setSample(Sample sample) {
		this.sample = sample;
	}
}
