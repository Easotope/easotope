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

package org.easotope.shared.analysis.execute.dependency;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.easotope.framework.commands.Command;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.shared.Messages;
import org.easotope.shared.analysis.execute.StepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin.PluginState;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.GenericCacheObjectListener;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.eclipse.swt.widgets.Display;

public abstract class DependencyManager implements GenericCacheObjectListener {
	private Display display;
	private ReplicateV1 replicate;
	private StepCalculator stepCalculator;
	private boolean isNoLongerValid = false;
	private boolean hasErrors = false;
	private boolean isDisposed = false;
	private Vector<DependencyPlugin> dependencyPlugins = new Vector<DependencyPlugin>();
	private ArrayList<DependencyPlugin> currentDependencyPluginStack = new ArrayList<DependencyPlugin>();
	private OutstandingRequests outstandingRequests = new OutstandingRequests();
	private HashSet<AbstractCache> abstractCaches = new HashSet<AbstractCache>();
	private Vector<DependencyManagerListener> listeners = new Vector<DependencyManagerListener>();

	protected DependencyManager() {
		this.display = Display.findDisplay(Thread.currentThread());
		currentDependencyPluginStack.add(null);
	}

	public void dispose() {
		Log.getInstance().log(Level.DEBUG, this, "disposing");

		replicate = null;
		display = null;
		outstandingRequests.clear();

		for (AbstractCache abstractCache : abstractCaches) {
			abstractCache.removeListener(this);
		}

		abstractCaches.clear();
		listeners.clear();
		isDisposed = true;
	}

	protected void addPlugin(DependencyPlugin plugin) {
		plugin.setNumber(dependencyPlugins.size());
		dependencyPlugins.add(plugin);
	}

	public Vector<DependencyPlugin> getDependencyPlugins() {
		return dependencyPlugins;
	}

	public ReplicateV1 getReplicate() {
		return replicate;
	}

	public StepCalculator getStepCalculator() {
		return stepCalculator;
	}

	public void execute(ReplicateV1 replicate, StepCalculator stepCalculator) {
		Log.getInstance().log(Level.DEBUG, this, "setting and executing");

		this.replicate = replicate;
		this.stepCalculator = stepCalculator;

		for (DependencyPlugin plugin : dependencyPlugins) {
			AbstractCache[] caches = plugin.getCachesToListenTo();
			
			if (caches != null) {
				for (AbstractCache abstractCache : caches) {
					abstractCaches.add(abstractCache);
				}
			}
		}

		for (AbstractCache abstractCache : abstractCaches) {
			abstractCache.addListener(this);
		}

		for (DependencyPlugin plugin : dependencyPlugins) {
			requestObject(plugin);
		}

		if (allDependenciesAreLoaded()) {
			Log.getInstance().log(Level.DEBUG, this, "all dependencies were immediately loaded");
			broadcastDependenciesLoaded();
		} else {
			Log.getInstance().log(Level.DEBUG, this, "dependencies have been requested but still waiting on " + outstandingRequests.size() + " requests");
		}
	}

	private void requestObject(DependencyPlugin plugin) {
		plugin.setState(PluginState.WAITING);

		currentDependencyPluginStack.add(0, plugin);
		int resultingCommandId = plugin.requestObject(this);
		currentDependencyPluginStack.remove(0);

		handleResult(resultingCommandId, plugin);
	}

	@Override
	public void objectGetCompleted(int commandId, Object object) {
		DependencyPlugin plugin = outstandingRequests.containsKey(commandId) ? outstandingRequests.remove(commandId) : currentDependencyPluginStack.get(0);
		assert(plugin != null);

		currentDependencyPluginStack.add(0, plugin);
		int resultingCommandId = plugin.receivedObject(this, object);
		currentDependencyPluginStack.remove(0);

		handleResult(resultingCommandId, plugin);

		if (commandId != Command.UNDEFINED_ID && allDependenciesAreLoaded()) {
			broadcastDependenciesLoaded();
		}
	}

	private void handleResult(int resultingCommandId, DependencyPlugin plugin) {
		if (resultingCommandId != Command.UNDEFINED_ID) {
			Log.getInstance().log(Level.DEBUG, this, "handleResult(): load of plugin " + plugin.getName() + " is pending");
			outstandingRequests.put(resultingCommandId, plugin);
			return;
		}

		if (plugin.getState() != PluginState.WAITING) {
			// the result must have already been handled by a deeper recursion
			return;
		}

		if (plugin.verifyCurrentObject(this)) {
			Log.getInstance().log(Level.DEBUG, this, "handleResult(): plugin " + plugin.getName() + " data is valid");
			plugin.setState(PluginState.VALID);

		} else if (!outstandingRequests.containsValue(plugin)) {
			Log.getInstance().log(Level.DEBUG, this, "handleResult(): plugin " + plugin.getName() + " could not load data");
			String message = MessageFormat.format(Messages.dependencyManager_couldNotLoad, plugin.getName());
			plugin.setErrorStateAndMessage(message);
			hasErrors = true;
		}
	}

