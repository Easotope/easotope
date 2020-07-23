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

package org.easotope.framework.dbcore.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class RawFileManager {
	private static final String DIR_NAME = "raw_files";
	private static final String FILE_PREFIX = "rawfile_";
	private static final int FILE_COUNTER_LENGTH = 19;
	private static final String FILE_EXTENSION = ".zip";

	private String dirPath;
	private long nextRawFile = -1;

	public RawFileManager(String topDirPath) {
		this.dirPath = topDirPath + File.separator + DIR_NAME;

		for (String filename : getFileNamesWithoutExtension()) {
			String fileNumber = filename.substring(FILE_PREFIX.length(), FILE_PREFIX.length() + FILE_COUNTER_LENGTH);
			long fileNumberLong = Long.parseLong(fileNumber);
			nextRawFile = Math.max(nextRawFile, fileNumberLong);
		}

		nextRawFile++;
	}

    @SuppressWarnings("resource")
	public byte[] readRawFile(String filename) {
		String path = dirPath + File.separator + filename;
		boolean compression = false;

		if (new File(path + FILE_EXTENSION).exists()) {
			path += FILE_EXTENSION;
			compression = true;
		}

		InputStream is = null;
		FileInputStream fis = null;
		ZipInputStream zis = null;
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			int numRead = 0;
			final int BUF_SIZE = 1024;
			byte[] bytes = new byte[BUF_SIZE];
			is = fis = new FileInputStream(path);

			if (compression) {
				is = zis = new ZipInputStream(fis);
				((ZipInputStream) is).getNextEntry();
			}

			while ((numRead=is.read(bytes, 0, BUF_SIZE)) >= 0) {
				byteArrayOutputStream.write(bytes, 0, numRead);
			}

		} catch (Exception e) {
			return null;

		} finally {
			try {
				if (zis != null) {
					zis.close();
				}
				
				fis.close();
			} catch (IOException e) {
				// do nothing
			}
		}

		return byteArrayOutputStream.toByteArray();
    }

    public String writeRawFile(byte[] data) {
		String filename = String.format(FILE_PREFIX + "%0" + FILE_COUNTER_LENGTH + "d", nextRawFile);
		String path = dirPath + File.separator + filename + FILE_EXTENSION;

		if (writeRawFile(path, filename, data)) {
			nextRawFile++;
			return filename;
		}

		return null;
    }

//    private boolean writeRawFile(String path, byte[] data) {
//		if (new File(path).exists()) {
//			return false;
//		}
//
//		data = Compression.compress(data);
//
//		if (data == null) {
//			return false;
//		}
//
//		try {
//			File file = new File(path);
//			file.delete();
//
//			FileOutputStream out = new FileOutputStream(file);
//			out.write(data);
//			out.close();
//
//		} catch (Exception e) {
//			new File(path).delete();
//			return false;
//		}
//
//		return true;
//    }

    private boolean writeRawFile(String path, String shortName, byte[] data) {
		if (new File(path).exists()) {
			return false;
		}

		try {
			File file = new File(path);
			file.delete();

			ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file));
			zout.putNextEntry(new ZipEntry(shortName));
			zout.write(data);
			zout.close();

		} catch (Exception e) {
			new File(path).delete();
			return false;
		}

		return true;
    }

	public boolean deleteRawFile(String filename) {
		String path = dirPath + File.separator + filename;

		if (new File(path + FILE_EXTENSION).delete()) {
			return true;
		} else {
			return new File(path).delete();
		}
	}

	private ArrayList<String> getFileNamesWithoutExtension() {
		File dir = new File(dirPath);

		if (!dir.isDirectory() && !dir.mkdir()) {
			throw new RuntimeException("couldn't make directory " + dirPath);
		}

		String[] filenameList = dir.list();

		if (filenameList == null) {
			throw new RuntimeException("directory listing returned null for " + dirPath);
		}

		ArrayList<String> allIds = new ArrayList<String>();

		for (String filename : filenameList) {
			if (filename.startsWith(FILE_PREFIX)) {
				if (filename.endsWith(FILE_EXTENSION)) {
					filename = filename.substring(0, filename.length() - FILE_EXTENSION.length());
				}

				allIds.add(filename);
			}
		}

		return allIds;
	}

	public boolean upgrade20170328() {
		for (String filename : getFileNamesWithoutExtension()) {
			byte[] bytes = readRawFile(filename);

			if (bytes == null) {
				return false;
			}

			String newFilePath = dirPath + File.separator + filename + FILE_EXTENSION;
			new File(newFilePath).delete();

			if (!writeRawFile(newFilePath, filename, bytes)) {
				return false;
			}

			String oldFilePath = dirPath + File.separator + filename;
			new File(oldFilePath).delete();
		}

		return true;
	}
}
