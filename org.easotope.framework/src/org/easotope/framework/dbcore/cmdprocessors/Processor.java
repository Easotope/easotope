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

package org.easotope.framework.dbcore.cmdprocessors;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Hashtable;

import org.easotope.framework.Messages;
import org.easotope.framework.commands.Command;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;

public abstract class Processor {
	private static int counter = 0;

	private String name = null;

	private volatile boolean connected = false;
	private volatile boolean stopping = false;
	private volatile boolean pausing = false;
	private volatile boolean paused = false;

	private volatile int numCommandsExecuted = 0;
	private volatile int numDbErrors = 0;
	private volatile int numPermissionErrors = 0;
	private volatile int numExecutionErrors = 0;
	private volatile int numLocalEvents = 0;
	private volatile int numRemoteEvents = 0;

	private HashSet<ProcessorListener> processorListeners = new HashSet<ProcessorListener>();
	private HashSet<EventListener> eventListeners = new HashSet<EventListener>();
	
	public Processor() {
		name = "DB" + getUniqueNumber();
	}
	
	private static synchronized int getUniqueNumber() {
		return counter++;
	}

	public abstract String getSource();
	public abstract void process(Command command, Hashtable<String,Object> authenticationObjects, CommandListener listener);

	public void requestStop() { }
	public void pause() { }
	public void resume() { }

	public String getName() {
		return name;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setStopping(boolean stopping) {
		this.stopping = stopping;
	}

	public boolean isStopping() {
		return stopping;
	}

	public void setPausing(boolean pausing) {
		this.pausing = pausing;
	}

	public boolean isPausing() {
		return pausing;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isPaused() {
		return paused;
	}

	public void incNumDbErrors() {
		numDbErrors++;
	}

	public int getNumDbErrors() {
		return numDbErrors;
	}

	public void incNumPermissionErrors() {
		numPermissionErrors++;
	}

	public int getNumPermissionErrors() {
		return numPermissionErrors;
	}

	public void incNumExecutionErrors() {
		numExecutionErrors++;
	}

	public int getNumExecutionErrors() {
		return numExecutionErrors;
	}

	public void incNumCommandsExecuted() {
		numCommandsExecuted++;
	}

	public int getNumCommandsExecuted() {
		return numCommandsExecuted;
	}
	
	public int getNumLocalEvents() {
		return numLocalEvents;
	}

	public int getNumRemoteEvents() {
		return numRemoteEvents;
	}
	
	public void addListener(ProcessorListener listener) {
		synchronized (processorListeners) {
			processorListeners.add(listener);
		}
	}

	public void removeListener(ProcessorListener listener) {
		synchronized (processorListeners) {
			processorListeners.remove(listener);
		}
	}

	protected void notifyConnectionDropped() {
		Log.getInstance().log(Level.INFO, name, Messages.processor_connectionDropped);

		synchronized (processorListeners) {
			for (ProcessorListener listener : processorListeners) {
				listener.processorConnectionDropped(this);
			}
		}
	}

	protected void notifyDatabaseError(String message) {
		Log.getInstance().log(Level.INFO, name, MessageFormat.format(Messages.processor_dbError, message));

		synchronized (processorListeners) {
			for (ProcessorListener listener : processorListeners) {
				listener.processorDatabaseError(this, message);
			}
		}
	}

	protected void notifyStatusChanged() {
		synchronized (processorListeners) {
			for (ProcessorListener listener : processorListeners) {
				listener.processorStatusChanged(this);
			}
		}
	}
	
	public void addEventListener(EventListener listener) {
		synchronized (eventListeners) {
			eventListeners.add(listener);
		}
	}

	public void removeEventListener(EventListener listener) {
		synchronized (eventListeners) {
			eventListeners.remove(listener);
		}
	}

	public void distributeEventToListeners(Event event, Command command) {
		boolean needToUpdateStatus = false;

		if (command == null) {
			needToUpdateStatus = true;
			numRemoteEvents++;
			Log.getInstance().log(Level.INFO, name, MessageFormat.format(Messages.processor_receivedEvent, event.getName()));
		} else {
			numLocalEvents++;
			Log.getInstance().log(Level.INFO, name, MessageFormat.format(Messages.processor_receivedEventWithCommand, event.getName(), command.getName()));
		}

		synchronized (eventListeners) {
			for (EventListener eventListener : eventListeners) {
				eventListener.eventReceived(event, command);
			}
		}

		if (needToUpdateStatus) {
			notifyStatusChanged();
		}
	}
}
