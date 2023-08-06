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

package org.easotope.client.rawdata.batchimport.table;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.rawdata.batchimport.BatchImportComposite;
import org.easotope.client.rawdata.batchimport.ImportedFile;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class BatchImportTable extends Composite {
	private static final String ERROR_WARNING = "ERROR_WARNING";
	private static final String IGNORES = "IGNORES";
	private static final String ASSIGNMENTS = "ASSIGNMENTS";
	private static final String ACID_TEMPS = "ACID_TEMPS";
	private static final String GROUPINGS = "GROUPINGS";

	private BatchImportComposite listener;
	private TreeSet<ImportedFile> importedFiles = null;
	private Table table;
	private int tableRowHeight = 10;

	public BatchImportTable(Composite parent, BatchImportComposite listener, int style) {
		super(parent, style);
		this.listener = listener;
		setLayout(new FillLayout());

		table = new Table(this, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);

		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText(Messages.batchImportTable_statusHeader);
		tableColumn.setWidth(40);

		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText(Messages.batchImportTable_ignoreHeader);
		tableColumn.setWidth(40);

		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText(Messages.batchImportTable_timeHeader);
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText(Messages.batchImportTable_filenameHeader);
		tableColumn.setWidth(100);

		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText(Messages.batchImportTable_idHeader);
		tableColumn.setWidth(170);

		TableColumn tableColumnAssignment = new TableColumn(table, SWT.NONE);
		tableColumnAssignment.setText(Messages.batchImportTable_assignmentHeader);

		TableColumn tableColumnAcidTemp = new TableColumn(table, SWT.NONE);
		tableColumnAcidTemp.setText(Messages.batchImportTable_acidTempHeader);

		TableColumn tableColumnGroup = new TableColumn(table, SWT.NONE);
		tableColumnGroup.setText(Messages.batchImportTable_groupHeader);

		resizeTableItems(1);
		TableItem tableItem = table.getItem(0);

		TableEditor tableEditor = (TableEditor) tableItem.getData(ASSIGNMENTS);
		tableColumnAssignment.setWidth(tableEditor.getEditor().getSize().x + 10);

		tableEditor = (TableEditor) tableItem.getData(ACID_TEMPS);
		tableColumnAcidTemp.setWidth(tableEditor.getEditor().getSize().x + 10);

		tableEditor = (TableEditor) tableItem.getData(GROUPINGS);
		tableColumnGroup.setWidth(tableEditor.getEditor().getSize().x + 10);

		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		table.addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {
				event.height = tableRowHeight;
			}
		});
	}

	public void setImportedFiles(TreeSet<ImportedFile> importedFiles) {
		this.importedFiles = importedFiles;
		refreshTable();
	}

	public void refreshTable() {
		table.setRedraw(false);

		HashSet<Long> selectedTimestamps = new HashSet<Long>();

		for (TableItem tableItem : table.getSelection()) {
			ImportedFile importedFile = (ImportedFile) tableItem.getData();
			selectedTimestamps.add(importedFile.getTimestamp());
		}

		resizeTableItems(importedFiles.size());

		int count = 0;
		for (ImportedFile importedFile : importedFiles) {
			TableItem tableItem = table.getItem(count);
			tableItem.setData(importedFile);
		}

		ArrayList<TableItem> newSelectedItems = new ArrayList<TableItem>();

		for (TableItem tableItem : table.getSelection()) {
			ImportedFile importedFile = (ImportedFile) tableItem.getData();

			if (selectedTimestamps.contains(importedFile.getTimestamp())) {
				newSelectedItems.add(tableItem);
			}
		}

		TableItem[] tableItems = newSelectedItems.toArray(new TableItem[newSelectedItems.size()]);
		table.setSelection(tableItems);

		refreshControls(importedFiles);

		table.setRedraw(true);
	}

	private void resizeTableItems(int size) {
		if (size < 1) {
			size = 1;
		}

		while (table.getItemCount() > size) {
			TableItem tableItem = table.getItem(0);

			TableEditor tableEditor = (TableEditor) tableItem.getData(ERROR_WARNING);
			tableEditor.getEditor().dispose();
			tableEditor.dispose();
			
			tableEditor = (TableEditor) tableItem.getData(IGNORES);
			tableEditor.getEditor().dispose();
			tableEditor.dispose();
			
			tableEditor = (TableEditor) tableItem.getData(ASSIGNMENTS);
			tableEditor.getEditor().dispose();
			tableEditor.dispose();

			tableEditor = (TableEditor) tableItem.getData(ACID_TEMPS);
			tableEditor.getEditor().dispose();
			tableEditor.dispose();

			tableEditor = (TableEditor) tableItem.getData(GROUPINGS);
			tableEditor.getEditor().dispose();
			tableEditor.dispose();

			tableItem.dispose();
		}

		while (table.getItemCount() < size) {
			TableItem tableItem = new TableItem(table, SWT.NONE);

			// error-warning

			ErrorWarningComposite errorWarning = new ErrorWarningComposite(table, SWT.NONE);
			errorWarning.pack();

			TableEditor editor = new TableEditor(table);
			editor.minimumWidth = errorWarning.getSize().x;
			editor.minimumHeight = errorWarning.getSize().y;
			tableRowHeight = Math.max(tableRowHeight, editor.minimumHeight);
			editor.horizontalAlignment = SWT.CENTER;
			editor.setEditor(errorWarning, tableItem, 0);
			tableItem.setData(ERROR_WARNING, editor);

			// ignore

			final Button ignoreButton = new Button(table, SWT.CHECK);
			ignoreButton.addSelectionListener(new LoggingSelectionAdaptor() {
				public void loggingWidgetSelected(SelectionEvent e) {
					((ImportedFile) getData()).setIgnore(ignoreButton.getSelection());
					listener.widgetStatusChanged();
				}
			});
			ignoreButton.pack();

			editor = new TableEditor(table);
			editor.minimumWidth = ignoreButton.getSize().x;
			editor.minimumHeight = ignoreButton.getSize().y;
			tableRowHeight = Math.max(tableRowHeight, editor.minimumHeight);
			editor.horizontalAlignment = SWT.CENTER;
			editor.setEditor(ignoreButton, tableItem, 1);
			tableItem.setData(IGNORES, editor);

			// assignment

			AssignmentComposite assignment = new AssignmentComposite(table, SWT.NONE);

			editor = new TableEditor(table);
			editor.minimumWidth = assignment.getSize().x;
			editor.minimumHeight = assignment.getSize().y;
			tableRowHeight = Math.max(tableRowHeight, editor.minimumHeight);
			editor.horizontalAlignment = SWT.LEFT;
			editor.setEditor(assignment, tableItem, 5);
			tableItem.setData(ASSIGNMENTS, editor);

			// acid temp

			Combo acidTemp = new Combo(table, SWT.READ_ONLY);
			acidTemp.addSelectionListener(new LoggingSelectionAdaptor() {
				public void loggingWidgetSelected(SelectionEvent e) {
					listener.widgetStatusChanged();
				}
			});
			acidTemp.pack();

			editor = new TableEditor(table);
			editor.minimumWidth = GuiConstants.SHORT_COMBO_INPUT_WIDTH;
			editor.minimumHeight = acidTemp.getSize().y;
			tableRowHeight = Math.max(tableRowHeight, editor.minimumHeight);
			editor.horizontalAlignment = SWT.CENTER;
			editor.setEditor(acidTemp, tableItem, 6);
			tableItem.setData(ACID_TEMPS, editor);
	
			// grouping
			
			Spinner grouping = new Spinner(table, SWT.BORDER);
			grouping.addSelectionListener(new LoggingSelectionAdaptor() {
				public void loggingWidgetSelected(SelectionEvent e) {
					listener.widgetStatusChanged();
				}
			});
			grouping.pack();

			editor = new TableEditor(table);
			editor.minimumWidth = grouping.getSize().x;
			editor.minimumHeight = grouping.getSize().y;
			tableRowHeight = Math.max(tableRowHeight, editor.minimumHeight);
			editor.horizontalAlignment = SWT.CENTER;
			editor.setEditor(grouping, tableItem, 7);
			tableItem.setData(GROUPINGS, editor);
		}
	}

	public void refreshControls(TreeSet<ImportedFile> importedFiles) {
		int count = 0;

		for (ImportedFile importedFile : importedFiles) {
			setErrorWarning(count, importedFile);
			setIgnore(count, importedFile);
			setTime(count, importedFile);
			setFileName(count, importedFile);
			setIdentifier(count, importedFile);
			setAssignment(count, importedFile);
			setAcidTemp(count, importedFile);
			setGrouping(count, importedFile);

			count++;
		}
	}

	private void setErrorWarning(int count, ImportedFile importedFile) {
		TableItem tableItem = table.getItem(count);
		TableEditor tableEditor = (TableEditor) tableItem.getData(ERROR_WARNING);
		ErrorWarningComposite errorWarning = (ErrorWarningComposite) tableEditor.getEditor();

		if (importedFile.getError() != null) {
			errorWarning.setError(importedFile.getError());

		} else if (importedFile.getWarning() != null) {
			errorWarning.setWarning(importedFile.getWarning());
		}
	}

	private void setIgnore(int count, ImportedFile importedFile) {
		TableItem tableItem = table.getItem(count);
		TableEditor tableEditor = (TableEditor) tableItem.getData(IGNORES);
		Button ignore = (Button) tableEditor.getEditor();
		ignore.setSelection(importedFile.isIgnore());
	}

	private void setTime(int count, ImportedFile importedFile) {
		TableItem tableItem = table.getItem(count);

		long date = importedFile.getTimestamp();
		String zone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
		boolean withTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
		String timestamp = DateFormat.format(date, zone, withTimeZone, false);

		tableItem.setText(2, timestamp);
	}

	private void setFileName(int count, ImportedFile importedFile) {
		TableItem tableItem = table.getItem(count);
		tableItem.setText(3, importedFile.getFileName());
	}

	private void setIdentifier(int count, ImportedFile importedFile) {
		TableItem tableItem = table.getItem(count);
		tableItem.setText(4, importedFile.getIdentifier());
	}

	private void setAssignment(int count, ImportedFile importedFile) {
		TableItem tableItem = table.getItem(count);
		TableEditor tableEditor = (TableEditor) tableItem.getData(ASSIGNMENTS);
		AssignmentComposite assignment = (AssignmentComposite) tableEditor.getEditor();
		assignment.setComboItems(importedFile.getSourceList());
		assignment.selectSampleIdStandardId(importedFile.getSampleId(), importedFile.getStandardId());
	}

	private void setAcidTemp(int count, ImportedFile importedFile) {
		//TableItem tableItem = table.getItem(count);
		//TableEditor tableEditor = (TableEditor) tableItem.getData(ACID_TEMPS);
		//Combo acidTemp = (Combo) tableEditor.getEditor();
		//acidTemp.setSelection(importedFile.getGroup());		
	}

	private void setGrouping(int count, ImportedFile importedFile) {
		TableItem tableItem = table.getItem(count);
		TableEditor tableEditor = (TableEditor) tableItem.getData(GROUPINGS);
		Spinner group = (Spinner) tableEditor.getEditor();
		group.setSelection(importedFile.getGroup());		
	}
}
