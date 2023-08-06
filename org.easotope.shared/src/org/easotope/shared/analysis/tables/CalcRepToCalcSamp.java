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

package org.easotope.shared.analysis.tables;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName=CalcRepToCalcSamp.TABLE_NAME)
public class CalcRepToCalcSamp extends TableObjectWithIntegerId {
	private static final long serialVersionUID = 1L;

	public static final String TABLE_NAME = "CALCREPTOCALCSAMP_V0";
	public static final String CALC_REPLICATE_ID_FIELD_NAME = "CALCREPLICATEID";
	public static final String CALC_SAMPLE_ID_FIELD_NAME = "CALCSAMPLEID";

	@DatabaseField(columnName=CALC_REPLICATE_ID_FIELD_NAME, index=true)
	private int calcReplicateId;
	@DatabaseField(columnName=CALC_SAMPLE_ID_FIELD_NAME, index=true)
	private int calcSampleId;

	public CalcRepToCalcSamp() { }

	public CalcRepToCalcSamp(CalcRepToCalcSamp calcRepToCalcSamp) {
		super(calcRepToCalcSamp);

		this.calcReplicateId = calcRepToCalcSamp.calcReplicateId;
		this.calcSampleId = calcRepToCalcSamp.calcSampleId;
	}

	public int getCalcReplicateId() {
		return calcReplicateId;
	}

	public void setCalcReplicateId(int calcReplicateId) {
		this.calcReplicateId = calcReplicateId;
	}

	public int getCalcSampleId() {
		return calcSampleId;
	}

	public void setCalcSampleId(int calcSampleId) {
		this.calcSampleId = calcSampleId;
	}
}
