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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.easotope.framework.core.network.Compression;
import org.easotope.framework.core.network.Serialization;
import org.easotope.framework.dbcore.cmdprocessors.CommandThatCanBeSentInBatchMode;

public class BatchStorageManager {
	private static final String DIR_NAME = "batch_storage";
	private static final String FILE_PREFIX = "command_";

	private HashMap<String,Integer> socketIdBatchIdToReadCounter = new HashMap<String,Integer>();
	private HashMap<String,Integer> socketIdBatchIdToWriteCounter = new HashMap<String,Integer>();
	private String dirPath;

	public BatchStorageManager(String topDirPath) {
		this.dirPath = topDirPath + File.separator + DIR_NAME;
		File dir = new File(dirPath);

		if (!dir.isDirectory() && !dir.mkdir()) {
			throw new RuntimeException("couldn't make directory " + dirPath);
		}
	}

	public boolean saveCommand(int socketId, int batchId, CommandThatCanBeSentInBatchMode command) {
		byte[] bytes = null;

		try {
			bytes = Serialization.objectToBytes(command);
			bytes = Compression.compress(bytes);

		} catch (IOException e) {
			return false;
		}

		String socketAndBatchIds = socketId + "_" + batchId;
		int commandNumber = 0;

		if (socketIdBatchIdToWriteCounter.containsKey(socketAndBatchIds)) {
			commandNumber = socketIdBatchIdToWriteCounter.get(socketAndBatchIds);
		}

		socketIdBatchIdToWriteCounter.put(socketAndBatchIds, commandNumber+1);

		String filename = FILE_PREFIX + socketAndBatchIds + "_" + commandNumber;

		try {
			File file = new File(filename);
			FileOutputStream out = new FileOutputStream(file);
			out.write(bytes);
			out.close();

		} catch (Exception e) {
			new File(filename).delete();
			return false;
		}

		return true;
	}

	public CommandThatCanBeSentInBatchMode getNext(int socketId, int batchId) {
		String socketAndBatchIds = socketId + "_" + batchId;
		int commandNumber = 0;

		if (socketIdBatchIdToReadCounter.containsKey(socketAndBatchIds)) {
			commandNumber = socketIdBatchIdToReadCounter.get(socketAndBatchIds);
		}

		socketIdBatchIdToReadCounter.put(socketAndBatchIds, commandNumber+1);

		String filename = FILE_PREFIX + socketAndBatchIds + "_" + commandNumber;

		try {
			File file = new File(filename);
			byte[] bytes = new byte[(int) file.length()];

			FileInputStream in = new FileInputStream(file);

			int offset = 0;

			do {
				int numBytesRead = in.read(bytes, offset, 4096);
				offset += numBytesRead;
			} while (offset != bytes.length);

			in.close();
			new File(filename).delete();

			bytes = Compression.decompress(bytes);
			return (CommandThatCanBeSentInBatchMode) Serialization.bytesToObject(bytes);

		} catch (Exception e) {
			new File(filename).delete();
			return null;
		}
	}

	public void removeAllCommands() {
		File directory = new File(dirPath);
		File[] files = directory.listFiles();

		if (files != null) {
			for (File file : files) {
				file.delete();
			}
		}

		socketIdBatchIdToReadCounter.clear();
		socketIdBatchIdToWriteCounter.clear();
	}

	public void removeCommands(int socketId, int batchId) {
		String socketAndBatchIds = socketId + "_" + batchId;
		String filePrefix = FILE_PREFIX + socketAndBatchIds + "_";
		File[] filesInDir = new File(dirPath).listFiles();

		if (filesInDir != null) {
			for (File file: filesInDir) {
				String fileName = file.getName();
	
				if (fileName.startsWith(filePrefix)) {
					file.delete();
				}
			}
		}

		socketIdBatchIdToReadCounter.remove(socketAndBatchIds);
		socketIdBatchIdToWriteCounter.remove(socketAndBatchIds);
	}

	public void removeCommands(int socketId) {
		String filePrefix = FILE_PREFIX + socketId + "_";
		File[] filesInDir = new File(dirPath).listFiles();

		if (filesInDir != null) {
			for (File file: filesInDir) {
				String fileName = file.getName();
	
				if (fileName.startsWith(filePrefix)) {
					file.delete();
				}
			}
		}

		for (String key : socketIdBatchIdToReadCounter.keySet()) {
			if (key.startsWith(socketId + "_")) {
				socketIdBatchIdToReadCounter.remove(key);
			}
		}

		for (String key : socketIdBatchIdToWriteCounter.keySet()) {
			if (key.startsWith(socketId + "_")) {
				socketIdBatchIdToWriteCounter.remove(key);
			}
		}
	}
}
