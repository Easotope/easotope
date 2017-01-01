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

package org.easotope.framework.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;

import org.easotope.framework.Messages;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.logging.LogTerminateListener;
import org.easotope.framework.core.util.TopDir;
import org.easotope.framework.dbcore.cmdprocessors.FolderProcessor;
import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorListener;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class Server implements IApplication, ProcessorListener, LogTerminateListener {
	private static final int SEND_BUFFER_SIZE = 2 * 1024 * 1024;

	private volatile boolean shuttingDown = false;
	private ServerSocket serverSocket = null;
	private FolderProcessor processor = null;
	private BackupManager backupManager = null;
	private CountDownLatch shutdownComplete = new CountDownLatch(1);

	@Override
	public Object start(IApplicationContext context) throws Exception {
		try {
			return startCode(context);
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, this, "Server exiting due to exception.", e);
			throw e;
		}
	}

	private Object startCode(IApplicationContext context) throws Exception {
		String pathToTopDir = null;

		try {
			pathToTopDir = TopDir.getPathToTopDir();
		} catch (Exception e) {
			// ignore
		}

		if (pathToTopDir != null) {
			Log.getInstance().openLogFile(pathToTopDir);
		}

		Log.getInstance().log(Level.INFO, "Java version " + System.getProperty("java.version"));

		Log.getInstance().setServerMode(true);
		Log.getInstance().addLogTerminateListener(this);

		String[] appArgs = (String[]) context.getArguments().get("application.args");

		if ((appArgs == null || appArgs.length == 0) && pathToTopDir != null) {
			appArgs = new String[] { "-properties", pathToTopDir + File.separator + "args.properties" };
		}

		Arguments serverArgs = new Arguments(Server.class.getSimpleName(), appArgs);

		if (serverArgs.isInvalid()) {
			return EXIT_OK;
		}

		if (serverArgs.isDebug()) {
			Log.getInstance().setProcessingLevel(Level.DEBUG);
		}

		processor = new FolderProcessor(serverArgs.getDbDir(), true, true);
		processor.addListener(this);
		ProcessorManager.getInstance().installProcessor(processor, false);

		Runtime.getRuntime().addShutdownHook(new Shutdown(this));

		if (serverArgs.getBackupDir() != null && serverArgs.getBackupTimes() != null) {
			backupManager = new BackupManager(serverArgs.getDbDir(), serverArgs.getBackupDir(), serverArgs.getBackupTimes(), serverArgs.getMaxBackups(), processor);
		}

		final long delay = 60000;

		while (true) {
			listen(serverArgs, processor);

			if (shuttingDown) {
				shutdownComplete.countDown();
				Log.getInstance().log(Level.INFO, Server.class, Messages.server_exiting);
				return EXIT_OK;
			}

			Log.getInstance().log(Level.INFO, Server.class, MessageFormat.format(Messages.server_listenFailed, delay/1000));

			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	private void listen(Arguments tcpServerArgs, FolderProcessor processor) {
		try {
			serverSocket = new ServerSocket(tcpServerArgs.getPort());
		} catch (IOException e) {
			Log.getInstance().log(Level.INFO, Server.class, MessageFormat.format(Messages.server_socketOpenError, tcpServerArgs.getPort(), e));
			return;
		}

		while (true) {
			Socket socket;

			try {
				socket = serverSocket.accept();
				socket.setSendBufferSize(SEND_BUFFER_SIZE);
			} catch (IOException e) {
				Log.getInstance().log(Level.INFO, Server.class, MessageFormat.format(Messages.server_acceptError, tcpServerArgs.getPort(), e));
				return;
			}

			ObjSocketManager.getInstance().createObjSocket(socket, processor);
		}
	}

	@Override
	public void stop() {
		shutdown();
	}

	public void shutdown() {
		Log.getInstance().log(Level.INFO, Server.class, Messages.server_requestingStop);

		shuttingDown = true;
		processor.requestStop();

		if (backupManager != null) {
			backupManager.requestStop();
		}

		try {
			shutdownComplete.await();
		} catch (InterruptedException e) {
			// do nothing
		}
	}

	@Override
	public void processorStatusChanged(Processor processor) {
		if (!processor.isConnected()) {
			Log.getInstance().log(Level.INFO, Server.class, Messages.server_processorStopped);

			try {
				serverSocket.close();
			} catch (IOException e) {
				Log.getInstance().log(Level.TERMINAL, Server.class, Messages.server_errorClosingSocket, e);
			}
		}
	}

	@Override
	public void processorConnectionDropped(Processor processor) {
		// do nothing
	}

	@Override
	public void processorDatabaseError(Processor processor, String message) {
		// do nothing
	}

	@Override
	public void logTerminate(String timestamp, String source, String message, String stackTrace) {
		shutdown();
	}
}
