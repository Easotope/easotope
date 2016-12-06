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

package org.easotope.shared.core.cache;

import java.util.HashSet;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;

public abstract class CachePlugin {
	public abstract CacheKey createCacheKey(Object[] parameters);

	public abstract int getData(AbstractCache abstractCache, Object[] callParameters);
	public abstract void processData(Command command, CacheHashMap cache, Object[] callParameters);
	public abstract void callbackGetCompleted(Object listener, int commandId, Object result);
	public abstract void callbackGetError(Object listener, int commandId, String message);

	public abstract HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache);
	public abstract void callbackDeleted(Object listener, CacheKey cacheKey);
	public abstract HashSet<CacheKey> updateCacheBasedOnEvent(Event event, CacheHashMap cache);
	public abstract void callbackUpdated(Object listener, int commandId, Object updated);
	public abstract HashSet<CacheKey> getCacheKeysThatNeedReloadBasedOnEvent(Event event, CacheHashMap cache);

	public abstract int saveData(AbstractCache abstractCache, Object[] parameters);
	public abstract void callbackSaveCompleted(Object listener, Command command);
	public abstract void callbackSaveError(Object listener, int commandId, String message);

	public abstract int deleteData(AbstractCache abstractCache, Object[] parameters);
	public abstract void callbackDeleteCompleted(Object listener, Command command);
	public abstract void callbackDeleteError(Object listener, int commandId, String message);
}
