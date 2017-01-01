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

package org.easotope.client.rawdata.replicate.widget.rawresults;

import org.easotope.client.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ErrorComposite extends Composite {
	private Label errorStep;
	private Label errorMessage;

	ErrorComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new FormLayout());

		Label errorTitle = new Label(this, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(30);
		formData.left = new FormAttachment(20);
		formData.right = new FormAttachment(80);
		errorTitle.setLayoutData(formData);
		errorTitle.setText(Messages.resultsCompositeTab_errorTitle);
	
		Composite errorSubComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(errorTitle, 10);
		formData.left = new FormAttachment(25);
		formData.right = new FormAttachment(75);
		formData.bottom = new FormAttachment(100);
		errorSubComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		errorSubComposite.setLayout(gridLayout);
	
		Label stepLabel = new Label(errorSubComposite, SWT.NONE);
		stepLabel.setText(Messages.resultsCompositeTab_errorStepLabel);
	
		errorStep = new Label(errorSubComposite, SWT.WRAP);
	
		Label messageLabel = new Label(errorSubComposite, SWT.NONE);
		messageLabel.setText(Messages.resultsCompositeTab_errorMessageLabel);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		messageLabel.setLayoutData(gridData);
	
		errorMessage = new Label(errorSubComposite, SWT.WRAP);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		errorMessage.setLayoutData(gridData);
	}

	void setErrorMessage(String step, String message) {
		errorStep.setText(step);
		errorMessage.setText(message);
		errorMessage.getParent().layout();
	}
}
