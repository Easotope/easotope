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

package org.easotope.framework.dbcore.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.easotope.framework.Messages;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;

public class DbBackup {
	private final static String TEMP_FILE_PREFIX = "tmp";
	private final static String FINAL_FILE_PREFIX = "backup_";
	private final static String SIMPLE_DATE_FORMAT = "yyyy_MM_dd_HH_mm_ss";
	private final static String ZIP_EXTENSION = ".zip";
	private final Pattern backupFilePattern = Pattern.compile("^backup_\\d{4}_\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d{2}\\.zip$");

	private String dbFolderPath;
	private String backupFolderPath;
	private int maxBackups;
	
	private volatile boolean stopping = false;

	public DbBackup(String dbFolderPath, String backupFolderPath, int maxBackups) {
		this.dbFolderPath = dbFolderPath;
		this.backupFolderPath = backupFolderPath;
		this.maxBackups = maxBackups;

		if (!backupFolderPath.endsWith(File.separator)) {
			this.backupFolderPath = this.backupFolderPath + File.separator;
		}
	}

	public void execute() {
		if (stopping) {
			return;
		}

		String backupFile = backupFolderPath + TEMP_FILE_PREFIX + ZIP_EXTENSION;
		Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.dbBackup_starting, dbFolderPath, backupFile));

		try {
			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(backupFile));
			File fileSource = new File(dbFolderPath);

			String initialPath = "";
			int lastSeparator = fileSource.getCanonicalPath().lastIndexOf(File.separatorChar);

			if (lastSeparator != -1) {
				initialPath = fileSource.getCanonicalPath().substring(lastSeparator+1) + "/";
			}

			addDirectory(zout, initialPath, fileSource);
			zout.close();

		} catch (IOException e) {
			Log.getInstance().log(Level.INFO, this, Messages.dbBackup_ioError, e);
			return;
		}

		if (stopping) {
			new File(backupFile).delete();
			return;
		}

		SimpleDateFormat sdf = new SimpleDateFormat(SIMPLE_DATE_FORMAT);
		String date = sdf.format(Calendar.getInstance().getTime());
		
		String finalFileName = backupFolderPath + FINAL_FILE_PREFIX + date + ZIP_EXTENSION;
		
		if (!new File(backupFile).renameTo(new File(finalFileName))) {
			Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.dbBackup_renameFailed, backupFile, finalFileName));
			return;
		}

		if (maxBackups > 0) {
			cleanupOldBackups();
		}

		Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.dbBackup_complete, backupFile, finalFileName));
    }

    private void addDirectory(ZipOutputStream zout, String currentPath, File fileSource) throws IOException {
    		File[] files = fileSource.listFiles();

    		for (int i=0; i < files.length; i++) {
    			if (stopping) {
    				return;
    			}

    			if (files[i].isDirectory()) {
    				addDirectory(zout, currentPath + files[i].getName() + "/", files[i]);
    				continue;
    			}

			FileInputStream fin = new FileInputStream(files[i]);
			zout.putNextEntry(new ZipEntry(currentPath + files[i].getName()));

			int length;
			byte[] buffer = new byte[1024];

			while ((length = fin.read(buffer)) > 0 && !stopping) {
				zout.write(buffer, 0, length);
			}

			zout.closeEntry();
			fin.close();
    		}
    }
    
    private void cleanupOldBackups() {
		if (stopping) {
			return;
		}

		Log.getInstance().log(Level.INFO, this, Messages.dbBackup_cleanupOld);

		File fileSource = new File(backupFolderPath);

		File[] files = fileSource.listFiles();
		ArrayList<File> backupFiles = new ArrayList<File>();

		for (File file : files) {
			if (backupFilePattern.matcher(file.getName()).matches()) {
				backupFiles.add(file);
			}
		}

		Collections.sort(backupFiles, new FileNameComparator());
		
		while (backupFiles.size() > maxBackups) {
			File file = backupFiles.remove(0);

			if (!file.delete()) {
				Log.getInstance().log(Level.INFO, this, MessageFormat.format(Messages.dbBackup_deleteError, file.getName()));
			}
		}
    }

	public void requestStop() {
		Log.getInstance().log(Level.INFO, this, Messages.dbBackup_stopRequested);
		stopping = true;	
	}

    class FileNameComparator implements Comparator<Object> {
		@Override
		public int compare(Object arg0, Object arg1) {
			String name0 = ((File) arg0).getName();
			String name1 = ((File) arg1).getName();

			return name0.compareTo(name1);
		}
    }
}
