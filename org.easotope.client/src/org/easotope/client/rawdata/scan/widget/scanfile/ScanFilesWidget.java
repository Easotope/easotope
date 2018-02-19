/*
 * Copyright © 2016-2018 by Devon Bowen.
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

package org.easotope.client.rawdata.scan.widget.scanfile;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.easotope.client.Messages;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.rawdata.util.FileReader;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListItem;
import org.easotope.shared.core.DoubleTools;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.ScanFile;
import org.easotope.shared.rawdata.compute.ComputeScanFileParsed;
import org.easotope.shared.rawdata.tables.ScanFileInputV0;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;
import org.easotope.shared.rawdata.tables.ScanV3;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class ScanFilesWidget extends EasotopeComposite {
	private static HashSet<Integer> validMZX10s = new HashSet<Integer>();

	private Composite centerComposite;
	private TabFolder tabFolder;

	private ScanV3 originalScan;
	private TreeSet<ScanFile> originalScanFiles = new TreeSet<ScanFile>();
	private CorrIntervalList currentCorrIntervalList = null;

	private TreeSet<ScanFile> scanFiles = new TreeSet<ScanFile>();
	private boolean widgetIsEnabled;

	private Vector<ScanFilesWidgetListener> listeners = new Vector<ScanFilesWidgetListener>();

	public ScanFilesWidget(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
 		setLayout(new StackLayout());
 
 		centerComposite = new Composite(this, SWT.NONE);
 		centerComposite.setLayout(new GridLayout());
 		attachDropCode(centerComposite);
  
 		Composite importComposite = new Composite(centerComposite, SWT.NONE);
 		importComposite.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
 		importComposite.setLayout(new GridLayout());

 		Label message1Label = new Label(importComposite, SWT.NONE);
 		message1Label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		message1Label.setText(Messages.scanFilesWidget_message1);
 
 		Label message2Label = new Label(importComposite, SWT.NONE);
 		message2Label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		message2Label.setText(Messages.scanFilesWidget_message2);

 		Label message3Label = new Label(importComposite, SWT.NONE);
 		message3Label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		message3Label.setText(Messages.scanFilesWidget_message3);

 		tabFolder = new TabFolder(this, SWT.BORDER);
 		attachDropCode(tabFolder);

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
			return;
		}

		int startingNumOfScanFiles = scanFiles.size();

		for (String filename : filenames) {
			addFile(filename);
		}

		if (scanFiles.size() - startingNumOfScanFiles != 0) {
			rebuildTabs();
			notifyScanFileChanged();
		}
	}

	private void addFile(String filename) {
		if (!filename.endsWith(".scn") && !filename.endsWith(".properties")) {
			MessageDialog.openError(getParent().getShell(), Messages.scanFilesWidget_fileAddErrorTitle, Messages.scanFilesWidget_fileWrongType);
			return;
		}

		File file = new File(filename);
		byte[] fileBytes = null;

		try {
			fileBytes = FileReader.getBytesFromFile(filename);
		} catch (Exception e) {
			MessageDialog.openError(getParent().getShell(), Messages.scanFilesWidget_fileAddErrorTitle, e.getMessage());
			return;
		}

		RawFile rawFile = new RawFile();
		rawFile.setOriginalName(file.getName());

		ScanFileParsedV2 scanFileParsed = null;

		try {
			scanFileParsed = ComputeScanFileParsed.compute(rawFile, fileBytes, false);
		} catch (RuntimeException e) {
			MessageDialog.openError(getParent().getShell(), Messages.scanFilesWidget_fileAddErrorTitle, e.getMessage());
			return;
		}

		ScanFileInputV0 scanFileInput = new ScanFileInputV0();
		scanFileInput.setRawFileId(rawFile.getId());

		ScanFile scanFile = new ScanFile(fileBytes, rawFile, scanFileInput, scanFileParsed);

		if (!scanFiles.isEmpty() && !scanFile.isCompatibleWith(scanFiles.first())) {
			String error = MessageFormat.format(Messages.scanFilesWidget_dataMismatchReplicateText, new Object[] { new File(filename).getName() });
			if (!MessageDialog.openQuestion(getParent().getShell(), Messages.scanFilesWidget_fileAddErrorTitle, error)) {
				return;
			}
		}

		scanFiles.add(scanFile);
	}

	public ArrayList<ScanFile> getScanFiles() {
		return new ArrayList<ScanFile>(scanFiles);
	}

	public boolean hasChanged() {
		if (originalScanFiles.size() != scanFiles.size()) {
			return true;
		}

		Iterator<ScanFile> originalIterator = originalScanFiles.iterator();
		Iterator<ScanFile> iterator = scanFiles.iterator();

		while (originalIterator.hasNext()) {
			ScanFile originalScanFile = originalIterator.next();
			ScanFile scanFile = iterator.next();

			if (!originalScanFile.equals(scanFile)) {
				return true;
			}
		}

		if (originalScan != null) {
			Integer[] originalChannelToMzX10 = originalScan.getChannelToMzX10();
			int channelCount = 0;
			int tabCount = 0;

			for (TabItem tabItem : tabFolder.getItems()) {
				if (tabItem.getControl() instanceof ByMassWidget) {
					ByMassWidget byMassWidget = (ByMassWidget) tabItem.getControl();

					while (originalChannelToMzX10.length > tabCount && originalChannelToMzX10[channelCount] == null) {
						channelCount++;
					}

					if (originalChannelToMzX10.length <= channelCount) {
						return true;
					}

					if (getInputParameter(originalChannelToMzX10[channelCount]) != byMassWidget.getInputParameter()) {
						return true;
					}

					int algorithm = byMassWidget.getAlgorithm();
					int origAlgorithm = originalScan.getAlgorithm()[tabCount];

					if (algorithm != origAlgorithm) {
						return true;
					}

					BackgroundSelector backgroundSelector = byMassWidget.getBackgroundSelector();

					double x1 = backgroundSelector.getLeftXRangeSelection().getX1();
					double x2 = backgroundSelector.getLeftXRangeSelection().getX2();
					boolean xsAreValid = !Double.isNaN(x1) && !Double.isNaN(x2) && x1 != x2;

					if (!xsAreValid) {
						x1 = Double.NaN;
						x2 = Double.NaN;
					}

					double origX1 = originalScan.getLeftBackgroundX1()[tabCount];
					double origX2 = originalScan.getLeftBackgroundX2()[tabCount];
					boolean origXsAreValid = !Double.isNaN(origX1) && !Double.isNaN(origX2) && origX1 != origX2;
	
					if (!origXsAreValid) {
						origX1 = Double.NaN;
						origX2 = Double.NaN;
					}

					if (xsAreValid != origXsAreValid) {
						return true;
					}

					if (xsAreValid && (x1 != origX1 || x2 != origX2)) {
						return true;
					}

					x1 = backgroundSelector.getRightXRangeSelection().getX1();
					x2 = backgroundSelector.getRightXRangeSelection().getX2();
					xsAreValid = !Double.isNaN(x1) && !Double.isNaN(x2) && x1 != x2;

					if (!xsAreValid) {
						x1 = Double.NaN;
						x2 = Double.NaN;
					}

					origX1 = originalScan.getRightBackgroundX1()[tabCount];
					origX2 = originalScan.getRightBackgroundX2()[tabCount];
					origXsAreValid = !Double.isNaN(origX1) && !Double.isNaN(origX2) && origX1 != origX2;

					if (!origXsAreValid) {
						origX1 = Double.NaN;
						origX2 = Double.NaN;
					}

					if (xsAreValid != origXsAreValid) {
						return true;
					}

					if (xsAreValid && (x1 != origX1 || x2 != origX2)) {
						return true;
					}

					Integer referenceChannel = byMassWidget.getReferenceChannel();
					Integer[] originalReferenceChannels = originalScan.getReferenceChannel();
					Integer origReferenceChannel = (originalReferenceChannels != null) ? originalReferenceChannels[tabCount] : null;

					if ((referenceChannel == null || origReferenceChannel == null) && referenceChannel != origReferenceChannel) {
						return true;
					}

					if (referenceChannel != null && !referenceChannel.equals(origReferenceChannel)) {
						return true;
					}

					int degreeOfFit = byMassWidget.getDegreeOfFit();
					int origDegreeOfFit = originalScan.getDegreeOfFit()[tabCount];

					if (degreeOfFit != origDegreeOfFit) {
						return true;
					}

					Integer referenceChannel2 = byMassWidget.getReferenceChannel2();
					Integer[] originalReferenceChannels2 = originalScan.getReferenceChannel2();
					Integer origReferenceChannel2 = (originalReferenceChannels2 != null) ? originalReferenceChannels2[tabCount] : null;

					if ((referenceChannel2 == null || origReferenceChannel2 == null) && referenceChannel2 != origReferenceChannel2) {
						return true;
					}

					if (referenceChannel2 != null && !referenceChannel2.equals(origReferenceChannel2)) {
						return true;
					}

					double factor2 = byMassWidget.getFactor2();
					double origFactor2 = originalScan.getFactor2()[tabCount];

					if (!DoubleTools.essentiallyEqual(factor2, origFactor2)) {
						return true;
					}

				} else if (tabItem.getControl() instanceof ByFileWidget) {
					ByFileWidget byFileWidget = (ByFileWidget) tabItem.getControl();

					double onPeakX1 = byFileWidget.getOnPeakX1();
					double origOnPeakX1 = originalScan.getOnPeakX1();

					if (Double.isNaN(onPeakX1) && !Double.isNaN(origOnPeakX1)) {
						return true;
					}

					if (!Double.isNaN(onPeakX1) && Double.isNaN(origOnPeakX1)) {
						return true;
					}

					if ((!Double.isNaN(onPeakX1) || !Double.isNaN(origOnPeakX1)) && onPeakX1 != origOnPeakX1) {
						return true;
					}

					double onPeakX2 = byFileWidget.getOnPeakX2();
					double origOnPeakX2 = originalScan.getOnPeakX2();

					if (Double.isNaN(onPeakX2) && !Double.isNaN(origOnPeakX2)) {
						return true;
					}

					if (!Double.isNaN(onPeakX2) && Double.isNaN(origOnPeakX2)) {
						return true;
					}

					if ((!Double.isNaN(onPeakX2) || !Double.isNaN(origOnPeakX2)) && onPeakX2 != origOnPeakX2) {
						return true;
					}
				}

				channelCount++;
				tabCount++;
			}
		}

		return false;
	}

	public void setScanFiles(ScanV3 scan, ArrayList<ScanFile> scanFiles) {
		originalScan = scan == null ? null : new ScanV3(scan);
		originalScanFiles = scanFiles == null ? new TreeSet<ScanFile>() : new TreeSet<ScanFile>(scanFiles);
		revert();
	}

	public void setCorrIntervalList(CorrIntervalList currentCorrIntervalList) {
		this.currentCorrIntervalList = currentCorrIntervalList;
		setVisible(this.currentCorrIntervalList != null);
		rebuildTabs();
		setScanData(originalScan);
	}

	private Integer[] getChannelToMZX10() {
		if (currentCorrIntervalList == null) {
			return null;
		}

		if (originalScan != null && originalScan.getMassSpecId() == currentCorrIntervalList.getMassSpecId()) {
			return originalScan.getChannelToMzX10();
		}

		if (scanFiles.size() == 0) {
			return null;
		}

		CorrIntervalListItem corrIntervalListItem = null;

		ArrayList<CorrIntervalListItem> sorted = new ArrayList<CorrIntervalListItem>(currentCorrIntervalList.values());
		Collections.sort(sorted, new CorrIntervalListItemComparator());

		long searchDate = scanFiles.first().getScanFileParsed().getDate();

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
		}

		if (scanFiles.first().getScanFileParsed().getChannelToMzX10() != null) {
			Integer[] defaultMapping = (Integer[]) scanFiles.first().getScanFileParsed().getChannelToMzX10().clone(); 

			for (int i=0; i<defaultMapping.length; i++) {
				Integer mzX10 = defaultMapping[i];
				if (mzX10 != null && !validMZX10s.contains(mzX10)) {
					defaultMapping[i] = null;
				}
			}

			return defaultMapping;
		}

		return null;
	}

	@Override
	public void setEnabled(boolean enabled) {
		widgetIsEnabled = enabled;

		for (TabItem tabItem : tabFolder.getItems()) {
			if (tabItem.getControl() instanceof ByMassWidget) {
				ByMassWidget byMassWidget = (ByMassWidget) tabItem.getControl();
				byMassWidget.setEnabled(enabled);
			}

			if (tabItem.getControl() instanceof ByFileWidget) {
				ByFileWidget byFileWidget = (ByFileWidget) tabItem.getControl();
				byFileWidget.setEnabled(enabled);
			}
		}
	}

	public void revert() {
		if (!hasChanged()) {
			return;
		}

		scanFiles.clear();

		for (ScanFile scanFile : originalScanFiles) {
			scanFiles.add(new ScanFile(scanFile));
		}

		rebuildTabs();
		setScanData(originalScan);
	}

	private void rebuildTabs() {
		if (!getVisible()) {
			return;
		}

		if (scanFiles.size() == 0) {
			for (TabItem tabItem : tabFolder.getItems()) {
				if (tabItem.getControl() instanceof ByMassWidget) {
					ByMassWidget byMassWidget = (ByMassWidget) tabItem.getControl();
					byMassWidget.setValues(ByMassWidget.DEFAULT_ALGORITHM, null, -1, Double.NaN, Double.NaN, Double.NaN, Double.NaN, null, Double.NaN);
				}

				if (tabItem.getControl() instanceof ByFileWidget) {
					ByFileWidget byFileWidget = (ByFileWidget) tabItem.getControl();
					byFileWidget.setOnPeakXs(Double.NaN, Double.NaN);
				}
			}

			((StackLayout) getLayout()).topControl = centerComposite;
			layout();
			return;
		}

		ArrayList<InputParameter> newInputParameters = new ArrayList<InputParameter>();
		ArrayList<String> tabLabels = new ArrayList<String>();

		Integer[] channelToMZX10 = getChannelToMZX10();

		if (channelToMZX10 != null) {
			for (Integer mzX10 : channelToMZX10) {
				
				if (mzX10 != null) {
					InputParameter inputParameter = getInputParameter(mzX10);

					if (inputParameter != null) {
						newInputParameters.add(inputParameter);

						if (mzX10 % 10 == 0) {
							tabLabels.add(String.valueOf(mzX10 / 10));
						} else {
							tabLabels.add(String.valueOf(mzX10 / 10.0));
						}
					}
				}
			}
		}

		boolean needsNewTabs = tabFolder.getItemCount() == 0 || newInputParameters.size() != tabFolder.getItemCount()-1;

		if (!needsNewTabs) {
			int count = 0;
			for (TabItem tabItem : tabFolder.getItems()) {
				if (tabItem.getControl() instanceof ByMassWidget) {
					ByMassWidget byMassWidget = (ByMassWidget) tabItem.getControl();

					if (newInputParameters.get(count) != byMassWidget.getInputParameter()) {
						needsNewTabs = true;
						break;
					}
				}

				count++;
			}
		}

		if (needsNewTabs) {
			for (TabItem tabItem : tabFolder.getItems()) {
				tabItem.getControl().dispose();
				tabItem.dispose();
			}

			for (int i=0; i<newInputParameters.size(); i++) {
				InputParameter inputParameter = newInputParameters.get(i);
				TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
				tabItem.setText(tabLabels.get(i));
				ByMassWidget byMassWidget = new ByMassWidget(tabFolder, SWT.NONE);
				byMassWidget.setInputParameter(inputParameter, tabLabels.get(i));
				byMassWidget.setTabItem(tabItem);
				byMassWidget.addListener(new ByMassWidgetListener() {
					@Override
					public void rangesChanged() {
						notifyScanFileChanged();
					}
				});
				tabItem.setControl(byMassWidget);
			}

			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(Messages.scanFileWidget_byFileTab);
			ByFileWidget byFileWidget = new ByFileWidget(tabFolder, SWT.NONE);
			byFileWidget.setTabItem(tabItem);
			byFileWidget.addListener(new ByFileWidgetListener() {
				@Override
				public void rangesChanged() {
					updateAllReferenceChannelOnPeakValues();
					notifyScanFileChanged();
				}
			});
			tabItem.setControl(byFileWidget);
		}

		((StackLayout) getLayout()).topControl = tabFolder;
		layout();

		TabItem[] tabItems = tabFolder.getItems();

		if (tabItems != null && tabItems.length != 0) {
			ByFileWidget byFileWidget = (ByFileWidget) tabItems[tabItems.length-1].getControl();
			double onPeakX1 = byFileWidget.getOnPeakX1();
			double onPeakX2 = byFileWidget.getOnPeakX2();

			HashMap<Integer,double[]> allReferenceChannelOnPeakValues = calculateReferenceChannelOnPeakValues(channelToMZX10, onPeakX1, onPeakX2);

			for (int i=0; i<tabItems.length-1; i++) {
				ByMassWidget byMassWidget = (ByMassWidget) tabItems[i].getControl();
				byMassWidget.setInitialValues(scanFiles, channelToMZX10, allReferenceChannelOnPeakValues);
			}

			byFileWidget.setScanFiles(scanFiles, newInputParameters);
		}
	}

	private void updateAllReferenceChannelOnPeakValues() {
		Integer[] channelToMZX10 = getChannelToMZX10();

		TabItem[] tabItems = tabFolder.getItems();
		ByFileWidget byFileWidget = (ByFileWidget) tabItems[tabItems.length-1].getControl();
		double onPeakX1 = byFileWidget.getOnPeakX1();
		double onPeakX2 = byFileWidget.getOnPeakX2();

		HashMap<Integer,double[]> allReferenceChannelOnPeakValues = calculateReferenceChannelOnPeakValues(channelToMZX10, onPeakX1, onPeakX2);

		for (int i=0; i<tabItems.length-1; i++) {
			ByMassWidget byMassWidget = (ByMassWidget) tabItems[i].getControl();
			byMassWidget.setAllReferenceChannelOnPeakValues(allReferenceChannelOnPeakValues);
		}
	}

	private HashMap<Integer,double[]> calculateReferenceChannelOnPeakValues(Integer[] channelToMZX10, double onPeakX1, double onPeakX2) {
		HashMap<Integer,double[]> result = new HashMap<Integer,double[]>();

		int channel = 0;
		for (Integer mzX10 : channelToMZX10) {
			if (mzX10 == null) {
				channel++;
				continue;
			}

			int count=0;
			double[] array = new double[scanFiles.size()];
	
			for (ScanFile scanFile : scanFiles) {
				ScanFileParsedV2 scanFileParsed = scanFile.getScanFileParsed();
				Double[] scan = scanFileParsed.getMeasurements().get(getInputParameter(mzX10));

				double onPeakValue = Double.NEGATIVE_INFINITY;

				if (scan == null) {
					array[count++] = onPeakValue;
					continue;
				}

				if (Double.isNaN(onPeakX1) || Double.isNaN(onPeakX2)) {
					int start = (int) (scan.length * 0.05);
					int stop = (int) (scan.length * 0.95);

					for (int i=start; i<stop; i++) {
						if (scan[i] > onPeakValue) {
							onPeakValue = scan[i];
						}
					}

				} else {
					double m = (scanFileParsed.getToVoltage()-scanFileParsed.getFromVoltage()) / (scan.length-1);
					double b = scanFileParsed.getFromVoltage();

					double total = 0.0d;
					int number = 0;

					for (int i=0; i<scan.length; i++) {
						double x = m * i + b;

						if (x >= onPeakX1 && x <= onPeakX2) {
							total += scan[i];
							number++;
						}
					}

					onPeakValue = total / number;
				}

				array[count++] = onPeakValue;
			}

			result.put(channel++, array);
		}

		return result;
	}

	public InputParameter getInputParameter(int mzX10) {
		Integer[] channelToMZX10 = getChannelToMZX10();

		if (channelToMZX10 == null) {
			return null;
		}

		int channel = 0;
		while (channel < channelToMZX10.length && (channelToMZX10[channel] == null || channelToMZX10[channel] != mzX10)) {
			channel++;
		}

		if (channel == channelToMZX10.length) {
			return null;
		}
		
		return InputParameter.values()[InputParameter.Channel0_Scan.ordinal() + channel];
	}
	
	public void fillInScan(ScanV3 scan) {
		int numMasses = tabFolder.getItems().length - 1;

		int[] algorithm = new int[numMasses];
		double onPeakX1 = Double.NaN;
		double onPeakX2 = Double.NaN;
		Double[] leftBackgroundX1 = new Double[numMasses];
		Double[] leftBackgroundX2 = new Double[numMasses];
		Double[] rightBackgroundX1 = new Double[numMasses];
		Double[] rightBackgroundX2 = new Double[numMasses];
		int[] degreeOfFit = new int[numMasses];
		Double[] x2Coeff = new Double[numMasses];
		Double[] x1Coeff = new Double[numMasses];
		Double[] x0Coeff = new Double[numMasses];
		Integer[] referenceChannel = new Integer[numMasses];
		Integer[] referenceChannel2 = new Integer[numMasses];
		Double[] factor2 = new Double[numMasses];

		int count = 0;
		for (TabItem tabItem : tabFolder.getItems()) {
			if (tabItem.getControl() instanceof ByMassWidget) {
				ByMassWidget byMassWidget = (ByMassWidget) tabItem.getControl();

				algorithm[count] = byMassWidget.getAlgorithm();
				
				leftBackgroundX1[count] = byMassWidget.getBackgroundSelector().getLeftXRangeSelection().getX1();
				leftBackgroundX2[count] = byMassWidget.getBackgroundSelector().getLeftXRangeSelection().getX2();

				if (Double.isNaN(leftBackgroundX1[count]) || Double.isNaN(leftBackgroundX2[count]) || leftBackgroundX1[count] == leftBackgroundX2[count]) {
					leftBackgroundX1[count] = Double.NaN;
					leftBackgroundX2[count] = Double.NaN;
				}

				rightBackgroundX1[count] = byMassWidget.getBackgroundSelector().getRightXRangeSelection().getX1();
				rightBackgroundX2[count] = byMassWidget.getBackgroundSelector().getRightXRangeSelection().getX2();

				if (Double.isNaN(rightBackgroundX1[count]) || Double.isNaN(rightBackgroundX2[count]) || rightBackgroundX1[count] == rightBackgroundX2[count]) {
					rightBackgroundX1[count] = Double.NaN;
					rightBackgroundX2[count] = Double.NaN;
				}

				degreeOfFit[count] = byMassWidget.getDegreeOfFit();

				x2Coeff[count] = byMassWidget.getPblRegression().getX2Coeff();
				x1Coeff[count] = byMassWidget.getPblRegression().getX1Coeff();
				x0Coeff[count] = byMassWidget.getPblRegression().getX0Coeff();

				referenceChannel[count] = byMassWidget.getReferenceChannel();

				referenceChannel2[count] = byMassWidget.getReferenceChannel2();
				factor2[count] = byMassWidget.getFactor2();

				count++;

			} else if (tabItem.getControl() instanceof ByFileWidget) {
				ByFileWidget byFileWidget = (ByFileWidget) tabItem.getControl();

				onPeakX1 = byFileWidget.getOnPeakX1();
				onPeakX2 = byFileWidget.getOnPeakX2();
			}
		}

		scan.setAlgorithm(algorithm);
		scan.setOnPeakX1(onPeakX1);
		scan.setOnPeakX2(onPeakX2);
		scan.setLeftBackgroundX1(leftBackgroundX1);
		scan.setLeftBackgroundX2(leftBackgroundX2);
		scan.setRightBackgroundX1(rightBackgroundX1);
		scan.setRightBackgroundX2(rightBackgroundX2);
		scan.setDegreeOfFit(degreeOfFit);
		scan.setX2Coeff(x2Coeff);
		scan.setSlope(x1Coeff);
		scan.setIntercept(x0Coeff);
		scan.setReferenceChannel(referenceChannel);
		scan.setChannelToMzX10(getChannelToMZX10());
		scan.setReferenceChannel2(referenceChannel2);
		scan.setFactor2(factor2);
	}

	public void setScanData(ScanV3 scan) {
		if (scanFiles.size() == 0) {
			return;
		}

		int count = 0;
		for (TabItem tabItem : tabFolder.getItems()) {
			if (tabItem.getControl() instanceof ByMassWidget) {
				ByMassWidget byMassWidget = (ByMassWidget) tabItem.getControl();

				if (scan == null || scan.getLeftBackgroundX1().length <= count) {
					byMassWidget.setValues(ByMassWidget.DEFAULT_ALGORITHM, null, 1, Double.NaN, Double.NaN, Double.NaN, Double.NaN, null, Double.NaN);

				} else {
					int algorithm = scan.getAlgorithm()[count];
					Integer referenceChannel = scan.getReferenceChannel()[count];
					int degreeOfFit = scan.getDegreeOfFit()[count];
					double leftX1 = scan.getLeftBackgroundX1()[count];
					double leftX2 = scan.getLeftBackgroundX2()[count];
					double rightX1 = scan.getRightBackgroundX1()[count];
					double rightX2 = scan.getRightBackgroundX2()[count];
					Integer referenceChannel2 = scan.getReferenceChannel2()[count];
					double factor2 = scan.getFactor2()[count];

					byMassWidget.setValues(algorithm, referenceChannel, degreeOfFit, leftX1, leftX2, rightX1, rightX2, referenceChannel2, factor2);
				}

			} else if (tabItem.getControl() instanceof ByFileWidget) {
				ByFileWidget byFileWidget = (ByFileWidget) tabItem.getControl();

				double onPeakX1 = scan.getOnPeakX1();
				double onPeakX2 = scan.getOnPeakX2();

				byFileWidget.setOnPeakXs(onPeakX1, onPeakX2);
			}

			count++;
		}
	}

	public void addListener(ScanFilesWidgetListener listener) {
		listeners.add(listener);
	}

	private void notifyScanFileChanged() {
		for (ScanFilesWidgetListener scanFileWidgetListener : listeners) {
			scanFileWidgetListener.scanFilesChanged();
		}
	}

	public class CorrIntervalListItemComparator implements Comparator<CorrIntervalListItem> {
		@Override
		public int compare(CorrIntervalListItem arg0, CorrIntervalListItem arg1) {
			return ((Long) arg0.getDate()).compareTo(arg1.getDate());
		}
	}

	public boolean hasError() {
		for (TabItem tabItem : tabFolder.getItems()) {
			if (tabItem.getData(ByMassWidget.TAB_HAS_ERROR) != null) {
				return true;
			}
		}

		return false;
	}

	static {
		for (InputParameter inputParameter : InputParameter.values()) {
			if (inputParameter.getMzX10() != null) {
				validMZX10s.add(inputParameter.getMzX10());
			}
		}
	}
}
