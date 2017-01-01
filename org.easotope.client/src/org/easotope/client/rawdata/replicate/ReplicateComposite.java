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

package org.easotope.client.rawdata.replicate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.VButton;
import org.easotope.client.core.widgets.VText;
import org.easotope.client.rawdata.replicate.widget.acquisition.AcquisitionsWidget;
import org.easotope.client.rawdata.replicate.widget.acquisition.AcquisitionsWidget.ButtonType;
import org.easotope.client.rawdata.replicate.widget.acquisition.AcquisitionsWidgetListener;
import org.easotope.client.rawdata.replicate.widget.rawresults.ResultsComposite;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecCacheMassSpecListGetListener;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecList;
import org.easotope.shared.admin.cache.sampletype.SampleTypeCache;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.AcidTempList;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.SampleTypeCacheAcidTempListGetListener;
import org.easotope.shared.admin.cache.sampletype.sampletype.SampleTypeCacheSampleTypeGetListener;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standard.StandardCacheStandardGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardCacheStandardListGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardList;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.userlist.UserCacheUserListGetListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.admin.tables.SampleType;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalCacheCorrIntervalListGetListener;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListItem;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.projectlist.InputCacheProjectListGetListener;
import org.easotope.shared.rawdata.cache.input.projectlist.ProjectList;
import org.easotope.shared.rawdata.cache.input.rawfile.InputCacheRawFileGetListener;
import org.easotope.shared.rawdata.cache.input.replicate.InputCacheReplicateGetListener;
import org.easotope.shared.rawdata.cache.input.replicate.InputCacheReplicateSaveListener;
import org.easotope.shared.rawdata.cache.input.sample.InputCacheSampleGetListener;
import org.easotope.shared.rawdata.cache.input.samplelist.InputCacheSampleListGetListener;
import org.easotope.shared.rawdata.cache.input.samplelist.SampleList;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

