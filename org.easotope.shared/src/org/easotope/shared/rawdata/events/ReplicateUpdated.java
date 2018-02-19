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

package org.easotope.shared.rawdata.events;

import java.util.Hashtable;

import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.tables.ReplicateV1;

public class ReplicateUpdated extends Event {
	private static final long serialVersionUID = 1L;

	private ReplicateV1 replicate;
	private int oldSampleId;
	private boolean oldSampleHasChildren;
	private int newSampleId;
	private String newSampleName;
	private int oldProjectId;
	private int newProjectId;

	@Override
	public boolean isAuthorized(Hashtable<String, Object> authenticationObjects) {
		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		return replicate.getStandardId() != DatabaseConstants.EMPTY_DB_ID || user.id == replicate.getUserId() || permissions.isCanEditAllReplicates();
	}

	public ReplicateUpdated(ReplicateV1 replicate, int oldSampleId, boolean oldSampleHasChildren, int newSampleId, String newSampleName, int oldProjectId, int newProjectId) {
		this.replicate = replicate;
		this.oldSampleId = oldSampleId;
		this.oldSampleHasChildren = oldSampleHasChildren;
		this.newSampleId = newSampleId;
		this.newSampleName = newSampleName;
		this.oldProjectId = oldProjectId;
		this.newProjectId = newProjectId;
	}

	public ReplicateV1 getReplicate() {
		return replicate;
	}
	
	public int getOldSampleId() {
		return oldSampleId;
	}

	public boolean getOldSampleHasChildren() {
		return oldSampleHasChildren;
	}

	public int getNewSampleId() {
		return newSampleId;
	}

	public String getNewSampleName() {
		return newSampleName;
	}

	public int getOldProjectId() {
		return oldProjectId;
	}

	public int getNewProjectId() {
		return newProjectId;
	}
}
