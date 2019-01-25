/*
 * Copyright © 2016-2019 by Devon Bowen.
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

package org.easotope.client.rawdata.handler;

import java.util.HashMap;

import javax.inject.Named;

import org.easotope.client.rawdata.batchimport.BatchImportPart;
import org.easotope.client.rawdata.navigator.PartManager;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.tables.Permissions;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

public class BatchImport {
	@CanExecute
	public boolean canExecute(@Named(IServiceConstants.ACTIVE_PART) final MContribution contribution) {
		Permissions permissions = LoginInfoCache.getInstance().getPermissions();
		return permissions.isCanBatchImport();
	}

	@Execute
	public void execute(MApplication app, EPartService partService) {
	    PartManager.showRawDataPerspective(app, partService);
	    PartManager.openPart(app, partService, BatchImportPart.ELEMENTID_BASE, BatchImportPart.class.getName(), new HashMap<String,String>(), false);
	}
}
