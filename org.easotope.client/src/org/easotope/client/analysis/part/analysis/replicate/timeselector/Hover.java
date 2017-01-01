/*
 * Copyright © 2016-2017 by Devon Bowen.
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
import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.replicate.timeselector.YAxisTypes.ItemType;
import org.easotope.client.core.ColorCache;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListItem;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateListItem;
import org.easotope.shared.rawdata.cache.input.scanlist.ScanListItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

public class Hover {
	private CoordinateTransform coordinateTransform;
	private YAxisTypes yAxisTypes;

	private ItemType itemType = ItemType.NONE;
	private ReplicateListItem replicateListItem;
	private ScanListItem scanListItem;
	private CorrIntervalListItem corrIntervalListItem;

	private HashMap<Integer,String> standardIdToName = new HashMap<Integer,String>();
	private UserList userList = null;
	private FindObject findObject = null;

	Hover(CoordinateTransform coordinateTransform, YAxisTypes yAxisTypes, FindObject findObject) {
		this.coordinateTransform = coordinateTransform;
		this.yAxisTypes = yAxisTypes;
		this.findObject = findObject;
	}

	void setStandardName(int id, String name) {
		standardIdToName.put(id, name);
	}

	void setUserList(UserList userList) {
		this.userList = userList;
	}

	void setHoverObject() {
		this.itemType = findObject.getItemType();
		this.replicateListItem = findObject.getReplicateListItem();
		this.scanListItem = findObject.getScanListItem();
		this.corrIntervalListItem = findObject.getCorrIntervalListItem();
	}

	void reset() {
		itemType = YAxisTypes.ItemType.NONE;
	}

	void drawHoverInfo(GC gc, int canvasSizeX) {
		if (itemType == ItemType.NONE) {
			return;
		}

		String text = "";

		for (String string : getDisplayInfo(replicateListItem, scanListItem, corrIntervalListItem)) {
			text += text.isEmpty() ? string : "\n" + string;
		}

		long date;

		if (itemType == ItemType.CORRINTERVAL) {
			date = corrIntervalListItem.getDate();
		} else if (itemType == ItemType.SCAN) {
			date = scanListItem.getDate();
		} else {
			date = replicateListItem.getDate();
		}

		int mouseX = coordinateTransform.timeInMinutesToPixelX((int) (date / 1000 / 60));

		Font font = new Font(gc.getDevice(), "Arial", 12, SWT.NORMAL);
		gc.setFont(font);

		gc.setForeground(ColorCache.getColor(gc.getDevice(), ColorCache.BLACK));
		gc.setBackground(ColorCache.getColor(gc.getDevice(), ColorCache.WHITE));

		Point textExtent = gc.textExtent(text);

		int cornerX = 0;
		int cornerY = 0;
		int padding = 3;

		if (canvasSizeX / 2 < mouseX) {
			cornerX = mouseX - textExtent.x - 2 * padding - 8;
		} else {
			cornerX = mouseX + 10;
		}

		switch (itemType) {
			case NONE:
				break;

			case SAMPLE:
				cornerY = yAxisTypes.getSamplesY();
				break;

			case STANDARD:
				cornerY = yAxisTypes.getStandardsY();
				break;

			case SCAN:
				cornerY = yAxisTypes.getScansY();
				break;

			case CORRINTERVAL:
				cornerY = yAxisTypes.getCorrIntervalBaseY();
				break;
		}

		int textBoxHeight = textExtent.y - padding * 2;
		cornerY -= textBoxHeight / 2;

		if (cornerY > yAxisTypes.getCorrIntervalBaseY() - textBoxHeight) {
			cornerY = yAxisTypes.getCorrIntervalBaseY() - textBoxHeight;
		}

		if (cornerY < 2) {
			cornerY = 2;
		}

		gc.fillRectangle(cornerX, cornerY, textExtent.x + 2 * padding, textExtent.y + 2 * padding);
		gc.drawRectangle(cornerX, cornerY, textExtent.x + 2 * padding, textExtent.y + 2 * padding);

		gc.drawText(text, cornerX + padding, cornerY + padding);
		
		font.dispose();
	}

	ArrayList<String> getDisplayInfo(ReplicateListItem replicateListItem, ScanListItem scanListItem, CorrIntervalListItem corrIntervalListItem) {
		ArrayList<String> list = new ArrayList<String>();

		if (replicateListItem != null) {
			if (replicateListItem.getSampleId() != DatabaseConstants.EMPTY_DB_ID) {
				list.add(Messages.hover_sample + replicateListItem.getSampleName());
				list.add(Messages.hover_user + ((userList != null) ? userList.get(replicateListItem.getUserId()).getName() : replicateListItem.getUserId()));
				String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
				boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
				list.add(Messages.hover_time + DateFormat.format(replicateListItem.getDate(), timeZone, showTimeZone, false));

			} else {
				String standardName = standardIdToName.get(replicateListItem.getStandardId());

				if (standardName == null) {
					standardName = String.valueOf(replicateListItem.getStandardId());
				}

				list.add(Messages.hover_standard + standardName);
				list.add(Messages.hover_user + ((userList != null) ? userList.get(replicateListItem.getUserId()).getName() : replicateListItem.getUserId()));
				String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
				boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
				list.add(Messages.hover_time + DateFormat.format(replicateListItem.getDate(), timeZone, showTimeZone, false));
			}

		} else if (scanListItem != null) {
			list.add(Messages.hover_scanTitle);
			list.add(Messages.hover_user + ((userList != null) ? userList.get(scanListItem.getUserId()).getName() : scanListItem.getUserId()));
			String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			list.add(Messages.hover_time + DateFormat.format(scanListItem.getDate(), timeZone, showTimeZone, false));

		} else if (corrIntervalListItem != null) {
			list.add(Messages.hover_corrIntervalTitle);
			String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			list.add(Messages.hover_time + DateFormat.format(corrIntervalListItem.getDate(), timeZone, showTimeZone, false));
		}

		return list;
	}
}
