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

package org.easotope.framework.server;

import java.io.IOException;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.easotope.framework.Messages;
import org.easotope.framework.commands.Command;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.network.DiffieHellmanDES.MissingRemotePublicKey;
import org.easotope.framework.core.network.ObjSocket;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.EventListener;
import org.easotope.framework.dbcore.cmdprocessors.EventThatSetsAuthenticationObjects;
import org.easotope.framework.dbcore.cmdprocessors.FolderProcessor;

public class ObjSocketManager implements EventListener {
	private static ObjSocketManager instance = new ObjSocketManager();
	private HashSet<ObjSocket> objSockets = new HashSet<ObjSocket>();
	private HashMap<ObjSocket,Bridge> objSocketToBridge = new HashMap<ObjSocket,Bridge>();

	private ObjSocketManager() { }

	public static ObjSocketManager getInstance() {
		return instance;
	}

	public synchronized void createObjSocket(Socket socket, FolderProcessor processor) {
		Bridge bridge;
		ObjSocket objSocket;

		// processors automatically ignore duplicate listeners
		processor.addEventListener(this);

		try {
			bridge = new Bridge(processor);
			objSocket = new ObjSocket(socket);

			objSocket.addListener(bridge);
			bridge.setObjSocket(objSocket);

			new Thread(objSocket).start();

		} catch (Exception exception) {
			Log.getInstance().log(Level.INFO, this, Messages.objSocketManager_threadCreateFailed, exception);
			return;
		}

		objSockets.add(objSocket);
		objSocketToBridge.put(objSocket, bridge);
	}

	public synchronized void removeObjectSocket(ObjSocket objSocket) {
		if (!objSockets.remove(objSocket)) {
			Log.getInstance().log(Level.INFO, this, Messages.objSocketManager_noSuchThread);
		}

		objSocketToBridge.remove(objSockets);
	}

	@Override
	public void eventReceived(Event event, Command command) {
		for (ObjSocket objSocket : new HashSet<ObjSocket>(objSockets)) {
			Bridge bridge = objSocketToBridge.get(objSocket);

			if (event instanceof EventThatSetsAuthenticationObjects) {
				EventThatSetsAuthenticationObjects eventThatSetsAuthenticationObjects = (EventThatSetsAuthenticationObjects) event; 
				Hashtable<String,Object> authenticationObjects = bridge.getAuthenticationObjects();
				eventThatSetsAuthenticationObjects.updateAuthenticationObjects(authenticationObjects);
			}

			if (bridge.getAuthenticationObjects().isEmpty()) {
				continue;
			}

			if (event.isAuthorized(bridge.getAuthenticationObjects())) {
				try {
					objSocket.writeObject(event);

				} catch (IllegalBlockSizeException e) {
					Log.getInstance().log(Level.TERMINAL, this, Messages.objSocketManager_terminalError, e);

				} catch (BadPaddingException e) {
					Log.getInstance().log(Level.TERMINAL, this, Messages.objSocketManager_terminalError, e);

				} catch (IOException e) {
					Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocketManager_ioException, objSocket.getId()), e);

				} catch (MissingRemotePublicKey e) {
					Log.getInstance().log(Level.TERMINAL, this, Messages.objSocketManager_terminalError, e);
				}
			}
		}
	}
}
