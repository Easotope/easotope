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

package org.easotope.framework.dbcore.cmdprocessors;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.framework.core.logging.Log;

public class ProcessorManager {
	private static ProcessorManager instance = new ProcessorManager();

	private volatile Processor processor = null;
	private HashMap<Thread,Processor> override = new HashMap<Thread,Processor>();
	private HashSet<ProcessorManagerListener> listeners = new HashSet<ProcessorManagerListener>();

	public enum ProcessorTypes { SERVER, FOLDER };

	private ProcessorManager() { }

	public static ProcessorManager getInstance() {
		return instance;
	}

	public Processor getProcessor() {
		Processor result = override.get(Thread.currentThread());
		return (result != null) ? result : processor;
	}

	public void stopProcessor() {
		if (processor != null) {
			processor.requestStop();
		}
		
		processor = null;
	}

	public void installProcessor(Processor newProcessor, boolean alreadyStarted) {
		Log.getInstance().log(Log.Level.INFO, this, "Installing database as primary");
		
		if (processor != null) {
			processor.requestStop();
		}
		
		processor = newProcessor;

		synchronized (listeners) {
			for (ProcessorManagerListener listener: listeners) {
				processor.addListener(listener);
			}
		}

		if (!alreadyStarted && (processor instanceof ThreadProcessor)) {
			new Thread((ThreadProcessor) processor).start();
		}

		synchronized (listeners) {
			for (ProcessorManagerListener listener: listeners) {
				listener.newProcessor(processor);
			}
		}
	}

	public void overrideProcessorForThread(Processor processor) {
		override.put(Thread.currentThread(), processor);
	}

	public void addListener(ProcessorManagerListener listener) {
		boolean isNewListener = false;

		synchronized (listeners) {
			isNewListener = listeners.add(listener);
		}

		if (isNewListener && processor != null) {
			processor.addListener(listener);
		}
	}

	public void removeListener(ProcessorManagerListener listener) {
		boolean wasAlreadyListening = false;
		
		synchronized (listeners) {
			wasAlreadyListening = listeners.remove(listener);
		}

		if (wasAlreadyListening && processor != null) {
			processor.removeListener(listener);
		}
	}
}
