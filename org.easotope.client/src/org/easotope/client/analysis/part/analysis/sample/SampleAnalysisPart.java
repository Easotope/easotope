/*
 * Copyright © 2016-2017 by Devon Bowen.
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

package org.easotope.client.analysis.part.analysis.sample;

import javax.annotation.PostConstruct;

import org.easotope.client.analysis.part.analysis.sample.sampleanalysisselector.SamAnalysisSelectorComposite;
import org.easotope.client.analysis.part.analysis.sample.samsteps.SamStepComposite;
import org.easotope.client.analysis.part.analysis.sample.samstepselector.SamStepSelectorComposite;
import org.easotope.client.analysis.part.analysis.sample.table.TableComposite;
import org.easotope.client.analysis.part.analysis.sampleselector.SampleSelectorComposite;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.ChainedPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;

public class SampleAnalysisPart extends ChainedPart {
	public static final String SELECTION_USER_PROJ_SAMPLE_IDS = "SELECTION_USER_PROJ_SAMPLE_IDS";
	public static final String SELECTION_SAM_ANALYSIS_ID = "SELECTION_SAMPLE_ANALYSIS_ID";
	public static final String SELECTION_SAMSTEP_POSITION = "SELECTION_STEP_POSITION";

	private SampleSelectorComposite sampleSelectorComposite;
	private SamAnalysisSelectorComposite sampleAnalysisSelectorComposite;
	private SamStepSelectorComposite samStepSelectorComposite;
	private SamStepComposite samStepComposite;
	private TableComposite tableComposite;

	@PostConstruct
	public void postConstruct() {
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		getParent().setLayout(formLayout);

		sampleSelectorComposite = new SampleSelectorComposite(this, getParent(), SWT.NONE, true, SELECTION_USER_PROJ_SAMPLE_IDS);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(20);
		formData.bottom = new FormAttachment(60);
		sampleSelectorComposite.setLayoutData(formData);

		sampleAnalysisSelectorComposite = new SamAnalysisSelectorComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(sampleSelectorComposite);
		formData.right = new FormAttachment(40);
		formData.bottom = new FormAttachment(30);
		sampleAnalysisSelectorComposite.setLayoutData(formData);

		samStepSelectorComposite = new SamStepSelectorComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(sampleAnalysisSelectorComposite);
		formData.left = new FormAttachment(sampleSelectorComposite);
		formData.right = new FormAttachment(40);
		formData.bottom = new FormAttachment(60);
		samStepSelectorComposite.setLayoutData(formData);

		samStepComposite = new SamStepComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(sampleAnalysisSelectorComposite);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(60);
		samStepComposite.setLayoutData(formData);

		tableComposite = new TableComposite(this, getParent(), SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(sampleSelectorComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		tableComposite.setLayoutData(formData);

//		Composite composite = new Composite(getParent(), SWT.NONE);
//		formData = new FormData();
//		formData.top = new FormAttachment(corrIntervalComposite);
//		formData.left = new FormAttachment(0);
//		formData.right = new FormAttachment(30);
//		formData.bottom = new FormAttachment(80);
//		composite.setLayoutData(formData);
//		composite.setLayout(new FormLayout());

//		repStepSelectorComposite = new RepStepSelectorComposite(this, composite, SWT.NONE);
//		formData = new FormData();
//		formData.top = new FormAttachment(0);
//		formData.left = new FormAttachment(0);
//		formData.right = new FormAttachment(100);
//		formData.bottom = new FormAttachment(50);
//		repStepSelectorComposite.setLayoutData(formData);
//
//		errorComposite = new ErrorComposite(this, composite, SWT.NONE);
//		formData = new FormData();
//		formData.top = new FormAttachment(repStepSelectorComposite);
//		formData.left = new FormAttachment(0);
//		formData.right = new FormAttachment(100);
//		formData.bottom = new FormAttachment(100);
//		errorComposite.setLayoutData(formData);
//
//		repStepsComposite = new RepStepsComposite(this, getParent(), SWT.NONE);
//		formData = new FormData();
//		formData.top = new FormAttachment(0);
//		formData.left = new FormAttachment(corrIntervalComposite);
//		formData.right = new FormAttachment(100);
//		formData.bottom = new FormAttachment(80);
//		repStepsComposite.setLayoutData(formData);
//
//		timeSelectorComposite = new TimeSelector(this, getParent(), SWT.NONE);
//		formData = new FormData();
//		formData.top = new FormAttachment(composite);
//		formData.left = new FormAttachment(0);
//		formData.right = new FormAttachment(100);
//		formData.bottom = new FormAttachment(100);
//		timeSelectorComposite.setLayoutData(formData);
//
//		timeSelectorComposite.addChild(corrIntervalComposite);
//		corrIntervalComposite.addChild(repStepSelectorComposite);
//		repStepSelectorComposite.addChild(repStepsComposite);
	}

	@Override
	protected boolean closeOnSave() {
		return false;
	}
}
