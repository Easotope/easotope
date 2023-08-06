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

package org.easotope.framework.dbcore.tables;

import org.easotope.framework.core.logging.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=Options.TABLE_NAME)
public class Options extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "OPTIONS_V0";
	public static final String OVERVIEW_RES_FIELD_NAME = "OVERVIEWRES";
	public static final String OVERVIEW_INCLUDE_STDS_FIELD_NAME = "INCLUDESTDS";
	public static final String OVERVIEW_CONFIDENCE_LEVEL_FIELD_NAME = "CONFLEVEL";

	@DatabaseField(columnName=OVERVIEW_RES_FIELD_NAME, canBeNull=false)
	public OverviewResolution overviewRes;
	@DatabaseField(columnName=OVERVIEW_INCLUDE_STDS_FIELD_NAME, canBeNull=false)
	public boolean includeStds;
	@DatabaseField(columnName=OVERVIEW_CONFIDENCE_LEVEL_FIELD_NAME, canBeNull=false)
	public double confidenceLevel;

	public Options() { }

	public Options(Options options) {
		super(options);
		this.overviewRes = options.overviewRes;
		this.includeStds = options.includeStds;
		this.confidenceLevel = options.confidenceLevel;
	}

	public OverviewResolution getOverviewResolution() {
		return overviewRes;
	}

	public void setOverviewResolution(OverviewResolution overviewRes) {
		this.overviewRes = overviewRes;
	}
	
	public boolean isIncludeStds() {
		return includeStds;
	}

	public void setIncludeStds(boolean includeStds) {
		this.includeStds = includeStds;
	}

	public double getConfidenceLevel() {
		return confidenceLevel;
	}

	public void setConfidenceLevel(double confidenceLevel) {
		this.confidenceLevel = confidenceLevel;
	}

	public enum OverviewResolution {
		REPLICATE("Replicate", "org.easotope.shared.core.scratchpad.ReplicatePad"),
		ACQUISITION("Acquisition", "org.easotope.shared.core.scratchpad.AcquisitionPad"),
		CYCLE("Cycle", "org.easotope.shared.core.scratchpad.CyclePad");

		private String name;
		private Class<?> clazz;

		OverviewResolution(String name, String className) {
			this.name = name;

			try {
				this.clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				Log.getInstance().log(Log.Level.TERMINAL, "could not load class " + className);
			}
		}

		public String getName() {
			return name;
		}

		public Class<?> getPadClass() {
			return clazz;
		}
	}
}
