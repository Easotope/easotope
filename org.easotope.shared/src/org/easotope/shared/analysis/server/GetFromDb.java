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

package org.easotope.shared.analysis.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.analysis.tables.SamAnalysis;
import org.easotope.shared.analysis.tables.SamStep;
import org.easotope.shared.analysis.tables.SamStepParams;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class GetFromDb {
	public static RepAnalysis getRepAnalysis(ConnectionSource connectionSource, int repAnalysisId) throws SQLException {
		Dao<RepAnalysis,Integer> repAnalysisDao = DaoManager.createDao(connectionSource, RepAnalysis.class);
		return repAnalysisDao.queryForId(repAnalysisId);
	}

	public static List<RepStep> getRepSteps(ConnectionSource connectionSource, int repAnalysisId) throws SQLException {
		Dao<RepStep,Integer> repStepDao = DaoManager.createDao(connectionSource, RepStep.class);
		return repStepDao.queryForEq(RepStep.ANALYSIS_ID_FIELD_NAME, repAnalysisId);
	}

	public static List<RepStepParams> getRepStepParameters(ConnectionSource connectionSource, int corrIntervalId, int replicateAnalysisId) throws SQLException {
		Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);
		QueryBuilder<RepStepParams,Integer> queryBuilder1 = repStepParamsDao.queryBuilder();

		Where<RepStepParams,Integer> where1 = queryBuilder1.where();
		where1 = where1.eq(RepStepParams.CORR_INTERVAL_ID_FIELD_NAME, corrIntervalId);
		where1 = where1.and();
		where1 = where1.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);

		PreparedQuery<RepStepParams> preparedQuery1 = queryBuilder1.prepare();
		return repStepParamsDao.query(preparedQuery1);
	}

	public static SamAnalysis getSamAnalysis(ConnectionSource connectionSource, int samAnalysisId) throws SQLException {
		Dao<SamAnalysis,Integer> samAnalysisDao = DaoManager.createDao(connectionSource, SamAnalysis.class);
		return samAnalysisDao.queryForId(samAnalysisId);
	}

	public static List<SamStep> getSamSteps(ConnectionSource connectionSource, int samAnalysisId) throws SQLException {
		Dao<SamStep,Integer> samStepDao = DaoManager.createDao(connectionSource, SamStep.class);
		return samStepDao.queryForEq(RepStep.ANALYSIS_ID_FIELD_NAME, samAnalysisId);
	}

	public static List<SamStepParams> getSamStepParameters(ConnectionSource connectionSource, int sampleAnalysisId) throws SQLException {
		return new ArrayList<SamStepParams>();
	}

//	public static CorrIntervalScratchPad getCorrIntervalScratchPad(ConnectionSource connectionSource, int corrIntervalId, int replicateAnalysisId) throws SQLException {
//		Dao<CorrIntervalScratchPad,Integer> corrIntervalScratchPadDao = DaoManager.createDao(connectionSource, CorrIntervalScratchPad.class);
//		QueryBuilder<CorrIntervalScratchPad,Integer> queryBuilder2 = corrIntervalScratchPadDao.queryBuilder();
//
//		Where<CorrIntervalScratchPad,Integer> where2 = queryBuilder2.where();
//		where2 = where2.eq(CorrIntervalScratchPad.CORR_INTERVAL_ID_FIELD_NAME, corrIntervalId);
//		where2 = where2.and();
//		where2 = where2.eq(CorrIntervalScratchPad.DATA_ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);
//
//		PreparedQuery<CorrIntervalScratchPad> preparedQuery2 = queryBuilder2.prepare();
//		List<CorrIntervalScratchPad> results = corrIntervalScratchPadDao.query(preparedQuery2);
//		return results.size() == 0 ? null : results.get(0);
//	}
//
//	public static List<CorrIntervalError> getCorrIntervalErrors(ConnectionSource connectionSource, int corrIntervalId, int replicateAnalysisId) throws SQLException {
//		Dao<CorrIntervalError,Integer> corrIntervalErrorDao = DaoManager.createDao(connectionSource, CorrIntervalError.class);
//		QueryBuilder<CorrIntervalError,Integer> queryBuilder3 = corrIntervalErrorDao.queryBuilder();
//
//		Where<CorrIntervalError,Integer> where3 = queryBuilder3.where();
//		where3 = where3.eq(CorrIntervalError.CORR_INTERVAL_ID_FIELD_NAME, corrIntervalId);
//		where3 = where3.and();
//		where3 = where3.eq(CorrIntervalError.DATA_ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);
//
//		PreparedQuery<CorrIntervalError> preparedQuery3 = queryBuilder3.prepare();
//		return corrIntervalErrorDao.query(preparedQuery3);
//	}

	public static ArrayList<Acquisition> getAcquisitions(ConnectionSource connectionSource, int replicateId) throws SQLException {
		Dao<AcquisitionInputV0,Integer> acquisitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInputV0.class);
		List<AcquisitionInputV0> acquisitionInputs = acquisitionInputDao.queryForEq(AcquisitionInputV0.REPLICATEID_FIELD_NAME, replicateId);

		ArrayList<Acquisition> acquisitions = new ArrayList<Acquisition>();
		Dao<AcquisitionParsedV2,Integer> acquisitionParsedDao = DaoManager.createDao(connectionSource, AcquisitionParsedV2.class);

		for (AcquisitionInputV0 acquisitionInput : acquisitionInputs) {
			if (acquisitionInput.getOffPeakCycles() == null) {
				acquisitionInput.setOffPeakCycles(new boolean[acquisitionInput.getDisabledCycles().length]);
			}
			AcquisitionParsedV2 acquisitionParsed = acquisitionParsedDao.queryForId(acquisitionInput.getAcquisitionParsedId());
			acquisitions.add(new Acquisition(null, null, acquisitionParsed, acquisitionInput));
		}

		return acquisitions;
	}
}
