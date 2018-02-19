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

import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=ReplicateV1.TABLE_NAME)
public class ReplicateV1 extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "REPLICATE_V1";
	public static final String USERID_FIELD_NAME = "USERID";
	public static final String SAMPLEID_FIELD_NAME = "SAMPLEID";
	public static final String STANDARDID_FIELD_NAME = "STANDARDID";
	public static final String DATE_FIELD_NAME = "DATE";
	public static final String MASSSPECID_FIELD_NAME = "MASSSPECID";
	public static final String ACIDTEMPID_FIELD_NAME = "ACIDTEMPID";
	public static final String DISABLED_FIELD_NAME = "DISABLED";
	public static final String DESCRIPTION_FIELD_NAME = "DESCRIPTION";
	public static final String CHANNEL_TO_MZX10_FIELD_NAME = "CHANNELTOMZX10";

	@DatabaseField(columnName=USERID_FIELD_NAME, canBeNull=false)
	public int userId = DatabaseConstants.EMPTY_DB_ID;
	@DatabaseField(columnName=SAMPLEID_FIELD_NAME, canBeNull=false, index=true)
	public int sampleId = DatabaseConstants.EMPTY_DB_ID;
	@DatabaseField(columnName=STANDARDID_FIELD_NAME, canBeNull=false, index=true)
	public int standardId = DatabaseConstants.EMPTY_DB_ID;
	@DatabaseField(columnName=DATE_FIELD_NAME, canBeNull=false)
	public long date;
	@DatabaseField(columnName=MASSSPECID_FIELD_NAME, canBeNull=false)
	public int massSpecId = DatabaseConstants.EMPTY_DB_ID;
	@DatabaseField(columnName=ACIDTEMPID_FIELD_NAME, canBeNull=false)
	public int acidTempId = DatabaseConstants.EMPTY_DB_ID;
	@DatabaseField(columnName=DISABLED_FIELD_NAME, canBeNull=false)
	public boolean disabled;
	@DatabaseField(columnName=DESCRIPTION_FIELD_NAME, canBeNull=false)
	public String description;
	@DatabaseField(columnName=CHANNEL_TO_MZX10_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	public Integer[] channelToMzX10;

	public ReplicateV1() { }

	public ReplicateV1(ReplicateV1 replicate) {
		super(replicate);

		this.userId = replicate.userId;
		this.sampleId = replicate.sampleId;
		this.standardId = replicate.standardId;
		this.date = replicate.date;
		this.massSpecId = replicate.massSpecId;
		this.acidTempId = replicate.acidTempId;
		this.disabled = replicate.disabled;
		this.description = replicate.description;

		if (replicate.channelToMzX10 != null) {
			this.channelToMzX10 = Arrays.copyOf(replicate.channelToMzX10, replicate.channelToMzX10.length);
		}
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}

	public int getStandardId() {
		return standardId;
	}

	public void setStandardId(int standardId) {
		this.standardId = standardId;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public int getMassSpecId() {
		return massSpecId;
	}

	public void setMassSpecId(int massSpecId) {
		this.massSpecId = massSpecId;
	}

	public int getAcidTempId() {
		return acidTempId;
	}

	public void setAcidTempId(int acidTempId) {
		this.acidTempId = acidTempId;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer[] getChannelToMzX10() {
		return channelToMzX10;
	}

	public void setChannelToMzX10(Integer[] channelToMzX10) {
		this.channelToMzX10 = channelToMzX10;
	}
}
