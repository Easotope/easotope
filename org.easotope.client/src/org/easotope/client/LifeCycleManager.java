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

package org.easotope.client;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.easotope.client.core.BlockUntilNoOnePersisting;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.dialog.BlockWhileSavingDialog;
import org.easotope.client.dialog.LoginDialog;
import org.easotope.client.handler.QuitHandler;
import org.easotope.client.util.Sleak;
import org.easotope.client.versioninfo.VersionInfo;
import org.easotope.client.wizard.database.DatabaseWizard;
import org.easotope.framework.commands.Command;
import org.easotope.framework.core.global.OptionsInfo;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.logging.LogTerminateListener;
import org.easotope.framework.core.util.ApplicationPreferences;
import org.easotope.framework.core.util.ApplicationPreferences.Key;
import org.easotope.framework.core.util.Platform;
import org.easotope.framework.core.util.SystemProperty;
import org.easotope.framework.core.util.TopDir;
import org.easotope.framework.dbcore.cmdprocessors.EventListener;
import org.easotope.framework.dbcore.cmdprocessors.FolderProcessor;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorListener;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.framework.dbcore.cmdprocessors.ServerProcessor;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.PotentialGraphicsMethodsShared;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ISaveHandler;
import org.eclipse.e4.ui.workbench.modeling.IWindowCloseHandler;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class LifeCycleManager implements ProcessorListener, LoginInfoCacheLoginInfoGetListener, EventListener, LogTerminateListener {
	@Inject
	@Optional
	private EPartService partService;
	@Inject
	@Optional
	public Shell shell;
	@Inject
	@Optional
	private IWorkbench workbench;
	@Inject
	@Optional
	private Display display;
	@Inject
	private UISynchronize sync;
	@Inject
	@Optional
	MApplication application;
	@Inject
	@Optional
	EModelService modelService;

	@PostContextCreate
	public void postContextCreate() {
		if (display.getDeviceData().tracking) {
			Sleak.start(display);
		}

		if (!Platform.isMacOs()) {
			String topDir = null;

			try {
				topDir = TopDir.getPathToTopDir();
			} catch (Exception e) {
				// ignore
			}

			Log.getInstance().openLogFile(topDir);
		}

		Log.getInstance().addLogTerminateListener(this);
		Log.getInstance().log(Level.INFO, "Java version " + System.getProperty("java.version"));
		Log.getInstance().log(Level.INFO, "Available processors " + Runtime.getRuntime().availableProcessors());
		Log.getInstance().log(Level.INFO, "Total memory " + Runtime.getRuntime().totalMemory());
		Log.getInstance().log(Level.INFO, "Max memory " + Runtime.getRuntime().maxMemory());
		Log.getInstance().log(Level.INFO, "Free memory " + Runtime.getRuntime().freeMemory());

		String dbType = ApplicationPreferences.get(Key.DbType);

		if (dbType == null || dbType.isEmpty()) {
			WizardDialog wizardDialog = new WizardDialog(null, new DatabaseWizard(true));
			if (wizardDialog.open() == Window.CANCEL) {
				// No workbench exists at this point so just bail.
				System.exit(0);
			}

		} else {
			ProcessorManager.ProcessorTypes configType = ProcessorManager.ProcessorTypes.valueOf(dbType);
			Processor processor = null;

			switch (configType) {
				case SERVER:
					String host = ApplicationPreferences.get(Key.Param1);
					String port = ApplicationPreferences.get(Key.Param2);
					processor = new ServerProcessor(host, Integer.parseInt(port));
					break;

				case FOLDER:
					String path = ApplicationPreferences.get(Key.Param1);
					processor = new FolderProcessor(path, false, false);
					break;
			}

			ProcessorManager.getInstance().installProcessor(processor, false);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// do nothing
			}
		}

		while (LoginInfoCache.getInstance().getAuthenticationObjects() == null) {
			LoginDialog loginDialog = new LoginDialog();
			LoginDialog.Status status = loginDialog.open();

			switch (status) {
				case CANCEL:
					// No workbench exists at this point so just bail.
					System.exit(0);

				case OK:
					break;

				case NEW_DB:
					new WizardDialog(null, new DatabaseWizard(false)).open();
			}
		}

		LoginInfoCache.getInstance().addListener(this);
		ProcessorManager.getInstance().getProcessor().addListener(this);
		ProcessorManager.getInstance().getProcessor().addEventListener(this);

		OptionsInfo.getInstance();

		int serverVersion = VersionInfo.getInstance().getServerVersion();
		int clientVersion = SystemProperty.getVersion();

		boolean versionsDoNotMatch = (serverVersion > clientVersion) ? VersionInfo.getInstance().isServerDeniesConnection() : (serverVersion < SystemProperty.getOldestCompatVersion() || serverVersion > SystemProperty.getVersion());

		if (versionsDoNotMatch) {
			String message = Messages.lifecycle_versionMismatch;
			MessageDialog dialog= new MessageDialog(null, Messages.lifecycle_versionMismatchTitle, null, message, MessageDialog.ERROR, new String[] { Messages.lifecycle_downloadNewVersion, IDialogConstants.OK_LABEL }, 0);
			int button = dialog.open();

			if (button == 0) {
				String url = MessageFormat.format(Messages.lifecycle_bestClientForServerRedirect, String.valueOf(serverVersion)); 
				Program.launch(url);
			}

			// No workbench exists at this point so just bail.
			System.exit(0);
		}

		if (LoginInfoCache.getInstance().getPreferences().getCheckForUpdates()) {
			boolean usingServer = ProcessorManager.getInstance().getProcessor() instanceof ServerProcessor;
			new DownloadVersionInfo(display, serverVersion, usingServer).start();
		}
	}

	@Override
	public void processorStatusChanged(Processor processor) {
		// do nothing
	}

	@Override
	public void processorConnectionDropped(Processor processor) {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					MessageDialog.open(MessageDialog.ERROR, shell, Messages.lifecycle_lostConnectionTitle, Messages.lifecycle_lostConnection, SWT.NONE);
	
					if (workbench != null) {
						workbench.close();
					} else {
						System.exit(0);
					}

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(shell.getDisplay(), e);
				}
			}
		});
	}

	@Override
	public void processorDatabaseError(Processor processor, final String message) {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					String string = MessageFormat.format(Messages.lifecycle_databaseError, message);
					MessageDialog.open(MessageDialog.ERROR, shell, Messages.lifecycle_databaseErrorTitle, string, SWT.NONE);
	
					if (workbench != null) {
						workbench.close();
					} else {
						System.exit(0);
					}

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(shell.getDisplay(), e);
				}
			}
		});
	}

	@ProcessAdditions
	public void processAdditions(IEventBroker eventBroker) {
		ArrayList<MPart> allPermissionedParts = new ArrayList<MPart>();

		for (String tag : new String[] { User.ISADMIN_FIELD_NAME }) {
			ArrayList<String> tagsToMatch = new ArrayList<String>();
			tagsToMatch.add(tag);
			allPermissionedParts.addAll(modelService.findElements(application, null, MPart.class, tagsToMatch));
		}

		User user = (User) LoginInfoCache.getInstance().getAuthenticationObjects().get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) LoginInfoCache.getInstance().getAuthenticationObjects().get(AuthenticationKeys.PERMISSIONS);

		for (MPart part : allPermissionedParts) {
			setUiElementVisibility(part, user, permissions);
		}

		MWindow window = (MWindow) modelService.find("org.easotope.client.window.main", application);
		eventBroker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, new AppStartupCompleteEventHandler(window));
	}

	private void setUiElementVisibility(MPart part, User user, Permissions permissions) {
		boolean shouldBeVisible = true;
		List<String> tags = part.getTags();

		if (tags.contains(User.ISADMIN_FIELD_NAME) && !user.getIsAdmin()) {
			shouldBeVisible = false;
		}

		if (part.isVisible() != shouldBeVisible) {
			if (workbench != null && workbench.getApplication() != null && workbench.getApplication().getContext() != null && workbench.getApplication().getContext().getActiveChild() != null && partService.getActivePart() == part) {
				MPart monitorPart = partService.findPart("org.easotope.configuration.monitor.part");
				partService.showPart(monitorPart, PartState.ACTIVATE);
			}

			//TODO If the perspective is not visible, this doesn't work fully.
			//If the tab is selected but another perspective is active (so the tab is not visible)
			//then the composite is blanked and the active tab changes but the tab does not disappear.
			//Seems like an eclipse bug because it's such an incredibly specific case.
			part.setVisible(shouldBeVisible);
		}
	}

	@PreSave
	public void preSave() {
		BlockUntilNoOnePersisting.getInstance().clearErrorFlag();

		if (BlockUntilNoOnePersisting.getInstance().donePersisting()) {
			return;
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// ignore
		}

		if (!BlockUntilNoOnePersisting.getInstance().donePersisting() || BlockUntilNoOnePersisting.getInstance().getHadErrorsWhileBlocking()) {
			BlockWhileSavingDialog blockWhileSavingDialog = new BlockWhileSavingDialog();
			blockWhileSavingDialog.open();
		}
	}

	private class AppStartupCompleteEventHandler implements EventHandler {
		private MWindow theWindow;

		AppStartupCompleteEventHandler(MWindow window) {
			theWindow = window;
		}

		@Override
		public void handleEvent(Event event) {
			theWindow.getContext().set(IWindowCloseHandler.class, new WindowCloseHandler());
			theWindow.getContext().set(ISaveHandler.class, new PartSaveHandler());
		}
	}

	private class WindowCloseHandler implements IWindowCloseHandler {
		@Override
		public boolean close(MWindow window) {
			return QuitHandler.quitLogic(shell, application, modelService);
		} 
	}

	private class PartSaveHandler implements ISaveHandler {
		@Override
		public Save[] promptToSave(Collection<MPart> parts) {
			// it's not clear when/if this is called
			return null;
		}

        @Override
        public Save promptToSave(MPart part) {
        		// it's not clear when/if this is called
			return Save.CANCEL;
        }

		@Override
		public boolean saveParts(Collection<MPart> dirtyParts, boolean confirm) {
			return false;
		}

		@Override
		public boolean save(MPart dirtyPart, boolean confirm) {
			ChainedPart chainedPart = (ChainedPart) dirtyPart.getObject();

			if (chainedPart.canPersist()) {
				MessageDialog messageDialog = new MessageDialog(shell, Messages.lifecycle_saveDirtyTabTitle, null, Messages.lifecycle_saveDirtyTab, MessageDialog.QUESTION, new String[] { "Yes", "No", "Cancel" }, 2);
				int button = messageDialog.open();

				switch (button) {
					case 0:
						chainedPart.persist();
						break;

					case 1:
						chainedPart.closePartIfRequested();
						break;
				}

			} else {
				MessageDialog messageDialog = new MessageDialog(shell, Messages.lifecycle_saveDirtyTabWithErrorsTitle, null, Messages.lifecycle_saveDirtyTabWithErrors, MessageDialog.QUESTION, new String[] { "Yes", "No" }, 1);
				int button = messageDialog.open();

				if (button == 0) {
					chainedPart.closePartIfRequested();
				}
			}

			return false;
		}
    };

	@Override
	public Display getDisplay() {
		return display;
	}

	@Override
	public boolean stillCallabled() {
		return true;
	}

	@Override
	public void loginInfoUpdated(int commandId, final User user, final Permissions permissions, Preferences preferences) {
		sync.asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					for (MPart part : modelService.findElements(application, null, MPart.class, null)) {
						setUiElementVisibility(part, user, permissions);
					}

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(Display.getDefault(), e);
				}
			}
		});
	}

	@Override
	public void eventReceived(org.easotope.framework.dbcore.cmdprocessors.Event event, Command command) {

	}

	@Override
	public void logTerminate(String timestamp, String source, final String message, String stackTrace) {
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					String string = MessageFormat.format(Messages.lifecycle_terminalError, message);
					MessageDialog.open(MessageDialog.ERROR, shell, Messages.lifecycle_terminalErrorTitle, string, SWT.NONE);
	
					if (workbench != null) {
						workbench.close();
					} else {
						System.exit(0);
					}

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(shell.getDisplay(), e);
				}
			}
		});		
	}
}
