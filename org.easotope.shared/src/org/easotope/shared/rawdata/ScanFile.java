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
import java.util.Set;

import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.shared.rawdata.tables.ScanFileInputV0;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;

public class ScanFile implements Comparable<ScanFile>, Serializable {
	private static final long serialVersionUID = 1L;

	byte[] fileBytes;
	RawFile rawFile;
	ScanFileInputV0 scanFileInput;
	ScanFileParsedV2 scanFileParsed;

	public ScanFile(byte[] fileBytes, RawFile rawFile, ScanFileInputV0 scanFileInput, ScanFileParsedV2 scanFileParsed) {
		this.fileBytes = fileBytes;
		this.rawFile = rawFile;
		this.scanFileInput = scanFileInput;
		this.scanFileParsed = scanFileParsed;
	}

	public ScanFile(ScanFile scanFile) {
		this.fileBytes = scanFile.fileBytes == null ? null : scanFile.fileBytes.clone();
		this.rawFile = scanFile.rawFile == null ? null : new RawFile(scanFile.rawFile);
		this.scanFileInput = scanFile.scanFileInput == null ? null : new ScanFileInputV0(scanFile.scanFileInput);
		this.scanFileParsed = scanFile.scanFileParsed == null ? null : new ScanFileParsedV2(scanFile.scanFileParsed);
	}

	public boolean isCompatibleWith(ScanFile that) {		
		Set<InputParameter> thisKeys = this.getScanFileParsed().getMeasurements().keySet();
		Set<InputParameter> thatKeys = that.getScanFileParsed().getMeasurements().keySet();

		if (thisKeys.size() != thatKeys.size()) {
			return false;
		}

		for (InputParameter parameter : thisKeys) {
			if (!thatKeys.contains(parameter)) {
				return false;
			}

			Double[] thisList = this.getScanFileParsed().getMeasurements().get(parameter);
			Double[] thatList = that.getScanFileParsed().getMeasurements().get(parameter);

			if (thisList.length != thatList.length) {
				return false;
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

	public ScanFileInputV0 getScanFileInput() {
		return scanFileInput;
	}

	public void setScanFileInput(ScanFileInputV0 scanFileInput) {
		this.scanFileInput = scanFileInput;
	}

	public ScanFileParsedV2 getScanFileParsed() {
		return scanFileParsed;
	}

	public void setScanFileParsed(ScanFileParsedV2 scanFileParsed) {
		this.scanFileParsed = scanFileParsed;
	}

	@Override
	public int compareTo(ScanFile that) {
		return ((Long) this.scanFileParsed.getDate()).compareTo(that.scanFileParsed.getDate());
	}

	@Override
	public boolean equals(Object object) {
		ScanFile that = (ScanFile) object;

		if (that == null) {
			return false;
		}

		if (fileBytes != that.fileBytes || scanFileInput.getRawFileId() != that.getScanFileInput().getRawFileId()) {
			return false;
		}

		return true;
	}
}
