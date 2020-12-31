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

package org.easotope.framework.server;

import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.easotope.framework.Messages;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;

public class Arguments {
	private static String PROPERTIES_ARG = "properties";
	private static String PORT_ARG = "port";
	private static String DBDIR_ARG = "dbdir";
	private static String BACKUPDIR_ARG = "backupdir";
	private static String BACKUPTIMES_ARG = "backuptimes";
	private static String MAXBACKUPS_ARG = "maxbackups";
	private static String DEBUG_ARG = "debug";
	private static String REPARSE_ACQUISITIONS_ARG = "reparseAcquisitions";

	private boolean isInvalid = false;

	private int port = -1;
	private String dbdir = null;
	private String backupDir = null;
	private String backupTimes = null;
	private int maxBackups = -1;
	private boolean debug = false;
	private boolean reparseAcquisitions = false;

	public Arguments(String programName, String[] args) {
		Options options = new Options();

		options.addOption(PROPERTIES_ARG, true, Messages.arguments_propertiesHelp);
		options.addOption(PORT_ARG, true, Messages.arguments_portHelp);
		options.addOption(DBDIR_ARG, true, Messages.arguments_dbdirHelp);
		options.addOption(BACKUPDIR_ARG, true, Messages.arguments_backupdirHelp);
		options.addOption(BACKUPTIMES_ARG, true, Messages.arguments_backuptimesHelp);
		options.addOption(MAXBACKUPS_ARG, true, Messages.arguments_maxbackupsHelp);
		options.addOption(DEBUG_ARG, false, Messages.arguments_debugHelp);
		options.addOption(REPARSE_ACQUISITIONS_ARG, false, Messages.arguments_reparseacquisitionsHelp);

	    CommandLineParser parser = new DefaultParser();
	    CommandLine commandLine = null;

	    try {
	        commandLine = parser.parse(options, args);

	    } catch (ParseException e) {
	    		Log.getInstance().log(Level.INFO, MessageFormat.format(Messages.arguments_commandLineError, e.getMessage()));
	    		new HelpFormatter().printHelp(programName, options, true);
	    		isInvalid = false;
	    		return;
	    }

	    if (commandLine.hasOption(PROPERTIES_ARG)) {
	    		String filename = commandLine.getOptionValue(PROPERTIES_ARG);

	    		Properties properties = new Properties();

	    		try {
	    		    properties.load(new FileInputStream(filename));

	    		} catch (Exception e) {
	    			Log.getInstance().log(Level.INFO, MessageFormat.format(Messages.arguments_fileInputError, e.getMessage()));
		    		new HelpFormatter().printHelp(programName, options, true);
		    		isInvalid = true;
		    		return;
		    	}

	    		if (properties.containsKey(PORT_ARG)) {
	    			try {
	    				this.port = Integer.parseInt(properties.getProperty(PORT_ARG));
	    			} catch (NumberFormatException e) {
	    				Log.getInstance().log(Level.INFO, MessageFormat.format(Messages.arguments_cantParsePort, properties.getProperty(PORT_ARG)));
			    		isInvalid = true;
	    			}
	    		}

	    		if (properties.containsKey(DBDIR_ARG)) {
	    			this.dbdir = properties.getProperty(DBDIR_ARG);
	    		}
	    		
	    		if (properties.containsKey(BACKUPDIR_ARG)) {
	    			this.backupDir = properties.getProperty(BACKUPDIR_ARG);
	    		}
	    		
	    		if (properties.containsKey(BACKUPTIMES_ARG)) {
	    			this.backupTimes = properties.getProperty(BACKUPTIMES_ARG);
	    		}
	    		
	    		if (properties.containsKey(MAXBACKUPS_ARG)) {
	    			try {
	    				this.maxBackups = Integer.parseInt(properties.getProperty(MAXBACKUPS_ARG));
	    			} catch (NumberFormatException e) {
	    				Log.getInstance().log(Level.INFO, MessageFormat.format(Messages.arguments_cantParseMaxBackups, properties.getProperty(MAXBACKUPS_ARG)));
			    		isInvalid = true;
	    			}
	    		}

	    		if (properties.containsKey(DEBUG_ARG)) {
	    			this.debug = Boolean.parseBoolean(properties.getProperty(DEBUG_ARG));
	    		}

	    		if (properties.containsKey(REPARSE_ACQUISITIONS_ARG)) {
	    			this.reparseAcquisitions = Boolean.parseBoolean(properties.getProperty(REPARSE_ACQUISITIONS_ARG));
	    		}
	    }
	    
	    if (commandLine.getOptionValue(PORT_ARG) != null) {
	    		try {
	    			this.port = Integer.parseInt(commandLine.getOptionValue(PORT_ARG));
    			} catch (NumberFormatException e) {
    				Log.getInstance().log(Level.INFO, MessageFormat.format(Messages.arguments_cantParsePort, commandLine.getOptionValue(PORT_ARG)));
		    		isInvalid = true;
    			}
	    }
	    
	    if (commandLine.getOptionValue(DBDIR_ARG) != null) {
	    		this.dbdir = commandLine.getOptionValue(DBDIR_ARG);
	    }

		if (commandLine.getOptionValue(BACKUPDIR_ARG) != null) {
			this.backupDir = commandLine.getOptionValue(BACKUPDIR_ARG);
		}

		if (commandLine.getOptionValue(BACKUPTIMES_ARG) != null) {
			this.backupTimes = commandLine.getOptionValue(BACKUPTIMES_ARG);
		}
		
		if (commandLine.getOptionValue(MAXBACKUPS_ARG) != null) {
			try {
				this.maxBackups = Integer.parseInt(commandLine.getOptionValue(MAXBACKUPS_ARG));
			} catch (NumberFormatException e) {
				Log.getInstance().log(Level.INFO, MessageFormat.format(Messages.arguments_cantParseMaxBackups, commandLine.getOptionValue(MAXBACKUPS_ARG)));
				isInvalid = true;
			}
		}

	    if (commandLine.hasOption(DEBUG_ARG)) {
	    		this.debug = true;
	    }

	    if (commandLine.hasOption(REPARSE_ACQUISITIONS_ARG)) {
    		this.reparseAcquisitions = true;
	    }

	    if (getPort() == -1) {
	    		Log.getInstance().log(Level.INFO, MessageFormat.format(Messages.arguments_paramNotFound, PORT_ARG));
	        isInvalid = true;
	    }

	    if (getDbDir() == null) {
	    		Log.getInstance().log(Level.INFO, MessageFormat.format(Messages.arguments_paramNotFound, DBDIR_ARG));
	        isInvalid = true;
	    }
	    
	    if ((getBackupDir() != null || getBackupTimes() != null) && (getBackupDir() == null || getBackupTimes() == null)) {
	    		Log.getInstance().log(Level.INFO, Messages.arguments_incompleteBackupParams);
	    		isInvalid = true;
	    }

	    if (isInvalid) {
    			new HelpFormatter().printHelp(programName, options, true);
	    }
	}

	public boolean isInvalid() {
		return isInvalid;
	}

	public int getPort() {
		return port;
	}
	
	public String getDbDir() {
		return dbdir;
	}
	
	public String getBackupDir() {
		return backupDir;
	}
	
	public String getBackupTimes() {
		return backupTimes;
	}

	public int getMaxBackups() {
		return maxBackups;
	}

	public boolean isDebug() {
		return debug;
	}
	
	public boolean isReparseAcquisitions() {
		return reparseAcquisitions;
	}
}
