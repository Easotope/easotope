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

package org.easotope.shared.analysis.execute.dependency;

import java.util.HashMap;

import org.easotope.shared.core.cache.AbstractCache;

public abstract class DependencyPlugin {
	public enum PluginState { INIT, WAITING, VALID, NO_LONGER_VALID, ERROR };

	private String name;
	private int number;
	private PluginState pluginState = PluginState.INIT;
	private String message;

	protected DependencyPlugin(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}

	void setNumber(int number) {
		this.number = number;
	}

	public PluginState getState() {
		return pluginState;
	}

	void setState(PluginState state) {
		this.pluginState = state;
		this.message = null;
	}

	void setErrorStateAndMessage(String message) {
		this.pluginState = PluginState.ERROR;
		this.message = message;
	}

	public String getErrorMessage() {
		return message;
	}

	boolean isLoaded() {
		return pluginState != PluginState.INIT && pluginState != PluginState.WAITING;
	}

	/**
	 * If the plugin state is VALID then this method returns the value that
	 * was loaded by the plugin. If called in any other state, the return
	 * value is null.
	 * 
	 * @return the object loaded by the plugin
	 */
	public abstract Object getObject();

	/**
	 * If the plugin state is VALID then this method returns the value that
	 * was loaded formatted as a printable string. If called in any other state,
	 * the return value is null.
	 * 
	 * @return the object loaded by the plugin formatted as a string
	 */
	public abstract HashMap<String,String> getPrintableValues(DependencyManager dependencyManager);

	/**
	 * This method makes the initial call to the database cache to load the
	 * desired object. If the call did not return results immediately, the
	 * command ID of the request is returned.
	 * 
	 * @param dependencyManager
	 * @return the command id of the cache request or Command.UNDEFINED_ID if no request is outstanding
	 */
	public abstract int requestObject(DependencyManager dependencyManager);
	
	/**
	 * When the cache returns the requested object, this method is called
	 * to hand the object to the plugin. In complicated cases, it's possible
	 * that this object may trigger the loading of another object (chained
	 * loading). If so, the command ID of this new call is returned.
	 * 
	 * @param dependencyManager
	 * @param object
	 * @return the command id of the cache request or Command.UNDEFINED_ID if no request is outstanding
	 */
	public abstract int receivedObject(DependencyManager dependencyManager, Object object);

	/**
	 * When either requestObject() or receivedObject() has returned
	 * Command.UNDEFINED_ID, this means the plugin is no longer waiting on
	 * data from the database cache. This method is then called to decide
	 * whether the object we have is what we are looking for. It's possible,
	 * for example, that the data we were looking for does not exist or
	 * that it the cache returned an error.
	 * 
	 * @param dependencyManager
	 * @return true if the data we have is what we wanted
	 */
	public abstract boolean verifyCurrentObject(DependencyManager dependencyManager);

	/**
	 * Returns a list of all caches that this plugin needs to listen to in order
	 * to be informed when the loaded value has changed and needs to be invalidated.
	 * 
	 * @return array of cache objects
	 */
	public abstract AbstractCache[] getCachesToListenTo();

	/**
	 * If this plugin already has its object but an object update or delete
	 * event arrives from the cache, this method is called to see if the new object
	 * invalidates the old object.
	 * 
	 * @param dependencyManager
	 * @param object
	 * @return true if the new object invalidates the old one
	 */
	public abstract boolean isNoLongerValid(DependencyManager dependencyManager, Object object);
}
