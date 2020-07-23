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

package org.easotope.client.rawdata.scan;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.shared.core.FileEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

public class ScanPart extends ChainedPart implements FileEditor {
	public static final String ELEMENTID_BASE = "org.easotope.rawdata.gui.scan";

	public static final String INPUTURI_PARAM_MASS_SPEC = "massSpec";
	public static final String INPUTURI_PARAM_SCAN = "scan";

	public static final String SELECTION_INITIAL_MASS_SPEC_ID = "SELECTION_INITIAL_MASS_SPEC_ID";
	public static final String SELECTION_INITIAL_SCAN_ID = "SELECTION_INITIAL_SCAN_ID";

	private ScanComposite scanComposite;

	@PostConstruct
	public void postConstruct() {
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		getParent().setLayout(formLayout);

		scanComposite = new ScanComposite(this, getParent(), SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		scanComposite.setLayoutData(formData);

		Map<String,String> persistedState = getPart().getPersistedState();

		if (persistedState != null) {
			HashMap<String,Object> selection = new HashMap<String,Object>();

			if (persistedState.containsKey(INPUTURI_PARAM_MASS_SPEC)) {
				try {
					int massSpec = Integer.parseInt(persistedState.get(INPUTURI_PARAM_MASS_SPEC));
					selection.put(SELECTION_INITIAL_MASS_SPEC_ID, massSpec);
				} catch (NumberFormatException e) {
					Log.getInstance().log(Level.INFO, this, "could not parse mass spec id " + persistedState.get(INPUTURI_PARAM_MASS_SPEC), e);
				}
			}

			if (persistedState.containsKey(INPUTURI_PARAM_SCAN)) {
				try {
					int scan = Integer.parseInt(persistedState.get(INPUTURI_PARAM_SCAN));
					selection.put(SELECTION_INITIAL_SCAN_ID, scan);
				} catch (NumberFormatException e) {
					Log.getInstance().log(Level.INFO, this, "could not parse scan id " + persistedState.get(INPUTURI_PARAM_SCAN), e);
				}
			}

			setSelection(-1, selection);

			if (!selection.containsKey(SELECTION_INITIAL_SCAN_ID)) {
				scanComposite.receiveAddRequest();
			}
		}
	}

	@Override
	protected boolean closeOnSave() {
		return true;
	}

	@Override
	public boolean canAcceptFiles() {
		return scanComposite.canAcceptFiles();
	}

	@Override
	public void addFiles(String[] newFilenames) {
		scanComposite.addFiles(newFilenames);
	}
}