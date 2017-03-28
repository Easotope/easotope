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

import java.util.Timer;
import java.util.TimerTask;

import org.easotope.client.Messages;
import org.easotope.client.core.BlockUntilNoOnePersisting;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.shared.core.PotentialGraphicsMethodsShared;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class BlockWhileSavingDialog extends Dialog {
	public BlockWhileSavingDialog(Shell parent, int style) {
		super(parent, style);
		setText(Messages.dialog_login_title);
	}

	public BlockWhileSavingDialog(Shell parent) {
		this(parent, SWT.NONE);
	}

	public BlockWhileSavingDialog() {
		this(new Shell(SWT.INHERIT_NONE), SWT.NONE);
	}

	public void open() {
		Display display = getParent().getDisplay();

		final Shell shell = new Shell(display, SWT.DIALOG_TRIM | SWT.ON_TOP | SWT.APPLICATION_MODAL);
		shell.setLayout(new FillLayout());
		shell.setText(Messages.dialog_blockWhileSaving_title);

		final Composite stackComposite = new Composite(shell, SWT.NONE);
		final StackLayout stackLayout = new StackLayout();
        stackComposite.setLayout(stackLayout);

        Composite waitingComposite = new Composite(stackComposite, SWT.NONE);
        FormLayout formLayout= new FormLayout();
		formLayout.marginWidth = 20;
		formLayout.marginHeight = 20;
		waitingComposite.setLayout(formLayout);

		Label label = new Label(waitingComposite, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		label.setLayoutData(formData);
		label.setText(Messages.dialog_blockWhileSaving_explainWaiting);

		Button button = new Button(waitingComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label, 0);
		formData.bottom = new FormAttachment(100);
		formData.right = new FormAttachment(100);
		button.setLayoutData(formData);
		button.setText(Messages.dialog_blockWhileSaving_dontWait);
		button.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				shell.close();
			}
		});

        final Composite errorComposite = new Composite(stackComposite, SWT.NONE);
        formLayout= new FormLayout();
		formLayout.marginWidth = 20;
		formLayout.marginHeight = 20;
		errorComposite.setLayout(formLayout);

		label = new Label(errorComposite, SWT.WRAP);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		label.setLayoutData(formData);
		label.setText(Messages.dialog_blockWhileSaving_saveErrors);

		button = new Button(errorComposite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label, 0);
		formData.bottom = new FormAttachment(100);
		formData.right = new FormAttachment(100);
		button.setLayoutData(formData);
		button.setText(Messages.dialog_blockWhileSaving_OK);
		button.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				shell.close();
			}
		});

		boolean doneSaving = BlockUntilNoOnePersisting.getInstance().donePersisting();
		boolean hadErrorsWhileBlocking = BlockUntilNoOnePersisting.getInstance().getHadErrorsWhileBlocking();

		stackLayout.topControl = (doneSaving && hadErrorsWhileBlocking) ? errorComposite : waitingComposite;
		stackComposite.layout();

		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				boolean doneSaving = BlockUntilNoOnePersisting.getInstance().donePersisting();
				boolean hadErrorsWhileBlocking = BlockUntilNoOnePersisting.getInstance().getHadErrorsWhileBlocking();

				if (doneSaving) {
					if (!hadErrorsWhileBlocking) {
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								try {
									shell.close();

								} catch (Exception e) {
									Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
									PotentialGraphicsMethodsShared.reportErrorToUser(shell.getDisplay(), e);
								}
							}
						});
	
						return;

					} else {
						shell.getDisplay().syncExec(new Runnable() {
							public void run() {
								try {
									stackLayout.topControl = errorComposite;
									stackComposite.layout();

								} catch (Exception e) {
									Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
									PotentialGraphicsMethodsShared.reportErrorToUser(shell.getDisplay(), e);
								}
							}
						});
					}
				}
			}
		}, 500, 500);

		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		Rectangle bounds = display.getBounds();
		Point size = shell.getSize();
		shell.setLocation((bounds.width - size.x) / 2, (bounds.height - size.y) / 2);

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		timer.cancel();
	}
}
