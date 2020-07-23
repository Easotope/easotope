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

package org.easotope.framework.dbcore.cmdprocessors;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;

import org.easotope.framework.Messages;
import org.easotope.framework.commands.Command;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;

public abstract class ThreadProcessor extends Processor implements Runnable {
	private ArrayList<CommandPacket> commandQueue = new ArrayList<CommandPacket>();

	private CountDownLatch pausedLatch;
	private CountDownLatch resumeLatch;

	abstract protected boolean openConnection(boolean firstTimeOpened);
	abstract protected void closeConnection();
	abstract protected Command executeCommand(Command command, Hashtable<String,Object> authenticationObjects);
	abstract protected void handleEvents(ArrayList<Event> events, Command command);

	public ThreadProcessor() { }

	public void run() {
		long delay = 10000; // milliseconds

		Log.getInstance().log(Level.INFO, getName(), Messages.processor_connecting);

		while (!openConnection(true)) {
			Log.getInstance().log(Level.INFO, getName(), MessageFormat.format(Messages.processor_noConnection, (delay / 1000)));

			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// do nothing
			}

			if (isStopping()) {
				closeConnection();
				setStopping(false);
				notifyStatusChanged();
				Log.getInstance().log(Level.INFO, getName(), Messages.processor_shutDown);
				return;
			}
		}

		setConnected(true);
		notifyStatusChanged();
		Log.getInstance().log(Level.INFO, getName(), Messages.processor_connected);

		CommandPacket commandPacket = null;

		while ((commandPacket = getCommandPacket()) != null) {
			Command command = commandPacket.command;
			Command reply = null;
			Hashtable<String,Object> authenticationObjects = commandPacket.authenticationObjects;
			CommandListener listener = commandPacket.listener;
			
			Log.getInstance().log(Level.INFO, getName(), MessageFormat.format(Messages.processor_executing, command.getName()));

			reply = executeCommand(command, authenticationObjects);
			
			if (reply == null) {
				notifyConnectionDropped();
				setConnected(false);
				notifyStatusChanged();
				return;
			}

			Command.Status status = reply.getStatus();

			switch (status) {
				case NONE:
					break;

				case DB_ERROR:
					incNumDbErrors();
					break;

				case PERMISSION_ERROR:
					incNumPermissionErrors();
					break;

				case EXECUTION_ERROR:
					incNumExecutionErrors();
					break;

				case VERIFY_AND_RESEND:
					incNumVerifyAndResend();

				case OK:
					incNumCommandsExecuted();
					break;
			}

			if (status != Command.Status.OK) {
				Log.getInstance().log(Level.INFO, getName(), MessageFormat.format(Messages.processor_returnedStatus, status, reply.getMessage()));
			}

			if (status == Command.Status.DB_ERROR) {
				notifyDatabaseError(reply.getMessage());
			}

			ArrayList<Event> events = command.getAndRemoveEvents();

			if (listener != null) {
				listener.commandExecuted(reply);
			}

			handleEvents(events, command);

			notifyStatusChanged();
		}

		closeConnection();
		setConnected(false);
		notifyStatusChanged();
		Log.getInstance().log(Level.INFO, getName(), Messages.processor_shutDown);
	}

	public void process(Command command, Hashtable<String,Object> authenticationObjects, CommandListener listener) {
		synchronized (commandQueue) {
			commandQueue.add(new CommandPacket(command, authenticationObjects, listener));
			commandQueue.notify();
		}
	}

	public void requestStop() {
		Log.getInstance().log(Level.INFO, getName(), Messages.processor_stopRequested);

		synchronized (commandQueue) {
			setStopping(true);
			commandQueue.notify();
		}

		notifyStatusChanged();
	}

	public void pause() {
		Log.getInstance().log(Level.INFO, getName(), Messages.processor_pauseRequested);
		
		pausedLatch = new CountDownLatch(1);
		resumeLatch = new CountDownLatch(1);

		synchronized (commandQueue) {
			setPausing(true);
			commandQueue.notify();
		}
		
		notifyStatusChanged();

		try {
			pausedLatch.await();
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	public void resume() {
		Log.getInstance().log(Level.INFO, getName(), Messages.processor_resumeRequested);
		resumeLatch.countDown();
	}

	private CommandPacket getCommandPacket() {
		Log.getInstance().log(Level.DEBUG, this, Messages.processor_lookingForCommand);

		while (true) {
			if (isStopping()) {
				return null;
			}

			if (isPausing()) {
				closeConnection();

				Log.getInstance().log(Level.INFO, this, Messages.processor_paused);

				setConnected(false);
				setPausing(false);
				setPaused(true);
				notifyStatusChanged();

				pausedLatch.countDown();

				try {
					resumeLatch.await();
				} catch (InterruptedException e) {
					// do nothing
				}

				setPaused(false);

				if (isStopping()) {
					return null;
				}

				Log.getInstance().log(Level.INFO, this, Messages.processor_resuming);

				if (!openConnection(false)) {
					Log.getInstance().log(Level.TERMINAL, this, Messages.processor_unableToReconnect);
				}

				setConnected(true);
				notifyStatusChanged();
			}

			synchronized (commandQueue) {
				if (commandQueue.size() != 0) {
					Log.getInstance().log(Level.DEBUG, this, Messages.processor_returningCommand);
					return commandQueue.remove(0);
				}

				try {
					Log.getInstance().log(Level.DEBUG, this, Messages.processor_waitingForCommand);
					commandQueue.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}

	private class CommandPacket {
		private Command command;
		private Hashtable<String,Object> authenticationObjects;
		private CommandListener listener;
		
		private CommandPacket(Command command, Hashtable<String,Object> authenticationObjects, CommandListener listener) {
			this.command = command;
			this.authenticationObjects = authenticationObjects;
			this.listener = listener;
		}
	}
}
