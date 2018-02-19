/*
 * Copyright © 2016-2018 by Devon Bowen.
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
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.ReplicatePad;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=Options.TABLE_NAME)
public class Options extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "OPTIONS_V0";
	public static final String OVERVIEW_RES_FIELD_NAME = "OVERVIEWRES";

	@DatabaseField(columnName=OVERVIEW_RES_FIELD_NAME, canBeNull=false)
	public OverviewResolution overviewRes;

	public Options() { }

	public Options(Options options) {
		super(options);
		this.overviewRes = options.overviewRes;
	}

	public OverviewResolution getOverviewResolution() {
		return overviewRes;
	}

	public void setOverviewResolution(OverviewResolution overviewRes) {
		this.overviewRes = overviewRes;
	}

	public enum OverviewResolution {
		REPLICATE("Replicate", ReplicatePad.class),
		ACQUISITION("Acquisition", AcquisitionPad.class),
		CYCLE("Cycle", CyclePad.class);

		private String name;
		private Class<?> padClazz;

		OverviewResolution(String name, Class<?> clazz) {
			this.name = name;
			this.padClazz = clazz;
		}

		public String getName() {
			return name;
		}

		public Class<?> getPadClass() {
			return padClazz;
		}
	}
}
