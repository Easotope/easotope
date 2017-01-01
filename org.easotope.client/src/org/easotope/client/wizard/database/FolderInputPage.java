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

package org.easotope.client.wizard.database;

import org.easotope.client.Messages;
import org.easotope.client.core.adaptors.LoggingKeyAdaptor;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.framework.core.util.Platform;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FolderInputPage extends WizardPage {
	private Text path;

	public FolderInputPage() {
		super(Messages.wizard_config_folderInput_title);
	}

	public void createControl(Composite parent) {
		setTitle(Messages.wizard_config_folderInput_title);

		Composite composite = new Composite(parent, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		composite.setLayout(layout);

		Label explainLabel = new Label(composite, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(0, 500);
		explainLabel.setLayoutData(formData);
		explainLabel.setText(Messages.wizard_config_folderInput_explain);

		Composite gridComposite = new Composite(composite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(explainLabel, 15);
		formData.left = new FormAttachment(0, 20);
		formData.right = new FormAttachment(0, 490);
		gridComposite.setLayoutData(formData);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridComposite.setLayout(gridLayout);

		Label pathLabel = new Label(gridComposite, SWT.NONE);
		pathLabel.setText(Messages.wizard_config_folderInput_path);
		
		path = new Text(gridComposite, SWT.BORDER);
		path.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		path.addKeyListener(new LoggingKeyAdaptor() {
			public void loggingKeyPressed(KeyEvent e) {
			}

			public void loggingKeyReleased(KeyEvent e) {
				setPageComplete(!getPath().isEmpty());
			}
		});
		
		Button searchButton = new Button(gridComposite, SWT.PUSH);
		searchButton.setText(Messages.wizard_config_folderInput_searchButton);
		searchButton.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
			    DirectoryDialog dirDialog = new DirectoryDialog(getShell());
			    dirDialog.setText(Messages.wizard_config_folderInput_selectFolder);
			    String folderPath = dirDialog.open();
			    
			    if (folderPath != null && !folderPath.trim().isEmpty()) {
			    		FolderInputPage.this.path.setText(folderPath.trim());
			    		setPageComplete(true);
			    }
			}
		});
		
		Label emptyLabel = new Label(gridComposite, SWT.NONE);
		emptyLabel.setVisible(false);

		Label urlExampleLabel = new Label(gridComposite, SWT.NONE);
		
		if (Platform.isWindows()) {
			urlExampleLabel.setText(Messages.wizard_config_folderInput_pathWindowsExample);
		} else {
			urlExampleLabel.setText(Messages.wizard_config_folderInput_pathOtherExample);
		}
		
		gridComposite.setTabList(new Control[] { path, searchButton });
		
		setControl(composite);
		setPageComplete(false);
	}
	
	public void setVisible(boolean visible) {
		if (visible) {
			DatabaseWizard wizard = (DatabaseWizard) getWizard();
			
			if (wizard.processor != null) {
				wizard.processor.requestStop();
				wizard.processor = null;
			}
		}
		
		super.setVisible(visible);
	}
	
	public IWizardPage getNextPage() {
		return ((DatabaseWizard) getWizard()).folderTestPage;
	}
	
	public String getPath() {
		return path.getText().trim();
	}
}
