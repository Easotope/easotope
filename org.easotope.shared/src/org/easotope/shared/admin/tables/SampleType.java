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

package org.easotope.shared.admin.tables;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=SampleType.TABLE_NAME)
public class SampleType extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "SAMPLETYPE_V0";
	public static final String NAME_FIELD_NAME = "NAME";
	public static final String DESCRIPTION_FIELD_NAME = "DESCRIPTION";
	public static final String HASACIDTEMPS_FIELD_NAME = "HASACIDTEMP";
	public static final String DEFAULT_ACID_TEMP_ID_FIELD_NAME = "DEFAULTACIDTEMPID";

	@DatabaseField(columnName=NAME_FIELD_NAME, canBeNull=false, unique=true)
	public String name;
	@DatabaseField(columnName=DESCRIPTION_FIELD_NAME, canBeNull=false)
	public String description;
	@DatabaseField(columnName=HASACIDTEMPS_FIELD_NAME, canBeNull=false)
	public boolean hasAcidTemps;
	@DatabaseField(columnName=DEFAULT_ACID_TEMP_ID_FIELD_NAME, canBeNull=false)
	public int defaultAcidTemp;

	public SampleType() { }

	public SampleType(SampleType sampleType) {
		super(sampleType);

		this.name = sampleType.name;
		this.description = sampleType.description;
		this.hasAcidTemps = sampleType.hasAcidTemps;
		this.defaultAcidTemp = sampleType.defaultAcidTemp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean getHasAcidTemps() {
		return hasAcidTemps;
	}

	public void setHasAcidTemps(boolean hasAcidTemps) {
		this.hasAcidTemps = hasAcidTemps;
	}

	public int getDefaultAcidTemp() {
		return defaultAcidTemp;
	}

	public void setDefaultAcidTemp(int defaultAcidTemp) {
		this.defaultAcidTemp = defaultAcidTemp;
	}
}
