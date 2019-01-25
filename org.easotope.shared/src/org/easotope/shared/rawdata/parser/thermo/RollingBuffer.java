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

package org.easotope.shared.rawdata.parser.thermo;

public class RollingBuffer {
	private int[] buffer;
	private int first = 0;
	private int contains = 0;

	public RollingBuffer(int size) {
		buffer = new int[size];
	}

	public void addToEnd(int value) {
		if (contains == buffer.length) {
			throw new RuntimeException("RollingBuffer is full");
		}
		
		int index = first + contains;
		
		if (index >= buffer.length) {
			index -= buffer.length;
		}
		
		buffer[index] = value;
		contains++;
	}
	
	public void deleteFromStart(int count) {
		if (count > contains) {
			throw new RuntimeException("RollingBuffer contains too few items " + count + " " + contains + " " + buffer.length);
		}

		first += count;
		
		if (first >= buffer.length) {
			first -= buffer.length;
		}
		
		contains -= count;
	}
	
	public int get(int i) {
		if (i >= contains) {
			throw new RuntimeException("RollingBuffer contains too few items " + i + " " + contains + " " + buffer.length);
		}
		
		int index = first + i;
		
		if (index >= buffer.length) {
			index -= buffer.length;
		}
		
		return buffer[index];
	}
	
	public int getContains() {
		return contains;
	}
	
	public int getSize() {
		return buffer.length;
	}

	public boolean startsWith(Integer[] pattern) {
		if (pattern.length > contains) {
			return false;
		}

		for (int i=0; i<pattern.length; i++) {
			Integer value = pattern[i];

			if (value != null && value.intValue() != get(i)) {
				return false;
			}
		}

		return true;
	}

	public void clear() {
		first = 0;
		contains = 0;
	}
}
