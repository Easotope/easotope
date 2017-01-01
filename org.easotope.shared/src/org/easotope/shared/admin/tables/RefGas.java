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

package org.easotope.shared.admin.tables;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;
import org.easotope.shared.core.NumericValue;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=RefGas.TABLE_NAME)
public class RefGas extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "REFERENCEGAS_V0";
	public static final String MASSSPECID_FIELD_NAME = "MASSSPECID";
	public static final String DESCRIPTION_FIELD_NAME = "DESCRIPTION";
	public static final String VALIDFROM_FIELD_NAME = "VALIDFROM";
	public static final String VALIDUNTIL_FIELD_NAME = "VALIDUNTIL";
	public static final String VALUES_FIELD_NAME = "VALUES";

	@DatabaseField(columnName=MASSSPECID_FIELD_NAME, canBeNull=false)
	private int massSpecId;
	@DatabaseField(columnName=DESCRIPTION_FIELD_NAME, canBeNull=false)
	private String description;
	@DatabaseField(columnName=VALIDFROM_FIELD_NAME, canBeNull=false)
	private long validFrom = DatabaseConstants.EMPTY_DATE;
	@DatabaseField(columnName=VALIDUNTIL_FIELD_NAME, canBeNull=false)
	private long validUntil = DatabaseConstants.EMPTY_DATE;
	@DatabaseField(columnName=VALUES_FIELD_NAME, canBeNull=false, dataType=DataType.SERIALIZABLE)
	private HashMap<Integer,Object[]> values;

	private transient Map<Integer,NumericValue> valuesAsNumericValues = null;

	public RefGas() { }

	public RefGas(RefGas refGas) {
		super(refGas);

		this.massSpecId = refGas.massSpecId;
		this.description = refGas.description;
		this.validFrom = refGas.validFrom;
		this.validUntil = refGas.validUntil;
		this.values = new HashMap<Integer,Object[]>(refGas.values);

		for (int key : refGas.values.keySet()) {
			Object[] objects = refGas.values.get(key);

			if (objects == null) {
				this.values.put(key, null);
			} else {
				this.values.put(key, new Object[] { objects[0], objects[1], objects[2] });
			}
		}
	}
	
	public int getMassSpecId() {
		return massSpecId;
	}

	public void setMassSpecId(int massSpecId) {
		this.massSpecId = massSpecId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(long validFrom) {
		this.validFrom = validFrom;
	}

	public long getValidUntil() {
		return validUntil;
	}

	public void setValidUntil(long validUntil) {
		this.validUntil = validUntil;
	}

	public Map<Integer,NumericValue> getValues() {
		if (valuesAsNumericValues != null) {
			return (Map<Integer,NumericValue>) Collections.unmodifiableMap(valuesAsNumericValues);
		}

		if (values == null) {
			return null;
		}

		HashMap<Integer,NumericValue> valuesAsNumericValues = new HashMap<Integer,NumericValue>();

		for (Integer key : values.keySet()) {
			Object[] objects = values.get(key);
			valuesAsNumericValues.put(key, new NumericValue((Double) objects[0], (Integer) objects[1], (String) objects[2]));
		}

		return (Map<Integer,NumericValue>) Collections.unmodifiableMap(valuesAsNumericValues);
	}

	public void setValues(Map<Integer,NumericValue> valuesAsNumericValues) {
		this.valuesAsNumericValues = valuesAsNumericValues;

		if (valuesAsNumericValues == null) {
			values = null;
			return;
		}

		values = new HashMap<Integer,Object[]>();

		for (Integer key : valuesAsNumericValues.keySet()) {
			NumericValue numericValue = valuesAsNumericValues.get(key);
			values.put(key, new Object[] { numericValue.getValue(), numericValue.getDescription(), numericValue.getReference() });
		}
	}

	public boolean validForTime(long date) {
		return date > validFrom && date < validUntil;
	}
}
