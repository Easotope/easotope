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

package org.easotope.client.analysis.superclass;

import java.util.HashMap;
import java.util.List;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.replicate.ReplicateAnalysisPart;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrintervalcomp.CorrIntervalCacheCorrIntervalCompGetListener;
import org.easotope.shared.analysis.cache.corrinterval.repstepparams.CorrIntervalCacheRepStepParamsSaveListener;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.CorrIntervalScratchPad;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.eclipse.swt.widgets.Composite;

public abstract class RepStepParamComposite extends EditorComposite implements CorrIntervalCacheCorrIntervalCompGetListener, CorrIntervalCacheRepStepParamsSaveListener {
	private static final String REPSTEP_PARAMETERS_GET = "REPSTEP_PARAMETERS_GET";
	private static final String REPSTEP_PARAMETERS_SAVE = "REPSTEP_PARAMETERS_SAVE";

	private Integer corrIntervalId = null;
	private Integer repAnalysisId = null;
	private Integer repStepPosition = null;

	abstract protected HashMap<String,Object> buildNewParameters();

	protected RepStepParamComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);
		CorrIntervalCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		super.handleDispose();
		CorrIntervalCache.getInstance().removeListener(this);
	}

	@Override
	final protected void setDefaultFieldValues() {
		setCurrentFieldValues();
	}

	protected Object getParameter(String key) {
		RepStepParams repStepParameters = (RepStepParams) getCurrentObject();

		if (repStepParameters == null) {
			return null;
		}

		HashMap<String,Object> currentParameters = repStepParameters.getParameters();

		if (currentParameters == null) {
			return null;
		}

		return currentParameters.get(key);
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		Integer corrIntervalId = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_CORR_INTERVAL_ID);		
		Integer dataAnalysisTypeId = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID);
		Integer repStepPosition = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_REPSTEP_POSITION);

		boolean result = this.corrIntervalId != corrIntervalId || this.repAnalysisId != dataAnalysisTypeId || this.repStepPosition != repStepPosition;
		
		this.corrIntervalId = corrIntervalId;
		this.repAnalysisId = dataAnalysisTypeId;
		this.repStepPosition = repStepPosition;
		
		return result;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		if (corrIntervalId == null || corrIntervalId == DatabaseConstants.EMPTY_DB_ID || repAnalysisId == null || repAnalysisId == DatabaseConstants.EMPTY_DB_ID || repStepPosition == null) {
			return false;
		}

		int commandId = CorrIntervalCache.getInstance().corrIntervalCompGet(corrIntervalId, repAnalysisId, this);
		waitingFor(REPSTEP_PARAMETERS_GET, commandId);

		return true;
	}

	@Override
	protected void requestSave(boolean isResend) {
		RepStepParams repStepParams = null;

		if (getCurrentObject() == null) {
			repStepParams = new RepStepParams();
			repStepParams.setCorrIntervalId(corrIntervalId);
			repStepParams.setAnalysisId(repAnalysisId);
			repStepParams.setPosition(repStepPosition);
		} else {
			repStepParams = new RepStepParams(((RepStepParams) getCurrentObject()));
		}

		repStepParams.setParameters(buildNewParameters());
		int commandId = CorrIntervalCache.getInstance().repStepParametersSave(repStepParams, this);
		waitingFor(REPSTEP_PARAMETERS_SAVE, commandId);
	}

	@Override
	protected boolean canDelete() {
		return false;
	}

	@Override
	protected boolean requestDelete() {
		return false;
	}

	@Override
	public void corrIntervalCompGetCompleted(int commandId, int corrIntervalId, int dataAnalysisTypeId, List<RepStepParams> repStepParams, CorrIntervalScratchPad corrIntervalScratchPad, List<CorrIntervalError> errors) {
		if (commandIdForKey(REPSTEP_PARAMETERS_GET) == commandId) {
			for (RepStepParams parameter : repStepParams) {
				if (parameter.getPosition() == repStepPosition) {
					newObject(REPSTEP_PARAMETERS_GET, parameter);
					return;
				}
			}

			newObject(REPSTEP_PARAMETERS_GET, null);
		}
	}

	@Override
	public void corrIntervalCompUpdated(int commandId, int corrIntervalId, int dataAnalysisTypeId, List<RepStepParams> repStepParams, CorrIntervalScratchPad corrIntervalScratchPad, List<CorrIntervalError> errors) {
		if (this.corrIntervalId != null && this.corrIntervalId == corrIntervalId && this.repAnalysisId != null && this.repAnalysisId == dataAnalysisTypeId) {
			for (RepStepParams parameter : repStepParams) {
				if (parameter.getPosition() == repStepPosition) {
					updateObject(parameter, Messages.corrIntervalComp_hasBeenUpdated);
					return;
				}
			}
		}
	}

	@Override
	public void corrIntervalCompGetError(int commandId, String message) {
		raiseGetError(REPSTEP_PARAMETERS_GET, message);		
	}

	@Override
	public void repStepParamsSaveCompleted(int commandId, RepStepParams repStepParameters) {
		saveComplete(REPSTEP_PARAMETERS_SAVE, repStepParameters);
	}

	@Override
	public void repStepParamsSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(REPSTEP_PARAMETERS_SAVE, message);
	}
}
