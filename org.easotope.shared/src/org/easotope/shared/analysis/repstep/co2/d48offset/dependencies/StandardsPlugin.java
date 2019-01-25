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

package org.easotope.shared.analysis.repstep.co2.d48offset.dependencies;

import java.util.HashMap;

import org.easotope.framework.commands.Command;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.analysis.repstep.co2.d48offset.Calculator;
import org.easotope.shared.core.cache.AbstractCache;

public class StandardsPlugin extends DependencyPlugin {
	private int[] standardIds = null;
	private int currentStandardId = 0;
	private Standard[] standards = null;

	StandardsPlugin() {
		super("Standards");
	}

	@Override
	public Object getObject() {
		return standards;
	}

	@Override
	public HashMap<String,String> getPrintableValues(DependencyManager dependencyManager) {
		return null;
	}

	@Override
	public int requestObject(DependencyManager dependencyManager) {
		Calculator calculator = (Calculator) dependencyManager.getStepCalculator();

		HashMap<Integer,Integer> groups = calculator.getGroups();

		if (groups.keySet().size() == 0) {
			standardIds = null;

		} else {
			standardIds = new int[groups.keySet().size()];
	
			int count = 0;
			for (int key : groups.keySet()) {
				standardIds[count++] = key;
			}
		}

		standards = new Standard[standardIds == null ? 0 : standardIds.length];

		if (standardIds == null || standardIds.length == 0) {
			return Command.UNDEFINED_ID;
		}

		return StandardCache.getInstance().standardGet(standardIds[currentStandardId], dependencyManager);
	}

	@Override
	public int receivedObject(DependencyManager dependencyManager, Object object) {
		standards[currentStandardId] = (Standard) object;

		currentStandardId++;

		if (currentStandardId >= standardIds.length) {
			return Command.UNDEFINED_ID;
		}

		return StandardCache.getInstance().standardGet(standardIds[currentStandardId], dependencyManager);
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
		}

		return true;
	}

	@Override
	public AbstractCache[] getCachesToListenTo() {
		return new AbstractCache[] { StandardCache.getInstance() };
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

		return false;
	}
}
