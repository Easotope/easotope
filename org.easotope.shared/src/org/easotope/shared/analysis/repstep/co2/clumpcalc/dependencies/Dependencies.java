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

package org.easotope.shared.analysis.repstep.co2.clumpcalc.dependencies;

import org.easotope.shared.admin.tables.RefGas;
import org.easotope.shared.admin.tables.SciConstant;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;

public class Dependencies extends DependencyManager {
	public Dependencies() {
		addPlugin(new RefGasPlugin());
		addPlugin(new R13VPDBPlugin());
		addPlugin(new R17VSMOWPlugin());
		addPlugin(new R18VSMOWPlugin());
		addPlugin(new LambdaPlugin());
		addPlugin(new D18OVPDBVSMOWPlugin());
	}

	public RefGas getReferenceGas() {
		return (RefGas) getDependencyPlugins().get(0).getObject();
	}

	public double getR13_VPDB() {
		return ((SciConstant) getDependencyPlugins().get(1).getObject()).getValue();
	}

	public double getR17_VSMOW() {
		return ((SciConstant) getDependencyPlugins().get(2).getObject()).getValue();
	}

	public double getR18_VSMOW() {
		return ((SciConstant) getDependencyPlugins().get(3).getObject()).getValue();
	}

	public double getλ() {
		return ((SciConstant) getDependencyPlugins().get(4).getObject()).getValue();
	}

	public double getδ18O_VPDB_VSMOW() {
		return ((SciConstant) getDependencyPlugins().get(5).getObject()).getValue();
	}
}
