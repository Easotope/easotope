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

import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=CalcReplicateCache.TABLE_NAME)
public class CalcReplicateCache extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "CALCREPLICATECACHE_V0";
	public static final String REPLICATE_ID_FIELD_NAME = "REPLICATEID";
	public static final String REPLICATE_ANALYSIS_ID_FIELD_NAME = "REPLICATEANALYSISID";
	public static final String SCRATCH_PAD_FIELD_NAME = "SCRATCHPAD";
	public static final String DEPENDENCIES_FIELD_NAME = "DEPENDENCIES";
	public static final String ERROR_REP_STEP_FIELD_NAME = "ERRORREPSTEPLABEL";
	public static final String ERROR_MESSAGE_FIELD_NAME = "ERRORMESSAGE";
	public static final String COLUMN_ORDERING_FIELD_NAME = "COLUMN_ORDERING";
	public static final String FORMAT_LOOKUP_FIELD_NAME = "FORMAT_LOOKUP";
	public static final String CORR_INTERVAL_ID_FIELD_NAME = "CORRINTERVALID";

	@DatabaseField(columnName=REPLICATE_ID_FIELD_NAME, indexName="calcreplicatecache_replicateidanalysisid_idx")
	private int replicateId;
	@DatabaseField(columnName=REPLICATE_ANALYSIS_ID_FIELD_NAME, indexName="calcreplicatecache_replicateidanalysisid_idx")
	private int replicateAnalysisId;
	@DatabaseField(columnName=SCRATCH_PAD_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private ScratchPad<ReplicatePad> scratchPad;
	@DatabaseField(columnName=DEPENDENCIES_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private ArrayList<HashMap<String,String>> dependencies;
	@DatabaseField(columnName=ERROR_REP_STEP_FIELD_NAME)
	private String errorRepStep;
	@DatabaseField(columnName=ERROR_MESSAGE_FIELD_NAME)
	private String errorMessage;
	@DatabaseField(columnName=COLUMN_ORDERING_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private ColumnOrdering columnOrdering;
	@DatabaseField(columnName=FORMAT_LOOKUP_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private FormatLookup formatLookup;
	@DatabaseField(columnName=CORR_INTERVAL_ID_FIELD_NAME)
	private int corrIntervalId;

	public CalcReplicateCache() { }

	public CalcReplicateCache(CalcReplicateCache calcSampleCache) {
		super(calcSampleCache);

		this.replicateId = calcSampleCache.replicateId;
		this.replicateAnalysisId = calcSampleCache.replicateAnalysisId;
		this.scratchPad = calcSampleCache.scratchPad == null ? null : new ScratchPad<ReplicatePad>(calcSampleCache.scratchPad);

		if (calcSampleCache.dependencies != null) {
			this.dependencies = new ArrayList<HashMap<String,String>>();

			for (HashMap<String,String> map : calcSampleCache.dependencies) {
				this.dependencies.add(new HashMap<String,String>(map));
			}

		} else {
			this.dependencies = null;
		}

		this.errorRepStep = calcSampleCache.errorRepStep;
		this.errorMessage = calcSampleCache.errorMessage;
		this.columnOrdering = calcSampleCache.columnOrdering == null ? null : new ColumnOrdering(calcSampleCache.columnOrdering);
		this.formatLookup = calcSampleCache.formatLookup == null ? null : new FormatLookup(calcSampleCache.formatLookup);
		this.corrIntervalId = calcSampleCache.corrIntervalId;
	}

	public int getReplicateId() {
		return replicateId;
	}

	public void setReplicateId(int replicateId) {
		this.replicateId = replicateId;
	}

	public int getReplicateAnalysisId() {
		return replicateAnalysisId;
	}

	public void setReplicateAnalysisId(int replicateAnalysisId) {
		this.replicateAnalysisId = replicateAnalysisId;
	}

	public ScratchPad<ReplicatePad> getScratchPad() {
		return scratchPad;
	}

	public void setScratchPad(ScratchPad<ReplicatePad> scratchPad) {
		this.scratchPad = scratchPad;
	}

	public ArrayList<HashMap<String, String>> getDependencies() {
		return dependencies;
	}

	public void setDependencies(ArrayList<HashMap<String, String>> dependencies) {
		this.dependencies = dependencies;
	}

	public String getErrorRepStep() {
		return errorRepStep;
	}

	public void setErrorRepStep(String errorRepStep) {
		this.errorRepStep = errorRepStep;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
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

	public int getCorrIntervalId() {
		return corrIntervalId;
	}

	public void setCorrIntervalId(int corrIntervalId) {
		this.corrIntervalId = corrIntervalId;
	}
}
