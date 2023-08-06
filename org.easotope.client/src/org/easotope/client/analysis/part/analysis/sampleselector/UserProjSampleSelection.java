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

package org.easotope.client.analysis.part.analysis.sampleselector;

import java.util.Arrays;
import java.util.TreeSet;


public class UserProjSampleSelection {
	TreeSet<Integer> userIds = new TreeSet<Integer>();
	TreeSet<Integer> projectIds = new TreeSet<Integer>();
	TreeSet<Integer> sampleIds = new TreeSet<Integer>();

	void addUser(int userId) {
		userIds.add(userId);
	}
	
	void addProject(int projectId) {
		projectIds.add(projectId);
	}
	
	void addSample(int sampleId) {
		sampleIds.add(sampleId);
	}
	
	public TreeSet<Integer> getUserIds() {
		return userIds;
	}
	
	public TreeSet<Integer> getProjectIds() {
		return projectIds;
	}

	public TreeSet<Integer> getSampleIds() {
		return sampleIds;
	}

	public boolean isEmpty() {
		return userIds.isEmpty() && projectIds.isEmpty() && sampleIds.isEmpty();
	}

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof UserProjSampleSelection)) {
			return false;
		}

		UserProjSampleSelection that = (UserProjSampleSelection) object;

		return Arrays.equals(userIds.toArray(), that.userIds.toArray()) && Arrays.equals(projectIds.toArray(), that.projectIds.toArray()) && Arrays.equals(sampleIds.toArray(), that.sampleIds.toArray());
	}
}
