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

package org.easotope.shared.admin.tables;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;
import org.easotope.shared.admin.SciConstantNames;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=SciConstant.TABLE_NAME)
public class SciConstant extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "SCICONSTANTS_V0";
	public static final String ENUMERATION_FIELD_NAME = "ENUMERATION";
	public static final String VALUE_FIELD_NAME = "VALUE";
	public static final String REFERENCE_FIELD_NAME = "REFERENCE";

	@DatabaseField(columnName=ENUMERATION_FIELD_NAME, canBeNull=false, uniqueIndex=true)
	public SciConstantNames enumeration;
	@DatabaseField(columnName=VALUE_FIELD_NAME, canBeNull=false)
	public double value;
	@DatabaseField(columnName=REFERENCE_FIELD_NAME, canBeNull=false)
	public String reference;

	public SciConstant() { }

	public SciConstant(SciConstant constant) {
		super(constant);

		this.enumeration = constant.enumeration;
		this.value = constant.value;
		this.reference = constant.reference;
	}

	public SciConstantNames getEnumeration() {
		return enumeration;
	}

	public void setEnumeration(SciConstantNames enumeration) {
		this.enumeration = enumeration;
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}
}
