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
import java.util.List;

import org.easotope.shared.core.DoubleTools;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.Accumulator;
import org.easotope.shared.core.scratchpad.AccumulatorStdDevSample;
import org.easotope.shared.core.scratchpad.AccumulatorStdErr;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.AnalysisIdentifier.Level;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.core.tables.Preferences;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;

class ScratchPadBodyDataProvider implements IDataProvider, ITreeRowModel<Pad> {
	private final boolean DEFAULT_IS_COLLAPSED = false;

	private Pad[] pads = new Pad[0];
	private HashMap<Pad,Integer> padToIndex = new HashMap<Pad,Integer>();
	private int[] depth = new int[0];
	private boolean[] isCollapsed = new boolean[0];
	private String[] columnNamesInOrder = new String[0];
	private boolean formattingOn = true;
	private FormatLookup formatLookup = null;
	private int[] replicateIds = null;
	private HashMap<Integer,RepAnalysisChoice> replicateIdToRepAnalysisChoice;

	public ScratchPadBodyDataProvider(HashMap<Integer,RepAnalysisChoice> replicateIdToRepAnalysisChoice) {
		this.replicateIdToRepAnalysisChoice = replicateIdToRepAnalysisChoice;
	}

	void setScratchPad(Pad[] pads, String[] columnNamesInOrder, FormatLookup formatLookup) {
		updateScratchPad(pads);

		depth = new int[pads.length];

		for (int i=0; i<depth.length; i++) {
			depth[i] = pads[i].getDepth();
		}

		isCollapsed = new boolean[pads.length];

		for (int i=0; i<isCollapsed.length; i++) {
			isCollapsed[i] = DEFAULT_IS_COLLAPSED;
		}

		this.columnNamesInOrder = columnNamesInOrder;
		this.formatLookup = formatLookup;
	}

	void updateScratchPad(Pad[] pads) {
		this.pads = pads;

		padToIndex.clear();

		for (int i=0; i<pads.length; i++) {
			padToIndex.put(pads[i], i);
		}
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		if (Pad.ID.equals(columnNamesInOrder[columnIndex])) {
			return pads[rowIndex].getPrintableIdentifier();
		}

		if (rowIndex != 0 && replicateIds != null && replicateIds.length > rowIndex-1) {
			int replicateId = replicateIds[rowIndex-1];

			if (replicateIdToRepAnalysisChoice.containsKey(replicateId)) {
				if (Pad.ANALYSIS.equals(columnNamesInOrder[columnIndex])) {
					RepAnalysisChoice repAnalysisChoice = replicateIdToRepAnalysisChoice.get(replicateId);
					return repAnalysisChoice.getRepAnalysisName();
				} else {
					return "";
				}
			}
		}

		Object object = pads[rowIndex].getValue(columnNamesInOrder[columnIndex]);

		if (object == null) {
			return "";
		}

		Level level = null;
		String analysis = null;
		Pad replicateLevelPad = null;

		if (pads[rowIndex] instanceof SamplePad) {
			level = Level.SAMPLE;
			analysis = (String) pads[rowIndex].getValue(Pad.ANALYSIS);

		} else if (pads[rowIndex] instanceof ReplicatePad) {
			level = Level.REPLICATE;
			replicateLevelPad = pads[rowIndex];
			analysis = (String) pads[rowIndex].getValue(Pad.ANALYSIS);

		} else if (pads[rowIndex] instanceof AcquisitionPad) {
			level = Level.REPLICATE;
			replicateLevelPad = pads[rowIndex].getParent();
			analysis = (String) replicateLevelPad.getValue(Pad.ANALYSIS);

		} else if (pads[rowIndex] instanceof CyclePad) {
			level = Level.REPLICATE;
			replicateLevelPad = pads[rowIndex].getParent().getParent();
			analysis = (String) replicateLevelPad.getValue(Pad.ANALYSIS);
		}

		String format = null;

		if (level != null && analysis != null && formatLookup != null) {
			format = formatLookup.getFormat(level, analysis, columnNamesInOrder[columnIndex]);

			if (format == null && level == Level.REPLICATE) {
				Pad sampleLevelPad = replicateLevelPad.getParent();
				String sampleLevelAnalysis = (String) sampleLevelPad.getValue(Pad.ANALYSIS);
				format = formatLookup.getFormat(Level.SAMPLE, sampleLevelAnalysis, columnNamesInOrder[columnIndex]);
			}
		}

		Double doubleToFormat = null;

		if (object instanceof Double) {
			doubleToFormat = (Double) object;

		} else if (object instanceof Accumulator) {
			doubleToFormat = ((Accumulator) object).getAccumulatedValues()[0];

		} else if (object instanceof AccumulatorStdDevSample) {
			doubleToFormat = ((AccumulatorStdDevSample) object).getValue();

		} else if (object instanceof AccumulatorStdErr) {
			doubleToFormat = ((AccumulatorStdErr) object).getValue();
		}

		if (doubleToFormat != null) {
			if (Double.isNaN(doubleToFormat)) {
				object = Double.NaN;

			} else if (Double.isInfinite(doubleToFormat)) {
				if (doubleToFormat > 0.0d) {
					object = Double.POSITIVE_INFINITY;
				} else {
					object = Double.NEGATIVE_INFINITY;
				}

			} else if (formattingOn && format != null) {
				object = DoubleTools.format(doubleToFormat, format);

			} else {
				Preferences preferences = LoginInfoCache.getInstance().getPreferences();
				boolean leadingExponent = preferences.getLeadingExponent();
				boolean forceExponent = preferences.getForceExponent();
				object = DoubleTools.format(doubleToFormat, leadingExponent, forceExponent);
			}
		}

		return object;
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		if (newValue != null && replicateIds != null && replicateIds.length > rowIndex-1) {
			int replicateId = replicateIds[rowIndex-1];
			Object object = getDataValue(columnIndex, rowIndex);

			if (object != null && newValue.toString().equals(object.toString())) {
				replicateIdToRepAnalysisChoice.remove(replicateId);
			} else {
				replicateIdToRepAnalysisChoice.put(replicateId, (RepAnalysisChoice) newValue);
			}
		}
	}

