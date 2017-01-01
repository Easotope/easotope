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

package org.easotope.shared.analysis.repstep.co2.clumpcalc.dependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.admin.RefGasParameter;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.admin.cache.massspec.refgaslist.RefGasList;
import org.easotope.shared.admin.tables.RefGas;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.core.NumericValue;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.rawdata.tables.ReplicateV1;

public class RefGasPlugin extends DependencyPlugin {
	private RefGas refGas = null;

	RefGasPlugin() {
		super("reference gas");
	}

	@Override
	public Object getObject() {
		assert(getState() == PluginState.VALID);
		return refGas;
	}

	@Override
	public HashMap<String,String> getPrintableValues(DependencyManager dependencyManager) {
		HashMap<String,String> result = new HashMap<String,String>();

		if (refGas.getValues() != null) {
			NumericValue value = refGas.getValues().get(RefGasParameter.δ13C.ordinal());

			if (value != null) {
				result.put("Reference gas " + RefGasParameter.δ13C.toString(), String.valueOf(value.getValue()));
			}

			value = refGas.getValues().get(RefGasParameter.δ18O.ordinal());

			if (value != null) {
				result.put("Reference gas " + RefGasParameter.δ18O.toString(), String.valueOf(value.getValue()));
			}
		}

		return result;
	}

	@Override
	public int requestObject(DependencyManager dependencyManager) {
		int massSpecId = dependencyManager.getReplicate().getMassSpecId();
		return MassSpecCache.getInstance().refGasListGet(massSpecId, dependencyManager);
	}

	@Override
	public int receivedObject(DependencyManager dependencyManager, Object object) {
		if (object instanceof RefGasList) {
			ReplicateV1 replicate = dependencyManager.getReplicate();
			RefGasList refGasList = (RefGasList) object;

			ArrayList<ValidFromAndId> list = new ArrayList<ValidFromAndId>();

			for (Integer id : refGasList.keySet()) {
				long validFrom = refGasList.get(id).getValidFrom();
				list.add(new ValidFromAndId(validFrom, id));
			}

			int lastRefGasId = DatabaseConstants.EMPTY_DB_ID;
			Collections.sort(list);

			for (ValidFromAndId validFromAndId : list) {
				if (validFromAndId.getValidFrom() > replicate.getDate()) {
					if (lastRefGasId != DatabaseConstants.EMPTY_DB_ID) {
						return MassSpecCache.getInstance().refGasGet(lastRefGasId, dependencyManager);
					}
				} else {
					lastRefGasId = validFromAndId.getId();
				}
			}

			if (lastRefGasId != DatabaseConstants.EMPTY_DB_ID) {
				return MassSpecCache.getInstance().refGasGet(lastRefGasId, dependencyManager);
			}

			return Command.UNDEFINED_ID;
		}

		if (object instanceof RefGas) {
			refGas = (RefGas) object;
			return Command.UNDEFINED_ID;
		}

		return Command.UNDEFINED_ID;
	}

	@Override
	public boolean verifyCurrentObject(DependencyManager dependencyManager) {
		ReplicateV1 replicate = dependencyManager.getReplicate();
		return refGas != null && refGas.getMassSpecId() == replicate.getMassSpecId() && refGas.validForTime(replicate.getDate());
	}

	@Override
	public AbstractCache[] getCachesToListenTo() {
		return new AbstractCache[] { MassSpecCache.getInstance() };
	}

	@Override
	public boolean isNoLongerValid(DependencyManager dependencyManager, Object object) {
		if (refGas != null && object instanceof RefGas) {
			RefGas updatedRefGas = (RefGas) object;

			if (refGas.getId() == updatedRefGas.getId()) {
				return true;
			}
		}

		return false;
	}

	private class ValidFromAndId implements Comparable<ValidFromAndId> {
		private Long validFrom;
		private int id;

		ValidFromAndId(long validFrom, int id) {
			this.validFrom = validFrom;
			this.id = id;
		}

		public Long getValidFrom() {
			return validFrom;
		}

		public int getId() {
			return id;
		}

		@Override
		public int compareTo(ValidFromAndId that) {
			return this.getValidFrom().compareTo(that.getValidFrom());
		}
	}
}
