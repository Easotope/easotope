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

package org.easotope.client.core.scratchpadtable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ProjectPad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.core.scratchpad.UserPad;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.StructuralRefreshEvent;
import org.eclipse.nebula.widgets.nattable.layer.event.VisualRefreshEvent;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

class ScratchPadBodyLayerStack extends AbstractLayerTransform {
	private Pad[] pads;

	private ScratchPadBodyDataProvider bodyDataProvider;
	private DataLayer bodyDataLayer;
	private ColumnReorderLayer columnReorderLayer;
	private ColumnHideShowLayer columnHideShowLayer;
	private SelectionLayer selectionLayer;
	private TreeLayer treeLayer;
	private ViewportLayer viewportLayer;

	ScratchPadBodyLayerStack(boolean dataAnalysisIsEditable, HashMap<Integer,RepAnalysisChoice> replicateIdToRepAnalysisChoice) {
		bodyDataProvider = new ScratchPadBodyDataProvider(replicateIdToRepAnalysisChoice);

		bodyDataLayer = new DataLayer(bodyDataProvider);

		if (dataAnalysisIsEditable) {
			ColumnOverrideLabelAccumulator columnOverrideLabelAccumulator = new ColumnOverrideLabelAccumulator(bodyDataLayer);
			bodyDataLayer.setConfigLabelAccumulator(columnOverrideLabelAccumulator);
			columnOverrideLabelAccumulator.registerColumnOverrides(1, Pad.ANALYSIS);
		} else {
			bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
		}

		columnReorderLayer = new ColumnReorderLayer(bodyDataLayer);
		columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
		selectionLayer = new SelectionLayer(columnHideShowLayer);
		treeLayer = new TreeLayer(selectionLayer, bodyDataProvider, false);
		treeLayer.addConfiguration(new ScratchPadTreeLayerConfiguration());
		ScratchPadCopyDataCommandHandler scratchPadCopyDataCommandHandler = new ScratchPadCopyDataCommandHandler(selectionLayer, treeLayer);
		scratchPadCopyDataCommandHandler.setCopyLayer(treeLayer);
		treeLayer.registerCommandHandler(scratchPadCopyDataCommandHandler);
		viewportLayer = new ViewportLayer(treeLayer);

		setUnderlyingLayer(viewportLayer);
	}

	public ScratchPadBodyDataProvider getBodyDataProvider() {
		return bodyDataProvider;
	}

	SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	ColumnHideShowLayer getColumnHideShowLayer() {
		return columnHideShowLayer;
	}

	void setScratchPad(Pad[] pads, String[] columnNamesInOrder, FormatLookup formatLookup) {
		this.pads = pads;
		bodyDataProvider.setScratchPad(pads, columnNamesInOrder, formatLookup);
		columnHideShowLayer.showAllColumns();
		treeLayer.expandAll();
		bodyDataLayer.fireLayerEvent(new StructuralRefreshEvent(bodyDataLayer));
	}

	void updateScratchPad(Pad[] pads) {
		this.pads = pads;
		bodyDataProvider.updateScratchPad(pads);
		bodyDataLayer.fireLayerEvent(new VisualRefreshEvent(bodyDataLayer));
	}

	List<Integer> getColumnIndexOrder() {
		return columnReorderLayer.getColumnIndexOrder();
	}

	int[] getColumnWidths() {
		int[] columnWidth = new int[bodyDataLayer.getColumnCount()];

		for (int i=0; i<bodyDataLayer.getColumnCount(); i++) {
			columnWidth[i] = bodyDataLayer.getColumnWidthByPosition(i);
		}

		return columnWidth;
	}

	HashSet<Integer> getHiddenIndices() {
		return new HashSet<Integer>(columnHideShowLayer.getHiddenColumnIndexes());
	}

	void hideColumnsBeforeAfter(int numberOfInitialGeneralColumns, int numberOfColumnsToHide) {
		ArrayList<Integer> positionsToHide = new ArrayList<Integer>();

		for (int i=numberOfInitialGeneralColumns; i<numberOfColumnsToHide; i++) {
			positionsToHide.add(i);
		}

		columnHideShowLayer.hideColumnPositions(positionsToHide);
	}

	void setInitialColumnOrderAndWidths(HashMap<String,Integer> columnNameToIndex, String[] columnOrder, int[] columnWidth) {
		int howManyExist = 0;
		int columnCount = columnNameToIndex.size();

		for (int i=0; i<columnOrder.length; i++) {
			if (!columnNameToIndex.containsKey(columnOrder[i])) {
				continue;
			}

			howManyExist++;

			int columnIndex = columnNameToIndex.get(columnOrder[i]);
			this.bodyDataLayer.setColumnWidthByPosition(columnIndex, columnWidth[i]);

			int columnPosition = columnReorderLayer.getColumnPositionByIndex(columnIndex);
			columnReorderLayer.reorderColumnPosition(columnPosition, columnCount);
		}

		int totalHidden = columnCount - howManyExist;
		hideColumnsBeforeAfter(0, totalHidden);
	}

	void hideProjects() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof UserPad && !treeLayer.isRowIndexHidden(i)) {
				treeLayer.collapseTreeRow(i);
			}
		}
	}

	void showAllProjects() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof UserPad && !treeLayer.isRowIndexHidden(i)) {
				treeLayer.expandTreeRow(i);
			}
		}
	}

	void hideSamples() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof ProjectPad && !treeLayer.isRowIndexHidden(i)) {
				treeLayer.collapseTreeRow(i);
			}
		}
	}

	void showAllSamples() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof ProjectPad && !treeLayer.isRowIndexHidden(i)) {
				treeLayer.expandTreeRow(i);
			}
		}
	}

	void hideReplicates() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof SamplePad && !treeLayer.isRowIndexHidden(i)) {
				treeLayer.collapseTreeRow(i);
			}
		}
	}

	void showAllReplicates() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof SamplePad && !treeLayer.isRowIndexHidden(i)) {
				treeLayer.expandTreeRow(i);
			}
		}
	}

	void hideAcquisitions() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof ReplicatePad && !treeLayer.isRowIndexHidden(i)) {
				treeLayer.collapseTreeRow(i);
			}
		}
	}

	void showAllAcquisitions() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof ReplicatePad && !treeLayer.isRowIndexHidden(i)) {
				treeLayer.expandTreeRow(i);
			}
		}
	}

	boolean allAcquisitionsAreHidden() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof AcquisitionPad && !treeLayer.isRowIndexHidden(i)) {
				return false;
			}
		}

		return true;
	}

	void hideCycles() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof AcquisitionPad && !treeLayer.isRowIndexHidden(i)) {
				treeLayer.collapseTreeRow(i);
			}
		}
	}

	void showAllCycles() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof AcquisitionPad && !treeLayer.isRowIndexHidden(i)) {
				treeLayer.expandTreeRow(i);
			}
		}
	}

	boolean allCyclesAreHidden() {
		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof CyclePad && !treeLayer.isRowIndexHidden(i)) {
				return false;
			}
		}

		return true;
	}

	boolean isFormattingOn() {
		return bodyDataProvider.isFormattingOn();
	}

	public void setFormattingOn(boolean formattingOn) {
		bodyDataProvider.setFormattingOn(formattingOn);
		bodyDataLayer.fireLayerEvent(new VisualRefreshEvent(bodyDataLayer));
	}

	public void setReplicateIds(int[] replicateIds) {
		bodyDataProvider.setReplicateIds(replicateIds);		
	}
}
