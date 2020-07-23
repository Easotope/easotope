/*
 * Copyright © 2016-2020 by Devon Bowen.
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

import java.util.ArrayList;
import java.util.List;

import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.rawdata.replicate.widget.acquisition.AcquisitionsWidget.ButtonType;
import org.easotope.shared.analysis.execute.calculator.AnalysisConstants;
import org.easotope.shared.analysis.tables.RepAnalysis;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.RawDataHelper;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;


public class ResultsComposite extends EasotopeComposite implements CorrIntervalLoaderListener {
	private EditorComposite editorComposite;
	private TabFolder folder;
	private CorrIntervalLoader corrIntervalLoader;
	private ScratchPad<ReplicatePad> replicateScratchPad;

	public ResultsComposite(EasotopePart easotopePart, EditorComposite editorComposite, Composite parent, int style) {
		super(easotopePart, parent, style);
		setLayout(new FillLayout());

		setVisible(false);

		corrIntervalLoader = new CorrIntervalLoader(editorComposite);
		corrIntervalLoader.addListener(this);

		this.editorComposite = editorComposite;
		folder = new TabFolder(this, SWT.NONE);
	}

	@Override
	protected void handleDispose() {
		disable();
		corrIntervalLoader.dispose();
	}

	private void disable() {
		for (TabItem tabItems : folder.getItems()) {
			ResultsCompositeTab resultsCompositeTab = (ResultsCompositeTab) tabItems.getControl();
			resultsCompositeTab.stopCurrentActivity();
		}

		corrIntervalLoader.disable();

		if (replicateScratchPad != null) {
			replicateScratchPad.removeAllChildren();
		}
	}

	public void newReplicate(ReplicateV1 replicate, ArrayList<Acquisition> acquisitions) {
		disable();

		if (replicate == null || replicate.getChannelToMzX10() == null || acquisitions == null || acquisitions.size() == 0) {
			setVisible(false);
			return;
		}

		replicateScratchPad = new ScratchPad<ReplicatePad>();
		RawDataHelper.addReplicateToScratchPad(replicateScratchPad, replicate, acquisitions);
		replicateScratchPad.getChild(0).setVolatileData(AnalysisConstants.VOLATILE_DATA_REPLICATE, new ReplicateV1(replicate));

		corrIntervalLoader.newValues(replicate.getMassSpecId(), replicate.getDate());

		ScratchPad<ReplicatePad> copyOfReplicateScratchPad = new ScratchPad<ReplicatePad>(replicateScratchPad);

		for (TabItem tabItems : folder.getItems()) {
			ResultsCompositeTab resultsCompositeTab = (ResultsCompositeTab) tabItems.getControl();
			resultsCompositeTab.newReplicate(copyOfReplicateScratchPad);
		}
	}

	public void modifyButton(ButtonType buttonType, long acquisitionTimeStamp, int cycleNumber, boolean selected) {
		if (replicateScratchPad == null || replicateScratchPad.getChildren().size() == 0) {
			return;
		}

		ReplicatePad replicatePad = replicateScratchPad.getChild(0);

		switch (buttonType) {
			case DisableAcquisition:
				for (AcquisitionPad acquisitionPad : replicatePad.getChildren()) {
					if (acquisitionPad.getDate() == acquisitionTimeStamp) {
						acquisitionPad.setValue(AcquisitionPad.DISABLED, selected);
					}
				}

			case DisableCycle:
			case OffPeakCycle:
				for (AcquisitionPad acquisitionPad : replicatePad.getChildren()) {
					if (acquisitionPad.getDate() == acquisitionTimeStamp) {
						for (CyclePad cyclePad : acquisitionPad.getChildren()) {
							if (cyclePad.getNumber() == cycleNumber+1) {
								cyclePad.setValue(buttonType == ButtonType.DisableCycle ? CyclePad.DISABLED : CyclePad.OFF_PEAK, selected);
							}
						}
					}
				}
		}

		ScratchPad<ReplicatePad> copyOfReplicateScratchPad = new ScratchPad<ReplicatePad>(replicateScratchPad);

		for (TabItem tabItems : folder.getItems()) {
			ResultsCompositeTab resultsCompositeTab = (ResultsCompositeTab) tabItems.getControl();
			resultsCompositeTab.newReplicate(copyOfReplicateScratchPad);
		}
	}

	@Override
	public void corrIntervalInfoChanged() {
		RepAnalysis[] repAnalysisArray = corrIntervalLoader.getRepAnalysis();

		if (repAnalysisArray != null) {
			while (repAnalysisArray.length > folder.getItemCount()) {
				TabItem item = new TabItem(folder, SWT.NONE);
				ResultsCompositeTab resultsCompositeTab = new ResultsCompositeTab(getEasotopePart(), editorComposite, folder, SWT.NONE);
				item.setControl(resultsCompositeTab);
			}

			while (repAnalysisArray.length < folder.getItemCount()) {
				TabItem item = folder.getItem(0);
				((ResultsCompositeTab) item.getControl()).stopCurrentActivity();
				item.getControl().dispose();
				item.dispose();
			}

			ScratchPad<ReplicatePad> copyOfReplicateScratchPad = new ScratchPad<ReplicatePad>(replicateScratchPad);

			int folderIndex = 0;
			for (RepAnalysis repAnalysis : repAnalysisArray) {
				TabItem tabItem = folder.getItem(folderIndex);
				tabItem.setText(repAnalysis.getName());

				int corrIntervalId = corrIntervalLoader.getCorrIntervalId();
				List<RepStep> steps = corrIntervalLoader.getRepAnalysisStepsById(repAnalysis.getId());

				ResultsCompositeTab resultsCompositeTab = (ResultsCompositeTab) tabItem.getControl();
				resultsCompositeTab.setRepAnalysis(corrIntervalId, repAnalysis, steps);
				resultsCompositeTab.newReplicate(copyOfReplicateScratchPad);
				
				folderIndex++;
			}
		}

		setVisible(repAnalysisArray != null);
	}
}
