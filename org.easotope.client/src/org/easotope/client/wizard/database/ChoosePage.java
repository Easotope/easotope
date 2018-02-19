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

package org.easotope.client.wizard.database;

import org.easotope.client.Messages;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager.ProcessorTypes;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public class ChoosePage extends WizardPage {
	private Button serverButton;
	private Button folderButton;

	public ChoosePage() {
		super(Messages.wizard_config_choose_title);
	}

	public void createControl(Composite parent) {
		DatabaseWizard databaseWizard = (DatabaseWizard) getWizard();
		setTitle(databaseWizard.isFirstTime ? Messages.wizard_config_choose_firstTimeTitle : Messages.wizard_config_choose_title);

		Composite composite = new Composite(parent, SWT.NO_FOCUS);
		FormLayout layout= new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		composite.setLayout(layout);

		Label explainLabel = null;
		FormData formData;
		
		if (databaseWizard.isFirstTime) {
			explainLabel = new Label(composite, SWT.WRAP);
			formData = new FormData();
			formData.top = new FormAttachment(0);
			formData.left = new FormAttachment(0);
			formData.right = new FormAttachment(0, 500);
			explainLabel.setLayoutData(formData);
			explainLabel.setText(Messages.wizard_config_choose_explain);
		}

		Label questionLabel = new Label(composite, SWT.WRAP);
		formData = new FormData();
		formData.top = databaseWizard.isFirstTime ? new FormAttachment(explainLabel, 15) : new FormAttachment(0);
		formData.left = new FormAttachment(0);
		questionLabel.setLayoutData(formData);
		questionLabel.setText(Messages.wizard_config_choose_question);

		serverButton = new Button(composite, SWT.RADIO);
		formData = new FormData();
		formData.top = new FormAttachment(questionLabel, 15);
		formData.left = new FormAttachment(0, 20);
		serverButton.setLayoutData(formData);
		serverButton.setText(Messages.wizard_config_choose_server);
		serverButton.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				setPageComplete(true);
			}
		});

		folderButton = new Button(composite, SWT.RADIO);
		formData = new FormData();
		formData.top = new FormAttachment(serverButton, 0);
		formData.left = new FormAttachment(serverButton, 0, SWT.LEFT);
		folderButton.setLayoutData(formData);
		folderButton.setText(Messages.wizard_config_choose_filesystem);
		folderButton.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				setPageComplete(true);
			}
		});
		
		composite.setTabList(new Control[] { serverButton, folderButton }); 

		setControl(composite);
		setPageComplete(false);
	}

	public IWizardPage getNextPage() {
		DatabaseWizard databaseWizard = (DatabaseWizard) getWizard();
		
		switch (getSelectedConfigType()) {
			case SERVER: 
				return databaseWizard.serverInputPage;

			case FOLDER:
				return databaseWizard.folderInputPage;
		}

		return null;
	}

	public ProcessorTypes getSelectedConfigType() {
		if (serverButton.getSelection()) {
			return ProcessorManager.ProcessorTypes.SERVER;
		}
		
		if (folderButton.getSelection()) {
			return ProcessorManager.ProcessorTypes.FOLDER;
		}

		return null;
	}
}
