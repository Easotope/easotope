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

package org.easotope.client.wizard.database;

import org.easotope.framework.core.util.ApplicationPreferences;
import org.easotope.framework.core.util.ApplicationPreferences.Key;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.framework.dbcore.cmdprocessors.ThreadProcessor;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.eclipse.jface.wizard.Wizard;


public class DatabaseWizard extends Wizard {
	protected boolean isFirstTime;
    protected volatile ThreadProcessor processor;

	protected ChoosePage choosePage;
	protected ServerInputPage serverInputPage;
	protected FolderInputPage folderInputPage;
	protected FolderTestPage folderTestPage;
	protected GenericTestPage genericTestPage;
	protected InitializePage initializePage;

    public DatabaseWizard(boolean isFirstTime) {
    		this.isFirstTime = isFirstTime;
    }

	public void addPages() {
		choosePage = new ChoosePage();
		addPage(choosePage);

		serverInputPage = new ServerInputPage();
		addPage(serverInputPage);

		folderInputPage = new FolderInputPage();
		addPage(folderInputPage);
		
		folderTestPage = new FolderTestPage();
		addPage(folderTestPage);
		
		genericTestPage = new GenericTestPage();
		addPage(genericTestPage);
		
		initializePage = new InitializePage();
		addPage(initializePage);
	}

	public boolean canFinish() {
		return folderTestPage.canFinish() || genericTestPage.canFinish() || initializePage.canFinish();
	}

	public boolean performFinish() {
		ProcessorManager.getInstance().installProcessor(processor, true);

		User user = initializePage.getUser();
		Permissions attributes = initializePage.getAttributes();
		Preferences preferences = initializePage.getPreferences();

		synchronized (ApplicationPreferences.class) {
			if (user != null) {
				LoginInfoCache.getInstance().setLoginInfo(user, attributes, preferences, null);
				ApplicationPreferences.put(Key.LastUser, user == null ? "" : user.getUsername());
			}

			ProcessorManager.ProcessorTypes configType = choosePage.getSelectedConfigType();
			ApplicationPreferences.put(Key.DbType, configType.name());

			switch (configType) {
				case SERVER:
					ApplicationPreferences.put(Key.Param1, serverInputPage.getHost());
					ApplicationPreferences.put(Key.Param2, serverInputPage.getPort());
					break;

				case FOLDER:
					ApplicationPreferences.put(Key.Param1, folderInputPage.getPath());
					break;
			}

			ApplicationPreferences.flush();
		}

		return true;
	}

	public boolean performCancel() {
		folderTestPage.cancelPendingEvents();
		genericTestPage.cancelPendingEvents();
		initializePage.cancelPendingEvents();

		if (processor != null) {
			processor.requestStop();
			processor = null;
		}
		
		return true;
	}
}
