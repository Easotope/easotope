/*
 * Copyright © 2016-2020 by Devon Bowen.
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

import java.util.ArrayList;
import java.util.Hashtable;

import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.tables.Project;

public class ProjectUpdated extends Event {
	private static final long serialVersionUID = 1L;

	private Project project;
	private boolean hasChildren;
	private int oldUserId;
	private boolean oldUserHasChildren;
	private ArrayList<Integer> reassignedSampleIds;
	private ArrayList<Integer> reassignedReplicateIds;

	@Override
	public boolean isAuthorized(Hashtable<String, Object> authenticationObjects) {
		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		return user.id == project.userId || user.getIsAdmin() || permissions.isCanEditAllReplicates();
	}

	public ProjectUpdated(Project project, boolean hasChildren, int oldUserId, boolean oldUserHasChildren, ArrayList<Integer> reassignedSampleIds, ArrayList<Integer> reassignedReplicateIds) {
		this.project = project;
		this.hasChildren = hasChildren;
		this.oldUserId = oldUserId;
		this.oldUserHasChildren = oldUserHasChildren;
		this.reassignedSampleIds = reassignedSampleIds;
		this.reassignedReplicateIds = reassignedReplicateIds;
	}

	public Project getProject() {
		return project;
	}

	public int getOldUserId() {
		return oldUserId;
	}

	public boolean getOldUserHasChildren() {
		return oldUserHasChildren;
	}

	public boolean getHasChildren() {
		return hasChildren;
	}

	public ArrayList<Integer> getReassignedSampleIds() {
		return reassignedSampleIds;
	}

	public ArrayList<Integer> getReassignedReplicateIds() {
		return reassignedReplicateIds;
	}
}
