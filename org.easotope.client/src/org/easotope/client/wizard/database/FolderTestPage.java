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

package org.easotope.client.wizard.database;

import java.util.Timer;
import java.util.TimerTask;

import org.easotope.client.Messages;
import org.easotope.framework.commands.Command;
import org.easotope.framework.commands.VersionGet;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.cmdprocessors.CommandListener;
import org.easotope.framework.dbcore.cmdprocessors.FolderProcessor;
import org.easotope.shared.core.PotentialGraphicsMethodsShared;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;

public class FolderTestPage extends WizardPage implements CommandListener {
    private volatile boolean backButtonEnabled = false;
    private volatile int timersLeft;
    private volatile Timer timer;
    private volatile VersionGet versionGet;
    private Composite stackComposite;
    private StackLayout stackLayout;
    private Composite waitComposite;
    private Composite errorComposite;
    private Composite doneComposite;

	public FolderTestPage() {
		super(Messages.wizard_config_folderTest_title);
	}

	public void createControl(Composite parent) {
		setTitle(Messages.wizard_config_folderTest_title);

		stackComposite = new Composite(parent, SWT.NONE);
        stackLayout = new StackLayout();
        stackComposite.setLayout(stackLayout);

		waitComposite = new Composite(stackComposite, SWT.NONE);
		FormLayout layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		waitComposite.setLayout(layout);

		Label waitLabel = new Label(waitComposite, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(0, 500);
		waitLabel.setLayoutData(formData);
		waitLabel.setText(Messages.wizard_config_folderTest_wait);
		
		ProgressBar progressBar = new ProgressBar(waitComposite, SWT.HORIZONTAL | SWT.INDETERMINATE);
		formData = new FormData();
		formData.top = new FormAttachment(waitLabel, 15);
		formData.left = new FormAttachment(0, 20);
		progressBar.setLayoutData(formData);
		
		errorComposite = new Composite(stackComposite, SWT.NONE);
		layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		errorComposite.setLayout(layout);

		Label errorLabel = new Label(errorComposite, SWT.WRAP);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(0, 500);
		errorLabel.setLayoutData(formData);
		errorLabel.setText(Messages.wizard_config_folderTest_error);

		doneComposite = new Composite(stackComposite, SWT.NONE);
		layout = new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		doneComposite.setLayout(layout);

		Label doneLabel = new Label(doneComposite, SWT.WRAP);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(0, 500);
		doneLabel.setLayoutData(formData);
		doneLabel.setText(Messages.wizard_config_folderTest_done);
		
        stackLayout.topControl = waitComposite;
        stackComposite.layout();

		setControl(stackComposite);
		setPageComplete(false);
	}

	public void setVisible(boolean visible) {
		cancelPendingEvents();

		if (visible) {
			versionGet = null;

			setBackButtonEnabled(false);
			setPageComplete(false);
			
	        stackLayout.topControl = waitComposite;
	        stackComposite.layout();

			timer = new Timer();

			final int minDelayInMillis = 3000;
			final int oneSecondInMillis = 1000;
			timersLeft = 3;

			for (int i=0; i<=timersLeft; i++) {
				timer.schedule(new AlarmTask(), minDelayInMillis + oneSecondInMillis * i);
			}

			DatabaseWizard wizard = (DatabaseWizard) getWizard();
			String path = wizard.folderInputPage.getPath();
			wizard.processor = new FolderProcessor(path, false, false, false);
			wizard.processor.process(new VersionGet(), null, this);
			new Thread(wizard.processor).start();
		}

		super.setVisible(visible);
	}

	public void cancelPendingEvents() {
        if (timer != null) {
        		timer.cancel();
        		timer = null;
        }
	}

	public void setBackButtonEnabled(boolean enabled) {
        backButtonEnabled = enabled;
        getContainer().updateButtons();
    }
    
    public IWizardPage getPreviousPage() {
        if (!backButtonEnabled) {
            return null;
        }
        return super.getPreviousPage();
    }
 
	class AlarmTask extends TimerTask {
		public void run() {
			Display.getDefault().asyncExec(new Runnable() {
		    		public void run() {
		    			try {
			    			timersLeft--;
	
			    			if (versionGet == null && timersLeft != 0) {
			    				return;
			    			}

			    			cancelPendingEvents();

			    			if (versionGet == null || !versionGet.isServerInitialized()) {
			    				stackLayout.topControl = errorComposite;
	
			    				DatabaseWizard wizard = (DatabaseWizard) getWizard();
	
			    				if (wizard.processor != null) {
			    					wizard.processor.requestStop();
			    					wizard.processor = null;
			    				}
			    				
			    				setPageComplete(true);
	
			    			} else {
			    				stackLayout.topControl = doneComposite;
			    			}
	
			    	        stackComposite.layout();
			    	        
			    			setBackButtonEnabled(true);

					} catch (Exception e) {
						Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
						PotentialGraphicsMethodsShared.reportErrorToUser(Display.getDefault(), e);
					}
		    		}
		    	});
		}
	}

	public boolean canFinish() {
		return stackLayout.topControl == doneComposite;
	}
	
	public IWizardPage getNextPage() {
		return ((DatabaseWizard) getWizard()).genericTestPage;
	}

	@Override
	public void commandExecuted(Command getDbVersion) {
		this.versionGet = (VersionGet) getDbVersion;  
	}
}
