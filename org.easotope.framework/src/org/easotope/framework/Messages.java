/*
 * Copyright © 2016-2023 by Devon Bowen.
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

package org.easotope.framework;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.easotope.framework.messages"; //$NON-NLS-1$

	private Messages() {
	}
	
	public static String locale;

	public static String objSocket_created;
	public static String objSocket_noSuchAlgorithmException;
	public static String objSocket_invalidAlgorithmParameterException;
	public static String objSocket_errorWritingPublicKey;
	public static String objSocket_sentLocalPublicKey;
	public static String objSocket_errorReadingPublicKey;
	public static String objSocket_noPublicKeyReceived;
	public static String objSocket_receivedRemotePublicKey;
	public static String objSocket_errorReadingIv1;
	public static String objSocket_noIv1Received;
	public static String objSocket_receivedRemoteIv1;
	public static String objSocket_errorReadingIv2;
	public static String objSocket_noIv2Received;
	public static String objSocket_receivedRemoteIv2;
	public static String objSocket_invalidKeyException;
	public static String objSocket_invalidKeySpecException;
	public static String objSocket_noSuchPaddingException;
	public static String objSocket_serializeFailed;
	public static String objSocket_errorWritingToSocket;
	public static String objSocket_illegalBlockSizeException;
	public static String objSocket_badPaddingException;
	public static String objSocket_missingRemotePublicKey;
	public static String objSocket_decompressionError;
	public static String objSocket_wroteObjectToQueue;
	public static String objSocket_writingSuspended;
	public static String objSocket_writeThreadExit;
	public static String objSocket_writingBytes;
	public static String objSocket_wroteBytes;
	public static String objSocket_errorReadingBlock;
	public static String objSocket_readObject;
	public static String objSocket_errorDeserializing;
	public static String objSocket_deserializedClassNotFound;
	public static String objSocket_unexpectedEndOfFile;
	public static String objSocket_readBytes;

	public static String processor_connecting;
	public static String processor_noConnection;
	public static String processor_shutDown;
	public static String processor_connected;
	public static String processor_executing;
	public static String processor_dbError;
	public static String processor_connectionDropped;
	public static String processor_returnedStatus;
	public static String processor_stopRequested;
	public static String processor_pauseRequested;
	public static String processor_resumeRequested;
	public static String processor_paused;
	public static String processor_resuming;
	public static String processor_unableToReconnect;
	public static String processor_receivedEvent;
	public static String processor_receivedEventWithCommand;
	public static String processor_lookingForCommand;
	public static String processor_returningCommand;
	public static String processor_waitingForCommand;

	public static String general_notInitialized;
	public static String general_notPermitted;
	public static String general_unexpectedEvents;
	
	public static String initialize_versionTableExists;
	public static String initialize_userTableExists;

	public static String userUpdate_exists;
	public static String userUpdate_doesNotExist;
	
	public static String serverProcessor_unknownHost;
	public static String serverProcessor_ioException;
	
	public static String dbBackup_starting;
	public static String dbBackup_ioError;
	public static String dbBackup_renameFailed;
	public static String dbBackup_complete;
	public static String dbBackup_cleanupOld;
	public static String dbBackup_deleteError;
	public static String dbBackup_stopRequested;

	public static String userPasswordUpdate_userDoesNotExist;
	public static String userPasswordUpdate_noUniquePrefs;

	public static String arguments_propertiesHelp;
	public static String arguments_portHelp;
	public static String arguments_dbdirHelp;
	public static String arguments_backupdirHelp;
	public static String arguments_backuptimesHelp;
	public static String arguments_maxbackupsHelp;
	public static String arguments_rebuildcorrintervalHelp;
	public static String arguments_reparseacquisitionsHelp;
	public static String arguments_reparsescansHelp;
	public static String arguments_debugHelp;
	public static String arguments_commandLineError;
	public static String arguments_fileInputError;
	public static String arguments_cantParsePort;
	public static String arguments_cantParseMaxBackups;
	public static String arguments_paramNotFound;
	public static String arguments_incompleteBackupParams;

	public static String server_socketOpenError;
	public static String server_socketClosed;
	public static String server_acceptError;
	public static String server_requestingStop;
	public static String server_processorStopped;
	public static String server_closingSocket;
	public static String server_errorClosingSocket;
	public static String server_exiting;

	public static String shutdown_requestingStop;

	public static String bridge_ioException;
	public static String bridge_terminalError;
	public static String bridge_receivedObject;
	public static String bridge_objectGivenToProcessor;
	public static String bridge_socketClosed;

	public static String objSocketManager_threadCreateFailed;
	public static String objSocketManager_noSuchThread;
	public static String objSocketManager_terminalError;
	public static String objSocketManager_ioException;
	
	public static String backupMgrInvalidTime;

	public static String folderProcessor_couldNotSaveBatch;
	public static String folderProcessor_batchItemNumberError;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
