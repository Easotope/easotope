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

package org.easotope.shared.analysis.tables;

import java.util.HashMap;

import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

@SuppressWarnings("serial")
abstract public class StepParams extends TableObjectWithIntegerId {
	public static final String ANALYSIS_ID_FIELD_NAME = "ANALYSISID";
	public static final String POSITION_FIELD_NAME = "POSITION";
	public static final String PARAMETERS_FIELD_NAME = "PARAMETERS";

	@DatabaseField(columnName=ANALYSIS_ID_FIELD_NAME, index=true)
	public int analysisId;
	@DatabaseField(columnName=POSITION_FIELD_NAME)
	public int position;
	@DatabaseField(columnName=PARAMETERS_FIELD_NAME, dataType=DataType.SERIALIZABLE)
	public HashMap<String,Object> parameters;

	public StepParams() { }

	public StepParams(StepParams stepParameters) {
		super(stepParameters);

		analysisId = stepParameters.analysisId;
		position = stepParameters.position;
		parameters = new HashMap<String,Object>(stepParameters.parameters);
	}

	public int getAnalysisId() {
		return analysisId;
	}

	public void setAnalysisId(int analysisId) {
		this.analysisId = analysisId;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public HashMap<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(HashMap<String, Object> parameters) {
		this.parameters = parameters;
	}
}
