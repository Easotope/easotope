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

package org.easotope.shared;

import org.eclipse.osgi.util.NLS;

public class Messages {
	private static final String BUNDLE_NAME = "org.easotope.shared.messages"; //$NON-NLS-1$

	private Messages() {
	}

	public static String massSpecUpdate_massSpecAlreadyExists;
	public static String massSpecUpdate_massSpecDoesNotExist;

	public static String refGasUpdate_refGasValidFromAlreadyExists;
	public static String refGasUpdate_refGasDoesNotExist;

	public static String sampleTypeUpdate_sampleTypeAlreadyExists;
	public static String sampleTypeUpdate_sampleTypeDoesNotExist;

	public static String acidTempUpdate_acidTemperatureAlreadyExists;
	public static String acidTempUpdate_acidTempDoesNotExist;

	public static String standardUpdate_standardAlreadyExists;
	public static String standardUpdate_standardDoesNotExist;

	public static String corrIntervalUpdate_corrIntervalValidFromAlreadyExists;
	public static String corrIntervalUpdate_corrIntervalDoesNotExist;

	public static String stepParametersUpdate_stepParameterIdMismatch;

	public static String forceClientRestartDueToDatabaseRecalc_message;

	public static String correctionIntervalUpdatedHandler_scratchPadSaveError;

	public static String repStepCO2_name;
	public static String repStepCO2_shortDocumentation;
	public static String repStepCO2_documentationPath;

	public static String repStepCO2Clump_name;
	public static String repStepCO2Clump_shortDocumentation;
	public static String repStepCO2Clump_documentationPath;

	public static String repStepCO2ClumpPbl_name;
	public static String repStepCO2ClumpPbl_shortDocumentation;
	public static String repStepCO2ClumpPbl_documentationPath;

	public static String repStepCO2ClumpEthPbl_name;
	public static String repStepCO2ClumpEthPbl_shortDocumentation;
	public static String repStepCO2ClumpEthPbl_documentationPath;

	public static String repStepCO2CDrift_name;
	public static String repStepCO2CDrift_shortDocumentation;
	public static String repStepCO2CDrift_documentationPath;

	public static String repStepCO2OAcid_name;
	public static String repStepCO2OAcid_shortDocumentation;
	public static String repStepCO2OAcid_documentationPath;

	public static String repStepCO2ODrift_name;
	public static String repStepCO2ODrift_shortDocumentation;
	public static String repStepCO2ODrift_documentationPath;

	public static String repStepCO2OPdbToSmow_name;
	public static String repStepCO2OPdbToSmow_shortDocumentation;
	public static String repStepCO2OPdbToSmow_documentationPath;

	public static String repStepCO2ONonlinearity_name;
	public static String repStepCO2ONonlinearity_shortDocumentation;
	public static String repStepCO2ONonlinearity_documentationPath;

	public static String repStepCO2OETF_name;
	public static String repStepCO2OETF_shortDocumentation;
	public static String repStepCO2OETF_documentationPath;

	public static String repStepCO2OETFPBL_name;
	public static String repStepCO2OETFPBL_shortDocumentation;
	public static String repStepCO2OETFPBL_documentationPath;

	public static String repStepCO2OD47Acid_name;
	public static String repStepCO2OD47Acid_shortDocumentation;
	public static String repStepCO2OD47Acid_documentationPath;

	public static String repStepCO2OAcidInfo_name;
	public static String repStepCO2OAcidInfo_shortDocumentation;
	public static String repStepCO2OAcidInfo_documentationPath;

	public static String repStepCO2D48Offset_name;
	public static String repStepCO2D48Offset_shortDocumentation;
	public static String repStepCO2D48Offset_documentationPath;

	public static String repStepCO2D48OffsetPbl_name;
	public static String repStepCO2D48OffsetPbl_shortDocumentation;
	public static String repStepCO2D48OffsetPbl_documentationPath;

	public static String repStepGenericReplicate_name;
	public static String repStepGenericReplicate_shortDocumentation;
	public static String repStepGenericReplicate_documentationPath;

	public static String dataAnalysisCompiled_couldntCreateStepController;

	public static String dataAnalysisWithParameters_couldntCreateStepCalculator;

	public static String recalculateCorrInterval_couldNotLoadCorrInterval;
	public static String recalculateCorrInterval_errorWhileRemovingScratchPad;
	public static String recalculateCorrInterval_errorWhileRemovingErrors;
	public static String recalculateCorrInterval_couldNotLoadDataAnalysisWithParameters;
	public static String recalculateCorrInterval_couldNotSaveErrorToDb;
	public static String recalculateCorrInterval_errorReadingStandards;
	public static String recalculateCorrInterval_errorReadingStandardList;
	public static String recalculateCorrInterval_errorReadingScanList;
	public static String recalculateCorrInterval_errorStandardsCalculatorDidNotFinish;
	public static String recalculateCorrInterval_couldNotFindCorrInterval;
	public static String recalculateCorrInterval_errorLoadingCorrIntervalError;
	public static String recalculateCorrInterval_errorLoadingCorrIntervalScratchPad;

