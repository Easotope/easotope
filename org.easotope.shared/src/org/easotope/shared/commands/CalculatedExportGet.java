/*
 * Copyright © 2016-2019 by Devon Bowen.
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.analysis.server.LoadOrCalculateCorrInterval;
import org.easotope.shared.analysis.server.LoadOrCalculateSample;
import org.easotope.shared.analysis.tables.CalcSampleCache;
import org.easotope.shared.analysis.tables.CorrIntervalScratchPad;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ProjectPad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.core.scratchpad.UserPad;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.tables.Project;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class CalculatedExportGet extends Command {
	private static final long serialVersionUID = 1L;

	private ArrayList<Integer> userIds = new ArrayList<Integer>();
	private ArrayList<Integer> projectIds = new ArrayList<Integer>();
	private ArrayList<Integer> sampleIds = new ArrayList<Integer>();

	private transient RequestTree requestTree = null;

	private ScratchPad<?> requestedScratchPad;
	private ColumnOrdering requestedColumnOrdering;
	private FormatLookup requestedFormatLookup;

	private ScratchPad<ReplicatePad> corrIntervalReplicates;
	private ColumnOrdering corrIntervalColumnOrdering;
	private FormatLookup corrIntervalFormatLookup;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		requestTree = new RequestTree(connectionSource);

		requestTree.addUserIds(userIds);
		requestTree.addProjectIds(projectIds);
		requestTree.addSampleIds(sampleIds);

		userIds = null;
		projectIds = null;
		sampleIds = null;

		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);

		if (permissions.isCanEditAllReplicates()) {
			return true;
		}

		Set<Integer> userIds = requestTree.getUserIds();

		if (userIds.size() == 1 && userIds.toArray(new Integer[1])[0] == user.getId()) {
			return true;
		}

		requestTree = null;
		return false;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		requestedColumnOrdering = new ColumnOrdering();
		requestedFormatLookup = new FormatLookup();

		corrIntervalReplicates = new ScratchPad<ReplicatePad>();
		corrIntervalColumnOrdering = new ColumnOrdering();
		corrIntervalFormatLookup = new FormatLookup();

		HashSet<String> alreadyLoadedCorrIntervalRepAnalysis = new HashSet<String>();
		HashMap<Integer,HashMap<Integer,ArrayList<Integer>>> tree = requestTree.getTree();

		LoadOrCalculateSample loadOrCalculateSample = new LoadOrCalculateSample(connectionSource);

		if (requestTree.hasMultipleUsers()) {
			ScratchPad<UserPad> userScratchPad = new ScratchPad<UserPad>();
			requestedScratchPad = userScratchPad;

			for (Integer userId : tree.keySet()) {
				Dao<User,Integer> userDao = DaoManager.createDao(connectionSource, User.class);
				User thisUser = userDao.queryForId(userId);

				UserPad userPad = new UserPad(userScratchPad, thisUser.getUsername());

				for (Integer projectId : tree.get(userId).keySet()) {
					Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);
					Project project = projectDao.queryForId(projectId);
					ProjectPad projectPad = new ProjectPad(userPad, project.getName());

					for (Integer sampleId : tree.get(userId).get(projectId)) {
						Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
						Sample sample = sampleDao.queryForId(sampleId);

						if (sample.getSamAnalyses() != null) {
							for (int sampleAnalysisId : sample.getSamAnalyses()) {
								CalcSampleCache calcSampleCache = loadOrCalculateSample.getCalcSampleCache(sampleId, sampleAnalysisId);
								ScratchPad<SamplePad> scratchPad = calcSampleCache.getScratchPad();
								scratchPad.getChild(0).reassignToParent(projectPad);

								requestedColumnOrdering.add(calcSampleCache.getColumnOrdering());
								requestedFormatLookup.add(calcSampleCache.getFormatLookup());

								int[] corrIntervalIds = calcSampleCache.getCorrIntervalIds();
								int[] repAnalysisIds = calcSampleCache.getRepAnalysisIds();
								
								for (int i=0; i<corrIntervalIds.length; i++) {
									String key = corrIntervalIds[i] + "/" + repAnalysisIds[i];

									if (!alreadyLoadedCorrIntervalRepAnalysis.contains(key)) {
										LoadOrCalculateCorrInterval loadOrCalculateCorrInterval = new LoadOrCalculateCorrInterval(corrIntervalIds[i], repAnalysisIds[i], connectionSource);
										CorrIntervalScratchPad corrIntervalScratchPad = loadOrCalculateCorrInterval.getCorrIntervalScratchPad();
										corrIntervalScratchPad.getScratchPad().reassignAllStandardsToParent(corrIntervalReplicates);
										corrIntervalColumnOrdering.add(corrIntervalScratchPad.getColumnOrdering());
										corrIntervalFormatLookup.add(corrIntervalScratchPad.getFormatLookup());
										alreadyLoadedCorrIntervalRepAnalysis.add(key);
									}
								}
							}
						}
					}
				}
			}

		} else if (requestTree.hasMultipleProjects()) {
			ScratchPad<ProjectPad> projectScratchPad = new ScratchPad<ProjectPad>();
			requestedScratchPad = projectScratchPad;

			int userId = tree.keySet().toArray(new Integer[1])[0];

			for (Integer projectId : tree.get(userId).keySet()) {
				Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);
				Project project = projectDao.queryForId(projectId);
				ProjectPad projectPad = new ProjectPad(projectScratchPad, project.getName());

				for (Integer sampleId : tree.get(userId).get(projectId)) {
					Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
					Sample sample = sampleDao.queryForId(sampleId);

					if (sample.getSamAnalyses() != null) {
						for (int sampleAnalysisId : sample.getSamAnalyses()) {
							CalcSampleCache calcSampleCache = loadOrCalculateSample.getCalcSampleCache(sampleId, sampleAnalysisId);
							ScratchPad<SamplePad> scratchPad = calcSampleCache.getScratchPad();
							scratchPad.getChild(0).reassignToParent(projectPad);
							
							requestedColumnOrdering.add(calcSampleCache.getColumnOrdering());
							requestedFormatLookup.add(calcSampleCache.getFormatLookup());

							int[] corrIntervalIds = calcSampleCache.getCorrIntervalIds();
							int[] repAnalysisIds = calcSampleCache.getRepAnalysisIds();
							
							for (int i=0; i<corrIntervalIds.length; i++) {
								String key = corrIntervalIds[i] + "/" + repAnalysisIds[i];
								
								if (!alreadyLoadedCorrIntervalRepAnalysis.contains(key)) {
									LoadOrCalculateCorrInterval loadOrCalculateCorrInterval = new LoadOrCalculateCorrInterval(corrIntervalIds[i], repAnalysisIds[i], connectionSource);
									CorrIntervalScratchPad corrIntervalScratchPad = loadOrCalculateCorrInterval.getCorrIntervalScratchPad();
									corrIntervalScratchPad.getScratchPad().reassignAllStandardsToParent(corrIntervalReplicates);
									corrIntervalColumnOrdering.add(corrIntervalScratchPad.getColumnOrdering());
									corrIntervalFormatLookup.add(corrIntervalScratchPad.getFormatLookup());
									alreadyLoadedCorrIntervalRepAnalysis.add(key);
								}
							}
						}
					}
				}
			}

		} else {
			ScratchPad<SamplePad> sampleScratchPad = new ScratchPad<SamplePad>();
			requestedScratchPad = sampleScratchPad;

			int userId = tree.keySet().toArray(new Integer[1])[0];
			int projectId = tree.get(userId).keySet().toArray(new Integer[1])[0];

			for (Integer sampleId : tree.get(userId).get(projectId)) {
				Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
				Sample sample = sampleDao.queryForId(sampleId);

				if (sample.getSamAnalyses() != null) {
					for (int sampleAnalysisId : sample.getSamAnalyses()) {
						CalcSampleCache calcSampleCache = loadOrCalculateSample.getCalcSampleCache(sampleId, sampleAnalysisId);
						ScratchPad<SamplePad> scratchPad = calcSampleCache.getScratchPad();
						scratchPad.getChild(0).reassignToParent(sampleScratchPad);

						requestedColumnOrdering.add(calcSampleCache.getColumnOrdering());
						requestedFormatLookup.add(calcSampleCache.getFormatLookup());

						int[] corrIntervalIds = calcSampleCache.getCorrIntervalIds();
						int[] repAnalysisIds = calcSampleCache.getRepAnalysisIds();

						for (int i=0; i<corrIntervalIds.length; i++) {
							String key = corrIntervalIds[i] + "/" + repAnalysisIds[i];

							if (!alreadyLoadedCorrIntervalRepAnalysis.contains(key)) {
								LoadOrCalculateCorrInterval loadOrCalculateCorrInterval = new LoadOrCalculateCorrInterval(corrIntervalIds[i], repAnalysisIds[i], connectionSource);
								CorrIntervalScratchPad corrIntervalScratchPad = loadOrCalculateCorrInterval.getCorrIntervalScratchPad();
								corrIntervalScratchPad.getScratchPad().reassignAllStandardsToParent(corrIntervalReplicates);
								corrIntervalColumnOrdering.add(corrIntervalScratchPad.getColumnOrdering());
								corrIntervalFormatLookup.add(corrIntervalScratchPad.getFormatLookup());
								alreadyLoadedCorrIntervalRepAnalysis.add(key);
							}
						}
					}
				}
			}
		}

		for (ReplicatePad pad : new ArrayList<ReplicatePad>(corrIntervalReplicates.getChildren())) {
			if ((Boolean) pad.getValue(Pad.DISABLED)) {
				corrIntervalReplicates.removeChild(pad);
			}
		}
	}

	public void addUserIds(TreeSet<Integer> userIds) {
		this.userIds.addAll(userIds);
	}
	
	public void addProjectIds(TreeSet<Integer> projectIds) {
		this.projectIds.addAll(projectIds);
	}

	public void addSampleIds(TreeSet<Integer> sampleIds) {
		this.sampleIds.addAll(sampleIds);
	}
	
	public ScratchPad<?> getRequestedScratchPad() {
		return requestedScratchPad;
	}

	public ScratchPad<ReplicatePad> getCorrIntervalReplicates() {
		return corrIntervalReplicates;
	}

	private class RequestTree {
		private ConnectionSource connectionSource;
		private HashMap<Integer,HashMap<Integer,ArrayList<Integer>>> tree = new HashMap<Integer,HashMap<Integer,ArrayList<Integer>>>();

		RequestTree(ConnectionSource connectionSource) {
			this.connectionSource = connectionSource;
		}

		public HashMap<Integer,HashMap<Integer,ArrayList<Integer>>> getTree() {
			return tree;
		}

		public boolean hasMultipleUsers() {
			return tree.keySet().size() != 1;
		}

		public boolean hasMultipleProjects() {
			if (hasMultipleUsers()) {
				return true;
			}

			int userId = tree.keySet().toArray(new Integer[1])[0];

			return tree.get(userId).keySet().size() != 1;
		}

		private void addUserIds(ArrayList<Integer> userIds) throws SQLException {
			for (int userId : userIds) {
				addUserId(userId);
			}
		}

		private void addUserId(int userId) throws SQLException {
			Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);

			for (Project project : projectDao.queryForEq(Project.USER_ID_FIELD_NAME, userId)) {
				addProjectId(userId, project.getId());
			}
		}

		private void addProjectIds(ArrayList<Integer> projectIds) throws SQLException {
			for (int projectId : projectIds) {
				Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);
				Project project = projectDao.queryForId(projectId);

				addProjectId(project.getUserId(), project.getId());
			}
		}

		private void addProjectId(int userId, int projectId) throws SQLException {
			Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);

			for (Sample sample : sampleDao.queryForEq(Sample.PROJECT_ID_FIELD_NAME, projectId)) {
				addSample(userId, projectId, sample.getId());
			}
		}

		private void addSampleIds(ArrayList<Integer> sampleIds) throws SQLException {
			for (int sampleId : sampleIds) {
				Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
				Sample sample = sampleDao.queryForId(sampleId);

				Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);
				Project project = projectDao.queryForId(sample.getProjectId());

				addSample(project.getUserId(), project.getId(), sample.getId());
			}
		}

		private void addSample(int userId, int projectId, int sampleId) {
			if (!tree.containsKey(userId)) {
				tree.put(userId, new HashMap<Integer,ArrayList<Integer>>());
			}

			HashMap<Integer,ArrayList<Integer>> user = tree.get(userId);

			if (!user.containsKey(projectId)) {
				user.put(projectId, new ArrayList<Integer>());
			}

			ArrayList<Integer> project = user.get(projectId);

			project.add(sampleId);
		}

		public Set<Integer> getUserIds() {
			return tree.keySet();
		}
	}

	public ColumnOrdering getRequestedColumnOrdering() {
		return requestedColumnOrdering;
	}

	public FormatLookup getRequestedFormatLookup() {
		return requestedFormatLookup;
	}

	public ColumnOrdering getCorrIntervalColumnOrdering() {
		return corrIntervalColumnOrdering;
	}

	public FormatLookup getCorrIntervalFormatLookup() {
		return corrIntervalFormatLookup;
	}
}
