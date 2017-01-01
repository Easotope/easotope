/*
 * Copyright © 2016-2017 by Devon Bowen.
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
import org.easotope.shared.rawdata.InputParameter;

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

			////////////////

			RepAnalysis repAnalysis = new RepAnalysis();
			repAnalysis.setName("CO₂ clumped");
			repAnalysis.setDescription("A standard CO₂ clumped isotope analysis.");
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
					"Enabled Acquisitions"
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
					"δ¹⁸O VPDB (Raw)",
					"δ¹⁸O VPDB (Raw) SD",
					"δ¹⁸O VPDB (Raw) SE",
					"δ¹⁸O VSMOW (Raw)",
					"δ¹⁸O VSMOW (Raw) SD",
					"δ¹⁸O VSMOW (Raw) SE"
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
					"δ46 WG (Raw)",
					"δ46 WG (Raw) SD",
					"δ46 WG (Raw) SE",
					"δ47 WG (Raw)",
					"δ47 WG (Raw) SD",
					"δ47 WG (Raw) SE",
					"Δ47 WG (Raw)",
					"Δ47 WG (Raw) SD",
					"Δ47 WG (Raw) SE",
					"δ48 WG (Raw)",
					"δ48 WG (Raw) SD",
					"δ48 WG (Raw) SE",
					"Δ48 WG (Raw)",
					"Δ48 WG (Raw) SD",
					"Δ48 WG (Raw) SE",
					"δ49 WG (Raw)",
					"δ49 WG (Raw) SD",
					"δ49 WG (Raw) SE",
					"Δ49 WG (Raw)",
					"Δ49 WG (Raw) SD",
					"Δ49 WG (Raw) SE",
					"49 Param",
					"49 Param SD",
					"49 Param SE"
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
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.nonlinearity.Controller");
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
					"Δ47 WG (HG)"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.etf.Controller");
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
					"ETF Slope",
					"ETF Intercept",
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
					"Clumped AFF",
					"Δ47 CDES (Final)"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.d48offset.Controller");
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
					"Δ48 WG (HG)",
					"Δ48 Offset"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			////////////////

			repAnalysis = new RepAnalysis();
			repAnalysis.setName("CO₂ clumped ICL PBL");
			repAnalysis.setDescription("A standard CO₂ clumped isotope analysis with ICL-style PBL.");
			repAnalysisDao.create(repAnalysis);
			int co2ClumpedIclPblRepAnalysisId = repAnalysis.getId();

			repStep = new RepStep();
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
					"Enabled Acquisitions"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.iclpbl.Controller");
			repStep.setPosition(1);
			repStep.setApplyToContext(true);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					InputParameter.Disabled.toString(),
					InputParameter.Off_Peak.toString(),
					InputParameter.V44_Ref.toString(),
					InputParameter.V44_Sample.toString(),
					InputParameter.V44_Background.toString(),
					InputParameter.V45_Ref.toString(),
					InputParameter.V45_Sample.toString(),
					InputParameter.V45_Background.toString(),
					InputParameter.V46_Ref.toString(),
					InputParameter.V46_Sample.toString(),
					InputParameter.V46_Background.toString(),
					InputParameter.V47_Ref.toString(),
					InputParameter.V47_Sample.toString(),
					InputParameter.V47_Background.toString(),
					InputParameter.V48_Ref.toString(),
					InputParameter.V48_Sample.toString(),
					InputParameter.V48_Background.toString(),
					InputParameter.V49_Ref.toString(),
					InputParameter.V49_Sample.toString(),
					InputParameter.V49_Background.toString()
			});
			setStepOutputs(repStep, new String[] {
					"V44_Ref (PBL)",
					"V44_Sample (PBL)",
					"V45_Ref (PBL)",
					"V45_Sample (PBL)",
					"V46_Ref (PBL)",
					"V46_Sample (PBL)",
					"V47_Ref (PBL)",
					"V47_Sample (PBL)",
					"V48_Ref (PBL)",
					"V48_Sample (PBL)",
					"V49_Ref (PBL)",
					"V49_Sample (PBL)"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.co2calc.Controller");
			repStep.setPosition(2);
			repStep.setApplyToContext(true);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					InputParameter.Disabled.toString(),
					InputParameter.Off_Peak.toString(),
					"V44_Ref (PBL)",
					"V44_Sample (PBL)",
					"V45_Ref (PBL)",
					"V45_Sample (PBL)",
					"V46_Ref (PBL)",
					"V46_Sample (PBL)"
			});
			setStepOutputs(repStep, new String[] {
					"δ¹³C VPDB (PBL)",
					"δ¹³C VPDB (PBL) SD",
					"δ¹³C VPDB (PBL) SE",
					"δ¹⁸O VPDB (PBL)",
					"δ¹⁸O VPDB (PBL) SD",
					"δ¹⁸O VPDB (PBL) SE",
					"δ¹⁸O VSMOW (PBL)",
					"δ¹⁸O VSMOW (PBL) SD",
					"δ¹⁸O VSMOW (PBL) SE"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.clumpcalc.Controller");
			repStep.setPosition(3);
			repStep.setApplyToContext(true);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					InputParameter.Disabled.toString(),
					InputParameter.Off_Peak.toString(),
					"V44_Ref (PBL)",
					"V44_Sample (PBL)",
					"V45_Ref (PBL)",
					"V45_Sample (PBL)",
					"V46_Ref (PBL)",
					"V46_Sample (PBL)",
					"V47_Ref (PBL)",
					"V47_Sample (PBL)",
					"V48_Ref (PBL)",
					"V48_Sample (PBL)",
					"V49_Ref (PBL)",
					"V49_Sample (PBL)"
			});
			setStepOutputs(repStep, new String[] {
					"δ45 WG (PBL)",
					"δ45 WG (PBL) SD",
					"δ45 WG (PBL) SE",
					"δ46 WG (PBL)",
					"δ46 WG (PBL) SD",
					"δ46 WG (PBL) SE",
					"δ47 WG (PBL)",
					"δ47 WG (PBL) SD",
					"δ47 WG (PBL) SE",
					"Δ47 WG (PBL)",
					"Δ47 WG (PBL) SD",
					"Δ47 WG (PBL) SE",
					"δ48 WG (PBL)",
					"δ48 WG (PBL) SD",
					"δ48 WG (PBL) SE",
					"Δ48 WG (PBL)",
					"Δ48 WG (PBL) SD",
					"Δ48 WG (PBL) SE",
					"δ49 WG (PBL)",
					"δ49 WG (PBL) SD",
					"δ49 WG (PBL) SE",
					"Δ49 WG (PBL)",
					"Δ49 WG (PBL) SD",
					"Δ49 WG (PBL) SE",
					"49 Param",
					"49 Param SD",
					"49 Param SE"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.cdrift.Controller");
			repStep.setPosition(4);
			repStep.setApplyToContext(false);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					"δ¹³C VPDB (PBL)"
			});
			setStepOutputs(repStep, new String[] {
					"δ¹³C VPDB (Final)"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.oacid.Controller");
			repStep.setPosition(5);
			repStep.setApplyToContext(true);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					"δ¹⁸O VPDB (PBL)"
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
			repStep.setPosition(6);
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
			repStep.setPosition(7);
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
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.etfpbl.Controller");
			repStep.setPosition(8);
			repStep.setApplyToContext(false);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					"Δ47 WG (PBL)",
					"δ47 WG (PBL)",
					"Δ47 WG (PBL)",
					"Acid Temp"
			});
			setStepOutputs(repStep, new String[] {
					"ETF Slope",
					"ETF Intercept",
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
					"Clumped AFF",
					"Δ47 CDES (Final)"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.d48offsetpbl.Controller");
			repStep.setPosition(10);
			repStep.setApplyToContext(false);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					"Δ48 WG (PBL)",
					"Δ48 WG (PBL)"
			});
			setStepOutputs(repStep, new String[] {
					"Δ48 Offset"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			////////////////

			repAnalysis = new RepAnalysis();
			repAnalysis.setName("CO₂ clumped ETH PBL");
			repAnalysis.setDescription("A standard CO₂ clumped isotope analysis with ETH-style PBL.");
			repAnalysisDao.create(repAnalysis);
			int co2ClumpedEthPblRepAnalysisId = repAnalysis.getId();

			repStep = new RepStep();
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
					"Enabled Acquisitions"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.ethpbl.Controller");
			repStep.setPosition(1);
			repStep.setApplyToContext(true);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					InputParameter.Disabled.toString(),
					InputParameter.Off_Peak.toString(),
					InputParameter.V44_Ref.toString(),
					InputParameter.V44_Sample.toString(),
					InputParameter.V44_Background.toString(),
					InputParameter.V44_Scan_X2Coeff.toString(),
					InputParameter.V44_Scan_Slope.toString(),
					InputParameter.V44_Scan_Intercept.toString(),
					InputParameter.V45_Ref.toString(),
					InputParameter.V45_Sample.toString(),
					InputParameter.V45_Background.toString(),
					InputParameter.V45_Scan_X2Coeff.toString(),
					InputParameter.V45_Scan_Slope.toString(),
					InputParameter.V45_Scan_Intercept.toString(),
					InputParameter.V46_Ref.toString(),
					InputParameter.V46_Sample.toString(),
					InputParameter.V46_Background.toString(),
					InputParameter.V46_Scan_X2Coeff.toString(),
					InputParameter.V46_Scan_Slope.toString(),
					InputParameter.V46_Scan_Intercept.toString(),
					InputParameter.V47_Ref.toString(),
					InputParameter.V47_Sample.toString(),
					InputParameter.V47_Background.toString(),
					InputParameter.V47_Scan_X2Coeff.toString(),
					InputParameter.V47_Scan_Slope.toString(),
					InputParameter.V47_Scan_Intercept.toString(),
					InputParameter.V48_Ref.toString(),
					InputParameter.V48_Sample.toString(),
					InputParameter.V48_Background.toString(),
					InputParameter.V48_Scan_X2Coeff.toString(),
					InputParameter.V48_Scan_Slope.toString(),
					InputParameter.V48_Scan_Intercept.toString(),
					InputParameter.V49_Ref.toString(),
					InputParameter.V49_Sample.toString(),
					InputParameter.V49_Background.toString(),
					InputParameter.V49_Scan_X2Coeff.toString(),
					InputParameter.V49_Scan_Slope.toString(),
					InputParameter.V49_Scan_Intercept.toString(),
			});
			setStepOutputs(repStep, new String[] {
					"V44 Ref (PBL)",
					"V44 Sample (PBL)",
					"V45 Ref (PBL)",
					"V45 Sample (PBL)",
					"V46 Ref (PBL)",
					"V46 Sample (PBL)",
					"V47 Ref (PBL)",
					"V47 Sample (PBL)",
					"V48 Ref (PBL)",
					"V48 Sample (PBL)",
					"V49 Ref (PBL)",
					"V49 Sample (PBL)"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.co2calc.Controller");
			repStep.setPosition(2);
			repStep.setApplyToContext(true);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					InputParameter.Disabled.toString(),
					InputParameter.Off_Peak.toString(),
					"V44 Ref (PBL)",
					"V44 Sample (PBL)",
					"V45 Ref (PBL)",
					"V45 Sample (PBL)",
					"V46 Ref (PBL)",
					"V46 Sample (PBL)"
			});
			setStepOutputs(repStep, new String[] {
					"δ¹³C VPDB (PBL)",
					"δ¹³C VPDB (PBL) SD",
					"δ¹³C VPDB (PBL) SE",
					"δ¹⁸O VPDB (PBL)",
					"δ¹⁸O VPDB (PBL) SD",
					"δ¹⁸O VPDB (PBL) SE",
					"δ¹⁸O VSMOW (PBL)",
					"δ¹⁸O VSMOW (PBL) SD",
					"δ¹⁸O VSMOW (PBL) SE"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.clumpcalc.Controller");
			repStep.setPosition(3);
			repStep.setApplyToContext(true);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					InputParameter.Disabled.toString(),
					InputParameter.Off_Peak.toString(),
					"V44 Ref (PBL)",
					"V44 Sample (PBL)",
					"V45 Ref (PBL)",
					"V45 Sample (PBL)",
					"V46 Ref (PBL)",
					"V46 Sample (PBL)",
					"V47 Ref (PBL)",
					"V47 Sample (PBL)",
					"V48 Ref (PBL)",
					"V48 Sample (PBL)",
					"V49 Ref (PBL)",
					"V49 Sample (PBL)"
			});
			setStepOutputs(repStep, new String[] {
					"δ45 WG (PBL)",
					"δ45 WG (PBL) SD",
					"δ45 WG (PBL) SE",
					"δ46 WG (PBL)",
					"δ46 WG (PBL) SD",
					"δ46 WG (PBL) SE",
					"δ47 WG (PBL)",
					"δ47 WG (PBL) SD",
					"δ47 WG (PBL) SE",
					"Δ47 WG (PBL)",
					"Δ47 WG (PBL) SD",
					"Δ47 WG (PBL) SE",
					"δ48 WG (PBL)",
					"δ48 WG (PBL) SD",
					"δ48 WG (PBL) SE",
					"Δ48 WG (PBL)",
					"Δ48 WG (PBL) SD",
					"Δ48 WG (PBL) SE",
					"δ49 WG (PBL)",
					"δ49 WG (PBL) SD",
					"δ49 WG (PBL) SE",
					"Δ49 WG (PBL)",
					"Δ49 WG (PBL) SD",
					"Δ49 WG (PBL) SE",
					"49 Param",
					"49 Param SD",
					"49 Param SE"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.cdrift.Controller");
			repStep.setPosition(4);
			repStep.setApplyToContext(false);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					"δ¹³C VPDB (PBL)"
			});
			setStepOutputs(repStep, new String[] {
					"δ¹³C VPDB (Final)"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.oacid.Controller");
			repStep.setPosition(5);
			repStep.setApplyToContext(true);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					"δ¹⁸O VPDB (PBL)"
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
			repStep.setPosition(6);
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
			repStep.setPosition(7);
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
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.etfpbl.Controller");
			repStep.setPosition(8);
			repStep.setApplyToContext(false);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					"Δ47 WG (PBL)",
					"δ47 WG (PBL)",
					"Δ47 WG (PBL)",
					"Acid Temp"
			});
			setStepOutputs(repStep, new String[] {
					"ETF Slope",
					"ETF Intercept",
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
					"Clumped AFF",
					"Δ47 CDES (Final)"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			repStep = new RepStep();
			repStep.setAnalysisId(repAnalysis.getId());
			repStep.setClazz("org.easotope.shared.analysis.repstep.co2.d48offsetpbl.Controller");
			repStep.setPosition(10);
			repStep.setApplyToContext(false);
			repStep.setApplyToResults(true);
			setStepInputs(repStep, new String[] {
					"Δ48 WG (PBL)",
					"Δ48 WG (PBL)"
			});
			setStepOutputs(repStep, new String[] {
					"Δ48 Offset"
			});
			setStepFormats(repStep);
			repStepDao.create(repStep);

			////////////////

			repAnalysis = new RepAnalysis();
			repAnalysis.setName("CO₂ bulk");
			repAnalysis.setDescription("A standard CO₂ bulk isotope analysis.");
			repAnalysisDao.create(repAnalysis);
			int co2BulkRepAnalysisId = repAnalysis.getId();

			repStep = new RepStep();
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
					"Enabled Acquisitions"
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
					"δ¹⁸O VPDB (Raw)",
					"δ¹⁸O VPDB (Raw) SD",
					"δ¹⁸O VPDB (Raw) SE",
					"δ¹⁸O VSMOW (Raw)",
					"δ¹⁸O VSMOW (Raw) SD",
					"δ¹⁸O VSMOW (Raw) SE"
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

			Dao<SamAnalysis,Integer> samAnalysisDao = DaoManager.createDao(connectionSource, SamAnalysis.class);
			TableUtils.createTable(connectionSource, SamAnalysis.class);

			Dao<SamStep,Integer> samStepDao = DaoManager.createDao(connectionSource, SamStep.class);
			TableUtils.createTable(connectionSource, SamStep.class);

			SamAnalysis samAnalysis = new SamAnalysis();
			samAnalysis.setName("CO₂ clumped");
			samAnalysis.setDescription("A standard CO₂ clumped isotope analysis.");
			samAnalysis.setRepAnalyses(new int[] { co2ClumpedRepAnalysisId, co2ClumpedIclPblRepAnalysisId, co2ClumpedEthPblRepAnalysisId });
			samAnalysisDao.create(samAnalysis);

			SamStep samStep = new SamStep();
			samStep.setAnalysisId(samAnalysis.getId());
			samStep.setClazz("org.easotope.shared.analysis.samstep.generic.sample.Controller");
			samStep.setPosition(0);
			setStepInputs(samStep, new String[] {

			});
			setStepOutputs(samStep, new String[] {

			});
			setStepFormats(samStep);
			samStepDao.create(samStep);

			samStep = new SamStep();
			samStep.setAnalysisId(samAnalysis.getId());
			samStep.setClazz("org.easotope.shared.analysis.samstep.co2.co2average.Controller");
			samStep.setPosition(1);
			setStepInputs(samStep, new String[] {
					"δ¹³C VPDB (Final)",
					"δ¹⁸O VPDB (Final)",
					"δ¹⁸O VSMOW (Final)"
			});
			setStepOutputs(samStep, new String[] {
					"δ¹³C VPDB (Final)",
					"δ¹³C VPDB (Final) SD",
					"δ¹³C VPDB (Final) SE",
					"δ¹⁸O VPDB (Final)",
					"δ¹⁸O VPDB (Final) SD",
					"δ¹⁸O VPDB (Final) SE",
					"δ¹⁸O VSMOW (Final)",
					"δ¹⁸O VSMOW (Final) SD",
					"δ¹⁸O VSMOW (Final) SE"
			});
			setStepFormats(samStep);
			samStepDao.create(samStep);

			samStep = new SamStep();
			samStep.setAnalysisId(samAnalysis.getId());
			samStep.setClazz("org.easotope.shared.analysis.samstep.co2.clumpaverage.Controller");
			samStep.setPosition(2);
			setStepInputs(samStep, new String[] {
					"Δ47 CDES (Final)"
			});
			setStepOutputs(samStep, new String[] {
					"Δ47 CDES (Final)",
					"Δ47 CDES (Final) SD",
					"Δ47 CDES (Final) SE",
			});
			setStepFormats(samStep);
			samStepDao.create(samStep);

			samStep = new SamStep();
			samStep.setAnalysisId(samAnalysis.getId());
			samStep.setClazz("org.easotope.shared.analysis.samstep.co2.clumptemp.Controller");
			samStep.setPosition(3);
			setStepInputs(samStep, new String[] {
					"Acid Temp",
					"Clumped AFF",
					"Δ47 CDES (Final)"
			});
			setStepOutputs(samStep, new String[] {
					"Δ47 Dennis",
					"Δ47 Dennis SD",
					"Δ47 Dennis SE",
					"Dennis-SE ˚C",
					"Dennis ˚C",
					"Dennis+SE ˚C",
					"Δ47 Gosh",
					"Δ47 Gosh SD",
					"Δ47 Gosh SE",
					"Gosh-SE ˚C",
					"Gosh ˚C",
					"Gosh+SE ˚C",
					"Δ47 Henkes",
					"Δ47 Henkes SD",
					"Δ47 Henkes SE",
					"Henkes-SE ˚C",
					"Henkes ˚C",
					"Henkes+SE ˚C",
					"Δ47 Kluge",
					"Δ47 Kluge SD",
					"Δ47 Kluge SE",
					"Kluge-SE ˚C",
					"Kluge ˚C",
					"Kluge+SE ˚C",
					"Δ47 Passey",
					"Δ47 Passey SD",
					"Δ47 Passey SE",
					"Passey-SE ˚C",
					"Passey ˚C",
					"Passey+SE ˚C",
					"Δ47 Zaruur",
					"Δ47 Zaruur SD",
					"Δ47 Zaruur SE",
					"Zaruur-SE ˚C",
					"Zaruur ˚C",
					"Zaruur+SE ˚C"
			});
			setStepFormats(samStep);
			samStepDao.create(samStep);

			samAnalysis = new SamAnalysis();
			samAnalysis.setName("CO₂ bulk");
			samAnalysis.setDescription("A standard CO₂ bulk isotope analysis.");
			samAnalysis.setRepAnalyses(new int[] { co2ClumpedRepAnalysisId, co2ClumpedIclPblRepAnalysisId, co2ClumpedEthPblRepAnalysisId, co2BulkRepAnalysisId });
			samAnalysisDao.create(samAnalysis);

			samStep = new SamStep();
			samStep.setAnalysisId(samAnalysis.getId());
			samStep.setClazz("org.easotope.shared.analysis.samstep.generic.sample.Controller");
			samStep.setPosition(0);
			setStepInputs(samStep, new String[] {

			});
			setStepOutputs(samStep, new String[] {

			});
			setStepFormats(samStep);
			samStepDao.create(samStep);

			samStep = new SamStep();
			samStep.setAnalysisId(samAnalysis.getId());
			samStep.setClazz("org.easotope.shared.analysis.samstep.co2.co2average.Controller");
			samStep.setPosition(1);
			setStepInputs(samStep, new String[] {
					"δ¹³C VPDB (Final)",
					"δ¹⁸O VPDB (Final)",
					"δ¹⁸O VSMOW (Final)"
			});
			setStepOutputs(samStep, new String[] {
					"δ¹³C VPDB (Final)",
					"δ¹³C VPDB (Final) SD",
					"δ¹³C VPDB (Final) SE",
					"δ¹⁸O VPDB (Final)",
					"δ¹⁸O VPDB (Final) SD",
					"δ¹⁸O VPDB (Final) SE",
					"δ¹⁸O VSMOW (Final)",
					"δ¹⁸O VSMOW (Final) SD",
					"δ¹⁸O VSMOW (Final) SE"
			});
			setStepFormats(samStep);
			samStepDao.create(samStep);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, AnalysesCreator.class, "Error during creation of rep and sam analyses.", e);
			return false;
		}

		return true;
	}

	private static void setStepInputs(Step step, String[] strings) {
		HashMap<String,String> inputs = new HashMap<String,String>();
		StepController controller = (StepController) Reflection.createObject(step.getClazz(), (Object[]) null);
		InputDescription[] inputDescriptions = controller.getInputDescription();

		for (int i=0; i<inputDescriptions.length; i++) {
			inputs.put(inputDescriptions[i].getLabel(), strings[i]);
		}

		step.setInputs(inputs);
	}

	private static void setStepOutputs(Step step, String[] strings) {
		HashMap<String,String> outputs = new HashMap<String,String>();
		StepController controller = (StepController) Reflection.createObject(step.getClazz(), (Object[]) null);
		OutputDescription[] outputDescriptions = controller.getOutputDescription();

		for (int i=0; i<outputDescriptions.length; i++) {
			outputs.put(outputDescriptions[i].getLabel(), strings[i]);
		}

		step.setOutputs(outputs);
	}

	private static void setStepFormats(Step step) {
		StepController controller = (StepController) Reflection.createObject(step.getClazz(), (Object[]) null);
		OutputDescription[] outputDescriptions = controller.getOutputDescription();
		HashMap<String,String> formats = new HashMap<String,String>();

		for (int i=0; i<outputDescriptions.length; i++) {
			formats.put(outputDescriptions[i].getLabel(), outputDescriptions[i].getFormat());
		}

		step.setFormats(formats);
	}
}
