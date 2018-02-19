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

package org.easotope.shared.core.tables;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=Preferences.TABLE_NAME)
public class Preferences extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "PREFERENCES_V0";
	public static final String USERID_FIELD_NAME = "USERID";
	public static final String TIMEZONEID_FIELD_NAME = "TIMEZONEID";
	public static final String CHECKFORUPDATES_FIELD_NAME = "CHECKFORUPDATES";
	public static final String SHOWTIMEZONE_FIELD_NAME = "SHOWTIMEZONE";
	public static final String PREVIOUSBESTSERVER_FIELD_NAME = "PREVIOUSBESTSERVER";
	public static final String LEADINGEXPONENT_FIELD_NAME = "LEADINGEXPONENT";
	public static final String FORCEEXPONENT_FIELD_NAME = "FORCEEXPONENT";

	@DatabaseField(columnName=USERID_FIELD_NAME, uniqueIndex=true)
	private int userId;
	@DatabaseField(columnName=TIMEZONEID_FIELD_NAME)
	private String timeZoneId;
	@DatabaseField(columnName=CHECKFORUPDATES_FIELD_NAME)
	private boolean checkForUpdates = true;
	@DatabaseField(columnName=SHOWTIMEZONE_FIELD_NAME)
	private boolean showTimeZone = false;
	@DatabaseField(columnName=PREVIOUSBESTSERVER_FIELD_NAME)
	private int previousBestServer = 0;
	@DatabaseField(columnName=LEADINGEXPONENT_FIELD_NAME)
	private boolean leadingExponent = true;
	@DatabaseField(columnName=FORCEEXPONENT_FIELD_NAME)
	private boolean forceExponent = false;

	public Preferences() { }

	public Preferences(Preferences preferences) {
		id = preferences.id;
		userId = preferences.userId;
		timeZoneId = preferences.timeZoneId;
		checkForUpdates = preferences.checkForUpdates;
		showTimeZone = preferences.showTimeZone;
		previousBestServer = preferences.previousBestServer;
		leadingExponent = preferences.leadingExponent;
		forceExponent = preferences.forceExponent;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getTimeZoneId() {
		return timeZoneId;
	}

	public void setTimeZoneId(String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}

	public void setCheckForUpdates(boolean checkForUpdates) {
		this.checkForUpdates = checkForUpdates;
	}

	public boolean getCheckForUpdates() {
		return checkForUpdates;
	}

	public void setShowTimeZone(boolean showTimeZone) {
		this.showTimeZone = showTimeZone;
	}

	public boolean getShowTimeZone() {
		return showTimeZone;
	}

	public int getPreviousBestServer() {
		return previousBestServer;
	}

	public void setPreviousBestServer(int previousBestServer) {
		this.previousBestServer = previousBestServer;
	}

	public boolean getLeadingExponent() {
		return leadingExponent;
	}

	public void setLeadingExponent(boolean leadingExponent) {
		this.leadingExponent = leadingExponent;
	}

	public boolean getForceExponent() {
		return forceExponent;
	}

	public void setForceExponent(boolean forceExponent) {
		this.forceExponent = forceExponent;
	}
}
