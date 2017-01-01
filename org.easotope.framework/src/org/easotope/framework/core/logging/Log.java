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

package org.easotope.framework.core.logging;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import org.easotope.framework.Messages;

import com.j256.ormlite.logger.LocalLog;

public class Log {
	final String LOG_FILE_NAME_PREFIX = "log_";
	final String ORM_LOG_FILE_NAME_PREFIX = "orm_log_";
	final String LOG_FILE_NAME_POSTFIX = ".txt";
	final int MAX_NUM_LOG_FILES = 10;

	public enum Level { TERMINAL, INFO, DEBUG };

	private PrintStream printStream = System.err;
	private static Log instance = new Log();
	private boolean serverMode = false;
	private Level processingLevel = Level.INFO;
	private int bufferLength = 100;
	private ArrayList<String> history = new ArrayList<String>();
	private Vector<LogListener> logListeners = new Vector<LogListener>();
	private Vector<LogTerminateListener> logTerminateListeners = new Vector<LogTerminateListener>();

	private Log() {
		switch (processingLevel) {
			/// potential values for ORM log: TRACE, DEBUG, INFO, WARN, ERROR, and FATAL
			case DEBUG:
				System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "TRACE");
				break;
			case INFO:
				System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
				break;
			case TERMINAL:
				break;
			default:
				break;
		}
	}

	public static Log getInstance() {
		return instance;
	}

	public synchronized void setServerMode(boolean onOff) {
		serverMode = onOff;
	}

	public synchronized void openLogFile(String pathToTopDir) {
		try {
			String logDir = pathToTopDir + File.separator + "logs";
			File dir = new File(logDir);

			if (!dir.exists()) {
				dir.mkdirs();
			}

			ArrayList<File> logFiles = new ArrayList<File>();

			for (File file : dir.listFiles()) {
				String fileName = file.getName();

				if (fileName.startsWith(LOG_FILE_NAME_PREFIX) && fileName.endsWith(LOG_FILE_NAME_POSTFIX)) {
					logFiles.add(file);
				}
			}

			Collections.sort(logFiles, new LogFileComparator());

			while (logFiles.size() > MAX_NUM_LOG_FILES - 1) {
				logFiles.remove(0).delete();
			}

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", new Locale(Messages.locale));
		    String timestamp = formatter.format(new Date());

			this.printStream = new PrintStream(logDir + File.separator + LOG_FILE_NAME_PREFIX + timestamp + LOG_FILE_NAME_POSTFIX);
			LocalLog.openLogFile(logDir + File.separator + ORM_LOG_FILE_NAME_PREFIX + timestamp + LOG_FILE_NAME_POSTFIX);

		} catch (Exception e) {
			log(Level.INFO, this, "Could not open log file. Using stderr instead.", e);
		}
	}

	public PrintStream getPrintStream() {
		return printStream;
	}

	public synchronized void setBufferLength(int bufferLength) {
		this.bufferLength = bufferLength;
	}

	public synchronized void setProcessingLevel(Level level) {
		this.processingLevel = level;
	}

	public synchronized void log(Level level, String message) {
		log(level, (String) null, message, (Throwable) null);
	}
	
	public synchronized void log(Level level, String source, String message) {
		log(level, source, message, (Throwable) null);
	}

	public synchronized void log(Level level, Object source, String message) {
		log(level, source.getClass(), message, (Throwable) null);
	}

	public synchronized void log(Level level, Object source, String message, Throwable throwable) {
		log(level, source.getClass(), message, throwable);
	}
	
	public synchronized void log(Level level, Class<?> source, String message) {
		log(level, source, message, (Throwable) null);
	}

	public synchronized void log(Level level, Class<?> source, String message, Throwable throwable) {
		log(level, source.getSimpleName(), message, throwable);
	}

	public synchronized void log(Level level, String source, String message, Throwable throwable) {
		if (level.ordinal() > this.processingLevel.ordinal()) {
			return;
		}

		String timestamp = new Timestamp(new Date().getTime()).toString();

		while (timestamp.length() != 23) {
			timestamp += "0";
		}

		String formattedMessage = timestamp;
		
		if (source != null) {
			formattedMessage += ": " + source;
		}

		if (formattedMessage != null) {
			formattedMessage += ": " + message;
		}

		printStream.println(formattedMessage);

		if (!serverMode) {
			history.add(formattedMessage);
			notifyNewLogMessage(formattedMessage);
		}

		String stackTrace = null;

		if (throwable != null) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			throwable.printStackTrace(pw);
			stackTrace = sw.toString();

			printStream.println(stackTrace);

			if (!serverMode) {
				history.add(stackTrace);
				notifyNewLogMessage(stackTrace);
			}
		}

		while (history.size() > bufferLength) {
			history.remove(0);
		}

		if (level == Level.TERMINAL && logTerminateListeners.size() != 0) {
			for (LogTerminateListener listener : logTerminateListeners) {
				listener.logTerminate(timestamp, source, message, stackTrace);
			}
		}
	}

	public synchronized ArrayList<String> addLogListener(LogListener listener) {
		logListeners.add(listener);

		ArrayList<String> historyCopy = new ArrayList<String>();
		historyCopy.addAll(history);

		return historyCopy;
	}

	public synchronized void removeLogListener(LogListener listener) {
		logListeners.remove(listener);
	}

	public synchronized void addLogTerminateListener(LogTerminateListener listener) {
		logTerminateListeners.add(listener);
	}

	public synchronized void removeLogTerminateListener(LogTerminateListener listener) {
		logTerminateListeners.remove(listener);
	}
	
	private synchronized void notifyNewLogMessage(String message) {
		for (LogListener listener : logListeners) {
			listener.newLogMessage(message);
		}
	}

	private class LogFileComparator implements Comparator<File> {
		@Override
		public int compare(File file1, File file2) {
			return file1.getName().compareTo(file2.getName());
		}
	}
}
