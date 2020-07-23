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

package org.easotope.client.core.scratchpadtable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import org.easotope.client.Messages;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ProjectPad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.core.scratchpad.UserPad;
import org.easotope.shared.core.tables.TableLayout;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommand;
import org.eclipse.nebula.widgets.nattable.columnChooser.command.DisplayColumnChooserCommandHandler;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AggregateConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.export.ExportConfigAttributes;
import org.eclipse.nebula.widgets.nattable.export.command.ExportCommand;
import org.eclipse.nebula.widgets.nattable.export.config.DefaultExportBindings;
import org.eclipse.nebula.widgets.nattable.extension.poi.HSSFExcelExporter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.command.ShowAllColumnsCommand;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.CellVisualChangeEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.tooltip.NatTableContentTooltip;
import org.eclipse.nebula.widgets.nattable.ui.menu.AbstractHeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class ScratchPadTable extends Composite {
	private enum TableCommand { Export, ShowAllColumns, ShowColumnSelector, SaveTableLayout, TurnFormattingOn, TurnFormattingOff, HideProjects, ShowProjects, HideSamples, ShowSamples, HideReplicates, ShowReplicates, HideAcquisitions, ShowAcquisitions, HideCycles, ShowCycles };
	private HashMap<Integer,TableCommand> indexToTableCommand = new HashMap<Integer,TableCommand>();

	private Combo tableCommands;
	private NatTable natTable;

	boolean tableHasUserPads = false;
	boolean tableHasProjectPads = false;
	boolean tableHasSamplePads = false;
	boolean tableHasReplicatePads = false;
	boolean tableHasAcquisitionPads = false;
	boolean tableHasCyclePads = false;

	private ScratchPadBodyLayerStack bodyLayerStack;
	private ScratchPadColumnLayerStack columnLayerStack;
	private ScratchPadRowLayerStack rowLayerStack;
	private ScratchPadCornerLayerStack cornerLayerStack;

	private boolean canSaveTableLayout = true;
	private String[] columnNamesInDefaultOrder;
	private int numberOfInitialGeneralColumns;
	private int numberOfInitialDefaultHiddenColumns;

	ArrayList<ArrayList<RepAnalysisChoice>> repAnalysisChoices = null;
	HashMap<Integer,RepAnalysisChoice> replicateIdToRepAnalysisChoice = new HashMap<Integer,RepAnalysisChoice>();

	private Vector<ScratchPadTableListener> listeners = new Vector<ScratchPadTableListener>();

	public ScratchPadTable(Composite parent, boolean dataAnalysisIsEditable) {
		super(parent, SWT.NONE);
		setLayout(new FormLayout());

		Composite leftComposite = new Composite(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		leftComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 0;
		leftComposite.setLayout(gridLayout);

		Label label = new Label(leftComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.CENTER;
		label.setLayoutData(gridData);
		label.setText(Messages.scratchPadTable_tableCommandsLabel);

		tableCommands = new Combo(leftComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.CENTER;
		tableCommands.setLayoutData(gridData);
		tableCommands.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				int selectionIndex = tableCommands.getSelectionIndex();
				tableCommands.deselectAll();
				executeMenuCommand(selectionIndex);
			}
		});

		bodyLayerStack = new ScratchPadBodyLayerStack(dataAnalysisIsEditable, replicateIdToRepAnalysisChoice);
		columnLayerStack = new ScratchPadColumnLayerStack(bodyLayerStack);
		rowLayerStack = new ScratchPadRowLayerStack(bodyLayerStack);
		cornerLayerStack = new ScratchPadCornerLayerStack(columnLayerStack, rowLayerStack);

		GridLayer gridLayer = new GridLayer(bodyLayerStack, columnLayerStack, rowLayerStack, cornerLayerStack, false);
		gridLayer.addConfiguration(new GridLayerConfiguration(gridLayer));
		natTable = new NatTable(this, gridLayer, false);

		formData = new FormData();
		formData.top = new FormAttachment(leftComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		natTable.setLayoutData(formData);
		DefaultNatTableStyleConfiguration defaultNatTableStyleConfiguration = new DefaultNatTableStyleConfiguration();
		defaultNatTableStyleConfiguration.hAlign = HorizontalAlignmentEnum.LEFT;
		natTable.addConfiguration(defaultNatTableStyleConfiguration);
		natTable.addConfiguration(new AbstractHeaderMenuConfiguration(natTable) {
			@Override
			protected PopupMenuBuilder createColumnHeaderMenu(NatTable natTable) {
				return super.createColumnHeaderMenu(natTable).withHideColumnMenuItem();
			}
		});
		natTable.addConfiguration(new ExportConfiguration());
		if (dataAnalysisIsEditable) {
			natTable.addConfiguration(new EditorConfiguration());
		}
		natTable.addConfiguration(new PainterConfiguration());
		natTable.registerCommandHandler(new DisplayColumnChooserCommandHandler(
			bodyLayerStack.getSelectionLayer(),
			bodyLayerStack.getColumnHideShowLayer(),
			columnLayerStack.getColumnHeaderLayer(),
			columnLayerStack.getColumnHeaderDataLayer(),
			null,
			null
		));
		natTable.addLayerListener(new ILayerListener() {
			@Override
			public void handleLayerEvent(ILayerEvent event) {
				if (event instanceof CellVisualChangeEvent && listeners != null) {
					for (ScratchPadTableListener listener : listeners) {
						listener.tableModified();
					}
				}
			}
		});
		natTable.configure();

		natTable.setBackground(GUIHelper.COLOR_WHITE);
		natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
		new NatTableContentTooltip(natTable, GridRegion.BODY);

		setScratchPad(new ScratchPad<Pad>(), null, null, null);
	}

	public void setScratchPad(ScratchPad<?> scratchPad, ColumnOrdering columnOrdering, FormatLookup formatLookup, TableLayout tableLayout) {
		setScratchPad(scratchPad, columnOrdering, formatLookup, tableLayout, null, null);
	}

	public void setScratchPad(ScratchPad<?> scratchPad, ColumnOrdering columnOrdering, FormatLookup formatLookup, TableLayout tableLayout, int[] replicateIds, ArrayList<ArrayList<RepAnalysisChoice>> repAnalysisChoices) {
		replicateIdToRepAnalysisChoice.clear();

		ArrayList<Pad> allPads = scratchPad.getAllPads();
		Pad[] pads = allPads.toArray(new Pad[allPads.size()]);

		tableHasUserPads = false;
		tableHasProjectPads = false;
		tableHasSamplePads = false;
		tableHasReplicatePads = false;
		tableHasAcquisitionPads = false;
		tableHasCyclePads = false;

		for (Pad pad : pads) {
			if (pad instanceof CyclePad) {
				tableHasCyclePads = true;

			} else if (pad instanceof AcquisitionPad) {
				tableHasAcquisitionPads = true;

			} else if (pad instanceof ReplicatePad) {
				tableHasReplicatePads = true;
				
			} else if (pad instanceof SamplePad) {
				tableHasSamplePads = true;
				
			} else if (pad instanceof ProjectPad) {
				tableHasProjectPads = true;
				
			} else if (pad instanceof UserPad) {
				tableHasUserPads = true;
			}
		}

		generateDefaultColumnInfo(scratchPad, columnOrdering == null ? null : columnOrdering.getOrdering());

		columnLayerStack.setColumnNames(columnNamesInDefaultOrder);
		rowLayerStack.setPads(pads);
		bodyLayerStack.setScratchPad(pads, columnNamesInDefaultOrder, formatLookup);

		if (tableLayout == null) {
			bodyLayerStack.hideColumnsBeforeAfter(numberOfInitialGeneralColumns, numberOfInitialDefaultHiddenColumns);

		} else {
			HashMap<String,Integer> columnNameToIndex = new HashMap<String,Integer>();

			for (int i=0; i<columnNamesInDefaultOrder.length; i++) {
				columnNameToIndex.put(columnNamesInDefaultOrder[i], i);
			}

			bodyLayerStack.setFormattingOn(tableLayout.isFormattingOn());
			bodyLayerStack.setInitialColumnOrderAndWidths(columnNameToIndex, tableLayout.getColumnOrder(), tableLayout.getColumnWidth());

			if (tableLayout.isHideCycles()) {
				bodyLayerStack.hideCycles();
			}

			if (tableLayout.isHideAcquisitions()) {
				bodyLayerStack.hideAcquisitions();
			}
		}

		this.bodyLayerStack.setReplicateIds(replicateIds);
		this.repAnalysisChoices = repAnalysisChoices;

		setUpTableCommandMenu();
	}

	private void generateDefaultColumnInfo(ScratchPad<?> scratchPad, List<String> columnOrder) {
		if (scratchPad == null) {
			columnNamesInDefaultOrder = new String[0];
			numberOfInitialGeneralColumns = 0;
			numberOfInitialDefaultHiddenColumns = 0;
			return;
		}

		TreeSet<String> scratchPadColumns = scratchPad.getAllColumns();
		ArrayList<String> tempColumnOrder = new ArrayList<String>();
		ArrayList<String> stepColumns = new ArrayList<String>();

		numberOfInitialGeneralColumns = 0;

		if (scratchPadColumns.remove(Pad.ID)) {
			tempColumnOrder.add(Pad.ID);
			numberOfInitialGeneralColumns++;
		}

		if (scratchPadColumns.remove(Pad.ANALYSIS)) {
			tempColumnOrder.add(Pad.ANALYSIS);
			numberOfInitialGeneralColumns++;
		}

		if (scratchPadColumns.remove(Pad.ANALYSIS_STATUS)) {
			tempColumnOrder.add(Pad.ANALYSIS_STATUS);
			numberOfInitialGeneralColumns++;
		}

		if (scratchPadColumns.remove(Pad.DISABLED)) {
			tempColumnOrder.add(Pad.DISABLED);
			numberOfInitialGeneralColumns++;
		}

		if (scratchPadColumns.remove(Pad.OFF_PEAK)) {
			tempColumnOrder.add(Pad.OFF_PEAK);
			numberOfInitialGeneralColumns++;
		}

		if (columnOrder != null) {
			for (String stepGeneratedColumn : columnOrder) {
				if (scratchPadColumns.remove(stepGeneratedColumn)) {
					stepColumns.add(stepGeneratedColumn);
				}
			}
		}

		tempColumnOrder.addAll(scratchPadColumns);
		tempColumnOrder.addAll(stepColumns);
		columnNamesInDefaultOrder = tempColumnOrder.toArray(new String[tempColumnOrder.size()]);
		numberOfInitialDefaultHiddenColumns = scratchPadColumns.size();
	}

	public void setCanSaveTableLayout(boolean canSaveTableLayout) {
		this.canSaveTableLayout = canSaveTableLayout;
		setUpTableCommandMenu();
	}

	private void setUpTableCommandMenu() {
		tableCommands.removeAll();
		indexToTableCommand.clear();

		tableCommands.add(Messages.scratchPadTable_emptyCommand);

		indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.ShowAllColumns);
		tableCommands.add(Messages.scratchPadTable_showAllColumns);
		indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.ShowColumnSelector);
		tableCommands.add(Messages.scratchPadTable_showColumnSelector);

		if (canSaveTableLayout) {
			tableCommands.add(Messages.scratchPadTable_separator);

			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.SaveTableLayout);
			tableCommands.add(Messages.scratchPadTable_saveTableLayout);
		}

		tableCommands.add(Messages.scratchPadTable_separator);

		if (bodyLayerStack.isFormattingOn()) {
			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.TurnFormattingOff);
			tableCommands.add(Messages.scratchPadTable_disableFormatting);
		} else {
			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.TurnFormattingOn);
			tableCommands.add(Messages.scratchPadTable_enableFormatting);
		}

		tableCommands.add(Messages.scratchPadTable_separator);

		indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.Export);
		tableCommands.add(Messages.scratchPadTable_export);
		
		if (tableHasUserPads && tableHasProjectPads) {
			tableCommands.add(Messages.scratchPadTable_separator);

			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.HideProjects);
			tableCommands.add(Messages.scratchPadTable_hideProjects);
			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.ShowProjects);
			tableCommands.add(Messages.scratchPadTable_showProjects);
		}

		if (tableHasProjectPads && tableHasSamplePads) {
			tableCommands.add(Messages.scratchPadTable_separator);

			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.HideSamples);
			tableCommands.add(Messages.scratchPadTable_hideSamples);
			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.ShowSamples);
			tableCommands.add(Messages.scratchPadTable_showSamples);
		}

		if (tableHasSamplePads && tableHasReplicatePads) {
			tableCommands.add(Messages.scratchPadTable_separator);

			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.HideReplicates);
			tableCommands.add(Messages.scratchPadTable_hideReplicates);
			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.ShowReplicates);
			tableCommands.add(Messages.scratchPadTable_showReplicates);
		}

		if (tableHasReplicatePads && tableHasAcquisitionPads) {
			tableCommands.add(Messages.scratchPadTable_separator);

			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.HideAcquisitions);
			tableCommands.add(Messages.scratchPadTable_hideAcquisitions);
			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.ShowAcquisitions);
			tableCommands.add(Messages.scratchPadTable_showAcquisitions);
		}

		if (tableHasAcquisitionPads && tableHasCyclePads) {
			tableCommands.add(Messages.scratchPadTable_separator);

			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.HideCycles);
			tableCommands.add(Messages.scratchPadTable_hideCycles);
			indexToTableCommand.put(tableCommands.getItemCount(), TableCommand.ShowCycles);
			tableCommands.add(Messages.scratchPadTable_showCycles);
		}
	}

	private void executeMenuCommand(int selectionIndex) {
		if (!indexToTableCommand.containsKey(selectionIndex)) {
			return;
		}

		switch (indexToTableCommand.get(selectionIndex)) {
			case ShowAllColumns:
				executeShowAllColumns();
				break;
				
			case ShowColumnSelector:
				executeShowColumnSelector();
				break;

			case SaveTableLayout:
				executeSaveLayout();
				break;

			case TurnFormattingOn:
				executeTurnFormattingOn();
				break;

			case TurnFormattingOff:
				executeTurnFormattingOff();
				break;

			case Export:
				executeExport();

			case HideProjects:
				executeHideProjects();
				break;
				
			case ShowProjects:
				executeShowProjects();
				break;

			case HideSamples:
				executeHideSamples();
				break;
				
			case ShowSamples:
				executeShowSamples();
				break;

			case HideReplicates:
				executeHideReplicates();
				break;

			case ShowReplicates:
				executeShowReplicates();
				break;

			case HideAcquisitions:
				executeHideAcquisitions();
				break;
				
			case ShowAcquisitions:
				executeShowAcquisitions();
				break;

			case HideCycles:
				executeHideCycles();
				break;

			case ShowCycles:
				executeShowCycles();
				break;
		}
	}

	private void executeShowAllColumns() {
		natTable.doCommand(new ShowAllColumnsCommand());		
	}

	private void executeShowColumnSelector() {
		natTable.doCommand(new DisplayColumnChooserCommand(natTable));
	}

	private void executeSaveLayout() {
		TableLayout tableLayout = new TableLayout();

		List<Integer> columnIndexOrder = bodyLayerStack.getColumnIndexOrder();
		int[] allColumnWidths = bodyLayerStack.getColumnWidths();
		HashSet<Integer> hiddenIndices = bodyLayerStack.getHiddenIndices();

		ArrayList<String> visibleColumnNames = new ArrayList<String>();
		ArrayList<Integer> visibleColumnWidths = new ArrayList<Integer>();

		for (Integer columnIndex : columnIndexOrder) {
			if (!hiddenIndices.contains(columnIndex)) {
				visibleColumnNames.add(columnNamesInDefaultOrder[columnIndex]);
				visibleColumnWidths.add(allColumnWidths[columnIndex]);
			}
		}

		String[] columnOrder = visibleColumnNames.toArray(new String[visibleColumnNames.size()]);
		tableLayout.setColumnOrder(columnOrder);

		int[] columnWidth = new int[visibleColumnWidths.size()];

		int count = 0;
		for (int i : visibleColumnWidths) {
			columnWidth[count++] = i;
		}

		tableLayout.setColumnWidth(columnWidth);

		tableLayout.setFormattingOn(bodyLayerStack.isFormattingOn());
		tableLayout.setHideReplicates(false);
		tableLayout.setHideAcquisitions(bodyLayerStack.allAcquisitionsAreHidden());
		tableLayout.setHideCycles(bodyLayerStack.allCyclesAreHidden());

		for (ScratchPadTableListener listener : listeners) {
			listener.tableLayoutNeedsSaving(tableLayout);
		}
	}

	private void executeTurnFormattingOn() {
		bodyLayerStack.setFormattingOn(true);
		setUpTableCommandMenu();
	}

	private void executeTurnFormattingOff() {
		bodyLayerStack.setFormattingOn(false);
		setUpTableCommandMenu();
	}

	private void executeExport() {
		natTable.doCommand(new ExportCommand(natTable.getConfigRegistry(), natTable.getShell()));
	}

	private void executeHideProjects() {
		bodyLayerStack.hideProjects();
	}

	private void executeShowProjects() {
		bodyLayerStack.showAllProjects();
	}

	private void executeHideSamples() {
		bodyLayerStack.hideSamples();
	}

	private void executeShowSamples() {
		bodyLayerStack.showAllSamples();
	}

	private void executeHideReplicates() {
		bodyLayerStack.hideReplicates();
	}

	private void executeShowReplicates() {
		bodyLayerStack.showAllReplicates();
	}

	private void executeHideAcquisitions() {
		bodyLayerStack.hideAcquisitions();
	}

	private void executeShowAcquisitions() {
		bodyLayerStack.showAllAcquisitions();
	}

	private void executeHideCycles() {
		bodyLayerStack.hideCycles();
	}

	private void executeShowCycles() {
		bodyLayerStack.showAllCycles();
	}

	public void updateScratchPad(ScratchPad<?> scratchPad) {
		ArrayList<Pad> allPads = scratchPad.getAllPads();
		Pad[] pads = allPads.toArray(new Pad[allPads.size()]);

		tableHasUserPads = false;
		tableHasProjectPads = false;
		tableHasSamplePads = false;
		tableHasReplicatePads = false;
		tableHasAcquisitionPads = false;
		tableHasCyclePads = false;

		for (Pad pad : pads) {
			if (pad instanceof CyclePad) {
				tableHasCyclePads = true;

			} else if (pad instanceof AcquisitionPad) {
				tableHasAcquisitionPads = true;

			} else if (pad instanceof ReplicatePad) {
				tableHasReplicatePads = true;
				
			} else if (pad instanceof SamplePad) {
				tableHasSamplePads = true;
				
			} else if (pad instanceof ProjectPad) {
				tableHasProjectPads = true;
				
			} else if (pad instanceof UserPad) {
				tableHasUserPads = true;
			}
		}

		bodyLayerStack.updateScratchPad(pads);
	}

	public void addListener(ScratchPadTableListener resultsViewerPart) {
		listeners.add(resultsViewerPart);
	}

	public void removeListener(ScratchPadTableListener listener) {
		listeners.remove(listener);
	}

	public class GridLayerConfiguration extends AggregateConfiguration {
	    public GridLayerConfiguration(CompositeLayer gridLayer) {
	        addConfiguration(new DefaultEditConfiguration());
	        addConfiguration(new DefaultEditBindings());
	        addConfiguration(new DefaultExportBindings());
	    }
	}

	class ExportConfiguration extends AbstractRegistryConfiguration {
		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			configRegistry.registerConfigAttribute(
					ExportConfigAttributes.EXPORTER,
					new HSSFExcelExporter());

	        configRegistry.registerConfigAttribute(
	        		ExportConfigAttributes.EXPORT_FORMATTER,
	        		new ScratchPadExportFormatter());

//	        configRegistry.registerConfigAttribute(
//	        		ExportConfigAttributes.DATE_FORMAT, "yyyy-MM-dd HH:mm");
		}
	}

	class EditorConfiguration extends AbstractRegistryConfiguration  {
		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, new RepAnalysisEditableRule(), DisplayMode.NORMAL, Pad.ANALYSIS);

			configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITOR, new ComboBoxCellEditor(new DataAnalysisListDataProvider(), -1), DisplayMode.EDIT, Pad.ANALYSIS);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, new ComboBoxPainter(), DisplayMode.NORMAL, Pad.ANALYSIS);
		}
	}

	class PainterConfiguration extends AbstractRegistryConfiguration {
		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			StandardDeviationPainter painter = new StandardDeviationPainter(ScratchPadTable.this);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, painter);
		}
	}

	class DataAnalysisListDataProvider implements IComboBoxDataProvider {
		@Override
		public List<?> getValues(int columnIndex, int rowIndex) {
			if (repAnalysisChoices == null || repAnalysisChoices.size() <= rowIndex-1) {
				return new ArrayList<String>();
			}

			return repAnalysisChoices.get(rowIndex-1);
		}
	}

	class StandardDeviationPainter extends TextPainter {
		@SuppressWarnings("unused")
		private ScratchPadTable scratchPadTable;

		StandardDeviationPainter(ScratchPadTable scratchPadTable) {
			this.scratchPadTable = scratchPadTable;
		}

		@Override
		protected Color getBackgroundColour(ILayerCell cell, IConfigRegistry configRegistry) {
//			int columnIndex = cell.getColumnIndex();
//			int rowIndex = cell.getRowIndex();
//
//			ScratchPadBodyDataProvider dataProvider = scratchPadTable.bodyLayerStack.getBodyDataProvider();
//			Object o = dataProvider.getDataValue(columnIndex, rowIndex);
//
//			if (o instanceof String && ((String) o).equals("0.00")) {
//				return GUIHelper.getColor(255, 0, 0);
//			}

			return super.getBackgroundColour(cell, configRegistry);
		}
	}

	public boolean isDirty() {
		return !replicateIdToRepAnalysisChoice.isEmpty();
	}

	public HashMap<Integer,RepAnalysisChoice> getReplicateIdToRepAnalysisChoice() {
		return replicateIdToRepAnalysisChoice;
	}
}