	void setReplicateIds(int[] replicateIds) {
		this.replicateIds = replicateIds;
	}

	@Override
	public int getColumnCount() {
		return columnNamesInOrder.length;
	}

	@Override
	public int getRowCount() {
		return pads.length;
	}

	@Override
	public int depth(int index) {
		return depth[index];
	}

	@Override
	public boolean isLeaf(int index) {
		return !hasChildren(index);
	}

	@Override
	public String getObjectAtIndexAndDepth(int index, int depth) {
		return (String) pads[index].getValue(Pad.ID);
	}

	@Override
	public boolean hasChildren(int index) {
		return pads[index].getChildPads().size() != 0;
	}

	@Override
	public boolean isCollapsed(int index) {
		return isCollapsed[index];
	}

	@Override
	public boolean isCollapsed(Pad object) {
		return isCollapsed(padToIndex.get(object));
	}

	@Override
	public boolean isCollapsible(int index) {
		return hasChildren(index);
	}

	@Override
	public List<Integer> collapse(int parentIndex) {
		isCollapsed[parentIndex] = true;
		return getChildIndexes(parentIndex);
	}

	@Override
	public List<Integer> collapse(Pad object) {
		return collapse(padToIndex.get(object));
	}

	@Override
	public List<Integer> collapseAll() {
		List<Integer> collapsedChildren = new ArrayList<Integer>();

		for (int i=isCollapsed.length-1; i>=0; i--) {
			if (hasChildren(i) && !isCollapsed(i)) {
				collapsedChildren.addAll(collapse(i));
			}
		}

		return collapsedChildren;
	}

	@Override
	public List<Integer> expand(int parentIndex) {
		isCollapsed[parentIndex] = false;
		return getChildIndexes(parentIndex);
	}

	@Override
	public List<Integer> expandToLevel(int parentIndex, int level) {
		return null;
	}

	@Override
	public List<Integer> expand(Pad object) {
		return expand(padToIndex.get(object));
	}

	@Override
	public List<Integer> expandToLevel(Pad object, int level) {
		return null;
	}

	@Override
	public List<Integer> expandAll() {
		ArrayList<Integer> children = new ArrayList<Integer>();

		for (int index=0; index<isCollapsed.length; index++) {
			if (isCollapsed[index]) {
				children.addAll(getChildIndexes(index));
			}
			isCollapsed[index] = false;
		}

		return children;
	}

	@Override
	public List<Integer> expandToLevel(int level) {
		return null;
	}

	@Override
	public List<Integer> getChildIndexes(int parentIndex) {
		List<Integer> result = new ArrayList<Integer>();

		for (Pad child : getDirectChildren(parentIndex)) {
			int index = padToIndex.get(child);

			result.add(index);

			if (!isCollapsed[index]) {
				result.addAll(getChildIndexes(index));
			}
		}

		return result;
	}

	@Override
	public List<Integer> getDirectChildIndexes(int parentIndex) {
		List<Integer> result = new ArrayList<Integer>();

		Pad currentPad = pads[parentIndex];

		while (currentPad != null) {
			if (isCollapsed[padToIndex.get(currentPad)]) {
				return result;
			}

			currentPad = currentPad.getParent();
		}

		for (Pad pad : pads[parentIndex].getChildPads()) {
			result.add(padToIndex.get(pad));
		}

		return result;
	}

	@Override
	public List<Pad> getChildren(int parentIndex) {
		ArrayList<Pad> allChildren = new ArrayList<Pad>();
		pads[parentIndex].recursivelyAddChildrenToList(allChildren);
		return allChildren;
	}

	@Override
	public List<Pad> getDirectChildren(int parentIndex) {
		return pads[parentIndex].getChildPads();
	}

	public boolean isFormattingOn() {
		return formattingOn;
	}

	public void setFormattingOn(boolean formattingOn) {
		this.formattingOn = formattingOn;
	}
}
