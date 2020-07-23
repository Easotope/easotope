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

package org.easotope.client.rawdata.scan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.client.core.widgets.VButton;
import org.easotope.client.core.widgets.VCombo;
import org.easotope.client.core.widgets.VText;
import org.easotope.client.rawdata.scan.widget.scanfile.ScanFilesWidget;
import org.easotope.client.rawdata.scan.widget.scanfile.ScanFilesWidgetListener;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecCacheMassSpecListGetListener;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecList;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.userlist.UserCacheUserListGetListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalCacheCorrIntervalListGetListener;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.ScanFile;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.rawfile.InputCacheRawFileGetListener;
import org.easotope.shared.rawdata.cache.input.scan.InputCacheScanGetListener;
import org.easotope.shared.rawdata.cache.input.scan.InputCacheScanSaveListener;
import org.easotope.shared.rawdata.cache.input.scanlist.InputCacheScanListGetListener;
import org.easotope.shared.rawdata.cache.input.scanlist.ScanList;
import org.easotope.shared.rawdata.cache.input.scanlist.ScanListItem;
import org.easotope.shared.rawdata.tables.ScanV3;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

public class ScanComposite extends EditorComposite
		implements UserCacheUserListGetListener, MassSpecCacheMassSpecListGetListener,
		CorrIntervalCacheCorrIntervalListGetListener, InputCacheScanListGetListener,
		InputCacheScanGetListener, InputCacheScanSaveListener, InputCacheRawFileGetListener {

	private final String WAITING_FOR_USER_LIST = "WAITING_FOR_USER_LIST";
	private final String WAITING_FOR_MASS_SPEC_LIST = "WAITING_OR_MASS_SPEC_LIST";
	private final String WAITING_FOR_PRECEDING_SCAN = "WAITING_FOR_PRECEDING_SCAN";
	private final String WAITING_FOR_SCAN = "WAITING_FOR_SCAN";
	private final String WAITING_FOR_SCAN_SAVE = "WAITING_FOR_SCAN_SAVE";
	private final String WAITING_FOR_RAW_FILE = "WAITING_FOR_RAW_FILE";
	private final String WAITING_FOR_SCAN_DELETE = "WAITING_FOR_SCAN_DELETE";
	private final String WAITING_FOR_CORR_INTERVAL_LIST = "WAITING_FOR_CORR_INTERVAL_LIST";

	private Integer initialMassSpecId;
	private Integer initialScanId;

 	private Label id;
 	private SortedCombo user;
	private SortedCombo massSpec;
	private Canvas massSpecError;
	private VButton disabled;
	private Combo retrieveCombo;
	private Button loadPrecedingButton;
	private VText description;

	private ScanList currentScanList = null;
	private int precedingScanFileId = DatabaseConstants.EMPTY_DB_ID;

	private CorrIntervalList currentCorrIntervalList = null;

	private ScanFilesWidget scanFilesWidget;
	private HashMap<Integer,ScanFile> retrieveIndexToScanFile = new HashMap<Integer,ScanFile>();

	ScanComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);
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
		idLabel.setText(Messages.scanComposite_idLabel);

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
		userLabel.setText(Messages.scanComposite_userLabel);

		user = new SortedCombo(userComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		user.setLayoutData(gridData);
		user.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
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
		massSpecLabel.setText(Messages.scanComposite_massSpecLabel);

		massSpec = new SortedCombo(massSpecComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		massSpec.setLayoutData(gridData);
		massSpec.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				loadScanList();
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

		disabled = new VButton(firstLineComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		disabled.setLayoutData(gridData);
		disabled.setText(Messages.scanComposite_scanDisabled);
		disabled.addListener(SWT.Selection, new LoggingAdaptor() {
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		final Label descriptionLabel = new Label(firstLineComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		descriptionLabel.setLayoutData(gridData);
		descriptionLabel.setText(Messages.scanComposite_descriptionLabel);

		Composite secondLineComposite = new Composite(leftComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		secondLineComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		secondLineComposite.setLayout(gridLayout);

		final Label retrieveLabel = new Label(secondLineComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		retrieveLabel.setLayoutData(gridData);
		retrieveLabel.setText(Messages.scanComposite_retrieveLabel);

		retrieveCombo = new VCombo(secondLineComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		retrieveCombo.setLayoutData(gridData);
		retrieveCombo.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				ScanFile scanFile = retrieveIndexToScanFile.get(retrieveCombo.getSelectionIndex());
				retrieveCombo.deselectAll();

				if (scanFile.getFileBytes() != null) {
					retrieveRawFile(scanFile.getRawFile(), scanFile.getFileBytes());
				} else {
					retrieveRawFile(scanFile.getRawFile().getId());
				}
			}
		});

		loadPrecedingButton = new Button(secondLineComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.grabExcessHorizontalSpace = true;
		loadPrecedingButton.setLayoutData(gridData);
		loadPrecedingButton.setText(Messages.scanComposite_loadPrecedingButton);
		loadPrecedingButton.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				cancelWaitingFor(WAITING_FOR_PRECEDING_SCAN);
				int commandId = InputCache.getInstance().scanGet(precedingScanFileId, ScanComposite.this);
				waitingFor(WAITING_FOR_PRECEDING_SCAN, commandId);
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
 
 		scanFilesWidget = new ScanFilesWidget(chainedPart, tablesComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(100);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		scanFilesWidget.setLayoutData(formData);
		scanFilesWidget.addListener(new ScanFilesWidgetListener() {
			@Override
			public void scanFilesChanged() {
				widgetStatusChanged();
				setRetrieveDropdown();
				enableLoadPrecedingButtonIfAllowed();
			}
 		});

		cancelWaitingFor(WAITING_FOR_USER_LIST);
		int commandId = UserCache.getInstance().userListGet(this);
		waitingFor(WAITING_FOR_USER_LIST, commandId);

		cancelWaitingFor(WAITING_FOR_MASS_SPEC_LIST);
		commandId = MassSpecCache.getInstance().massSpecListGet(this);
		waitingFor(WAITING_FOR_MASS_SPEC_LIST, commandId);

		UserCache.getInstance().addListener(this);
		MassSpecCache.getInstance().addListener(this);
		InputCache.getInstance().addListener(this);
		CorrIntervalCache.getInstance().addListener(this);
	}

	private void setRetrieveDropdown() {
		retrieveIndexToScanFile.clear();
		retrieveCombo.removeAll();

		for (ScanFile scanFile : scanFilesWidget.getScanFiles()) {
			retrieveIndexToScanFile.put(retrieveCombo.getItemCount(), scanFile);
			retrieveCombo.add(scanFile.getRawFile().getOriginalName());
		}

		retrieveCombo.select(-1);
	}

	@Override
	protected void handleDispose() {
		UserCache.getInstance().removeListener(this);
		MassSpecCache.getInstance().removeListener(this);
		InputCache.getInstance().removeListener(this);
		CorrIntervalCache.getInstance().removeListener(this);
	}

	private ScanV3 getCurrentScan() {
		Object[] currentObject = (Object[]) getCurrentObject();
		return currentObject != null ? (ScanV3) currentObject[0] : null;
	}

	private ArrayList<ScanFile> getCurrentScanFiles() {
		Object[] currentObject = (Object[]) getCurrentObject();

		if (currentObject != null) {
			@SuppressWarnings("unchecked")
			ArrayList<ScanFile> scanFiles = (ArrayList<ScanFile>) currentObject[1];
			return scanFiles;
		}

		return null;
	}

	private void retrieveRawFile(int rawFileId) {
		cancelWaitingFor(WAITING_FOR_RAW_FILE);
		int commandId = InputCache.getInstance().rawFileGet(rawFileId, this);
		waitingFor(WAITING_FOR_RAW_FILE, commandId);
	}

	private void retrieveRawFile(RawFile rawFile, byte[] fileBytes) {
		String oldFilename = rawFile.getOriginalName();

		FileDialog fileDialog = new FileDialog(getParent().getShell(), SWT.SAVE);
		if (oldFilename != null) {
			fileDialog.setFileName(oldFilename);
		}
		fileDialog.setText(Messages.scanComposite_saveToFile);
		fileDialog.setOverwrite(true);
	    String filePath = fileDialog.open();

	    if (filePath != null && !filePath.trim().isEmpty()) {
	    		filePath = filePath.trim();

	    		try {
	    			FileOutputStream fileOutputStream = new FileOutputStream(filePath);
				fileOutputStream.write(fileBytes);
				fileOutputStream.close();
			} catch (IOException e) {
				String error = MessageFormat.format(Messages.scanComposite_errorSavingFile, new File(filePath).getName());
				MessageDialog.openError(getParent().getShell(), Messages.scanComposite_errorSavingFileTitle, error);
			}
	    }
	}

	@Override
	protected void setCurrentFieldValues() {
		ScanV3 currentScan = getCurrentScan();

		String tz = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
		String date = DateFormat.format(currentScan.getDate(), tz, false, false);
		getChainedPart().getPart().setLabel(Messages.scanComposite_labelPrefix + date);

		id.setText(String.valueOf(currentScan.getId()));
		massSpec.selectInteger(currentScan.getMassSpecId());
		user.selectInteger(currentScan.getUserId());
		disabled.setSelection(currentScan.isDisabled());
		description.setText(currentScan.getDescription());

		ArrayList<ScanFile> currentScanFiles = getCurrentScanFiles();
		ArrayList<ScanFile> scanFiles = new ArrayList<ScanFile>();

		if (currentScanFiles != null) {
			for (ScanFile scanFile : currentScanFiles) {
				scanFiles.add(new ScanFile(scanFile));
			}
		}

		scanFilesWidget.setScanFiles(getCurrentScan(), scanFiles);
		setRetrieveDropdown();
		loadScanList();
		loadCorrIntervalList();
	}

	@Override
	protected void setDefaultFieldValues() {
		getChainedPart().getPart().setLabel(Messages.editor_scanTab);

		id.setText(Messages.scanComposite_newId);
		massSpec.selectInteger(initialMassSpecId != null ? initialMassSpecId : DatabaseConstants.EMPTY_DB_ID);
		user.selectInteger(LoginInfoCache.getInstance().getUser().getId());
		disabled.setSelection(false);
		description.setText("");
		scanFilesWidget.setScanFiles(null, new ArrayList<ScanFile>());
		setRetrieveDropdown();
		loadScanList();
		loadCorrIntervalList();
	}

	@Override
	public void enableWidgets() {
		User currentUser = LoginInfoCache.getInstance().getUser();
		Permissions permissions = LoginInfoCache.getInstance().getPermissions();

		boolean canEditAllInput = permissions.isCanEditAllReplicates();
		boolean belongsToUser = getCurrentScan() == null ? true : getCurrentScan().getUserId() == currentUser.getId();
		boolean hasGeneralEditPermission = canEditAllInput || belongsToUser;

		if (!canEditAllInput) {
			user.revert();
		}

		user.setEnabled(canEditAllInput);

		if (!hasGeneralEditPermission) {
			massSpec.revert();
		}

		massSpec.setEnabled(hasGeneralEditPermission);

		if (!hasGeneralEditPermission) {
			disabled.revert();
		}

		disabled.setEnabled(hasGeneralEditPermission);

		if (!hasGeneralEditPermission) {
			description.revert();
		}

		description.setEnabled(hasGeneralEditPermission);

		if (!hasGeneralEditPermission) {
			scanFilesWidget.revert();
			setRetrieveDropdown();
			enableLoadPrecedingButtonIfAllowed();
			loadCorrIntervalList();
		}

		scanFilesWidget.setEnabled(hasGeneralEditPermission);
	}

	public boolean canAcceptFiles() {
		return scanFilesWidget.canAcceptFiles();
	}

	public void addFiles(String[] newFilenames) {
		scanFilesWidget.addFiles(newFilenames);
	}

	@Override
	public void disableWidgets() {
		user.setEnabled(false);
		massSpec.setEnabled(false);
		disabled.setEnabled(false);
		description.setEnabled(false);
		scanFilesWidget.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean fieldsContainNewData = false;

		fieldsContainNewData = fieldsContainNewData || user.hasChanged();
		fieldsContainNewData = fieldsContainNewData || massSpec.hasChanged();
		fieldsContainNewData = fieldsContainNewData || disabled.hasChanged();
		fieldsContainNewData = fieldsContainNewData || description.hasChanged();
		fieldsContainNewData = fieldsContainNewData || scanFilesWidget.hasChanged();

		return fieldsContainNewData;
	}

	@Override
	protected boolean hasError() {
		boolean shouldBeError = false;

		if (massSpec.getSelectionIndex() == -1) {
			massSpecError.setToolTipText(Messages.scanComposite_massSpecEmpty);
			shouldBeError = true;
		}

		if (massSpecError.getVisible() != shouldBeError) {
			massSpecError.setVisible(shouldBeError);
			layoutNeeded();
		}

		boolean hasError = massSpecError.isVisible() || scanFilesWidget.getScanFiles().size() == 0 || scanFilesWidget.hasError();

		return hasError;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String, Object> selection) {
		// only called once - so set these globally here

		initialScanId = (Integer) getSelection().get(ScanPart.SELECTION_INITIAL_SCAN_ID);
		initialMassSpecId = (Integer) getSelection().get(ScanPart.SELECTION_INITIAL_MASS_SPEC_ID);

		return true;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		if (initialScanId != null) {
			cancelWaitingFor(WAITING_FOR_SCAN);
			int commandId = InputCache.getInstance().scanGet(initialScanId, this);
			waitingFor(WAITING_FOR_SCAN, commandId);
		}

		return true;
	}

	@Override
	protected void requestSave(boolean isResend) {
		ScanV3 scan = new ScanV3();

		if (getCurrentScan() != null) {
			scan.setId(getCurrentScan().getId());
		}

		scan.setUserId(user.getSelectedInteger());

		long earliestDate = Long.MAX_VALUE;

		for (ScanFile scanFile : scanFilesWidget.getScanFiles()) {
			long thisDate = scanFile.getScanFileParsed().getDate();

			if (thisDate < earliestDate) {
				earliestDate = thisDate;
			}
		}

		scan.setDate(earliestDate);

		scan.setMassSpecId(massSpec.getSelectedInteger());
		scan.setDisabled(disabled.getSelection());
		scan.setDescription(description.getText());

		scanFilesWidget.fillInScan(scan);

		cancelWaitingFor(WAITING_FOR_SCAN_SAVE);
		int commandId = InputCache.getInstance().scanSave(scan, scanFilesWidget.getScanFiles(), this);
		waitingFor(WAITING_FOR_SCAN_SAVE, commandId);
	}

	@Override
	protected boolean canDelete() {
		ScanV3 currentScan = getCurrentScan();

		if (currentScan != null) {
			if (LoginInfoCache.getInstance().getPermissions().isCanDeleteAll()) {
				return true;
			}

			int currentUserId = LoginInfoCache.getInstance().getUser().getId();
			boolean canDeleteOwn = LoginInfoCache.getInstance().getPermissions().isCanDeleteOwn();

			if (canDeleteOwn && currentScan.getUserId() == currentUserId) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean requestDelete() {
		if (getChainedPart().raiseQuestion(Messages.replicateComposite_reallyDelete)) {
			int commandId = InputCache.getInstance().scanDelete(getCurrentScan().getId(), this);
			waitingFor(WAITING_FOR_SCAN_DELETE, commandId);
			return true;
		}

		return false;
	}

	private void loadCorrIntervalList() {
		currentCorrIntervalList = null;
		int massSpecId = massSpec.getSelectedInteger();

		if (massSpecId != DatabaseConstants.EMPTY_DB_ID) {
			cancelWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST);
			int commandId = CorrIntervalCache.getInstance().corrIntervalListGet(massSpecId, this);
			waitingFor(WAITING_FOR_CORR_INTERVAL_LIST, commandId);

			if (commandId == Command.UNDEFINED_ID) {
				setChannelToMZX10IfReady();
			}
		}
	}

	private void setChannelToMZX10IfReady() {
		if (currentCorrIntervalList != null && currentCorrIntervalList.getMassSpecId() == massSpec.getSelectedInteger()) {
			scanFilesWidget.setCorrIntervalList(currentCorrIntervalList);
		} else {
			scanFilesWidget.setCorrIntervalList(null);
		}
	}

	private void loadScanList() {
		currentScanList = null;
		int massSpecId = massSpec.getSelectedInteger();

		if (massSpecId != DatabaseConstants.EMPTY_DB_ID) {
			InputCache.getInstance().scanListGet(this, massSpecId);
		}

		enableLoadPrecedingButtonIfAllowed();
	}

	private void enableLoadPrecedingButtonIfAllowed() {
		if (massSpec.getSelectedInteger() == DatabaseConstants.EMPTY_DB_ID || currentScanList == null || scanFilesWidget.getScanFiles().size() == 0) {
			loadPrecedingButton.setEnabled(false);
			return;
		}

		long earliestScanFile = Long.MAX_VALUE;

		for (ScanFile scanFile : scanFilesWidget.getScanFiles()) {
			if (scanFile.getScanFileParsed().getDate() < earliestScanFile) {
				earliestScanFile = scanFile.getScanFileParsed().getDate();
			}
		}

		long precedingScanFileDate = Long.MIN_VALUE;
		precedingScanFileId = DatabaseConstants.EMPTY_DB_ID;

		for (Integer id : currentScanList.keySet()) {
			ScanListItem scanListItem = currentScanList.get(id);

			if (scanListItem.getDate() < earliestScanFile && scanListItem.getDate() > precedingScanFileDate) {
				precedingScanFileDate = scanListItem.getDate();
				precedingScanFileId = scanListItem.getScanId();
			}
		}

		loadPrecedingButton.setEnabled(precedingScanFileDate != Long.MIN_VALUE);
	}

	@Override
	public void scanListGetCompleted(int commandId, ScanList scanList) {
		if (scanList.getMassSpecId() == massSpec.getSelectedInteger()) {
			currentScanList = scanList;
			enableLoadPrecedingButtonIfAllowed();
		}
	}

	@Override
	public void scanListUpdated(int commandId, ScanList scanList) {
		if (scanList.getMassSpecId() == massSpec.getSelectedInteger()) {
			currentScanList = scanList;
			enableLoadPrecedingButtonIfAllowed();
		}
	}

	@Override
	public void scanListGetError(int commandId, String message) {
		raiseGetError(null, message);
	}

	@Override
	public void scanGetCompleted(int commandId, ScanV3 scan, ArrayList<ScanFile> scanFiles) {
		if (scan.getId() == precedingScanFileId) {
			scanFilesWidget.setScanData(scan);
			cancelWaitingFor(WAITING_FOR_PRECEDING_SCAN);
			widgetStatusChanged();

		} else if (commandIdForKey(WAITING_FOR_SCAN) == commandId) {
			newObject(WAITING_FOR_SCAN, new Object[] { scan, scanFiles });
		}
	}

	@Override
	public void scanUpdated(int commandId, ScanV3 scan, ArrayList<ScanFile> scanFiles) {
		if (getCurrentScan() != null && getCurrentScan().getId() == scan.getId()) {
			updateObject(new Object[] { scan, scanFiles }, Messages.scanComposite_scanHasBeenUpdated);
		}
	}

	@Override
	public void scanDeleted(int scanId) {
		if (getCurrentScan() != null && getCurrentScan().getId() == scanId) {
			getChainedPart().raiseInfo(Messages.scanComposite_scanDeleted);
			getChainedPart().closePart();
		}
	}

	@Override
	public void scanGetError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_PRECEDING_SCAN) == commandId) {
			raiseGetError(WAITING_FOR_PRECEDING_SCAN, message);

		} else if (commandIdForKey(WAITING_FOR_SCAN) == commandId) {
			raiseGetError(WAITING_FOR_SCAN, message);
		}
	}
 
	@Override
	public void scanSaveCompleted(int commandId) {
		if (commandIdForKey(WAITING_FOR_SCAN_SAVE) == commandId) {
			saveComplete(WAITING_FOR_SCAN_SAVE, null);
		}
	}

	@Override
	public void scanSaveError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_SCAN_SAVE) == commandId) {
			raiseSaveOrDeleteError(WAITING_FOR_SCAN_SAVE, message);
		}
	}

	@Override
	public void scanDeleteCompleted(int commandId) {
		if (commandIdForKey(WAITING_FOR_SCAN_DELETE) == commandId) {
			deleteComplete(WAITING_FOR_SCAN_DELETE);
		}
	}

	@Override
	public void scanDeleteError(int commandId, String message) {
		if (commandIdForKey(WAITING_FOR_SCAN_DELETE) == commandId) {
			raiseSaveOrDeleteError(WAITING_FOR_SCAN_DELETE, message);
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
			int loneMassSpec = (Integer) massSpecList.keySet().toArray()[0];
			massSpec.setSelectionButLeaveRevertValue(loneMassSpec);
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
	public void rawFileGetCompleted(final int commandId, final RawFile rawFile, final byte[] fileBytes) {
		doneWaitingFor(WAITING_FOR_RAW_FILE);
		retrieveRawFile(rawFile, fileBytes);
	}

	@Override
	public void rawFileGetError(final int commandId, final String message) {
		raiseGetError(WAITING_FOR_RAW_FILE, message);
	}

	@Override
	public void corrIntervalListGetCompleted(int commandId, CorrIntervalList corrIntervalList) {
		if (commandIdForKey(WAITING_FOR_CORR_INTERVAL_LIST) == commandId) {
			doneWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST);
			this.currentCorrIntervalList = corrIntervalList;
			setChannelToMZX10IfReady();
			widgetStatusChanged();
		}
	}

	@Override
	public void corrIntervalListUpdated(int commandId, CorrIntervalList corrIntervalList) {
		if (this.currentCorrIntervalList != null && this.currentCorrIntervalList.getMassSpecId() == corrIntervalList.getMassSpecId()) {
			this.currentCorrIntervalList = corrIntervalList;
			setChannelToMZX10IfReady();
		}
	}

	@Override
	public void corrIntervalListGetError(int commandId, String message) {
		raiseGetError(WAITING_FOR_CORR_INTERVAL_LIST, message);
	}
}
