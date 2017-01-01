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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class RawFileManager {
	private final String prefix = "rawfile_";

	private String dirPath;
	private long nextRawFile = -1;
	private Pattern validFilenamePattern = Pattern.compile(prefix + "\\d{19}");

	public RawFileManager(String dirPath) {
		this.dirPath = dirPath;

		for (String id : getAllIds()) {
			String idNumber = id.substring(prefix.length());
			long idValue = Long.parseLong(idNumber);
			nextRawFile = Math.max(nextRawFile, idValue);
		}

		nextRawFile++;
	}

    public byte[] readRawFile(String filename) {
		String path = dirPath + File.separator + filename;

		long length = new File(path).length();
		byte[] bytes = new byte[(int) length];

		int offset = 0;
		int numRead = 0;
		InputStream is = null;

		try {
			is = new FileInputStream(path);

			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}

		} catch (Exception e) {
			return null;
    	
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}

		if (offset < bytes.length) {
			return null;
		}

		return bytes;
    }

	public String writeRawFile(byte[] data) {
		String filename = String.format(prefix + "%019d", nextRawFile);

		if (new File(dirPath + File.separator + filename).exists()) {
			return null;
		}

		if (!overwriteRawFile(filename, data)) {
			return null;
		}

		nextRawFile++;

		return filename;
	}

	public boolean overwriteRawFile(String filename, byte[] data) {
		String path = dirPath + File.separator + filename;

		try {
			File file = new File(path);
			file.delete();

			FileOutputStream out = new FileOutputStream(file);
			out.write(data);
			out.close();

		} catch (Exception e) {
			new File(path).delete();
			return false;
		}
		
		return true;
	}

	public boolean deleteRawFile(String filename) {
		String path = dirPath + File.separator + filename;
		return new File(path).delete();
	}
	
	public ArrayList<String> getAllIds() {
		File dir = new File(dirPath);

		if (!dir.isDirectory() && !dir.mkdir()) {
			throw new RuntimeException("couldn't make directory " + dirPath);
		}

		String[] list = dir.list();

		if (list == null) {
			throw new RuntimeException("directory listing returned null for " + dirPath);
		}

		ArrayList<String> allIds = new ArrayList<String>();

		for (String filename : list) {
			if (validFilenamePattern.matcher(filename).matches()) {
				allIds.add(filename);
			}
		}

		return allIds;
	}
}
