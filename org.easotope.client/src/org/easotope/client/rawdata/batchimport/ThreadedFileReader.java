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

package org.easotope.client.rawdata.batchimport;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.easotope.client.Messages;
import org.easotope.client.rawdata.util.FileReader;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.cache.sourcelist.sourcelist.SourceList;
import org.easotope.shared.rawdata.cache.sourcelist.sourcelist.SourceListItem;
import org.easotope.shared.rawdata.compute.ComputeAcquisitionParsed;
import org.easotope.shared.rawdata.parser.AutoParser;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class ThreadedFileReader implements Runnable {
	private static final int MAX_LEVENSHTEIN_DISTANCE = 4;
	private static final int MAX_COMBO_SIZE = 10;

	private BatchImportComposite batchImportComposite;
	private ArrayList<String> fileNameQueue = new ArrayList<String>();
	private HashSet<String> acceptableFileExtensions = new HashSet<String>();
	private HashSet<String> alreadyParsedFiles = new HashSet<String>();
	private boolean abort = false;
	private String assumedTimeZone = null;
	private SourceList globalSourceList = null;

	public ThreadedFileReader(BatchImportComposite batchImportComposite) {
		this.batchImportComposite = batchImportComposite;

		for (String extension : AutoParser.getFileExtensions()) {
			if (!extension.equals(".scn")) {
				acceptableFileExtensions.add(extension);
			}
		}
	}

	@Override
	public void run() {
		synchronized (fileNameQueue) {
			while (true) {
				if (abort || batchImportComposite.isDisposed()) {
					return;
				}

				if (fileNameQueue.size() != 0) {
					String filename = fileNameQueue.remove(0);
					String message = MessageFormat.format(Messages.threadedFileReader_processingFileName, filename);
					Log.getInstance().log(Level.DEBUG, this, message);
					processFileOrDirectory(filename);
				}

				try {
					if (assumedTimeZone != null) {
						final String tz = assumedTimeZone;
						batchImportComposite.getDisplay().asyncExec(() -> {
							if (!batchImportComposite.isDisposed()) {
								Shell shell = batchImportComposite.getParent().getShell();
								String message = MessageFormat.format(Messages.threadedFileReader_assumedTimeZone, tz);
								MessageDialog.openError(shell, Messages.threadedFileReader_assumedTimeZoneTitle, message);
							}
						});

						assumedTimeZone = null;
					}

					Log.getInstance().log(Level.DEBUG, this, Messages.threadedFileReader_waitingForFileName);
					fileNameQueue.wait();

				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}

	public void dispose() {
		abort = true;
		synchronized (fileNameQueue) {
			fileNameQueue.notify();
		}
	}

	public void setSourceList(SourceList sourceList) {
		this.globalSourceList = sourceList;
	}

	public void process(String[] fileNames) {
		synchronized (fileNameQueue) {
			for (String fileName : fileNames) {
				fileNameQueue.add(fileName);
				fileNameQueue.notify();
			}
		}
	}

	public void process(String filename) {
		synchronized (fileNameQueue) {
			fileNameQueue.add(filename);
			fileNameQueue.notify();
		}
	}

	private void processFileOrDirectory(String path) {
		File file = new File(path);
		processFileOrDirectory(file, false);
	}

	private void processFileOrDirectory(File file, boolean fileWasFound) {
		String canonicalPath;

		try {
			canonicalPath = file.getCanonicalPath();
		} catch (IOException e) {
			return;
		}

		if (alreadyParsedFiles.contains(canonicalPath)) {
			return;
		}

		alreadyParsedFiles.add(canonicalPath);

		boolean fileExtensionIsAcceptable = false;

		for (String extension : acceptableFileExtensions) {
			if (canonicalPath.endsWith(extension)) {
				fileExtensionIsAcceptable = true;
				break;
			}
		}

		if (file.isFile() && fileExtensionIsAcceptable) {
			processFile(file, fileWasFound);

		} else if (file.isDirectory()) {
	        File[] list = file.listFiles();

	        for (File subDirFile : list) {
	        	processFileOrDirectory(subDirFile, true);
	        }
		}
	}

	private void processFile(File file, boolean fileWasFound) {
		byte[] fileBytes = null;

		try {
			fileBytes = FileReader.getBytesFromFile(file.getCanonicalPath());
		} catch (Exception e) {
			batchImportComposite.getDisplay().asyncExec(() -> {
				if (!batchImportComposite.isDisposed()) {
					Shell shell = batchImportComposite.getParent().getShell();
					MessageDialog.openError(shell, Messages.threadedFileReader_fileAddErrorTitle, e.getMessage());
				}
			});

			return;
		}

		RawFile rawFile = new RawFile();
		rawFile.setOriginalName(file.getName());

		ComputeAcquisitionParsed computeAcquisitionParsed = null;

		try {
			computeAcquisitionParsed = new ComputeAcquisitionParsed(rawFile, fileBytes, false, null);
		} catch (RuntimeException e) {
			if (!fileWasFound) {
				batchImportComposite.getDisplay().asyncExec(() -> {
					if (!batchImportComposite.isDisposed()) {
						Shell shell = batchImportComposite.getParent().getShell();
						MessageDialog.openError(shell, Messages.threadedFileReader_fileAddErrorTitle, e.getMessage());
					}
				});
			}

			return;
		}

		if (assumedTimeZone == null) {
			assumedTimeZone = computeAcquisitionParsed.getAssumedTimeZone();
		}

		int acquisitionNumber = 0;
		for (AcquisitionParsedV2 acquisitionParsed : computeAcquisitionParsed.getMaps()) {
			ImportedFile importedFile = new ImportedFile();
			importedFile.setAssumedTime(computeAcquisitionParsed.getAssumedTimeZone() != null);
			importedFile.setTimestamp(acquisitionParsed.getDate());
			importedFile.setAcquisitionNumber(acquisitionNumber++);
			importedFile.setFileName(file.getName());

			String id1 = (String) acquisitionParsed.getMisc().get(InputParameter.Identifier_1);
			String id2 = (String) acquisitionParsed.getMisc().get(InputParameter.Identifier_2);
			String sampleName = (String) acquisitionParsed.getMisc().get(InputParameter.Sample_Name);
			
			if (id1 != null || id2 != null) {
				importedFile.setIdentifier1(id1);
				importedFile.setIdentifier2(id2);
			} else {
				importedFile.setIdentifier1(sampleName);
			}

			String firstString = importedFile.getIdentifier1();
			int firstStringFactor = 1;

			if (firstString != null && firstString.length() != 0) {
				firstString = firstString.toUpperCase();
				firstStringFactor = firstString.length();
			}

			String secondString = importedFile.getIdentifier2();
			int secondStringFactor = 1;

			if (secondString != null && secondString.length() != 0) {
				secondString = secondString.toUpperCase();
				secondStringFactor = secondString.length();
			}

			ArrayList<SourceListItem> sourceList = null;

			if (globalSourceList != null) {
				ArrayList<SourceListItemSorter> comboItems = new ArrayList<SourceListItemSorter>();

				for (SourceListItem sourceListItem : globalSourceList) {
					int levenshteinDistance = -1;
					SourceListItemSorter sorter = null;

					if (firstString != null && firstString.length() != 0) {
						int distance = StringUtils.getLevenshteinDistance(firstString, sourceListItem.getSourceNameToUpper(), MAX_LEVENSHTEIN_DISTANCE) * secondStringFactor;

						if (distance >= 0 && (levenshteinDistance == -1 || distance < levenshteinDistance)) {
							levenshteinDistance = distance;
							sorter = new SourceListItemSorter(levenshteinDistance, sourceListItem);
						}
					}

					if (secondString != null && secondString.length() != 0) {
						int distance = StringUtils.getLevenshteinDistance(secondString, sourceListItem.getSourceNameToUpper(), MAX_LEVENSHTEIN_DISTANCE) * firstStringFactor;

						if (distance >= 0 && (levenshteinDistance == -1 || distance < levenshteinDistance)) {
							levenshteinDistance = distance;
							sorter = new SourceListItemSorter(levenshteinDistance, sourceListItem);
						}
					}

					if (levenshteinDistance >= 0) {
						comboItems.add(sorter);
					}
				}

				Collections.sort(comboItems);
				sourceList = new ArrayList<SourceListItem>();

				for (SourceListItemSorter sourceListItemSorter : comboItems) {
					sourceList.add(sourceListItemSorter.getSourceListItem());
					
					if (sourceList.size() == MAX_COMBO_SIZE) {
						break;
					}
				}
			}

			importedFile.setSourceList(sourceList);

			if (sourceList.size() != 0) {
				importedFile.setSampleId(sourceList.get(0).getSampleId());
				importedFile.setStandardId(sourceList.get(0).getStandardId());
			} else {
				importedFile.setSampleId(DatabaseConstants.EMPTY_DB_ID);
				importedFile.setStandardId(DatabaseConstants.EMPTY_DB_ID);
			}

			batchImportComposite.addImportedFile(importedFile);
		}
	}

	public class SourceListItemSorter implements Comparable<SourceListItemSorter> {
		private int levenshteinDistance;
		private SourceListItem sourceListItem;

		public SourceListItemSorter(int levenshteinDistance, SourceListItem sourceListItem) {
			this.levenshteinDistance = levenshteinDistance;
			this.sourceListItem = sourceListItem;
		}

		public SourceListItem getSourceListItem() {
			return sourceListItem;
		}

		@Override
		public int compareTo(SourceListItemSorter that) {
			int distanceCompare = ((Integer) this.levenshteinDistance).compareTo(that.levenshteinDistance);

			if (distanceCompare != 0) {
				return distanceCompare;
			}

			int timeCompare = ((Long) this.sourceListItem.getLastUsed()).compareTo(that.sourceListItem.getLastUsed());

			return -timeCompare;
		}

		@Override
		public boolean equals(Object obj) {
			SourceListItemSorter that = (SourceListItemSorter) obj;
			return compareTo(that) == 0;
		}
	}
}
