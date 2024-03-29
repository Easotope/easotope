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

package org.easotope.shared.rawdata.cache.input.projectlist;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.commands.ProjectListGet;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.cache.input.project.ProjectCacheKey;
import org.easotope.shared.rawdata.events.ProjectDeleted;
import org.easotope.shared.rawdata.events.ProjectUpdated;
import org.easotope.shared.rawdata.events.SampleDeleted;
import org.easotope.shared.rawdata.events.SampleUpdated;
import org.easotope.shared.rawdata.tables.Project;
import org.easotope.shared.rawdata.tables.Sample;

public class ProjectListPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new ProjectListCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		ProjectListGet projectListGet = new ProjectListGet();
		projectListGet.setUserId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(projectListGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return projectListGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		ProjectListGet projectListGet = (ProjectListGet) command;

		for (Integer projectId : projectListGet.getProjectList().keySet()) {
			cache.put(new ProjectCacheKey(projectId), this, callParameters, projectListGet.getProjectList().get(projectId).getProject());
		}

		ProjectList projectList = new ProjectList(projectListGet.getUserId());
		projectList.putAll(projectListGet.getProjectList());
		cache.put(new ProjectListCacheKey(projectListGet.getUserId()), this, callParameters, projectList);
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof InputCacheProjectListGetListener) {
			((InputCacheProjectListGetListener) listener).projectListGetCompleted(commandId, (ProjectList) result);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof InputCacheProjectListGetListener) {
			((InputCacheProjectListGetListener) listener).projectListGetError(commandId, message);
		}
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatNeedReloadBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public HashSet<CacheKey> updateCacheBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof ProjectUpdated) {
			ProjectUpdated projectUpdated = (ProjectUpdated) event;

			Project project = projectUpdated.getProject();
			boolean hasChildren = projectUpdated.getHasChildren();
			int oldUserId = projectUpdated.getOldUserId();
			int newUserId = project.getUserId();

			if (oldUserId != DatabaseConstants.EMPTY_DB_ID && oldUserId != newUserId) {
				CacheKey cacheKey = new ProjectListCacheKey(oldUserId);

				if (cache.containsKey(cacheKey)) {
					ProjectList projectList = (ProjectList) cache.get(cacheKey);

					ProjectList newProjectList = new ProjectList(oldUserId);
					newProjectList.putAll(projectList);
					newProjectList.remove(project.getId());

					CacheKey oldCacheKey = cache.update(cacheKey, newProjectList);
					result.add(oldCacheKey);
				}
			}

			CacheKey cacheKey = new ProjectListCacheKey(newUserId);

			if (cache.containsKey(cacheKey)) {
				ProjectList projectList = (ProjectList) cache.get(cacheKey);

				ProjectList newProjectList = new ProjectList(newUserId);
				newProjectList.putAll(projectList);
				newProjectList.put(project.getId(), new ProjectListItem(project, hasChildren));

				CacheKey oldCacheKey = cache.update(cacheKey, newProjectList);
				result.add(oldCacheKey);
			}

		} else if (event instanceof ProjectDeleted) {
			ProjectDeleted projectDeleted = (ProjectDeleted) event;
			int projectId = projectDeleted.getProjectId();
			int userId = projectDeleted.getUserId();

			ProjectListCacheKey projectListCacheKey = new ProjectListCacheKey(userId);

			if (cache.containsKey(projectListCacheKey)) {
				ProjectList oldProjectList = (ProjectList) cache.get(projectListCacheKey);

				ProjectList newProjectList = new ProjectList(userId);
				newProjectList.putAll(oldProjectList);
				newProjectList.remove(projectId);

				CacheKey oldCacheKey = cache.update(projectListCacheKey, newProjectList);
				result.add(oldCacheKey);
			}

		} else if (event instanceof SampleUpdated) {
			SampleUpdated sampleUpdated = (SampleUpdated) event;

			Sample sample = sampleUpdated.getSample();
			int oldProjectId = sampleUpdated.getOldProjectId();
			boolean oldProjectHasChildren = sampleUpdated.getOldProjectHasChildren();
			int newProjectId = sample.getProjectId();
			int oldUserId = sampleUpdated.getOldUserId();
			int newUserId = sample.getUserId();

			ProjectListCacheKey projectListCacheKey;

			if (oldProjectId != DatabaseConstants.EMPTY_DB_ID && oldProjectId != newProjectId) {
				projectListCacheKey = new ProjectListCacheKey(oldUserId);

				if (cache.containsKey(projectListCacheKey)) {
					ProjectList oldProjectList = (ProjectList) cache.get(projectListCacheKey);
					Project oldProject = oldProjectList.get(oldProjectId).getProject();

					ProjectList newProjectList = new ProjectList(oldUserId);
					newProjectList.putAll(oldProjectList);
					newProjectList.put(oldProjectId, new ProjectListItem(oldProject, oldProjectHasChildren));

					CacheKey oldCacheKey = cache.update(projectListCacheKey, newProjectList);
					result.add(oldCacheKey);
				}
			}

			projectListCacheKey = new ProjectListCacheKey(newUserId);

			if (cache.containsKey(projectListCacheKey)) {
				ProjectList oldProjectList = (ProjectList) cache.get(projectListCacheKey);
				Project oldProject = oldProjectList.get(newProjectId).getProject();

				ProjectList newProjectList = new ProjectList(newUserId);
				newProjectList.putAll(oldProjectList);
				newProjectList.put(oldProject.getId(), new ProjectListItem(oldProject, true));

				CacheKey oldCacheKey = cache.update(projectListCacheKey, newProjectList);
				result.add(oldCacheKey);
			}

		} else if (event instanceof SampleDeleted) {
			SampleDeleted sampleDeleted = (SampleDeleted) event;
			int oldProjectId = sampleDeleted.getProjectId();
			boolean oldProjectHasChildren = sampleDeleted.getProjectHasChildren();
			int oldUserId = sampleDeleted.getUserId();

			ProjectListCacheKey projectListCacheKey = new ProjectListCacheKey(oldUserId);

			if (cache.containsKey(projectListCacheKey)) {
				ProjectList oldProjectList = (ProjectList) cache.get(projectListCacheKey);
				Project oldProject = oldProjectList.get(oldProjectId).getProject();

				ProjectList newProjectList = new ProjectList(oldUserId);
				newProjectList.putAll(oldProjectList);
				newProjectList.put(oldProjectId, new ProjectListItem(oldProject, oldProjectHasChildren));

				CacheKey oldCacheKey = cache.update(projectListCacheKey, newProjectList);
				result.add(oldCacheKey);
			}
		}

		return result;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		if (listener instanceof InputCacheProjectListGetListener) {
			((InputCacheProjectListGetListener) listener).projectListUpdated(commandId, (ProjectList) updated);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		assert(false);
		return 0;
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		assert(false);
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		assert(false);
	}
	
	@Override
	public void callbackVerifyAndResend(Object listener, int commandId, String message) {
		assert(false);
	}
	
	@Override
	public int deleteData(AbstractCache abstractCache, Object[] parameters) {
		assert(false);
		return 0;
	}

	@Override
	public void callbackDeleteCompleted(Object listener, Command command) {
		assert(false);
	}

	@Override
	public void callbackDeleteError(Object listener, int commandId, String message) {
		assert(false);
	}
}
