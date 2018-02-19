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

package org.easotope.shared.analysis.repstep.co2.co2calc.dependencies;

import java.util.HashMap;

import org.easotope.framework.commands.Command;
import org.easotope.shared.admin.SciConstantNames;
import org.easotope.shared.admin.cache.sciconstant.SciConstantCache;
import org.easotope.shared.admin.tables.SciConstant;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.core.cache.AbstractCache;

public class R13VPDBPlugin extends DependencyPlugin {
	SciConstant sciConstant;

	R13VPDBPlugin() {
		super(SciConstantNames.R13_VPDB.toString());
	}

	@Override
	public Object getObject() {
		return sciConstant;
	}

	@Override
	public HashMap<String,String> getPrintableValues(DependencyManager dependencyManager) {
		HashMap<String,String> result = new HashMap<String,String>();
		result.put(getName(), String.valueOf(sciConstant.getValue()));
		return result;
	}

	@Override
	public int requestObject(DependencyManager dependencyManager) {
		return SciConstantCache.getInstance().sciConstantGet(SciConstantNames.R13_VPDB, dependencyManager);
	}

	@Override
	public int receivedObject(DependencyManager dependencyManager, Object object) {
		sciConstant = (SciConstant) object;
		return Command.UNDEFINED_ID;
	}

	@Override
	public boolean verifyCurrentObject(DependencyManager dependencyManager) {
		return sciConstant != null && sciConstant.getEnumeration() == SciConstantNames.R13_VPDB;
	}

	@Override
	public AbstractCache[] getCachesToListenTo() {
		// SciConstants cannot be changed at the moment
		return null;
	}

	@Override
	public boolean isNoLongerValid(DependencyManager dependencyManager, Object object) {
		// SciConstants cannot be changed at the moment
		return false;
	}
}
