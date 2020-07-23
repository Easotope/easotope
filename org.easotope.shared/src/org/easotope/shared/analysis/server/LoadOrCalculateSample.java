/*
 * Copyright © 2016-2020 by Devon Bowen.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.easotope.framework.core.global.OptionsInfo;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.shared.Messages;
import org.easotope.shared.analysis.execute.AnalysisWithParameters;
import org.easotope.shared.analysis.execute.CalculationError;
import org.easotope.shared.analysis.execute.calculator.AnalysisConstants;
import org.easotope.shared.analysis.execute.calculator.SingleReplicateCalculator;
import org.easotope.shared.analysis.execute.calculator.SingleSampleCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.analysis.tables.CalcRepToCalcSamp;
import org.easotope.shared.analysis.tables.CalcReplicateCache;
import org.easotope.shared.analysis.tables.CalcSampleCache;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepAnalysisChoice;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.analysis.tables.SamAnalysis;
import org.easotope.shared.analysis.tables.SamStep;
import org.easotope.shared.analysis.tables.SamStepParams;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.scratchpad.AnalysisIdentifier;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.Pad.Status;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ReplicatePad.ReplicateType;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.RawDataHelper;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

public class LoadOrCalculateSample {
	private static final String CALC_REPLICATE_CACHE_IDS = "CALC_REPLICATE_CACHE_IDS";

	private ConnectionSource connectionSource;

	public LoadOrCalculateSample(ConnectionSource connectionSource) {
		this.connectionSource = connectionSource;
	}

	public CalcSampleCache getCalcSampleCache(int sampleId, int sampleAnalysisId) {
		try {
			Dao<CalcSampleCache,Integer> calcSampleDao = DaoManager.createDao(connectionSource, CalcSampleCache.class);

			HashMap<String,Object> queryMap = new HashMap<String,Object>();
			queryMap.put(CalcSampleCache.SAMPLEID_FIELD_NAME, sampleId);
			queryMap.put(CalcSampleCache.SAMPLE_ANALYSIS_ID_FIELD_NAME, sampleAnalysisId);
			List<CalcSampleCache> list = calcSampleDao.queryForFieldValues(queryMap);

			if (list != null && list.size() == 1) {
				return list.get(0);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_problemReadingCalcSampleCache, e);
		}

		CalcSampleCache calcSampleCache = createCalcSampleCache(sampleId, sampleAnalysisId);

		try {
			Dao<CalcSampleCache,Integer> calcSampleCacheDao = DaoManager.createDao(connectionSource, CalcSampleCache.class);
			calcSampleCacheDao.create(calcSampleCache);

			@SuppressWarnings("unchecked")
			HashSet<Integer> calcReplicateCacheIds = (HashSet<Integer>) calcSampleCache.getScratchPad().getVolatileData(CALC_REPLICATE_CACHE_IDS);

			if (calcReplicateCacheIds != null) {
				Dao<CalcRepToCalcSamp,Integer> calcRepToCalcSampDao = DaoManager.createDao(connectionSource, CalcRepToCalcSamp.class);

				for (int calcReplicateId : calcReplicateCacheIds) {
					CalcRepToCalcSamp calcRepToCalcSamp = new CalcRepToCalcSamp();

					calcRepToCalcSamp.setCalcSampleId(calcSampleCache.getId());
					calcRepToCalcSamp.setCalcReplicateId(calcReplicateId);

					calcRepToCalcSampDao.create(calcRepToCalcSamp);
				}
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_sampleSaveFailed, e);
		}

		AbstractCache.clearCachesForThisThread();

		return calcSampleCache;
	}

	private CalcSampleCache createCalcSampleCache(int sampleId, int sampleAnalysisId) {
		CalcSampleCache calcSampleCache = new CalcSampleCache();
		calcSampleCache.setSampleId(sampleId);
		calcSampleCache.setSampleAnalysisId(sampleAnalysisId);
		calcSampleCache.setColumnOrdering(new ColumnOrdering());
		calcSampleCache.setFormatLookup(new FormatLookup());
		calcSampleCache.setScratchPad(new ScratchPad<SamplePad>());

		HashSet<Integer> calcReplicateCacheIds = new HashSet<Integer>();
		calcSampleCache.getScratchPad().setVolatileData(CALC_REPLICATE_CACHE_IDS, calcReplicateCacheIds);

		ArrayList<String> list = new ArrayList<String>();
		list.add(Pad.ID);
		list.add(Pad.ANALYSIS);
		list.add(Pad.ANALYSIS_STATUS);
		calcSampleCache.getColumnOrdering().add(list);

		HashMap<Long,Integer> replicateIds = new HashMap<Long,Integer>();
		HashMap<Long,ArrayList<Integer>> potentialRepAnalysisIds = new HashMap<Long,ArrayList<Integer>>();
		ArrayList<Integer> corrIntervalIds = new ArrayList<Integer>();
		ArrayList<Integer> repAnalysisIds = new ArrayList<Integer>();

		Sample sample = null;
		SamAnalysis samAnalysis = null;

		try {
			Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
			sample = sampleDao.queryForId(sampleId);

			samAnalysis = GetFromDb.getSamAnalysis(connectionSource, sampleAnalysisId);

		} catch (SQLException e) {
			// ignore
		}

		if (sample == null || samAnalysis == null) {
			SamplePad samplePad = new SamplePad(calcSampleCache.getScratchPad(), Messages.loadOrCalculateSample_unknown);
			samplePad.setValue(Pad.ANALYSIS, Messages.loadOrCalculateSample_unknown);
			samplePad.setValue(Pad.ANALYSIS_STATUS, Status.ERROR);
			return calcSampleCache;
		}

		new SamplePad(calcSampleCache.getScratchPad(), sample.getName());

		List<ReplicateV1> replicateList = null;

		try {
			Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
			HashMap<String,Object> queryMap = new HashMap<String,Object>();
			queryMap.put(ReplicateV1.SAMPLEID_FIELD_NAME, sampleId);
			queryMap.put(ReplicateV1.DISABLED_FIELD_NAME, false);
			replicateList = replicateDao.queryForFieldValues(queryMap);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_problemReadingReplicateList, e);
			return calcSampleCache;
		}

		if (replicateList != null) {
			for (ReplicateV1 replicate : replicateList) {
				replicateIds.put(replicate.getDate(), replicate.getId());

				ArrayList<Integer> potentialRepAnalysesForThisReplicate = new ArrayList<Integer>();
				potentialRepAnalysisIds.put(replicate.getDate(), potentialRepAnalysesForThisReplicate);

				List<CorrIntervalV1> corrIntervalList = null;

				try {
					Dao<CorrIntervalV1,Integer> corrIntervalDao = DaoManager.createDao(connectionSource, CorrIntervalV1.class);
					QueryBuilder<CorrIntervalV1, Integer> queryBuilder = corrIntervalDao.queryBuilder();
					queryBuilder.where()
						.eq(CorrIntervalV1.MASSSPECID_FIELD_NAME, replicate.getMassSpecId())
						.and()
						.le(CorrIntervalV1.VALIDFROM_FIELD_NAME, replicate.getDate())
						.and()
						.gt(CorrIntervalV1.VALIDUNTIL_FIELD_NAME, replicate.getDate());
					PreparedQuery<CorrIntervalV1> preparedQuery = queryBuilder.prepare();

					corrIntervalList = corrIntervalDao.query(preparedQuery);

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_problemReadingCorrIntervalList, e);
					corrIntervalList = null;
				}

				if (corrIntervalList == null || corrIntervalList.size() == 0) {
					ReplicatePad pad = new ReplicatePad(calcSampleCache.getScratchPad().getChild(0), replicate.getDate(), replicate.getId(), ReplicateType.SAMPLE_RUN);
					pad.setValue(Pad.ANALYSIS, Messages.loadOrCalculateSample_unknown);
					pad.setValue(Pad.ANALYSIS_STATUS, Status.ERROR);
	
					continue;
				}

				CorrIntervalV1 corrInterval = corrIntervalList.get(0);
				HashSet<Integer> existingRepAnalyses = new HashSet<Integer>();

				for (int i : corrInterval.getDataAnalysis()) {
					potentialRepAnalysesForThisReplicate.add(i);
					existingRepAnalyses.add(i);
				}

				HashSet<Integer> potentialRepAnalyses = new HashSet<Integer>();

				for (int i: samAnalysis.getRepAnalyses()) {
					if (existingRepAnalyses.contains(i)) {
						potentialRepAnalyses.add(i);
					}
				}

				if (potentialRepAnalyses.size() == 0) {
					ReplicatePad pad = new ReplicatePad(calcSampleCache.getScratchPad().getChild(0), replicate.getDate(), replicate.getId(), ReplicateType.SAMPLE_RUN);
					pad.setValue(Pad.ANALYSIS, Messages.loadOrCalculateSample_unknown);
					pad.setValue(Pad.ANALYSIS_STATUS, Status.ERROR);

				} else if (potentialRepAnalyses.size() == 1) {
					Integer repAnalysisId = potentialRepAnalyses.toArray(new Integer[1])[0];
					CalcReplicateCache calcReplicateCache = getCalcReplicateCache(replicate, repAnalysisId, corrInterval);
					calcReplicateCache.getScratchPad().getChild(0).reassignToParent(calcSampleCache.getScratchPad().getChild(0));

					calcReplicateCacheIds.add(calcReplicateCache.getId());
					corrIntervalIds.add(calcReplicateCache.getCorrIntervalId());
					repAnalysisIds.add(calcReplicateCache.getReplicateAnalysisId());

					calcSampleCache.getColumnOrdering().add(calcReplicateCache.getColumnOrdering());
					calcSampleCache.getFormatLookup().add(calcReplicateCache.getFormatLookup());

				} else {
					List<RepAnalysisChoice> repAnalysisChoices = null;

					try {
						Dao<RepAnalysisChoice,Integer> repSelectionDao = DaoManager.createDao(connectionSource, RepAnalysisChoice.class);

						HashMap<String,Object> fields = new HashMap<String,Object>();
						fields.put(RepAnalysisChoice.SAMPLE_ID_FIELD_NAME, sample.getId());
						fields.put(RepAnalysisChoice.SAM_ANALYSIS_ID_FIELD_NAME, samAnalysis.getId());

						repAnalysisChoices = repSelectionDao.queryForFieldValues(fields);

					} catch (SQLException e) {
						// ignore
					}

					if (repAnalysisChoices != null && repAnalysisChoices.size() == 1 && potentialRepAnalyses.contains(repAnalysisChoices.get(0).getRepIdsToRepAnalysisChoice().get(replicate.getId()))) {
						int userSelectedRepAnalysisId = repAnalysisChoices.get(0).getRepIdsToRepAnalysisChoice().get(replicate.getId());
						CalcReplicateCache calcReplicateCache = getCalcReplicateCache(replicate, userSelectedRepAnalysisId, corrInterval);
						calcReplicateCache.getScratchPad().getChild(0).reassignToParent(calcSampleCache.getScratchPad().getChild(0));

						calcReplicateCacheIds.add(calcReplicateCache.getId());
						corrIntervalIds.add(calcReplicateCache.getCorrIntervalId());
						repAnalysisIds.add(calcReplicateCache.getReplicateAnalysisId());

						calcSampleCache.getColumnOrdering().add(calcReplicateCache.getColumnOrdering());
						calcSampleCache.getFormatLookup().add(calcReplicateCache.getFormatLookup());

					} else {
						ReplicatePad pad = new ReplicatePad(calcSampleCache.getScratchPad().getChild(0), replicate.getDate(), replicate.getId(), ReplicateType.SAMPLE_RUN);
						pad.setValue(Pad.ANALYSIS, Messages.loadOrCalculateSample_unknown);
						pad.setValue(Pad.ANALYSIS_STATUS, Status.ERROR);
					}
				}
			}
		}

		// run the sample calculator

		try {
			List<SamStep> samSteps = GetFromDb.getSamSteps(connectionSource, sampleAnalysisId);
			List<SamStepParams> samStepParameters = GetFromDb.getSamStepParameters(connectionSource, sampleAnalysisId);
			AnalysisWithParameters dataAnalysisWithParameters = new AnalysisWithParameters(samAnalysis, samSteps, samStepParameters);

			SingleSampleCalculator singleSampleCalculator = new SingleSampleCalculator(dataAnalysisWithParameters, calcSampleCache.getScratchPad());
			singleSampleCalculator.execute();

			if (singleSampleCalculator.isFinished() && singleSampleCalculator.getErrors().size() == 0) {
				calcSampleCache.getColumnOrdering().add(dataAnalysisWithParameters.getGeneratedOutputColumns());
				calcSampleCache.getFormatLookup().add(AnalysisIdentifier.Level.SAMPLE, dataAnalysisWithParameters.getAnalysis().getName(), dataAnalysisWithParameters.getOutputColumnToFormat());

				ArrayList<HashMap<String,String>> allDependencies = new ArrayList<HashMap<String,String>>();

				for (DependencyManager manager : singleSampleCalculator.getDependencyManagers()) {
					if (manager == null) {
						allDependencies.add(null);
						continue;
					}

					HashMap<String,String> dependencies = new HashMap<String,String>();

					for (DependencyPlugin dependencyPlugin : manager.getDependencyPlugins()) {
						if (dependencyPlugin == null) {
							continue;
						}

						HashMap<String,String> printableValues = dependencyPlugin.getPrintableValues(manager);

						if (printableValues != null) {
							dependencies.putAll(printableValues);
						}
					}

					allDependencies.add(dependencies);
				}

				calcSampleCache.setDependencies(allDependencies);

			} else {
				Pad pad = calcSampleCache.getScratchPad().getChild(0);
				Object analysis = pad.getValue(Pad.ANALYSIS);
				pad.clearData();
				pad.setValue(Pad.ANALYSIS, analysis);
				pad.setValue(Pad.ANALYSIS_STATUS, Status.ERROR);

				if (singleSampleCalculator.getErrors().size() != 0) {
					CalculationError error = singleSampleCalculator.getErrors().get(0);
					calcSampleCache.setErrorSamStep(error.getStepController() == null ? null : error.getStepController().getStepName());
					calcSampleCache.setErrorMessage(error.getErrorMessage());

				} else {
					calcSampleCache.setErrorMessage(Messages.loadOrCalculateSample_unknownSampleError);
				}
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_replicateCalculateFailed, e);
		}

		//TODO probably not necessary
		//calcSampleCache.getScratchPad().getChild(0).setValue(Pad.ANALYSIS, samAnalysis.getName());

		// format corresponding corrInteralId and RepAnalysisId pairs

		calcSampleCache.setCorrIntervalIds(new int[corrIntervalIds.size()]);
		calcSampleCache.setRepAnalysisIds(new int[corrIntervalIds.size()]);

		for (int i=0; i<corrIntervalIds.size(); i++) {
			calcSampleCache.getCorrIntervalIds()[i] = corrIntervalIds.get(i);
			calcSampleCache.getRepAnalysisIds()[i] = repAnalysisIds.get(i);
		}

		// format replicate ids

		int[] replicateIdArray = new int[replicateIds.size()];
		int count = 0;

		for (ReplicatePad replicatePad : calcSampleCache.getScratchPad().getChild(0).getChildren()) {
			replicateIdArray[count++] = replicateIds.get(replicatePad.getDate());
		}

		calcSampleCache.setReplicateIds(replicateIdArray);

		// format the potential RepAnalysisIds for each replicate

		int[][] potentialRepAnalysisIdsArray = new int[potentialRepAnalysisIds.size()][];
		count = 0;

		for (ReplicatePad replicatePad : calcSampleCache.getScratchPad().getChild(0).getChildren()) {
			ArrayList<Integer> listForReplicate = potentialRepAnalysisIds.get(replicatePad.getDate());
			potentialRepAnalysisIdsArray[count] = new int[listForReplicate.size()];

			for (int j=0; j<listForReplicate.size(); j++) {
				potentialRepAnalysisIdsArray[count][j] = listForReplicate.get(j);
			}
			
			count++;
		}

		calcSampleCache.setPotentialRepAnalyses(potentialRepAnalysisIdsArray);

		return calcSampleCache;
	}

	private CalcReplicateCache getCalcReplicateCache(ReplicateV1 replicate, int replicateAnalysisId, CorrIntervalV1 corrInterval) {
		try {
			Dao<CalcReplicateCache,Integer> calcReplicateDao = DaoManager.createDao(connectionSource, CalcReplicateCache.class);

			HashMap<String,Object> queryMap = new HashMap<String,Object>();
			queryMap.put(CalcReplicateCache.REPLICATE_ID_FIELD_NAME, replicate.getId());
			queryMap.put(CalcReplicateCache.REPLICATE_ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);
			List<CalcReplicateCache> list = calcReplicateDao.queryForFieldValues(queryMap);

			if (list != null && list.size() == 1) {
				return list.get(0);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_problemReadingCalcReplicateCache, e);
		}

		CalcReplicateCache calcReplicateCache = createCalcReplicateCache(replicate, replicateAnalysisId, corrInterval);

		try {
			Dao<CalcReplicateCache,Integer> calcReplicateCacheDao = DaoManager.createDao(connectionSource, CalcReplicateCache.class);
			calcReplicateCacheDao.create(calcReplicateCache);

		} catch (Exception e) {
				Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_replicateSaveFailed, e);
		}

		return calcReplicateCache;
	}

	private CalcReplicateCache createCalcReplicateCache(ReplicateV1 replicate, int replicateAnalysisId, CorrIntervalV1 corrInterval) {		
		CalcReplicateCache calcReplicateCache = new CalcReplicateCache();

		calcReplicateCache.setReplicateId(replicate.getId());
		calcReplicateCache.setReplicateAnalysisId(replicateAnalysisId);
		calcReplicateCache.setScratchPad(new ScratchPad<ReplicatePad>());
		calcReplicateCache.setCorrIntervalId(corrInterval.getId());
		calcReplicateCache.setColumnOrdering(new ColumnOrdering());
		calcReplicateCache.setFormatLookup(new FormatLookup());

		ArrayList<String> list = new ArrayList<String>();
		list.add(Pad.ID);
		list.add(Pad.ANALYSIS);
		list.add(Pad.ANALYSIS_STATUS);
		calcReplicateCache.getColumnOrdering().add(list);

		ScratchPad<ReplicatePad> scratchPad = null;
		ReplicatePad replicatePad = null;
		String repAnalysisName = Messages.loadOrCalculateSample_unknown;

		try {
			RepAnalysis repAnalysis = GetFromDb.getRepAnalysis(connectionSource, replicateAnalysisId);
			repAnalysisName = repAnalysis.getName();
			List<RepStep> repSteps = GetFromDb.getRepSteps(connectionSource, replicateAnalysisId);
			List<RepStepParams> repStepParameters = GetFromDb.getRepStepParameters(connectionSource, corrInterval.getId(), replicateAnalysisId);
			AnalysisWithParameters dataAnalysisWithParameters = new AnalysisWithParameters(repAnalysis, repSteps, repStepParameters);

			LoadOrCalculateCorrInterval loadOrCalculateCorrInterval = new LoadOrCalculateCorrInterval(corrInterval.getId(), replicateAnalysisId, connectionSource);
			scratchPad = loadOrCalculateCorrInterval.getCorrIntervalScratchPad().getScratchPad();
			List<CorrIntervalError> corrIntervalErrors = loadOrCalculateCorrInterval.getCorrIntervalError();

			ArrayList<Acquisition> acquisitions = GetFromDb.getAcquisitions(connectionSource, replicate.getId());
			int index = RawDataHelper.addReplicateToScratchPad(scratchPad, replicate, acquisitions);
			replicatePad = scratchPad.getChild(index);
			replicatePad.setVolatileData(AnalysisConstants.VOLATILE_DATA_REPLICATE, new ReplicateV1(replicate));

			SingleReplicateCalculator singleRunCalculator = new SingleReplicateCalculator(dataAnalysisWithParameters, scratchPad, replicatePad, corrIntervalErrors);
			singleRunCalculator.execute();

			if (singleRunCalculator.isFinished() && singleRunCalculator.getErrors().size() == 0) {
				calcReplicateCache.getColumnOrdering().add(dataAnalysisWithParameters.getGeneratedOutputColumns());
				calcReplicateCache.getFormatLookup().add(AnalysisIdentifier.Level.REPLICATE, repAnalysisName, dataAnalysisWithParameters.getOutputColumnToFormat());

				ArrayList<HashMap<String,String>> allDependencies = new ArrayList<HashMap<String,String>>();

				for (DependencyManager manager : singleRunCalculator.getDependencyManagers()) {
					if (manager == null) {
						allDependencies.add(null);
						continue;
					}

					HashMap<String,String> dependencies = new HashMap<String,String>();

					for (DependencyPlugin dependencyPlugin : manager.getDependencyPlugins()) {
						if (dependencyPlugin == null) {
							continue;
						}

						HashMap<String,String> printableValues = dependencyPlugin.getPrintableValues(manager);

						if (printableValues != null) {
							dependencies.putAll(printableValues);
						}
					}

					allDependencies.add(dependencies);
				}

				calcReplicateCache.setDependencies(allDependencies);

				replicatePad.trimChildrenToLevel(OptionsInfo.getInstance().getOptions().getOverviewResolution().getPadClass());
				replicatePad.reassignToParent(calcReplicateCache.getScratchPad());

			} else if (singleRunCalculator.getErrors().size() != 0) {
				CalculationError error = singleRunCalculator.getErrors().get(0);
				calcReplicateCache.setErrorRepStep(error.getStepController().getStepName());
				calcReplicateCache.setErrorMessage(error.getErrorMessage());

			} else {
				calcReplicateCache.setErrorMessage(Messages.loadOrCalculateSample_unknownReplicateError);
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_replicateCalculateFailed, e);
		}

		// make sure the scratch pad is cleaned up
		if (scratchPad != null && replicatePad != null) {
			scratchPad.removeChild(replicatePad);
		}

		// this means something went wrong somewhere
		if (calcReplicateCache.getScratchPad().getChildren().size() == 0) {
			ReplicatePad pad = new ReplicatePad(calcReplicateCache.getScratchPad(), replicate.getDate(), replicate.getId(), ReplicateType.SAMPLE_RUN);
			pad.setValue(Pad.ANALYSIS, repAnalysisName);
			pad.setValue(Pad.ANALYSIS_STATUS, Status.ERROR);

			calcReplicateCache.setErrorMessage(Messages.loadOrCalculateSample_unknownReplicateError);
		}

		return calcReplicateCache;
	}

	public ArrayList<Integer> getCalcSampleCacheIdsFromCorrIntervalAndReplicateAnalysis(int corrIntervalId, int replicateAnalysisId) {
		ArrayList<Integer> results = new ArrayList<Integer>();

		try {
			Dao<CalcReplicateCache,Integer> calcReplicateCacheDao = DaoManager.createDao(connectionSource, CalcReplicateCache.class);
			Dao<CalcRepToCalcSamp,Integer> calcRepToCalcSampDao = DaoManager.createDao(connectionSource, CalcRepToCalcSamp.class);

			HashMap<String,Object> queryMap = new HashMap<String,Object>();
			queryMap.put(CalcReplicateCache.CORR_INTERVAL_ID_FIELD_NAME, corrIntervalId);
			queryMap.put(CalcReplicateCache.REPLICATE_ANALYSIS_ID_FIELD_NAME, replicateAnalysisId);

			for (CalcReplicateCache calcReplicateCache : calcReplicateCacheDao.queryForFieldValues(queryMap)) {
				for (CalcRepToCalcSamp calcRepToCalcSamp : calcRepToCalcSampDao.queryForEq(CalcRepToCalcSamp.CALC_REPLICATE_ID_FIELD_NAME, calcReplicateCache.getId())) {
					results.add(calcRepToCalcSamp.getCalcSampleId());
				}
			}

		} catch (SQLException e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_problemWithGetCalcSampleCacheIdsFromCorrIntervalAndReplicateAnalysis, e);
		}

		return results;
	}

	public ArrayList<Integer> getCalcSampleCacheIdsFromSampleIdAndSampleAnalysisId(int sampleId, int sampleAnalysisId) {
		ArrayList<Integer> results = new ArrayList<Integer>();

		try {
			Dao<CalcSampleCache,Integer> calcSampleDao = DaoManager.createDao(connectionSource, CalcSampleCache.class);

			HashMap<String,Object> queryMap = new HashMap<String,Object>();
			queryMap.put(CalcSampleCache.SAMPLEID_FIELD_NAME, sampleId);
			queryMap.put(CalcSampleCache.SAMPLE_ANALYSIS_ID_FIELD_NAME, sampleAnalysisId);

			for (CalcSampleCache calcSampleCache : calcSampleDao.queryForFieldValues(queryMap)) {
				results.add(calcSampleCache.getId());
			}

		} catch (SQLException e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_problemWithGetCalcSampleCacheIdsFromSampleId, e);
		}

		return results;
	}

	public ArrayList<Integer> getCalcSampleCacheIdsFromReplicateId(int replicateId) {
		ArrayList<Integer> results = new ArrayList<Integer>();

		try {
			Dao<CalcReplicateCache,Integer> calcReplicateCacheDao = DaoManager.createDao(connectionSource, CalcReplicateCache.class);
			Dao<CalcRepToCalcSamp,Integer> calcRepToCalcSampDao = DaoManager.createDao(connectionSource, CalcRepToCalcSamp.class);

			for (CalcReplicateCache calcReplicateCache : calcReplicateCacheDao.queryForEq(CalcReplicateCache.REPLICATE_ID_FIELD_NAME, replicateId)) {
				for (CalcRepToCalcSamp calcRepToCalcSamp : calcRepToCalcSampDao.queryForEq(CalcRepToCalcSamp.CALC_REPLICATE_ID_FIELD_NAME, calcReplicateCache.getId())) {
					results.add(calcRepToCalcSamp.getCalcSampleId());
				}
			}

		} catch (SQLException e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_problemWithGetCalcSampleCacheIdsFromReplicateId, e);
		}

		return results;
	}

	public ArrayList<Integer> getCalcSampleCacheIdsFromSampleId(int sampleId) {
		ArrayList<Integer> results = new ArrayList<Integer>();

		try {
			Dao<CalcSampleCache,Integer> calcSampleDao = DaoManager.createDao(connectionSource, CalcSampleCache.class);

			for (CalcSampleCache calcSampleCache : calcSampleDao.queryForEq(CalcSampleCache.SAMPLEID_FIELD_NAME, sampleId)) {
				results.add(calcSampleCache.getId());
			}

		} catch (SQLException e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_problemWithGetCalcSampleCacheIdsFromSampleId, e);
		}

		return results;
	}

	public CalcSampleCache removeSampleCalculations(int calcSampleCacheId) {
		CalcSampleCache calcSampleCache = null;

		try {
			Dao<CalcSampleCache,Integer> calcSampleDao = DaoManager.createDao(connectionSource, CalcSampleCache.class);
			calcSampleCache = calcSampleDao.queryForId(calcSampleCacheId);

			if (calcSampleCache == null) {
				return null;
			}

			Dao<CalcRepToCalcSamp,Integer> calcRepToCalcSampDao = DaoManager.createDao(connectionSource, CalcRepToCalcSamp.class);
			HashSet<Integer> calcReplicateCacheIds = new HashSet<Integer>();

			for (CalcRepToCalcSamp calcRepToCalcSamp : calcRepToCalcSampDao.queryForEq(CalcRepToCalcSamp.CALC_SAMPLE_ID_FIELD_NAME, calcSampleCacheId)) {
				calcReplicateCacheIds.add(calcRepToCalcSamp.getCalcReplicateId());
				calcRepToCalcSampDao.deleteById(calcRepToCalcSamp.getId());
			}

			Dao<CalcReplicateCache,Integer> calcReplicateCacheDao = DaoManager.createDao(connectionSource, CalcReplicateCache.class);

			for (int calcReplicateCacheId : calcReplicateCacheIds) {
				if (calcRepToCalcSampDao.queryForEq(CalcRepToCalcSamp.CALC_REPLICATE_ID_FIELD_NAME, calcReplicateCacheId).size() == 0) {
					calcReplicateCacheDao.deleteById(calcReplicateCacheId);
				}
			}

			calcSampleDao.delete(calcSampleCache);

		} catch (SQLException e) {
			Log.getInstance().log(Level.INFO, LoadOrCalculateSample.class, Messages.loadOrCalculateSample_problemDeletingCalcSampleCache, e);
		}

		return calcSampleCache;
	}
}
