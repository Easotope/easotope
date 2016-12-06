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

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.sampleselector.UserProjSampleSelection;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class LoadButtonComposite extends ChainedComposite {
	private Button loadButton;
	private UserProjSampleSelection lastUserProjSampleSelection = new UserProjSampleSelection();

	protected LoadButtonComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);
		setLayout(new FillLayout());

		loadButton = new Button(this, SWT.NONE);
		loadButton.setText(Messages.loadButtonComposite_loadButton);
		loadButton.setEnabled(false);
		loadButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				propogateSelection(ExportPart.SELECTION_FINISHED, true);
				loadButton.setEnabled(false);
			}
		});
	}

	@Override
	public boolean isWaiting() {
		return false;
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
		UserProjSampleSelection newUserProjSampleSelection = (UserProjSampleSelection) getChainedPart().getSelection().get(ExportPart.SELECTION_SAMPLE_IDS);

		if (!lastUserProjSampleSelection.equals(newUserProjSampleSelection)) {
			loadButton.setEnabled(!newUserProjSampleSelection.isEmpty());
			propogateSelection(ExportPart.SELECTION_FINISHED, false);
			lastUserProjSampleSelection = newUserProjSampleSelection;
		}
	}
}
