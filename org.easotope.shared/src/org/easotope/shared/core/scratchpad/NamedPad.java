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
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

abstract class NamedPad extends Pad {
	private String name;

	NamedPad(Pad parent, String name) {
		super(parent);
		this.name = name;
		Collections.sort(parent.children);
	}

	NamedPad(Pad parent, NamedPad oldPad) {
		super(parent, oldPad);
		name = oldPad.name;
	}
	
	public NamedPad(Pad parent, ObjectInput input, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		super(parent, input, allProperties, propertyToIndex);
		name = input.readUTF();
	}

	public String getName() {
		return name;
	}

	@Override
	public String getPrintableIdentifier() {
		return name;
	}

	@Override
	void writeExternal(ObjectOutput output, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		super.writeExternal(output, allProperties, propertyToIndex);
		output.writeUTF(name);
	}

	@Override
	public int compareTo(Pad that) {
		if (that instanceof NamedPad) {
			String thisName = this.name;
			String thatName = ((NamedPad) that).name;

			if (thisName.equals(thatName)) {
				String thisAnalysis = (String) this.getValue(Pad.ANALYSIS);
				String thatAnalysis = (String) that.getValue(Pad.ANALYSIS);

				if (thisAnalysis == null) {
					thisAnalysis = "";
				}

				if (thatAnalysis == null) {
					thatAnalysis = "";
				}

				return thisAnalysis.compareTo(thatAnalysis);
			}

			return thisName.compareTo(thatName);
		}

		return 0;
	}
}
