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

package org.easotope.shared.rawdata;

import java.io.Serializable;

import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;

public class Acquisition implements Comparable<Acquisition>, Serializable {
	private static final long serialVersionUID = 1L;

	byte[] fileBytes;
	RawFile rawFile;
	AcquisitionParsedV2 acquisitionParsed;
	AcquisitionInputV0 acquisitionInput;

	public Acquisition(byte[] fileBytes, RawFile rawFile, AcquisitionParsedV2 acquisitionParsed, AcquisitionInputV0 acquisitionInput) {
		this.fileBytes = fileBytes;
		this.rawFile = rawFile;
		this.acquisitionParsed = acquisitionParsed;
		this.acquisitionInput = acquisitionInput;
	}

	public Acquisition(Acquisition acquisition) {
		this.fileBytes = acquisition.fileBytes == null ? null : acquisition.fileBytes.clone();
		this.rawFile = acquisition.rawFile == null ? null : new RawFile(acquisition.rawFile);
		this.acquisitionParsed = acquisition.acquisitionParsed == null ? null : new AcquisitionParsedV2(acquisition.acquisitionParsed);
		this.acquisitionInput = acquisition.acquisitionInput == null ? null : new AcquisitionInputV0(acquisition.acquisitionInput);
	}

	public boolean isCompatibleWith(Acquisition that) {
		Integer[] thisKeys = this.getAcquisitionParsed().getChannelToMzX10();
		Integer[] thatKeys = that.getAcquisitionParsed().getChannelToMzX10();

		if (thisKeys == null && thatKeys != null) {
			return false;
		}

		if (thisKeys != null && thatKeys == null) {
			return false;
		}

		if (thisKeys != null) {
			if (thisKeys.length != thatKeys.length) {
				return false;
			}
	
			for (int i=0; i<thisKeys.length; i++) {
				if (!thisKeys[i].equals(thatKeys[i])) {
					return false;
				}
			}
		}

		return true;
	}

	public byte[] getFileBytes() {
		return fileBytes;
	}

	public void setFileBytes(byte[] fileBytes) {
		this.fileBytes = fileBytes;
	}

	public RawFile getRawFile() {
		return rawFile;
	}

	public void setRawFile(RawFile rawFile) {
		this.rawFile = rawFile;
	}

	public AcquisitionInputV0 getAcquisitionInput() {
		return acquisitionInput;
	}

	public AcquisitionParsedV2 getAcquisitionParsed() {
		return acquisitionParsed;
	}

	public void setAcquisitionParsed(AcquisitionParsedV2 acquisitionParsed) {
		this.acquisitionParsed = acquisitionParsed;
	}

	@Override
	public int compareTo(Acquisition that) {
		return ((Long) this.acquisitionParsed.getDate()).compareTo(that.acquisitionParsed.getDate());
	}

	@Override
	public boolean equals(Object object) {
		Acquisition that = (Acquisition) object;

		if (that == null) {
			return false;
		}

		if (fileBytes != that.fileBytes || acquisitionInput.getRawFileId() != that.getAcquisitionInput().getRawFileId()) {
			return false;
		}

		return acquisitionInput.equals(that.acquisitionInput);
	}
}
