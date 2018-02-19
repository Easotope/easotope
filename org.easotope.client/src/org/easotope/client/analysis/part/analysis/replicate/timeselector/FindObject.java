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

package org.easotope.client.analysis.part.analysis.replicate.timeselector;

import java.util.Arrays;

import org.easotope.client.analysis.part.analysis.replicate.timeselector.YAxisTypes.ItemType;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListItem;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateListItem;
import org.easotope.shared.rawdata.cache.input.scanlist.ScanListItem;

public class FindObject {
	private final int PIXEL_RANGE = 4;

	private CoordinateTransform coordinateTransform;
	private RenderableItems renderableItems;
	private YAxisTypes yAxisTypes;

	private YAxisTypes.ItemType itemType = YAxisTypes.ItemType.NONE;
	private ReplicateListItem replicateListItem;
	private ScanListItem scanListItem;
	private CorrIntervalListItem corrIntervalListItem;

	FindObject(CoordinateTransform coordinateTransform, RenderableItems renderableItems, YAxisTypes yAxisTypes) {
		this.coordinateTransform = coordinateTransform;
		this.renderableItems = renderableItems;
		this.yAxisTypes = yAxisTypes;
	}

	void setLocation(int mouseX, int mouseY) {
		itemType = yAxisTypes.getItemTypeForY(mouseY);

		int exactTimeInMinutes = coordinateTransform.pixelXToTimeInMinutes(mouseX);
		long earliestTimeAsDate = (long) coordinateTransform.pixelXToTimeInMinutes(mouseX - PIXEL_RANGE) * 1000 * 60;
		int latestTimeInMinutes = coordinateTransform.pixelXToTimeInMinutes(mouseX + PIXEL_RANGE);

		DateAndObject[] sortedRenderableItems = renderableItems.getSortedRenderableItems();
		int searchIndex = Arrays.binarySearch(sortedRenderableItems, new DateAndObject(earliestTimeAsDate, null));

		if (searchIndex < 0) {
			searchIndex = -searchIndex - 1;
		}

		int closestDistance = Integer.MAX_VALUE;
		Object hoverItem = null;

		while (searchIndex < sortedRenderableItems.length && sortedRenderableItems[searchIndex].getTimeInMinutes() <= latestTimeInMinutes) {
			if (itemType != getItemTypeOfObject(sortedRenderableItems[searchIndex].getObject())) {
				searchIndex++;
				continue;
			}

			int thisDistance = Math.abs(sortedRenderableItems[searchIndex].getTimeInMinutes() - exactTimeInMinutes);

			if (thisDistance < closestDistance) {
				hoverItem = sortedRenderableItems[searchIndex].getObject();
				closestDistance = thisDistance;
			}

			searchIndex++;
		}

		replicateListItem = null;
		scanListItem = null;
		corrIntervalListItem = null;

		if (hoverItem == null) {
			itemType = ItemType.NONE;

		} else {
			if (hoverItem instanceof CorrIntervalListItem) {
				corrIntervalListItem = (CorrIntervalListItem) hoverItem;

			} else if (hoverItem instanceof ScanListItem) {
				scanListItem = (ScanListItem) hoverItem;

			} else if (hoverItem instanceof ReplicateListItem) {
				replicateListItem = (ReplicateListItem) hoverItem;
			}
		}
	}

	ItemType getItemType() {
		return itemType;
	}

	ReplicateListItem getReplicateListItem() {
		return replicateListItem;
	}

	ScanListItem getScanListItem() {
		return scanListItem;
	}

	CorrIntervalListItem getCorrIntervalListItem() {
		return corrIntervalListItem;
	}

	private ItemType getItemTypeOfObject(Object object) {
		if (object instanceof CorrIntervalListItem) {
			return ItemType.CORRINTERVAL;
		}

		if (object instanceof ScanListItem) {
			return ItemType.SCAN;
		}

		return ((ReplicateListItem) object).getSampleId() == DatabaseConstants.EMPTY_DB_ID ? ItemType.STANDARD : ItemType.SAMPLE;
	}
}
