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

package org.easotope.shared.core.scratchpad;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import org.easotope.shared.core.scratchpad.ReplicatePad.ReplicateType;

public class ScratchPad<T> extends Pad implements Externalizable {
	private final byte CURRENT_EXTERNALIZATION_ALGORITHM = 0;

	private enum NodeType { CYCLE, ACQUISITION, REPLICATE, SAMPLE, PROJECT, USER };
	private ArrayList<String> naturalColumnOrder = null;

	public ScratchPad() { }

	public ScratchPad(ScratchPad<T> oldPad) {
		super(null, oldPad);

		if (oldPad.naturalColumnOrder != null) {
			naturalColumnOrder = new ArrayList<String>(oldPad.naturalColumnOrder);
		}

		for (Object child : oldPad.getChildren()) {
			if (child instanceof UserPad) {
				new UserPad(this, (UserPad) child);

			} else if (child instanceof ProjectPad) {
				new ProjectPad(this, (ProjectPad) child);

			} else if (child instanceof SamplePad) {
				new SamplePad(this, (SamplePad) child);

			} else if (child instanceof ReplicatePad) {
				new ReplicatePad(this, (ReplicatePad) child);

			} else if (child instanceof AcquisitionPad) {
				new AcquisitionPad(this, (AcquisitionPad) child);

			} else if (child instanceof CyclePad) {
				new CyclePad(this, (CyclePad) child);
			}
		}
	}

	@Override
	public String getPrintableIdentifier() {
		return "ScratchPath";
	}

	@Override
	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		 // externalizationAlgorithm

		input.readByte();

		// naturalPropertyOrder

		int numNaturalPropertyOrder = input.readInt();

		if (numNaturalPropertyOrder != 0) {
			naturalColumnOrder = new ArrayList<String>();

			for (int i=0; i<numNaturalPropertyOrder; i++) {
				String columnName = input.readUTF();
				naturalColumnOrder.add(columnName);
			}
		}

		// allPropertiesSorted

		int numProperties = input.readInt();
		
		TreeSet<String> allPropertiesAsTreeSet = getAllColumns();

		for (int i=0; i<numProperties; i++) {
			String property = input.readUTF();
			allPropertiesAsTreeSet.add(property);
		}

		Vector<String> allProperties = new Vector<String>(allPropertiesAsTreeSet);
		
		// property to index mapping

		HashMap<String,Integer> propertyToIndex = new HashMap<String,Integer>();

		int count=0;
		for (String property : allProperties) {
			propertyToIndex.put(property, count++);
		}

		// children
		
		int numChildren = input.readInt();

		if (numChildren == 0) {
			return;
		}

		int nodeTypeOrdinal = input.readByte();
		NodeType childType = NodeType.values()[nodeTypeOrdinal];

		for (int i=0; i<numChildren; i++) {
			switch (childType) {
				case CYCLE:
					new CyclePad(this, input, allProperties, propertyToIndex);
					break;

				case ACQUISITION:
					new AcquisitionPad(this, input, allProperties, propertyToIndex);
					break;

				case REPLICATE:
					new ReplicatePad(this, input, allProperties, propertyToIndex);
					break;
	
				case SAMPLE:
					new SamplePad(this, input, allProperties, propertyToIndex);
					break;
					
				case PROJECT:
					new ProjectPad(this, input, allProperties, propertyToIndex);
					break;

				case USER:
					new UserPad(this, input, allProperties, propertyToIndex);
					break;
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput output) throws IOException {
		Vector<String> allProperties = new Vector<String>(getAllColumns());
		HashMap<String,Integer> propertyToIndex = new HashMap<String,Integer>();

		int count=0;
		for (String property : allProperties) {
			propertyToIndex.put(property, count++);
		}

		writeExternal(output, allProperties, propertyToIndex);
	}

	@Override
	void writeExternal(ObjectOutput output, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		 // externalizationAlgorithm

		output.writeByte(CURRENT_EXTERNALIZATION_ALGORITHM);

		// naturalPropertyOrder

		if (naturalColumnOrder == null) {
			output.writeInt(0);
		} else {
			output.writeInt(naturalColumnOrder.size());

			for (String column : naturalColumnOrder) {
				output.writeUTF(column);
			}
		}

		// allPropertiesSorted

		output.writeInt(allProperties.size());

		for (String property : allProperties) {
			output.writeUTF(property);
		}

		// children

		output.writeInt(children.size());

		if (children.size() == 0) {
			return;
		}

		NodeType nodeType = null;
		Object childObject = children.get(0);

		if (childObject instanceof CyclePad) {
			nodeType = NodeType.CYCLE;
		} else if (childObject instanceof AcquisitionPad) {
			nodeType = NodeType.ACQUISITION;
		} else if (childObject instanceof ReplicatePad) {
			nodeType = NodeType.REPLICATE;
		} else if (childObject instanceof SamplePad) {
			nodeType = NodeType.SAMPLE;
		} else if (childObject instanceof ProjectPad) {
			nodeType = NodeType.PROJECT;
		} else if (childObject instanceof UserPad) {
			nodeType = NodeType.USER;
		}

		output.writeByte(nodeType.ordinal());

		for (T node : getChildren()) {
			switch (nodeType) {
				case CYCLE:
					((CyclePad) node).writeExternal(output, allProperties, propertyToIndex);
					break;

				case ACQUISITION:
					((AcquisitionPad) node).writeExternal(output, allProperties, propertyToIndex);
					break;

				case REPLICATE:
					((ReplicatePad) node).writeExternal(output, allProperties, propertyToIndex);
					break;

				case SAMPLE:
					((SamplePad) node).writeExternal(output, allProperties, propertyToIndex);
					break;

				case PROJECT:
					((ProjectPad) node).writeExternal(output, allProperties, propertyToIndex);
					break;

				case USER:
					((UserPad) node).writeExternal(output, allProperties, propertyToIndex);
					break;
			}
		}
	}

	public T getChild(int number) {
		@SuppressWarnings("unchecked")
		T result = ((ArrayList<T>) children).get(number);
		return result;
	}

	public ArrayList<T> getChildren() {
		@SuppressWarnings("unchecked")
		ArrayList<T> t = (ArrayList<T>) children;
		return t;
	}

	public void reassignAllStandardsToParent(ScratchPad<ReplicatePad> newParent) {
		assert(children.size() == 0 || children.get(0) instanceof ReplicatePad);

		Iterator<T> iter = getChildren().iterator();

		while (iter.hasNext()) {
			ReplicatePad pad = (ReplicatePad) iter.next();
			iter.remove();

			if (pad.getReplicateType() == ReplicateType.STANDARD_RUN) {
				pad.parent = newParent;
				pad.parent.children.add(pad);
			}
		}

		Collections.sort(newParent.children);
	}

	@Override
	public int compareTo(Pad arg0) {
		return 0;
	}

	@Override
	boolean hasDisabled() {
		return false;
	}

	@Override
	boolean hasOffPeak() {
		return false;
	}
}
