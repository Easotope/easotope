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

package org.easotope.shared.plugin.analysis.initializedhandler;

import java.sql.SQLException;

import org.easotope.shared.analysis.tables.SamAnalysis;
import org.easotope.shared.analysis.tables.SamStep;

import com.j256.ormlite.dao.Dao;

public class SamAnalysisCo2Bulk extends AnalysesCreator {
	public static void create(Dao<SamAnalysis, Integer> samAnalysisDao, Dao<SamStep, Integer> samStepDao, int[] repAnalyse) throws SQLException {
		SamAnalysis samAnalysis = new SamAnalysis();
		samAnalysis.setName("CO₂ bulk");
		samAnalysis.setDescription("A standard CO₂ bulk isotope analysis.");
		samAnalysis.setRepAnalyses(repAnalyse);
		samAnalysisDao.create(samAnalysis);

		SamStep samStep = new SamStep();
		samStep.setAnalysisId(samAnalysis.getId());
		samStep.setClazz(org.easotope.shared.analysis.samstep.generic.sample.Controller.class.getName());
		samStep.setPosition(0);
		setStepInputs(samStep, new String[] {

		});
		setStepOutputs(samStep, new String[] {

		});
		setStepFormats(samStep);
		samStepDao.create(samStep);

		samStep = new SamStep();
		samStep.setAnalysisId(samAnalysis.getId());
		samStep.setClazz(org.easotope.shared.analysis.samstep.co2.co2average.Controller.class.getName());
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
				"δ¹³C VPDB (Final) CI",
				"δ¹⁸O VPDB (Final)",
				"δ¹⁸O VPDB (Final) SD",
				"δ¹⁸O VPDB (Final) SE",
				"δ¹⁸O VPDB (Final) CI",
				"δ¹⁸O VSMOW (Final)",
				"δ¹⁸O VSMOW (Final) SD",
				"δ¹⁸O VSMOW (Final) SE",
				"δ¹⁸O VSMOW (Final) CI"
		});
		setStepFormats(samStep);
		samStepDao.create(samStep);
	}
}
