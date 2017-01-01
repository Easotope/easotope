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

package org.easotope.framework.core.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.zip.DataFormatException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.easotope.framework.Messages;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.network.DiffieHellmanDES.MissingRemotePublicKey;

public class ObjSocket implements Runnable {
	private static volatile int idCounter = 0;

	private int id;
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	private DiffieHellmanDES diffieHellmanDES;
	private WriteThread writeThread;
	private HashSet<ObjSocketListener> listeners;

	public ObjSocket(Socket socket) throws IOException {
		id = idCounter++;

		this.socket = socket;
		inputStream = socket.getInputStream();
		outputStream = socket.getOutputStream();
		writeThread = new WriteThread();
		listeners = new HashSet<ObjSocketListener>();

		byte[] inetAddress = socket.getInetAddress().getAddress();
		String message = MessageFormat.format(Messages.objSocket_created, id, (int) (inetAddress[0] & 0xff), (int) (inetAddress[1] & 0xff), (int) (inetAddress[2] & 0xff), (int) (inetAddress[3] & 0xff));
		Log.getInstance().log(Level.DEBUG, this, message);
	}

	public int getId() {
		return id;
	}

	public void run() {
		if (initDiffieHellman()) {
			new Thread(writeThread).start();

			synchronized (listeners) {
				for (ObjSocketListener objSocketListener : listeners) {
					objSocketListener.objSocketConnected(this);
				}
			}

			handleReads();
		}

		close();
	}

	public void close() {
		writeThread.suspendWrites();

		try {
			socket.close();
		} catch (Exception e) {
			// do nothing
		}

		synchronized (listeners) {
			for (ObjSocketListener objSocketListener : listeners) {
				objSocketListener.objSocketClosed(this);
			}
		}
	}

