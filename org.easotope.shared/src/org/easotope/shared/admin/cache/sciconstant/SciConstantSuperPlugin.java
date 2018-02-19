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

package org.easotope.shared.admin.cache.sciconstant;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.admin.cache.sciconstant.sciconstant.SciConstantCacheKey;
import org.easotope.shared.admin.cache.sciconstant.sciconstantlist.SciConstantList;
import org.easotope.shared.admin.cache.sciconstant.sciconstantlist.SciConstantListCacheKey;
import org.easotope.shared.admin.cache.sciconstant.sciconstantlist.SciConstantListItem;
import org.easotope.shared.admin.tables.SciConstant;
import org.easotope.shared.commands.SciConstantsGetAll;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public abstract class SciConstantSuperPlugin extends CachePlugin {
	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		Processor processor = ProcessorManager.getInstance().getProcessor();
		Command command = new SciConstantsGetAll();
		processor.process(command, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);
		return command.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		SciConstantsGetAll constantsGetAll = (SciConstantsGetAll) command;
		SciConstantList sciConstantList = new SciConstantList();

		for (SciConstant sciConstant : constantsGetAll.getConstants()) {
			sciConstantList.put(sciConstant.getId(), new SciConstantListItem(sciConstant.getEnumeration().toString()));

			cache.put(new SciConstantCacheKey(sciConstant.getId()), this, callParameters, sciConstant);
			cache.put(new SciConstantCacheKey(sciConstant.getEnumeration()), this, callParameters, sciConstant);
		}

		cache.put(new SciConstantListCacheKey(), this, callParameters, sciConstantList);
	}
}
