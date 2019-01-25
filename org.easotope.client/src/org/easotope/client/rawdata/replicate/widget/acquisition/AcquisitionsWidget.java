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

package org.easotope.client.rawdata.replicate.widget.acquisition;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.easotope.client.Messages;
import org.easotope.client.core.adaptors.LoggingKeyAdaptor;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.DateTimeLabel;
import org.easotope.client.rawdata.util.FileReader;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListItem;
import org.easotope.shared.core.DoubleTools;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.math.Statistics;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.InputParameterType;
import org.easotope.shared.rawdata.compute.ComputeAcquisitionParsed;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class AcquisitionsWidget extends EasotopeComposite {
	private static HashMap<Integer,InputParameter> mzX10ToRefVoltageInputParameter = new HashMap<Integer,InputParameter>();
	private static HashMap<Integer,InputParameter> mzX10ToSampleVoltageInputParameter = new HashMap<Integer,InputParameter>();
	private static HashSet<Integer> validMZX10s = new HashSet<Integer>();

	public enum ButtonType {
		DisableAcquisition,
		DisableCycle,
		OffPeakCycle
	}

	private final int NUM_COLUMNS_TO_SKIP = 3;

	private final String ACQUISITION = "ACQUISITION";
	private final String CYCLE = "CYCLE";
	private final String CYCLE_DISABLE_BUTTONS = "CYCLE_DISABLE_BUTTONS";
	private final String CYCLE_OFF_PEAK_BUTTONS = "CYCLE_OFF_PEAK_BUTTONS";
	private final String ACQUISITION_DISABLE_BUTTON = "ACQUISITION_DISABLE_BUTTON";
	private final String DOWNLOAD_BUTTON = "DOWNLOAD_BUTTON";
	private final String DATE_LABEL = "DATE_LABEL";
	private final String TABLE = "TABLE";
	private final String TABLE_NUM_COLUMNS = "TABLE_NUM_COLUMNS";
	private final String TABLE_EDITORS = "TABLE_EDITORS";
	private final String STATISTICS_LINE = "STATISTICS_LINE";

	private ReplicateV1 originalReplicate = null;
	private TreeSet<Acquisition> originalAcquisitions = new TreeSet<Acquisition>();
	private TreeSet<Acquisition> acquisitions = new TreeSet<Acquisition>();
	private ArrayList<InputParameter> columnTitles = null;
	private boolean widgetIsEnabled = true;
	private CorrIntervalList currentCorrIntervalList = null;

	private Composite centerComposite;
	private TabFolder tabFolder;

	private Vector<AcquisitionsWidgetListener> acquisitionsWidgetListeners = new Vector<AcquisitionsWidgetListener>();

	public AcquisitionsWidget(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
 		setLayout(new StackLayout());
 
 		attachDropCode(this);

 		centerComposite = new Composite(this, SWT.NONE);
 		centerComposite.setLayout(new GridLayout());

 		Composite importComposite = new Composite(centerComposite, SWT.NONE);
 		importComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
 		importComposite.setLayout(new GridLayout());

 		Label message1Label = new Label(importComposite, SWT.NONE);
 		message1Label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		message1Label.setText(Messages.acquisitionsWidget_message1);
 
 		Label message2Label = new Label(importComposite, SWT.NONE);
 		message2Label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		message2Label.setText(Messages.acquisitionsWidget_message2);

 		Label message3Label = new Label(importComposite, SWT.NONE);
 		message3Label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		message3Label.setText(Messages.acquisitionsWidget_message3);

 		tabFolder = new TabFolder(this, SWT.BORDER);

 		((StackLayout) getLayout()).topControl = centerComposite;
 		layout();
 		
		setVisible(false);
 	}

	@Override
	protected void handleDispose() {
		
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
 					addFiles(filenames);
  				}
 			}
 		});
	}

	public boolean canAcceptFiles() {
		return widgetIsEnabled;
	}

	public void addFiles(String[] filenames) {
		if (!widgetIsEnabled) {
			//TODO make this more polite!
			return;
		}

		int startingNumOfAcquisitions = acquisitions.size();

		for (String filename : filenames) {
			addFile(filename);
		}

		if (acquisitions.size() - startingNumOfAcquisitions != 0) {
			refreshTables();
			notifyAcquisitionChanged();
		}
	}

	private void addFile(String filename) {
		// TODO make this more robust
		if (filename.endsWith(".scn")) {
			MessageDialog.openError(getParent().getShell(), Messages.acquisitionsWidget_fileAddErrorTitle, Messages.acquisitionsWidget_fileWrongType);
			return;
		}
		
		File file = new File(filename);
		byte[] fileBytes = null;

		try {
			fileBytes = FileReader.getBytesFromFile(filename);
		} catch (Exception e) {
			MessageDialog.openError(getParent().getShell(), Messages.acquisitionsWidget_fileAddErrorTitle, e.getMessage());
			return;
		}

		RawFile rawFile = new RawFile();
		rawFile.setOriginalName(file.getName());

		ComputeAcquisitionParsed computeAcquisitionParsed = null;

		try {
			computeAcquisitionParsed = new ComputeAcquisitionParsed(rawFile, fileBytes, false, null);
		} catch (RuntimeException e) {
			MessageDialog.openError(getParent().getShell(), Messages.acquisitionsWidget_fileAddErrorTitle, e.getMessage());
			return;
		}

		if (computeAcquisitionParsed.getAssumedTimeZone() != null) {
			String message = MessageFormat.format(Messages.acquisitionsWidget_assumedTimeZone, file.getName(), computeAcquisitionParsed.getAssumedTimeZone());
			MessageDialog.openError(getParent().getShell(), Messages.acquisitionsWidget_assumedTimeZoneTitle, message);
		}

		int acquisitionNumber = 0;
		for (AcquisitionParsedV2 acquisitionParsed : computeAcquisitionParsed.getMaps()) {
			AcquisitionInputV0 acquisitionInput = new AcquisitionInputV0();
			acquisitionInput.setDisabledCycles(new boolean[acquisitionParsed.getNumCycles()]);
			acquisitionInput.setOffPeakCycles(new boolean[acquisitionParsed.getNumCycles()]);
			acquisitionInput.setAssumedTimeZone(computeAcquisitionParsed.getAssumedTimeZone());
			acquisitionInput.setAcquisitionNumber(acquisitionNumber++);

			Acquisition acquisition = new Acquisition(fileBytes, rawFile, acquisitionParsed, acquisitionInput);

			if (!acquisitions.isEmpty() && !acquisition.isCompatibleWith(acquisitions.first())) {
				String error = MessageFormat.format(Messages.acquisitionsWidget_dataMismatchReplicateText, new Object[] { new File(filename).getName() });
				MessageDialog.openError(getParent().getShell(), Messages.acquisitionsWidget_fileAddErrorTitle, error);
				return;
			}

			acquisitions.add(acquisition);
	
			HashMap<InputParameter,Double[]> remappedMeasurements = remapMeasurements(acquisition.getAcquisitionParsed().getMeasurements());
			OffPeakCalculator offPeakCalculator = new OffPeakCalculator(remappedMeasurements);
			boolean[] offPeakCycles = acquisition.getAcquisitionInput().getOffPeakCycles();
	
			for (Integer cycleIndex : offPeakCalculator.getOffPeakCycles()) {
				offPeakCycles[cycleIndex] = true;
			}
		}
	}

	public ArrayList<Acquisition> getAcquisitions() {
		if (getVisible()) {
			return new ArrayList<Acquisition>(acquisitions);
		} else {
			return new ArrayList<Acquisition>();
		}
	}

	public TreeSet<Acquisition> getAcquisitionsAsTreeSet() {
		return acquisitions;
	}

	public void setAcquisitions(ArrayList<Acquisition> acquisitions) {
		originalAcquisitions = acquisitions == null ? new TreeSet<Acquisition>() : new TreeSet<Acquisition>(acquisitions);
		revert();
	}

	public void revert() {
		if (!hasChanged()) {
			return;
		}

		disposeOfTabItems();

		columnTitles = null;
		acquisitions.clear();

		for (Acquisition acquisition : originalAcquisitions) {
			acquisitions.add(new Acquisition(acquisition));
		}

		refreshTables();
		notifyAcquisitionChanged();
	}

	public boolean hasChanged() {
		if (originalAcquisitions.size() != acquisitions.size()) {
			return true;
		}

		Iterator<Acquisition> originalIterator = originalAcquisitions.iterator();
		Iterator<Acquisition> iterator = acquisitions.iterator();

		while (originalIterator.hasNext()) {
			Acquisition originalAcquisition = originalIterator.next();
			Acquisition acquisition = iterator.next();

			if (!originalAcquisition.equals(acquisition)) {
				return true;
			}
		}

		return false;
	}

	public void refreshTables() {
		if (currentCorrIntervalList == null) {
			setVisible(false);
			return;
		}

		setVisible(true);

		if (acquisitions.size() == 0) {
			((StackLayout) getLayout()).topControl = centerComposite;
			layout();
			return;
		}

		Acquisition selectedAcquisition = null;

		if (tabFolder.getSelectionIndex() != -1) {
			TabItem selectedTab = tabFolder.getItem(tabFolder.getSelectionIndex());
			selectedAcquisition = (Acquisition) selectedTab.getData(ACQUISITION);
		}

		boolean columnTitlesAreNew = false;

		if (columnTitles == null) {
			columnTitles = new ArrayList<InputParameter>();
			HashMap<InputParameter,Double[]> remappedMeasurements = remapMeasurements(acquisitions.first().getAcquisitionParsed().getMeasurements());

			for (InputParameter parameter : InputParameter.values()) {
				if (remappedMeasurements.get(parameter) != null) {
					columnTitles.add(parameter);
				}
			}
			columnTitlesAreNew = true;
		}

		while (acquisitions.size() != tabFolder.getItemCount()) {
	 		final TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
	 		Composite itemComposite = new Composite(tabFolder, SWT.NONE);
	 		FormLayout formLayout = new FormLayout();
	 		formLayout.marginBottom = 5;
	 		formLayout.marginWidth = 5;
	 		itemComposite.setLayout(formLayout);
	 		tabItem.setControl(itemComposite);
	 		tabItem.setText(String.valueOf(tabFolder.getItemCount()));
	 		tabItem.setData(CYCLE_DISABLE_BUTTONS, new ArrayList<Button>());
	 		tabItem.setData(CYCLE_OFF_PEAK_BUTTONS, new ArrayList<Button>());
	 		
			final int horizontalIndent = 15;
	
	 		Composite controlComposite = new Composite(itemComposite, SWT.NONE);
			FormData formData = new FormData();
			formData.top = new FormAttachment(0);
			formData.left = new FormAttachment(0);
			controlComposite.setLayoutData(formData);
	 		GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 9;
			gridLayout.marginHeight = 0;
			gridLayout.marginWidth = 0;
			gridLayout.horizontalSpacing = 0;
	 		controlComposite.setLayout(gridLayout);

	 		DateTimeLabel date = new DateTimeLabel(controlComposite, SWT.NONE);
			GridData gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			date.setLayoutData(gridData);
			date.setWithSeconds(true);
			tabItem.setData(DATE_LABEL, date);
			
	 		final Button disable = new Button(controlComposite, SWT.CHECK);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			gridData.horizontalIndent = horizontalIndent;
			disable.setLayoutData(gridData);
			disable.setText(Messages.acquisitionsWidget_disabledAcquisition);
			disable.addSelectionListener(new LoggingSelectionAdaptor() {
				public void loggingWidgetSelected(SelectionEvent e) {
					TabItem tabItem = tabFolder.getItem(tabFolder.getSelectionIndex());
					Acquisition acquisition = (Acquisition) tabItem.getData(ACQUISITION);
					acquisition.getAcquisitionInput().setDisabled(disable.getSelection());
					notifyButtonsChanged(ButtonType.DisableAcquisition, acquisition.getAcquisitionParsed().getDate(), -1, disable.getSelection());
				}
			});
			tabItem.setData(ACQUISITION_DISABLE_BUTTON, disable);
	
			Button getDownloadButton = new Button(itemComposite, SWT.PUSH);
			formData = new FormData();
			formData.top = new FormAttachment(0);
			formData.right = new FormAttachment(100);
			getDownloadButton.setLayoutData(formData);
			getDownloadButton.setText(Messages.acquisitionsWidget_download);
			getDownloadButton.addSelectionListener(new LoggingSelectionAdaptor() {
				public void loggingWidgetSelected(SelectionEvent e) {
					downloadRawFile();
				}
			});
			tabItem.setData(DOWNLOAD_BUTTON, getDownloadButton);

			final Table table = new Table(itemComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
			formData = new FormData();
			formData.top = new FormAttachment(getDownloadButton);
			formData.bottom = new FormAttachment(100);
			formData.left = new FormAttachment(0);
			formData.right = new FormAttachment(100);
			table.setLayoutData(formData);
			table.setLinesVisible(true);
			table.setHeaderVisible(true);
			table.setData(TABLE_EDITORS, new ArrayList<TableEditor>());
			table.addKeyListener(new LoggingKeyAdaptor() {
				public void loggingKeyPressed(KeyEvent e) {
					if (((e.stateMask & SWT.MOD1) == SWT.MOD1) && (e.keyCode == 'a')) {
						table.selectAll();
						
					} else if (((e.stateMask & SWT.MOD1) == SWT.MOD1) && (e.keyCode == 'c')) {	
						int[] indices = table.getSelectionIndices();
						
						if (indices.length != 0) {
							Arrays.sort(indices);

							StringBuffer result = new StringBuffer("<table>");

							for (int i : indices) {
								TableItem tableItem = table.getItem(i);
								int tableNumColumns = (Integer) table.getData(TABLE_NUM_COLUMNS);

								result.append("<tr>");

								for (int column = 3; column<tableNumColumns+1; column++) {
									result.append("<td>");
									String text = DoubleTools.removeLeadingExponent(tableItem.getText(column));
									result.append(text == null ? "" : text);
									result.append("</td>");
								}

								result.append("</tr>");
							}

							result.append("</table>");
							
							Clipboard clipboard = new Clipboard(table.getDisplay());
							clipboard.setContents(new Object[] { result.toString() }, new Transfer[] { HTMLTransfer.getInstance() });
						}
					}
				}

				public void loggingKeyReleased(KeyEvent e) {
					// do nothing
				}
			});
			tabItem.setData(TABLE, table);
			table.setData(TABLE_NUM_COLUMNS, new Integer(columnTitles.size() + 2));

			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(60);

			column = new TableColumn(table, SWT.NONE);
			column.setWidth(55);
			column.setText(Messages.acquisitionsWidget_disableColumn);

			column = new TableColumn(table, SWT.NONE);
			column.setWidth(55);
			column.setText(Messages.acquisitionsWidget_offPeakColumn);

			for (InputParameter parameter : columnTitles) {
				column = new TableColumn(table, SWT.NONE);
				column.setWidth(75);
				column.setText(parameter.toString());
			}
		}

		if (columnTitlesAreNew) {
			for (TabItem tabItem : tabFolder.getItems()) {
				Table table = (Table) tabItem.getData(TABLE);
				table.setRedraw(false);

				while (table.getColumnCount() != NUM_COLUMNS_TO_SKIP) {
					TableColumn[] columns = table.getColumns();
					columns[columns.length-1].dispose();
				}

				for (InputParameter parameter : columnTitles) {
					TableColumn column = new TableColumn(table, SWT.NONE);
					column.setWidth(75);
					column.setText(parameter.toString());
				}

				table.setRedraw(true);
				table.setData(TABLE_NUM_COLUMNS, new Integer(columnTitles.size() + 2));
			}
		}

		int tab=0;
		int newSelectedTab=0;

		for (Acquisition acquisition : acquisitions) {
			final TabItem tabItem = tabFolder.getItem(tab);

			if (acquisition == selectedAcquisition) {
				newSelectedTab = tab;
			}

			Acquisition alreadySetForAcq = (Acquisition) tabItem.getData(ACQUISITION);

			if (columnTitlesAreNew || acquisition != alreadySetForAcq) {
				tabItem.setData(ACQUISITION, acquisition);

				DateTimeLabel dateTimeLabel = (DateTimeLabel) tabItem.getData(DATE_LABEL);
				dateTimeLabel.setDate(acquisition.getAcquisitionParsed().getDate());
				
				disposeOfTabItemContents(tabItem);

				@SuppressWarnings("unchecked")
				ArrayList<Button> disableButtons = (ArrayList<Button>) tabItem.getData(CYCLE_DISABLE_BUTTONS);
				disableButtons.clear();

				@SuppressWarnings("unchecked")
				ArrayList<Button> offPeakButtons = (ArrayList<Button>) tabItem.getData(CYCLE_OFF_PEAK_BUTTONS);
				offPeakButtons.clear();

				Table table = (Table) tabItem.getData(TABLE);

				Preferences preferences = LoginInfoCache.getInstance().getPreferences();
				boolean leadingExponent = preferences.getLeadingExponent();
				boolean forceExponent = preferences.getForceExponent();

				for (int i=0; i<acquisition.getAcquisitionInput().getDisabledCycles().length; i++) {
					final TableItem tableItem = new TableItem(table, SWT.NONE);

					int currentColumn = NUM_COLUMNS_TO_SKIP;
					HashMap<InputParameter,Double[]> remappedMeasurements = remapMeasurements(acquisition.getAcquisitionParsed().getMeasurements());

					for (InputParameter parameter : columnTitles) {
						Double[] array = remappedMeasurements.get(parameter);

						if (array != null) {
							Double value = array[i];
							tableItem.setText(currentColumn, (value != null) ? DoubleTools.format(value, leadingExponent, forceExponent) : "");
						}

						currentColumn++;
					}

					tableItem.setText(0, Messages.acquisitionsWidget_cycle + " " + (i+1));

					final Button disableCycleButton = new Button(table, SWT.CHECK);
					disableCycleButton.setData(ACQUISITION, acquisition);
					disableCycleButton.setData(CYCLE, i);
					disableCycleButton.addSelectionListener(new LoggingSelectionAdaptor() {
						public void loggingWidgetSelected(SelectionEvent e) {
							Acquisition acquisition = (Acquisition) disableCycleButton.getData(ACQUISITION);
							int cycleNumber = (Integer) disableCycleButton.getData(CYCLE);
							acquisition.getAcquisitionInput().getDisabledCycles()[cycleNumber] = disableCycleButton.getSelection();
							refreshStatistics(tabItem);
							notifyButtonsChanged(ButtonType.DisableCycle, acquisition.getAcquisitionParsed().getDate(), cycleNumber, disableCycleButton.getSelection());
						}
					});
					disableCycleButton.setSelection(acquisition.getAcquisitionInput().getDisabledCycles()[i]);
					disableCycleButton.pack();
					disableButtons.add(disableCycleButton);

					TableEditor editor = new TableEditor(table);
					editor.minimumWidth = disableCycleButton.getSize().x;
					editor.horizontalAlignment = SWT.CENTER;
					editor.setEditor(disableCycleButton, tableItem, 1);

					@SuppressWarnings("unchecked")
					ArrayList<TableEditor> tableEditors = (ArrayList<TableEditor>) table.getData(TABLE_EDITORS);
					tableEditors.add(editor);

					final Button offPeakCycleButton = new Button(table, SWT.CHECK);
					offPeakCycleButton.setData(ACQUISITION, acquisition);
					offPeakCycleButton.setData(CYCLE, i);
					offPeakCycleButton.addSelectionListener(new LoggingSelectionAdaptor() {
						public void loggingWidgetSelected(SelectionEvent e) {
							Acquisition acquisition = (Acquisition) offPeakCycleButton.getData(ACQUISITION);
							int cycleNumber = (Integer) offPeakCycleButton.getData(CYCLE);
							acquisition.getAcquisitionInput().getOffPeakCycles()[cycleNumber] = offPeakCycleButton.getSelection();
							refreshStatistics(tabItem);
							notifyButtonsChanged(ButtonType.OffPeakCycle, acquisition.getAcquisitionParsed().getDate(), cycleNumber, offPeakCycleButton.getSelection());
						}
					});
					boolean[] offPeakCycles = acquisition.getAcquisitionInput().getOffPeakCycles();
					offPeakCycleButton.setSelection(offPeakCycles != null && offPeakCycles[i] == true);
					offPeakCycleButton.pack();
					offPeakButtons.add(offPeakCycleButton);

					editor = new TableEditor(table);
					editor.minimumWidth = offPeakCycleButton.getSize().x;
					editor.horizontalAlignment = SWT.CENTER;
					editor.setEditor(offPeakCycleButton, tableItem, 2);

					tableEditors.add(editor);
				}

				new TableItem(table, SWT.NONE);
				tabItem.setData(STATISTICS_LINE, new Integer(table.getItemCount()));
				new TableItem(table, SWT.NONE);
				new TableItem(table, SWT.NONE);
				new TableItem(table, SWT.NONE);

				refreshStatistics(tabItem);

				((Composite) tabItem.getControl()).layout();
			}

			tab++;
		}

		refreshButtons();
		tabFolder.setSelection(newSelectedTab);
		tabFolder.pack(true);
		tabFolder.redraw();

		((StackLayout) getLayout()).topControl = tabFolder;
 		layout();
	}
 
	private HashMap<InputParameter,Double[]> remapMeasurements(HashMap<InputParameter,Double[]> measurements) {
		HashMap<InputParameter,Double[]> remappedMeasurements = new HashMap<InputParameter,Double[]>();
		Integer[] channelToMZX10 = getChannelToMZX10();

		if (channelToMZX10 == null) {
			return remappedMeasurements;
		}

		for (InputParameter inputParameter : measurements.keySet()) {
			int channel;

			if (inputParameter.getInputParameterType() == InputParameterType.RefMeasurement) {
				channel = inputParameter.ordinal() - InputParameter.Channel0_Ref.ordinal();
			} else {
				channel = inputParameter.ordinal() - InputParameter.Channel0_Sample.ordinal();
			}

			if (channelToMZX10.length > channel && channelToMZX10[channel] != null) {
				InputParameter remappedInputParameter = getInputParameter(channelToMZX10[channel], inputParameter.getInputParameterType());
				Double[] value = measurements.get(inputParameter);

				remappedMeasurements.put(remappedInputParameter, value);
			}
		}

		return remappedMeasurements;
	}

	private Integer[] getChannelToMZX10() {
		if (currentCorrIntervalList == null) {
			return null;
		}

		if (originalReplicate != null && originalReplicate.getMassSpecId() == this.currentCorrIntervalList.getMassSpecId()) {
			return originalReplicate.getChannelToMzX10();
		}

		if (acquisitions.size() == 0) {
			return null;
		}

		CorrIntervalListItem corrIntervalListItem = null;

		ArrayList<CorrIntervalListItem> sorted = new ArrayList<CorrIntervalListItem>(currentCorrIntervalList.values());
		Collections.sort(sorted, new CorrIntervalListItemComparator());

		Acquisition firstAcquisition = acquisitions.first();
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

	private InputParameter getInputParameter(int mzX10, InputParameterType inputParameterType) {
		if (inputParameterType == InputParameterType.RefMeasurement) {
			return mzX10ToRefVoltageInputParameter.get(mzX10);
		} else {
			return mzX10ToSampleVoltageInputParameter.get(mzX10);
		}
	}

	private void refreshStatistics(TabItem tabItem) {
		Acquisition acquisition = (Acquisition) tabItem.getData(ACQUISITION);
		Table table = (Table) tabItem.getData(TABLE);
		int statisticsLine = (Integer) tabItem.getData(STATISTICS_LINE);

		Statistics[] statistics = new Statistics[columnTitles.size()];
		for (int i=0; i<statistics.length; i++) {
			statistics[i] = new Statistics();
		}

		boolean[] disabledCycles = acquisition.getAcquisitionInput().getDisabledCycles();
		boolean[] offPeakCycles = acquisition.getAcquisitionInput().getOffPeakCycles();

		for (int cycle=0; cycle<disabledCycles.length; cycle++) {
			if (!disabledCycles[cycle] && (offPeakCycles == null || !offPeakCycles[cycle])) {
				int column = 0;
				for (InputParameter parameter : columnTitles) {
					HashMap<InputParameter,Double[]> remappedMeasurements = remapMeasurements(acquisition.getAcquisitionParsed().getMeasurements());
					Double[] values = remappedMeasurements.get(parameter);
					Double value = values[cycle];

					if (value != null) {
						statistics[column].addNumber(value);
					}
					
					column++;
				}
			}
		}

		Preferences preferences = LoginInfoCache.getInstance().getPreferences();
		boolean leadingExponent = preferences.getLeadingExponent();
		boolean forceExponent = preferences.getForceExponent();

		boolean oneMeanSet = false;
		TableItem mean = table.getItem(statisticsLine);

		for (int i=0; i<statistics.length; i++) {
			if (statistics[i].getSampleSize() > 1) {
				mean.setText(i+NUM_COLUMNS_TO_SKIP, DoubleTools.format(statistics[i].getMean(), leadingExponent, forceExponent));
				oneMeanSet = true;
			} else {
				mean.setText(i+NUM_COLUMNS_TO_SKIP, "");
			}
		}
		
		mean.setText(0, oneMeanSet ? Messages.rawResultsWidget_mean : "");
		
		boolean oneStdDev = false;
		TableItem stdDev = table.getItem(statisticsLine+1);

		for (int i=0; i<statistics.length; i++) {
			if (statistics[i].getSampleSize() > 1) {
				stdDev.setText(i+NUM_COLUMNS_TO_SKIP, DoubleTools.format(statistics[i].getStandardDeviationSample(), leadingExponent, forceExponent));
				oneStdDev = true;
			} else {
				stdDev.setText(i+NUM_COLUMNS_TO_SKIP, "");
			}
		}
		
		stdDev.setText(0, oneStdDev ? Messages.rawResultsWidget_standardDeviation : "");
		
		boolean oneStdErr = false;
		TableItem stdErr = table.getItem(statisticsLine+2);

		for (int i=0; i<statistics.length; i++) {
			if (statistics[i].getSampleSize() > 1) {
				stdErr.setText(i+NUM_COLUMNS_TO_SKIP, DoubleTools.format(statistics[i].getStandardErrorSample(), leadingExponent, forceExponent));
				oneStdErr = true;
			} else {
				stdErr.setText(i+NUM_COLUMNS_TO_SKIP, "");
			}
		}
		
		stdErr.setText(0, oneStdErr ? Messages.rawResultsWidget_standardError : "");
	}

	public void refreshButtons() {
		for (TabItem tabItem : tabFolder.getItems()) {
			Acquisition acquisition = (Acquisition) tabItem.getData(ACQUISITION);

			Button acqDisableButton = (Button) tabItem.getData(ACQUISITION_DISABLE_BUTTON);
			acqDisableButton.setSelection(acquisition.getAcquisitionInput().isDisabled());

			@SuppressWarnings("unchecked")
			ArrayList<Button> disableButtons = (ArrayList<Button>) tabItem.getData(CYCLE_DISABLE_BUTTONS);

			for (int i=0; i<disableButtons.size(); i++) {
				Button button = disableButtons.get(i);

				if (button != null) {
					button.setSelection(acquisition.getAcquisitionInput().getDisabledCycles()[i]);
				}
			}

			@SuppressWarnings("unchecked")
			ArrayList<Button> offPeakButtons = (ArrayList<Button>) tabItem.getData(CYCLE_OFF_PEAK_BUTTONS);
			boolean[] offPeakCycles = acquisition.getAcquisitionInput().getOffPeakCycles();

			for (int i=0; i<offPeakButtons.size(); i++) {
				Button button = offPeakButtons.get(i);

				if (button != null) {
					button.setSelection(offPeakCycles != null ? offPeakCycles[i] : false);
				}
			}
		}
	}

	private void downloadRawFile() {
		TabItem tabItem = tabFolder.getItem(tabFolder.getSelectionIndex());
		Acquisition acquisition = (Acquisition) tabItem.getData(ACQUISITION);

		RawFile rawFile = acquisition.getRawFile();
		byte[] fileBytes = acquisition.getFileBytes();

		if (rawFile == null || fileBytes == null) {
			this.notifyRetrieveRawFile(acquisition.getAcquisitionInput().getRawFileId());
		} else {
			this.notifyRetrieveRawFile(rawFile, fileBytes);
		}
	}

	public void addAcquisitionChangedListener(AcquisitionsWidgetListener acquisitionChangedListener) {
		acquisitionsWidgetListeners.add(acquisitionChangedListener);
	}
	
	public void removeAcquisitionChangedListener(AcquisitionsWidgetListener acquisitionChangedListener) {
		acquisitionsWidgetListeners.remove(acquisitionChangedListener);
	}

	private void notifyAcquisitionChanged() {
		for (AcquisitionsWidgetListener acquisitionWidgetListener : acquisitionsWidgetListeners) {
			acquisitionWidgetListener.acquisitionsChanged(acquisitions);
		}
	}

	private void notifyButtonsChanged(ButtonType buttonType, long acquisitionTimeStamp, int cycleNumber, boolean selected) {
		for (AcquisitionsWidgetListener acquisitionWidgetListener : acquisitionsWidgetListeners) {
			acquisitionWidgetListener.acquisitionButtonsChanged(buttonType, acquisitionTimeStamp, cycleNumber, selected);
		}
	}

	private void notifyRetrieveRawFile(int rawFileId) {
		for (AcquisitionsWidgetListener acquisitionChangedListener : acquisitionsWidgetListeners) {
			acquisitionChangedListener.retrieveRawFile(rawFileId);
		}
	}

	private void notifyRetrieveRawFile(RawFile rawFile, byte[] fileBytes) {
		for (AcquisitionsWidgetListener acquisitionChangedListener : acquisitionsWidgetListeners) {
			acquisitionChangedListener.retrieveRawFile(rawFile, fileBytes);
		}
	}

	private void disposeOfTabItems() {
		for (TabItem tabItem : tabFolder.getItems()) {
			disposeOfTabItemContents(tabItem);
			
			if (!tabItem.isDisposed()) {
				tabItem.dispose();
			}
		}
	}

	private void disposeOfTabItemContents(TabItem tabItem) {
		Table table = (Table) tabItem.getData(TABLE);
		table.removeAll();

		@SuppressWarnings("unchecked")
		ArrayList<TableEditor> tableEditors = (ArrayList<TableEditor>) table.getData(TABLE_EDITORS);

		for (TableEditor editor : tableEditors) {
			if (!editor.getEditor().isDisposed()) {
				editor.getEditor().dispose();
				editor.dispose();
			}
		}

		tableEditors.clear();
	}

	@Override
	public void setEnabled(boolean enabled) {
		widgetIsEnabled = enabled; 

		for (TabItem tabItem : tabFolder.getItems()) {
			Button button = (Button) tabItem.getData(ACQUISITION_DISABLE_BUTTON);
			button.setEnabled(enabled);

			@SuppressWarnings("unchecked")
			ArrayList<Button> cycleDisableButtons = (ArrayList<Button>) tabItem.getData(CYCLE_DISABLE_BUTTONS);

			for (Button cycleDisableButton : cycleDisableButtons) {
				if (cycleDisableButton != null) {
					cycleDisableButton.setEnabled(enabled);
				}
			}

			@SuppressWarnings("unchecked")
			ArrayList<Button> cycleOffPeakButtons = (ArrayList<Button>) tabItem.getData(CYCLE_OFF_PEAK_BUTTONS);

			for (Button cycleOffPeakButton : cycleOffPeakButtons) {
				if (cycleOffPeakButton != null) {
					cycleOffPeakButton.setEnabled(enabled);
				}
			}
		}
	}

	public void setCorrIntervalList(ReplicateV1 originalReplicate, CorrIntervalList currentCorrIntervalList) {
		if (this.originalReplicate == originalReplicate && this.currentCorrIntervalList == currentCorrIntervalList) {
			return;
		}

		this.originalReplicate = originalReplicate;
		this.currentCorrIntervalList = currentCorrIntervalList;

		columnTitles = null;
		refreshTables();
		notifyAcquisitionChanged();
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
				if (inputParameter.getInputParameterType() == InputParameterType.RefMeasurement) {
					mzX10ToRefVoltageInputParameter.put(inputParameter.getMzX10(), inputParameter);
				}

				if (inputParameter.getInputParameterType() == InputParameterType.SampleMeasurement) {
					mzX10ToSampleVoltageInputParameter.put(inputParameter.getMzX10(), inputParameter);
				}
			}
		}

		for (InputParameter inputParameter : InputParameter.values()) {
			if (inputParameter.getMzX10() != null) {
				validMZX10s.add(inputParameter.getMzX10());
			}
		}
	}
}
