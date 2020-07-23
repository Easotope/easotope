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

package org.easotope.client.rawdata.batchimport.savedialog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.rawdata.batchimport.ImportedFile;
import org.easotope.shared.rawdata.cache.input.replicate.InputCacheReplicateSaveListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class SaveDialog extends Dialog implements InputCacheReplicateSaveListener {
	private Shell shell;

	private Composite uploadingComposite;
	private Composite executingComposite;
	private Composite errorUploadingComposite;
	private Composite errorExecutingComposite;

	private int numReplicates;
	private int currentReplicate = 1;
	private ArrayList<ImportedFile> importedFiles;
	private boolean allowDuplicates;

	public SaveDialog(Shell shell, int style) {
		super(shell, style);
		setText(Messages.saveDialog_title);
	}

	public SaveDialog(Shell shell) {
		this(shell, SWT.NONE);
	}

	public void open(TreeSet<ImportedFile> importedFiles, boolean allowDuplicates) {
		this.importedFiles = new ArrayList<ImportedFile>(importedFiles);
		HashSet<Integer> groupNumbers = new HashSet<Integer>();

		for (ImportedFile importedFile : importedFiles) {
			if (!importedFile.isIgnore()) {
				int group = importedFile.getGroup();

				if (group != 0) {
					groupNumbers.add(group);
				} else {
					numReplicates++;
				}
			}
		}

		numReplicates = groupNumbers.size();
		this.allowDuplicates = allowDuplicates;

		Display display = getDisplay();

		shell = new Shell(display, SWT.TITLE | SWT.BORDER | SWT.ON_TOP | SWT.APPLICATION_MODAL);
		shell.setLayout(new StackLayout());
		shell.setText(Messages.saveDialog_title);

		uploadingComposite = createUploadingComposite(shell);
		//executingComposite = createExecutingComposite(shell);
		//errorUploadingComposite = createErrorUploadingComposite(shell);
		//errorExecutingComposite = createErrorExecutingComposite(shell);

		((StackLayout) shell.getLayout()).topControl = uploadingComposite;
		shell.layout();

		shell.setSize(shell.computeSize(500, SWT.DEFAULT));
		Rectangle bounds = display.getBounds();
		Point size = shell.getSize();
		shell.setLocation((bounds.width - size.x) / 2, (bounds.height - size.y) / 2);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	private Composite createUploadingComposite(Shell shell) {
		Composite composite = new Composite(shell, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		composite.setLayout(formLayout);

		Label label = new Label(composite, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		label.setLayoutData(formData);

		String message = MessageFormat.format(Messages.saveDialog_uploading, currentReplicate, numReplicates);
		label.setText(message);

		return composite;
	}

	@Override
	public Display getDisplay() {
		return getParent().getDisplay();
	}

	@Override
	public boolean stillCallabled() {
		return !shell.isDisposed();
	}

	@Override
	public void replicateSaveCompleted(int commandId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void replicateSaveError(int commandId, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void replicateRequestResend(int commandId, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void replicateDeleteCompleted(int commandId) {
		// ignore
	}

	@Override
	public void replicateDeleteError(int commandId, String message) {
		// ignore
	}
}
