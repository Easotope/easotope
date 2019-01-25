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

package org.easotope.shared.rawdata.tables;

import java.util.HashMap;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;
import org.easotope.shared.rawdata.InputParameter;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=ScanFileParsedV2.TABLE_NAME)
public class ScanFileParsedV2 extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "SCANFILEPARSED_V2";
	public static final String DATE_FIELD_NAME = "DATE";
	public static final String MEASUREMENTS_FIELD_NAME = "MEASUREMENTS";
	public static final String FROM_VOLTAGE_FIELD_NAME = "FROM_VOLTAGE";
	public static final String TO_VOLTAGE_FIELD_NAME = "TO_VOLTAGE";
	public static final String CHANNEL_TO_MZX10_FIELD_NAME = "CHANNELTOMZX10";

	@DatabaseField(columnName=DATE_FIELD_NAME, canBeNull=false, index=true)
	private long date;
	@DatabaseField(columnName=MEASUREMENTS_FIELD_NAME, canBeNull=false, dataType=DataType.SERIALIZABLE)
	private HashMap<InputParameter,Double[]> measurements;
	@DatabaseField(columnName=FROM_VOLTAGE_FIELD_NAME, canBeNull=false)
	private double fromVoltage;
	@DatabaseField(columnName=TO_VOLTAGE_FIELD_NAME, canBeNull=false)
	private double toVoltage;
	@DatabaseField(columnName=CHANNEL_TO_MZX10_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	public Integer[] channelToMzX10;

	public ScanFileParsedV2() { }

	public ScanFileParsedV2(ScanFileParsedV2 scanFileParsed) {
		super(scanFileParsed);

		this.date = scanFileParsed.date;
		this.measurements = scanFileParsed.measurements == null ? null : new HashMap<InputParameter,Double[]>(scanFileParsed.measurements);
		this.fromVoltage = scanFileParsed.fromVoltage;
		this.toVoltage = scanFileParsed.toVoltage;

		if (scanFileParsed.channelToMzX10 != null) {
			this.channelToMzX10 = new Integer[scanFileParsed.channelToMzX10.length];

			for (int i=0; i<scanFileParsed.channelToMzX10.length; i++) {
				this.channelToMzX10[i] = scanFileParsed.channelToMzX10[i];
			}
		}
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public HashMap<InputParameter,Double[]> getMeasurements() {
		return measurements;
	}

	public void setMeasurements(HashMap<InputParameter,Double[]> measurements) {
		this.measurements = measurements;
	}

	public double getFromVoltage() {
		return fromVoltage;
	}

	public void setFromVoltage(double fromVoltage) {
		this.fromVoltage = fromVoltage;
	}

	public double getToVoltage() {
		return toVoltage;
	}

	public void setToVoltage(double toVoltage) {
		this.toVoltage = toVoltage;
	}

	public Integer[] getChannelToMzX10() {
		return channelToMzX10;
	}

	public void setChannelToMzX10(Integer[] channelToMzX10) {
		this.channelToMzX10 = channelToMzX10;
	}
}
