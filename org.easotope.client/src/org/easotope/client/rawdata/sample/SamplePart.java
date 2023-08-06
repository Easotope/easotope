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

package org.easotope.client.rawdata.sample;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

public class SamplePart extends ChainedPart {
	public static final String ELEMENTID_BASE = "org.easotope.rawdata.gui.sample";
	public static final String INPUTURI_PARAM_USER = "user";
	public static final String INPUTURI_PARAM_PROJECT = "project";
	public static final String INPUTURI_PARAM_SAMPLE = "sample";

	public static final String SELECTION_USER_ID = "SELECTION_USER_ID";
	public static final String SELECTION_PROJECT_ID = "SELECTION_PROJECT_ID";
	public static final String SELECTION_SAMPLE_ID = "SELECTION_SAMPLE_ID";

	private SampleComposite sampleComposite;

	@PostConstruct
	public void postConstruct() {
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		getParent().setLayout(formLayout);

		sampleComposite = new SampleComposite(this, getParent(), SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		sampleComposite.setLayoutData(formData);

		Map<String,String> persistedState = getPart().getPersistedState();

		if (persistedState != null) {
			HashMap<String,Object> selection = new HashMap<String,Object>();

			if (persistedState.containsKey(INPUTURI_PARAM_USER)) {
				try {
					int user = Integer.parseInt(persistedState.get(INPUTURI_PARAM_USER));
					selection.put(SELECTION_USER_ID, user);
				} catch (NumberFormatException e) {
					Log.getInstance().log(Level.INFO, this, "could not parse user id " + persistedState.get(INPUTURI_PARAM_USER), e);
				}
			}

			if (persistedState.containsKey(INPUTURI_PARAM_PROJECT)) {
				try {
					int project = Integer.parseInt(persistedState.get(INPUTURI_PARAM_PROJECT));
					selection.put(SELECTION_PROJECT_ID, project);
				} catch (NumberFormatException e) {
					Log.getInstance().log(Level.INFO, this, "could not parse project id " + persistedState.get(INPUTURI_PARAM_PROJECT), e);
				}
			}

			if (persistedState.containsKey(INPUTURI_PARAM_SAMPLE)) {
				try {
					int sample = Integer.parseInt(persistedState.get(INPUTURI_PARAM_SAMPLE));
					selection.put(SELECTION_SAMPLE_ID, sample);
				} catch (NumberFormatException e) {
					Log.getInstance().log(Level.INFO, this, "could not parse sample id " + persistedState.get(INPUTURI_PARAM_SAMPLE), e);
				}
			}

			setSelection(-1, selection);

			if (!selection.containsKey(SELECTION_SAMPLE_ID)) {
				sampleComposite.receiveAddRequest();
			}
		}
	}

	@Override
	protected boolean closeOnSave() {
		return true;
	}
}