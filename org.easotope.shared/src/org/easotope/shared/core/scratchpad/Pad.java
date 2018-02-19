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

package org.easotope.shared.core.scratchpad;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.easotope.shared.Messages;

public abstract class Pad implements Comparable<Pad> {
	public static final String ID = "ID";
	public static final String ANALYSIS = "Analysis";
	public static final String ANALYSIS_STATUS = "Analysis Status";
	public static final String DISABLED = "Disabled";
	public static final String OFF_PEAK = "Off_Peak";

	public enum Status { NONE, OK, WARNING, ERROR };

	private enum CellType { NULL, BOOLEAN_TRUE, BOOLEAN_FALSE, DOUBLE, INTEGER, STRING, ACCUMULATOR_DYNAMIC, ACCUMULATOR_FROZEN, ACCUMULATOR_STDDEV_SAMPLE, ACCUMULATOR_STDERR, STATUS, DATE };

	protected Pad parent = null;
	private Vector<Pad> path = null;
	protected HashMap<String,Object> data = new HashMap<String,Object>();
	protected ArrayList<Pad> children = new ArrayList<Pad>();
	private HashMap<String,Object> volatileData = null;

	abstract public String getPrintableIdentifier();
	abstract boolean hasDisabled();
	abstract boolean hasOffPeak();

	Pad() { }

	Pad(Pad parent) {
		assert(parent.children.size() == 0 || parent.children.get(0).getClass() == this.getClass());

		this.parent = parent;
		this.parent.children.add(this);
	}

	Pad(Pad parent, Pad oldPad) {
		assert(parent.children.size() == 0 || parent.children.get(0).getClass() == this.getClass());

		this.parent = parent;

		if (parent != null) {
			this.parent.children.add(this);
		}

		for (String property : oldPad.data.keySet()) {
			Object value = oldPad.data.get(property);

			if (value instanceof Accumulator) {
				Accumulator oldAccumulator = (Accumulator) value;

				if (oldAccumulator.isFrozen()) {
					data.put(property, value);
				} else {
					data.put(property, new Accumulator(this, property, oldAccumulator.isRecursive()));
				}

			} else if (value instanceof AccumulatorStdDevSample) {
				AccumulatorStdDevSample oldAccumulatorStdDevSample = (AccumulatorStdDevSample) value;
				data.put(property, new AccumulatorStdDevSample(this, oldAccumulatorStdDevSample.getAccumulatorColumn()));
				
			} else if (value instanceof AccumulatorStdErr) {
				AccumulatorStdErr oldAccumulatorStdErr = (AccumulatorStdErr) value;
				data.put(property, new AccumulatorStdErr(this, oldAccumulatorStdErr.getAccumulatorColumn()));

			} else {
				data.put(property, value);
			}
		}

		if (oldPad.volatileData != null) {
			volatileData = new HashMap<String,Object>(oldPad.volatileData);
		}
	}

	Pad(Pad parent, ObjectInput input, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		assert(parent.children.size() == 0 || parent.children.get(0).getClass() == this.getClass());

		this.parent = parent;
		this.parent.children.add(this);

		for (String property : allProperties) {
			int cellTypeOrdinal = input.readByte();
			CellType cellType = CellType.values()[cellTypeOrdinal];

			switch (cellType) {
				case NULL:
					break;

				case BOOLEAN_TRUE:
					setValue(property, new Boolean(true));
					break;

				case BOOLEAN_FALSE:
					setValue(property, new Boolean(false));
					break;

				case DOUBLE:
					double doubleValue = input.readDouble();
					setValue(property, new Double(doubleValue));
					break;

				case INTEGER:
					int intValue = input.readInt();
					setValue(property, new Integer(intValue));
					break;

				case STRING:
					String stringValue = input.readUTF();
					setValue(property, stringValue);
					break;

				case ACCUMULATOR_DYNAMIC:
					byte byteValue = input.readByte();
					data.put(property, new Accumulator(this, property, byteValue == 1));
					break;

				case ACCUMULATOR_FROZEN:
					double meanValue = input.readDouble();
					double stdDevSampleValue = input.readDouble();
					double stdErrValue = input.readDouble();
					data.put(property, new Accumulator(meanValue, stdDevSampleValue, stdErrValue));
					break;

				case ACCUMULATOR_STDDEV_SAMPLE:
					int propertyIndex = input.readInt();
					data.put(property, new AccumulatorStdDevSample(this, allProperties.get(propertyIndex)));
					break;

				case ACCUMULATOR_STDERR:
					propertyIndex = input.readInt();
					data.put(property, new AccumulatorStdErr(this, allProperties.get(propertyIndex)));
					break;

				case STATUS:
					byteValue = input.readByte();
					data.put(property, Status.values()[byteValue]);
					break;

				case DATE:
					long longValue = input.readLong();
					data.put(property, new PadDate(longValue));
					break;
			}
		}
	}

