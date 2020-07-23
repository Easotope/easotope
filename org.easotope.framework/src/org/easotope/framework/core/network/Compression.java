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

package org.easotope.framework.core.network;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compression {
	public static byte[] compress(byte[] bytes) {
		Deflater dfl = new Deflater();
		dfl.setLevel(Deflater.BEST_COMPRESSION);
		dfl.setInput(bytes);
		dfl.finish();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] tmp = new byte[4*1024];

		try {
			while (!dfl.finished()) {
				int size = dfl.deflate(tmp);
				baos.write(tmp, 0, size);
			}

		} finally {
			try {
				baos.close();
			} catch (Exception ex) {
				// ignore
			}
		}

        return baos.toByteArray();
    }

	public static byte[] decompress(byte[] bytes) throws DataFormatException {
		Inflater iflr = new Inflater();
		iflr.setInput(bytes);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] tmp = new byte[4*1024];

		try {
			while (!iflr.finished()) {
				int size = iflr.inflate(tmp);
				baos.write(tmp, 0, size);
			}

		} finally {
			try {
				baos.close();
			} catch (Exception ex) {
				// ignore
			}
		}

		return baos.toByteArray();
    }
}