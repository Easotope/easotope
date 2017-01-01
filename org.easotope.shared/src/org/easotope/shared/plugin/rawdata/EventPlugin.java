/*
 * Copyright © 2016-2017 by Devon Bowen.
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

package org.easotope.shared.plugin.rawdata;

import java.util.ArrayList;

import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.events.Initialized;
import org.easotope.framework.dbcore.extensionpoint.definition.Plugin;
import org.easotope.framework.dbcore.util.RawFileManager;

import com.j256.ormlite.support.ConnectionSource;

public class EventPlugin implements Plugin {
	public ArrayList<Event> processEvent(ArrayList<Event> newEvents, ArrayList<Event> previousEvents, RawFileManager rawFileManager, ConnectionSource connectionSource) {
		ArrayList<Event> returnEvents = new ArrayList<Event>();

		for (Event event : newEvents) {
			if (event instanceof Initialized) {
				ArrayList<Event> events = InitializedHandler.execute((Initialized) event, rawFileManager, connectionSource);

				if (events != null) {
					returnEvents.addAll(events);
				}
			}
		}

		return returnEvents;
	}
}
