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

import org.easotope.client.Messages;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrintervalcomp.CorrIntervalCacheCorrIntervalCompGetListener;
import org.easotope.shared.analysis.execute.AnalysisCalculator;
import org.easotope.shared.analysis.execute.AnalysisCalculatorListener;
import org.easotope.shared.analysis.execute.AnalysisCompiled;
import org.easotope.shared.analysis.execute.AnalysisWithParameters;
import org.easotope.shared.analysis.execute.CalculationError;
import org.easotope.shared.analysis.execute.calculator.SingleReplicateCalculator;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.CorrIntervalScratchPad;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.core.tables.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ResultsCompositeTab extends EasotopeComposite implements CorrIntervalCacheCorrIntervalCompGetListener, AnalysisCalculatorListener {
	private final String WAITING_FOR_CORR_INTERVAL_COMP = "WAITING_FOR_CORR_INTERVAL_COMP";
	final String TABLE_LAYOUT_CONTEXT = "INPUT RESULTS";

	private EditorComposite editorComposite;

	private int corrIntervalId;
	private RepAnalysis repAnalysis;
	private List<RepStep> repSteps;
	private AnalysisCompiled dataAnalysisCompiled;

	private List<RepStepParams> repStepParameters;
	private ScratchPad<ReplicatePad> corrIntervalScratchPad;
	private List<CorrIntervalError> errors;

	private ScratchPad<ReplicatePad> replicateScratchPad;

	private SingleReplicateCalculator singleRunCalculator;
	private ReplicatePad currentReplicatePad;
	private boolean needToSetScratchPad = true;

	private StackLayout stackLayout;

	private ComputingComposite computingComposite;
	private ErrorComposite errorComposite;
	private ScratchPadTableComposite scratchPadTableComposite;

	public ResultsCompositeTab(EasotopePart easotopePart, EditorComposite editorComposite, Composite parent, int style) {
		super(easotopePart, parent, style);
		this.editorComposite = editorComposite;

		stackLayout = new StackLayout();
		setLayout(stackLayout);

		computingComposite = new ComputingComposite(this, SWT.NONE);
		errorComposite = new ErrorComposite(this, SWT.NONE);
		scratchPadTableComposite = new ScratchPadTableComposite(this, SWT.NONE);

		stackLayout.topControl = computingComposite;
		layout();

		CorrIntervalCache.getInstance().addListener(this);
	}

	void saveTableLayout(TableLayout tableLayout) {
		tableLayout.setUserId(LoginInfoCache.getInstance().getUser().getId());
		tableLayout.setContext(TABLE_LAYOUT_CONTEXT);
		tableLayout.setDataAnalysisId(repAnalysis.getId());

		LoginInfoCache.getInstance().saveTableLayout(tableLayout);

		CorrIntervalCache.getInstance().removeListener(this);
	}

	private String waitingForConstant() {
		if (repAnalysis == null) {
			return null;
		}

		return WAITING_FOR_CORR_INTERVAL_COMP + repAnalysis.getId();
	}

	@Override
	protected void handleDispose() {
		stopCurrentActivity();
	}

	void stopCurrentActivity() {
		editorComposite.cancelWaitingFor(waitingForConstant());
		
		if (singleRunCalculator != null) {
			singleRunCalculator.dispose();
			singleRunCalculator = null;
		}
	}

	void setRepAnalysis(int corrIntervalId, RepAnalysis repAnalysis, List<RepStep> repSteps) {
		stopCurrentActivity();
		needToSetScratchPad = true;

		this.corrIntervalId = corrIntervalId;
		this.repAnalysis = repAnalysis;
		this.repSteps = repSteps;
		this.dataAnalysisCompiled = new AnalysisCompiled(repAnalysis, repSteps);

		repStepParameters = null;
		corrIntervalScratchPad = null;
		errors = null;

		int commandId = CorrIntervalCache.getInstance().corrIntervalCompGet(corrIntervalId, repAnalysis.getId(), this);
		editorComposite.waitingFor(waitingForConstant(), commandId);

		startComputationIfPossible();
		renderStatus();
	}

	void newReplicate(ScratchPad<ReplicatePad> replicateScratchPad) {
		this.replicateScratchPad = replicateScratchPad;
		startComputationIfPossible();
		renderStatus();
	}

	private void startComputationIfPossible() {
		if (singleRunCalculator != null) {
			singleRunCalculator.dispose();
			singleRunCalculator = null;

			if (corrIntervalScratchPad != null && currentReplicatePad != null) {
				corrIntervalScratchPad.removeChild(currentReplicatePad);
				currentReplicatePad = null;
			}
		}

		if (replicateScratchPad != null && repStepParameters != null && corrIntervalScratchPad != null && errors != null) {
			AnalysisWithParameters dataAnalysisWithParameters = new AnalysisWithParameters(repAnalysis, repSteps, repStepParameters);

			ScratchPad<ReplicatePad> tmpScratchPad = new ScratchPad<ReplicatePad>(replicateScratchPad);
			currentReplicatePad = tmpScratchPad.getChild(0);
			currentReplicatePad.reassignToParent(corrIntervalScratchPad);

			singleRunCalculator = new SingleReplicateCalculator(dataAnalysisWithParameters, corrIntervalScratchPad, currentReplicatePad, errors);
			singleRunCalculator.execute();
			singleRunCalculator.addListener(this);
		}
	}

	private void renderStatus() {
		if (singleRunCalculator == null || !singleRunCalculator.isFinished()) {
			stackLayout.topControl = computingComposite;

		} else if (singleRunCalculator.getErrors().size() != 0) {
			CalculationError error = singleRunCalculator.getErrors().get(0);
			String step = (error.getStepController() != null) ? error.getStepController().getStepName() : Messages.resultsCompositeTab_emptyStepName;
			String message = (error.getErrorMessage() != null) ? error.getErrorMessage() : "";
			errorComposite.setErrorMessage(step, message);

			stackLayout.topControl = errorComposite;

		} else {
			boolean showStandardsErrorMessage = errors != null && errors.size() != 0;
			ScratchPad<ReplicatePad> tmpScratchPad = new ScratchPad<ReplicatePad>();
			currentReplicatePad.reassignToParent(tmpScratchPad);

			if (needToSetScratchPad) {
				scratchPadTableComposite.setScratchPad(dataAnalysisCompiled, tmpScratchPad, showStandardsErrorMessage);
			} else {
				scratchPadTableComposite.updateScratchPad(tmpScratchPad, showStandardsErrorMessage);
			}

			needToSetScratchPad = false;
			stackLayout.topControl = scratchPadTableComposite;
		}

		layout();
	}

	@Override
	public Display getDisplay() {
		return (!isDisposed()) ? super.getDisplay() : null;
	}

	@Override
	public boolean stillCallabled() {
		return editorComposite.stillCallabled();
	}

	@Override
	public void corrIntervalCompGetCompleted(int commandId, int corrIntervalId, int dataAnalysisId, List<RepStepParams> stepParameters, CorrIntervalScratchPad corrIntervalScratchPad, List<CorrIntervalError> errors) {
		if (editorComposite.commandIdForKey(waitingForConstant()) == commandId) {
			editorComposite.cancelWaitingFor(waitingForConstant());

			this.repStepParameters = stepParameters;
			//TODO we really should never get null here
			if (corrIntervalScratchPad == null || corrIntervalScratchPad.getScratchPad() == null) {
				this.corrIntervalScratchPad = new ScratchPad<ReplicatePad>();
			} else {
				this.corrIntervalScratchPad = new ScratchPad<ReplicatePad>(corrIntervalScratchPad.getScratchPad());
			}
			this.errors = errors;

			startComputationIfPossible();
			renderStatus();
		}
	}

	@Override
	public void corrIntervalCompUpdated(int commandId, int corrIntervalId, int dataAnalysisId, List<RepStepParams> repStepParameters, CorrIntervalScratchPad corrIntervalScratchPad, List<CorrIntervalError> errors) {
		if (this.corrIntervalId == corrIntervalId && repAnalysis.getId() == dataAnalysisId) {
			editorComposite.cancelWaitingFor(waitingForConstant());

			this.repStepParameters = repStepParameters;
			this.corrIntervalScratchPad = new ScratchPad<ReplicatePad>(corrIntervalScratchPad.getScratchPad());
			this.errors = errors;

			startComputationIfPossible();
			renderStatus();
		}
	}

	@Override
	public void corrIntervalCompGetError(int commandId, String message) {
		if (editorComposite.commandIdForKey(waitingForConstant()) == commandId) {
			editorComposite.raiseGetError(waitingForConstant(), message);
		}
	}

	@Override
	public void statusChanged(AnalysisCalculator analysisNetworkCalculator) {
		if (analysisNetworkCalculator.isNoLongerValid()) {
			startComputationIfPossible();
		}

		renderStatus();
	}
}
