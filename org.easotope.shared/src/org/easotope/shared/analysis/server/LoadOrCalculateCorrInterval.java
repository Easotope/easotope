/*
 * Copyright © 2016 by Devon Bowen.
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.Messages;
import org.easotope.shared.analysis.execute.AnalysisWithParameters;
import org.easotope.shared.analysis.execute.CalculationError;
import org.easotope.shared.analysis.execute.calculator.AllStandardsCalculator;
import org.easotope.shared.analysis.execute.calculator.AnalysisConstants;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.CorrIntervalScratchPad;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.scratchpad.AnalysisIdentifier;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ReplicatePad.ReplicateType;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.InputParameterType;
import org.easotope.shared.rawdata.RawDataHelper;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.ScanV2;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class LoadOrCalculateCorrInterval {
	private static HashMap<Integer,String> mzX10ToX2 = new HashMap<Integer,String>();
	private static HashMap<Integer,String> mzX10ToSlope = new HashMap<Integer,String>();
	private static HashMap<Integer,String> mzX10ToIntercept = new HashMap<Integer,String>();

	private int corrIntervalId;
	private int replicateAnalysisId;
	private ConnectionSource connectionSource;

	private CorrIntervalScratchPad corrIntervalScratchPad = null;
	private List<CorrIntervalError> corrIntervalErrors = null;

	public LoadOrCalculateCorrInterval(int corrIntervalId, int replicateAnalysisId, ConnectionSource connectionSource) {
		this.corrIntervalId = corrIntervalId;
		this.replicateAnalysisId = replicateAnalysisId;
		this.connectionSource = connectionSource;

		try {
			Dao<CorrIntervalError,Integer> corrIntervalErrorDao = DaoManager.createDao(connectionSource, CorrIntervalError.class);

			HashMap<String,Object> map = new HashMap<String,Object>();
			map.put(CorrIntervalScratchPad.CORR_INTERVAL_ID_FIELD_NAME, corrIntervalId);
			map.put(CorrIntervalScratchPad.DATA_ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);
			corrIntervalErrors = corrIntervalErrorDao.queryForFieldValues(map);

		} catch (SQLException e) {
			String message = MessageFormat.format(Messages.recalculateCorrInterval_errorLoadingCorrIntervalError, corrIntervalId, replicateAnalysisId);
			Log.getInstance().log(Level.INFO, this, message, e);
		}

		List<CorrIntervalScratchPad> corrIntervalScratchPadList = null;

		try {
			Dao<CorrIntervalScratchPad,Integer> corrIntervalScratchPadDao = DaoManager.createDao(connectionSource, CorrIntervalScratchPad.class);

			HashMap<String,Object> map = new HashMap<String,Object>();
			map.put(CorrIntervalScratchPad.CORR_INTERVAL_ID_FIELD_NAME, corrIntervalId);
			map.put(CorrIntervalScratchPad.DATA_ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);
			corrIntervalScratchPadList = corrIntervalScratchPadDao.queryForFieldValues(map);

		} catch (SQLException e) {
			String message = MessageFormat.format(Messages.recalculateCorrInterval_errorLoadingCorrIntervalScratchPad, corrIntervalId, replicateAnalysisId);
			Log.getInstance().log(Level.INFO, this, message, e);
		}

		boolean hasScratchPad = corrIntervalScratchPadList != null && corrIntervalScratchPadList.size() == 1;
		boolean hasErrors = corrIntervalErrors != null && corrIntervalErrors.size() != 0;

		if (hasScratchPad) {
			corrIntervalScratchPad = corrIntervalScratchPadList.get(0);
		}

		if (!hasScratchPad && !hasErrors) {
			if (corrIntervalScratchPadList != null && corrIntervalScratchPadList.size() > 1) {
				removeCorrIntervalCalculations(corrIntervalId, replicateAnalysisId, connectionSource);
			}

			recalculateCorrInterval();
		}

		AbstractCache.clearCachesForThisThread();
	}

	public CorrIntervalScratchPad getCorrIntervalScratchPad() {
		return corrIntervalScratchPad;
	}

	public List<CorrIntervalError> getCorrIntervalError() {
		return corrIntervalErrors;
	}

	public static boolean removeCorrIntervalCalculations(int corrIntervalId, int replicateAnalysisId, ConnectionSource connectionSource) {
		int recordsRemoved = 0;

		try {
			Dao<CorrIntervalScratchPad,Integer> corrIntervalScratchPadDao = DaoManager.createDao(connectionSource, CorrIntervalScratchPad.class);
			DeleteBuilder<CorrIntervalScratchPad,Integer> deleteBuilder = corrIntervalScratchPadDao.deleteBuilder();

			Where<CorrIntervalScratchPad,Integer> where = deleteBuilder.where();
			where = where.eq(CorrIntervalScratchPad.CORR_INTERVAL_ID_FIELD_NAME, corrIntervalId);
			where = where.and();
			where = where.eq(CorrIntervalScratchPad.DATA_ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);

			PreparedDelete<CorrIntervalScratchPad> preparedDelete = deleteBuilder.prepare();
			recordsRemoved += corrIntervalScratchPadDao.delete(preparedDelete);

		} catch (SQLException e) {
			String message = MessageFormat.format(Messages.recalculateCorrInterval_errorWhileRemovingScratchPad, corrIntervalId, replicateAnalysisId);
			Log.getInstance().log(Level.INFO, LoadOrCalculateCorrInterval.class, message, e);
		}

		try {
			Dao<CorrIntervalError,Integer> corrIntervalErrorDao = DaoManager.createDao(connectionSource, CorrIntervalError.class);
			DeleteBuilder<CorrIntervalError,Integer> deleteBuilder = corrIntervalErrorDao.deleteBuilder();

			Where<CorrIntervalError,Integer> where = deleteBuilder.where();
			where = where.eq(CorrIntervalError.CORR_INTERVAL_ID_FIELD_NAME, corrIntervalId);
			where = where.and();
			where = where.eq(CorrIntervalError.DATA_ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);

			PreparedDelete<CorrIntervalError> preparedDelete = deleteBuilder.prepare();
			recordsRemoved += corrIntervalErrorDao.delete(preparedDelete);

		} catch (SQLException e) {
			String message = MessageFormat.format(Messages.recalculateCorrInterval_errorWhileRemovingErrors, corrIntervalId, replicateAnalysisId);
			Log.getInstance().log(Level.INFO, LoadOrCalculateCorrInterval.class, message, e);
		}

		return recordsRemoved != 0;
	}

	private void recalculateCorrInterval() {
		corrIntervalScratchPad = null;
		corrIntervalErrors = new ArrayList<CorrIntervalError>();

		CorrIntervalV1 corrInterval = null;

		try {
			Dao<CorrIntervalV1,Integer> corrIntervalDao = DaoManager.createDao(connectionSource, CorrIntervalV1.class);
			corrInterval = corrIntervalDao.queryForId(corrIntervalId);

		} catch (SQLException e) {
			String message = MessageFormat.format(Messages.recalculateCorrInterval_couldNotFindCorrInterval, corrIntervalId);
			Log.getInstance().log(Level.INFO, this, message, e);
			writeErrorToDb(message);
			return;
		}

		CorrIntervalScratchPad corrIntervalScratchPad = new CorrIntervalScratchPad();
		corrIntervalScratchPad.setCorrIntervalId(corrIntervalId);
		corrIntervalScratchPad.setDataAnalysisId(replicateAnalysisId);
		corrIntervalScratchPad.setScratchPad(new ScratchPad<ReplicatePad>());

		AnalysisWithParameters replicateAnalysisWithParameters = null;

		try {
			Dao<RepAnalysis,Integer> replicateAnalysisDao = DaoManager.createDao(connectionSource, RepAnalysis.class);
			RepAnalysis replicateAnalysis = replicateAnalysisDao.queryForId(replicateAnalysisId);

			Dao<RepStep,Integer> repStepDao = DaoManager.createDao(connectionSource, RepStep.class);
			List<RepStep> repSteps = repStepDao.queryForEq(RepStep.ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);

			Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);
			QueryBuilder<RepStepParams,Integer> queryBuilder = repStepParamsDao.queryBuilder();

			Where<RepStepParams,Integer> where = queryBuilder.where();
			where = where.eq(RepStepParams.CORR_INTERVAL_ID_FIELD_NAME, corrIntervalId);
			where = where.and();
			where = where.eq(RepStepParams.ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);

			PreparedQuery<RepStepParams> preparedQuery = queryBuilder.prepare();
			List<RepStepParams> repStepParams = repStepParamsDao.query(preparedQuery);

			replicateAnalysisWithParameters = new AnalysisWithParameters(replicateAnalysis, repSteps, repStepParams);

		} catch (SQLException e) {
			String message = MessageFormat.format(Messages.recalculateCorrInterval_couldNotLoadDataAnalysisWithParameters, corrIntervalId, replicateAnalysisId);
			Log.getInstance().log(Level.INFO, this, message, e);
			writeErrorToDb(message);
			return;
		}

		if (replicateAnalysisWithParameters.getErrors() != null && replicateAnalysisWithParameters.getErrors().size() != 0) {
			for (String message : replicateAnalysisWithParameters.getErrors()) {
				Log.getInstance().log(Level.INFO, this, message);
				writeErrorToDb(message);
			}

			return;
		}

		corrIntervalScratchPad.setColumnOrdering(new ColumnOrdering());
		corrIntervalScratchPad.getColumnOrdering().add(replicateAnalysisWithParameters.getGeneratedOutputColumns());
		corrIntervalScratchPad.setFormatLookup(new FormatLookup());
		corrIntervalScratchPad.getFormatLookup().add(AnalysisIdentifier.Level.REPLICATE, replicateAnalysisWithParameters.getAnalysis().getName(), replicateAnalysisWithParameters.getOutputColumnToFormat());

		readStandards(corrInterval, corrIntervalScratchPad);
		readScans(corrInterval, corrIntervalScratchPad);

		AllStandardsCalculator allStandardsCalculator = new AllStandardsCalculator(replicateAnalysisWithParameters, corrIntervalScratchPad.getScratchPad());
		allStandardsCalculator.execute();

		if (!allStandardsCalculator.isFinished()) {
			HashSet<Pad> unwanted = new HashSet<Pad>(corrIntervalScratchPad.getScratchPad().getChildren());
			corrIntervalScratchPad.getScratchPad().removeChildren(unwanted);
			writeErrorToDb(Messages.recalculateCorrInterval_errorStandardsCalculatorDidNotFinish);

		} else {
			for (CalculationError calculationError : allStandardsCalculator.getErrors()) {
				ReplicateV1 replicate = (ReplicateV1) calculationError.getPad().getVolatileData(AnalysisConstants.VOLATILE_DATA_REPLICATE);

				CorrIntervalError corrIntervalError = new CorrIntervalError();
				corrIntervalError.setCorrIntervalId(corrIntervalId);
				corrIntervalError.setDataAnalysislId(replicateAnalysisId);
				corrIntervalError.setReplicateId(replicate.getId());
				corrIntervalError.setReplicateDate(replicate.getDate());
				corrIntervalError.setReplicateUserId(replicate.getUserId());
				corrIntervalError.setRepStepName(calculationError.getStepController().getStepName());
				corrIntervalError.setErrorMessage(calculationError.getErrorMessage());
				writeErrorToDb(corrIntervalError);
			}

			HashSet<Pad> unwanted = new HashSet<Pad>();

			for (ReplicatePad replicatePad : corrIntervalScratchPad.getScratchPad().getChildren()) {
				if (replicatePad.getVolatileData(AnalysisConstants.VOLATILE_DATA_HAS_ERRORS) != null) {
					unwanted.add(replicatePad);
				} else {
					replicatePad.trimChildren();
				}
			}
	
			corrIntervalScratchPad.getScratchPad().removeChildren(unwanted);
		}

		try {
			Dao<CorrIntervalScratchPad,Integer> corrIntervalScratchPadDao = DaoManager.createDao(connectionSource, CorrIntervalScratchPad.class);
			corrIntervalScratchPadDao.create(corrIntervalScratchPad);
			this.corrIntervalScratchPad = corrIntervalScratchPad;

		} catch (SQLException e) {
			String message = MessageFormat.format(Messages.correctionIntervalUpdatedHandler_scratchPadSaveError, corrIntervalId, replicateAnalysisId);
			Log.getInstance().log(Level.INFO, this, message);
		}
	}

	private void readStandards(CorrIntervalV1 corrInterval, CorrIntervalScratchPad coreIntervalScratchPad) {
		try {
			Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
			QueryBuilder<ReplicateV1,Integer> queryBuilder = replicateDao.queryBuilder();

			Where<ReplicateV1,Integer> where = queryBuilder.where();
			where = where.eq(ReplicateV1.MASSSPECID_FIELD_NAME, corrInterval.getMassSpecId());
			where = where.and();
			where = where.ne(ReplicateV1.STANDARDID_FIELD_NAME, DatabaseConstants.EMPTY_DB_ID);
			where = where.and();
			where = where.ge(ReplicateV1.DATE_FIELD_NAME, corrInterval.getValidFrom());

			if (corrInterval.getValidUntil() != DatabaseConstants.MAX_DATE) {
				where = where.and();
				where = where.lt(ReplicateV1.DATE_FIELD_NAME, corrInterval.getValidUntil());
			}

			PreparedQuery<ReplicateV1> preparedQuery = queryBuilder.prepare();
			List<ReplicateV1> results = replicateDao.query(preparedQuery);

			for (ReplicateV1 replicate : results) {
				try {
					List<Acquisition> acquisitions = GetFromDb.getAcquisitions(connectionSource, replicate.getId());
					int replicateNumber = RawDataHelper.addReplicateToScratchPad(coreIntervalScratchPad.getScratchPad(), replicate, acquisitions);
					coreIntervalScratchPad.getScratchPad().getChild(replicateNumber).setVolatileData(AnalysisConstants.VOLATILE_DATA_REPLICATE, replicate);

				} catch (Exception e) {
					String message = MessageFormat.format(Messages.recalculateCorrInterval_errorReadingStandards, replicate.getId(), corrInterval.getId());
					Log.getInstance().log(Level.INFO, this, message, e);
					writeErrorToDb(message);
				}
			}

		} catch (Exception e) {
			String message = MessageFormat.format(Messages.recalculateCorrInterval_errorReadingStandardList, corrInterval.getId());
			Log.getInstance().log(Level.INFO, this, message, e);
			writeErrorToDb(message);
		}
	}

	private void readScans(CorrIntervalV1 corrInterval, CorrIntervalScratchPad coreIntervalScratchPad) {
		try {
			Dao<ScanV2,Integer> scanDao = DaoManager.createDao(connectionSource, ScanV2.class);
			QueryBuilder<ScanV2,Integer> queryBuilder = scanDao.queryBuilder();

			Where<ScanV2,Integer> where = queryBuilder.where();
			where = where.eq(ScanV2.MASSSPECID_FIELD_NAME, corrInterval.getMassSpecId());
			where = where.and();
			where = where.ge(ScanV2.DATE_FIELD_NAME, corrInterval.getValidFrom());

			if (corrInterval.getValidUntil() != DatabaseConstants.MAX_DATE) {
				where = where.and();
				where = where.lt(ReplicateV1.DATE_FIELD_NAME, corrInterval.getValidUntil());
			}

			PreparedQuery<ScanV2> preparedQuery = queryBuilder.prepare();
			List<ScanV2> results = scanDao.query(preparedQuery);

			for (ScanV2 scan : results) {
				ReplicatePad replicatePad = new ReplicatePad(coreIntervalScratchPad.getScratchPad(), scan.getDate(), ReplicateType.SCAN);
				replicatePad.setVolatileData(AnalysisConstants.VOLATILE_DATA_SCAN, scan);
				replicatePad.setValue(Pad.DISABLED, scan.isDisabled());

				int count=0;
				for (int i=0; i<scan.getChannelToMzX10().length; i++) {
					if (scan.getChannelToMzX10()[i] == null) {
						continue;
					}

					Double x2 = scan.getX2Coeff()[count];
					Double slope = scan.getSlope()[count];
					Double intercept = scan.getIntercept()[count];
					count++;

					if (Double.isNaN(slope) || Double.isNaN(intercept)) {
						continue;
					}

					replicatePad.setValue(mzX10ToX2.get(scan.getChannelToMzX10()[i]), x2);
					replicatePad.setValue(mzX10ToSlope.get(scan.getChannelToMzX10()[i]), slope);
					replicatePad.setValue(mzX10ToIntercept.get(scan.getChannelToMzX10()[i]), intercept);
				}
			}

		} catch (Exception e) {
			String message = MessageFormat.format(Messages.recalculateCorrInterval_errorReadingScanList, corrInterval.getId());
			Log.getInstance().log(Level.INFO, this, message, e);
			writeErrorToDb(message);
		}
	}

	private CorrIntervalError writeErrorToDb(String message) {
		CorrIntervalError corrIntervalError = new CorrIntervalError();
		corrIntervalError.setCorrIntervalId(corrIntervalId);
		corrIntervalError.setDataAnalysislId(replicateAnalysisId);
		corrIntervalError.setErrorMessage(message);

		writeErrorToDb(corrIntervalError);

		return corrIntervalError;
	}

	private void writeErrorToDb(CorrIntervalError corrIntervalError) {		
		try {
			Dao<CorrIntervalError,Integer> corrIntervalErrorDao = DaoManager.createDao(connectionSource, CorrIntervalError.class);
			corrIntervalErrorDao.create(corrIntervalError);

		} catch (SQLException e) {
			Log.getInstance().log(Level.INFO, this, Messages.recalculateCorrInterval_couldNotSaveErrorToDb + corrIntervalError.getErrorMessage(), e);
		}

		corrIntervalErrors.add(corrIntervalError);
	}

	static {
		for (InputParameter inputParameter : InputParameter.values()) {
			if (inputParameter.getInputParameterType() == InputParameterType.X2Coeff) {
				mzX10ToX2.put(inputParameter.getMzX10(), inputParameter.toString());
			}

			if (inputParameter.getInputParameterType() == InputParameterType.X1Coeff) {
				mzX10ToSlope.put(inputParameter.getMzX10(), inputParameter.toString());
			}

			if (inputParameter.getInputParameterType() == InputParameterType.X0Coeff) {
				mzX10ToIntercept.put(inputParameter.getMzX10(), inputParameter.toString());
			}
		}
	}
}
