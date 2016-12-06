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

package org.easotope.shared.rawdata.tables;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=AcquisitionInputV0.TABLE_NAME)
public class AcquisitionInputV0 extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "ACQUISITIONINPUT_V0";
	public static final String RAWFILEID_FIELD_NAME = "RAWFILEID";
	public static final String REPLICATEID_FIELD_NAME = "REPLICATEID";
	public static final String DISABLED_FIELD_NAME = "DISABLED";
	public static final String DISABLED_CYCLES_FIELD_NAME = "DISABLEDCYCLES";
	public static final String OFF_PEAK_CYCLES_FIELD_NAME = "OFFPEAKCYCLES";
	public static final String ACQUISITION_PARSED_ID_FIELD_NAME = "ACQUISITIONPARSEDID";
	public static final String ASSUMED_TIME_ZONE_FIELD_NAME = "ASSUMEDTIMEZONE";
	public static final String ACQUISITION_NUMBER_FIELD_NAME = "ACQUISITIONNUMBER";

	@DatabaseField(columnName=RAWFILEID_FIELD_NAME, canBeNull=false, index=true)
	private int rawFileId;
	@DatabaseField(columnName=REPLICATEID_FIELD_NAME, canBeNull=false)
	private int replicateId;
	@DatabaseField(columnName=DISABLED_FIELD_NAME, canBeNull=false)
	private boolean disabled;
	@DatabaseField(columnName=DISABLED_CYCLES_FIELD_NAME, canBeNull=false, dataType=DataType.SERIALIZABLE)
	private boolean[] disabledCycles;
	@DatabaseField(columnName=OFF_PEAK_CYCLES_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	private boolean[] offPeakCycles;
	@DatabaseField(columnName=ACQUISITION_PARSED_ID_FIELD_NAME)
	private int acquisitionParsedId;
	@DatabaseField(columnName=ASSUMED_TIME_ZONE_FIELD_NAME)
	private String assumedTimeZone;
	@DatabaseField(columnName=ACQUISITION_NUMBER_FIELD_NAME)
	private int acquisitionNumber;

	public AcquisitionInputV0() { }

	public AcquisitionInputV0(AcquisitionInputV0 acquisitionInput) {
		super(acquisitionInput);

		this.rawFileId = acquisitionInput.rawFileId;
		this.replicateId = acquisitionInput.replicateId;
		this.disabled = acquisitionInput.disabled;
		this.disabledCycles = acquisitionInput.disabledCycles == null ? null : acquisitionInput.disabledCycles.clone();
		this.offPeakCycles = acquisitionInput.offPeakCycles == null ? null : acquisitionInput.offPeakCycles.clone();
		this.acquisitionParsedId = acquisitionInput.acquisitionParsedId;
		this.assumedTimeZone = acquisitionInput.assumedTimeZone;
		this.acquisitionNumber = acquisitionInput.acquisitionNumber;

		// don't forget to modify equals() below
	}

	public int getRawFileId() {
		return rawFileId;
	}

	public void setRawFileId(int rawFileId) {
		this.rawFileId = rawFileId;
	}

	public int getReplicateId() {
		return replicateId;
	}

	public void setReplicateId(int replicateId) {
		this.replicateId = replicateId;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public boolean[] getDisabledCycles() {
		return disabledCycles;
	}

	public void setDisabledCycles(boolean[] disabledCycles) {
		this.disabledCycles = disabledCycles;
	}

	public boolean[] getOffPeakCycles() {
		return offPeakCycles;
	}

	public void setOffPeakCycles(boolean[] offPeakCycles) {
		this.offPeakCycles = offPeakCycles;
	}

	public int getAcquisitionParsedId() {
		return acquisitionParsedId;
	}

	public void setAcquisitionParsedId(int acquisitionParsedId) {
		this.acquisitionParsedId = acquisitionParsedId;
	}

	public String getAssumedTimeZone() {
		return assumedTimeZone;
	}

	public void setAssumedTimeZone(String assumedTimeZone) {
		this.assumedTimeZone = assumedTimeZone;
	}

	public int getAcquisitionNumber() {
		return acquisitionNumber;
	}

	public void setAcquisitionNumber(int acquisitionNumber) {
		this.acquisitionNumber = acquisitionNumber;
	}

	@Override
	public boolean equals(Object object) {
		AcquisitionInputV0 that = (AcquisitionInputV0) object;

		if (that == null) {
			return false;
		}

		if (this.rawFileId != that.rawFileId) {
			return false;
		}

		if (this.replicateId != that.replicateId) {
			return false;
		}

		if (this.disabled != that.disabled) {
			return false;
		}

		if (this.disabledCycles != null || that.disabledCycles != null) {
			if (this.disabledCycles == null || that.disabledCycles == null) {
				return false;
			}
	
			if (this.disabledCycles.length != that.disabledCycles.length) {
				return false;
			}
	
			for (int i=0; i<this.disabledCycles.length; i++) {
				if (this.disabledCycles[i] != that.disabledCycles[i]) {
					return false;
				}
			}
		}

		if (this.offPeakCycles != null || that.offPeakCycles != null) {
			if (this.offPeakCycles == null || that.offPeakCycles == null) {
				return false;
			}
	
			if (this.offPeakCycles.length != that.offPeakCycles.length) {
				return false;
			}
	
			for (int i=0; i<this.offPeakCycles.length; i++) {
				if (this.offPeakCycles[i] != that.offPeakCycles[i]) {
					return false;
				}
			}
		}

		if (this.acquisitionParsedId != that.acquisitionParsedId) {
			return false;
		}

		if (this.assumedTimeZone != null && that.assumedTimeZone == null) {
			return false;
		}

		if (this.assumedTimeZone == null && that.assumedTimeZone != null) {
			return false;
		}

		if (this.assumedTimeZone != null && !this.assumedTimeZone.equals(that.assumedTimeZone)) {
			return false;
		}

		if (this.acquisitionNumber != that.acquisitionNumber) {
			return false;
		}

		return true;
	}
}