	public static String singleReplicateCalculator_missingInput;

	public static String singleSampleCalculator_missingInput;
	public static String singleSampleCalculator_badReplicate;

	public static String co2PblCalculator_insufficientOffPeakData;

	public static String co2EthPblCalculator_noScanFileFound;

	public static String loadOrCalculateSample_problemReadingCalcSampleCache;
	public static String loadOrCalculateSample_problemReadingReplicateList;
	public static String loadOrCalculateSample_problemReadingCorrIntervalList;
	public static String loadOrCalculateSample_problemReadingCalcReplicateCache;
	public static String loadOrCalculateSample_sampleSaveFailed;
	public static String loadOrCalculateSample_replicateCalculateFailed;
	public static String loadOrCalculateSample_replicateSaveFailed;
	public static String loadOrCalculateSample_unknownReplicateError;
	public static String loadOrCalculateSample_unknownSampleError;
	public static String loadOrCalculateSample_unknown;
	public static String loadOrCalculateSample_problemDeletingCalcSampleCache;
	public static String loadOrCalculateSample_problemWithGetCalcSampleCacheIdsFromCorrIntervalAndReplicateAnalysis;
	public static String loadOrCalculateSample_problemWithGetCalcSampleCacheIdsFromReplicateId;
	public static String loadOrCalculateSample_problemWithGetCalcSampleCacheIdsFromSampleId;

	public static String samStepGenericSample_name;
	public static String samStepGenericSample_shortDocumentation;
	public static String samStepGenericSample_documentationPath;

	public static String samStepCO2Average_name;
	public static String samStepCO2Average_shortDocumentation;
	public static String samStepCO2Average_documentationPath;

	public static String samStepClumpAverage_name;
	public static String samStepClumpAverage_shortDocumentation;
	public static String samStepClumpAverage_documentationPath;

	public static String samStepClumpedTemperature_name;
	public static String samStepClumpedTemperature_shortDocumentation;
	public static String samStepClumpedTemperature_documentationPath;

	public static String acidInfoPlugin_temperature;
	public static String acidInfoPlugin_acidFractionationFactor;

	public static String massSpecPlugin_massSpec;

	public static String sourceAndSampleTypePlugin_source;
	public static String sourceAndSampleTypePlugin_sampleType;

	public static String corrIntervalPlugin_corrInterval;

	public static String etfPblCalc_noKnownD47;

	public static String etfCalc_noKnownD47;

	public static String locale;
	
	public static String loginWithPermissions_noPermissions;
	public static String loginWithPermissions_noPreferences;

	public static String userPermsPrefsUpdate_permIdDoesntMatch;
	public static String userPermsPrefsUpdate_prefIdDoesntMatch;
	public static String userPermsPrefsUpdate_permsDoNotExist;
	public static String userPermsPrefsUpdate_prefsDoNotExist;
	public static String userPermsPrefsUpdate_permissionsExists;
	public static String userPermsPrefsUpdate_preferencesExists;

	public static String dataAnalysisUpdate_dataAnalysisDoesNotExist;

	public static String dependencyManager_couldNotLoad;

	public static String initializeWithPermsPrefs_permissionsTableExists;
	public static String initializeWithPermsPrefs_preferencesTableExists;

	public static String unexpectedException_couldNotInstantiateGraphicObject;
	public static String unexpectedException_couldNotCallGraphicObject;

	public static String scratchPad_propertyCannotBeNull;

	public static String autoParser_noFileExtension;
	public static String autoParser_unknownFileExtension;
	public static String autoParser_fileNotValid;

	public static String computeAcquisitionParsed_fileHasNoDateText;
	public static String computeAcquisitionParsed_fileHasNoCycles;
	public static String computeAcquisitionParsed_fileHasUnequalCycles;

	public static String computeScanFileParsed_fileHasNoDateText;
	public static String computeScanFileParsed_fileHasNoFromToVoltages;

	public static String projectUpdate_doesNotExist;
	
	public static String sampleUpdate_doesNotExist;
	
	public static String replicateUpdate_duplicateReplicate;
	public static String replicateUpdate_doesNotExist;

	public static String scanUpdate_duplicateScan;
	public static String scanUpdate_doesNotExist;

	public static String disabledStatusUpdate_noSuchReplicate;
	public static String disabledStatusUpdate_noSampleForReplicate;
	public static String disabledStatusUpdate_replicateStatusUnchanged;
	public static String disabledStatusUpdate_noEnabledAcquisitions;
	public static String disabledStatusUpdate_disabled;
	public static String disabledStatusUpdate_enabled;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
