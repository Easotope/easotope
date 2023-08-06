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

package org.easotope.client.analysis.part.analysis.replicate;

import javax.annotation.PostConstruct;

import org.easotope.client.analysis.part.analysis.replicate.correctioninterval.CorrIntervalComposite;
import org.easotope.client.analysis.part.analysis.replicate.errors.ErrorComposite;
import org.easotope.client.analysis.part.analysis.replicate.repsteps.RepStepComposite;
import org.easotope.client.analysis.part.analysis.replicate.repstepselector.RepStepSelectorComposite;
import org.easotope.client.analysis.part.analysis.replicate.timeselector.TimeSelector;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

public class ReplicateAnalysisPart extends ChainedPart {
	public static final String SELECTION_CORR_INTERVAL_ID = "SELECTION_CORR_INTERVAL_ID";
	public static final String SELECTION_REPLICATE_ID = "SELECTION_REPLICATE_ID";
	public static final String SELECTION_REPLICATE_IS_A_STANDARD = "SELECTION_REPLICATE_IS_A_STANDARD";
	public static final String SELECTION_DATA_ANALYSIS_ID = "SELECTION_DATA_ANALYSIS_ID";
	public static final String SELECTION_REPSTEP_POSITION = "SELECTION_REPSTEP_POSITION"; 

	private CorrIntervalComposite corrIntervalComposite;
	private ErrorComposite errorComposite;
	private RepStepSelectorComposite repStepSelectorComposite;
	private RepStepComposite repStepComposite;
	private TimeSelector timeSelectorComposite;

	@PostConstruct
	public void postConstruct() {
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		getParent().setLayout(formLayout);

		corrIntervalComposite = new CorrIntervalComposite(this, getParent(), SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(30);
		corrIntervalComposite.setLayoutData(formData);

		Composite composite = new Composite(getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(corrIntervalComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(30);
		formData.bottom = new FormAttachment(80);
		composite.setLayoutData(formData);
		composite.setLayout(new FormLayout());

		repStepSelectorComposite = new RepStepSelectorComposite(this, composite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(50);
		repStepSelectorComposite.setLayoutData(formData);

		errorComposite = new ErrorComposite(this, composite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(repStepSelectorComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		errorComposite.setLayoutData(formData);

		repStepComposite = new RepStepComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(corrIntervalComposite);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(80);
		repStepComposite.setLayoutData(formData);

		timeSelectorComposite = new TimeSelector(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(composite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		timeSelectorComposite.setLayoutData(formData);

		timeSelectorComposite.addChild(corrIntervalComposite);
		corrIntervalComposite.addChild(repStepSelectorComposite);
		repStepSelectorComposite.addChild(repStepComposite);
	}

	@Override
	protected boolean closeOnSave() {
		return false;
	}
}
