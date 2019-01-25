/*
 * Copyright © 2016-2019 by Devon Bowen.
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

public class UserPad extends NamedPad {
	public UserPad(ScratchPad<UserPad> parent, String name) {
		super(parent, name);
	}

	UserPad(Pad parent, UserPad oldPad) {
		super(parent, oldPad);

		for (ProjectPad child : oldPad.getChildren()) {
			new ProjectPad(this, child);
		}
	}

	UserPad(Pad parent, ObjectInput input, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		super(parent, input, allProperties, propertyToIndex);

		int numOfChildren = input.readInt();

		for (int i=0; i<numOfChildren; i++) {
			new ProjectPad(this, input, allProperties, propertyToIndex);
		}
	}

	@Override
	public void writeExternal(ObjectOutput output, Vector<String> allProperties, HashMap<String,Integer> propertyToIndex) throws IOException {
		super.writeExternal(output, allProperties, propertyToIndex);

		ArrayList<ProjectPad> children = getChildren();
		output.writeInt(children.size());

		for (ProjectPad projectPad : children) {
			projectPad.writeExternal(output, allProperties, propertyToIndex);
		}
	}

	public ProjectPad getChild(int number) {
		@SuppressWarnings("unchecked")
		ProjectPad result = ((ArrayList<ProjectPad>) (ArrayList<?>) children).get(number);
		return result;
	}

	public ArrayList<ProjectPad> getChildren() {
		@SuppressWarnings("unchecked")
		ArrayList<ProjectPad> projects = (ArrayList<ProjectPad>) (ArrayList<?>) children;
		return projects;
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