	private boolean initDiffieHellman() {
		try {
			diffieHellmanDES = new DiffieHellmanDES();
			byte[] bytes = diffieHellmanDES.getLocalPublicKey();

			outputStream.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
			outputStream.write(bytes);

		} catch (NoSuchAlgorithmException exception) {
			Log.getInstance().log(Level.DEBUG, this, Messages.objSocket_noSuchAlgorithmException, exception);
			notifyException(Messages.objSocket_noSuchAlgorithmException, exception);
			return false;

		} catch (InvalidAlgorithmParameterException exception) {
			Log.getInstance().log(Level.DEBUG, this, Messages.objSocket_invalidAlgorithmParameterException, exception);
			notifyException(Messages.objSocket_invalidAlgorithmParameterException, exception);
			return false;

		} catch (IOException exception) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_errorWritingPublicKey, id), exception);
			return false;
		}

		Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_sentLocalPublicKey, id));

		byte[] bytes = null;

		try {
			bytes = readByteBlock();

		} catch (IOException e) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_errorReadingPublicKey, id), e);
			return false;
		}

		if (bytes == null) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_noPublicKeyReceived, id));
			return false;
		}

		Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_receivedRemotePublicKey, id));

		try {
			diffieHellmanDES.setRemotePublicKey(bytes);

		} catch (InvalidKeyException exception) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_invalidKeyException, id), exception);
			return false;

		} catch (NoSuchAlgorithmException exception) {
			Log.getInstance().log(Level.DEBUG, this, Messages.objSocket_noSuchAlgorithmException, exception);
			return false;

		} catch (InvalidKeySpecException exception) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_invalidKeySpecException, id), exception);
			return false;

		} catch (NoSuchPaddingException exception) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_noSuchPaddingException, id), exception);
			return false;
		}

		return true;
	}

	private void handleReads() {
		while (true) {
			Serializable object = readObject();

			if (object == null) {
				return;
			}

			synchronized (listeners) {
				for (ObjSocketListener obSocketListener : listeners) {
					obSocketListener.objSocketReceivedObject(this, object);
				}
			}
		}
	}

	private Serializable readObject() {
		byte[] bytes;

		try {
			bytes = readByteBlock();

		} catch (IOException e) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_errorReadingBlock, id), e);
			return null;
		}

		if (bytes == null) {
			return null;
		}

		Serializable object = null;

		try {
			bytes = diffieHellmanDES.decrypt(bytes);

			try {
				bytes = Compression.decompress(bytes);
			} catch (DataFormatException exception) {
				Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_decompressionError, id), exception);
			}
			
			object = Serialization.bytesToObject(bytes);

		} catch (IOException exception) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_errorDeserializing, id), exception);
			return null;

		} catch (ClassNotFoundException exception) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_deserializedClassNotFound, id), exception);
			return null;

		} catch (IllegalBlockSizeException exception) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_illegalBlockSizeException, id), exception);
			return null;

		} catch (BadPaddingException exception) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_badPaddingException, id), exception);
			return null;

		} catch (MissingRemotePublicKey exception) {
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_missingRemotePublicKey, id), exception);
			return null;
		}

		Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_readObject, object.getClass().getSimpleName(), id));

		return object;
	}

	private byte[] readByteBlock() throws IOException {
		byte[] bytes = readBytes(Integer.SIZE / Byte.SIZE);
		
		if (bytes == null) {
			return null;
		}

		bytes = readBytes(ByteBuffer.wrap(bytes).getInt());

		if (bytes == null) {
			throw new IOException(MessageFormat.format(Messages.objSocket_unexpectedEndOfFile, id));
		}

		Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_readBytes, bytes.length, id));

		return bytes;
	}

	private byte[] readBytes(int size) throws IOException {
		byte[] bytes = new byte[size];
		int offset = 0;

		while (size != 0) {
			int numRead = inputStream.read(bytes, offset, size);

			if (numRead == -1) {
				if (offset != 0) {
					throw new IOException(MessageFormat.format(Messages.objSocket_unexpectedEndOfFile, id));
				} else {
					return null;
				}
			}

			size -= numRead;
			offset += numRead;
		}

		return bytes;
	}

	private void notifyException(String message, Throwable throwable) {
		ObjSocketException objSocketError = new ObjSocketException(message, throwable);

		synchronized (listeners) {
			for (ObjSocketListener objSocketListener : listeners) {
				objSocketListener.objSocketException(this, objSocketError);
			}
		}
	}

	public void addListener(ObjSocketListener objSocketListener) {
		synchronized (listeners) {
			listeners.add(objSocketListener);
		}
	}

	public void removeListener(ObjSocketListener objSocketListener) {
		synchronized (listeners) {
			listeners.remove(objSocketListener);
		}
	}

	public void writeObject(Serializable object) throws IllegalBlockSizeException, BadPaddingException, IOException, MissingRemotePublicKey {
		writeThread.writeObject(object);
	}

	public void suspendWrites() {
		writeThread.suspendWrites();
	}

	private class WriteThread implements Runnable {
		private boolean suspended = false;
		private ArrayList<byte[]> byteBlocks = new ArrayList<byte[]>();

		void writeObject(Serializable object) throws IOException, IllegalBlockSizeException, BadPaddingException, MissingRemotePublicKey {
			if (suspended) {
				return;
			}

			byte[] bytes = null;

			try {
				bytes = Serialization.objectToBytes(object);

			} catch (IOException exception) {
				Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_serializeFailed, object.getClass().getName(), id), exception);
				throw exception;
			}

			bytes = Compression.compress(bytes);

			try {
				bytes = diffieHellmanDES.encrypt(bytes);

				synchronized (byteBlocks) {
					byteBlocks.add(0, bytes);
					byteBlocks.notify();
				}

			} catch (IllegalBlockSizeException exception) {
				Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_illegalBlockSizeException, id), exception);
				throw exception;

			} catch (BadPaddingException exception) {
				Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_badPaddingException, id), exception);
				throw exception;

			} catch (MissingRemotePublicKey exception) {
				Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_missingRemotePublicKey, id), exception);
				throw exception;
			}

			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_wroteObjectToQueue, object.getClass().getSimpleName(), id));
		}

		void suspendWrites() {
			suspended = true;

			synchronized(byteBlocks) {
				byteBlocks.clear();
				byteBlocks.notify();
			}
			
			Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_writingSuspended, id));
		}

		@Override
		public void run() {
			while (true) {
				byte[] bytes = null;

				synchronized(byteBlocks) {
					while (byteBlocks.size() == 0) {
						try {
							byteBlocks.wait();
						} catch (InterruptedException e) {
							// ignore
						}

						if (suspended || socket.isClosed()) {
							Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_writeThreadExit, id));
							return;
						}
					}

					bytes = byteBlocks.remove(byteBlocks.size()-1);
				}

				Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_writingBytes, bytes.length, id));

				try {
					outputStream.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
					outputStream.write(bytes);
					Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_wroteBytes, bytes.length, id));

				} catch (IOException exception) {
					Log.getInstance().log(Level.DEBUG, this, MessageFormat.format(Messages.objSocket_errorWritingToSocket, id), exception);

					try {
						socket.close();
					} catch (IOException e) {
						// ignore
					}

					return;
				}
			}
		}
	}
}
