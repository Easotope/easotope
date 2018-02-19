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

package org.easotope.shared.analysis.tables;

import java.util.HashMap;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=RepAnalysisChoice.TABLE_NAME)
public class RepAnalysisChoice extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "REPANALYSISCHOICE_V0";
	public static final String SAMPLE_ID_FIELD_NAME = "SAMPLEID";
	public static final String SAM_ANALYSIS_ID_FIELD_NAME = "SAMANALYSISID";
	public static final String REP_ANALYSIS_CHOICE_FIELD_NAME = "REPANALYSISCHOICE";

	@DatabaseField(columnName=SAMPLE_ID_FIELD_NAME, index=true)
	public int sampleId;
	@DatabaseField(columnName=SAM_ANALYSIS_ID_FIELD_NAME, index=true)
	public int samAnalysisId;
	@DatabaseField(columnName=REP_ANALYSIS_CHOICE_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	public HashMap<Integer,Integer> repIdsToRepAnalysisChoice = new HashMap<Integer,Integer>();

	public RepAnalysisChoice() { }
	
	public RepAnalysisChoice(RepAnalysisChoice repAnalysisChoice) {
		super(repAnalysisChoice);

		this.sampleId = repAnalysisChoice.sampleId;
		this.samAnalysisId = repAnalysisChoice.samAnalysisId;
		this.repIdsToRepAnalysisChoice = new HashMap<Integer,Integer>(repAnalysisChoice.repIdsToRepAnalysisChoice);
	}

	public int getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}
	
	public int getSamAnalysisId() {
		return samAnalysisId;
	}

	public void setSamAnalysisId(int samAnalysisId) {
		this.samAnalysisId = samAnalysisId;
	}

	public HashMap<Integer,Integer> getRepIdsToRepAnalysisChoice() {
		return repIdsToRepAnalysisChoice;
	}

	public void setRepIdsToRepAnalysisChoice(HashMap<Integer,Integer> repIdsToRepAnalysisChoice) {
		this.repIdsToRepAnalysisChoice = repIdsToRepAnalysisChoice;
	}
}
