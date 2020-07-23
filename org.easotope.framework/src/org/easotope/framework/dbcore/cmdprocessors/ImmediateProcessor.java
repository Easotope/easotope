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
import java.util.concurrent.Callable;

import org.easotope.framework.Messages;
import org.easotope.framework.commands.Command;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.util.RawFileManager;

import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

public class ImmediateProcessor extends Processor {
	private ConnectionSource connectionSource;
	private RawFileManager rawFileManager;

	public ImmediateProcessor(ConnectionSource connectionSource, RawFileManager rawFileManager) {
		this.connectionSource = connectionSource;
		this.rawFileManager = rawFileManager;
	}

	public String getSource() {
		return "immediate";
	}

	public void process(Command command, Hashtable<String,Object> authenticationObjects, CommandListener listener) {
		command = execute(command, authenticationObjects);

		if (command == null || listener == null) {
			return;
		}

		ArrayList<Event> events = command.getAndRemoveEvents();

		if (events != null && events.size() != 0) {
			String message = MessageFormat.format(Messages.general_unexpectedEvents, command.getName());
			Log.getInstance().log(Level.INFO, message);
		}

		listener.commandExecuted(command);
	}

	private Command execute(final Command command, final Hashtable<String,Object> authenticationObjects) {
		boolean authenticatePassed = false;

		try {
			authenticatePassed = command.authenticate(connectionSource, rawFileManager, authenticationObjects);

		} catch (Exception e) {
			Throwable t = e;

			String message = t.getMessage();
			Log.getInstance().log(Log.Level.INFO, this, message, t);

			while (t.getCause() != null) {
				t = t.getCause();
				message += "\n" + t.getMessage();
			}

			command.setStatus(Command.Status.DB_ERROR, message);
			return command;
		}

		if (!authenticatePassed) {
			String message = MessageFormat.format(Messages.general_notPermitted, command.getName());
			command.setStatus(Command.Status.PERMISSION_ERROR, message);
			return command;
		}

		command.setStatus(Command.Status.OK);
		command.pauseBeforeExecute();

		try {
			TransactionManager.callInTransaction(connectionSource, new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					try {
						command.execute(connectionSource, rawFileManager, authenticationObjects);

					} catch (Exception e) {
						Throwable t = e;
	
						String message = t.getMessage();
						Log.getInstance().log(Log.Level.INFO, this, message, t);
	
						while (t.getCause() != null) {
							t = t.getCause();
							message += "\n" + t.getMessage();
						}
	
						command.setStatus(Command.Status.DB_ERROR, message);
						throw e;
					}

					return null;
				}
			});

		} catch (Exception e) {
			return command;
		}

		return command;
	}
}
