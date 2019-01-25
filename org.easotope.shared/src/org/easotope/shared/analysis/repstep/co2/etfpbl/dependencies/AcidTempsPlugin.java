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

package org.easotope.shared.analysis.repstep.co2.etfpbl.dependencies;

import java.util.HashMap;

import org.easotope.framework.commands.Command;
import org.easotope.shared.admin.AcidTempParameter;
import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.StandardParameter;
import org.easotope.shared.admin.cache.sampletype.SampleTypeCache;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.AcidTempList;
import org.easotope.shared.admin.cache.sampletype.acidtemplist.AcidTempListItem;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.admin.tables.SampleType;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.analysis.repstep.co2.etfpbl.Calculator;
import org.easotope.shared.core.DoubleTools;
import org.easotope.shared.core.NumericValue;
import org.easotope.shared.core.cache.AbstractCache;

public class AcidTempsPlugin extends DependencyPlugin {
	private int[] standardIds = null;
	private int currentStandardId = 0;
	private Standard[] standards = null;
	private SampleType[] sampleTypes = null;
	private AcidTemp[] acidTemps = null;

	AcidTempsPlugin() {
		super("Acid Temperatures");
	}

	@Override
	public Object getObject() {
		return new Object[] { standards, acidTemps };
	}

	@Override
	public HashMap<String,String> getPrintableValues(DependencyManager dependencyManager) {
		HashMap<String,String> result = new HashMap<String,String>();

		for (int i=0; i<standards.length; i++) {
			Standard standard = standards[i];
			NumericValue numericValue = standard.getValues().get(StandardParameter.Δ47.ordinal());
			String value = null;

			if (numericValue != null) {
				value = String.valueOf(numericValue.getValue());
				value += " ";
				value += IsotopicScale.values()[numericValue.getDescription()];
			}

			result.put(standard.getName() + " Δ47", (value == null) ? "UNDEFINED" : value);

			AcidTemp acidTemp = acidTemps[i];
			numericValue = acidTemp == null ? null : acidTemp.getValues().get(AcidTempParameter.clumped.ordinal());
			value = null;

			if (numericValue != null) {
				value = String.valueOf(numericValue.getValue());
			}

			Calculator calculator = (Calculator) dependencyManager.getStepCalculator();
			result.put(standard.getName() + " " + calculator.getAcidTemperature() + "° clumped α", (value == null) ? "UNDEFINED" : value);
		}

		return result;
	}

	@Override
	public int requestObject(DependencyManager dependencyManager) {
		Calculator calculator = (Calculator) dependencyManager.getStepCalculator();
		standardIds = calculator.getStandardIds();
		standards = new Standard[standardIds == null ? 0 : standardIds.length];
		sampleTypes = new SampleType[standardIds == null ? 0 : standardIds.length];
		acidTemps = new AcidTemp[standardIds == null ? 0 : standardIds.length];

		if (standardIds == null || standardIds.length == 0) {
			return Command.UNDEFINED_ID;
		}

		return StandardCache.getInstance().standardGet(standardIds[currentStandardId], dependencyManager);
	}

	@Override
	public int receivedObject(DependencyManager dependencyManager, Object object) {
		if (object == null) {
			return Command.UNDEFINED_ID;
		}

		if (object instanceof Standard) {
			standards[currentStandardId] = (Standard) object;
			return SampleTypeCache.getInstance().sampleTypeGet(standards[currentStandardId].getSampleTypeId(), dependencyManager);
		}

		if (object instanceof SampleType) {
			sampleTypes[currentStandardId] = (SampleType) object;

			if (sampleTypes[currentStandardId].getHasAcidTemps()) {
				return SampleTypeCache.getInstance().acidTempListGet(standards[currentStandardId].getSampleTypeId(), dependencyManager);

			} else {
				currentStandardId++;

				if (currentStandardId < standardIds.length) {
					return StandardCache.getInstance().standardGet(standardIds[currentStandardId], dependencyManager);
				}
			}

			return Command.UNDEFINED_ID;
		}

		if (object instanceof AcidTempList) {
			AcidTempList acidTempList = (AcidTempList) object;
			Calculator calculator = (Calculator) dependencyManager.getStepCalculator();

			for (Integer acidTempId : acidTempList.keySet()) {
				AcidTempListItem acidTempListItem = acidTempList.get(acidTempId);

				if (DoubleTools.essentiallyEqual(acidTempListItem.getTemperature(), calculator.getAcidTemperature())) {
					return SampleTypeCache.getInstance().acidTempGet(acidTempId, dependencyManager);
				}
			}

			return Command.UNDEFINED_ID;
		}

		if (object instanceof AcidTemp) {
			acidTemps[currentStandardId] = (AcidTemp) object;

			currentStandardId++;

			if (currentStandardId < standardIds.length) {
				return StandardCache.getInstance().standardGet(standardIds[currentStandardId], dependencyManager);
			}
			
			return Command.UNDEFINED_ID;
		}

		return Command.UNDEFINED_ID;
	}

	@Override
	public boolean verifyCurrentObject(DependencyManager dependencyManager) {
		for (int i=0; i<standards.length; i++) {
			Standard standard = standards[i];

			if (standard == null) {
				return false;
			}

			if (standard.getId() != standardIds[i]) {
				return false;
			}

			if (	standard.getValues().get(StandardParameter.Δ47.ordinal()) == null) {
				return false;
			}

			SampleType sampleType = sampleTypes[i];

			if (sampleType == null) {
				return false;
			}

			AcidTemp acidTemp = acidTemps[i];

			if (sampleType.getHasAcidTemps()) {	
				if (acidTemp == null) {
					return false;
				}
	
				if (	acidTemp.getValues().get(AcidTempParameter.clumped.ordinal()) == null) {
					return false;
				}
			} else {
				if (acidTemp != null) {
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public AbstractCache[] getCachesToListenTo() {
		return new AbstractCache[] { StandardCache.getInstance(), SampleTypeCache.getInstance() };
	}

	@Override
	public boolean isNoLongerValid(DependencyManager dependencyManager, Object object) {
		if (object instanceof Standard) {
			Standard newStandard = (Standard) object;

			for (Standard standard : standards) {
				if (standard != null && standard.getId() == newStandard.getId()) {
					return true;
				}
			}
		}

		if (object instanceof SampleType) {
			SampleType newSampleType = (SampleType) object;

			for (SampleType sampleType : sampleTypes) {
				if (sampleType != null && sampleType.getId() == newSampleType.getId()) {
					return true;
				}
			}
		}
		
		if (object instanceof AcidTemp) {
			AcidTemp newAcidTemp = (AcidTemp) object;

			for (AcidTemp acidTemp : acidTemps) {
				if (acidTemp != null && acidTemp.getId() == newAcidTemp.getId()) {
					return true;
				}
			}
		}

		return false;
	}
}
