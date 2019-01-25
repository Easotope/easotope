/*
 * Copyright © 2016-2019 by Devon Bowen.
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

package org.easotope.client.analysis.part.analysis.sample.samsteps;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.SamStepGraphicComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class EmptySamStepGraphicComposite extends SamStepGraphicComposite {

	public EmptySamStepGraphicComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
		setLayout(new GridLayout());

        Label label = new Label(this, SWT.NONE);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        label.setLayoutData(gridData);
        label.setText(Messages.emptySamStepGraphics_message);
    }

	@Override
	protected void handleDispose() {

	}

	@Override
	public void newCalculation(ScratchPad<SamplePad> scratchPad) {
		// ignore
	}
}
