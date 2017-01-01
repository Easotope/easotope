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

package org.easotope.shared.analysis.tables.old;

import java.util.Arrays;

import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=CorrIntervalV0.TABLE_NAME)
public class CorrIntervalV0 extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "CORRINTERVAL_V0";
	public static final String MASSSPECID_FIELD_NAME = "MASSSPECID";
	public static final String DESCRIPTION_FIELD_NAME = "DESCRIPTION";
	public static final String VALIDFROM_FIELD_NAME = "VALIDFROM";
	public static final String VALIDUNTIL_FIELD_NAME = "VALIDUNTIL";
	public static final String DATA_ANALYSIS_FIELD_NAME = "DATAANALYSIS";
	public static final String BATCH_DELIMITER_FIELD_NAME = "BATCHDELIMITER";

	@DatabaseField(columnName=MASSSPECID_FIELD_NAME, canBeNull=false)
	private int massSpecId;
	@DatabaseField(columnName=DESCRIPTION_FIELD_NAME, canBeNull=false)
	private String description;
	@DatabaseField(columnName=VALIDFROM_FIELD_NAME, canBeNull=false)
	private long validFrom = DatabaseConstants.EMPTY_DATE;
	@DatabaseField(columnName=VALIDUNTIL_FIELD_NAME, canBeNull=false)
	private long validUntil = DatabaseConstants.EMPTY_DATE;
	@DatabaseField(columnName=DATA_ANALYSIS_FIELD_NAME, canBeNull=false, dataType=DataType.SERIALIZABLE)
	private int[] dataAnalysis;
	@DatabaseField(columnName=BATCH_DELIMITER_FIELD_NAME, canBeNull=false)
	private int batchDelimiter;

	public CorrIntervalV0() { }

	public CorrIntervalV0(CorrIntervalV0 corrInterval) {
		super(corrInterval);

		this.massSpecId = corrInterval.massSpecId;
		this.description = corrInterval.description;
		this.validFrom = corrInterval.validFrom;
		this.validUntil = corrInterval.validUntil;
		this.dataAnalysis = Arrays.copyOf(corrInterval.dataAnalysis, corrInterval.dataAnalysis.length);
		this.batchDelimiter = corrInterval.batchDelimiter;
	}

	public int getMassSpecId() {
		return massSpecId;
	}

	public void setMassSpecId(int massSpecId) {
		this.massSpecId = massSpecId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(long validFrom) {
		this.validFrom = validFrom;
	}

	public long getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(long validUntil) {
		this.validUntil = validUntil;
	}

	public boolean validForTime(long date) {
		return date >= validFrom && date < validUntil;
	}

	public int[] getDataAnalysis() {
		return dataAnalysis;
	}

	public void setDataAnalysis(int[] dataAnalysis) {
		this.dataAnalysis = dataAnalysis;
	}

	public int getBatchDelimiter() {
		return batchDelimiter;
	}

	public void setBatchDelimiter(int batchDelimiter) {
		this.batchDelimiter = batchDelimiter;
	}
}