	@Override
	public void objectUpdated(int commandId, Object updateObject) {
		boolean atLeastOneWasAffected = false;

		for (DependencyPlugin plugin : dependencyPlugins) {
			if (plugin.getState() == PluginState.VALID && plugin.isNoLongerValid(this, updateObject)) {
				Log.getInstance().log(Level.DEBUG, this, "plugin " + plugin.getName() + " data is no longer valid");
				plugin.setState(PluginState.NO_LONGER_VALID);
				atLeastOneWasAffected = true;
			}
		}

		if (allDependenciesAreLoaded() && atLeastOneWasAffected && !isNoLongerValid) {
			isNoLongerValid = true;
			broadcastDependenciesNoLongerValid();
		}
	}

	@Override
	public void objectDeleted(int commandId, Object object) {
		objectUpdated(commandId, object);
	}

	@Override
	public void objectGetError(int commandId, String message) {
		DependencyPlugin plugin = outstandingRequests.containsKey(commandId) ? outstandingRequests.remove(commandId) : currentDependencyPluginStack.get(0);
		assert(plugin != null);

		Log.getInstance().log(Level.DEBUG, this, "plugin " + plugin.getName() + " received error from cache");
		plugin.setErrorStateAndMessage(message);
		hasErrors = true;

		if (commandId != Command.UNDEFINED_ID && allDependenciesAreLoaded()) {
			broadcastDependenciesLoaded();
		}
	}

	public boolean allDependenciesAreLoaded() {
		return outstandingRequests.isEmpty();
	}

	public boolean allDependenciesAreValid() {
		return allDependenciesAreLoaded() && !isNoLongerValid && !hasErrors;
	}

	public boolean isNoLongerValid() {
		return isNoLongerValid;
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	private void broadcastDependenciesLoaded() {
		Log.getInstance().log(Level.DEBUG, this, "broadcasting dependencies loaded " + allDependenciesAreValid() + " " + isNoLongerValid + " " + hasErrors);

		if (hasErrors) {
			for (DependencyPlugin plugin : dependencyPlugins) {
				if (plugin.getState() == PluginState.ERROR) {
					Log.getInstance().log(Level.DEBUG, this, "plugin " + plugin.getName() + " has errors");
				}
			}
		}

		for (DependencyManagerListener listener : new ArrayList<DependencyManagerListener>(listeners)) {
			listener.dependenciesReady(DependencyManager.this);
		}
	}

	private void broadcastDependenciesNoLongerValid() {
		Log.getInstance().log(Level.DEBUG, this, "broadcasting dependencies no longer valid");

		for (DependencyManagerListener listener : new ArrayList<DependencyManagerListener>(listeners)) {
			listener.dependenciesNoLongerValid(DependencyManager.this);
		}
	}

	public void addListener(DependencyManagerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(DependencyManagerListener listener) {
		listeners.remove(listener);
	}

	@Override
	public Display getDisplay() {
		return display;
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed;
	}

	class OutstandingRequests {
		private HashMap<Integer,ArrayList<DependencyPlugin>> requests = new HashMap<Integer,ArrayList<DependencyPlugin>>();

		public int size() {
			int total = 0;

			for (ArrayList<DependencyPlugin> list : requests.values()) {
				total += list.size();
			}

			return total;
		}

		public void clear() {
			requests.clear();
		}

		public boolean isEmpty() {
			return requests.isEmpty();
		}

		public boolean containsValue(DependencyPlugin plugin) {
			for (ArrayList<DependencyPlugin> list : requests.values()) {
				if (list.contains(plugin)) {
					return true;
				}
			}

			return false;
		}

		public void put(int commandId, DependencyPlugin plugin) {			
			ArrayList<DependencyPlugin> list = requests.get(commandId);

			if (list == null) {
				list = new ArrayList<DependencyPlugin>();
				requests.put(commandId, list);
			}

			list.add(plugin);
		}

		public DependencyPlugin remove(int commandId) {
			ArrayList<DependencyPlugin> list = requests.get(commandId);

			if (list == null) {
				return null;
			}

			DependencyPlugin result = list.remove(0);

			if (list.isEmpty()) {
				requests.remove(commandId);
			}

			return result;
		}

		public boolean containsKey(int commandId) {
			return requests.containsKey(commandId);
		}
	}
}
