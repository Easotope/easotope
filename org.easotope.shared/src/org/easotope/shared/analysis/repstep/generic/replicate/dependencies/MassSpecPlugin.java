/*
 * Copyright © 2016 by Devon Bowen.
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

package org.easotope.shared.analysis.repstep.generic.replicate.dependencies;

import java.util.HashMap;

import org.easotope.framework.commands.Command;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecList;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecListItem;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.core.cache.AbstractCache;

public class MassSpecPlugin extends DependencyPlugin {
	private String massSpec = null;

	MassSpecPlugin() {
		super("mass spec");
	}

	@Override
	public Object getObject() {
		return massSpec;
	}

	@Override
	public HashMap<String,String> getPrintableValues(DependencyManager dependencyManager) {
		HashMap<String,String> result = new HashMap<String,String>();
		result.put(Messages.massSpecPlugin_massSpec, massSpec == null ? "UNDEFINED" : massSpec);
		return result;
	}

	@Override
	public int requestObject(DependencyManager dependencyManager) {
		return MassSpecCache.getInstance().massSpecListGet(dependencyManager);
	}

	@Override
	public int receivedObject(DependencyManager dependencyManager, Object object) {
		int massSpecId = dependencyManager.getReplicate().getMassSpecId();
		MassSpecListItem massSpecItem = ((MassSpecList) object).get(massSpecId);

		if (massSpecItem != null) {
			massSpec = massSpecItem.getName();
		}

		return Command.UNDEFINED_ID;
	}

	@Override
	public boolean verifyCurrentObject(DependencyManager dependencyManager) {
		return massSpec != null;
	}

	@Override
	public AbstractCache[] getCachesToListenTo() {
		return new AbstractCache[] { MassSpecCache.getInstance() };
	}

	@Override
	public boolean isNoLongerValid(DependencyManager dependencyManager, Object object) {
		if (object instanceof MassSpecList) {
			int massSpecId = dependencyManager.getReplicate().getMassSpecId();
			MassSpecListItem massSpecItem = ((MassSpecList) object).get(massSpecId);

			if (massSpecItem == null && massSpec != null) {
				return true;
			}

			if (massSpecItem != null && massSpec == null) {
				return true;
			}

			if (massSpecItem != null && !massSpecItem.getName().equals(massSpec)) {
				return true;
			}
		}

		return false;
	}
}
