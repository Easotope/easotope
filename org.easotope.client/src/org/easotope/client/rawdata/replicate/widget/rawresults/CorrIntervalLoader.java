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

package org.easotope.client.rawdata.replicate.widget.rawresults;

import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

import org.easotope.client.core.part.EditorComposite;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.repanalysis.AnalysisCacheRepAnalysisGetListener;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrinterval.CorrIntervalCacheCorrIntervalGetListener;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalCacheCorrIntervalListGetListener;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListItem;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.eclipse.swt.widgets.Display;

public class CorrIntervalLoader implements CorrIntervalCacheCorrIntervalListGetListener, CorrIntervalCacheCorrIntervalGetListener, AnalysisCacheRepAnalysisGetListener {
	private final String WAITING_FOR_CORR_INTERVAL_LIST = "WAITING_FOR_CORR_INTERVAL_LIST";
	private final String WAITING_FOR_CORR_INTERVAL = "WAITING_FOR_CORR_INTERVAL";
	private final String WAITING_FOR_DATA_ANALYSIS = "WAITING_FOR_DATA_ANALYSIS";

	private EditorComposite editorComposite;

	private long replicateDate = Long.MIN_VALUE;

	private CorrIntervalList currentCorrIntervalList;
	private CorrIntervalV1 currentCorrInterval;
	private TreeMap<Integer,RepAnalysis> repAnalysisType = new TreeMap<Integer,RepAnalysis>();
	private TreeMap<Integer,List<RepStep>> repAnalysisSteps = new TreeMap<Integer,List<RepStep>>();

	private Vector<CorrIntervalLoaderListener> listeners = new Vector<CorrIntervalLoaderListener>();

	CorrIntervalLoader(EditorComposite editorComposite) {
		this.editorComposite = editorComposite;

		CorrIntervalCache.getInstance().addListener(this);
		AnalysisCache.getInstance().addListener(this);
	}

	void dispose() {
		CorrIntervalCache.getInstance().removeListener(this);
		AnalysisCache.getInstance().removeListener(this);
	}

