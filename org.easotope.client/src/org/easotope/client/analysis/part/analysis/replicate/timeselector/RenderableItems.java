/*
 * Copyright © 2016-2023 by Devon Bowen.
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

package org.easotope.client.analysis.part.analysis.replicate.timeselector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListItem;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateList;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateListItem;
import org.easotope.shared.rawdata.cache.input.scanlist.ScanList;
import org.easotope.shared.rawdata.cache.input.scanlist.ScanListItem;

public class RenderableItems {
	private ReplicateList sampleList = null;
	private ReplicateList standardList = null;
	private ScanList scanList = null;
	private CorrIntervalList corrIntervalList = null;
	private DateAndObject[] sortedRenderableItems = new DateAndObject[0];
	private HashMap<Integer,ReplicateListItem> idToReplicateListItem = new HashMap<Integer,ReplicateListItem>();
	private HashMap<ReplicateListItem,Integer> replicateListItemToId = new HashMap<ReplicateListItem,Integer>();
	private HashMap<CorrIntervalListItem,Integer> corrIntervalListItemToId = new HashMap<CorrIntervalListItem,Integer>();
	private HashMap<CorrIntervalListItem,CorrIntervalListItem> followingCorrIntervalListItem = new HashMap<CorrIntervalListItem,CorrIntervalListItem>();

	void reset() {
		sampleList = null;
		standardList = null;
		scanList = null;
		corrIntervalList = null;
		sortedRenderableItems = new DateAndObject[0];
		idToReplicateListItem.clear();
		replicateListItemToId.clear();
		corrIntervalListItemToId.clear();
		followingCorrIntervalListItem.clear();
	}

	boolean isReady() {
		return sampleList != null && standardList != null && corrIntervalList != null && scanList != null;
	}

	void setSampleList(ReplicateList sampleList) {
		this.sampleList = sampleList;
		generateSortedRenderableItems();
	}

	void setStandardList(ReplicateList standardList) {
		this.standardList = standardList;
		generateSortedRenderableItems();
	}

	public void setScanList(ScanList scanList) {
		this.scanList = scanList;
		generateSortedRenderableItems();
	}

	CorrIntervalList getCorrIntervalList() {
		return corrIntervalList;
	}

	void setCorrIntervalList(CorrIntervalList corrIntervalList) {
		this.corrIntervalList = corrIntervalList;
		generateSortedRenderableItems();

		ArrayList<DateAndObject> sortedCorrIntervalList = new ArrayList<DateAndObject>();

		for (Integer key : corrIntervalList.keySet()) {
			CorrIntervalListItem corrIntervalListItem = corrIntervalList.get(key);
			sortedCorrIntervalList.add(new DateAndObject(corrIntervalListItem.getDate(), corrIntervalListItem));
		}

		Collections.sort(sortedCorrIntervalList);

		followingCorrIntervalListItem.clear();
		CorrIntervalListItem precedingCorrIntervalListItem = null;

		for (DateAndObject dateAndObject : sortedCorrIntervalList) {
			CorrIntervalListItem currentCorrIntervalListItem = (CorrIntervalListItem) dateAndObject.getObject();

			if (precedingCorrIntervalListItem != null) {
				followingCorrIntervalListItem.put(precedingCorrIntervalListItem, currentCorrIntervalListItem);
			}

			precedingCorrIntervalListItem = currentCorrIntervalListItem;
		}
	}

	DateAndObject[] getSortedRenderableItems() {
		return sortedRenderableItems;
	}

	int getStartTimeInMinutes() {
		return sortedRenderableItems.length == 0 ? 0 : sortedRenderableItems[0].getTimeInMinutes();
	}

	int getEndTimeInMinutes() {
		return sortedRenderableItems.length == 0 ? Integer.MAX_VALUE : sortedRenderableItems[sortedRenderableItems.length-1].getTimeInMinutes();
	}

	private void generateSortedRenderableItems() {
		if (!isReady()) {
			return;
		}

		corrIntervalListItemToId.clear();
		idToReplicateListItem.clear();
		replicateListItemToId.clear();

		ArrayList<DateAndObject> list = new ArrayList<DateAndObject>();

		for (int id : sampleList.keySet()) {
			ReplicateListItem item = sampleList.get(id);
			list.add(new DateAndObject(item.getDate(), item));
			idToReplicateListItem.put(id, item);
			replicateListItemToId.put(item, id);
		}

		for (int id : standardList.keySet()) {
			ReplicateListItem item = standardList.get(id);
			list.add(new DateAndObject(item.getDate(), item));
			idToReplicateListItem.put(id, item);
			replicateListItemToId.put(item, id);
		}

		for (int id : scanList.keySet()) {
			ScanListItem item = scanList.get(id);
			list.add(new DateAndObject(item.getDate(), item));
		}

		for (int id : corrIntervalList.keySet()) {
			CorrIntervalListItem item = corrIntervalList.get(id);
			list.add(new DateAndObject(item.getDate(), item));
			corrIntervalListItemToId.put(item, id);
		}

		sortedRenderableItems = list.toArray(new DateAndObject[list.size()]);
		Arrays.sort(sortedRenderableItems);
	}

	CorrIntervalListItem getFollowingCorrIntervalListItem(CorrIntervalListItem corrIntervalListItem) {
		return followingCorrIntervalListItem.get(corrIntervalListItem);
	}

	CorrIntervalListItem getCorrIntervalListItemForDate(long date) {
		CorrIntervalListItem bestChoice = null;
		long bestChoiceDistance = Long.MAX_VALUE;

		for (Integer key : corrIntervalList.keySet()) {
			CorrIntervalListItem corrIntervalListItem = corrIntervalList.get(key);			

			if (corrIntervalListItem.getDate() <= date && bestChoiceDistance > date - corrIntervalListItem.getDate()) {
				bestChoice = corrIntervalListItem;
				bestChoiceDistance = date - corrIntervalListItem.getDate();
			}
		}

		return bestChoice;
	}

	Integer getId(CorrIntervalListItem corrIntervalListItem) {
		return corrIntervalListItemToId.get(corrIntervalListItem);
	}

	Integer getId(ReplicateListItem replicateListItem) {
		return replicateListItemToId.get(replicateListItem);
	}

	public ReplicateListItem getReplicateListItem(Integer replicateId) {
		return idToReplicateListItem.get(replicateId);
	}
}
