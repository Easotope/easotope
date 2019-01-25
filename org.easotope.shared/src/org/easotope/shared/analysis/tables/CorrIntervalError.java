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

package org.easotope.shared.analysis.tables;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=CorrIntervalError.TABLE_NAME)
public class CorrIntervalError extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "CORRINTERVALERROR_V0";
	public static final String CORR_INTERVAL_ID_FIELD_NAME = "CORRINTERVALID";
	public static final String DATA_ANALYSIS_ID_FIELD_NAME = "DATAANALYSISID";
	public static final String REPLICATE_ID_FIELD_NAME = "REPLICATEID";
	public static final String REPLICATE_DATE_FIELD_NAME = "REPLICATEDATE";
	public static final String REPLICATE_USER_ID_FIELD_NAME = "REPLICATEUSERID";
	public static final String REPSTEP_CLASS_FIELD_NAME = "REPSTEPCLASS";
	public static final String ERROR_MESSAGE_FIELD_NAME = "ERRORMESSAGE";

	// TODO there should be a dual index here
	@DatabaseField(columnName=CORR_INTERVAL_ID_FIELD_NAME)
	public int corrIntervalId;
	@DatabaseField(columnName=DATA_ANALYSIS_ID_FIELD_NAME)
	public int dataAnalysislId;
	@DatabaseField(columnName=REPLICATE_ID_FIELD_NAME)
	public int replicateId;
	@DatabaseField(columnName=REPLICATE_DATE_FIELD_NAME)
	public long replicateDate;
	@DatabaseField(columnName=REPLICATE_USER_ID_FIELD_NAME)
	public int replicateUserId;
	@DatabaseField(columnName=REPSTEP_CLASS_FIELD_NAME)
	public String repStepName;
	@DatabaseField(columnName=ERROR_MESSAGE_FIELD_NAME)
	public String errorMessage;

	public CorrIntervalError() { }

	public CorrIntervalError(CorrIntervalError corrIntervalError) {
		super();

		corrIntervalId = corrIntervalError.corrIntervalId;
		dataAnalysislId = corrIntervalError.dataAnalysislId;
		replicateId = corrIntervalError.replicateId;
		replicateDate = corrIntervalError.replicateDate;
		replicateUserId = corrIntervalError.replicateUserId;
		repStepName = corrIntervalError.repStepName;
		errorMessage = corrIntervalError.errorMessage;
	}

	public int getCorrIntervalId() {
		return corrIntervalId;
	}

	public void setCorrIntervalId(int corrIntervalId) {
		this.corrIntervalId = corrIntervalId;
	}

	public int getDataAnalysislId() {
		return dataAnalysislId;
	}

	public void setDataAnalysislId(int dataAnalysislId) {
		this.dataAnalysislId = dataAnalysislId;
	}

	public int getReplicateId() {
		return replicateId;
	}

	public void setReplicateId(int replicateId) {
		this.replicateId = replicateId;
	}

	public long getReplicateDate() {
		return replicateDate;
	}

	public void setReplicateDate(long replicateDate) {
		this.replicateDate = replicateDate;
	}

	public int getReplicateUserId() {
		return replicateUserId;
	}

	public void setReplicateUserId(int replicateUserId) {
		this.replicateUserId = replicateUserId;
	}

	public String getRepStepName() {
		return repStepName;
	}

	public void setRepStepName(String repStepName) {
		this.repStepName = repStepName;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
