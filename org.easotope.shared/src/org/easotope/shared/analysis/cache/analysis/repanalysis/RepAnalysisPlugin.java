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

package org.easotope.shared.analysis.cache.analysis.repanalysis;

import java.util.HashSet;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.commands.RepAnalysisGet;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class RepAnalysisPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new RepAnalysisCacheKey((Integer) parameters[0]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		RepAnalysisGet repAnalysisGet = new RepAnalysisGet();
		repAnalysisGet.setRepAnalysisId((Integer) parameters[0]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(repAnalysisGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return repAnalysisGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		RepAnalysisGet repAnalysisGet = (RepAnalysisGet) command;
		RepAnalysis repAnalysis = repAnalysisGet.getRepAnalysis();
		List<RepStep> repSteps = repAnalysisGet.getRepSteps();

		cache.put(new RepAnalysisCacheKey(repAnalysis.getId()), this, callParameters, new Object[] { repAnalysis, repSteps });
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		if (listener instanceof AnalysisCacheRepAnalysisGetListener) {
			Object[] resultArray = (Object[]) result;
			@SuppressWarnings("unchecked")
			List<RepStep> repSteps = (List<RepStep>) resultArray[1];
			((AnalysisCacheRepAnalysisGetListener) listener).repAnalysisGetCompleted(commandId, (RepAnalysis) resultArray[0], repSteps);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof AnalysisCacheRepAnalysisGetListener) {
			((AnalysisCacheRepAnalysisGetListener) listener).repAnalysisGetError(commandId, message);
		}
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatNeedReloadBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public HashSet<CacheKey> updateCacheBasedOnEvent(Event event, CacheHashMap cache) {
		assert(false);
		return null;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		assert(false);
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		assert(false);
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		assert(false);
		return 0;
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		assert(false);
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		assert(false);
	}
	
	@Override
	public void callbackVerifyAndResend(Object listener, int commandId, String message) {
		assert(false);
	}

	@Override
	public int deleteData(AbstractCache abstractCache, Object[] parameters) {
		assert(false);
		return 0;
	}

	@Override
	public void callbackDeleteCompleted(Object listener, Command command) {
		assert(false);
	}

	@Override
	public void callbackDeleteError(Object listener, int commandId, String message) {
		assert(false);
	}
}
