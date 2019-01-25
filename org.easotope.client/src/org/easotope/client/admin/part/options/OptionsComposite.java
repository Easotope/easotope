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

package org.easotope.client.admin.part.options;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.VButton;
import org.easotope.client.core.widgets.VCombo;
import org.easotope.client.core.widgets.VSpinner;
import org.easotope.framework.dbcore.tables.Options;
import org.easotope.framework.dbcore.tables.Options.OverviewResolution;
import org.easotope.shared.admin.cache.options.OptionsCache;
import org.easotope.shared.admin.cache.options.options.OptionsCacheOptionsGetListener;
import org.easotope.shared.admin.cache.options.options.OptionsCacheOptionsSaveListener;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class OptionsComposite extends EditorComposite implements OptionsCacheOptionsGetListener, OptionsCacheOptionsSaveListener {
	private static final String OPTIONS_GET = "OPTIONS_GET";
	private static final String OPTIONS_SAVE = "OPTIONS_SAVE";

	private VCombo overviewRes;
	private VButton includeStds;
	private VSpinner confidenceLevel;

	protected OptionsComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label tableLabel = new Label(this, SWT.NONE);
		tableLabel.setText(Messages.optionsComposite_overviewResLabel);

		overviewRes = new VCombo(this, SWT.READ_ONLY);
		for (OverviewResolution overviewResolution : OverviewResolution.values()) {
			overviewRes.add(overviewResolution.getName());
		}
		overviewRes.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		Label incStdsLabel = new Label(this, SWT.NONE);
		incStdsLabel.setText(Messages.optionsComposite_includeStds);

		includeStds = new VButton(this, SWT.CHECK);
		includeStds.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		Label confLevelLabel = new Label(this, SWT.NONE);
		confLevelLabel.setText(Messages.optionsComposite_confidenceLevel);

		confidenceLevel = new VSpinner(this, SWT.BORDER);
		confidenceLevel.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		OptionsCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		OptionsCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private Options getCurrentOptions() {
		return (Options) getCurrentObject();
	}

	@Override
	protected void setCurrentFieldValues() {
		Options currentConfig = getCurrentOptions();
		overviewRes.select(currentConfig.getOverviewResolution().ordinal());
		includeStds.setSelection(currentConfig.isIncludeStds());
		confidenceLevel.setSelection((int) Math.round(currentConfig.getConfidenceLevel()));
	}

	@Override
	protected void setDefaultFieldValues() {
		overviewRes.select(OverviewResolution.REPLICATE.ordinal());
		includeStds.setSelection(false);
		confidenceLevel.setSelection(90);
	}

	@Override
	public void enableWidgets() {
		boolean isAdmin = LoginInfoCache.getInstance().getUser().getIsAdmin();

		overviewRes.setEnabled(isAdmin);
		includeStds.setEnabled(isAdmin);
		confidenceLevel.setEnabled(isAdmin);

		if (!isAdmin) {
			overviewRes.revert();
			includeStds.revert();
			confidenceLevel.revert();
		}
	}

	@Override
	public void disableWidgets() {
		overviewRes.setEnabled(false);
		includeStds.setEnabled(false);
		confidenceLevel.setEnabled(false);
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || overviewRes.hasChanged();
		isDirty = isDirty || includeStds.hasChanged();
		isDirty = isDirty || confidenceLevel.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		return false;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		return true;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		int commandId = OptionsCache.getInstance().optionsGet(this);
		waitingFor(OPTIONS_GET, commandId);
		return true;
	}

	@Override
	protected void requestSave(boolean isResend) {
		Options oldConfig = getCurrentOptions();
		Options newConfig = new Options();

		if (oldConfig != null) {
			newConfig.setId(oldConfig.getId());
		}

		newConfig.setOverviewResolution(OverviewResolution.values()[overviewRes.getSelectionIndex()]);
		newConfig.setIncludeStds(includeStds.getSelection());
		newConfig.setConfidenceLevel(confidenceLevel.getSelection());

		int commandId = OptionsCache.getInstance().optionsSave(newConfig, this);
		waitingFor(OPTIONS_SAVE, commandId);
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
	public void optionsGetCompleted(int commandId, Options options) {
		newObject(OPTIONS_GET, options);
	}

	@Override
	public void optionsUpdated(int commandId, Options options) {
		updateObject(options, Messages.optionsComposite_optionsHasBeenUpdated);
	}

	@Override
	public void optionsGetError(int commandId, String message) {
		raiseGetError(OPTIONS_GET, message);		
	}
	
	@Override
	public void optionsSaveCompleted(int commandId, Options options) {
		saveComplete(OPTIONS_SAVE, options);
	}

	@Override
	public void optionsSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(OPTIONS_SAVE, message);
	}
}
