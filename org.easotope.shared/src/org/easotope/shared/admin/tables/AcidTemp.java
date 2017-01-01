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

@DatabaseTable(tableName=AcidTemp.TABLE_NAME)
public class AcidTemp extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "ACIDTEMP_V0";
	public static final String TEMPERATURE_FIELD_NAME = "TEMPERATURE";
	public static final String SAMPLETYPEID_FIELD_NAME = "SAMPLETYPEID";
	public static final String DESCRIPTION_FIELD_NAME = "DESCRIPTION";
	public static final String VALUES_FIELD_NAME = "VALUES";

	@DatabaseField(columnName=TEMPERATURE_FIELD_NAME, canBeNull=false)
	public double temperature;
	@DatabaseField(columnName=SAMPLETYPEID_FIELD_NAME, canBeNull=false)
	public int sampleTypeId;
	@DatabaseField(columnName=DESCRIPTION_FIELD_NAME, canBeNull=false)
	public String description;
	@DatabaseField(columnName=VALUES_FIELD_NAME, canBeNull=false, dataType=DataType.SERIALIZABLE)
	private HashMap<Integer,Object[]> values;

	private transient Map<Integer,NumericValue> valuesAsNumericValues = null;

	public AcidTemp() { }

	public AcidTemp(AcidTemp acidTemp) {
		super(acidTemp);

		this.temperature = acidTemp.temperature;
		this.sampleTypeId = acidTemp.sampleTypeId;
		this.description = acidTemp.description;
		this.values = new HashMap<Integer,Object[]>();

		for (int key : acidTemp.values.keySet()) {
			Object[] objects = acidTemp.values.get(key);

			if (objects == null) {
				this.values.put(key, null);
			} else {
				this.values.put(key, new Object[] { objects[0], objects[1], objects[2] });
			}
		}
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
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
			return (HashMap<Integer,NumericValue>) Collections.unmodifiableMap(valuesAsNumericValues);
		}

		if (values == null) {
			return null;
		}

		HashMap<Integer,NumericValue> valuesAsNumericValues = new HashMap<Integer,NumericValue>();

		for (Integer key : values.keySet()) {
			Object[] objects = values.get(key);
			
			if (objects != null) {
				valuesAsNumericValues.put(key, new NumericValue((Double) objects[0], (Integer) objects[1], (String) objects[2]));
			}
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
			
			if (numericValue != null) {
				values.put(key, new Object[] { numericValue.getValue(), numericValue.getDescription(), numericValue.getReference() });
			}
		}
	}
}
