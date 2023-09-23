/*
 * Copyright © 2016-2023 by Devon Bowen.
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

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;

import org.easotope.framework.Messages;
import org.easotope.framework.commands.Command;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.network.ObjSocket;
import org.easotope.framework.core.network.ObjSocketException;
import org.easotope.framework.core.network.ObjSocketListener;


public class ServerProcessor extends ThreadProcessor implements ObjSocketListener {
	private String host;
	private int port;
	private ObjSocket objSocket;
	private SocketStatus socketStatus = new SocketStatus();
	
	public ServerProcessor(String host, int port) {
		this.host = host;
		this.port = port;
	}

	public String getSource() {
		return host + ":" + port;
	}

	@Override
	protected boolean openConnection(boolean firstTimeOpened) {
		Socket socket;

		try {
			socket = new Socket(host, port);

		} catch (UnknownHostException e) {
			Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.serverProcessor_unknownHost, host), e);
			return false;

		} catch (IOException e) {
			Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.serverProcessor_ioException, host), e);
			return false;
		}

		try {
			objSocket = new ObjSocket(socket, false);
		} catch (IOException e) {
			Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.serverProcessor_ioException, host, port), e);
		}

		objSocket.addListener(this);
		new Thread(objSocket).start();

		synchronized (socketStatus) {
			while (!socketStatus.getOpen() && !socketStatus.getClosed()) {
				try {
					socketStatus.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}

		if (socketStatus.getClosed()) {
			closeConnection();
			return false;
		}

		return true;
	}

	protected void closeConnection() {
		if (objSocket != null) {
			objSocket.removeListener(this);
			objSocket.close();
			objSocket = null;
		}
	}

	protected Command executeCommand(Command command, Hashtable<String,Object> authenticationObjects) {
		if (socketStatus.getClosed()) {
			closeConnection();
			return null;
		}

		try {
			objSocket.writeObject(command);

		} catch (Exception e) {
			closeConnection();
			return null;
		}

		synchronized (socketStatus) {
			while (socketStatus.getCommand() == null && !socketStatus.getClosed()) {
				try {
					socketStatus.wait();
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}

		if (socketStatus.getClosed()) {
			closeConnection();
			return null;
		}

		Command reply = socketStatus.getCommand();
		socketStatus.setCommand(null);

		return reply;
	}

	protected void handleEvents(ArrayList<Event> events, Command command) {
		if (events == null) {
			return;
		}

		for (Event event : events) {
			distributeEventToListeners(event, command);
		}
	}

	public void objSocketConnected(ObjSocket objSocket) {
		synchronized (socketStatus) {
			socketStatus.markOpen();
			socketStatus.notify();
		}
	}

	public void objSocketReceivedObject(ObjSocket objSocket, Object object) {
		if (object instanceof Command) {
			synchronized (socketStatus) {
				socketStatus.setCommand((Command) object);
				socketStatus.notify();
			}

		} else if (object instanceof Event) {
			distributeEventToListeners((Event) object, null);
		}
	}

	@Override
	public void objSocketClosed(ObjSocket objSocket) {
		synchronized (socketStatus) {
			socketStatus.markClosed();
			socketStatus.notify();
		}
	}
	
	private class SocketStatus {
		private volatile boolean open = false;
		private volatile boolean closed = false;
		private volatile Command command = null;

		private void markOpen() {
			this.open = true;
		}

		private boolean getOpen() {
			return open;
		}

		private void markClosed() {
			this.closed = true;
		}

		private boolean getClosed() {
			return closed;
		}
		
		private void setCommand(Command command) {
			this.command = command;
		}
		
		private Command getCommand() {
			return command;
		}
	}

	@Override
	public void objSocketException(ObjSocket objSocket, ObjSocketException error) {
		Log.getInstance().log(Level.TERMINAL, this, error.getMessage(), error.getThrowable());
	}
}
