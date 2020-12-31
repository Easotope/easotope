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

public class RepAnalysisCo2ClumpedD48 extends AnalysesCreator {
	public static int create(Dao<RepAnalysis,Integer> repAnalysisDao, Dao<RepStep,Integer> repStepDao) throws SQLException {
		RepAnalysis repAnalysis = new RepAnalysis();
		repAnalysis.setName("CO₂ clumpΔ48");
		repAnalysis.setDescription("A standard CO₂ clumped isotope analysis with Δ48 calculations.");
		repAnalysisDao.create(repAnalysis);
		int co2ClumpedRepAnalysisId = repAnalysis.getId();

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
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.clumpcalc.Controller");
		repStep.setPosition(2);
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
				InputParameter.V46_Sample.toString(),
				InputParameter.V47_Ref.toString(),
				InputParameter.V47_Sample.toString(),
				InputParameter.V48_Ref.toString(),
				InputParameter.V48_Sample.toString(),
				InputParameter.V49_Ref.toString(),
				InputParameter.V49_Sample.toString()
		});
		setStepOutputs(repStep, new String[] {
				"δ45 WG (Raw)",
				"δ45 WG (Raw) SD",
				"δ45 WG (Raw) SE",
				"δ45 WG (Raw) CI",
				"δ46 WG (Raw)",
				"δ46 WG (Raw) SD",
				"δ46 WG (Raw) SE",
				"δ46 WG (Raw) CI",
				"δ47 WG (Raw)",
				"δ47 WG (Raw) SD",
				"δ47 WG (Raw) SE",
				"δ47 WG (Raw) CI",
				"Δ47 WG (Raw)",
				"Δ47 WG (Raw) SD",
				"Δ47 WG (Raw) SE",
				"Δ47 WG (Raw) CI",
				"δ48 WG (Raw)",
				"δ48 WG (Raw) SD",
				"δ48 WG (Raw) SE",
				"δ48 WG (Raw) CI",
				"Δ48 WG (Raw)",
				"Δ48 WG (Raw) SD",
				"Δ48 WG (Raw) SE",
				"Δ48 WG (Raw) CI",
				"δ49 WG (Raw)",
				"δ49 WG (Raw) SD",
				"δ49 WG (Raw) SE",
				"δ49 WG (Raw) CI",
				"Δ49 WG (Raw)",
				"Δ49 WG (Raw) SD",
				"Δ49 WG (Raw) SE",
				"Δ49 WG (Raw) CI",
				"49 Param",
				"49 Param SD",
				"49 Param SE",
				"49 Param CI"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.cdrift.Controller");
		repStep.setPosition(3);
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
		repStep.setPosition(4);
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
		repStep.setPosition(5);
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
		repStep.setPosition(6);
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

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.d47nonlinearity.Controller");
		repStep.setPosition(7);
		repStep.setApplyToContext(false);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"δ47 WG (Raw)",
				"Δ47 WG (Raw)",
				"δ47 WG (Raw)",
				"Δ47 WG (Raw)",
		});
		setStepOutputs(repStep, new String[] {
				"Δ47 Nonlinearity Slope",
				"Δ47 Nonlinearity Intercepts",
				"Δ47 WG (HG)"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.d47etf.Controller");
		repStep.setPosition(8);
		repStep.setApplyToContext(false);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"Δ47 WG (HG)",
				"δ47 WG (Raw)",
				"Δ47 WG (Raw)",
				"Δ47 WG (HG)",
				"Acid Temp"
		});
		setStepOutputs(repStep, new String[] {
				"Δ47 ETF Slope",
				"Δ47 ETF Intercept",
				"Δ47 CDES (ETF)"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.d47acid.Controller");
		repStep.setPosition(9);
		repStep.setApplyToContext(false);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"Δ47 CDES (ETF)"
		});
		setStepOutputs(repStep, new String[] {
				"Δ47 AFF",
				"Δ47 CDES (Final)"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.d48nonlinearity.Controller");
		repStep.setPosition(10);
		repStep.setApplyToContext(false);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"δ48 WG (Raw)",
				"Δ48 WG (Raw)",
				"δ48 WG (Raw)",
				"Δ48 WG (Raw)",
		});
		setStepOutputs(repStep, new String[] {
				"Δ48 Nonlinearity Slope",
				"Δ48 Nonlinearity Intercepts",
				"Δ48 WG (HG)"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.d48etf.Controller");
		repStep.setPosition(11);
		repStep.setApplyToContext(false);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"Δ48 WG (HG)",
				"δ48 WG (Raw)",
				"Δ48 WG (Raw)",
				"Δ48 WG (HG)",
				"Acid Temp"
		});
		setStepOutputs(repStep, new String[] {
				"Δ48 ETF Slope",
				"Δ48 ETF Intercept",
				"Δ48 CDES (ETF)"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.d48acid.Controller");
		repStep.setPosition(12);
		repStep.setApplyToContext(false);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"Δ48 CDES (ETF)"
		});
		setStepOutputs(repStep, new String[] {
				"Δ48 AFF",
				"Δ48 CDES (Final)"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);

		repStep = new RepStep();
		repStep.setAnalysisId(repAnalysis.getId());
		repStep.setClazz("org.easotope.shared.analysis.repstep.co2.d48offset.Controller");
		repStep.setPosition(13);
		repStep.setApplyToContext(false);
		repStep.setApplyToResults(true);
		setStepInputs(repStep, new String[] {
				"δ48 WG (Raw)",
				"Δ48 WG (Raw)",
				"δ48 WG (Raw)",
				"Δ48 WG (Raw)",
		});
		setStepOutputs(repStep, new String[] {
				"Δ48 WG (Off)",
				"Δ48 Offset"
		});
		setStepFormats(repStep);
		repStepDao.create(repStep);
		
		return co2ClumpedRepAnalysisId;
	}
}