public class ReplicateComposite extends EditorComposite
		implements UserCacheUserListGetListener, MassSpecCacheMassSpecListGetListener,
		StandardCacheStandardListGetListener, StandardCacheStandardGetListener,
		InputCacheProjectListGetListener, InputCacheSampleListGetListener,
		InputCacheSampleGetListener, SampleTypeCacheSampleTypeGetListener,
		SampleTypeCacheAcidTempListGetListener, InputCacheReplicateGetListener,
		InputCacheReplicateSaveListener, InputCacheRawFileGetListener,
		CorrIntervalCacheCorrIntervalListGetListener {

	private static HashSet<Integer> validMZX10s = new HashSet<Integer>();

	private final String WAITING_FOR_USER_LIST = "WAITING_FOR_USER_LIST";
	private final String WAITING_FOR_MASS_SPEC_LIST = "WAITING_FOR_MASS_SPEC_LIST";
	private final String WAITING_FOR_STANDARD_LIST = "WAITING_FOR_STANDARD_LIST";
	private final String WAITING_FOR_STANDARD = "WAITING_FOR_STANDARD";
	private final String WAITING_FOR_PROJECT_LIST = "WAITING_FOR_PROJECT_LIST";
	private final String WAITING_FOR_SAMPLE_LIST = "WAITING_FOR_SAMPLE_LIST";
	private final String WAITING_FOR_SAMPLE = "WAITING_FOR_SAMPLE";
	private final String WAITING_FOR_SAMPLE_TYPE = "WAITING_FOR_SAMPLE_TYPE";
	private final String WAITING_FOR_ACID_TEMP_LIST = "WAITING_FOR_ACID_TEMP_LIST";
	private final String WAITING_FOR_REPLICATE = "WAITING_FOR_REPLICATE";
	private final String WAITING_FOR_REPLICATE_SAVE = "WAITING_FOR_REPLICATE_SAVE";
	private final String WAITING_FOR_REPLICATE_DELETE = "WAITING_FOR_REPLICATE_DELETE";
	private final String WAITING_FOR_RAW_FILE = "WAITING_FOR_RAW_FILE";
	private final String WAITING_FOR_CORR_INTERVAL_LIST_MAIN = "WAITING_FOR_CORR_INTERVAL_LIST_MAIN";

	private boolean standardMode;

	private Integer initialUserId;
	private Integer initialProjectId;
	private Integer initialSampleId;
	private Integer initialMassSpecId;
	private Integer initialReplicateId;

	private Sample currentSample;
	private Standard currentStandard;
	private SampleType currentSampleType;
	private AcidTempList currentAcidTempList;

	private ReplicateV1 newReplicate = new ReplicateV1();
	private CorrIntervalList currentCorrIntervalList = null;
	private boolean resultsNeedUpdating = true;
	private Integer sourceId;

 	private Label id;
 	private SortedCombo user;
	private SortedCombo massSpec;
	private Canvas massSpecError;
 	private SortedCombo project;
 	private Canvas projectError;
 	private SortedCombo source;
 	private Canvas sourceError;
	private Composite acidTempComposite;
	private SortedCombo acidTemp;
	private Canvas acidTempError;
	private VButton disabled;
	private Canvas disabledError;
	private VText description;

	private AcquisitionsWidget acquisitionsWidget;
	private ResultsComposite resultsComposite;

	ReplicateComposite(ChainedPart chainedPart, Composite parent, int style, boolean isStandard) {
		super(chainedPart, parent, style);
		standardMode = isStandard;
		final Image errorImage = Icons.getError(parent.getDisplay());

		FormLayout formLayout = new FormLayout();
		setLayout(formLayout);

		Composite leftComposite = new Composite(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		leftComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		leftComposite.setLayout(gridLayout);

		Composite firstLineComposite = new Composite(leftComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		firstLineComposite.setLayoutData(gridData);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		firstLineComposite.setLayout(gridLayout);

		Composite idComposite = new Composite(firstLineComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		idComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		idComposite.setLayout(gridLayout);

		Label idLabel = new Label(idComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		idLabel.setLayoutData(gridData);
		idLabel.setText(Messages.replicateComposite_idLabel);

		id = new Label(idComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		id.setLayoutData(gridData);

		Composite userComposite = new Composite(firstLineComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		userComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		userComposite.setLayout(gridLayout);

		Label userLabel = new Label(userComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		userLabel.setLayoutData(gridData);
		userLabel.setText(Messages.replicateComposite_userLabel);

		user = new SortedCombo(userComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		user.setLayoutData(gridData);
		user.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				if (!standardMode) {
					acidTemp.setSelectionButLeaveRevertValue(DatabaseConstants.EMPTY_DB_ID);

					source.setSelectionButLeaveRevertValue(DatabaseConstants.EMPTY_DB_ID);
					sourceWasChanged();

					project.setSelectionButLeaveRevertValue(DatabaseConstants.EMPTY_DB_ID);
					projectWasChanged();

					userWasChanged();
					resultsNeedUpdating = true;
				}

				widgetStatusChanged();
			}
		});

		Composite massSpecComposite = new Composite(firstLineComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		massSpecComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		massSpecComposite.setLayout(gridLayout);

		final Label massSpecLabel = new Label(massSpecComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		massSpecLabel.setLayoutData(gridData);
		massSpecLabel.setText(Messages.replicateComposite_massSpecLabel);

		massSpec = new SortedCombo(massSpecComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		massSpec.setLayoutData(gridData);
		massSpec.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				resultsNeedUpdating = true;
				loadCorrIntervalList();
				widgetStatusChanged();
			}
		});

		massSpecError = new Canvas(massSpecComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		massSpecError.setLayoutData(gridData);
		massSpecError.setVisible(false);
		massSpecError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Composite disabledComposite = new Composite(firstLineComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		disabledComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		disabledComposite.setLayout(gridLayout);

		disabled = new VButton(disabledComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		disabled.setLayoutData(gridData);
		disabled.setText(Messages.replicateComposite_replicateDisabled);
		disabled.addListener(SWT.Selection, new LoggingAdaptor() {
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		disabledError = new Canvas(disabledComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		disabledError.setLayoutData(gridData);
		disabledError.setVisible(false);
		disabledError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		final Label descriptionLabel = new Label(firstLineComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		gridData.horizontalAlignment = SWT.RIGHT;
		gridData.grabExcessHorizontalSpace = true;
		descriptionLabel.setLayoutData(gridData);
		descriptionLabel.setText(Messages.replicateComposite_descriptionLabel);

		Composite secondLineComposite = new Composite(leftComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		secondLineComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = standardMode ? 3 : 4;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		secondLineComposite.setLayout(gridLayout);

		if (standardMode) {
			Composite sourceComposite = new Composite(secondLineComposite, SWT.NONE);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			sourceComposite.setLayoutData(gridData);
			gridLayout = new GridLayout();
			gridLayout.numColumns = 3;
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			gridLayout.horizontalSpacing = 0;
			sourceComposite.setLayout(gridLayout);

			Label sourceLabel = new Label(sourceComposite, SWT.NONE);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			sourceLabel.setLayoutData(gridData);
			sourceLabel.setText(Messages.replicateComposite_standardLabel);

			source = new SortedCombo(sourceComposite, SWT.READ_ONLY);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
			source.setLayoutData(gridData);
			source.addListener(SWT.Selection, new LoggingAdaptor() {
				@Override
				public void loggingHandleEvent(Event event) {
					acidTemp.setSelectionButLeaveRevertValue(DatabaseConstants.EMPTY_DB_ID);
					sourceWasChanged();
					resultsNeedUpdating = true;
					widgetStatusChanged();
				}
			});

			sourceError = new Canvas(sourceComposite, SWT.NONE);
			gridData = new GridData();
			gridData.widthHint = errorImage.getImageData().width;
			gridData.heightHint = errorImage.getImageData().height;
			sourceError.setLayoutData(gridData);
			sourceError.setVisible(false);
			sourceError.addPaintListener(new LoggingPaintAdaptor() {
				public void loggingPaintControl(PaintEvent e) {
					e.gc.setAntialias(SWT.ON);
					e.gc.drawImage(errorImage, 0, 0);
				}
			});

		} else {
			Composite projectComposite = new Composite(secondLineComposite, SWT.NONE);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			projectComposite.setLayoutData(gridData);
			gridLayout = new GridLayout();
			gridLayout.numColumns = 3;
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			gridLayout.horizontalSpacing = 0;
			projectComposite.setLayout(gridLayout);
	
			Label projectLabel = new Label(projectComposite, SWT.NONE);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			projectLabel.setLayoutData(gridData);
			projectLabel.setText(Messages.replicateComposite_projectLabel);
	
			project = new SortedCombo(projectComposite, SWT.READ_ONLY);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
			project.setLayoutData(gridData);
			project.addListener(SWT.Selection, new LoggingAdaptor() {
				@Override
				public void loggingHandleEvent(Event event) {
					acidTemp.setSelectionButLeaveRevertValue(DatabaseConstants.EMPTY_DB_ID);

					source.setSelectionButLeaveRevertValue(DatabaseConstants.EMPTY_DB_ID);
					sourceWasChanged();

					projectWasChanged();

					resultsNeedUpdating = true;
					widgetStatusChanged();
				}
			});

			projectError = new Canvas(projectComposite, SWT.NONE);
			gridData = new GridData();
			gridData.widthHint = errorImage.getImageData().width;
			gridData.heightHint = errorImage.getImageData().height;
			projectError.setLayoutData(gridData);
			projectError.setVisible(false);
			projectError.addPaintListener(new LoggingPaintAdaptor() {
				public void loggingPaintControl(PaintEvent e) {
					e.gc.setAntialias(SWT.ON);
					e.gc.drawImage(errorImage, 0, 0);
				}
			});

			Composite sourceComposite = new Composite(secondLineComposite, SWT.NONE);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
			sourceComposite.setLayoutData(gridData);
			gridLayout = new GridLayout();
			gridLayout.numColumns = 3;
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			gridLayout.horizontalSpacing = 0;
			sourceComposite.setLayout(gridLayout);
	
			Label sourceLabel = new Label(sourceComposite, SWT.NONE);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			sourceLabel.setLayoutData(gridData);
			sourceLabel.setText(Messages.replicateComposite_sampleLabel);

			source = new SortedCombo(sourceComposite, SWT.READ_ONLY);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
			source.setLayoutData(gridData);
			source.addListener(SWT.Selection, new LoggingAdaptor() {
				@Override
				public void loggingHandleEvent(Event event) {
					acidTemp.setSelectionButLeaveRevertValue(DatabaseConstants.EMPTY_DB_ID);
					sourceWasChanged();
					resultsNeedUpdating = true;
					widgetStatusChanged();
				}
			});

			sourceError = new Canvas(sourceComposite, SWT.NONE);
			gridData = new GridData();
			gridData.widthHint = errorImage.getImageData().width;
			gridData.heightHint = errorImage.getImageData().height;
			sourceError.setLayoutData(gridData);
			sourceError.setVisible(false);
			sourceError.addPaintListener(new LoggingPaintAdaptor() {
				public void loggingPaintControl(PaintEvent e) {
					e.gc.setAntialias(SWT.ON);
					e.gc.drawImage(errorImage, 0, 0);
				}
			});
		}

		acidTempComposite = new Composite(secondLineComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		acidTempComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		acidTempComposite.setLayout(gridLayout);
		acidTempComposite.setVisible(false);

		final Label acidTempLabel = new Label(acidTempComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		acidTempLabel.setLayoutData(gridData);
		acidTempLabel.setText(Messages.replicateComposite_acidTempLabel);

		acidTemp = new SortedCombo(acidTempComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.SHORT_COMBO_INPUT_WIDTH;
		acidTemp.setLayoutData(gridData);
		acidTemp.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				resultsNeedUpdating = true;
				widgetStatusChanged();
			}
		});

		acidTempError = new Canvas(acidTempComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		acidTempError.setLayoutData(gridData);
		acidTempError.setVisible(false);
		acidTempError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Composite rightComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(leftComposite);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(leftComposite, 0, SWT.BOTTOM);
		rightComposite.setLayoutData(formData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		rightComposite.setLayout(gridLayout);

		description = new VText(rightComposite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		description.setLayoutData(gridData);
		description.setTextLimit(GuiConstants.LONG_TEXT_INPUT_LIMIT);
		description.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		Composite tablesComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(leftComposite, 10);
		formData.bottom = new FormAttachment(100);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		tablesComposite.setLayoutData(formData);
 		tablesComposite.setLayout(new FormLayout());
 
 		acquisitionsWidget = new AcquisitionsWidget(chainedPart, tablesComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(50);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		acquisitionsWidget.setLayoutData(formData);
 		acquisitionsWidget.addAcquisitionChangedListener(new AcquisitionsWidgetListener() {
			@Override
			public void acquisitionsChanged(TreeSet<Acquisition> acquisitions) {
				long earliestDate = Long.MAX_VALUE;

				for (Acquisition acquisition : acquisitions) {
					long thisDate = acquisition.getAcquisitionParsed().getDate();

					if (thisDate < earliestDate) {
						earliestDate = thisDate;
					}
				}

				newReplicate.setDate(earliestDate);
				newReplicate.setChannelToMzX10(getChannelToMZX10());

				resultsNeedUpdating = true;
				widgetStatusChanged();
			}

			@Override
			public void acquisitionButtonsChanged(ButtonType buttonType, long acquisitionTimeStamp, int cycleNumber, boolean selected) {
				resultsComposite.modifyButton(buttonType, acquisitionTimeStamp, cycleNumber, selected);
				widgetStatusChanged();
			}

			@Override
			public void retrieveRawFile(int rawFileId) {
				ReplicateComposite.this.retrieveRawFile(rawFileId);
			}

			@Override
			public void retrieveRawFile(RawFile rawFile, byte[] fileBytes) {
				ReplicateComposite.this.retrieveRawFile(rawFile, fileBytes);
			}
 		});

		resultsComposite = new ResultsComposite(chainedPart, this, tablesComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(acquisitionsWidget);
		formData.bottom = new FormAttachment(100);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		resultsComposite.setLayoutData(formData);

		int commandId = UserCache.getInstance().userListGet(this);
		waitingFor(WAITING_FOR_USER_LIST, commandId);

		commandId = MassSpecCache.getInstance().massSpecListGet(this);
		waitingFor(WAITING_FOR_MASS_SPEC_LIST, commandId);

		if (standardMode) {
			commandId = StandardCache.getInstance().standardListGet(this);
			waitingFor(WAITING_FOR_STANDARD_LIST, commandId);
		}

		UserCache.getInstance().addListener(this);
		MassSpecCache.getInstance().addListener(this);
		CorrIntervalCache.getInstance().addListener(this);
		InputCache.getInstance().addListener(this);
		SampleTypeCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		UserCache.getInstance().removeListener(this);
		MassSpecCache.getInstance().removeListener(this);
		CorrIntervalCache.getInstance().removeListener(this);
		InputCache.getInstance().removeListener(this);
		SampleTypeCache.getInstance().removeListener(this);
	}

	private ReplicateV1 getCurrentReplicate() {
		Object[] currentObject = (Object[]) getCurrentObject();
		return currentObject != null ? (ReplicateV1) currentObject[0] : null;
	}

	private ArrayList<Acquisition> getCurrentAcquisitions() {
		Object[] currentObject = (Object[]) getCurrentObject();
		
		if (currentObject != null) {
			@SuppressWarnings("unchecked")
			ArrayList<Acquisition> acquisitions = (ArrayList<Acquisition>) currentObject[1];
			return acquisitions;
		}

		return null;
	}

	private Integer getCurrentProjectId() {
		Object[] currentObject = (Object[]) getCurrentObject();
		return currentObject != null ? (Integer) currentObject[2] : null;
	}

 	private void userWasChanged() {
 		if (standardMode) {
 			return;
 		}
 
		project.setPossibilities((HashMap<Integer,String>) null);
		cancelWaitingFor(WAITING_FOR_PROJECT_LIST);

 		int userId = user.getSelectedInteger();

		if (userId != DatabaseConstants.EMPTY_DB_ID) {
			int commandId = InputCache.getInstance().projectListGet(this, userId);
			waitingFor(WAITING_FOR_PROJECT_LIST, commandId);
		}
 	}
 
	private void projectWasChanged() {
		source.setPossibilities((HashMap<Integer,String>) null);
		updateLabel();
		cancelWaitingFor(WAITING_FOR_SAMPLE_LIST);

 		int projectId = project.getSelectedInteger();

		if (projectId != DatabaseConstants.EMPTY_DB_ID) {
			int commandId = InputCache.getInstance().sampleListGet(this, projectId);
			waitingFor(WAITING_FOR_SAMPLE_LIST, commandId);
		}
	}

	private void sourceWasChanged() {
		currentStandard = null;
		cancelWaitingFor(WAITING_FOR_STANDARD);

		currentSample = null;
		cancelWaitingFor(WAITING_FOR_SAMPLE);

		currentSampleType = null;
		sampleTypeWasChanged();
		cancelWaitingFor(WAITING_FOR_SAMPLE_TYPE);

		int sourceId = source.getSelectedInteger();

		if (sourceId != DatabaseConstants.EMPTY_DB_ID) {
			if (standardMode) {
				int commandId = StandardCache.getInstance().standardGet(sourceId, this);
				waitingFor(WAITING_FOR_STANDARD, commandId);
			} else {
				int commandId = InputCache.getInstance().sampleGet(this, sourceId);
				waitingFor(WAITING_FOR_SAMPLE, commandId);
			}
		}
	}

	private void sampleTypeWasChanged() {
		if (currentSampleType == null) {
			acidTempComposite.setVisible(false);

		} else {
			if (currentSampleType.getHasAcidTemps()) {
				acidTempComposite.setVisible(true);

				if (acidTemp.getSelectedInteger() == DatabaseConstants.EMPTY_DB_ID && currentSampleType.getDefaultAcidTemp() != DatabaseConstants.EMPTY_DB_ID) {
					if (getCurrentReplicate() != null) {
						acidTemp.setSelectionButLeaveRevertValue(currentSampleType.getDefaultAcidTemp());
					} else {
						acidTemp.selectInteger(currentSampleType.getDefaultAcidTemp());
					}
					
					resultsNeedUpdating = true;
				}

				int sampleTypeId = currentSampleType.getId();

				if (currentAcidTempList == null || currentAcidTempList.getSampleTypeId() != sampleTypeId) {
					currentAcidTempList = null;
					acidTemp.setPossibilities((HashMap<Integer,String>) null);
					cancelWaitingFor(WAITING_FOR_ACID_TEMP_LIST);

					if (sampleTypeId != DatabaseConstants.EMPTY_DB_ID) {
						int commandId = SampleTypeCache.getInstance().acidTempListGet(sampleTypeId, this);
						waitingFor(WAITING_FOR_ACID_TEMP_LIST, commandId);
					}
				} else {
					if (acidTemp.getSelectedInteger() == DatabaseConstants.EMPTY_DB_ID && currentAcidTempList.size() == 1) {
						int longAcidTemp = (Integer) currentAcidTempList.keySet().toArray()[0];

						if (getCurrentReplicate() != null) {
							acidTemp.setSelectionButLeaveRevertValue(longAcidTemp);
						} else {
							acidTemp.selectInteger(longAcidTemp);
						}
						
						resultsNeedUpdating = true;
					}
				}

			} else {
				acidTempComposite.setVisible(false);
			}
		}
	}

	private void retrieveRawFile(int rawFileId) {
		int commandId = InputCache.getInstance().rawFileGet(rawFileId, this);
		waitingFor(WAITING_FOR_RAW_FILE, commandId);
	}

	private void retrieveRawFile(RawFile rawFile, byte[] fileBytes) {
		String oldFilename = rawFile.getOriginalName();

		FileDialog fileDialog = new FileDialog(getParent().getShell(), SWT.SAVE);
		if (oldFilename != null) {
			fileDialog.setFileName(oldFilename);
		}
		fileDialog.setText(Messages.replicateComposite_saveToFile);
		fileDialog.setOverwrite(true);
	    String filePath = fileDialog.open();

	    if (filePath != null && !filePath.trim().isEmpty()) {
	    		filePath = filePath.trim();

	    		try {
	    			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
				fileOutputStream.write(fileBytes);
				fileOutputStream.close();
			} catch (IOException e) {
				String error = MessageFormat.format(Messages.replicateComposite_errorSavingFile, new File(filePath).getName());
				MessageDialog.openError(getParent().getShell(), Messages.replicateComposite_errorSavingFileTitle, error);
			}
	    }
	}

	@Override
	protected void setCurrentFieldValues() {
		ReplicateV1 currentReplicate = getCurrentReplicate();

		id.setText(String.valueOf(currentReplicate.getId()));
		massSpec.selectInteger(currentReplicate.getMassSpecId());
		acidTemp.selectInteger(currentReplicate.getAcidTempId());

		if (standardMode) {
			sourceId = currentReplicate.getStandardId();
			updateLabel();

			source.selectInteger(currentReplicate.getStandardId());
			sourceWasChanged();

			user.selectInteger(currentReplicate.getUserId());

		} else {
			sourceId = currentReplicate.getSampleId();
			updateLabel();

			source.selectInteger(currentReplicate.getSampleId());
			sourceWasChanged();

			project.selectInteger(getCurrentProjectId());
			projectWasChanged();

			user.selectInteger(currentReplicate.getUserId());
			userWasChanged();
		}

		disabled.setSelection(currentReplicate.isDisabled());
		description.setText(currentReplicate.getDescription());
		
		ArrayList<Acquisition> currentAcquisitions = getCurrentAcquisitions();
		ArrayList<Acquisition> acquistions = new ArrayList<Acquisition>();

		if (currentAcquisitions != null) {
			for (Acquisition acquisition : currentAcquisitions) {
				acquistions.add(new Acquisition(acquisition));
			}
		}
		
		acquisitionsWidget.setAcquisitions(acquistions);

		resultsNeedUpdating = true;
		loadCorrIntervalList();
	}

	private void updateLabel() {
		HashMap<Integer,String> possibilities = source.getPossibilities();

		if (possibilities != null && sourceId != null) {
			getChainedPart().getPart().setLabel(Messages.replicateComposite_labelPrefix + possibilities.get(sourceId));
		}
	}
	
	@Override
	protected void setDefaultFieldValues() {
		getChainedPart().getPart().setLabel(Messages.editor_replicateTab);

		id.setText(Messages.replicateComposite_newId);
		massSpec.selectInteger(initialMassSpecId != null ? initialMassSpecId : DatabaseConstants.EMPTY_DB_ID);
		acidTemp.selectInteger(DatabaseConstants.EMPTY_DB_ID);

		if (standardMode) {
			source.selectInteger(DatabaseConstants.EMPTY_DB_ID);
			sourceWasChanged();

			user.selectInteger(initialUserId != null ? initialUserId : LoginInfoCache.getInstance().getUser().getId());

		} else {
			source.selectInteger(initialSampleId != null ? initialSampleId : DatabaseConstants.EMPTY_DB_ID);
			sourceWasChanged();

			project.selectInteger(initialProjectId != null ? initialProjectId : DatabaseConstants.EMPTY_DB_ID);
			projectWasChanged();

			user.selectInteger(initialUserId != null ? initialUserId : LoginInfoCache.getInstance().getUser().getId());
			userWasChanged();
		}

		disabled.setSelection(false);
		description.setText("");
		acquisitionsWidget.setAcquisitions(new ArrayList<Acquisition>());
		
		resultsNeedUpdating = true;
		loadCorrIntervalList();
	}

	@Override
	public void enableWidgets() {
		User currentUser = LoginInfoCache.getInstance().getUser();
		Permissions permissions = LoginInfoCache.getInstance().getPermissions();

		boolean canEditAllInput = permissions.isCanEditAllReplicates();
		boolean belongsToUser = getCurrentReplicate() == null ? true : getCurrentReplicate().getUserId() == currentUser.getId();
		boolean hasGeneralEditPermission = canEditAllInput || belongsToUser;
		boolean userNeedsRevert = !canEditAllInput && user.hasChanged();

		if (!hasGeneralEditPermission) {
			massSpec.revert();
			resultsNeedUpdating = true;
			loadCorrIntervalList();
		}

		massSpec.setEnabled(hasGeneralEditPermission);

		if (!hasGeneralEditPermission || userNeedsRevert) {
			acidTemp.revert();
			resultsNeedUpdating = true;
		}

		acidTemp.setEnabled(hasGeneralEditPermission);

		if (!hasGeneralEditPermission || userNeedsRevert) {
			source.revert();
			sourceWasChanged();
		}

		source.setEnabled(hasGeneralEditPermission);

		if (!standardMode) {
			if (!hasGeneralEditPermission || userNeedsRevert) {
				project.revert();
				projectWasChanged();
			}

			project.setEnabled(hasGeneralEditPermission);
		}

		if (userNeedsRevert) {
			user.revert();
			userWasChanged();
		}

		user.setEnabled(canEditAllInput);

		if (!hasGeneralEditPermission) {
			disabled.revert();
		}

		disabled.setEnabled(hasGeneralEditPermission);
		
		if (!hasGeneralEditPermission) {
			description.revert();
		}

		description.setEnabled(hasGeneralEditPermission);

		if (!hasGeneralEditPermission) {
			acquisitionsWidget.revert();
			resultsNeedUpdating = true;
		}

		acquisitionsWidget.setEnabled(hasGeneralEditPermission);
	}

	@Override
	public void disableWidgets() {
		user.setEnabled(false);
		massSpec.setEnabled(false);
		if (!standardMode) {
			project.setEnabled(false);
		}
		source.setEnabled(false);
		acidTemp.setEnabled(false);
		disabled.setEnabled(false);
		description.setEnabled(false);
		acquisitionsWidget.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean fieldsContainNewData = false;

		fieldsContainNewData = fieldsContainNewData || user.hasChanged();
		fieldsContainNewData = fieldsContainNewData || massSpec.hasChanged();
		fieldsContainNewData = fieldsContainNewData || (!standardMode && project.hasChanged());
		fieldsContainNewData = fieldsContainNewData || source.hasChanged();
		fieldsContainNewData = fieldsContainNewData || (acidTempComposite.isVisible() && acidTemp.hasChanged());
		fieldsContainNewData = fieldsContainNewData || disabled.hasChanged();
		fieldsContainNewData = fieldsContainNewData || description.hasChanged();
		fieldsContainNewData = fieldsContainNewData || acquisitionsWidget.hasChanged();

		return fieldsContainNewData;
	}

	@Override
	protected boolean hasError() {
		if (getCurrentReplicate() != null) {
			newReplicate.setId(getCurrentReplicate().getId());
		}

		newReplicate.setUserId(user.getSelectedInteger());
		newReplicate.setDisabled(disabled.getSelection());
		newReplicate.setDescription(description.getText());

		boolean shouldBeError = false;

		if (massSpec.getSelectionIndex() == -1) {
			massSpecError.setToolTipText(Messages.replicateComposite_massSpecEmpty);
			shouldBeError = true;
		} else {
			newReplicate.setMassSpecId(massSpec.getSelectedInteger());
		}

		if (massSpecError.getVisible() != shouldBeError) {
			massSpecError.setVisible(shouldBeError);
			layoutNeeded();
		}

		if (disabled.getSelection()) {
			shouldBeError = false;
		} else {
			shouldBeError = true;

			for (Acquisition acquisition : acquisitionsWidget.getAcquisitions()) {
				if (!acquisition.getAcquisitionInput().isDisabled()) {
					shouldBeError = false;
					break;
				}
			}
		}

		if (shouldBeError) {
			disabledError.setToolTipText(Messages.replicateComposite_noAcqusitionsEnabled);
		}
	
		if (disabledError.getVisible() != shouldBeError) {
			disabledError.setVisible(shouldBeError);
			layoutNeeded();
		}

		if (!standardMode) {
			shouldBeError = false;
	
			if (project.getSelectionIndex() == -1) {
				projectError.setToolTipText(Messages.replicateComposite_projectEmpty);
				shouldBeError = true;
			}
	
			if (projectError.getVisible() != shouldBeError) {
				projectError.setVisible(shouldBeError);
				layoutNeeded();
			}
		}

		shouldBeError = false;

		if (source.getSelectionIndex() == -1) {
			sourceError.setToolTipText(standardMode ? Messages.replicateComposite_standardEmpty : Messages.replicateComposite_sampleEmpty);
			shouldBeError = true;

		} else {
			if (standardMode) {
				newReplicate.setSampleId(DatabaseConstants.EMPTY_DB_ID);
				newReplicate.setStandardId(source.getSelectedInteger());
			} else {				
				newReplicate.setSampleId(source.getSelectedInteger());
				newReplicate.setStandardId(DatabaseConstants.EMPTY_DB_ID);
			}
		}

		if (sourceError.getVisible() != shouldBeError) {
			sourceError.setVisible(shouldBeError);
			layoutNeeded();
		}

		shouldBeError = false;

		if (acidTempComposite.isVisible() && acidTemp.getSelectionIndex() == -1) {
			acidTempError.setToolTipText(Messages.replicateComposite_acidTempEmpty);
			shouldBeError = true;
		} else {
			newReplicate.setAcidTempId((currentSampleType != null && currentSampleType.getHasAcidTemps()) ? acidTemp.getSelectedInteger() : DatabaseConstants.EMPTY_DB_ID);
		}

		if (acidTempError.getVisible() != shouldBeError) {
			acidTempError.setVisible(shouldBeError);
			layoutNeeded();
		}

		ArrayList<Acquisition> acquisitions = acquisitionsWidget.getAcquisitions();
		boolean hasError = massSpecError.isVisible() || disabledError.isVisible() || (projectError != null && projectError.isVisible()) || sourceError.isVisible() || currentSampleType == null || (acidTempComposite.isVisible() && acidTempError.isVisible()) || acquisitions.size() == 0;

		if (hasError) {
			resultsComposite.newReplicate(null, null);

		} else {
			if (resultsNeedUpdating) {
				resultsNeedUpdating = false;
				resultsComposite.newReplicate(newReplicate, new ArrayList<Acquisition>(acquisitions));
			}
		}

		return hasError;
	}

	private Integer[] getChannelToMZX10() {
		if ((initialReplicateId != null && getCurrentReplicate() == null) || currentCorrIntervalList == null) {
			return null;
		}

		if (getCurrentReplicate() != null && getCurrentReplicate().getMassSpecId() == massSpec.getSelectedInteger()) {
			return getCurrentReplicate().getChannelToMzX10();
		}

		if (acquisitionsWidget.getAcquisitionsAsTreeSet().size() == 0) {
			return null;
		}

		CorrIntervalListItem corrIntervalListItem = null;

		ArrayList<CorrIntervalListItem> sorted = new ArrayList<CorrIntervalListItem>(currentCorrIntervalList.values());
		Collections.sort(sorted, new CorrIntervalListItemComparator());

		Acquisition firstAcquisition = acquisitionsWidget.getAcquisitionsAsTreeSet().first();
		long searchDate = firstAcquisition.getAcquisitionParsed().getDate();

		for (int i=0; i<sorted.size(); i++) {
			CorrIntervalListItem thisOne = sorted.get(i);
			CorrIntervalListItem nextOne = (i == sorted.size()-1) ? null : sorted.get(i+1);

			if (thisOne.getDate() <= searchDate && (nextOne == null || nextOne.getDate() > searchDate)) {
				corrIntervalListItem = thisOne;
				break;
			}
		}

		if (corrIntervalListItem != null && corrIntervalListItem.getChannelToMZX10() != null && corrIntervalListItem.getChannelToMZX10().length != 0) {
			return corrIntervalListItem.getChannelToMZX10();

		} else if (firstAcquisition.getAcquisitionParsed().getChannelToMzX10() == null) {
			return null;

		} else {
			Integer[] defaultMapping = (Integer[]) firstAcquisition.getAcquisitionParsed().getChannelToMzX10().clone(); 

			for (int i=0; i<defaultMapping.length; i++) {
				Integer mzX10 = defaultMapping[i];
				if (mzX10 != null && !validMZX10s.contains(mzX10)) {
					defaultMapping[i] = null;
				}
			}

			return defaultMapping;
		}
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String, Object> selection) {
		// only called once - so set these globally here

		initialUserId = (Integer) getSelection().get(ReplicatePart.SELECTION_INITIAL_USER_ID);
		initialProjectId = (Integer) getSelection().get(ReplicatePart.SELECTION_INITIAL_PROJECT_ID);
		initialMassSpecId = (Integer) getSelection().get(ReplicatePart.SELECTION_INITIAL_MASS_SPEC_ID);
		initialSampleId = (Integer) getSelection().get(ReplicatePart.SELECTION_INITIAL_SAMPLE_ID);
		initialReplicateId = (Integer) getSelection().get(ReplicatePart.SELECTION_REPLICATE_ID);

		return true;
	}

	@Override
	protected boolean processSelection(HashMap<String, Object> selection) {
		if (initialReplicateId != null) {
			int commandId = InputCache.getInstance().replicateGet(initialReplicateId, this);
			waitingFor(WAITING_FOR_REPLICATE, commandId);
		}

		return true;
	}

	@Override
	protected void requestSave() {
		int commandId = InputCache.getInstance().replicateSave(newReplicate, acquisitionsWidget.getAcquisitions(), this);
		waitingFor(WAITING_FOR_REPLICATE_SAVE, commandId);
	}

	@Override
	protected boolean canDelete() {
		ReplicateV1 currentReplicate = getCurrentReplicate();

		if (currentReplicate != null) {
			if (LoginInfoCache.getInstance().getPermissions().isCanDeleteAll()) {
				return true;
			}

			int currentUserId = LoginInfoCache.getInstance().getUser().getId();
			boolean canDeleteOwn = LoginInfoCache.getInstance().getPermissions().isCanDeleteOwn();

			if (canDeleteOwn && currentReplicate.getUserId() == currentUserId) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean requestDelete() {
		if (getChainedPart().raiseQuestion(Messages.replicateComposite_reallyDelete)) {
			int commandId = InputCache.getInstance().replicateDelete(getCurrentReplicate().getId(), this);
			waitingFor(WAITING_FOR_REPLICATE_DELETE, commandId);
			return true;
		}

		return false;
	}

	private void loadCorrIntervalList() {
		if (currentCorrIntervalList != null && currentCorrIntervalList.getMassSpecId() == massSpec.getSelectedInteger()) {
			return;
		}

		currentCorrIntervalList = null;
		int massSpecId = massSpec.getSelectedInteger();

		if (massSpecId != DatabaseConstants.EMPTY_DB_ID) {
			cancelWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST_MAIN);
			int commandId = CorrIntervalCache.getInstance().corrIntervalListGet(massSpecId, this);
			waitingFor(WAITING_FOR_CORR_INTERVAL_LIST_MAIN, commandId);

			if (commandId == Command.UNDEFINED_ID) {
				setChannelToMZX10IfReady();
			}
		}
	}

	private void setChannelToMZX10IfReady() {
		if (currentCorrIntervalList != null && currentCorrIntervalList.getMassSpecId() == massSpec.getSelectedInteger()) {
			acquisitionsWidget.setCorrIntervalList(getCurrentReplicate(), currentCorrIntervalList);
		} else {
			acquisitionsWidget.setCorrIntervalList(null, null);
		}

		newReplicate.setChannelToMzX10(getChannelToMZX10());
	}

	@Override
	public void replicateGetCompleted(int commandId, ReplicateV1 replicate, ArrayList<Acquisition> acquisitions, Integer projectId, SampleType sampleType) {
		if (commandIdForKey(WAITING_FOR_REPLICATE) == commandId) {
			newObject(WAITING_FOR_REPLICATE, new Object[] { replicate, acquisitions, projectId, sampleType });
			newReplicate.setChannelToMzX10(getChannelToMZX10());
		}
	}

	@Override
	public void replicateGetError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_REPLICATE) == commandId) {
			raiseGetError(WAITING_FOR_REPLICATE, message);
		}
	}

	@Override
	public void replicateUpdated(int commandId, ReplicateV1 replicate, ArrayList<Acquisition> acquisitions, Integer projectId, SampleType sampleType) {
		if (getCurrentReplicate() != null && getCurrentReplicate().getId() == replicate.getId()) {
			updateObject(new Object[] { replicate, acquisitions, projectId, sampleType }, Messages.replicateComposite_replicateHasBeenUpdated);
			newReplicate.setChannelToMzX10(getChannelToMZX10());
		}
	}
 
	@Override
	public void replicateDeleted(int replicateId) {
		if (getCurrentReplicate() != null && getCurrentReplicate().getId() == replicateId) {
			getChainedPart().raiseInfo(Messages.replicateComposite_replicateDeleted);
			getChainedPart().closePart();
		}
	}

	@Override
	public void replicateSaveCompleted(int commandId) {
		if (commandIdForKey(WAITING_FOR_REPLICATE_SAVE) == commandId) {
			saveComplete(WAITING_FOR_REPLICATE_SAVE, null);
		}
	}

	@Override
	public void replicateSaveError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_REPLICATE_SAVE) == commandId) {
			raiseSaveOrDeleteError(WAITING_FOR_REPLICATE_SAVE, message);
		}
	}

	public void replicateDeleteCompleted(int commandId) {
		if (commandIdForKey(WAITING_FOR_REPLICATE_DELETE) == commandId) {
			deleteComplete(WAITING_FOR_REPLICATE_DELETE);
		}
	}

	public void replicateDeleteError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_REPLICATE_DELETE) == commandId) {
			raiseSaveOrDeleteError(WAITING_FOR_REPLICATE_DELETE, message);
		}
	}

	@Override
	public void userListGetCompleted(int commandId, final UserList userList) {
		doneWaitingFor(WAITING_FOR_USER_LIST);
		user.setPossibilities(userList);

		if (commandId != Command.UNDEFINED_ID) {
			widgetStatusChanged();
		}
	}

	@Override
	public void userListUpdated(int commandId, final UserList userList) {
		doneWaitingFor(WAITING_FOR_USER_LIST);
		user.setPossibilities(userList);
		widgetStatusChanged();
	}

	@Override
	public void userListGetError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_USER_LIST) == commandId) {
			raiseGetError(WAITING_FOR_USER_LIST, message);
		}
	}

	@Override
	public void massSpecListGetCompleted(int commandId, MassSpecList massSpecList) {
		doneWaitingFor(WAITING_FOR_MASS_SPEC_LIST);
		massSpec.setPossibilities(massSpecList);

		if (massSpec.getSelectedInteger() == DatabaseConstants.EMPTY_DB_ID && massSpecList.size() == 1) {
			int loneProject = (Integer) massSpecList.keySet().toArray()[0];
			massSpec.setSelectionButLeaveRevertValue(loneProject);
			resultsNeedUpdating = true;
			loadCorrIntervalList();
		}

		if (commandId != Command.UNDEFINED_ID) {
			widgetStatusChanged();
		}
	}

	@Override
	public void massSpecListUpdated(int commandId, MassSpecList massSpecList) {
		doneWaitingFor(WAITING_FOR_MASS_SPEC_LIST);
		massSpec.setPossibilities(massSpecList);
		widgetStatusChanged();
	}

	@Override
	public void massSpecListGetError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_MASS_SPEC_LIST) == commandId) {
			raiseGetError(WAITING_FOR_MASS_SPEC_LIST, message);
		}
	}

	@Override
	public void standardListGetCompleted(int commandId, StandardList standardList) {
		doneWaitingFor(WAITING_FOR_STANDARD_LIST);
		source.setPossibilities(standardList);
		updateLabel();

		if (commandId != Command.UNDEFINED_ID) {
			widgetStatusChanged();
		}
	}

	@Override
	public void standardListUpdated(int commandId, StandardList standardList) {
		doneWaitingFor(WAITING_FOR_STANDARD_LIST);
		source.setPossibilities(standardList);
		updateLabel();
		widgetStatusChanged();
	}

	@Override
	public void standardListGetError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_STANDARD_LIST) == commandId) {
			raiseGetError(WAITING_FOR_STANDARD_LIST, message);
		}
	}

	@Override
	public void projectListGetCompleted(int commandId, ProjectList projectList) {
		if (commandIdForKey(WAITING_FOR_PROJECT_LIST) == commandId) {
			doneWaitingFor(WAITING_FOR_PROJECT_LIST);
			project.setPossibilities(projectList);

			if (project.getSelectedInteger() == DatabaseConstants.EMPTY_DB_ID && projectList.size() == 1) {
				int loneProjectId = (Integer) projectList.keySet().toArray()[0];
				project.setSelectionButLeaveRevertValue(loneProjectId);
				projectWasChanged();
			}

			if (commandId != Command.UNDEFINED_ID) {
				widgetStatusChanged();
			}
		}
	}

	@Override
	public void projectListUpdated(int commandId, ProjectList projectList) {
		if (project != null && user.getSelectedInteger() == projectList.getUserId()) {
			doneWaitingFor(WAITING_FOR_PROJECT_LIST);
			project.setPossibilities(projectList);
			widgetStatusChanged();
		}
	}

	@Override
	public void projectListGetError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_PROJECT_LIST) == commandId) {
			raiseGetError(WAITING_FOR_PROJECT_LIST, message);
		}
	}

	@Override
	public void sampleListGetCompleted(int commandId, SampleList sampleList) {
		if (commandIdForKey(WAITING_FOR_SAMPLE_LIST) == commandId) {
			doneWaitingFor(WAITING_FOR_SAMPLE_LIST);
			source.setPossibilities(sampleList);
			updateLabel();
			
			if (source.getSelectedInteger() == DatabaseConstants.EMPTY_DB_ID && sampleList.size() == 1) {
				int loneSample = (Integer) sampleList.keySet().toArray()[0];
				source.setSelectionButLeaveRevertValue(loneSample);
				sourceWasChanged();
			}

			if (commandId != Command.UNDEFINED_ID) {
				widgetStatusChanged();
			}
		}
	}

	@Override
	public void sampleListUpdated(int commandId, SampleList sampleList) {
		if (project != null && project.getSelectedInteger() == sampleList.getProjectId()) {
			doneWaitingFor(WAITING_FOR_SAMPLE_LIST);
			source.setPossibilities(sampleList);
			updateLabel();
			widgetStatusChanged();
		}
	}

	@Override
	public void sampleListGetError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_SAMPLE_LIST) == commandId) {
			raiseGetError(WAITING_FOR_SAMPLE_LIST, message);
		}
	}

	@Override
	public void standardGetCompleted(final int commandId, final Standard standard) {
		if (commandIdForKey(WAITING_FOR_STANDARD) == commandId) {
			int sampleTypeId = standard.getSampleTypeId();
			int newCommandId = SampleTypeCache.getInstance().sampleTypeGet(sampleTypeId, this);
			waitingFor(WAITING_FOR_SAMPLE_TYPE, newCommandId);
			doneWaitingFor(WAITING_FOR_STANDARD);
		}
	}

	@Override
	public void standardUpdated(final int commandId, final Standard standard) {
		if (standardMode && source.getSelectedInteger() == standard.getId()) {
			int sampleTypeId = standard.getSampleTypeId();
			int newCommandId = SampleTypeCache.getInstance().sampleTypeGet(sampleTypeId, this);
			waitingFor(WAITING_FOR_SAMPLE_TYPE, newCommandId);
			doneWaitingFor(WAITING_FOR_STANDARD);
		}
	}

	@Override
	public void standardGetError(final int commandId, final String message) {
		if (commandIdForKey(WAITING_FOR_STANDARD) == commandId) {
			raiseGetError(WAITING_FOR_STANDARD, message);
		}
	}

	@Override
	public void sampleGetCompleted(int commandId, Sample sample) {
		if (commandIdForKey(WAITING_FOR_SAMPLE) == commandId) {
			int sampleTypeId = sample.getSampleTypeId();
			int newCommandId = SampleTypeCache.getInstance().sampleTypeGet(sampleTypeId, this);
			waitingFor(WAITING_FOR_SAMPLE_TYPE, newCommandId);
			doneWaitingFor(WAITING_FOR_SAMPLE);
		}
	}

	@Override
	public void sampleUpdated(int commandId, Sample sample) {
		if (!standardMode && source.getSelectedInteger() == sample.getId()) {
			int sampleTypeId = sample.getSampleTypeId();
			int newCommandId = SampleTypeCache.getInstance().sampleTypeGet(sampleTypeId, this);
			waitingFor(WAITING_FOR_SAMPLE_TYPE, newCommandId);
			doneWaitingFor(WAITING_FOR_STANDARD);
		}
	}

	@Override
	public void sampleGetError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_SAMPLE) == commandId) {
			raiseGetError(WAITING_FOR_SAMPLE, message);
		}
	}

	@Override
	public void sampleTypeGetCompleted(final int commandId, final SampleType sampleType) {
		if (commandIdForKey(WAITING_FOR_SAMPLE_TYPE) == commandId) {
			currentSampleType = sampleType;
			sampleTypeWasChanged();
			doneWaitingFor(WAITING_FOR_SAMPLE_TYPE);

			if (commandId != Command.UNDEFINED_ID) {
				widgetStatusChanged();
			}
		}
	}

	@Override
	public void sampleTypeUpdated(int commandId, SampleType sampleType) {
		boolean matchesStandard = standardMode && currentStandard != null && sampleType.getId() == currentStandard.getSampleTypeId();
		boolean matchesSample = !standardMode && currentSample != null && sampleType.getId() == currentSample.getSampleTypeId();

		if (matchesSample || matchesStandard) {
			currentSampleType = sampleType;
			sampleTypeWasChanged();
			doneWaitingFor(WAITING_FOR_SAMPLE_TYPE);
			
			widgetStatusChanged();
		}
	}

	@Override
	public void sampleTypeGetError(final int commandId, final String message) {
		if (commandIdForKey(WAITING_FOR_SAMPLE_TYPE) == commandId) {
			raiseGetError(WAITING_FOR_SAMPLE_TYPE, message);
		}
	}

	@Override
	public void acidTempListGetCompleted(final int commandId, final AcidTempList acidTempList) {
		if (commandIdForKey(WAITING_FOR_ACID_TEMP_LIST) == commandId) {
			doneWaitingFor(WAITING_FOR_ACID_TEMP_LIST);
			currentAcidTempList = acidTempList;
			acidTemp.setPossibilities(acidTempList);

			if (acidTemp.getSelectedInteger() == DatabaseConstants.EMPTY_DB_ID && acidTempList.size() == 1) {
				int loneProject = (Integer) acidTempList.keySet().toArray()[0];

				if (getCurrentReplicate() != null) {
					acidTemp.setSelectionButLeaveRevertValue(loneProject);
				} else {
					acidTemp.selectInteger(loneProject);
				}

				resultsNeedUpdating = true;
			}

			if (commandId != Command.UNDEFINED_ID) {
				widgetStatusChanged();
			}
		}
	}

	@Override
	public void acidTempListUpdated(int commandId, final AcidTempList acidTempList) {
		if (currentSampleType != null && currentSampleType.getId() == acidTempList.getSampleTypeId()) {
			doneWaitingFor(WAITING_FOR_ACID_TEMP_LIST);
			currentAcidTempList = acidTempList;
			acidTemp.setPossibilities(acidTempList);
			widgetStatusChanged();
		}
	}

	@Override
	public void acidTempListGetError(final int commandId, final String message) {
		if (commandIdForKey(WAITING_FOR_ACID_TEMP_LIST) == commandId) {
			raiseGetError(WAITING_FOR_ACID_TEMP_LIST, message);
		}
	}

	@Override
	public void rawFileGetCompleted(final int commandId, final RawFile rawFile, final byte[] fileBytes) {
		doneWaitingFor(WAITING_FOR_RAW_FILE);
		retrieveRawFile(rawFile, fileBytes);
	}

	@Override
	public void rawFileGetError(final int commandId, final String message) {
		raiseGetError(WAITING_FOR_RAW_FILE, message);
	}

	public boolean canAcceptFiles() {
		return acquisitionsWidget.canAcceptFiles();
	}

	public void addFiles(String[] newFilenames) {
		acquisitionsWidget.addFiles(newFilenames);
	}

	@Override
	public void corrIntervalListGetCompleted(int commandId, CorrIntervalList corrIntervalList) {
		if (commandIdForKey(WAITING_FOR_CORR_INTERVAL_LIST_MAIN) == commandId) {
			doneWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST_MAIN);
			this.currentCorrIntervalList = corrIntervalList;
			setChannelToMZX10IfReady();
		}
	}

	@Override
	public void corrIntervalListUpdated(int commandId, CorrIntervalList corrIntervalList) {
		if (massSpec.getSelectedInteger() == corrIntervalList.getMassSpecId()) {
			cancelWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST_MAIN);
			this.currentCorrIntervalList = corrIntervalList;
			setChannelToMZX10IfReady();
		}
	}

	@Override
	public void corrIntervalListGetError(int commandId, String message) {
		raiseGetError(WAITING_FOR_CORR_INTERVAL_LIST_MAIN, message);
	}

	public class CorrIntervalListItemComparator implements Comparator<CorrIntervalListItem> {
		@Override
		public int compare(CorrIntervalListItem arg0, CorrIntervalListItem arg1) {
			return ((Long) arg0.getDate()).compareTo(arg1.getDate());
		}
	}

	static {
		for (InputParameter inputParameter : InputParameter.values()) {
			if (inputParameter.getMzX10() != null) {
				validMZX10s.add(inputParameter.getMzX10());
			}
		}
	}
}
