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

package org.easotope.client.analysis.repstep.co2.clumpcalc;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepParamComposite;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.VButton;
import org.easotope.shared.analysis.repstep.co2.clumpcalc.Calculator;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ParameterComposite extends RepStepParamComposite {
	private VButton requireAverageRefValues;

	public ParameterComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label requireAverageRefValuesLabel = new Label(this, SWT.NONE);
		requireAverageRefValuesLabel.setText(Messages.co2IclPblParameterComposite_allowUnaveragedRefValues);

		requireAverageRefValues = new VButton(this, SWT.CHECK);
		requireAverageRefValues.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				widgetStatusChanged();
			}
		});
    }

	@Override
	protected HashMap<String,Object> buildNewParameters() {
		HashMap<String,Object> parameters = new HashMap<String,Object>();

		parameters.put(Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES, requireAverageRefValues.getSelection());

		return parameters;
	}

	@Override
	protected void setCurrentFieldValues() {
		Boolean value = (Boolean) getParameter(Calculator.PARAMETER_ALLOW_UNAVERAGED_REF_VALUES);
		value = (value == null) ? Calculator.DEFAULT_ALLOW_UNAVERAGED_REF_VALUES : value;

		requireAverageRefValues.setSelection(value);
	}

	@Override
	public void enableWidgets() {
		boolean hasAdminPermissions = LoginInfoCache.getInstance().getPermissions().isCanEditCorrIntervals();

		requireAverageRefValues.setEnabled(hasAdminPermissions);

		if (!hasAdminPermissions) {
			requireAverageRefValues.revert();
		}
	}

	@Override
	public void disableWidgets() {
		requireAverageRefValues.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || requireAverageRefValues.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		return false;
	}
}
