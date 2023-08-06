/*
 * Copyright © 2016-2023 by Devon Bowen.
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

package org.easotope.client.rawdata.batchimport;

import java.util.ArrayList;

import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.rawdata.cache.sourcelist.sourcelist.SourceListItem;

public class ImportedFile implements Comparable<ImportedFile> {
	private String warning;
	private boolean assumedTimeZone;
	private String error;
	private boolean ignore;
	private long timestamp;
	private int acquisitionNumber;
	private String fileName = "";
	private String identifier1 = null;
	private String identifier2 = null;
	private int sampleId = DatabaseConstants.EMPTY_DB_ID;
	private int standardId = DatabaseConstants.EMPTY_DB_ID;
	private ArrayList<SourceListItem> sourceList = null;
	private int group;

	public String getWarning() {
		return warning;
	}

	public void setWarning(String warning) {
		this.warning = warning;
	}

	public boolean isAssumedTimeZone() {
		return assumedTimeZone;
	}

	public void setAssumedTimeZone(boolean assumedTimeZone) {
		this.assumedTimeZone = assumedTimeZone;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isIgnore() {
		return ignore;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getAcquisitionNumber() {
		return acquisitionNumber;
	}

	public void setAcquisitionNumber(int acquisitionNumber) {
		this.acquisitionNumber = acquisitionNumber;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getIdentifier() {
		if (identifier1 == null && identifier2 == null) {
			return "";
		}

		if (identifier1 != null && identifier2 != null) {
			return identifier1 + " | " + identifier2;
		}

		if (identifier1 != null) {
			return identifier1;
		} else {
			return identifier2;
		}
	}

	public String getIdentifier1() {
		return identifier1;
	}

	public void setIdentifier1(String identifier1) {
		this.identifier1 = identifier1;
	}

	public String getIdentifier2() {
		return identifier2;
	}

	public void setIdentifier2(String identifier2) {
		this.identifier2 = identifier2;
	}

	public int getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}

	public int getStandardId() {
		return standardId;
	}

	public void setStandardId(int standardId) {
		this.standardId = standardId;
	}

	public ArrayList<SourceListItem> getSourceList() {
		return sourceList;
	}

	public void setSourceList(ArrayList<SourceListItem> sourceList) {
		this.sourceList = sourceList;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	@Override
	public int compareTo(ImportedFile that) {
		return ((Long) this.timestamp).compareTo(that.timestamp);
	}
}
