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

package org.easotope.shared.rawdata.tables;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=ScanFileInputV0.TABLE_NAME)
public class ScanFileInputV0 extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "SCANFILEINPUT_V0";
	public static final String RAWFILEID_FIELD_NAME = "RAWFILEID";
	public static final String SCANID_FIELD_NAME = "SCANID";
	public static final String SCAN_FILE_PARSED_ID_FIELD_NAME = "SCANFILEPARSEDID";

	@DatabaseField(columnName=RAWFILEID_FIELD_NAME, canBeNull=false, index=true)
	private int rawFileId;
	@DatabaseField(columnName=SCANID_FIELD_NAME, canBeNull=false)
	private int scanId;
	@DatabaseField(columnName=SCAN_FILE_PARSED_ID_FIELD_NAME, canBeNull=false)
	private int scanFileParsedId;

	public ScanFileInputV0() { }

	public ScanFileInputV0(ScanFileInputV0 scanFileParsed) {
		super(scanFileParsed);

		this.rawFileId = scanFileParsed.rawFileId;
		this.scanId = scanFileParsed.scanId;
		this.scanFileParsedId = scanFileParsed.scanFileParsedId;
	}

	public int getRawFileId() {
		return rawFileId;
	}

	public void setRawFileId(int rawFileId) {
		this.rawFileId = rawFileId;
	}

	public int getScanId() {
		return scanId;
	}

	public void setScanId(int scanId) {
		this.scanId = scanId;
	}

	public int getScanFileParsedId() {
		return scanFileParsedId;
	}

	public void setScanFileParsedId(int scanFileParsedId) {
		this.scanFileParsedId = scanFileParsedId;
	}
}
