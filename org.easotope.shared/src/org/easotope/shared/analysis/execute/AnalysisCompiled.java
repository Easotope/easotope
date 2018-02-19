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

package org.easotope.shared.analysis.execute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.easotope.framework.core.util.Reflection;
import org.easotope.shared.Messages;
import org.easotope.shared.analysis.step.InputDescription;
import org.easotope.shared.analysis.step.OutputDescription;
import org.easotope.shared.analysis.step.StepController;
import org.easotope.shared.analysis.tables.Analysis;
import org.easotope.shared.analysis.tables.Step;

public class AnalysisCompiled {
	private Analysis analysis;
	protected Step[] step;
	protected StepController[] stepController;
	private ArrayList<String> requiredInputColumns;
	protected ArrayList<String> generatedOutputColumns;
	private HashMap<String,String> outputColumnToFormat;
	private ArrayList<String> errors;

	public AnalysisCompiled(Analysis analysis, List<? extends Step> stepList) {
		this.analysis = analysis;

		step = stepList.toArray(new Step[stepList.size()]);
		Arrays.sort(step, new StepComparator());

		stepController = new StepController[step.length];
		for (int i=0; i<step.length; i++) {
			String className = step[i].getClazz();

			try {
				stepController[i] = (StepController) Reflection.createObject(className);
			} catch (Exception e) {
				e.printStackTrace(System.err);
				addError(Messages.dataAnalysisCompiled_couldntCreateStepController + className);
			}
		}

		requiredInputColumns = new ArrayList<String>();
		generatedOutputColumns = new ArrayList<String>();
		outputColumnToFormat = new HashMap<String,String>();
		HashSet<String> scratchPadHasColumn = new HashSet<String>();

		for (int i=0; i<step.length; i++) {
			for (InputDescription inputDescription : stepController[i].getInputDescription()) {
				if (!inputDescription.isRequired()) {
					continue;
				}

				String requiredInputKey = inputDescription.getLabel();
				String requiredInputValue = step[i].getInputs().get(requiredInputKey);
				
				if (!scratchPadHasColumn.contains(requiredInputValue) && !requiredInputColumns.contains(requiredInputValue)) {
					requiredInputColumns.add(requiredInputValue);
				}
			}

			for (OutputDescription outputDescription : stepController[i].getOutputDescription()) {
				String outputKey = outputDescription.getLabel();
				String outputValue = step[i].getOutputs().get(outputKey);

				if (!generatedOutputColumns.contains(outputValue)) {
					generatedOutputColumns.add(outputValue);
					HashMap<String,String> formats = step[i].getFormats();

					if (formats != null) {
						String formatForColumn = formats.get(outputKey);
						
						if (formatForColumn != null) {
							outputColumnToFormat.put(outputValue, formatForColumn);
						}
					}
				}

				scratchPadHasColumn.add(outputValue);
			}
		}
	}

    public Analysis getAnalysis() {
		return analysis;
	}

	public Step[] getSteps() {
		return step;
	}

	public StepController[] getStepControllers() {
		return stepController;
	}

	public ArrayList<String> getRequiredInputColumns() {
		return requiredInputColumns;
	}

	public ArrayList<String> getGeneratedOutputColumns() {
		return generatedOutputColumns;
	}

	public HashMap<String,String> getOutputColumnToFormat() {
		return outputColumnToFormat;
	}

	protected void addError(String message) {
		if (errors == null) {
			errors = new ArrayList<String>();
		}

		errors.add(message);
	}

	public ArrayList<String> getErrors() {
		return errors;
	}

	class StepComparator implements Comparator<Step> {
		@Override
		public int compare(Step arg0, Step arg1) {
			return ((Integer) arg0.getPosition()).compareTo(arg1.getPosition());
		}
    }
}
