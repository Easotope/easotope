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

package org.easotope.client.analysis.part.export;

import java.util.TreeSet;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.sampleselector.UserProjSampleSelection;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.scratchpadtable.ScratchPadTable;
import org.easotope.client.core.scratchpadtable.ScratchPadTableListener;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.analysis.cache.calculated.CalculatedCache;
import org.easotope.shared.analysis.cache.calculated.export.CalculatedCacheCalculatedExportGetListener;
import org.easotope.shared.analysis.cache.calculated.export.CalculatedExport;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.core.tables.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ExportTableComposite extends ChainedComposite implements CalculatedCacheCalculatedExportGetListener {
	private final String TABLE_LAYOUT_EXPORT = "Export Results";
	private final String TABLE_LAYOUT_CORR_INTERVALS = "Corr Interval Results";

	private ScratchPadTable requestedScratchPadTable;
	private ScratchPadTable corrIntervalReplicatesTable;

	private boolean currentlyFinished = false;
	private int waitingOnCommandId = Command.UNDEFINED_ID;

	protected ExportTableComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Label label = new Label(this, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		label.setLayoutData(formData);
		label.setText(Messages.exportTableComposite_instructions);

		Composite composite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		composite.setLayoutData(formData);
		composite.setLayout(new FormLayout());

		requestedScratchPadTable = new ScratchPadTable(composite, false);
		formData = new FormData();
		formData.top = new FormAttachment(0, 5);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(50);
		requestedScratchPadTable.setLayoutData(formData);
		requestedScratchPadTable.addListener(new ScratchPadTableListener() {
			@Override
			public void tableLayoutNeedsSaving(TableLayout tableLayout) {
				tableLayout.setUserId(LoginInfoCache.getInstance().getUser().getId());
				tableLayout.setContext(TABLE_LAYOUT_EXPORT);
				tableLayout.setDataAnalysisId(DatabaseConstants.EMPTY_DB_ID);

				LoginInfoCache.getInstance().saveTableLayout(tableLayout);
			}

			@Override
			public void tableModified() {
				// ignore
			}
		});

		corrIntervalReplicatesTable = new ScratchPadTable(composite, false);
		formData = new FormData();
		formData.top = new FormAttachment(requestedScratchPadTable, 5);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		corrIntervalReplicatesTable.setLayoutData(formData);
		corrIntervalReplicatesTable.addListener(new ScratchPadTableListener() {
			@Override
			public void tableLayoutNeedsSaving(TableLayout tableLayout) {
				tableLayout.setUserId(LoginInfoCache.getInstance().getUser().getId());
				tableLayout.setContext(TABLE_LAYOUT_CORR_INTERVALS);
				tableLayout.setDataAnalysisId(DatabaseConstants.EMPTY_DB_ID);

				LoginInfoCache.getInstance().saveTableLayout(tableLayout);
			}

			@Override
			public void tableModified() {
				
			}
		});

		setVisible(false);
	}

	@Override
	public boolean isWaiting() {
		return waitingOnCommandId != Command.UNDEFINED_ID;
	}

	@Override
	protected void setWidgetsEnabled() {
		// ignore
	}

	@Override
	protected void setWidgetsDisabled() {
		// ignore
	}

	@Override
	protected void receiveAddRequest() {
		// ignore
	}

	@Override
	protected void cancelAddRequest() {
		// ignore
	}

	@Override
	protected void receiveSelection() {
		Boolean finished = (Boolean) getChainedPart().getSelection().get(ExportPart.SELECTION_FINISHED);

		if (!currentlyFinished && finished) {
			UserProjSampleSelection newUserProjSampleSelection = (UserProjSampleSelection) getChainedPart().getSelection().get(ExportPart.SELECTION_SAMPLE_IDS);

			TreeSet<Integer> userIds = newUserProjSampleSelection.getUserIds();
			TreeSet<Integer> projectIds = newUserProjSampleSelection.getProjectIds();
			TreeSet<Integer> sampleIds = newUserProjSampleSelection.getSampleIds();

			waitingOnCommandId = Command.UNDEFINED_ID;
			waitingOnCommandId = CalculatedCache.getInstance().calculatedExportGet(userIds, projectIds, sampleIds, this);
			getChainedPart().setCursor();

			if (waitingOnCommandId != Command.UNDEFINED_ID) {
				setVisible(false);
			}
		}

		currentlyFinished = finished;
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	@Override
	public void calculatedExportGetCompleted(int commandId, CalculatedExport calculatedExport) {
		if (waitingOnCommandId == commandId) {
			ScratchPad<?> requestedScratchPad = calculatedExport.getRequestedScratchPad();
			ColumnOrdering requestedColumnOrdering = calculatedExport.getRequestedColumnOrdering();
			FormatLookup requestedFormatLookup = calculatedExport.getRequestedFormatLookup();
			TableLayout requestedTableLayout = LoginInfoCache.getInstance().getTableLayout(DatabaseConstants.EMPTY_DB_ID, TABLE_LAYOUT_EXPORT);
			requestedScratchPadTable.setScratchPad(requestedScratchPad, requestedColumnOrdering, requestedFormatLookup, requestedTableLayout);

			ScratchPad<ReplicatePad> corrIntervalReplicates = calculatedExport.getCorrIntervalReplicates();
			ColumnOrdering corrIntervalColumnOrdering = calculatedExport.getCorrIntervalColumnOrdering();
			FormatLookup corrIntervalFormatLookup = calculatedExport.getCorrIntervalFormatLookup();
			TableLayout corrIntervalTableLayout = LoginInfoCache.getInstance().getTableLayout(DatabaseConstants.EMPTY_DB_ID, TABLE_LAYOUT_CORR_INTERVALS);
			corrIntervalReplicatesTable.setScratchPad(corrIntervalReplicates, corrIntervalColumnOrdering, corrIntervalFormatLookup, corrIntervalTableLayout);

			setVisible(true);

			waitingOnCommandId = Command.UNDEFINED_ID;
			getChainedPart().setCursor();
		}
	}

	@Override
	public void calculatedExportGetError(int commandId, String message) {
		if (waitingOnCommandId == commandId) {
			setVisible(false);

			waitingOnCommandId = Command.UNDEFINED_ID;
			getChainedPart().setCursor();

			getChainedPart().raiseError(message);
		}
	}
}
