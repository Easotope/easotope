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
import java.util.HashMap;
import java.util.Vector;

import org.easotope.framework.dbcore.DatabaseConstants;

public class ReplicatePad extends DatedPad {
	public enum ReplicateType { SAMPLE_RUN, STANDARD_RUN, SCAN }; 

	private int replicateId;
	private ReplicateType replicateType;
	private int sourceId = DatabaseConstants.EMPTY_DB_ID;

	public ReplicatePad(ScratchPad<ReplicatePad> parent, long date, int replicateId, ReplicateType replicateType) {
		super(parent, date);
		this.replicateId = replicateId;
		this.replicateType = replicateType;
	}

	public ReplicatePad(SamplePad parent, long date, int replicateId, ReplicateType replicateType) {
		super(parent, date);
		this.replicateId = replicateId;
		this.replicateType = replicateType;
	}

	public ReplicatePad(Pad parent, ReplicatePad oldPad) {
		super(parent, oldPad);

		this.replicateId = oldPad.replicateId;
		this.replicateType = oldPad.replicateType;
		this.sourceId = oldPad.sourceId;

		for (AcquisitionPad child : oldPad.getChildren()) {
			new AcquisitionPad(this, child);
		}
	}

	ReplicatePad(Pad parent, ObjectInput input, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		super(parent, input, allProperties, propertyToIndex);

		replicateId = input.readInt();
		replicateType = ReplicateType.values()[input.readByte()];
		sourceId = input.readInt();
		int numOfChildren = input.readInt();

		for (int i=0; i<numOfChildren; i++) {
			new AcquisitionPad(this, input, allProperties, propertyToIndex);
		}
	}

	@Override
	public void writeExternal(ObjectOutput output, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		super.writeExternal(output, allProperties, propertyToIndex);

		output.writeInt(replicateId);
		output.writeByte(replicateType.ordinal());
		output.writeInt(sourceId);

		ArrayList<AcquisitionPad> children = getChildren();
		output.writeInt(children.size());

		for (AcquisitionPad acquisitionPad : children) {
			acquisitionPad.writeExternal(output, allProperties, propertyToIndex);
		}
	}

	public AcquisitionPad getChild(int number) {
		@SuppressWarnings("unchecked")
		AcquisitionPad result = ((ArrayList<AcquisitionPad>) (ArrayList<?>) children).get(number);
		return result;
	}

	public ArrayList<AcquisitionPad> getChildren() {
		@SuppressWarnings("unchecked")
		ArrayList<AcquisitionPad> acquisitions = (ArrayList<AcquisitionPad>) (ArrayList<?>) children;
		return acquisitions;
	}

	public int getReplicateId() {
		return replicateId;
	}

	public ReplicateType getReplicateType() {
		return replicateType;
	}

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	@Override
	public boolean hasDisabled() {
		return true;
	}

	@Override
	boolean hasOffPeak() {
		return false;
	}
}
