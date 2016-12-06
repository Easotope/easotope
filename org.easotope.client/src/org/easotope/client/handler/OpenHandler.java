/*
 * Copyright © 2016 by Devon Bowen.
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

package org.easotope.client.handler;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Named;

import org.easotope.shared.core.FileEditor;
import org.easotope.shared.rawdata.parser.AutoParser;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class OpenHandler {
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) final MContribution contribution) {
		Object object = contribution.getObject();
		
		if (object != null && object instanceof FileEditor) {
			FileEditor fileEditor = (FileEditor) object;
			return fileEditor.canAcceptFiles();
		}

		return false;
	}

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, @Named(IServiceConstants.ACTIVE_PART) final MContribution contribution) throws InvocationTargetException, InterruptedException {
		Object object = contribution.getObject();

		if (object == null || !(object instanceof FileEditor)) {
			return;
		}

		String[] fileExtensions = AutoParser.getFileExtensions();
		String[] newFileExtensions = new String[fileExtensions.length+1];
		newFileExtensions[0] = "";

		for (int i=0; i<fileExtensions.length; i++) {
			newFileExtensions[0] += (newFileExtensions[0].isEmpty() ? "*." : ";*.") + fileExtensions[i];
			newFileExtensions[i+1] = "*." + fileExtensions[i];
		}
		
		String[] filterNames = AutoParser.getFilterNames();
		String[] newFilterNames = new String[filterNames.length+1];
		newFilterNames[0] = "All";
		
		for (int i=0; i<filterNames.length; i++) {
			newFilterNames[i+1] = filterNames[i] + " (*." + fileExtensions[i] + ")";
		}

		FileDialog fileDialog = new FileDialog(shell, SWT.MULTI);
		fileDialog.setFilterExtensions(newFileExtensions);
		fileDialog.setFilterNames(newFilterNames);
		fileDialog.open();

		String directory = fileDialog.getFilterPath();
		String[] filenames = fileDialog.getFileNames();

		if (filenames.length == 0) {
			return;
		}

		String[] newFilenames = new String[filenames.length];

		for (int i=0; i<filenames.length; i++) {
			newFilenames[i] = directory + File.separator + filenames[i];
		}

		((FileEditor) object).addFiles(newFilenames);
	}
}
