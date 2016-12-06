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

package org.easotope.shared.analysis.execute;

import java.util.ArrayList;
import java.util.List;

import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.StandardParameter;
import org.easotope.shared.analysis.repstep.RepOutputDescription;
import org.easotope.shared.analysis.step.OutputDescription;

public class ComparableStandardOutputs {
	private List<ComparableStandardOutput> comparableStandardOutputs = new ArrayList<ComparableStandardOutput>();

	public ComparableStandardOutputs(AnalysisCompiled analysisCompiled) {
		for (int i=0; i<analysisCompiled.step.length; i++) {
			for (OutputDescription outputDescription : analysisCompiled.stepController[i].getOutputDescription()) {
				RepOutputDescription repOutputDescription = (RepOutputDescription) outputDescription;

				String outputKey = outputDescription.getLabel();
				String outputValue = analysisCompiled.step[i].getOutputs().get(outputKey);

				if (analysisCompiled.generatedOutputColumns.contains(outputValue)) {
					if (repOutputDescription.getComparableStandardParameter() != null && repOutputDescription.getIsotopicScale() != null) {
						comparableStandardOutputs.add(new ComparableStandardOutput(outputValue, repOutputDescription.getComparableStandardParameter(), repOutputDescription.getIsotopicScale()));
					}
				}
			}
		}
	}
	
	public List<ComparableStandardOutput> getComparableStandardOutputs() {
		return comparableStandardOutputs;
	}

	public class ComparableStandardOutput {
		private String columnName;
		private StandardParameter comparableStandardParameter;
		private IsotopicScale isotopicScale;

		public ComparableStandardOutput(String columnName, StandardParameter comparableStandardParameter, IsotopicScale isotopicScale) {
			this.columnName = columnName;
			this.comparableStandardParameter = comparableStandardParameter;
			this.isotopicScale = isotopicScale;
		}

		public String getColumnName() {
			return columnName;
		}

		public StandardParameter getComparableStandardParameter() {
			return comparableStandardParameter;
		}

		public IsotopicScale getIsotopicScale() {
			return isotopicScale;
		}
	}
}
