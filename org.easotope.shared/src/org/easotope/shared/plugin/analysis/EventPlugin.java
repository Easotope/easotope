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

package org.easotope.shared.plugin.analysis;

import java.util.ArrayList;

import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.events.CoreStartup;
import org.easotope.framework.dbcore.events.Initialized;
import org.easotope.framework.dbcore.extensionpoint.definition.Plugin;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcAll;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcById;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcByTime;
import org.easotope.shared.analysis.events.CorrIntervalCompUpdated;
import org.easotope.shared.analysis.events.RepAnalysisChoiceUpdated;
import org.easotope.shared.plugin.analysis.initializedhandler.InitializedHandler;
import org.easotope.shared.rawdata.events.ReplicateDeleted;
import org.easotope.shared.rawdata.events.ReplicateUpdated;
import org.easotope.shared.rawdata.events.SampleUpdated;

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

			if (event instanceof CoreStartup) {				
				ArrayList<Event> events = CoreStartupHandler.execute((CoreStartup) event, rawFileManager, connectionSource);

				if (events != null) {
					returnEvents.addAll(events);
				}
			}

			if (event instanceof CorrIntervalsNeedRecalcAll) {
				ArrayList<Event> events = new CorrIntervalsNeedRecalcAllHandler().execute((CorrIntervalsNeedRecalcAll) event, rawFileManager, connectionSource);
	
				if (events != null) {
					returnEvents.addAll(events);
				}
			}

			if (event instanceof CorrIntervalsNeedRecalcById) {
				ArrayList<Event> events = new CorrIntervalsNeedRecalcByIdHandler().execute((CorrIntervalsNeedRecalcById) event, rawFileManager, connectionSource);
	
				if (events != null) {
					returnEvents.addAll(events);
				}
			}

			if (event instanceof CorrIntervalsNeedRecalcByTime) {
				ArrayList<Event> events = new CorrIntervalsNeedRecalcByTimeHandler().execute((CorrIntervalsNeedRecalcByTime) event, rawFileManager, connectionSource);
	
				if (events != null) {
					returnEvents.addAll(events);
				}
			}

			if (event instanceof CorrIntervalCompUpdated) {
				ArrayList<Event> events = new CorrIntervalCompUpdatedHandler().execute((CorrIntervalCompUpdated) event, rawFileManager, connectionSource);

				if (events != null) {
					returnEvents.addAll(events);
				}
			}

			if (event instanceof ReplicateUpdated) {
				ArrayList<Event> events = new ReplicateUpdatedHandler().execute((ReplicateUpdated) event, rawFileManager, connectionSource);
				
				if (events != null) {
					returnEvents.addAll(events);
				}
			}

			if (event instanceof ReplicateDeleted) {
				ArrayList<Event> events = new ReplicateDeletedHandler().execute((ReplicateDeleted) event, rawFileManager, connectionSource);
				
				if (events != null) {
					returnEvents.addAll(events);
				}
			}

			if (event instanceof RepAnalysisChoiceUpdated) {
				ArrayList<Event> events = new RepAnalysisChoiceUpdatedHandler().execute((RepAnalysisChoiceUpdated) event, rawFileManager, connectionSource);

				if (events != null) {
					returnEvents.addAll(events);
				}
			}

			if (event instanceof SampleUpdated) {
				ArrayList<Event> events = new SampleUpdatedHandler().execute((SampleUpdated) event, rawFileManager, connectionSource);
				
				if (events != null) {
					returnEvents.addAll(events);
				}
			}
		}

		return returnEvents;
	}
}
