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

package org.easotope.shared.rawdata.events;

import java.util.HashSet;
import java.util.Hashtable;

import org.easotope.framework.dbcore.cmdprocessors.Event;

public class SampleDeleted extends Event {
	private static final long serialVersionUID = 1L;

	private int sampleId;
	private int projectId;
	private boolean projectHasChildren;
	private int userId;
	private HashSet<Integer> deletedReplicateIds;

	@Override
	public boolean isAuthorized(Hashtable<String, Object> authenticationObjects) {
		return true;
	}

	public SampleDeleted(int sampleId, int projectId, boolean projectHasChildren, int userId, HashSet<Integer> deletedReplicateIds) {
		this.sampleId = sampleId;
		this.projectId = projectId;
		this.projectHasChildren = projectHasChildren;
		this.userId = userId;
		this.deletedReplicateIds = deletedReplicateIds;
	}

	public int getSampleId() {
		return sampleId;
	}

	public int getProjectId() {
		return projectId;
	}

	public boolean getProjectHasChildren() {
		return projectHasChildren;
	}

	public int getUserId() {
		return userId;
	}

	public HashSet<Integer> getDeletedReplicateIds() {
		return deletedReplicateIds;
	}
}
