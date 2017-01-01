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
import java.util.Arrays;
import java.util.HashMap;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.core.scratchpad.ScratchPad;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=CalcSampleCache.TABLE_NAME)
public class CalcSampleCache extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "CALCSAMPLECACHE_V0";
	public static final String SAMPLEID_FIELD_NAME = "SAMPLEID";
	public static final String SAMPLE_ANALYSIS_ID_FIELD_NAME = "SAMPLEANALYSISID";
	public static final String SCRATCH_PAD_FIELD_NAME = "SCRATCHPAD";
	public static final String DEPENDENCIES_FIELD_NAME = "DEPENDENCIES";
	public static final String ERROR_SAM_STEP_FIELD_NAME = "ERRORSAMSTEPLABEL";
	public static final String ERROR_MESSAGE_FIELD_NAME = "ERRORMESSAGE";
	public static final String COLUMN_ORDERING_FIELD_NAME = "COLUMNORDERING";
	public static final String FORMAT_LOOKUP_FIELD_NAME = "FORMATLOOKUP";
	public static final String CORR_INTERVALS_IDS_FIELD_NAME = "CORRINTERVALIDS";
	public static final String REP_ANALYSIS_IDS_FIELD_NAME = "REPANALYSISIDS";
	public static final String REPLICATE_IDS_FIELD_NAME = "REPLICATEIDS";
	public static final String POTENTIAL_REP_ANALYSES_FIELD_NAME = "POTENTIALREPANALYSES";

	@DatabaseField(columnName=SAMPLEID_FIELD_NAME, indexName="calcsamplecache_sampleidanalysisid_idx")
	private int sampleId;
	@DatabaseField(columnName=SAMPLE_ANALYSIS_ID_FIELD_NAME, indexName="calcsamplecache_sampleidanalysisid_idx")
	private int sampleAnalysisId;
	@DatabaseField(columnName=SCRATCH_PAD_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private ScratchPad<SamplePad> scratchPad;
	@DatabaseField(columnName=DEPENDENCIES_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private ArrayList<HashMap<String,String>> dependencies;
	@DatabaseField(columnName=ERROR_SAM_STEP_FIELD_NAME)
	private String errorSamStep;
	@DatabaseField(columnName=ERROR_MESSAGE_FIELD_NAME)
	private String errorMessage;
	@DatabaseField(columnName=COLUMN_ORDERING_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private ColumnOrdering columnOrdering;
	@DatabaseField(columnName=FORMAT_LOOKUP_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private FormatLookup formatLookup;
	@DatabaseField(columnName=CORR_INTERVALS_IDS_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private int[] corrIntervalIds;
	@DatabaseField(columnName=REP_ANALYSIS_IDS_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private int[] repAnalysisIds;
	@DatabaseField(columnName=REPLICATE_IDS_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private int[] replicateIds;
	@DatabaseField(columnName=POTENTIAL_REP_ANALYSES_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private int[][] potentialRepAnalyses;

	public CalcSampleCache() { }

	public CalcSampleCache(CalcSampleCache calcSampleCache) {
		super(calcSampleCache);

		this.sampleId = calcSampleCache.sampleId;
		this.sampleAnalysisId = calcSampleCache.sampleAnalysisId;
		this.scratchPad = new ScratchPad<SamplePad>(calcSampleCache.scratchPad);
		
		if (calcSampleCache.dependencies != null) {
			this.dependencies = new ArrayList<HashMap<String,String>>();

			for (HashMap<String,String> map : calcSampleCache.dependencies) {
				this.dependencies.add(new HashMap<String,String>(map));
			}

		} else {
			this.dependencies = null;
		}

		this.errorSamStep = calcSampleCache.errorSamStep;
		this.errorMessage = calcSampleCache.errorMessage;
		this.corrIntervalIds = Arrays.copyOf(calcSampleCache.corrIntervalIds, calcSampleCache.corrIntervalIds.length);
		this.columnOrdering = new ColumnOrdering(calcSampleCache.columnOrdering);
		this.formatLookup = new FormatLookup(calcSampleCache.formatLookup);
		this.repAnalysisIds = Arrays.copyOf(calcSampleCache.repAnalysisIds, calcSampleCache.repAnalysisIds.length);

		if (calcSampleCache.potentialRepAnalyses == null) {
			this.potentialRepAnalyses = null;

		} else {
			this.potentialRepAnalyses = new int[calcSampleCache.potentialRepAnalyses.length][];

			for (int i=0; i<calcSampleCache.potentialRepAnalyses.length; i++) {
				if (calcSampleCache.potentialRepAnalyses[i] != null) {
					this.potentialRepAnalyses[i] = Arrays.copyOf(calcSampleCache.potentialRepAnalyses[i], calcSampleCache.potentialRepAnalyses[i].length);
				}
			}
		}
	}

	public int getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}

	public int getSampleAnalysisId() {
		return sampleAnalysisId;
	}

	public void setSampleAnalysisId(int sampleAnalysisId) {
		this.sampleAnalysisId = sampleAnalysisId;
	}

	public ScratchPad<SamplePad> getScratchPad() {
		return scratchPad;
	}

	public void setScratchPad(ScratchPad<SamplePad> scratchPad) {
		this.scratchPad = scratchPad;
	}

	public ArrayList<HashMap<String, String>> getDependencies() {
		return dependencies;
	}

	public void setDependencies(ArrayList<HashMap<String, String>> dependencies) {
		this.dependencies = dependencies;
	}

	public String getErrorSamStep() {
		return errorSamStep;
	}

	public void setErrorSamStep(String errorSamStep) {
		this.errorSamStep = errorSamStep;
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

	public int[] getCorrIntervalIds() {
		return corrIntervalIds;
	}

	public void setCorrIntervalIds(int[] corrIntervalIds) {
		this.corrIntervalIds = corrIntervalIds;
	}

	public int[] getRepAnalysisIds() {
		return repAnalysisIds;
	}

	public void setRepAnalysisIds(int[] repAnalysisIds) {
		this.repAnalysisIds = repAnalysisIds;
	}

	public int[] getReplicateIds() {
		return replicateIds;
	}

	public void setReplicateIds(int[] replicateIds) {
		this.replicateIds = replicateIds;
	}

	public int[][] getPotentialRepAnalyses() {
		return potentialRepAnalyses;
	}

	public void setPotentialRepAnalyses(int[][] potentialRepAnalyses) {
		this.potentialRepAnalyses = potentialRepAnalyses;
	}
}
