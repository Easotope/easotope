/*
 * Copyright © 2016-2017 by Devon Bowen.
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

package org.easotope.shared.analysis.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=SamStepParams.TABLE_NAME)
public class SamStepParams extends StepParams {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "SAMSTEPPARAMS_V0";
	public static final String USER_ID_FIELD_NAME = "USERID";
	public static final String PROJECT_ID_FIELD_NAME = "PROJECTID";
	public static final String SAMPLE_ID_FIELD_NAME = "SAMPLEID";

	@DatabaseField(columnName=USER_ID_FIELD_NAME)
	public int userId;
	@DatabaseField(columnName=PROJECT_ID_FIELD_NAME)
	public int projectId;
	@DatabaseField(columnName=SAMPLE_ID_FIELD_NAME)
	public int sampleId;

	public SamStepParams() { }

	public SamStepParams(SamStepParams samStepParameters) {
		super(samStepParameters);

		this.userId = samStepParameters.userId;
		this.projectId = samStepParameters.projectId;
		this.sampleId = samStepParameters.sampleId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public int getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}
}
