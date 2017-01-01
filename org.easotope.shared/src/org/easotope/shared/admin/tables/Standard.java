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

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;
import org.easotope.shared.core.NumericValue;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=Standard.TABLE_NAME)
public class Standard extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "STANDARD_V0";
	public static final String NAME_FIELD_NAME = "NAME";
	public static final String COLORID_FIELD_NAME = "COLORID";
	public static final String SHAPEID_FIELD_NAME = "SHAPEID";
	public static final String SAMPLETYPEID_FIELD_NAME = "SAMPLETYPEID";
	public static final String DESCRIPTION_FIELD_NAME = "DESCRIPTION";
	public static final String VALUES_FIELD_NAME = "VALUES";

	@DatabaseField(columnName=NAME_FIELD_NAME, canBeNull=false, unique=true)
	public String name;
	@DatabaseField(columnName=COLORID_FIELD_NAME, canBeNull=false)
	public int colorId;
	@DatabaseField(columnName=SHAPEID_FIELD_NAME, canBeNull=false)
	public int shapeId;
	@DatabaseField(columnName=SAMPLETYPEID_FIELD_NAME, canBeNull=false)
	public int sampleTypeId;
	@DatabaseField(columnName=DESCRIPTION_FIELD_NAME, canBeNull=false)
	public String description;
	@DatabaseField(columnName=VALUES_FIELD_NAME, canBeNull=false, dataType=DataType.SERIALIZABLE)
	private HashMap<Integer,Object[]> values;

	private transient Map<Integer,NumericValue> valuesAsNumericValues = null;
	
	public Standard() { }

	public Standard(Standard standard) {
		super(standard);

		this.name = standard.name;
		this.colorId = standard.colorId;
		this.shapeId = standard.shapeId;
		this.sampleTypeId = standard.sampleTypeId;
		this.description = standard.description;
		this.values = new HashMap<Integer,Object[]>(standard.values);

		for (int key : standard.values.keySet()) {
			Object[] objects = standard.values.get(key);

			if (objects == null) {
				this.values.put(key, null);
			} else {
				this.values.put(key, new Object[] { objects[0], objects[1], objects[2] });
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getColorId() {
		return colorId;
	}

	public void setColorId(int colorId) {
		this.colorId = colorId;
	}

	public int getShapeId() {
		return shapeId;
	}

	public void setShapeId(int shapeId) {
		this.shapeId = shapeId;
	}

	public int getSampleTypeId() {
		return sampleTypeId;
	}

	public void setSampleTypeId(int sampleTypeId) {
		this.sampleTypeId = sampleTypeId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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
}
