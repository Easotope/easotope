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
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.AcidTempParameter;
import org.easotope.shared.admin.cache.sampletype.SampleTypeCache;
import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.core.cache.AbstractCache;

public class AcidInfoPlugin extends DependencyPlugin {
	private AcidTemp acidTemp = null;

	AcidInfoPlugin() {
		super("acid information");
	}

	@Override
	public Object getObject() {
		return acidTemp;
	}

	@Override
	public HashMap<String,String> getPrintableValues(DependencyManager dependencyManager) {
		HashMap<String,String> result = new HashMap<String,String>();

		Double temperature = null;

		if (acidTemp != null) {
			temperature = acidTemp.getTemperature();
		}

		result.put(Messages.acidInfoPlugin_temperature, temperature == null ? "UNDEFINED" : String.valueOf(temperature));

		return result;
	}

	@Override
	public int requestObject(DependencyManager dependencyManager) {
		int acidTempId = dependencyManager.getReplicate().getAcidTempId();
		return acidTempId == DatabaseConstants.EMPTY_DB_ID ? Command.UNDEFINED_ID : SampleTypeCache.getInstance().acidTempGet(acidTempId, dependencyManager);
	}

	@Override
	public int receivedObject(DependencyManager dependencyManager, Object object) {
		acidTemp = (AcidTemp) object;
		return Command.UNDEFINED_ID;
	}

	@Override
	public boolean verifyCurrentObject(DependencyManager dependencyManager) {
		int acidTempId = dependencyManager.getReplicate().getAcidTempId();

		if (acidTempId == DatabaseConstants.EMPTY_DB_ID && acidTemp == null) {
			return true;
		}

		if (acidTempId != DatabaseConstants.EMPTY_DB_ID && acidTemp != null && acidTemp.getId() == acidTempId && acidTemp.getValues().get(AcidTempParameter.clumped.ordinal()) != null) {
			return true;
		}

		return false;
	}

	@Override
	public AbstractCache[] getCachesToListenTo() {
		return new AbstractCache[] { SampleTypeCache.getInstance() };
	}

	@Override
	public boolean isNoLongerValid(DependencyManager dependencyManager, Object object) {
		if (object instanceof AcidTemp) {
			AcidTemp newAcidTemp = (AcidTemp) object;

			if (acidTemp != null && newAcidTemp.getId() == acidTemp.getId()) {
				return true;
			}
		}

		return false;
	}
}
