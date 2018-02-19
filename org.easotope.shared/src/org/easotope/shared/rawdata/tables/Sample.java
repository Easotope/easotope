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

package org.easotope.shared.rawdata.tables;

import java.util.Arrays;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=Sample.TABLE_NAME)
public class Sample extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "SAMPLE_V0";
	public static final String USER_ID_FIELD_NAME = "USERID";
	public static final String PROJECT_ID_FIELD_NAME = "PROJECTID";
	public static final String NAME_FIELD_NAME = "NAME";
	public static final String DESCRIPTION_FIELD_NAME = "DESCRIPTION";
	public static final String SAMPLETYPEID_FIELD_NAME = "SAMPLETYPEID";
	public static final String SAMANALYSES_FIELD_NAME = "SAMANALYSES";

	@DatabaseField(columnName=USER_ID_FIELD_NAME)
	private int userId;
	@DatabaseField(columnName=PROJECT_ID_FIELD_NAME)
	private int projectId;
	@DatabaseField(columnName=NAME_FIELD_NAME)
	private String name;
	@DatabaseField(columnName=DESCRIPTION_FIELD_NAME)
	private String description;
	@DatabaseField(columnName=SAMPLETYPEID_FIELD_NAME)
	private int sampleTypeId;
	@DatabaseField(columnName=SAMANALYSES_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private int[] samAnalyses;

	public Sample() { }

	public Sample(Sample sample) {
		super(sample);

		this.userId = sample.userId;
		this.projectId = sample.projectId;
		this.name = sample.name;
		this.description = sample.description;
		this.sampleTypeId = sample.sampleTypeId;

		if (sample.samAnalyses != null) {
			this.samAnalyses = Arrays.copyOf(sample.samAnalyses, sample.samAnalyses.length);
		}
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSampleTypeId() {
		return sampleTypeId;
	}

	public void setSampleTypeId(int sampleTypeId) {
		this.sampleTypeId = sampleTypeId;
	}

	public int[] getSamAnalyses() {
		return samAnalyses;
	}

	public void setSamAnalyses(int[] samAnalyses) {
		this.samAnalyses = samAnalyses;
	}
}