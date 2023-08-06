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

package org.easotope.shared.analysis.tables;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=CorrIntervalScratchPad.TABLE_NAME)
public class CorrIntervalScratchPad extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "CORRINTERVALSCRATCHPAD_V0";
	public static final String CORR_INTERVAL_ID_FIELD_NAME = "CORRINTERVALID";
	public static final String DATA_ANALYSIS_ID_FIELD_NAME = "DATAANALYSISID";
	public static final String SCRATCH_PAD_FIELD_NAME = "SCRATCHPAD";
	public static final String COLUMN_ORDERING_FIELD_NAME = "COLUMNORDERING";
	public static final String FORMAT_LOOKUP_FIELD_NAME = "FORMATLOOKUP";

	// TODO there should be a dual index here
	@DatabaseField(columnName=CORR_INTERVAL_ID_FIELD_NAME)
	public int corrIntervalId;
	@DatabaseField(columnName=DATA_ANALYSIS_ID_FIELD_NAME)
	public int dataAnalysisId;
	@DatabaseField(columnName=SCRATCH_PAD_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	public ScratchPad<ReplicatePad> scratchPad;
	@DatabaseField(columnName=COLUMN_ORDERING_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	public ColumnOrdering columnOrdering;
	@DatabaseField(columnName=FORMAT_LOOKUP_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	public FormatLookup formatLookup;

	public CorrIntervalScratchPad() { }

	public CorrIntervalScratchPad(CorrIntervalScratchPad analysisResults) {
		this.corrIntervalId = analysisResults.corrIntervalId;
		this.dataAnalysisId = analysisResults.dataAnalysisId;
		this.scratchPad = new ScratchPad<ReplicatePad>(analysisResults.scratchPad);
		this.columnOrdering = new ColumnOrdering(analysisResults.columnOrdering);
		this.formatLookup = new FormatLookup(analysisResults.formatLookup);
	}

	public int getCorrIntervalId() {
		return corrIntervalId;
	}

	public void setCorrIntervalId(int corrIntervalId) {
		this.corrIntervalId = corrIntervalId;
	}

	public int getDataAnalysisId() {
		return dataAnalysisId;
	}

	public void setDataAnalysisId(int dataAnalysisId) {
		this.dataAnalysisId = dataAnalysisId;
	}

	public ScratchPad<ReplicatePad> getScratchPad() {
		return scratchPad;
	}

	public void setScratchPad(ScratchPad<ReplicatePad> scratchPad) {
		this.scratchPad = scratchPad;
	}

	public ColumnOrdering getColumnOrdering() {
		return columnOrdering;
	}

	public void setColumnOrdering(ColumnOrdering columnOrdering) {
		this.columnOrdering = columnOrdering;
	}

	public FormatLookup getFormatLookup() {
		return formatLookup;
	}

	public void setFormatLookup(FormatLookup formatLookup) {
		this.formatLookup = formatLookup;
	}
}
