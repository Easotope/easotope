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

package org.easotope.client.dialog;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingKeyAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.handler.AboutHandler;
import org.easotope.framework.commands.Command;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.ApplicationPreferences;
import org.easotope.framework.core.util.ApplicationPreferences.Key;
import org.easotope.framework.dbcore.cmdprocessors.CommandListener;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorListener;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.commands.LoginWithPermsPrefs;
import org.easotope.shared.core.Browse;
import org.easotope.shared.core.PotentialGraphicsMethodsShared;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.core.tables.TableLayout;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginDialog extends Dialog implements ProcessorListener, CommandListener {
	private volatile Status result = Status.CANCEL;

	private Shell shell;
	Composite stackComposite;
	StackLayout stackLayout;
	Composite loginComposite;
	Composite inputComposite;
	private Text userText;
	private Text passwordText;
	private Button okButton;
	private Link newDatabaseLink1;
	private Link newDatabaseLink2;

	public enum Status { CANCEL, OK, NEW_DB };

	public LoginDialog(Shell parent, int style) {
		super(parent, style);
		setText(Messages.dialog_login_title);
	}

	public LoginDialog(Shell parent) {
		this(parent, SWT.NONE);
	}
	
	public LoginDialog() {
	     this(new Shell(SWT.INHERIT_NONE), SWT.NONE);
	}
	
	public Status open() {
		Display display = getParent().getDisplay();

		shell = new Shell(display, SWT.DIALOG_TRIM | SWT.ON_TOP);
		FormLayout layout= new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		shell.setLayout(layout);
		shell.setText(getText());

		InputStream inputStream = AboutHandler.class.getClassLoader().getResourceAsStream("images/logo.png");
		final Image logo = new Image(display, inputStream);
		final int logoSize = 230;

		inputStream = AboutHandler.class.getClassLoader().getResourceAsStream("images/easotope.png");
		final Image easotope = new Image(display, inputStream);
		final int easotopeHeight = 65;
		final double easotopeWidth = ((double) easotopeHeight) * ((double) easotope.getImageData().width) / ((double) easotope.getImageData().height);

		stackComposite = new Composite(shell, SWT.NONE);
		stackLayout = new StackLayout();
        stackComposite.setLayout(stackLayout);

		loginComposite = new Composite(stackComposite, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.horizontalSpacing = 15;
		loginComposite.setLayout(gridLayout);

		Composite logoComposite = new Composite(loginComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.widthHint = logoSize;
		logoComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		logoComposite.setLayout(gridLayout);
		logoComposite.setBackground(ColorCache.getColor(logoComposite.getDisplay(), ColorCache.RED));

		Canvas canvas1 = new Canvas(logoComposite, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.widthHint = logoSize;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = SWT.CENTER;
		gridData.heightHint = logoSize;
		canvas1.setLayoutData(gridData);
	    canvas1.addPaintListener(new LoggingPaintAdaptor() {
	        public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
	        		e.gc.drawImage(logo, 0, 0, logo.getImageData().width, logo.getImageData().height, 0, 0, logoSize, logoSize);
	        	}
	    });

		Composite restComposite = new Composite(loginComposite, SWT.NONE);
		gridData = new GridData();
		restComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 13;
		restComposite.setLayout(gridLayout);

	    Canvas canvas2 = new Canvas(restComposite, SWT.NONE);
	    gridData = new GridData();
	    gridData.heightHint = easotopeHeight;
	    gridData.widthHint = (int) Math.round(easotopeWidth);
		canvas2.setLayoutData(gridData);
		canvas2.addPaintListener(new LoggingPaintAdaptor() {
	        public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
	        		e.gc.drawImage(easotope, 0, 0, easotope.getImageData().width, easotope.getImageData().height, 0, 0, (int) Math.round(easotopeWidth), easotopeHeight);
			}
	    });

	    inputComposite = new Composite(restComposite, SWT.NONE);
		inputComposite.setLayoutData(new GridData());
		gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		inputComposite.setLayout(gridLayout);

		Label userLabel = new Label(inputComposite, SWT.NONE);
		userLabel.setText(Messages.dialog_login_user);

		userText = new Text(inputComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.TINY_TEXT_INPUT_WIDTH;
		userText.setLayoutData(gridData);
		String lastUser = ApplicationPreferences.get(Key.LastUser);
		if (lastUser != null) {
			userText.setText(lastUser);
		}
		userText.addKeyListener(new LoggingKeyAdaptor() {
			public void loggingKeyPressed(KeyEvent e) {
			}

			public void loggingKeyReleased(KeyEvent e) {
				okButton.setEnabled(!getUser().isEmpty() && !getPassword().isEmpty());
			}
		});

		Label passwordLabel = new Label(inputComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalIndent = 5;
		passwordLabel.setLayoutData(gridData);
		passwordLabel.setText(Messages.dialog_login_password);

		passwordText = new Text(inputComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.TINY_TEXT_INPUT_WIDTH;
		passwordText.setLayoutData(gridData);
		passwordText.setEchoChar('*');
		passwordText.addKeyListener(new LoggingKeyAdaptor() {
			public void loggingKeyPressed(KeyEvent e) {
				if (!getUser().isEmpty() && !getPassword().isEmpty() && (e.character == '\r' || e.character == '\n')) {
					sendLogin();
				}
			}

			public void loggingKeyReleased(KeyEvent e) {
				okButton.setEnabled(!getUser().isEmpty() && !getPassword().isEmpty());
			}
		});

		okButton = new Button(inputComposite, SWT.PUSH);
		gridData = new GridData();
		gridData.horizontalIndent = 5;
		okButton.setLayoutData(gridData);
		okButton.setText(Messages.dialog_login_ok);
		okButton.setEnabled(false);
		okButton.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				sendLogin();
			}
		});
		okButton.addKeyListener(new LoggingKeyAdaptor() {
			public void loggingKeyPressed(KeyEvent e) {
				if (e.character == '\r' || e.character == '\n') {
					sendLogin();
				}
			}
		});

		newDatabaseLink1 = new Link(inputComposite, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.RIGHT;
		newDatabaseLink1.setLayoutData(gridData);
		newDatabaseLink1.setText(Messages.dialog_login_newDatabase);
		newDatabaseLink1.addListener(SWT.Selection, new LoggingAdaptor() {
			public void loggingHandleEvent(Event event) {
				result = Status.NEW_DB;
				shell.close();
			}
		});

		Composite paperComposite = new Composite(restComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = 400;
		paperComposite.setLayoutData(gridData);
		gridLayout = new GridLayout();
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.verticalSpacing = 2;
		paperComposite.setLayout(gridLayout);

		Label paperText = new Label(paperComposite, SWT.WRAP);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		paperText.setLayoutData(gridData);
		paperText.setText(Messages.dialog_login_paperLabel);

		Link paperLink = new Link(paperComposite, SWT.WRAP);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = SWT.FILL;
		paperLink.setLayoutData(gridData);
		paperLink.setText(Messages.dialog_login_paperLink);
		paperLink.addListener(SWT.Selection, new LoggingAdaptor() {
			public void loggingHandleEvent(Event event) {
				Browse.launch(Messages.dialog_login_doiLink);
			}
		});

        Composite waitingComposite = new Composite(stackComposite, SWT.NONE);
		waitingComposite.setLayout(new GridLayout());
		waitingComposite.setSize(loginComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		Label waitingLabel = new Label(waitingComposite, SWT.NONE);
		waitingLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BOTTOM, true, true));
		waitingLabel.setText(Messages.dialog_login_waiting);

		newDatabaseLink2 = new Link(waitingComposite, SWT.NONE);
		newDatabaseLink2.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true));
		newDatabaseLink2.setText(Messages.dialog_login_newDatabase);
		newDatabaseLink2.addListener(SWT.Selection, new LoggingAdaptor() {
			public void loggingHandleEvent(Event event) {
				result = Status.NEW_DB;
				shell.close();
			}
		});

	    stackLayout.topControl = waitingComposite;
		Processor processor = ProcessorManager.getInstance().getProcessor();
	    processor.addListener(this);

		if (processor.isConnected()) {
		    stackLayout.topControl = loginComposite;
		}

		stackComposite.layout();

		inputComposite.setTabList(new Control[] { userText, passwordText, okButton });
		restComposite.setTabList(new Control[] { inputComposite });

		if (stackLayout.topControl == loginComposite) {
			(getUser().isEmpty() ? userText : passwordText).setFocus();
		} else {
			waitingComposite.forceFocus();
		}

		Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		shell.setSize(size);
		Rectangle bounds = display.getBounds();
		shell.setLocation((bounds.width - size.x) / 2, (bounds.height - size.y) / 2);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		ProcessorManager.getInstance().getProcessor().removeListener(this);
		logo.dispose();

		return result;
    }
	
	private void sendLogin() {
		userText.setEnabled(false);
		passwordText.setEnabled(false);
		okButton.setEnabled(false);
		newDatabaseLink1.setEnabled(false);
				
		LoginWithPermsPrefs command = new LoginWithPermsPrefs();
		command.setUsername(userText.getText());
		command.setPassword(passwordText.getText());

		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(command, null, this);
	}

	@Override
	public void commandExecuted(Command command) {
		LoginWithPermsPrefs login = (LoginWithPermsPrefs) command;

		final User user = login.getUser();
		final Permissions permissions = login.getPermissions();
		final Preferences preferences = login.getPreferences();
		final ArrayList<TableLayout> tableLayouts = login.getTableLayouts();

		if (login.getDatabaseIsNotInitialized()) {
			shell.getDisplay().asyncExec(new Runnable() {
				public void run() {
					try {
						if (shell.isDisposed()) {
							return;
						}
	
						MessageDialog.openError(shell, Messages.dialog_login_databaseNotInitializedTitle, Messages.dialog_login_databaseNotInitializedMessage);
	
						result = Status.NEW_DB;
						shell.close();

					} catch (Exception e) {
						Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
						PotentialGraphicsMethodsShared.reportErrorToUser(shell.getDisplay(), e);
					}
				}
			});
			
			return;
		}

		if (user == null) {
			shell.getDisplay().asyncExec(new Runnable() {
		    		public void run() {
		    			try {
			    			if (userText.isDisposed()) {
			    				return;
			    			}

						userText.setEnabled(true);
						passwordText.setEnabled(true);
						okButton.setEnabled(true);
						newDatabaseLink1.setEnabled(true);

						MessageDialog.openError(shell, Messages.dialog_login_failedTitle, Messages.dialog_login_failedMessage);

						passwordText.selectAll();
						passwordText.setFocus();
	
					} catch (Exception e) {
						Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
						PotentialGraphicsMethodsShared.reportErrorToUser(shell.getDisplay(), e);
					}
		    		}
			});
			
			return;
		}

		synchronized (ApplicationPreferences.class) {
			ApplicationPreferences.put(Key.LastUser, user.username);
			ApplicationPreferences.flush();
		}

		LoginInfoCache.getInstance().setLoginInfo(user, permissions, preferences, tableLayouts);
		
	    	shell.getDisplay().asyncExec(new Runnable() {
	    	    	public void run() {
	    	    		try {
		    	    		if (shell.isDisposed()) {
		    	    			return;
		    	    		}
		    	    		
					result = Status.OK;
					shell.close();

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(shell.getDisplay(), e);
				}
	    		}
		});
	}

	private String getUser() {
		return userText.getText().trim();
	}

	private String getPassword() {
		return passwordText.getText().trim();
	}

	@Override
	public void processorStatusChanged(Processor processor) {
		shell.getDisplay().asyncExec(new Runnable() {
	    		public void run() {
	    			try {
		    			if (stackComposite.isDisposed()) {
		    				return;
		    			}
	
		    			stackLayout.topControl = loginComposite;
		    			stackComposite.layout();
		    			(getUser().isEmpty() ? userText : passwordText).setFocus();

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(shell.getDisplay(), e);
				}
	    		}
		});
	}

	@Override
	public void processorConnectionDropped(Processor processor) {
		// TODO what to do here?
	}

	@Override
	public void processorDatabaseError(Processor processor, final String message) {
		shell.getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					String string = MessageFormat.format(Messages.dialog_login_databaseError, message);
					MessageDialog.open(MessageDialog.ERROR, shell, Messages.dialog_login_databaseErrorTitle, string, SWT.NONE);
					System.exit(0);

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(shell.getDisplay(), e);
				}
			}
		});
	}
}
