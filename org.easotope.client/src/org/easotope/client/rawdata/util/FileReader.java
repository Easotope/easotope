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

package org.easotope.client.rawdata.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import org.easotope.client.Messages;
import org.easotope.client.rawdata.replicate.widget.acquisition.AcquisitionsWidget;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;

public class FileReader {
	public static byte[] getBytesFromFile(String filename) throws Exception {
		final int MAX_FILE_SIZE = 10*1024*1024;
		long length = new File(filename).length();

		if (length > MAX_FILE_SIZE) {
			String error = MessageFormat.format(Messages.acquisitionsWidget_fileTooBig, new Object[] { new File(filename).getName(), length, MAX_FILE_SIZE });
			Log.getInstance().log(Level.INFO, AcquisitionsWidget.class, error);
			throw new Exception(error);
		}

		byte[] bytes = new byte[(int) length];

		int offset = 0;
		int numRead = 0;
		InputStream is = null;

		try {
			is = new FileInputStream(filename);

			while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
				offset += numRead;
			}

		} catch (Exception e) {
			String error = MessageFormat.format(Messages.acquisitionsWidget_exceptionWhileReadingFile, new Object[] { new File(filename).getName(), e.getMessage() });
			Log.getInstance().log(Level.INFO, AcquisitionsWidget.class, error, e);
			throw new Exception(error);
    	
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
			String error = MessageFormat.format(Messages.acquisitionsWidget_fileOffsetIncorrect, new Object[] { new File(filename).getName(), offset, bytes.length });
			Log.getInstance().log(Level.INFO, AcquisitionsWidget.class, error);
			throw new Exception(error);
		}

		return bytes;
    }
}
