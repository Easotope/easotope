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

import org.easotope.client.core.ColorCache;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListItem;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateListItem;
import org.eclipse.swt.graphics.GC;

public class Selection {
	private CoordinateTransform coordinateTransform;
	private RenderableItems renderableItems;
	private YAxisTypes yAxisTypes;

	private Integer replicateId = null;
	private ReplicateListItem replicateListItem = null;
	private CorrIntervalListItem corrIntervalListItem = null;

	Selection(CoordinateTransform coordinateTransform, RenderableItems renderableItems, YAxisTypes yAxisTypes) {
		this.coordinateTransform = coordinateTransform;
		this.renderableItems = renderableItems;
		this.yAxisTypes = yAxisTypes;
	}

	void setSelection(ReplicateListItem replicateListItem) {
		if (replicateListItem != null) {
			this.replicateId = renderableItems.getId(replicateListItem);
			this.replicateListItem = replicateListItem;
			this.corrIntervalListItem = renderableItems.getCorrIntervalListItemForDate(replicateListItem.getDate());
		} else {
			this.replicateId = null;
			this.replicateListItem = null;
			this.corrIntervalListItem = null;
		}
	}

	void reset() {
		this.replicateId = null;
		this.corrIntervalListItem = null;
		this.replicateListItem = null;
	}

	CorrIntervalListItem getCorrIntervalListItem() {
		return corrIntervalListItem;
	}

	ReplicateListItem getReplicateListItem() {
		return replicateListItem;
	}

	void renderSelection(GC gc, int canvasSizeX) {
		gc.setBackground(ColorCache.getColor(gc.getDevice(), ColorCache.LIGHT_GREY));

		if (corrIntervalListItem != null) {
			int startX = coordinateTransform.timeInMinutesToPixelX((int) (corrIntervalListItem.getDate() / 1000 / 60));

			CorrIntervalListItem following = renderableItems.getFollowingCorrIntervalListItem(corrIntervalListItem);
			int endX = (following == null) ? canvasSizeX : coordinateTransform.timeInMinutesToPixelX((int) (following.getDate() / 1000 / 60));

			gc.fillRectangle(startX, 0, endX - startX, yAxisTypes.getCorrIntervalBaseY());
		}

		if (replicateListItem != null) {
			int x = coordinateTransform.timeInMinutesToPixelX((int) (replicateListItem.getDate() / 1000 / 60));
			gc.drawLine(x, 0, x, yAxisTypes.getCorrIntervalBaseY());
		}
	}

	public void updateReplicateListItem() {
		if (replicateId == null) {
			return;
		}

		replicateListItem = renderableItems.getReplicateListItem(replicateId);
		corrIntervalListItem = (replicateListItem != null) ? renderableItems.getCorrIntervalListItemForDate(replicateListItem.getDate()) : null;
	}
}
