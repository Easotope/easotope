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

package org.easotope.shared.rawdata.cache.sourcelist.sourcelist;

import java.io.Serializable;

import org.easotope.framework.dbcore.DatabaseConstants;

public class SourceListItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private int userId = DatabaseConstants.EMPTY_DB_ID;
	private String userName;
	private int projectId = DatabaseConstants.EMPTY_DB_ID;
	private String projectName;
	private int sampleId = DatabaseConstants.EMPTY_DB_ID;
	private int standardId = DatabaseConstants.EMPTY_DB_ID;
	private String sourceName;
	private transient String sourceNameToUpper;

	public SourceListItem(int userId, String userName, int projectId, String projectName, int sampleId, String sampleName) {
		this.userId = userId;
		this.userName = userName;
		this.projectId = projectId;
		this.projectName = projectName;
		this.sampleId = sampleId;
		this.sourceName = sampleName;
	}

	public SourceListItem(int standardId, String standardName) {
		this.standardId = standardId;
		this.sourceName = standardName;
	}

	public int getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public int getProjectId() {
		return projectId;
	}

	public String getProjectName() {
		return projectName;
	}

	public int getSampleId() {
		return sampleId;
	}

	public int getStandardId() {
		return standardId;
	}

	public String getSourceName() {
		return sourceName;
	}

	public String getSourceNameToUpper() {
		if (sourceNameToUpper == null) {
			sourceNameToUpper = sourceName.toUpperCase();
		}

		return sourceNameToUpper;
	}

	public boolean isStandard() {
		return standardId != DatabaseConstants.EMPTY_DB_ID;
	}

	@Override
	public String toString() {
		if (standardId != DatabaseConstants.EMPTY_DB_ID) {
			return sourceName;
		} else {
			return sourceName + " | " + userName + " | " + projectName;
		}
	}
}