	void disable() {
		editorComposite.cancelWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST);
		currentCorrIntervalList = null;
		clearForCorrIntervalLoad();
	}

	private void clearForCorrIntervalLoad() {
		editorComposite.cancelWaitingFor(WAITING_FOR_CORR_INTERVAL);

		if (currentCorrInterval != null) {
			int[] dataAnalysis = currentCorrInterval.getDataAnalysis();

			for (int i : dataAnalysis) {
				editorComposite.cancelWaitingFor(WAITING_FOR_DATA_ANALYSIS + i);
			}
		}

		currentCorrInterval = null;
		repAnalysisType.clear();
	}

	void newValues(int massSpecId, long replicateDate) {
		this.replicateDate = replicateDate;

		if (currentCorrInterval != null && currentCorrInterval.getMassSpecId() == massSpecId && currentCorrInterval.validForTime(replicateDate)) {
			return;
		}

		editorComposite.cancelWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST);
		currentCorrIntervalList = null;
		clearForCorrIntervalLoad();

		int commandId = CorrIntervalCache.getInstance().corrIntervalListGet(massSpecId, this);
		editorComposite.waitingFor(WAITING_FOR_CORR_INTERVAL_LIST, commandId);

		notifyListeners();
	}

	public int getCorrIntervalId() {
		return currentCorrInterval.getId();
	}

	RepAnalysis[] getRepAnalysis() {
		if (repAnalysisType.isEmpty() || repAnalysisType.containsValue(null)) {
			return null;
		} else {
			return repAnalysisType.values().toArray(new RepAnalysis[repAnalysisType.size()]);
		}
	}

	List<RepStep> getRepAnalysisStepsById(int id) {
		return repAnalysisSteps.get(id);
	}

	void addListener(CorrIntervalLoaderListener listener) {
		listeners.add(listener);
	}

	void removeListener(CorrIntervalLoaderListener listener) {
		listeners.remove(listener);
	}
	
	private void notifyListeners() {
		for (CorrIntervalLoaderListener listener: listeners) {
			listener.corrIntervalInfoChanged();
		}
	}

	@Override
	public Display getDisplay() {
		return editorComposite.getDisplay();
	}

	@Override
	public boolean stillCallabled() {
		return editorComposite.stillCallabled();
	}

	private void newCorrIntervalList() {
		int corrIntervalId = DatabaseConstants.EMPTY_DB_ID;
		long corrIntervalDate = Long.MIN_VALUE;

		for (int id : currentCorrIntervalList.keySet()) {
			CorrIntervalListItem item = currentCorrIntervalList.get(id);

			if (item.getDate() > corrIntervalDate && item.getDate() <= replicateDate) {
				corrIntervalId = id;
				corrIntervalDate = item.getDate();
			}
		}

		if (corrIntervalId != DatabaseConstants.EMPTY_DB_ID) {
			int newCommandId = CorrIntervalCache.getInstance().corrIntervalGet(corrIntervalId, this);
			editorComposite.waitingFor(WAITING_FOR_CORR_INTERVAL, newCommandId);
		}

		editorComposite.doneWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST);
	}

	@Override
	public void corrIntervalListGetCompleted(int commandId, CorrIntervalList corrIntervalList) {
		if (commandId == editorComposite.commandIdForKey(WAITING_FOR_CORR_INTERVAL_LIST)) {
			currentCorrIntervalList = corrIntervalList;
			newCorrIntervalList();

			editorComposite.cancelWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST);

			if (commandId != Command.UNDEFINED_ID) {
				notifyListeners();
			}
		}
	}

	@Override
	public void corrIntervalListUpdated(int commandId, CorrIntervalList corrIntervalList) {
		if (currentCorrIntervalList != null && currentCorrIntervalList.getMassSpecId() == corrIntervalList.getMassSpecId()) {
			clearForCorrIntervalLoad();

			currentCorrIntervalList = corrIntervalList;
			newCorrIntervalList();
			
			notifyListeners();
		}
	}

	@Override
	public void corrIntervalListGetError(int commandId, String message) {
		editorComposite.raiseGetError(WAITING_FOR_CORR_INTERVAL_LIST, message);
	}

	private void newCorrInterval() {
		int[] dataAnalysis = currentCorrInterval.getDataAnalysis();

		repAnalysisType.clear();
		repAnalysisSteps.clear();

		for (int dataAnalysisId : dataAnalysis) {
			repAnalysisType.put(dataAnalysisId, null);
			repAnalysisSteps.put(dataAnalysisId, null);

			int commandId = AnalysisCache.getInstance().repAnalysisGet(dataAnalysisId, this);
			editorComposite.waitingFor(WAITING_FOR_DATA_ANALYSIS + dataAnalysisId, commandId);
		}
		
		editorComposite.doneWaitingFor(WAITING_FOR_CORR_INTERVAL);
	}

	@Override
	public void corrIntervalGetCompleted(int commandId, CorrIntervalV1 corrInterval) {
		if (commandId == editorComposite.commandIdForKey(WAITING_FOR_CORR_INTERVAL)) {
			editorComposite.cancelWaitingFor(WAITING_FOR_CORR_INTERVAL_LIST);

			currentCorrInterval = corrInterval;
			newCorrInterval();

			if (commandId != Command.UNDEFINED_ID) {
				notifyListeners();
			}
		}
	}

	@Override
	public void corrIntervalUpdated(int commandId, CorrIntervalV1 corrInterval) {
		if (currentCorrInterval != null && corrInterval.getId() == currentCorrInterval.getId()) {
			for (int id : currentCorrInterval.getDataAnalysis()) {
				editorComposite.cancelWaitingFor(WAITING_FOR_DATA_ANALYSIS + id);
			}

			currentCorrInterval = corrInterval;
			newCorrInterval();
			
			notifyListeners();
		}
	}

	@Override
	public void corrIntervalGetError(int commandId, String message) {
		editorComposite.raiseGetError(WAITING_FOR_CORR_INTERVAL, message);
	}

	@Override
	public void repAnalysisGetCompleted(int commandId, RepAnalysis repAnalysis, List<RepStep> steps) {
		if (commandId == editorComposite.commandIdForKey(WAITING_FOR_DATA_ANALYSIS + repAnalysis.getId())) {
			repAnalysisType.put(repAnalysis.getId(), repAnalysis);
			repAnalysisSteps.put(repAnalysis.getId(), steps);

			editorComposite.cancelWaitingFor(WAITING_FOR_DATA_ANALYSIS + repAnalysis.getId());

			if (commandId != Command.UNDEFINED_ID && !repAnalysisType.containsValue(null)) {
				notifyListeners();
			}
		}
	}

	@Override
	public void repAnalysisGetError(int commandId, String message) {
		if (currentCorrInterval != null) {
			for (int dataAnalysisId : currentCorrInterval.getDataAnalysis()) {
				if (commandId == editorComposite.commandIdForKey(WAITING_FOR_DATA_ANALYSIS + dataAnalysisId)) {
					editorComposite.raiseGetError(WAITING_FOR_DATA_ANALYSIS + dataAnalysisId, message);	
				}
			}
		}
	}
}