	public void reassignToParent(Pad parent) {
		assert(this.parent.children.size() == 0 || this.parent.children.get(0).getClass() == this.getClass());
		this.parent.children.remove(this);
		this.parent = parent;
		this.parent.children.add(this);
		Collections.sort(this.parent.children);
	}

	public void recursivelyAddChildrenToList(ArrayList<Pad> listOfNodes) {
		for (Pad child : children) {
			listOfNodes.add(child);
			child.recursivelyAddChildrenToList(listOfNodes);
		}
	}

	public void clearData() {
		data.clear();
	}
	
	public void removeAllChildren() {
		children.clear();
	}

	public void removeChild(Pad child) {
		children.remove(child);
	}

	public void removeChildren(HashSet<Pad> unwanted) {
		Iterator<Pad> iter = children.iterator();

		while (iter.hasNext()) {
			if (unwanted.contains(iter.next())) {
				iter.remove();
			}
		}
	}

	public Object getValue(String string) {
		if (ID.equals(string)) {
			return getPrintableIdentifier();
		}

		return data.get(string);
	}

	public void setValue(String property, Object object) {
		assert(!data.containsKey(property));

		if (property == null) {
			throw new RuntimeException(Messages.scratchPad_propertyCannotBeNull);
		}

		data.put(property, object);
	}

	public void setAccumulator(String meanProperty, String stdDevSampleProperty, String stdErrProperty, boolean isRecursive) {
		assert(meanProperty != null);
		assert(!data.containsKey(meanProperty));

		Accumulator accumulator = new Accumulator(this, meanProperty, isRecursive);
		data.put(meanProperty, accumulator);

		if (stdDevSampleProperty != null) {
			data.put(stdDevSampleProperty, new AccumulatorStdDevSample(accumulator));
		}

		if (stdErrProperty != null) {
			data.put(stdErrProperty, new AccumulatorStdErr(accumulator));
		}
	}

	public Pad getParent() {
		return parent;
	}

	public ArrayList<Pad> getChildPads() {
		return children;
	}

	public int getDepth() {
		int depth = 0;
		Pad currentPad = parent;

		while (currentPad != null) {
			depth++;
			currentPad = currentPad.parent;
		}
		
		return depth;
	}

	public Vector<Pad> getPath() {
		if (path == null) {
			path = new Vector<Pad>();

			if (!(parent instanceof ScratchPad)) {
				path.addAll(parent.getPath());
			}

			path.add(this);
		}

		return path;
	}

	Set<String> getColumns() {
		HashSet<String> set = new HashSet<String>(data.keySet());

		set.add(ID);

		if (this.hasDisabled()) {
			set.add(DISABLED);
		}

		if (this.hasOffPeak()) {
			set.add(OFF_PEAK);
		}

		return set;
	}

