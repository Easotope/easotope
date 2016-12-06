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

package org.easotope.client;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.easotope.client.messages"; //$NON-NLS-1$

	private Messages() {
	}

	public static String documentation_server;
	public static String documentation_property;
	public static String documentation_license;

	public static String reference;

	public static String wizard_config_choose_title;
	public static String wizard_config_choose_firstTimeTitle;
	public static String wizard_config_choose_explain;
	public static String wizard_config_choose_question;
	public static String wizard_config_choose_server;
	public static String wizard_config_choose_filesystem;

	public static String wizard_config_serverInput_title;
	public static String wizard_config_serverInput_explain;
	public static String wizard_config_serverInput_hostname;
	public static String wizard_config_serverInput_hostExample;
	public static String wizard_config_serverInput_port;
	public static String wizard_config_serverInput_portExample;

	public static String wizard_config_folderInput_title;
	public static String wizard_config_folderInput_explain;
	public static String wizard_config_folderInput_path;
	public static String wizard_config_folderInput_searchButton;
	public static String wizard_config_folderInput_selectFolder;
	public static String wizard_config_folderInput_pathOtherExample;
	public static String wizard_config_folderInput_pathWindowsExample;

	public static String wizard_config_folderTest_title;
	public static String wizard_config_folderTest_wait;
	public static String wizard_config_folderTest_error;
	public static String wizard_config_folderTest_done;

	public static String wizard_config_genericTest_title;
	public static String wizard_config_genericTest_wait;
	public static String wizard_config_genericTest_error;
	public static String wizard_config_genericTest_done;
	public static String wizard_config_genericTest_first;
	public static String wizard_config_genericTest_firstPassword;
	public static String wizard_config_genericTest_firstReenterPassword;
	public static String wizard_config_genericTest_firstPasswordsNotEqual;

	public static String wizard_config_initialize_title;
	public static String wizard_config_initialize_wait;
	public static String wizard_config_initialize_error;
	public static String wizard_config_initialize_alreadyInitialized;
	public static String wizard_config_initialize_done;

	public static String dialog_about_title;
	public static String dialog_about_release;
	public static String dialog_about_concept;
	public static String dialog_about_coded;
	public static String dialog_about_thanksTo;
	public static String dialog_about_copyright;
	public static String dialog_about_license;
	
	public static String dialog_login_title;
	public static String dialog_login_waiting;
	public static String dialog_login_user;
	public static String dialog_login_password;
	public static String dialog_login_ok;
	public static String dialog_login_newDatabase;
	public static String dialog_login_paperLabel;
	public static String dialog_login_paperLink;
	public static String dialog_login_doiLink;
	public static String dialog_login_databaseNotInitializedTitle;
	public static String dialog_login_databaseNotInitializedMessage;

	public static String dialog_login_failedTitle;
	public static String dialog_login_failedMessage;
	public static String dialog_login_databaseError;
	public static String dialog_login_databaseErrorTitle;
	
	public static String part_dbMonitor_databaseLabel;
	public static String part_dbMonitor_statusLabel;
	public static String part_dbMonitor_statusConnected;
	public static String part_dbMonitor_statusStopping;
	public static String part_dbMonitor_statusPausing;
	public static String part_dbMonitor_statusPaused;
	public static String part_dbMonitor_statusNotConnected;
	public static String part_dbMonitor_validCommandsLabel;
	public static String part_dbMonitor_executionErrorsLabel;
	public static String part_dbMonitor_permissionErrorsLabel;
	public static String part_dbMonitor_localEventsLabel;
	public static String part_dbMonitor_remoteEventsLabel;
	public static String part_dbMonitor_clearLogButton;

	public static String lifecycle_terminalErrorTitle;
	public static String lifecycle_terminalError;
	public static String lifecycle_lostConnectionTitle;
	public static String lifecycle_lostConnection;
	public static String lifecycle_databaseErrorTitle;
	public static String lifecycle_databaseError;
	public static String lifecycle_versionMismatchTitle;
	public static String lifecycle_versionMismatch;
	public static String lifecycle_downloadNewVersion;
	public static String lifecycle_bestClientForServerRedirect;
	public static String lifecycle_saveDirtyTabTitle;
	public static String lifecycle_saveDirtyTab;
	public static String lifecycle_saveDirtyTabWithErrorsTitle;
	public static String lifecycle_saveDirtyTabWithErrors;

	public static String dialog_blockWhileSaving_title;
	public static String dialog_blockWhileSaving_explainWaiting;
	public static String dialog_blockWhileSaving_dontWait;
	public static String dialog_blockWhileSaving_saveErrors;
	public static String dialog_blockWhileSaving_OK;

	public static String quitHandler_title;
	public static String quitHandler_error;

	public static String downloadVersionInfo_bestClientForServerUrl;
	public static String downloadVersionInfo_bestServerUrl;
	public static String downloadVersionInfo_newVersion;
	public static String downloadVersionInfo_newVersionTitle;
	public static String downloadVersionInfo_newServer;
	public static String downloadVersionInfo_downloadNewVersion;
	public static String downloadVersionInfo_versionUrl;

	public static String subscribeToUserList_explainTitle;
	public static String subscribeToUserList_explainMessage;
	public static String subscribeToUserList_noEmailTitle;
	public static String subscribeToUserList_noEmailMessage;
	public static String subscribeToUserList_address;
	public static String subscribeToUserList_subject;
	public static String subscribeToUserList_body;
	
	public static String reportBug_explainTitle;
	public static String reportBug_explainMessage;
	public static String reportBug_noEmailTitle;
	public static String reportBug_noEmailMessage;
	public static String reportBug_address;
	public static String reportBug_subject;
	public static String reportBug_body;

	public static String showVideos_url;
	public static String showVideos_noBrowserTitle;
	public static String showVideos_noBrowserMessage;

	public static String showHomePage_url;
	public static String showHomePage_noBrowserTitle;
	public static String showHomePage_noBrowserMessage;

	public static String showReference_title;
	public static String showReference_message;
	
	public static String showBugzilla_url;
	public static String showBugzilla_noBrowserTitle;
	public static String showBugzilla_noBrowserMessage;

	public static String modalMessage_genericInfoTitle;
	public static String modalMessage_genericErrorTitle;
	public static String modalMessage_inactive;

	public static String selectComposite_addUserButton;

	public static String numericList_allLeastOne;
	public static String numericList_selectionDoesNotExist;
	public static String numericList_valueIsRequired;
	public static String numericList_descriptionIsRequired;

	public static String scratchPadTable_tableCommandsLabel;
	public static String scratchPadTable_separator;
	public static String scratchPadTable_emptyCommand;
	public static String scratchPadTable_showAllColumns;
	public static String scratchPadTable_showColumnSelector;
	public static String scratchPadTable_saveTableLayout;
	public static String scratchPadTable_enableFormatting;
	public static String scratchPadTable_disableFormatting;
	public static String scratchPadTable_export;
	public static String scratchPadTable_hideProjects;
	public static String scratchPadTable_showProjects;
	public static String scratchPadTable_hideSamples;
	public static String scratchPadTable_showSamples;
	public static String scratchPadTable_hideReplicates;
	public static String scratchPadTable_showReplicates;
	public static String scratchPadTable_hideAcquisitions;
	public static String scratchPadTable_showAcquisitions;
	public static String scratchPadTable_hideCycles;
	public static String scratchPadTable_showCycles;

	public static String graph_autoscaleMenuItem;
	public static String graph_toggleBackgroundMenuItem;
	public static String graph_horizontalAxisHideLabel;
	public static String graph_horizontalAxisShowLabel;
	public static String graph_horizontalAxisHideScale;
	public static String graph_horizontalAxisShowScale;
	public static String graph_verticalAxisHideLabel;
	public static String graph_verticalAxisShowLabel;
	public static String graph_verticalAxisHideScale;
	public static String graph_verticalAxisShowScale;

	public static String adaptor_eventLoopError;

	public static String potentialGraphicsMethods_email;
	public static String potentialGraphicsMethods_subject;
	public static String potentialGraphicsMethods_instructions;
	public static String potentialGraphicsMethods_version;
	public static String potentialGraphicsMethods_noThrowable;
	public static String potentialGraphicsMethods_title;
	public static String potentialGraphicsMethods_askAboutEmail;
	public static String potentialGraphicsMethods_emailFailed;
	public static String potentialGraphicsMethods_suggestSendingLog;

	public static String locale;

	public static String loginInfo_id;
	public static String loginInfo_username;
	public static String loginInfo_fullName;
	public static String loginInfo_oldPassword;
	public static String loginInfo_oldPasswordEmpty;
	public static String loginInfo_oldPasswordInvalid;
	public static String loginInfo_newPassword1;
	public static String loginInfo_newPassword2;
	public static String loginInfo_newPasswordEmpty;
	public static String loginInfo_newPasswordsDontMatch;
	public static String loginInfo_isAdmin;
	public static String loginInfo_canEditMassSpecs;
	public static String loginInfo_canEditSampleTypes;
	public static String loginInfo_canEditStandards;
	public static String loginInfo_canEditConstants;
	public static String loginInfo_canEditAllInput;
	public static String loginInfo_canDeleteAll;
	public static String loginInfo_canDeleteOwn;
	public static String loginInfo_canEditCorrections;
	public static String loginInfo_timeZone;
	public static String loginInfo_defaultTimeZone;
	public static String loginInfo_checkForUpdates;
	public static String loginInfo_showTimeZone;
	public static String loginInfo_leadingExponent;
	public static String loginInfo_forceExponent;
	public static String loginInfo_hasBeenUpdated;

	public static String userAdminPart_userSelectCompositeLabel;
	public static String userAdminPart_userHasBeenUpdated;

	public static String userComposite_userIdLabel;
	public static String userComposite_newUserId;
	public static String userComposite_usernameLabel;
	public static String userComposite_usernameEmpty;
	public static String userComposite_usernameNotUnique;
	public static String userComposite_fullNameLabel;
	public static String userComposite_passwordLabel;
	public static String userComposite_passwordEmpty;
	public static String userComposite_isDisabledLabel;
	public static String userComposite_isAdminLabel;
	public static String userComposite_canEditMassSpecsLabel;
	public static String userComposite_canEditSampleTypesLabel;
	public static String userComposite_canEditStandardsLabel;
	public static String userComposite_canEditConstantsLabel;
	public static String userComposite_canEditAllInputLabel;
	public static String userComposite_canDeleteAllLabel;
	public static String userComposite_canDeleteOwnLabel;
	public static String userComposite_canEditCorrIntervalsLabel;

	public static String massSpec_massSpecSelectCompositeLabel;
	public static String massSpec_refGasSelectCompositeLabel;
	public static String massSpec_corrIntervalSelectCompositeLabel;
	public static String massSpec_massSpecHasBeenUpdated;
	public static String massSpec_refGasHasBeenUpdated;
	public static String massSpec_corrIntervalHasBeenUpdated;
	
	public static String massSpecComposite_massSpecIdLabel;
	public static String massSpecComposite_massSpecNameLabel;
	public static String massSpecComposite_descriptionLabel;
	public static String massSpecComposite_newMassSpecId;
	public static String massSpecComposite_massSpecNameEmpty;
	public static String massSpecComposite_massSpecNameNotUnique;

	public static String refGasComposite_idLabel;
	public static String refGasComposite_newRefGasId;
	public static String refGasComposite_validFromLabel;
	public static String refGasComposite_validFromDateNotUnique;
	public static String refGasComposite_validToLabel;
	public static String refGasComposite_newRefGasValidTo;
	public static String refGasComposite_descriptionLabel;
	public static String refGasComposite_valuesLabel;
	public static String refGasComposite_valueName;
	public static String refGasComposite_descriptionName;

	public static String massSpecCorrIntervalComposite_idLabel;
	public static String massSpecCorrIntervalComposite_newRefGasId;
	public static String massSpecCorrIntervalComposite_validFromLabel;
	public static String massSPecCorrIntervalComposite_validFromDateNotUnique;
	public static String massSpecCorrIntervalComposite_validToLabel;
	public static String massSpecCorrIntervalComposite_newCorrIntervalValidTo;
	public static String massSpecCorrIntervalComposite_batchDelimiterLabel;
	public static String massSpecCorrIntervalComposite_channelToMzX10Label;
	public static String massSpecCorrIntervalComposite_channelToMzX10Tip;
	public static String massSpecCorrIntervalComposite_descriptionLabel;
	public static String massSpecCorrIntervalComposite_dataAnalysisLabel;
	public static String massSpecCorrIntervalComposite_copyDataAnalyses;
	public static String massSpecCorrIntervalComposite_defaultDataAnalyses;
	public static String massSpecCorrIntervalComposite_dataAnalysisTip;

    public static String sampleType_sampleTypeSelectCompositeLabel;
    public static String sampleType_acidTempSelectCompositeLabel;
	public static String sampleType_sampleTypeHasBeenUpdated;

	public static String sampleTypeComposite_sampleTypeIdLabel;
	public static String sampleTypeComposite_sampleTypeNameLabel;
	public static String sampleTypeComposite_descriptionLabel;
	public static String sampleTypeComposite_hasAcidTempsLabel;
	public static String sampleTypeComposite_defaultAcidTempLabel;
	public static String sampleTypeComposite_newSampleTypeId;
	public static String sampleTypeComposite_sampleTypeNameEmpty;
	public static String sampleTypeComposite_sampleTypeNameNotUnique;

	public static String acidTempComposite_idLabel;
	public static String acidTempComposite_temperatureLabel;
	public static String acidTempComposite_descriptionLabel;
	public static String acidTempComposite_valuesLabel;
	public static String acidTempComposite_valueName;
	public static String acidTempComposite_temperatureRequired;
	public static String acidTempComposite_temperatureMustBeValidNumber;
	public static String acidTempComposite_temperatureNotUnique;
	public static String acidTempComposite_newAcidTempId;
	public static String acidTempComposite_acidTempHasBeenUpdated;
	
	public static String standardAdminPart_standardSelectCompositeLabel;
	public static String standardAdminPart_standardHasBeenUpdated;

	public static String standardComposite_standardIdLabel;
	public static String standardComposite_standardNameLabel;
	public static String standardComposite_graphicDesign;
	public static String standardComposite_descriptionLabel;
	public static String standardComposite_sampleTypeLabel;
	public static String standardComposite_newStandardId;
	public static String standardComposite_standardNameEmpty;
	public static String standardComposite_standardNameNotUnique;
	public static String standardComposite_sampleTypeEmpty;
	public static String standardComposite_valuesLabel;
	public static String standardComposite_valueName;
	public static String standardComposite_descriptionName;

	public static String constantsPart_explanation;
	public static String constantsPart_name;
	public static String constantsPart_description;
	public static String constantsPart_value;
	public static String constantsPart_reference;

	public static String sciConstant_sciConstantSelectCompositeLabel;
	public static String sciConstant_sciConstantHasBeenUpdated;

	public static String sciConstantComposite_sciConstantIdLabel;
	public static String sciConstantComposite_sciConstantNameLabel;
	public static String sciConstantComposite_sciConstantDescriptionLabel;
	public static String sciConstantComposite_sciConstantDefaultValueLabel;
	public static String sciConstantComposite_sciConstantDefaultReferenceLabel;
	public static String sciConstantComposite_valueLabel;
	public static String sciConstantComposite_referenceLabel;
	public static String sciConstantComposite_resetButton;
	public static String sciConstantComposite_valueEmpty;
	public static String sciConstantComposite_valueMalformed;

	public static String rawResultsWidget_mean;
	public static String rawResultsWidget_standardDeviation;
	public static String rawResultsWidget_standardError;
	
	public static String sampleNavigator_addProject;
	public static String sampleNavigator_editProject;
	public static String sampleNavigator_addSample;
	public static String sampleNavigator_editSample;
	public static String sampleNavigator_addReplicate;
	public static String sampleNavigator_editReplicate;

	public static String standardNavigator_addReplicateButton;

	public static String scanNavigator_addScanButton;
	
	public static String editor_sampleTab;
	public static String editor_replicateTab;
	public static String editor_projectTab;
	public static String editor_scanTab;
	
    public static String acquisitionsWidget_message1;
    public static String acquisitionsWidget_message2;
    public static String acquisitionsWidget_message3;
    public static String acquisitionsWidget_disabledAcquisition;
    public static String acquisitionsWidget_download;
    public static String acquisitionsWidget_disableColumn;
    public static String acquisitionsWidget_offPeakColumn;
    public static String acquisitionsWidget_cycle;
    public static String acquisitionsWidget_fileAddErrorTitle;
	public static String acquisitionsWidget_assumedTimeZoneTitle;
	public static String acquisitionsWidget_assumedTimeZone;
	public static String acquisitionsWidget_fileWrongType;
	public static String acquisitionsWidget_fileTooBig;
	public static String acquisitionsWidget_exceptionWhileReadingFile;
	public static String acquisitionsWidget_fileOffsetIncorrect;
    public static String acquisitionsWidget_dataMismatchReplicateText;

    public static String replicateComposite_idLabel;
    public static String replicateComposite_projectLabel;
    public static String replicateComposite_projectEmpty;
	public static String replicateComposite_sampleLabel;
	public static String replicateComposite_sampleEmpty;
	public static String replicateComposite_standardLabel;
	public static String replicateComposite_standardEmpty;
	public static String replicateComposite_massSpecLabel;
	public static String replicateComposite_massSpecEmpty;
	public static String replicateComposite_acidTempLabel;
	public static String replicateComposite_acidTempEmpty;
	public static String replicateComposite_replicateDisabled;
	public static String replicateComposite_descriptionLabel;
	public static String replicateComposite_newId;
    public static String replicateComposite_saveToFile;
    public static String replicateComposite_errorSavingFile;
    public static String replicateComposite_errorSavingFileTitle;
	public static String replicateComposite_userLabel;
	public static String replicateComposite_replicateHasBeenUpdated;
	public static String replicateComposite_reallyDelete;
	public static String replicateComposite_replicateDeleted;
	public static String replicateComposite_labelPrefix;

	public static String scanComposite_idLabel;
	public static String scanComposite_userLabel;
	public static String scanComposite_massSpecLabel;
	public static String scanComposite_massSpecEmpty;
	public static String scanComposite_scanDisabled;
	public static String scanComposite_retrieveLabel;
	public static String scanComposite_loadPrecedingButton;
	public static String scanComposite_descriptionLabel;
	public static String scanComposite_newId;
	public static String scanComposite_saveToFile;
	public static String scanComposite_errorSavingFile;
	public static String scanComposite_errorSavingFileTitle;
	public static String scanComposite_scanHasBeenUpdated;
	public static String scanComposite_scanDeleted;
	public static String scanComposite_labelPrefix;

	public static String projectEditor_projectHasBeenUpdated;

	public static String projectComposite_idLabel;
	public static String projectComposite_nameLabel;
	public static String projectComposite_descriptionLabel;
	public static String projectComposite_newId;
	public static String projectComposite_nameEmpty;
	public static String projectComposite_labelPrefix;

	public static String samplePart_sampleHasBeenUpdated;

	public static String sampleComposite_idLabel;
	public static String sampleComposite_nameLabel;
	public static String sampleComposite_descriptionLabel;
	public static String sampleComposite_sampleTypeLabel;
	public static String sampleComposite_sampleAnalysesLabel;
	public static String sampleComposite_newId;
	public static String sampleComposite_nameEmpty;
	public static String sampleComposite_sampleTypeEmpty;
	public static String sampleComposite_labelPrefix;

	public static String resultsCompositeTab_computingMessage;
	public static String resultsCompositeTab_errorTitle;
	public static String resultsCompositeTab_errorStepLabel;
	public static String resultsCompositeTab_emptyStepName;
	public static String resultsCompositeTab_errorMessageLabel;
	public static String resultsCompositeTab_standardsErrorMessage;

	public static String scanFilesWidget_message1;
	public static String scanFilesWidget_message2;
	public static String scanFilesWidget_message3;
	public static String scanFilesWidget_fileAddErrorTitle;
	public static String scanFilesWidget_fileWrongType;
	public static String scanFilesWidget_dataMismatchReplicateText;
	public static String scanFileWidget_byFileTab;

	public static String byMassWidget_referenceChannelLabel;
	public static String byMassWidget_degreeOfFitLabel;
	public static String byMassWidget_degreeOne;
	public static String byMassWidget_degreeTwo;
	public static String byMassWidget_showRegression;
	public static String byMassWidget_verticalLabel;
	public static String byMassWidget_horizontalLabel;
	public static String byMassWidget_warningToolTip;
	public static String byMassWidget_toggleLinesMenuItem;

	public static String byFileWidget_fileLabel;
	public static String byFileWidget_onPeakX1Label;
	public static String byFileWidget_onPeakX2Label;
	public static String byFileWidget_noRangeSelected;
	public static String byFileWidget_rangeNotComplete;
	public static String byFileWidget_invalidNumber;
	public static String byFileWidget_fromNotLessThanTo;
	public static String byFileWidget_verticalLabel;
	public static String byFileWidget_horizontalLabel;
	public static String byFileWidget_toggleLinesMenuItem;

	public static String pblRegression_verticalLabel;
	public static String pblRegression_horizontalLabel;
	public static String pblRegression_yLabel;
	public static String pblRegression_x2Label;
	public static String pblRegression_x1Label;
	public static String pblRegression_r2Label;

	public static String pointGraph_resetMenu;
	public static String pointGraph_savePngMenu;
	public static String pointGraph_saveToFile;

	public static String timeSelector_addCorrInterval;
	public static String timeSelector_magnify;
	public static String timeSelector_shrink;

	public static String correctionPart_massSpecLabel;

	public static String corrIntervalComposite_idLabel;
	public static String corrIntervalComposite_validFromLabel;
	public static String corrIntervalComposite_newCorrIntervalValidFrom;
	public static String corrIntervalComposite_validToLabel;
	public static String corrIntervalComposite_newCorrIntervalValidTo;
	public static String corrIntervalComposite_descriptionLabel;
	public static String corrIntervalComposite_dataAnalysisLabel;

	public static String hover_corrIntervalTitle;
	public static String hover_user;
	public static String hover_time;
	public static String hover_sample;
	public static String hover_standard;
	public static String hover_scanTitle;

	public static String repStepSelector_title;

	public static String samAnalysisSelector_title;

	public static String errorComposite_title;
	public static String errorComposite_column1;
	public static String errorComposite_column2;
	public static String errorComposite_column3;
	public static String errorComposite_column4;
	public static String errorComposite_emptyReplicate;
	public static String errorComposite_emptyUsername;
	public static String errorComposite_emptyRepStepName;

	public static String repStepComposite_offsetsLabel;
	public static String repStepComposite_documentationTabLabel;
	public static String repStepComposite_ioTabLabel;
	public static String repStepComposite_dependenciesTabLabel;
	public static String repStepComposite_parametersTabLabel;
	public static String repStepComposite_graphicsTabLabel;
	public static String repStepComposite_resultsTabLabel;
	public static String repStepComposite_errorScratchPad;
	public static String repStepComposite_errorDocumentation;
	public static String repStepComposite_errorIO;
	public static String repStepComposite_errorParameters;
	public static String repStepComposite_errorGraphicsComposite;
	public static String repStepComposite_errorGraphics;
	public static String repStepComposite_errorDependencies;
	public static String repStepComposite_errorResults;
	
	public static String samStepComposite_documentationTabLabel;
	public static String samStepComposite_ioTabLabel;
	public static String samStepComposite_parametersTabLabel;
	public static String samStepComposite_graphicsTabLabel;
	public static String samStepComposite_dependenciesTabLabel;
	public static String samStepComposite_resultsTabLabel;
	public static String samStepComposite_errorDocumentation;
	public static String samStepComposite_errorIO;
	public static String samStepComposite_errorParameters;
	public static String samStepComposite_errorGraphicsComposite;
	public static String samStepComposite_errorDependencies;
	public static String samStepComposite_errorResults;

	public static String tabOffsetsComposite_waitingOnInput;
	public static String tabOffsetsComposite_absolute;
	public static String tabOffsetsComposite_description;
	public static String tabOffsetsComposite_verticalRelativeLabel;
	public static String tabOffsetsComposite_verticalAbsoluteLabel;
	public static String tabOffsetsComposite_horizontalLabel;
	public static String tabOffsetsComposite_disabled;
	public static String tabOffsetsComposite_measured;
	public static String tabOffsetsComposite_expected;
	public static String tabOffsetsComposite_difference;

	public static String repTabIoComposite_inputsLabel;
	public static String repTabIoComposite_outputsLabel;
	public static String repTabIoComposite_description;
	public static String repTabIoComposite_column;

	public static String samTabIoComposite_inputsLabel;
	public static String samTabIoComposite_outputsLabel;
	public static String samTabIoComposite_description;
	public static String samTabIoComposite_column;

	public static String repAnalysisSelectComposite_label;

	public static String repAnalysisComposite_repAnalysisIdLabel;
	public static String repAnalysisComposite_repAnalysisNameLabel;
	public static String repAnalysisComposite_descriptionLabel;
	public static String repAnalysisComposite_repStepLabel;
	public static String repAnalysisComposite_requiredInputsLabel;
	public static String repAnalysisComposite_generatedOutputsLabel;

	public static String sampleSelectorComposite_title;

	public static String samAnalysisSelectComposite_label;

	public static String samStepSelector_title;

	public static String samAnalysisComposite_samAnalysisIdLabel;
	public static String samAnalysisComposite_samAnalysisNameLabel;
	public static String samAnalysisComposite_descriptionLabel;
	public static String samAnalysisComposite_samStepLabel;
	public static String samAnalysisComposite_requiredInputsLabel;
	public static String samAnalysisComposite_generatedOutputsLabel;
	public static String samAnalysisComposite_validRepAnalysesLabel;

	public static String emptyRepStepParamComposite_noParameters;

	public static String emptySamStepParamComposite_noParameters;

	public static String repTabDependenciesComposite_waitingOnInput;
	public static String repTabDependenciesComposite_waitingOnCalculations;
	public static String repTabDependenciesComposite_instructions;
	public static String repTabDependenciesComposite_column1;
	public static String repTabDependenciesComposite_column2;
	public static String repTabDependenciesComposite_noDependencies;
	public static String repTabDependenciesComposite_errorTitle;
	public static String repTabDependenciesComposite_errorRepStepLabel;
	public static String repTabDependenciesComposite_emptyRepStepName;
	public static String repTabDependenciesComposite_errorMessageLabel;

	public static String samTabDependenciesComposite_waitingOnInput;
	public static String samTabDependenciesComposite_instructions;
	public static String samTabDependenciesComposite_column1;
	public static String samTabDependenciesComposite_column2;
	public static String samTabDependenciesComposite_errorTitle;
	public static String samTabDependenciesComposite_errorSamStepLabel;
	public static String samTabDependenciesComposite_errorMessageLabel;
	public static String samTabDependenciesComposite_noDependencies;

	public static String repTabGraphicsComposite_waitingOnInput;
	public static String repTabGraphicsComposite_waitingOnCalculations;
	public static String repTabGraphicsComposite_errorTitle;
	public static String repTabGraphicsComposite_errorRepStepLabel;
	public static String repTabGraphicsComposite_emptyRepStepName;
	public static String repTabGraphicsComposite_errorMessageLabel;

	public static String samTabGraphicsComposite_waitingOnInput;
	public static String samTabGraphicsComposite_waitingOnCalculations;
	public static String samTabGraphicsComposite_errorTitle;
	public static String samTabGraphicsComposite_errorSamStepLabel;
	public static String samTabGraphicsComposite_emptySamStepName;
	public static String samTabGraphicsComposite_errorMessageLabel;

	public static String emptyRepStepGraphics_message;

	public static String emptySamStepGraphics_message;

	public static String repTabResultsComposite_waitingOnInput;
	public static String repTabResultsComposite_waitingOnCalculations;
	public static String repTabResultsComposite_instructions;
	public static String repTabResultsComposite_errorTitle;
	public static String repTabResultsComposite_errorRepStepLabel;
	public static String repTabResultsComposite_emptyRepStepName;
	public static String repTabResultsComposite_errorMessageLabel;

	public static String samTabResultsComposite_waitingOnInput;
	public static String samTabResultsComposite_instructions;
	public static String samTabResultsComposite_errorTitle;
	public static String samTabResultsComposite_errorRepStepLabel;
	public static String samTabResultsComposite_errorMessageLabel;
	public static String samTabResultsComposite_emptyRepStepName;

	public static String co2IclPblParameterComposite_allowUnaveragedRefValues;

	public static String co2IclPblGraphicComposite_instructions;
	public static String co2IclPblGraphicComposite_acquisition;
	public static String co2IclPblGraphicComposite_slope;
	public static String co2IclPblGraphicComposite_intercept;
	public static String co2IclPblGraphicComposite_r2;
	public static String co2IclPblGraphicComposite_yAxis;
	public static String co2IclPblGraphicComposite_xAxis;

	public static String co2EthMonitorComposite_scanFileDate;
	public static String co2EthMonitorComposite_scanFileTimeBetween;
	public static String co2EthMonitorComposite_mass;
	public static String co2EthMonitorComposite_X2Coeff;
	public static String co2EthMonitorComposite_X1Coeff;
	public static String co2EthMonitorComposite_X0Coeff;
	public static String co2EthMonitorComposite_nanWarning;
	public static String co2EthMonitorComposite_timeBetweenFormat;

	public static String driftParameterComposite_windowType;
	public static String driftParameterComposite_corrInterval;
	public static String driftParameterComposite_window;
	public static String driftParameterComposite_minNumStandardsBeforeAndAfter;
	public static String driftParameterComposite_applyStretching;
	public static String driftParameterComposite_standards;

	public static String corrIntervalComp_hasBeenUpdated;

	public static String repTabParametersComposite_instructions;

	public static String samTabParametersComposite_instructions;

	public static String co2SuperDriftGraphicComposite_disabled;
	public static String co2SuperDriftGraphicComposite_measured;
	public static String co2SuperDriftGraphicComposite_expected;
	public static String co2SuperDriftGraphicComposite_afterCorrection;
	public static String co2SuperDriftGraphicComposite_slope;
	public static String co2SuperDriftGraphicComposite_intercept;
	public static String co2SuperDriftGraphicComposite_offset;

	public static String co2ODriftGraphicComposite_horizontalLabel;
	public static String co2ODriftGraphicComposite_verticalLabel;

	public static String co2CDriftGraphicComposite_horizontalLabel;
	public static String co2CDriftGraphicComposite_verticalLabel;

	public static String nonlinearityParameterComposite_windowType;
	public static String nonlinearityParameterComposite_corrInterval;
	public static String nonlinearityParameterComposite_window;
	public static String nonlinearityParameterComposite_minNumStandardsBeforeAndAfter;
	public static String nonlinearityParameterComposite_standards;

	public static String nonlinearityGraphicComposite_slope;
	public static String nonlinearityGraphicComposite_group;
	public static String nonlinearityGraphicComposite_intercept;
	public static String nonlinearityGraphicComposite_correction;
	public static String nonlinearityGraphicComposite_disabled;
	public static String nonlinearityGraphicComposite_d47;
	public static String nonlinearityGraphicComposite_D47;
	public static String nonlinearityGraphicComposite_D47_measured;
	public static String nonlinearityGraphicComposite_D47_corrected;
	public static String nonlinearityGraphicComposite_verticalLabel;
	public static String nonlinearityGraphicComposite_horizontalLabel;

	public static String etfParameterComposite_acidTempLabel;
	public static String etfParameterComposite_temperatureMustBeValidNumber;
	public static String etfParameterComposite_temperatureRequired;
	public static String etfParameterComposite_windowType;
	public static String etfParameterComposite_corrInterval;
	public static String etfParameterComposite_window;
	public static String etfParameterComposite_newStandardsBeforeAfter;
	public static String etfParameterComposite_standards;
	public static String etfParameterComposite_averageStandardsFirst;

	public static String etfPblParameterComposite_acidTempLabel;
	public static String etfPblParameterComposite_temperatureMustBeValidNumber;
	public static String etfPblParameterComposite_temperatureRequired;
	public static String etfPblParameterComposite_windowType;
	public static String etfPblParameterComposite_corrInterval;
	public static String etfPblParameterComposite_window;
	public static String etfPblParameterComposite_newStandardsBeforeAfter;
	public static String etfPblParameterComposite_standards;
	public static String etfPblParameterComposite_averageStandardsFirst;

	public static String co2Etf_slope;
	public static String co2Etf_intercept;
	public static String co2Etf_r2;
	public static String co2Etf_offset;
	public static String co2Etf_disabled;
	public static String co2Etf_sample;
	public static String co2Etf_d47;
	public static String co2Etf_D47;
	public static String co2Etf_D47CDES;
	public static String co2Etf_graph1VerticalLabel;
	public static String co2Etf_graph1HorizontalLabel;
	public static String co2Etf_graph2VerticalLabel;
	public static String co2Etf_graph2HorizontalLabel;

	public static String co2EtfPbl_slope;
	public static String co2EtfPbl_intercept;
	public static String co2EtfPbl_r2;
	public static String co2EtfPbl_offset;
	public static String co2EtfPbl_disabled;
	public static String co2EtfPbl_sample;
	public static String co2EtfPbl_d47;
	public static String co2EtfPbl_D47;
	public static String co2EtfPbl_D47CDES;
	public static String co2EtfPbl_graph1VerticalLabel;
	public static String co2EtfPbl_graph1HorizontalLabel;
	public static String co2EtfPbl_graph2VerticalLabel;
	public static String co2EtfPbl_graph2HorizontalLabel;

	public static String co2D48OffsetParameterComposite_windowType;
	public static String co2D48OffsetParameterComposite_corrInterval;
	public static String co2D48OffsetParameterComposite_window;
	public static String co2D48OffsetParameterComposite_minNumStandardsBeforeAndAfter;
	public static String co2D48OffsetParameterComposite_standard;

	public static String co2D48OffsetGraphicComposite_slope;
	public static String co2D48OffsetGraphicComposite_group;
	public static String co2D48OffsetGraphicComposite_intercept;
	public static String co2D48OffsetGraphicComposite_correction;
	public static String co2D48OffsetGraphicComposite_offset;
	public static String co2D48OffsetGraphicComposite_disabled;
	public static String co2D48OffsetGraphicComposite_d48;
	public static String co2D48OffsetGraphicComposite_D48;
	public static String co2D48OffsetGraphicComposite_D48_measured;
	public static String co2D48OffsetGraphicComposite_D48_corrected;
	public static String co2D48OffsetGraphicComposite_verticalLabel;
	public static String co2D48OffsetGraphicComposite_horizontalLabel;

	public static String co2D48OffsetPblParameterComposite_windowType;
	public static String co2D48OffsetPblParameterComposite_corrInterval;
	public static String co2D48OffsetPblParameterComposite_window;
	public static String co2D48OffsetPblParameterComposite_minNumStandardsBeforeAndAfter;
	public static String co2D48OffsetPblParameterComposite_standards;

	public static String co2D48OffsetPblGraphicComposite_average;
	public static String co2D48OffsetPblGraphicComposite_stddev;
	public static String co2D48OffsetPblGraphicComposite_date;
	public static String co2D48OffsetPblGraphicComposite_name;
	public static String co2D48OffsetPblGraphicComposite_D48;
	public static String co2D48OffsetPblGraphicComposite_disabled;
	public static String co2D48OffsetPblGraphicComposite_disabledLabel;

	public static String loadButtonComposite_loadButton;

	public static String sampleAnalysisErrorComposite_errorTitle;
	public static String sampleAnalysisErrorComposite_errorStepLabel;
	public static String sampleAnalysisErrorComposite_errorMessageLabel;
	public static String sampleAnalysisErrorComposite_unknownStep;

	public static String exportTableComposite_instructions;

	public static String calculatedSampleTableComposite_calculatedSampleHasBeenUpdated;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
