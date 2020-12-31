/*
 * Copyright © 2016-2020 by Devon Bowen.
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

package org.easotope.shared.plugin.analysis.initializedhandler;

import java.util.HashMap;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.Reflection;
import org.easotope.shared.analysis.step.InputDescription;
import org.easotope.shared.analysis.step.OutputDescription;
import org.easotope.shared.analysis.step.StepController;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.analysis.tables.SamAnalysis;
import org.easotope.shared.analysis.tables.SamStep;
import org.easotope.shared.analysis.tables.Step;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class AnalysesCreator {
	public static boolean create(ConnectionSource connectionSource) {
		try {
			TableUtils.dropTable(connectionSource, RepAnalysis.class, true);
			TableUtils.dropTable(connectionSource, RepStep.class, true);
			TableUtils.dropTable(connectionSource, SamAnalysis.class, true);
			TableUtils.dropTable(connectionSource, SamStep.class, true);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, AnalysesCreator.class, "Error while clearing analysis tables.", e);
			return false;
		}

		try {
			TableUtils.createTable(connectionSource, RepAnalysis.class);
			Dao<RepAnalysis,Integer> repAnalysisDao = DaoManager.createDao(connectionSource, RepAnalysis.class);

			TableUtils.createTable(connectionSource, RepStep.class);
			Dao<RepStep,Integer> repStepDao = DaoManager.createDao(connectionSource, RepStep.class);

			// THIS ORDER SHOULD NOT BE CHANGED - NEW ANALYSES SHOULD BE ADDED TO THE END
			int co2ClumpedRepAnalysisId = RepAnalysisCo2Clumped.create(repAnalysisDao, repStepDao);
			int co2ClumpedIclPblRepAnalysisId = RepAnalysisCo2ClumpedIclPbl.create(repAnalysisDao, repStepDao);
			int co2ClumpedEthPblRepAnalysisId = RepAnalysisCo2ClumpedEthPbl.create(repAnalysisDao, repStepDao);
			int co2BulkRepAnalysisId = RepAnalysisCo2Bulk.create(repAnalysisDao, repStepDao);
			int co2ClumpedD48RepAnalysisId = RepAnalysisCo2ClumpedD48.create(repAnalysisDao, repStepDao);
			int co2ClumpedIclPblD48RepAnalysisId = RepAnalysisCo2ClumpedIclPblD48.create(repAnalysisDao, repStepDao);
			int co2ClumpedEthPblD48RepAnalysisId = RepAnalysisCo2ClumpedEthPblD48.create(repAnalysisDao, repStepDao);

			Dao<SamAnalysis,Integer> samAnalysisDao = DaoManager.createDao(connectionSource, SamAnalysis.class);
			TableUtils.createTable(connectionSource, SamAnalysis.class);

			Dao<SamStep,Integer> samStepDao = DaoManager.createDao(connectionSource, SamStep.class);
			TableUtils.createTable(connectionSource, SamStep.class);

			// THIS ORDER SHOULD NOT BE CHANGED - NEW ANALYSES SHOULD BE ADDED TO THE END
			SamAnalysisCo2Clumped.create(samAnalysisDao, samStepDao, new int[] { co2ClumpedRepAnalysisId, co2ClumpedIclPblRepAnalysisId, co2ClumpedEthPblRepAnalysisId, co2ClumpedD48RepAnalysisId, co2ClumpedIclPblD48RepAnalysisId, co2ClumpedEthPblD48RepAnalysisId });
			SamAnalysisCo2Bulk.create(samAnalysisDao, samStepDao, new int[] { co2ClumpedRepAnalysisId, co2ClumpedIclPblRepAnalysisId, co2ClumpedEthPblRepAnalysisId, co2BulkRepAnalysisId, co2ClumpedD48RepAnalysisId, co2ClumpedIclPblD48RepAnalysisId, co2ClumpedEthPblD48RepAnalysisId });

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, AnalysesCreator.class, "Error during creation of rep and sam analyses.", e);
			return false;
		}

		return true;
	}

	protected static void setStepInputs(Step step, String[] strings) {
		HashMap<String,String> inputs = new HashMap<String,String>();
		StepController controller = (StepController) Reflection.createObject(step.getClazz(), (Object[]) null);
		InputDescription[] inputDescriptions = controller.getInputDescription();

		for (int i=0; i<inputDescriptions.length; i++) {
			inputs.put(inputDescriptions[i].getLabel(), strings[i]);
		}

		step.setInputs(inputs);
	}

	protected static void setStepOutputs(Step step, String[] strings) {
		HashMap<String,String> outputs = new HashMap<String,String>();
		StepController controller = (StepController) Reflection.createObject(step.getClazz(), (Object[]) null);
		OutputDescription[] outputDescriptions = controller.getOutputDescription();

		for (int i=0; i<outputDescriptions.length; i++) {
			outputs.put(outputDescriptions[i].getLabel(), strings[i]);
		}

		step.setOutputs(outputs);
	}

	protected static void setStepFormats(Step step) {
		StepController controller = (StepController) Reflection.createObject(step.getClazz(), (Object[]) null);
		OutputDescription[] outputDescriptions = controller.getOutputDescription();
		HashMap<String,String> formats = new HashMap<String,String>();

		for (int i=0; i<outputDescriptions.length; i++) {
			formats.put(outputDescriptions[i].getLabel(), outputDescriptions[i].getFormat());
		}

		step.setFormats(formats);
	}
}
