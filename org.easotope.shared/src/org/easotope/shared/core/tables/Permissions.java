/*
 * Copyright © 2016 by Devon Bowen.
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

@DatabaseTable(tableName=Permissions.TABLE_NAME)
public class Permissions extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "PERMISSIONS_V0";
	public static final String USERID_FIELD_NAME = "USERID";
	public static final String CANEDITMASSSPECS_FIELD_NAME = "CANEDITMASSSPECS";
	public static final String CANEDITSAMPLETYPES_FIELD_NAME = "CANEDITSAMPLETYPES";
	public static final String CANEDITSTANDARDS_FIELD_NAME = "CANEDITSTANDARDS";
	public static final String CANEDITALLREPLICATES_FIELD_NAME = "CANEDITALLREPLICATES";
	public static final String CANEDITCORRINTERVALS_FIELD_NAME = "CANEDITCORRINTERVALS";
	public static final String CANDELETEALL_FIELD_NAME = "CANDELETEALL";
	public static final String CANDELETEOWN_FIELD_NAME = "CANDELETEOWN";
	public static final String CANEDITCONSTANTS_FIELD_NAME = "CANEDITCONSTANTS";

	@DatabaseField(columnName=USERID_FIELD_NAME, uniqueIndex=true)
	public int userId;
	@DatabaseField(columnName=CANEDITMASSSPECS_FIELD_NAME)
	private boolean canEditMassSpecs;
	@DatabaseField(columnName=CANEDITSAMPLETYPES_FIELD_NAME)
	private boolean canEditSampleTypes;
	@DatabaseField(columnName=CANEDITSTANDARDS_FIELD_NAME)
	private boolean canEditStandards;
	@DatabaseField(columnName=CANEDITALLREPLICATES_FIELD_NAME)
	private boolean canEditAllReplicates;
	@DatabaseField(columnName=CANEDITCORRINTERVALS_FIELD_NAME)
	private boolean canEditCorrIntervals;
	@DatabaseField(columnName=CANDELETEALL_FIELD_NAME)
	private boolean canDeleteAll;
	@DatabaseField(columnName=CANDELETEOWN_FIELD_NAME)
	private boolean canDeleteOwn;
	@DatabaseField(columnName=CANEDITCONSTANTS_FIELD_NAME)
	private boolean canEditConstants;

	public Permissions() { }

	public Permissions(Permissions permissions) {
		super(permissions);

		userId = permissions.id;
		canEditMassSpecs = permissions.canEditMassSpecs;
		canEditSampleTypes = permissions.canEditSampleTypes;
		canEditStandards = permissions.canEditStandards;
		canEditAllReplicates = permissions.canEditAllReplicates;
		canEditCorrIntervals = permissions.canEditCorrIntervals;
		canDeleteAll = permissions.canDeleteAll;
		canDeleteOwn = permissions.canDeleteOwn;
		canEditConstants = permissions.canEditConstants;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public boolean isCanEditMassSpecs() {
		return canEditMassSpecs;
	}

	public void setCanEditMassSpecs(boolean canEditMassSpecs) {
		this.canEditMassSpecs = canEditMassSpecs;
	}

	public boolean isCanEditSampleTypes() {
		return canEditSampleTypes;
	}

	public void setCanEditSampleTypes(boolean canEditSampleTypes) {
		this.canEditSampleTypes = canEditSampleTypes;
	}

	public boolean isCanEditStandards() {
		return canEditStandards;
	}

	public void setCanEditStandards(boolean canEditStandards) {
		this.canEditStandards = canEditStandards;
	}

	public boolean isCanEditAllReplicates() {
		return canEditAllReplicates;
	}

	public void setCanEditAllReplicates(boolean canEditAllReplicates) {
		this.canEditAllReplicates = canEditAllReplicates;
	}

	public boolean isCanEditCorrIntervals() {
		return canEditCorrIntervals;
	}

	public void setCanEditCorrIntervals(boolean canEditCorrIntervals) {
		this.canEditCorrIntervals = canEditCorrIntervals;
	}

	public boolean isCanDeleteAll() {
		return canDeleteAll;
	}

	public void setCanDeleteAll(boolean canDeleteAll) {
		this.canDeleteAll = canDeleteAll;
	}

	public boolean isCanDeleteOwn() {
		return canDeleteOwn;
	}

	public void setCanDeleteOwn(boolean canDeleteOwn) {
		this.canDeleteOwn = canDeleteOwn;
	}
	
	public boolean isCanEditConstants() {
		return canEditConstants;
	}

	public void setCanEditConstants(boolean canEditConstants) {
		this.canEditConstants = canEditConstants;
	}
}
