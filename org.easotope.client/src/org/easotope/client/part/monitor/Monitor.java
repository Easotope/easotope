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

package org.easotope.client.part.monitor;

import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.easotope.client.Messages;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.logging.LogListener;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManagerListener;
import org.easotope.shared.core.PotentialGraphicsMethodsShared;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class Monitor implements LogListener, ProcessorManagerListener {
	@Inject
	private Composite parent;
	@Inject
	private Display display;

	private final int TOTAL_LINES = 200;
	
	private Label database;
	private Label status;
	private Label commandsExecuted;
	private Label permissionErrors;
	private Label executionErrors;
	private Label verifyAndResends;
	private Label localEvents;
	private Label remoteEvents;
	private Text log;

	@PostConstruct
	public synchronized void postConstruct() {
		FormLayout layout= new FormLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		parent.setLayout(layout);
		
		Composite gridComposite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridComposite.setLayout(gridLayout);
		
		Label databaseLabel = new Label(gridComposite, SWT.NONE);
		databaseLabel.setText(Messages.part_dbMonitor_databaseLabel);
		
		database = new Label(gridComposite, SWT.NONE);
		
		Label statusLabel = new Label(gridComposite, SWT.NONE);
		statusLabel.setText(Messages.part_dbMonitor_statusLabel);
		
		status = new Label(gridComposite, SWT.NONE);
		
		Label commandsExecutedLabel = new Label(gridComposite, SWT.NONE);
		commandsExecutedLabel.setText(Messages.part_dbMonitor_validCommandsLabel);
		
		commandsExecuted = new Label(gridComposite, SWT.NONE);

		Label executionErrorsLabel = new Label(gridComposite, SWT.NONE);
		executionErrorsLabel.setText(Messages.part_dbMonitor_executionErrorsLabel);

		executionErrors = new Label(gridComposite, SWT.NONE);

		Label verifyAndResendLabel = new Label(gridComposite, SWT.NONE);
		verifyAndResendLabel.setText(Messages.part_dbMonitor_verifyAndResendLabel);

		verifyAndResends = new Label(gridComposite, SWT.NONE);

		Label permissionErrorsLabel = new Label(gridComposite, SWT.NONE);
		permissionErrorsLabel.setText(Messages.part_dbMonitor_permissionErrorsLabel);
		
		permissionErrors = new Label(gridComposite, SWT.NONE);

		Label localEventsLabel = new Label(gridComposite, SWT.NONE);
		localEventsLabel.setText(Messages.part_dbMonitor_localEventsLabel);

		localEvents = new Label(gridComposite, SWT.NONE);

		Label remoteEventsLabel = new Label(gridComposite, SWT.NONE);
		remoteEventsLabel.setText(Messages.part_dbMonitor_remoteEventsLabel);
		
		remoteEvents = new Label(gridComposite, SWT.NONE);

		log = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		FormData formData = new FormData();
		formData.top = new FormAttachment(gridComposite, 10);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		log.setLayoutData(formData);
		log.setEditable(false);
		
		Button clearLog = new Button(parent, SWT.PUSH);
		formData = new FormData();
		formData.bottom = new FormAttachment(log, 0, SWT.TOP);
		formData.right = new FormAttachment(100);
		clearLog.setLayoutData(formData);
		clearLog.setText(Messages.part_dbMonitor_clearLogButton);
		clearLog.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				clearLog();
			}
		});

		ProcessorManager processorManager = ProcessorManager.getInstance();
		processorManager.addListener(this);

		if (processorManager.getProcessor() != null) {
			updateLabels(processorManager.getProcessor());
		}

		ArrayList<String> list = Log.getInstance().addLogListener(this);

		for (String message : list) {
			addLineToLog(message);
		}
	}

	private synchronized void clearLog() {
		log.setText("");
	}
	
	private synchronized void updateLabels(Processor processor) {
		database.setText(processor.getSource());

		if (processor.isStopping()) {
			status.setText(Messages.part_dbMonitor_statusStopping);
		} else if (processor.isPausing()) {
			status.setText(Messages.part_dbMonitor_statusPausing);
		} else if (processor.isPaused()) {
			status.setText(Messages.part_dbMonitor_statusPaused);
		} else if (processor.isConnected()) {
			status.setText(Messages.part_dbMonitor_statusConnected);
		} else {
			status.setText(Messages.part_dbMonitor_statusNotConnected);
		}

		commandsExecuted.setText(String.valueOf(processor.getNumCommandsExecuted()));
		permissionErrors.setText(String.valueOf(processor.getNumPermissionErrors()));
		executionErrors.setText(String.valueOf(processor.getNumExecutionErrors()));
		verifyAndResends.setText(String.valueOf(processor.getNumVerifyAndResend()));
		localEvents.setText(String.valueOf(processor.getNumLocalEvents()));
		remoteEvents.setText(String.valueOf(processor.getNumRemoteEvents()));
	}

	private synchronized void addLineToLog(String message) {
		String delimiter = log.getLineDelimiter();

		int newLineCount = log.getLineCount();
		StringBuffer newText = new StringBuffer(log.getText());

		while (newLineCount > TOTAL_LINES) {
			int eol = newText.indexOf(delimiter);

			if (eol == -1) {
				break;
			}

			newLineCount--;
			newText.delete(0, eol + delimiter.length());
		}

		newText.append(message + delimiter);
		log.setText(newText.toString());
	}

	@Override
	public void newProcessor(final Processor processor) {
		display.asyncExec(new Runnable() {
			public void run() {
				try {
					synchronized (Monitor.this) {
						if (parent == null || parent.isDisposed()) {
							return;
						}
		
						updateLabels(processor);
					}

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(display, e);
				}
			}
		});
	}

	@Override
	public void processorStatusChanged(final Processor processor) {
		display.asyncExec(new Runnable() {
			public void run() {
				try {
					synchronized (Monitor.this) {
						if (parent == null || parent.isDisposed()) {
							return;
						}
		
						updateLabels(processor);
					}

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(display, e);
				}
			}
		});
	}

	@Override
	public void processorConnectionDropped(Processor processor) {	
		// handled by the lifecycle manager
	}

	@Override
	public void processorDatabaseError(Processor processor, String message) {
		// handled by the lifecycle manager
	}

	@Override
	public void newLogMessage(final String message) {
		display.asyncExec(new Runnable() {
			public void run() {
				try {
					synchronized (Monitor.this) {
						if (parent == null || parent.isDisposed()) {
							return;
						}
		
						addLineToLog(message);
					}

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(display, e);
				}
			}
		});
	}
}
