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

import java.sql.SQLException;

import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.rawdata.InputParameter;

import com.j256.ormlite.dao.Dao;

public class RepAnalysisCo2Bulk extends AnalysesCreator {
	public static int create(Dao<RepAnalysis, Integer> repAnalysisDao, Dao<RepStep, Integer> repStepDao) throws SQLException {
		RepAnalysis repAnalysis = new RepAnalysis();
		repAnalysis.setName("CO₂ bulk");
		repAnalysis.setDescription("A standard CO₂ bulk isotope analysis.");
		repAnalysisDao.create(repAnalysis);
		int co2BulkRepAnalysisId = repAnalysis.getId();

		RepStep repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.generic.replicate.Controller");
		repStep.setPosition(0);
		repStep.setApplyToContext(true);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {

		});
		setStepOutputs(repStep, new String[] {
				"Mass Spec",
				"Easotope Name",
				"Sample Type",
				"Corr Interval",
				"Acid Temp",
				"Acquisitions",
				"Enabled Acquisitions",
				"First MZ44 Ref Gas",
				"First MZ44 Sample"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.co2calc.Controller");
		repStep.setPosition(1);
		repStep.setApplyToContext(true);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				InputParameter.Disabled.toString(),
				InputParameter.Off_Peak.toString(),
				InputParameter.V44_Ref.toString(),
				InputParameter.V44_Sample.toString(),
				InputParameter.V45_Ref.toString(),
				InputParameter.V45_Sample.toString(),
				InputParameter.V46_Ref.toString(),
				InputParameter.V46_Sample.toString()
		});
		setStepOutputs(repStep, new String[] {
				"δ¹³C VPDB (Raw)",
				"δ¹³C VPDB (Raw) SD",
				"δ¹³C VPDB (Raw) SE",
				"δ¹³C VPDB (Raw) CI",
				"δ¹⁸O VPDB (Raw)",
				"δ¹⁸O VPDB (Raw) SD",
				"δ¹⁸O VPDB (Raw) SE",
				"δ¹⁸O VPDB (Raw) CI",
				"δ¹⁸O VSMOW (Raw)",
				"δ¹⁸O VSMOW (Raw) SD",
				"δ¹⁸O VSMOW (Raw) SE",
				"δ¹⁸O VSMOW (Raw) CI"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.cdrift.Controller");
		repStep.setPosition(2);
		repStep.setApplyToContext(false);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"δ¹³C VPDB (Raw)"
		});
		setStepOutputs(repStep, new String[] {
				"δ¹³C VPDB (Final)"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.oacid.Controller");
		repStep.setPosition(3);
		repStep.setApplyToContext(true);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"δ¹⁸O VPDB (Raw)"
		});
		setStepOutputs(repStep, new String[] {
				"δ¹⁸O AFF",
				"δ¹⁸O VPDB (Acid)"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.odrift.Controller");
		repStep.setPosition(4);
		repStep.setApplyToContext(false);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"δ¹⁸O VPDB (Acid)"
		});
		setStepOutputs(repStep, new String[] {
				"δ¹⁸O VPDB (Final)"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.opdbtosmow.Controller");
		repStep.setPosition(5);
		repStep.setApplyToContext(false);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"δ¹⁸O VPDB (Final)"
		});
		setStepOutputs(repStep, new String[] {
				"δ¹⁸O VSMOW (Final)"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);
		
		return co2BulkRepAnalysisId;
	}
}
