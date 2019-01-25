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

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.easotope.framework.Messages;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.cmdprocessors.FolderProcessor;
import org.easotope.framework.dbcore.util.DbBackup;

public class BackupManager {
	private Timer timer;

	private String dbFolderPath;
	private String backupDir;
	private int maxBackups;
	private BackupTask mostRecentBackupTask;

	private final Pattern timePattern = Pattern.compile("^\\d{4}$");
	private final String TIME_SEPARATOR = ",";

	private HashSet<HoursMinutes> times = new HashSet<HoursMinutes>();
	private FolderProcessor processor;

	public BackupManager(String dbFolderPath, String backupDir, String times, int maxBackups, FolderProcessor processor) {		
		this.dbFolderPath = dbFolderPath;
		this.backupDir = backupDir;
		this.maxBackups = maxBackups;
		this.processor = processor;

		timer = new Timer(true);

		for (String time : times.split(TIME_SEPARATOR)) {
			if (!timePattern.matcher(time).matches()) {
				Log.getInstance().log(Level.TERMINAL, MessageFormat.format(Messages.backupMgrInvalidTime, time));
			}

			int hours = Integer.parseInt(time.substring(0,2));
			int minutes = Integer.parseInt(time.substring(2));

			if (hours > 23 || minutes > 59) {
				Log.getInstance().log(Level.TERMINAL, MessageFormat.format(Messages.backupMgrInvalidTime, time));
			}

			this.times.add(new HoursMinutes(hours, minutes));
		}

		if (this.times.size() == 0) {
			timer.cancel();
			return;
		}

		scheduleNext();
	}

    private void scheduleNext() {
    	if (timer == null) {
    		return;
    	}

		long nextMillisUntil = Long.MAX_VALUE;

		for (HoursMinutes time : times) {
			long millisUntil = time.millisUntil();

			if (nextMillisUntil > millisUntil) {
				nextMillisUntil = millisUntil;
			}
		}

		if (nextMillisUntil != Long.MAX_VALUE) {
			mostRecentBackupTask = new BackupTask(dbFolderPath, backupDir, maxBackups);
			timer.schedule(mostRecentBackupTask, nextMillisUntil);
		}
	}

    class HoursMinutes {
    		private int hours;
    		private int minutes;
 
    		public HoursMinutes(int hours, int minutes) {
    			this.hours = hours;
    			this.minutes = minutes;
    		}

    	    private long millisUntil() {
    	    		Calendar currCalendar = new GregorianCalendar();

    	    		Calendar newCalendar = new GregorianCalendar();
    	    		newCalendar.set(currCalendar.get(Calendar.YEAR), currCalendar.get(Calendar.MONTH), currCalendar.get(Calendar.DATE), hours, minutes, 0);

    	    		while (newCalendar.getTimeInMillis() - currCalendar.getTimeInMillis() <= 0) {
    	    			newCalendar.add(Calendar.DAY_OF_MONTH, 1);
    	    		}
 
    	    		return newCalendar.getTimeInMillis() - currCalendar.getTimeInMillis();
    	    }
    }

    public class BackupTask extends TimerTask {
		private DbBackup dbBackup;

		public BackupTask(String dbFolderPath, String backupDir, int maxBackups) {
			this.dbBackup = new DbBackup(dbFolderPath, backupDir, maxBackups);
		}

		@Override
        public void run() {
        	processor.pause();
        	dbBackup.execute();
        	processor.resume();

        	scheduleNext();
        }

		public void requestStop() {
			dbBackup.requestStop();
		}
    }

	public void requestStop() {
		timer.cancel();
		timer = null;
		mostRecentBackupTask.requestStop();
	}
}
