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

package org.easotope.framework.dbcore.tables;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=RawFile.TABLE_NAME)
public class RawFile extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "RAWFILE_V0";
	public static final String USERID_FIELD_NAME = "USERID";
	public static final String ORIGINALNAME_FIELD_NAME = "ORIGINALNAME";
	public static final String DATABASENAME_FIELD_NAME = "DATABASENAME";

	@DatabaseField(columnName=USERID_FIELD_NAME, canBeNull=false)
	private int userId;
	@DatabaseField(columnName=ORIGINALNAME_FIELD_NAME, canBeNull=false)
	private String originalName;
	@DatabaseField(columnName=DATABASENAME_FIELD_NAME, canBeNull=false, unique=true)
	private String databaseName;

	public RawFile() { }

	public RawFile(RawFile rawFile) {
		super(rawFile);

		this.userId = rawFile.userId;
		this.originalName = rawFile.originalName;
		this.databaseName = rawFile.databaseName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
}
