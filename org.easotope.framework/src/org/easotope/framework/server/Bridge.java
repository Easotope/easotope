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

package org.easotope.framework.server;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Hashtable;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.easotope.framework.Messages;
import org.easotope.framework.commands.Command;
import org.easotope.framework.commands.Command.Status;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.network.DiffieHellmanDES.MissingRemotePublicKey;
import org.easotope.framework.core.network.ObjSocket;
import org.easotope.framework.core.network.ObjSocketException;
import org.easotope.framework.core.network.ObjSocketListener;
import org.easotope.framework.dbcore.cmdprocessors.CommandListener;
import org.easotope.framework.dbcore.cmdprocessors.CommandThatSetsAuthenticationObjects;
import org.easotope.framework.dbcore.cmdprocessors.FolderProcessor;

public class Bridge implements CommandListener, ObjSocketListener {
	private Hashtable<String,Object> authenticationObjects = new Hashtable<String,Object>();
	private ObjSocket objSocket;
	private FolderProcessor processor;

	public Bridge(FolderProcessor processor) {
		this.processor = processor;
	}

	public void setObjSocket(ObjSocket objSocket) {
		this.objSocket = objSocket;
	}

	public Hashtable<String,Object> getAuthenticationObjects() {
		return authenticationObjects;
	}

	@Override
	public void commandExecuted(Command command) {
		if (command.getStatus() == Status.OK && command instanceof CommandThatSetsAuthenticationObjects) {
			CommandThatSetsAuthenticationObjects commandThatSetsAuthenticationObjects = (CommandThatSetsAuthenticationObjects) command;
			commandThatSetsAuthenticationObjects.updateAuthenticationObjects(authenticationObjects);
		}

//		if (events != null) {
//			for (Event event : events) {
//				if (event instanceof EventThatSetsAuthenticationObjects) {
//					EventThatSetsAuthenticationObjects eventThatSetsAuthenticationObjects = (EventThatSetsAuthenticationObjects) event; 
//					eventThatSetsAuthenticationObjects.updateAuthenticationObjects(authenticationObjects);
//				}
//			}
//		}

		try {
			objSocket.writeObject(command);

		} catch (IllegalBlockSizeException e) {
			Log.getInstance().log(Level.TERMINAL, this, Messages.bridge_terminalError, e);

		} catch (BadPaddingException e) {
			Log.getInstance().log(Level.TERMINAL, this, Messages.bridge_terminalError, e);

		} catch (IOException e) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.bridge_ioException, objSocket.getId()), e);

		} catch (MissingRemotePublicKey e) {
			Log.getInstance().log(Level.TERMINAL, this, Messages.bridge_terminalError, e);
		}
	}

	@Override
	public void objSocketConnected(ObjSocket objSocket) {
		// this should never happen on the server
	}

	@Override
	public void objSocketReceivedObject(ObjSocket objSocket, Object object) {
		Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.bridge_receivedObject, objSocket.getId()));		
		processor.process((Command) object, authenticationObjects, this);
		Log.getInstance().log(Level.DEBUG, this, Messages.bridge_objectGivenToProcessor);		
	}

	@Override
	public void objSocketClosed(ObjSocket objSocket) {
		Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.bridge_socketClosed, objSocket.getId()));		
		ObjSocketManager.getInstance().removeObjectSocket(objSocket);
	}

	@Override
	public void objSocketException(ObjSocket objSocket, ObjSocketException exception) {
		Log.getInstance().log(Level.INFO, this, Messages.bridge_terminalError, exception.getThrowable());		
	}
}
