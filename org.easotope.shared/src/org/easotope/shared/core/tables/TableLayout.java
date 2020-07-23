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

package org.easotope.shared.core.tables;

import java.util.Arrays;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=TableLayout.TABLE_NAME)
public class TableLayout extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "TABLELAYOUT_V0";
	public static final String USERID_FIELD_NAME = "USERID";
	public static final String CONTEXT_FIELD_NAME = "CONTEXT";
	public static final String DATAANALYSISID_FIELD_NAME = "DATAANALYSISID";
	public static final String COLUMNORDER_FIELD_NAME = "COLUMNORDER";
	public static final String COLUMNWIDTH_FIELD_NAME = "COLUMNWIDTH";
	public static final String FORMATTINGON_FIELD_NAME = "FORMATTINGON";
	public static final String HIDEREPLICATES_FIELD_NAME = "HIDEREPLICATES";
	public static final String HIDEACQUSITIONS_FIELD_NAME = "HIDEACQUSITIONS";
	public static final String HIDECYCLES_FIELD_NAME = "HIDECYCLES";

	@DatabaseField(columnName=USERID_FIELD_NAME)
	private int userId;
	@DatabaseField(columnName=CONTEXT_FIELD_NAME)
	private String context;
	@DatabaseField(columnName=DATAANALYSISID_FIELD_NAME)
	private int dataAnalysisId;
	@DatabaseField(columnName=COLUMNORDER_FIELD_NAME,dataType=DataType.SERIALIZABLE)
	private String[] columnOrder;
	@DatabaseField(columnName=COLUMNWIDTH_FIELD_NAME,dataType=DataType.SERIALIZABLE)
	private int[] columnWidth;
	@DatabaseField(columnName=FORMATTINGON_FIELD_NAME)
	private boolean formattingOn;
	@DatabaseField(columnName=HIDEREPLICATES_FIELD_NAME)
	private boolean hideReplicates;
	@DatabaseField(columnName=HIDEACQUSITIONS_FIELD_NAME)
	private boolean hideAcquisitions;
	@DatabaseField(columnName=HIDECYCLES_FIELD_NAME)
	private boolean hideCycles;

	public TableLayout() { }

	public TableLayout(TableLayout tableLayout) {
		super(tableLayout);

		userId = tableLayout.userId;
		context = tableLayout.context;
		dataAnalysisId = tableLayout.dataAnalysisId;

		if (tableLayout.columnOrder != null) {
			columnOrder = Arrays.copyOf(tableLayout.columnOrder, tableLayout.columnOrder.length);
		}

		if (tableLayout.columnWidth != null) {
			columnWidth = Arrays.copyOf(tableLayout.columnWidth, tableLayout.columnWidth.length);
		}

		formattingOn = tableLayout.formattingOn;
		hideReplicates = tableLayout.hideReplicates;
		hideAcquisitions = tableLayout.hideAcquisitions;
		hideCycles = tableLayout.hideCycles;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public int getDataAnalysisId() {
		return dataAnalysisId;
	}

	public void setDataAnalysisId(int dataAnalysisId) {
		this.dataAnalysisId = dataAnalysisId;
	}

	public String[] getColumnOrder() {
		return columnOrder;
	}

	public void setColumnOrder(String[] columnOrder) {
		this.columnOrder = columnOrder;
	}

	public int[] getColumnWidth() {
		return columnWidth;
	}

	public void setColumnWidth(int[] columnWidth) {
		this.columnWidth = columnWidth;
	}

	public boolean isFormattingOn() {
		return formattingOn;
	}

	public void setFormattingOn(boolean formattingOn) {
		this.formattingOn = formattingOn;
	}

	public boolean isHideReplicates() {
		return hideReplicates;
	}

	public void setHideReplicates(boolean hideReplicates) {
		this.hideReplicates = hideReplicates;
	}

	public boolean isHideAcquisitions() {
		return hideAcquisitions;
	}

	public void setHideAcquisitions(boolean hideAcquisitions) {
		this.hideAcquisitions = hideAcquisitions;
	}

	public boolean isHideCycles() {
		return hideCycles;
	}

	public void setHideCycles(boolean hideCycles) {
		this.hideCycles = hideCycles;
	}
}
