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

package org.easotope.client.dialog;

import java.io.InputStream;

import org.easotope.client.Messages;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.handler.AboutHandler;
import org.easotope.client.util.Documentation;
import org.easotope.framework.core.util.SystemProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

public class AboutDialog extends Dialog {
	public AboutDialog(Shell parent, int style) {
		super(parent, style);
		setText(Messages.dialog_login_title);
	}

	public AboutDialog(Shell parent) {
		this(parent, SWT.NONE);
	}
	
	public void open() {
		Display display = getParent().getDisplay();
		
		final Shell shell = new Shell(display, SWT.DIALOG_TRIM | SWT.ON_TOP | SWT.APPLICATION_MODAL);
		GridLayout gridLayout= new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 20;
		gridLayout.marginHeight = 20;
		gridLayout.horizontalSpacing = 20;
		shell.setLayout(gridLayout);
		shell.setText(Messages.dialog_about_title);

		InputStream inputStream = AboutHandler.class.getClassLoader().getResourceAsStream("images/logo.png");
		final Image logo = new Image(display, inputStream);

		final int logoSize = 150;

		Canvas logoCanvas = new Canvas(shell, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.widthHint = logoSize;
		gridData.heightHint = logoSize;
		logoCanvas.setLayoutData(gridData);
		logoCanvas.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(logo, 0, 0, logo.getImageData().width, logo.getImageData().height, 0, 0, logoSize, logoSize);
			}
		});

		Composite composite = new Composite(shell, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.horizontalIndent = 5;
		composite.setLayoutData(gridData);
		FormLayout formLayout = new FormLayout();
		composite.setLayout(formLayout);

		inputStream = AboutHandler.class.getClassLoader().getResourceAsStream("images/easotope.png");
		final Image easotope = new Image(display, inputStream);

		final int originalEasotopeWidth = easotope.getImageData().width;
		final int originalEasotopeHeight = easotope.getImageData().height;
		
		final int newEasotopeHeight = 40;
		final int newEasotopeWidth = (int) (((double) newEasotopeHeight) * originalEasotopeWidth / originalEasotopeHeight);

		Canvas easotopeCanvas = new Canvas(composite, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(0, newEasotopeHeight);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(0, newEasotopeWidth);
		easotopeCanvas.setLayoutData(formData);
		easotopeCanvas.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(easotope, 0, 0, originalEasotopeWidth, originalEasotopeHeight, 0, 0, newEasotopeWidth, newEasotopeHeight);
			}
		});

		Link versionLink = new Link(composite, SWT.NO_FOCUS);
		formData = new FormData();
		formData.top = new FormAttachment(easotopeCanvas, 10);
		versionLink.setLayoutData(formData);
		versionLink.setText(Messages.dialog_about_release + SystemProperty.getVersion());

		Link cedricLink = new Link(composite, SWT.NO_FOCUS);
		formData = new FormData();
		formData.top = new FormAttachment(versionLink, 10);
		cedricLink.setLayoutData(formData);
		cedricLink.setText(Messages.dialog_about_concept);

		Link devonLink = new Link(composite, SWT.NO_FOCUS);
		formData = new FormData();
		formData.top = new FormAttachment(cedricLink);
		devonLink.setLayoutData(formData);
		devonLink.setText(Messages.dialog_about_coded);
		
		inputStream = AboutHandler.class.getClassLoader().getResourceAsStream("images/carbonate_group.png");
		final Image carbonateGroup = new Image(display, inputStream);

		final int originalCarbonateGroupWidth = carbonateGroup.getImageData().width;
		final int originalCarbonateGroupHeight = carbonateGroup.getImageData().height;

		final int newCarbonateGroupHeight = 50;
		final int newCarbonateGroupWidth = (int) (((double) newCarbonateGroupHeight) * originalCarbonateGroupWidth / originalCarbonateGroupHeight);

		Canvas carbonateGroupCanvas = new Canvas(composite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(0, newCarbonateGroupHeight);
		formData.left = new FormAttachment(easotopeCanvas, 60);
		formData.right = new FormAttachment(easotopeCanvas, newCarbonateGroupWidth+60, SWT.RIGHT);
		carbonateGroupCanvas.setLayoutData(formData);
		carbonateGroupCanvas.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(carbonateGroup, 0, 0, originalCarbonateGroupWidth, originalCarbonateGroupHeight, 0, 0, newCarbonateGroupWidth, newCarbonateGroupHeight);
			}
		});

		inputStream = AboutHandler.class.getClassLoader().getResourceAsStream("images/ziggurat.png");
		final Image ziggurat = new Image(display, inputStream);

		final int originalZigguratWidth = ziggurat.getImageData().width;
		final int originalZigguratHeight = ziggurat.getImageData().height;

		final int newZigguratHeight = 50;
		final int newZigguratWidth = (int) (((double) newZigguratHeight) * originalZigguratWidth / originalZigguratHeight);

		Canvas zigguratCanvas = new Canvas(composite, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.bottom = new FormAttachment(0, newZigguratHeight);
		formData.left = new FormAttachment(carbonateGroupCanvas, 10);
		formData.right = new FormAttachment(carbonateGroupCanvas, newZigguratWidth+10, SWT.RIGHT);
		zigguratCanvas.setLayoutData(formData);
		zigguratCanvas.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(ziggurat, 0, 0, originalZigguratWidth, originalZigguratHeight, 0, 0, newZigguratWidth, newZigguratHeight);
			}
		});

		int totalWidth = newEasotopeWidth + 60 + newZigguratWidth + 10 + newCarbonateGroupWidth;
		
		Label thanksToLabel = new Label(composite, SWT.WRAP);
		formData = new FormData();
		formData.top = new FormAttachment(devonLink, 10);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(0, totalWidth);
		thanksToLabel.setLayoutData(formData);
		thanksToLabel.setText(Messages.dialog_about_thanksTo);

		// this is a kludge because the layout doesn't understand the above SWT.WRAP
		// until after things are already laid out. So we pre-calculate the offset.
		// Note that 4 is the number of lines that the thanks ends up being.
		thanksToLabel.pack();
		int offset = thanksToLabel.getSize().y * 4;

		Link copyrightLink = new Link(composite, SWT.NO_FOCUS);
		formData = new FormData();
		formData.top = new FormAttachment(thanksToLabel, offset+10, SWT.TOP);
		copyrightLink.setLayoutData(formData);
		copyrightLink.setText(Messages.dialog_about_copyright);
		copyrightLink.addListener(SWT.Selection, new LoggingAdaptor() {
			public void loggingHandleEvent(Event event) {
				try {
					String docPath = Documentation.getPath(Messages.documentation_license);
					Program.launch(docPath);
				} catch (Exception e) {
					// ignore
				}
			}
		});

		Link licenseLink = new Link(composite, SWT.NO_FOCUS);
		formData = new FormData();
		formData.top = new FormAttachment(copyrightLink);
		licenseLink.setLayoutData(formData);
		licenseLink.setText(Messages.dialog_about_license);
		
		composite.setTabList(new Control[] { });

		shell.setSize(shell.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		Rectangle bounds = display.getBounds();
		Point size = shell.getSize();
		shell.setLocation((bounds.width - size.x) / 2, (bounds.height - size.y) / 2);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		logo.dispose();
		easotope.dispose();
	}
}
