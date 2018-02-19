/*
 * Copyright © 2016-2018 by Devon Bowen.
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

package org.easotope.shared.core.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.easotope.framework.commands.Command;
import org.easotope.framework.commands.Command.Status;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.Reflection;
import org.easotope.framework.dbcore.cmdprocessors.CommandListener;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.EventListener;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.core.PotentialGraphicsMethodsShared;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractCache implements CommandListener, EventListener {
	private static HashMap<Thread,HashMap<Class<?>,AbstractCache>> instanceManager = new HashMap<Thread,HashMap<Class<?>,AbstractCache>>();

	private CacheHashMap cache = new CacheHashMap();
	private Vector<CachePlugin> cachePlugins = new Vector<CachePlugin>();

	private HashMap<Integer,DatabaseCallParameters> commandIdsToDatabaseGetCallParameters = new HashMap<Integer,DatabaseCallParameters>();
	private HashMap<CacheKey,DatabaseCallParameters> cacheKeyToDatabaseGetCallParameters = new HashMap<CacheKey,DatabaseCallParameters>();
	private HashMap<Integer,DatabaseCallParameters> commandIdsToDatabaseSaveCallParameters = new HashMap<Integer,DatabaseCallParameters>();
	private HashMap<Integer,DatabaseCallParameters> commandIdsToDatabaseDeleteCallParameters = new HashMap<Integer,DatabaseCallParameters>();
	private HashMap<Integer,CacheKey> commandIdsToReloadedCacheKey = new HashMap<Integer,CacheKey>();

	private DatabaseCallParameters currentDatabaseGetCallParameters = null;
	private DatabaseCallParameters currentDatabaseSaveCallParameters = null;
	private DatabaseCallParameters currentDatabaseDeleteCallParameters = null;
	private CacheKey currentReloadCacheKey = null;

	private Vector<CacheListener> broadcastListener = new Vector<CacheListener>();

	protected AbstractCache() {
		ProcessorManager.getInstance().getProcessor().addEventListener(this);
	}

	public CacheHashMap getCache() {
		return cache;
	}

	protected static AbstractCache getCacheInstanceForThisThread(Class<?> clazz) {
		synchronized (instanceManager) {
			HashMap<Class<?>,AbstractCache> instancesForThread = instanceManager.get(Thread.currentThread());

			if (instancesForThread == null) {
				instancesForThread = new HashMap<Class<?>,AbstractCache>();
				instanceManager.put(Thread.currentThread(), instancesForThread);
			}

			AbstractCache abstractCache = instancesForThread.get(clazz);

			if (abstractCache == null) {
				abstractCache = (AbstractCache) Reflection.createObject(clazz.getName());
				instancesForThread.put(clazz, abstractCache);
			}

			return abstractCache;
		}
	}

	public static void clearCachesForThisThread() {
		synchronized (instanceManager) {
			if (!instanceManager.containsKey(Thread.currentThread())) {
				return;
			}

			HashMap<Class<?>,AbstractCache> instancesForThread = instanceManager.get(Thread.currentThread());

			for (Class<?> clazz : instancesForThread.keySet()) {
				AbstractCache abstractCache = instancesForThread.get(clazz);
				abstractCache.dispose();
			}

			instanceManager.remove(Thread.currentThread());
		}
	}

	private synchronized void dispose() {
		ProcessorManager.getInstance().getProcessor().removeEventListener(this);
		
		cache.clear();
		cachePlugins.clear();
		
		commandIdsToDatabaseGetCallParameters.clear();
		cacheKeyToDatabaseGetCallParameters.clear();
		commandIdsToDatabaseSaveCallParameters.clear();
		commandIdsToReloadedCacheKey.clear();
		
		currentDatabaseGetCallParameters = null;
		currentDatabaseSaveCallParameters = null;
		currentReloadCacheKey = null;

		broadcastListener.clear();
	}

	protected synchronized void addPlugin(CachePlugin cachePlugin) {
		cachePlugins.add(cachePlugin);
	}

	protected synchronized int getObject(CachePlugin cachePlugin, CacheListener cacheListener, Object... callParameters) {
		CacheKey cacheKey = cachePlugin.createCacheKey(callParameters);
		cacheKey.setRecallInfo(cachePlugin, callParameters);

		if (cache.containsKey(cacheKey)) {
			Object result = cache.get(cacheKey);
			cachePlugin.callbackGetCompleted(cacheListener, Command.UNDEFINED_ID, result);

			if (cacheListener instanceof GenericCacheObjectListener) {
				((GenericCacheObjectListener) cacheListener).objectGetCompleted(Command.UNDEFINED_ID, result);
			}

			return Command.UNDEFINED_ID;
		}

		if (cacheKeyToDatabaseGetCallParameters.containsKey(cacheKey)) {
			DatabaseCallParameters databaseCallParameters = cacheKeyToDatabaseGetCallParameters.get(cacheKey);
			databaseCallParameters.getGuiCallbackParameters().add(new GuiCallbackParameters(Display.findDisplay(Thread.currentThread()), cacheListener));

			return databaseCallParameters.getCommandId();
		}

		currentDatabaseGetCallParameters = new DatabaseCallParameters(Command.UNDEFINED_ID, cacheKey, cachePlugin, callParameters);
		currentDatabaseGetCallParameters.getGuiCallbackParameters().add(new GuiCallbackParameters(Display.findDisplay(Thread.currentThread()), cacheListener));

		int commandId = cachePlugin.getData(this, callParameters);

		if (currentDatabaseGetCallParameters != null) {
			currentDatabaseGetCallParameters.setCommandId(commandId);

			commandIdsToDatabaseGetCallParameters.put(commandId, currentDatabaseGetCallParameters);
			cacheKeyToDatabaseGetCallParameters.put(cacheKey, currentDatabaseGetCallParameters);

			currentDatabaseGetCallParameters = null;
			return commandId;
		}

		return Command.UNDEFINED_ID;
	}

	protected synchronized int saveObject(CachePlugin cachePlugin, CacheListener cacheListener, Object... callParameters) {
		currentDatabaseSaveCallParameters = new DatabaseCallParameters(Command.UNDEFINED_ID, null, cachePlugin, callParameters);
		currentDatabaseSaveCallParameters.getGuiCallbackParameters().add(new GuiCallbackParameters(Display.findDisplay(Thread.currentThread()), cacheListener));

		int commandId = cachePlugin.saveData(this, callParameters);

		if (currentDatabaseSaveCallParameters != null) {
			currentDatabaseSaveCallParameters.setCommandId(commandId);

			commandIdsToDatabaseSaveCallParameters.put(commandId, currentDatabaseSaveCallParameters);

			currentDatabaseSaveCallParameters = null;
			return commandId;
		}

		return Command.UNDEFINED_ID;
	}

	protected synchronized int deleteObject(CachePlugin cachePlugin, CacheListener cacheListener, Object... callParameters) {
		CacheKey cacheKey = cachePlugin.createCacheKey(callParameters);
		currentDatabaseDeleteCallParameters = new DatabaseCallParameters(Command.UNDEFINED_ID, cacheKey, cachePlugin, callParameters);
		currentDatabaseDeleteCallParameters.getGuiCallbackParameters().add(new GuiCallbackParameters(Display.findDisplay(Thread.currentThread()), cacheListener));

		int commandId = cachePlugin.deleteData(this, callParameters);
		
		if (currentDatabaseDeleteCallParameters != null) {
			currentDatabaseDeleteCallParameters.setCommandId(commandId);

			commandIdsToDatabaseDeleteCallParameters.put(commandId, currentDatabaseDeleteCallParameters);

			currentDatabaseDeleteCallParameters = null;
			return commandId;
		}

		return Command.UNDEFINED_ID;
	}

	@Override
	public synchronized void commandExecuted(Command command) {
		if (currentDatabaseGetCallParameters != null) {
			if (command.getStatus() == Status.OK) {
				currentDatabaseGetCallParameters.getCachePlugin().processData(command, cache, currentDatabaseGetCallParameters.getCallParameters());
			}

			callBackDatabaseCallParameters(currentDatabaseGetCallParameters, command.getStatus(), command.getMessage());
			currentDatabaseGetCallParameters = null;

		} else if (commandIdsToDatabaseGetCallParameters.containsKey(command.getClientUniqueId())) {
			DatabaseCallParameters databaseCallParameters = commandIdsToDatabaseGetCallParameters.remove(command.getClientUniqueId());
			cacheKeyToDatabaseGetCallParameters.remove(databaseCallParameters.getCacheKey());

			if (command.getStatus() == Status.OK) {
				databaseCallParameters.getCachePlugin().processData(command, cache, databaseCallParameters.getCallParameters());
			}

			callBackDatabaseCallParameters(databaseCallParameters, command.getStatus(), command.getMessage());

		} else if (currentDatabaseSaveCallParameters != null) {
			CachePlugin cachePlugin = currentDatabaseSaveCallParameters.getCachePlugin();
			GuiCallbackParameters guiCallbackParameters = currentDatabaseSaveCallParameters.getGuiCallbackParameters().get(0);
			Display display = guiCallbackParameters.getDisplay();
			CacheListener commandListener = guiCallbackParameters.getCacheListener();

			callSave(display, command, cachePlugin, commandListener);
			currentDatabaseSaveCallParameters = null;

		} else if (commandIdsToDatabaseSaveCallParameters.containsKey(command.getClientUniqueId())) {
			DatabaseCallParameters databaseCallParameters = commandIdsToDatabaseSaveCallParameters.remove(command.getClientUniqueId());

			CachePlugin cachePlugin = databaseCallParameters.getCachePlugin();
			GuiCallbackParameters guiCallbackParameters = databaseCallParameters.getGuiCallbackParameters().get(0);
			Display display = guiCallbackParameters.getDisplay();
			CacheListener commandListener = guiCallbackParameters.getCacheListener();

			callSave(display, command, cachePlugin, commandListener);

		} else if (currentDatabaseDeleteCallParameters != null) {
			if (command.getStatus() == Command.Status.OK) {
				cache.remove(currentDatabaseDeleteCallParameters.getCacheKey());
			}

			CachePlugin cachePlugin = currentDatabaseDeleteCallParameters.getCachePlugin();
			GuiCallbackParameters guiCallbackParameters = currentDatabaseDeleteCallParameters.getGuiCallbackParameters().get(0);
			Display display = guiCallbackParameters.getDisplay();
			CacheListener commandListener = guiCallbackParameters.getCacheListener();

			callDelete(display, command, cachePlugin, commandListener);
			currentDatabaseDeleteCallParameters= null;

		} else if (commandIdsToDatabaseDeleteCallParameters.containsKey(command.getClientUniqueId())) {
			DatabaseCallParameters databaseCallParameters = commandIdsToDatabaseDeleteCallParameters.remove(command.getClientUniqueId());

			if (command.getStatus() == Command.Status.OK) {
				cache.remove(databaseCallParameters.getCacheKey());
			}

			CachePlugin cachePlugin = databaseCallParameters.getCachePlugin();
			GuiCallbackParameters guiCallbackParameters = databaseCallParameters.getGuiCallbackParameters().get(0);
			Display display = guiCallbackParameters.getDisplay();
			CacheListener commandListener = guiCallbackParameters.getCacheListener();

			callDelete(display, command, cachePlugin, commandListener);
			
		} else if (currentReloadCacheKey != null || commandIdsToReloadedCacheKey.containsKey(command.getClientUniqueId())) {
			CacheKey cacheKey = currentReloadCacheKey != null ? currentReloadCacheKey : commandIdsToReloadedCacheKey.get(command.getClientUniqueId());

			if (command.getStatus() == Status.OK) {
				CachePlugin cachePlugin = cacheKey.getCachePlugin();
				cachePlugin.processData(command, cache, cacheKey.getCallParameters());

				for (CacheListener listener : broadcastListener) {
					callbackUpdate(command, cachePlugin, listener, cache.get(cacheKey));
				}

			} else {
				// TODO what to do if it wasn't successful???
			}
		}
	}

	private void callBackDatabaseCallParameters(DatabaseCallParameters databaseCallParameters, Status status, String message) {
		int commandId = databaseCallParameters.getCommandId();
		CacheKey cacheKey = databaseCallParameters.getCacheKey();
		CachePlugin cachePlugin = databaseCallParameters.getCachePlugin();
		Object result = cache.get(cacheKey);

		for (GuiCallbackParameters guiCallbackParameters : databaseCallParameters.getGuiCallbackParameters()) {
			Display display = guiCallbackParameters.getDisplay();
			CacheListener cacheListener = guiCallbackParameters.getCacheListener();

			callbackGet(display, status == Status.OK, commandId, cachePlugin, cacheListener, result, message);
		}

		if (cacheKey.isDeleteAfterDelivery()) {
			cache.remove(cacheKey);
		}
	}

	private void callbackGet(final Display display, final boolean notError, final int commandId, final CachePlugin cachePlugin, final CacheListener cacheListener, final Object result, final String message) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					if (!cacheListener.stillCallabled()) {
						return;
					}

					if (notError) {
						cachePlugin.callbackGetCompleted(cacheListener, commandId, result);

						if (cacheListener instanceof GenericCacheObjectListener) {
							((GenericCacheObjectListener) cacheListener).objectGetCompleted(commandId, result);
						}

					} else {
						cachePlugin.callbackGetError(cacheListener, commandId, message);

						if (cacheListener instanceof GenericCacheObjectListener) {
							((GenericCacheObjectListener) cacheListener).objectGetError(commandId, message);
						}
					}

				} catch (RuntimeException t) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", t);
					PotentialGraphicsMethodsShared.reportErrorToUser(display, t);
				}
			}
		};

		if (display == null || display == Display.findDisplay(Thread.currentThread())) {
			runnable.run();
		} else {
			display.asyncExec(runnable);
		}
	}

	private void callSave(final Display display, final Command command, final CachePlugin cachePlugin, final CacheListener cacheListener) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					if (!cacheListener.stillCallabled()) {
						return;
					}

					if (command.getStatus() == Status.OK) {
						cachePlugin.callbackSaveCompleted(cacheListener, command);
					} else if (command.getStatus() == Status.VERIFY_AND_RESEND) {
						cachePlugin.callbackVerifyAndResend(cacheListener, command.getClientUniqueId(), command.getMessage());
					} else {
						cachePlugin.callbackSaveError(cacheListener, command.getClientUniqueId(), command.getMessage());
					}

				} catch (RuntimeException t) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", t);
					PotentialGraphicsMethodsShared.reportErrorToUser(display, t);
				}
			}
		};

		if (display == null || display == Display.findDisplay(Thread.currentThread())) {
			runnable.run();
		} else {
			display.asyncExec(runnable);
		}
	}

	private void callDelete(final Display display, final Command command, final CachePlugin cachePlugin, final CacheListener cacheListener) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					if (!cacheListener.stillCallabled()) {
						return;
					}

					if (command.getStatus() == Status.OK) {
						cachePlugin.callbackDeleteCompleted(cacheListener, command);
					} else {
						cachePlugin.callbackDeleteError(cacheListener, command.getClientUniqueId(), command.getMessage());
					}

				} catch (RuntimeException t) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", t);
					PotentialGraphicsMethodsShared.reportErrorToUser(display, t);
				}
			}
		};

		if (display == null || display == Display.findDisplay(Thread.currentThread())) {
			runnable.run();
		} else {
			display.asyncExec(runnable);
		}
	}

	@Override
	public synchronized void eventReceived(Event event, Command command) {
		for (CachePlugin cachePlugin : cachePlugins) {
			HashSet<CacheKey> cacheKeys = cachePlugin.getCacheKeysThatNeedReloadBasedOnEvent(event, cache);

			if (cacheKeys != null) {
				for (CacheKey cacheKey : cacheKeys) {
					if (cache.containsKey(cacheKey)) {
						CacheKey realCacheKey = cache.getKey(cacheKey);

						if (realCacheKey.getCachePlugin() != null && realCacheKey.getCallParameters() != null) {
							currentReloadCacheKey = realCacheKey;
							int commandId = cachePlugin.getData(this, realCacheKey.getCallParameters());

							if (currentReloadCacheKey != null) {
								commandIdsToReloadedCacheKey.put(commandId, cacheKey);
								currentReloadCacheKey = null;
							}

						} else {
							Log.getInstance().log(Level.INFO, "Requested to reload cache key but at least one of plugin/parameters are null: " + cacheKey);
						}
					}
				}
			}

			cacheKeys = cachePlugin.updateCacheBasedOnEvent(event, cache);

			if (cacheKeys != null) {
				for (CacheKey cacheKey : cacheKeys) {
					for (CacheListener listener : broadcastListener) {
						callbackUpdate(command, cachePlugin, listener, cache.get(cacheKey));
					}
				}
			}

			cacheKeys = cachePlugin.getCacheKeysThatShouldBeDeletedBasedOnEvent(event, cache);

			if (cacheKeys != null) {
				for (CacheKey cacheKey : cacheKeys) {
					for (CacheListener listener : broadcastListener) {
						Object object = cache.get(cacheKey);
						cache.remove(cacheKey);
						callbackDeleted(command, cachePlugin, listener, cacheKey, object);
					}
				}
			}
		}
	}

	private synchronized void callbackUpdate(final Command command, final CachePlugin cachePlugin, final CacheListener cacheListener, final Object updatedObject) {
		if (!cacheListener.stillCallabled()) {
			return;
		}

		final Display display = cacheListener.getDisplay();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					if (!cacheListener.stillCallabled()) {
						return;
					}

					cachePlugin.callbackUpdated(cacheListener, command == null ? Command.UNDEFINED_ID : command.getClientUniqueId(), updatedObject);
					
					if (cacheListener instanceof GenericCacheObjectListener) {
						((GenericCacheObjectListener) cacheListener).objectUpdated(command == null ? Command.UNDEFINED_ID : command.getClientUniqueId(), updatedObject);
					}

				} catch (RuntimeException t) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", t);
					PotentialGraphicsMethodsShared.reportErrorToUser(display, t);
				}
			}
		};

		if (display == null || display == Display.findDisplay(Thread.currentThread())) {
			runnable.run();
		} else {
			display.asyncExec(runnable);
		}
	}

	private synchronized void callbackDeleted(final Command command, final CachePlugin cachePlugin, final CacheListener cacheListener, final CacheKey cacheKey, final Object deletedObject) {
		if (!cacheListener.stillCallabled()) {
			return;
		}

		final Display display = cacheListener.getDisplay();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					if (!cacheListener.stillCallabled()) {
						return;
					}

					cachePlugin.callbackDeleted(cacheListener, cacheKey);

					if (cacheListener instanceof GenericCacheObjectListener && deletedObject != null) {
						((GenericCacheObjectListener) cacheListener).objectDeleted(command == null ? Command.UNDEFINED_ID : command.getClientUniqueId(), deletedObject);
					}

				} catch (RuntimeException t) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", t);
					PotentialGraphicsMethodsShared.reportErrorToUser(display, t);
				}
			}
		};

		if (display == null || display == Display.findDisplay(Thread.currentThread())) {
			runnable.run();
		} else {
			display.asyncExec(runnable);
		}
	}

	public synchronized void addListener(CacheListener listener) {
		broadcastListener.add(listener);
	}

	public synchronized void removeListener(CacheListener listener) {
		broadcastListener.remove(listener);
	}

	class DatabaseCallParameters {
		int commandId;
		CacheKey cacheKey;
		CachePlugin cachePlugin;
		Object[] callParameters;
		ArrayList<GuiCallbackParameters> guiCallbackParameters = new ArrayList<GuiCallbackParameters>();

		DatabaseCallParameters(int commandId, CacheKey cacheKey, CachePlugin cachePlugin, Object[] callParameters) {
			this.cacheKey = cacheKey;
			this.cachePlugin = cachePlugin;
			this.callParameters = callParameters;
		}

		public void setCommandId(int commandId) {
			this.commandId = commandId;
		}

		public int getCommandId() {
			return commandId;
		}

		public CacheKey getCacheKey() {
			return cacheKey;
		}

		public CachePlugin getCachePlugin() {
			return cachePlugin;
		}

		public Object[] getCallParameters() {
			return callParameters;
		}

		public ArrayList<GuiCallbackParameters> getGuiCallbackParameters() {
			return guiCallbackParameters;
		}
	}

	class GuiCallbackParameters {
		Display display;
		CacheListener cacheListener;

		GuiCallbackParameters(Display display, CacheListener cacheListener) {
			this.display = display;
			this.cacheListener = cacheListener;
		}

		public Display getDisplay() {
			return display;
		}

		public CacheListener getCacheListener() {
			return cacheListener;
		}
	}
}
