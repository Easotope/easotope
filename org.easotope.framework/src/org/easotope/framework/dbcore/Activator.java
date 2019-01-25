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

package org.easotope.framework.dbcore;

import java.util.ArrayList;

import org.easotope.framework.Constants;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.extensionpoint.definition.Plugin;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.SafeRunner;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.j256.ormlite.support.ConnectionSource;

public class Activator implements BundleActivator {
	private static BundleContext context;

	public void start(BundleContext context) throws Exception {
		Activator.context = context;
	}

	public void stop(BundleContext context) throws Exception {
		Activator.context = null;
	}

	private static IExtensionRegistry getExtensionRegistry() {
		ServiceReference<?> ref = context.getServiceReference(IExtensionRegistry.class.getName());
		IExtensionRegistry reg = (IExtensionRegistry) context.getService(ref);
		context.ungetService(ref);
		return reg;
	}

	public static ArrayList<Event> distributeEventToPlugins(ArrayList<Event> currentEvent, RawFileManager rawFileManager, ConnectionSource connectionSource) {
		ArrayList<Event> waterfallEvents = new ArrayList<Event>();

		IExtensionRegistry registry = Activator.getExtensionRegistry();
		IConfigurationElement[] pluginConfigs = registry.getConfigurationElementsFor(Constants.PLUGIN_ID);

		@SuppressWarnings("unchecked")
		ArrayList<Event>[] newEvents = new ArrayList[pluginConfigs.length];
		@SuppressWarnings("unchecked")
		ArrayList<Event>[] previousEvents = new ArrayList[pluginConfigs.length];

		for (int i=0; i<pluginConfigs.length; i++) {
			newEvents[i] = new ArrayList<Event>();
			newEvents[i].addAll(currentEvent);
			previousEvents[i] = new ArrayList<Event>();
		}

		boolean eventsWereDelivered;

		do {
			eventsWereDelivered = false;

			for (int i=0; i<newEvents.length; i++) {
				if (!newEvents[i].isEmpty()) {
					Object object = null;

					try {
						IConfigurationElement configElement = pluginConfigs[i];
						object = configElement.createExecutableExtension("class");
					} catch (CoreException ex) {
						// do nothing
					}

					if (object instanceof Plugin) {
						final Plugin plugin = (Plugin) object;
						PluginRunnable runnable = new PluginRunnable(plugin, newEvents[i], previousEvents[i], rawFileManager, connectionSource);
						SafeRunner.run(runnable);
						ArrayList<Event> returnValue = runnable.getReturnValue();

						previousEvents[i].addAll(newEvents[i]);
						newEvents[i].clear();

						for (int j=0; j<newEvents.length; j++) {
							if (returnValue != null) {
								newEvents[i].addAll(returnValue);
							}
						}

						if (returnValue != null) {
							waterfallEvents.addAll(returnValue);
						}
						
						eventsWereDelivered = true;
					}
				}
			}
		} while (eventsWereDelivered);
		
		return waterfallEvents;
	}
}
