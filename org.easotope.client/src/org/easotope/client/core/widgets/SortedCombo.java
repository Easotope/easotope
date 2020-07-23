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

package org.easotope.client.core.widgets;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.easotope.shared.core.cache.CacheList;
import org.eclipse.swt.widgets.Composite;


public class SortedCombo extends VCombo {
	private int oldInteger = -1;
	private int selectedInteger = -1;

	private HashMap<Integer,String> possibilities = null;
	private HashMap<Integer,Integer> integerToIndex = new HashMap<Integer,Integer>();
	private HashMap<Integer,Integer> indexToInteger = new HashMap<Integer,Integer>();

	public SortedCombo(Composite parent, int style) {
		super(parent, style);
	}

	public void selectFirst() {
		if (indexToInteger.size() == 0) {
			selectInteger(-1);
		} else {
			selectInteger(indexToInteger.get(0));
		}
	}

	public void selectInteger(int integer) {
		setSelectionButLeaveRevertValue(integer);
		oldInteger = integer;
	}

	public void setSelectionButLeaveRevertValue(int integer) {
		if (integer != -1 && integerToIndex.containsKey(integer)) {
			select(integerToIndex.get(integer));
		} else {
			deselectAll();
		}

		selectedInteger = integer;
	}

	public int getSelectedInteger() {
		int selectionIndex = getSelectionIndex();

		if (selectionIndex == -1) {
			return selectedInteger;
		}

		return indexToInteger.get(getSelectionIndex());
	}

	@Override
	public boolean hasChanged() {
		return getSelectedInteger() != oldInteger;
	}

	@Override
	public void revert() {
		if (oldInteger != -1 && integerToIndex.containsKey(oldInteger)) {
			select(integerToIndex.get(oldInteger));
		} else {
			deselectAll();
		}

		selectedInteger = oldInteger;
	}

	public void setPossibilities(CacheList<?> possibilities) {
		HashMap<Integer,String> possibilitiesAsStrings = new HashMap<Integer,String>();

		for (Integer key : possibilities.keySet()) {
			possibilitiesAsStrings.put(key, possibilities.get(key).toString());
		}
		
		setPossibilities(possibilitiesAsStrings);
	}

	public void setPossibilities(HashMap<Integer,String> possibilities) {
		this.possibilities  = possibilities;

		selectedInteger = getSelectedInteger();

		ArrayList<IntegerAndString> allPossibilities = new ArrayList<IntegerAndString>();

		if (possibilities != null) {
			for (Integer integer : possibilities.keySet()) {
				String string = possibilities.get(integer);
				allPossibilities.add(new IntegerAndString(integer, string));
			}
		}

		Collections.sort(allPossibilities);

		integerToIndex.clear();
		indexToInteger.clear();

		removeAll();

		for (IntegerAndString integerAndString : allPossibilities) {
			int integer = integerAndString.getInteger();
			String string = integerAndString.getString();

			integerToIndex.put(integer, getItemCount());
			indexToInteger.put(getItemCount(), integer);

			add(string);
		}

		if (selectedInteger != -1) {
			setSelectionButLeaveRevertValue(selectedInteger);
		}
	}

	public HashMap<Integer,String> getPossibilities() {
		return possibilities;
	}

	private class IntegerAndString implements Comparable<IntegerAndString> {
		private int integer;
		private String string;

		public IntegerAndString(int integer, String string) {
			this.integer = integer;
			this.string = string;
		}

		@Override
		public int compareTo(IntegerAndString that) {
			return Collator.getInstance().compare(string, that.string);
		}
		
		public int getInteger() {
			return integer;
		}

		public String getString() {
			return string;
		}
	}
}
