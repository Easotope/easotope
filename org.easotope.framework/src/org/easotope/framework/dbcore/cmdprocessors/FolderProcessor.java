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

package org.easotope.framework.dbcore.cmdprocessors;

import java.io.File;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.Callable;

import org.easotope.framework.Messages;
import org.easotope.framework.commands.Command;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.Platform;
import org.easotope.framework.core.util.SystemProperty;
import org.easotope.framework.dbcore.Activator;
import org.easotope.framework.dbcore.events.CoreStartup;
import org.easotope.framework.dbcore.tables.Version;
import org.easotope.framework.dbcore.util.RawFileManager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

public class FolderProcessor extends ThreadProcessor {
	private boolean isServerMode;
	private String source;
	private String jdbcUrl;
	private RawFileManager rawFileManager;
	private boolean dbInitialized = false;
	private ConnectionSource connectionSource;

	public FolderProcessor(String source, boolean isServerMode, boolean createIfNecessary) {
		this.isServerMode = isServerMode;
		this.source = source;

		String jdbcCompatibleSource = Platform.isWindows() ? source.replace(File.separator, "/") : source;
		this.jdbcUrl = "jdbc:h2:" + jdbcCompatibleSource + "/" + SystemProperty.getDatabaseName() + ";USER=admin;PASSWORD=admin;WRITE_DELAY=0" + (createIfNecessary ? "" : ";IFEXISTS=TRUE");
	}

	@Override
	public String getSource() {
		return source;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	@Override
	protected boolean openConnection(boolean notReopeningAfterBackup) {
		Version version = null;

		try {
			rawFileManager = new RawFileManager(source + File.separator + "raw_files");
			connectionSource = new JdbcConnectionSource(jdbcUrl);

			Dao<Version,Integer> versionDao = DaoManager.createDao(connectionSource, Version.class);
			dbInitialized = versionDao.isTableExists(); // throws an exception if we couldn't open a database

			if (dbInitialized) {
				version = versionDao.queryForId(1);

				if (version.getLastServerVersion() > SystemProperty.getVersion()) {
					Log.getInstance().log(Level.TERMINAL, "Database version " + version.getLastServerVersion() + " is greater than this software version " + SystemProperty.getVersion());
					closeConnection();
					return false;
				}
			}

		} catch (Exception e) {
			Log.getInstance().log(isServerMode ? Level.TERMINAL : Level.INFO, this, "Error while opening " + jdbcUrl, e);
			closeConnection();
			return false;
		}

		ProcessorManager.getInstance().overrideProcessorForThread(new ImmediateProcessor(connectionSource, rawFileManager));

		if (notReopeningAfterBackup && dbInitialized) {
			ArrayList<Event> events = new ArrayList<Event>();
			events.add(new CoreStartup(isServerMode, version.getLastServerVersion()));
			Activator.distributeEventToPlugins(events, rawFileManager, connectionSource);
		}

		return true;
	}

	public static ConnectionSource createConnectionSource(String jdbcUrl) throws SQLException {
		return new JdbcConnectionSource(jdbcUrl);
	}

	@Override
	protected Command executeCommand(final Command command, final Hashtable<String,Object> authenticationObjects) {
		if (!dbInitialized && !(command instanceof CommandThatDoesNotRequireInitializedDb)) {
			command.setStatus(Command.Status.DB_ERROR, Messages.general_notInitialized);
			return command;
		}

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

		if (!dbInitialized) {
			try {
				Dao<Version,Integer> versionDao = DaoManager.createDao(connectionSource, Version.class);
				dbInitialized = versionDao.isTableExists(); // throws an exception if we couldn't open a database
			} catch (Exception e) {
				// ignore
			}
		}

		return command;
	}

	@Override
	protected void closeConnection() {
		if (connectionSource != null) {
			connectionSource.closeQuietly();
			connectionSource = null;
		}

		ProcessorManager.getInstance().overrideProcessorForThread(null);
	}

	@Override
	protected void handleEvents(ArrayList<Event> events, Command command) {
		if (events == null) {
			return;
		}

		ArrayList<Event> waterfallEvents = Activator.distributeEventToPlugins(events, rawFileManager, connectionSource);
		
		ArrayList<Event> allEvents = new ArrayList<Event>();
		allEvents.addAll(events);
		allEvents.addAll(waterfallEvents);

		for (Event event : allEvents) {
			distributeEventToListeners(event, command);
		}
	}
}
