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

package org.easotope.client.wizard.database;

import org.easotope.client.Messages;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingKeyAdaptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ServerInputPage extends WizardPage {
	private Text host;
	private Text port;

	public ServerInputPage() {
		super(Messages.wizard_config_serverInput_title);
	}

	public void createControl(Composite parent) {
		setTitle(Messages.wizard_config_serverInput_title);

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
		explainLabel.setText(Messages.wizard_config_serverInput_explain);

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

		Label hostLabel = new Label(gridComposite, SWT.NONE);
		hostLabel.setText(Messages.wizard_config_serverInput_hostname);
		
		host = new Text(gridComposite, SWT.BORDER);
		host.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		host.setTextLimit(255);
		host.addListener(SWT.Verify, new LoggingAdaptor() {
			public void loggingHandleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!Character.isLetterOrDigit(chars[i]) && chars[i] != '.' && chars[i] != '-') {
						e.doit = false;
						return;
					}
				}
			}
		});
		host.addKeyListener(new LoggingKeyAdaptor() {
			public void loggingKeyPressed(KeyEvent e) {
			}

			public void loggingKeyReleased(KeyEvent e) {
				setPageComplete(!getHost().isEmpty() && !getPort().isEmpty());
			}
		});
		
		Label hostExampleLabel = new Label(gridComposite, SWT.NONE);
		hostExampleLabel.setText(Messages.wizard_config_serverInput_hostExample);
		
		Label portLabel = new Label(gridComposite, SWT.NONE);
		portLabel.setText(Messages.wizard_config_serverInput_port);
		
		port = new Text(gridComposite, SWT.BORDER);
		port.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		port.setTextLimit(5);
		port.addListener(SWT.Verify, new LoggingAdaptor() {
			public void loggingHandleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!Character.isDigit(chars[i])) {
						e.doit = false;
						return;
					}
				}
			}
		});
		port.addKeyListener(new LoggingKeyAdaptor() {
			public void loggingKeyPressed(KeyEvent e) {
			}

			public void loggingKeyReleased(KeyEvent e) {
				setPageComplete(!getHost().isEmpty() && !getPort().isEmpty());
			}
		});

		Label portExampleLabel = new Label(gridComposite, SWT.NONE);
		portExampleLabel.setText(Messages.wizard_config_serverInput_portExample);

		gridComposite.setTabList(new Control[] { host, port });

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
		return ((DatabaseWizard) getWizard()).genericTestPage;
	}
	
	public String getHost() {
		return host.getText().trim();
	}
	
	public String getPort() {
		return port.getText().trim();
	}
}
