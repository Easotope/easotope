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

package org.easotope.client.rawdata.batchimport;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.easotope.client.rawdata.batchimport.grouping.Grouping;
import org.easotope.client.rawdata.batchimport.loader.CorrIntervalListLoader;
import org.easotope.client.rawdata.batchimport.loader.MassSpecListLoader;
import org.easotope.client.rawdata.batchimport.loader.SourceListLoader;
import org.easotope.client.rawdata.batchimport.savedialog.SaveDialog;
import org.easotope.client.rawdata.batchimport.table.BatchImportTable;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecList;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
//import org.easotope.shared.rawdata.cache.input.InputCache;
//import org.easotope.shared.rawdata.cache.input.replicate.InputCacheReplicateSaveListener;
import org.easotope.shared.rawdata.cache.sourcelist.sourcelist.SourceList;
import org.easotope.shared.rawdata.cache.sourcelist.sourcelist.SourceListItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class BatchImportComposite extends EditorComposite {
	@SuppressWarnings("unused")
	private final String WAITING_FOR_REPLICATE_SAVE = "WAITING_FOR_REPLICATE_SAVE";

	private MassSpecListLoader massSpecListLoader;
	private CorrIntervalListLoader corrIntervalListLoader;
	private SourceListLoader sourceListLoader;
	private ThreadedFileReader threadedFileReader;

	private SortedCombo massSpec;
	private Canvas massSpecError;
	private VButton createCorrInterval;
	private Combo acidTemp;
	private SortedCombo grouping;
	@SuppressWarnings("unused")
	private Composite warningComposite;
	@SuppressWarnings("unused")
	private Label warningMessage;
	private Composite tableComposite;
	private Composite centerComposite;
	private BatchImportTable table;

	private TreeSet<ImportedFile> importedFiles = new TreeSet<ImportedFile>();
	private ArrayList<ImportedFile> waitingImportedFiles = new ArrayList<ImportedFile>();

	BatchImportComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		@SuppressWarnings("unused")
		final Image warningImage = Icons.getWarning(parent.getDisplay());
		final Image errorImage = Icons.getError(parent.getDisplay());

		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Composite controlComposite = new Composite(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		controlComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		controlComposite.setLayout(gridLayout);

		Composite massSpecComposite = new Composite(controlComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
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
		massSpecLabel.setText(Messages.batchImportComposite_massSpecLabel);

		massSpec = new SortedCombo(massSpecComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		massSpec.setLayoutData(gridData);
		massSpec.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
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

		createCorrInterval = new VButton(controlComposite, SWT.CHECK);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		createCorrInterval.setLayoutData(gridData);
		createCorrInterval.setText(Messages.batchImportComposite_createCorrIntLabel);
		createCorrInterval.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				widgetStatusChanged();
			}
		});
		
		Composite acidTempComposite = new Composite(controlComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		acidTempComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		acidTempComposite.setLayout(gridLayout);

		final Label acidTempLabel = new Label(acidTempComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		acidTempLabel.setLayoutData(gridData);
		acidTempLabel.setText(Messages.batchImportComposite_acidTempLabel);

		acidTemp = new Combo(acidTempComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.SHORT_COMBO_INPUT_WIDTH;
		acidTemp.setLayoutData(gridData);
		acidTemp.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		Composite groupingComposite = new Composite(controlComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
		groupingComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.horizontalSpacing = 0;
		groupingComposite.setLayout(gridLayout);

		final Label groupingLabel = new Label(groupingComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		groupingLabel.setLayoutData(gridData);
		groupingLabel.setText(Messages.batchImportComposite_groupingLabel);

		grouping = new SortedCombo(groupingComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		grouping.setLayoutData(gridData);
		grouping.setPossibilities(Grouping.groupingMenu);
		grouping.selectInteger(Grouping.groupingMenuDefaultInteger);
		grouping.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				Grouping.applyGroupingAlgorithm(grouping.getSelectedInteger(), importedFiles);
				table.refreshControls(importedFiles);
				widgetStatusChanged();
			}
		});

		tableComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(controlComposite, GuiConstants.INTER_WIDGET_GAP);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		tableComposite.setLayoutData(formData);

		tableComposite.setLayout(new StackLayout());

 		attachDropCode(this);

 		centerComposite = new Composite(tableComposite, SWT.NONE);
 		centerComposite.setLayout(new GridLayout());

 		Composite importComposite = new Composite(centerComposite, SWT.NONE);
 		importComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
 		importComposite.setLayout(new GridLayout());

 		Label message1Label = new Label(importComposite, SWT.NONE);
 		message1Label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		message1Label.setText(Messages.batchImportComposite_message1);
 
 		Label message2Label = new Label(importComposite, SWT.NONE);
 		message2Label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		message2Label.setText(Messages.batchImportComposite_message2);

 		Label message3Label = new Label(importComposite, SWT.NONE);
 		message3Label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		message3Label.setText(Messages.batchImportComposite_message3);

		table = new BatchImportTable(tableComposite, this, SWT.NONE);

		((StackLayout) tableComposite.getLayout()).topControl = centerComposite;
		tableComposite.layout();

		massSpecListLoader = new MassSpecListLoader(this);
		corrIntervalListLoader = new CorrIntervalListLoader(this);
		sourceListLoader = new SourceListLoader(this);
		
		threadedFileReader = new ThreadedFileReader(this);
		new Thread(threadedFileReader).start();
	}

	@Override
	protected void handleDispose() {
		massSpecListLoader.dispose();
		corrIntervalListLoader.dispose();
		sourceListLoader.dispose();
		threadedFileReader.dispose();
	}

	private void attachDropCode(Control control) {
 		int operations = DND.DROP_COPY | DND.DROP_DEFAULT;
 		DropTarget target = new DropTarget(control, operations);

 		final FileTransfer fileTransfer = FileTransfer.getInstance();
 		Transfer[] types = new Transfer[] { fileTransfer };
 		target.setTransfer(types);

 		target.addDropListener(new DropTargetListener() {
 			public void dragEnter(DropTargetEvent event) {
 				if (event.detail == DND.DROP_DEFAULT) {
 					if ((event.operations & DND.DROP_COPY) != 0) {
 						event.detail = DND.DROP_COPY;
 					} else {
 						event.detail = DND.DROP_NONE;
 					}
 				}
 
 				for (int i = 0; i < event.dataTypes.length; i++) {
 					if (fileTransfer.isSupportedType(event.dataTypes[i])){
 						event.currentDataType = event.dataTypes[i];

 						if (event.detail != DND.DROP_COPY) {
 							event.detail = DND.DROP_NONE;
 						}

 						break;
 					}
 				}
 			}

 			public void dragOver(DropTargetEvent event) {
 				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
 			}

 			public void dragOperationChanged(DropTargetEvent event) {
 				if (event.detail == DND.DROP_DEFAULT) {
 					if ((event.operations & DND.DROP_COPY) != 0) {
 						event.detail = DND.DROP_COPY;
 					} else {
 						event.detail = DND.DROP_NONE;
 					}
 				}

 				if (fileTransfer.isSupportedType(event.currentDataType)){
 					if (event.detail != DND.DROP_COPY) {
 						event.detail = DND.DROP_NONE;
 					}
 				}
 			}

 			public void dragLeave(DropTargetEvent event) {
 			}

 			public void dropAccept(DropTargetEvent event) {
 			}
 
 			public void drop(DropTargetEvent event) {
 				if (fileTransfer.isSupportedType(event.currentDataType)) {
 					String[] filenames = (String[]) event.data;
 					threadedFileReader.process(filenames);
  				}
 			}
 		});
	}

//	public void addFiles(String[] filenames) {
//		if (!widgetIsEnabled) {
//			//TODO make this more polite!
//			return;
//		}
//
//		int startingNumOfAcquisitions = importedFiles.size();
//
//		threadedFileReader.process(filenames);
//
//		if (importedFiles.size() - startingNumOfAcquisitions != 0) {
//			table.setImportedFiles(importedFiles);
//
//			((StackLayout) tableComposite.getLayout()).topControl = table;
//			tableComposite.layout();
//
//			widgetStatusChanged();
//		}
//	}

	@Override
	protected void setCurrentFieldValues() {
		setDefaultFieldValues();
	}

	@Override
	protected void setDefaultFieldValues() {
		getChainedPart().getPart().setLabel(Messages.editor_bulkImportTab);

		massSpec.selectInteger(-1);
		createCorrInterval.setSelection(false);
		acidTemp.select(-1);
		grouping.revert();
		importedFiles.clear();
		
		((StackLayout) tableComposite.getLayout()).topControl = centerComposite;
		tableComposite.layout();
	}

	@Override
	public void enableWidgets() {
		massSpec.setEnabled(true);
		createCorrInterval.setEnabled(true);
		acidTemp.setEnabled(true);
		grouping.setEnabled(true);
		table.setEnabled(true);		// TODO individually enable table widgets
	}

	@Override
	public void disableWidgets() {
		massSpec.setEnabled(false);
		createCorrInterval.setEnabled(false);
		acidTemp.setEnabled(false);
		grouping.setEnabled(false);
		table.setEnabled(false);	// TODO individually disable table widgets
	}

	@Override
	protected boolean isDirty() {
		boolean fieldsContainNewData = false;

		fieldsContainNewData = fieldsContainNewData || massSpec.hasChanged();
		fieldsContainNewData = fieldsContainNewData || createCorrInterval.hasChanged();
		fieldsContainNewData = fieldsContainNewData || acidTemp.getSelectionIndex() != -1;
		fieldsContainNewData = fieldsContainNewData || grouping.hasChanged();
		fieldsContainNewData = fieldsContainNewData || importedFiles.size() != 0;

		return fieldsContainNewData;
	}

	@Override
	protected boolean hasError() {
		int currentGrouping = Grouping.getCurrentGrouping(grouping.getSelectedInteger(), importedFiles);
		grouping.setSelectionButLeaveRevertValue(currentGrouping);

		boolean massSpecErrorIsSet = false;

		if (massSpec.getSelectedInteger() == -1) {
			massSpecError.setToolTipText(Messages.batchImportComposite_noMassSpecError);

			if (!massSpecError.getVisible()) {
				layoutNeeded();
			}

			massSpecErrorIsSet = true;
		}

		massSpecError.setVisible(massSpecErrorIsSet);

		//import is missing input needed by 
		//acid temp not supported by sample type
		//sample type has no default acid temp

		return massSpecErrorIsSet;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		return false;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		return true;
	}

	@Override
	protected void requestSave(boolean isResend) {
		new SaveDialog(getShell()).open(importedFiles, true); //TODO fix allowDuplicates 
	}

	@Override
	protected boolean canDelete() {
		return false;
	}

	@Override
	protected boolean requestDelete() {
		return false;
	}

	public void newMassSpecList(MassSpecList massSpecList) {
		massSpec.setPossibilities(massSpecList);

		if (massSpec.getSelectedInteger() == DatabaseConstants.EMPTY_DB_ID && massSpecList.size() == 1) {
			int loneMassSpecId = (Integer) massSpecList.keySet().toArray()[0];
			massSpec.setSelectionButLeaveRevertValue(loneMassSpecId);
			corrIntervalListLoader.setMassSpecId(loneMassSpecId);
		}

		widgetStatusChanged();
	}

	@SuppressWarnings("unused")
	public void newSourceList(SourceList sourceList) {
		for (SourceListItem item : sourceList) {

		}

		threadedFileReader.setSourceList(sourceList);
	}

	public void newCorrIntervalList(CorrIntervalList corrIntervalList) {
		// TODO Auto-generated method stub
		
	}

	public void addImportedFile(ImportedFile importedFile) {
		if (isDisposed()) {
			return;
		}

		synchronized (waitingImportedFiles) {
			waitingImportedFiles.add(importedFile);
		}

		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				synchronized (waitingImportedFiles) {
					if (isDisposed() || waitingImportedFiles.size() == 0) {
						return;
					}

					importedFiles.addAll(waitingImportedFiles);
					waitingImportedFiles.clear();
				}

				table.setImportedFiles(importedFiles);
				((StackLayout) tableComposite.getLayout()).topControl = table;
				tableComposite.layout();

				widgetStatusChanged();
			}
		});
	}
}
