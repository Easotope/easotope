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

package org.easotope.shared.analysis.cache.corrinterval.corrintervalcomp;

import java.util.HashSet;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.analysis.events.CorrIntervalCompUpdated;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.CorrIntervalScratchPad;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.commands.CorrIntervalCompGet;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheHashMap;
import org.easotope.shared.core.cache.CacheKey;
import org.easotope.shared.core.cache.CachePlugin;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;

public class CorrIntervalCompPlugin extends CachePlugin {
	@Override
	public CacheKey createCacheKey(Object[] parameters) {
		return new CorrIntervalCompCacheKey((Integer) parameters[0], (Integer) parameters[1]);
	}

	@Override
	public int getData(AbstractCache abstractCache, Object[] parameters) {
		CorrIntervalCompGet corrIntervalCompGet = new CorrIntervalCompGet();
		corrIntervalCompGet.setCorrIntervalId((Integer) parameters[0]);
		corrIntervalCompGet.setDataAnalysisId((Integer) parameters[1]);

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(corrIntervalCompGet, LoginInfoCache.getInstance().getAuthenticationObjects(), abstractCache);

		return corrIntervalCompGet.getClientUniqueId();
	}

	@Override
	public void processData(Command command, CacheHashMap cache, Object[] callParameters) {
		CorrIntervalCompGet corrIntervalCompGet = (CorrIntervalCompGet) command;
		int corrIntervalId = corrIntervalCompGet.getCorrIntervalId();
		int dataAnalysisId = corrIntervalCompGet.getDataAnalysisId();
		List<RepStepParams> repStepParameters = corrIntervalCompGet.getRepStepParams();
		CorrIntervalScratchPad corrIntervalScratchPad = corrIntervalCompGet.getCorrIntervalScratchPad();
		List<CorrIntervalError> errors = corrIntervalCompGet.getCorrIntervalErrors();

		cache.put(new CorrIntervalCompCacheKey(corrIntervalId, dataAnalysisId), this, callParameters, new Object[] { corrIntervalId, dataAnalysisId, repStepParameters, corrIntervalScratchPad, errors });
	}

	@Override
	public void callbackGetCompleted(Object listener, int commandId, Object result) {
		int corrIntervalId = (Integer) ((Object[]) result)[0];
		int dataAnalysisId = (Integer) ((Object[]) result)[1];
		@SuppressWarnings("unchecked")
		List<RepStepParams> repStepParams = (List<RepStepParams>) ((Object[]) result)[2];
		CorrIntervalScratchPad corrIntervalScratchPad = (CorrIntervalScratchPad) ((Object[]) result)[3];
		@SuppressWarnings("unchecked")
		List<CorrIntervalError> errors = (List<CorrIntervalError>) ((Object[]) result)[4];
		
		if (listener instanceof CorrIntervalCacheCorrIntervalCompGetListener) {
			((CorrIntervalCacheCorrIntervalCompGetListener) listener).corrIntervalCompGetCompleted(commandId, corrIntervalId, dataAnalysisId, repStepParams, corrIntervalScratchPad, errors);
		}
	}

	@Override
	public void callbackGetError(Object listener, int commandId, String message) {
		if (listener instanceof CorrIntervalCacheCorrIntervalCompGetListener) {
			((CorrIntervalCacheCorrIntervalCompGetListener) listener).corrIntervalCompGetError(commandId, message);
		}
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatShouldBeDeletedBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public HashSet<CacheKey> getCacheKeysThatNeedReloadBasedOnEvent(Event event, CacheHashMap cache) {
		HashSet<CacheKey> result = new HashSet<CacheKey>();

		if (event instanceof CorrIntervalCompUpdated) {
			CorrIntervalCompUpdated corrIntervalsCompUpdated = (CorrIntervalCompUpdated) event;

			for (int i=0; i<corrIntervalsCompUpdated.size(); i++) {
				CorrIntervalCompCacheKey corrIntervalCompCacheKey = new CorrIntervalCompCacheKey(corrIntervalsCompUpdated.getCorrIntervalId(i), corrIntervalsCompUpdated.getDataAnalysisId(i));
				CacheKey oldCacheKey = cache.getKey(corrIntervalCompCacheKey);

				if (oldCacheKey != null) {
					result.add(oldCacheKey);
				}
			}
		}

		return result;
	}

	@Override
	public HashSet<CacheKey> updateCacheBasedOnEvent(Event event, CacheHashMap cache) {
		return null;
	}

	@Override
	public void callbackUpdated(Object listener, int commandId, Object updated) {
		int corrIntervalId = (Integer) ((Object[]) updated)[0];
		int dataAnalysisTypeId = (Integer) ((Object[]) updated)[1];
		@SuppressWarnings("unchecked")
		List<RepStepParams> repStepParameters = (List<RepStepParams>) ((Object[]) updated)[2];
		CorrIntervalScratchPad corrIntervalScratchPad = (CorrIntervalScratchPad) ((Object[]) updated)[3];
		@SuppressWarnings("unchecked")
		List<CorrIntervalError> errors = (List<CorrIntervalError>) ((Object[]) updated)[4];

		if (listener instanceof CorrIntervalCacheCorrIntervalCompGetListener) {
			((CorrIntervalCacheCorrIntervalCompGetListener) listener).corrIntervalCompUpdated(commandId, corrIntervalId, dataAnalysisTypeId, repStepParameters, corrIntervalScratchPad, errors);
		}
	}

	@Override
	public void callbackDeleted(Object listener, CacheKey cacheKey) {
		
	}

	@Override
	public int saveData(AbstractCache abstractCache, Object[] parameters) {
		// ignore
		return DatabaseConstants.EMPTY_DB_ID;
	}

	@Override
	public void callbackSaveCompleted(Object listener, Command command) {
		// ignore
	}

	@Override
	public void callbackSaveError(Object listener, int commandId, String message) {
		// ignore
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