	void writeExternal(ObjectOutput output, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		for (String property : allProperties) {
			Object object = data.get(property);

			if (object == null) {
				output.writeByte(CellType.NULL.ordinal());
				continue;
			}

			if (object instanceof Boolean) {
				if ((Boolean) object) {
					output.writeByte(CellType.BOOLEAN_TRUE.ordinal());
				} else {
					output.writeByte(CellType.BOOLEAN_FALSE.ordinal());
				}

				continue;
			}

			if (object instanceof Double) {
				output.writeByte(CellType.DOUBLE.ordinal());
				output.writeDouble((Double) object);
				continue;
			}

			if (object instanceof Integer) {
				output.writeByte(CellType.INTEGER.ordinal());
				output.writeInt((Integer) object);
				continue;
			}

			if (object instanceof String) {
				output.writeByte(CellType.STRING.ordinal());
				output.writeUTF((String) object);
				continue;
			}

			if (object instanceof Accumulator) {
				Accumulator accumulator = (Accumulator) object;

				if (accumulator.isFrozen()) {
					output.writeByte(CellType.ACCUMULATOR_FROZEN.ordinal());
					double[] values = accumulator.getMeanStdDevSampleAndStdError();
					output.writeDouble(values[0]);
					output.writeDouble(values[1]);
					output.writeDouble(values[2]);

				} else {
					output.writeByte(CellType.ACCUMULATOR_DYNAMIC.ordinal());
					output.writeByte(accumulator.isRecursive() ? 1 : 0);
				}

				continue;
			}

			if (object instanceof AccumulatorStdDevSample) {
				output.writeByte(CellType.ACCUMULATOR_STDDEV_SAMPLE.ordinal());
				String accumulatorColumn = ((AccumulatorStdDevSample) object).getAccumulatorColumn();
				output.writeInt(propertyToIndex.get(accumulatorColumn));

				continue;
			}

			if (object instanceof AccumulatorStdErr) {
				output.writeByte(CellType.ACCUMULATOR_STDERR.ordinal());
				String accumulatorColumn = ((AccumulatorStdErr) object).getAccumulatorColumn();
				output.writeInt(propertyToIndex.get(accumulatorColumn));

				continue;
			}

			if (object instanceof Status) {
				output.writeByte(CellType.STATUS.ordinal());
				output.writeByte(((Status) object).ordinal());

				continue;
			}

			if (object instanceof PadDate) {
				output.writeByte(CellType.DATE.ordinal());
				output.writeLong(((PadDate) object).getLongValue());
			}
		}
	}

	public void removeChild(int index) {
		children.remove(index);
	}

	public void trimChildrenToLevel(Class<?> clazz) {
		if (this.getClass() == clazz) {
			for (String key : data.keySet()) {
				Object value = data.get(key);
	
				if (value instanceof Accumulator) {
					Accumulator oldAccumulator = (Accumulator) value;
					double[] old = oldAccumulator.getMeanStdDevSampleAndStdError();
					Accumulator newAccumulator = new Accumulator(old[0], old[1], old[2]);
					data.put(key, newAccumulator);
				}
			}

			children.clear();

		} else {
			for (Pad pad : children) {
				pad.trimChildrenToLevel(clazz);
			}
		}
	}

	public Object getVolatileData(String key) {
		if (volatileData == null) {
			return null;
		} else {
			return volatileData.get(key);
		}
	}

	public void setVolatileData(String key, Object value) {
		if (volatileData == null) {
			volatileData = new HashMap<String,Object>();
		}

		volatileData.put(key, value);
	}

	public ArrayList<Pad> getAllPads() {
		ArrayList<Pad> listOfPads = new ArrayList<Pad>();
		recursivelyAddChildrenToList(listOfPads);
		return listOfPads;
	}

	public TreeSet<String> getAllColumns() {
		TreeSet<String> allColumns = new TreeSet<String>();

		for (Pad hashNode : getAllPads()) {
			allColumns.addAll(hashNode.getColumns());
		}

		return allColumns;
	}

	public boolean hasAllColumns(List<String> requiredInputColumns) {
		TreeSet<String> allColumns = getAllColumns();

		boolean hasAllColumns = true;

		for (String column : requiredInputColumns) {
			if (!allColumns.contains(column)) {
				hasAllColumns = false;
				break;
			}
		}

		return hasAllColumns;
	}
}
