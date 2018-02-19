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

package org.easotope.client.rawdata.replicate.widget.rawresults;

import java.util.HashMap;
import java.util.List;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.scratchpadtable.ScratchPadTable;
import org.easotope.client.core.scratchpadtable.ScratchPadTableListener;
import org.easotope.shared.analysis.execute.AnalysisCompiled;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.AnalysisIdentifier.Level;
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

public class ScratchPadTableComposite extends Composite {
	private ScratchPadTable scratchPadTable;
	private FormData scratchPadTableFormData;
	private Label standardsErrorMessage;

	ScratchPadTableComposite(final ResultsCompositeTab parent, int style) {
		super(parent, style);
		FormLayout formLayout = new FormLayout();
		formLayout.marginHeight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginWidth = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		scratchPadTable = new ScratchPadTable(this, false);
		scratchPadTableFormData = new FormData();
		scratchPadTableFormData.top = new FormAttachment(0);
		scratchPadTableFormData.left = new FormAttachment(0);
		scratchPadTableFormData.right = new FormAttachment(100);
		scratchPadTableFormData.bottom = new FormAttachment(100);
		scratchPadTable.setLayoutData(scratchPadTableFormData);
		scratchPadTable.addListener(new ScratchPadTableListener() {
			@Override
			public void tableLayoutNeedsSaving(TableLayout tableLayout) {
				parent.saveTableLayout(tableLayout);
			}

			@Override
			public void tableModified() {
				// ignore
			}
		});

		standardsErrorMessage = new Label(this, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		standardsErrorMessage.setLayoutData(formData);
		standardsErrorMessage.setText(Messages.resultsCompositeTab_standardsErrorMessage);
		standardsErrorMessage.setForeground(ColorCache.getColor(getDisplay(), ColorCache.YELLOW));
		standardsErrorMessage.setVisible(false);
	}

	private void showStandardsErrorMessage(boolean show) {
		if (show == standardsErrorMessage.getVisible()) {
			return;
		}

		if (show) {
			scratchPadTableFormData.top = new FormAttachment(standardsErrorMessage, 5);
			standardsErrorMessage.setVisible(true);
		} else {
			scratchPadTableFormData.top = new FormAttachment(0);
			standardsErrorMessage.setVisible(false);
		}

		layout();
	}

	void setScratchPad(AnalysisCompiled dataAnalysisCompiled, ScratchPad<ReplicatePad> scratchPad, boolean showStandardsErrorMessage) {
		showStandardsErrorMessage(showStandardsErrorMessage);

		List<String> generatedOutputColumns = dataAnalysisCompiled.getGeneratedOutputColumns();
		ColumnOrdering columnOrdering = new ColumnOrdering();
		columnOrdering.add(generatedOutputColumns);

		HashMap<String,String> outputColumnToFormat = dataAnalysisCompiled.getOutputColumnToFormat();
		FormatLookup formatLookup = new FormatLookup();
		formatLookup.add(Level.REPLICATE, dataAnalysisCompiled.getAnalysis().getName(), outputColumnToFormat);

		TableLayout tableLayout = LoginInfoCache.getInstance().getTableLayout(dataAnalysisCompiled.getAnalysis().getId(), ((ResultsCompositeTab) getParent()).TABLE_LAYOUT_CONTEXT);
		scratchPadTable.setScratchPad(scratchPad, columnOrdering, formatLookup, tableLayout);
	}

	void updateScratchPad(ScratchPad<ReplicatePad> tmpScratchPad, boolean showStandardsErrorMessage) {
		showStandardsErrorMessage(showStandardsErrorMessage);
		scratchPadTable.updateScratchPad(tmpScratchPad);		
	}
}
