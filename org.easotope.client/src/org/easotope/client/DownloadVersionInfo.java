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

package org.easotope.client;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.SystemProperty;
import org.easotope.shared.core.PotentialGraphicsMethodsShared;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

public class DownloadVersionInfo extends Thread {
	private Display display;
	private int serverVersion;
	private boolean usingServer;

	public DownloadVersionInfo(Display display, int serverVersion, boolean usingServer) {
		this.display = display;
		this.serverVersion = serverVersion;
		this.usingServer = usingServer;
	}

	public void run() {
		int tempInt;

		try {
			String url = MessageFormat.format(Messages.downloadVersionInfo_bestClientForServerUrl, String.valueOf(serverVersion));
			tempInt = downloadVersion(url);
		} catch (Exception e) {
			return;
		}

		final int bestClientForServer = tempInt;

		if (bestClientForServer != SystemProperty.getVersion()) {
			Log.getInstance().log(Level.DEBUG, DownloadVersionInfo.class, "Best client version for this server is " + bestClientForServer);

			try {
				sleep(5000);
			} catch (InterruptedException e) {
				// ignore
			}

			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						String message = MessageFormat.format(Messages.downloadVersionInfo_newVersion, String.valueOf(bestClientForServer));
						MessageDialog dialog= new MessageDialog(null, Messages.downloadVersionInfo_newVersionTitle, null, message, MessageDialog.INFORMATION, new String[] { Messages.downloadVersionInfo_downloadNewVersion, IDialogConstants.OK_LABEL }, 0);
						int button = dialog.open();

						if (button == 0) {
							Program.launch(MessageFormat.format(Messages.downloadVersionInfo_versionUrl, String.valueOf(bestClientForServer)));
						}

					} catch (Exception e) {
						Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
						PotentialGraphicsMethodsShared.reportErrorToUser(display, e);
					}
				}
			});

			return;
		}

		Log.getInstance().log(Level.DEBUG, DownloadVersionInfo.class, "This is the best client for this server");

		try {
			tempInt = downloadVersion(Messages.downloadVersionInfo_bestServerUrl);
		} catch (Exception e) {
			return;
		}

		final int bestServer = tempInt;

		if (bestServer > SystemProperty.getVersion()) {
			Log.getInstance().log(Level.DEBUG, DownloadVersionInfo.class, "Best server version is " + bestServer);

			int previousBestServer = LoginInfoCache.getInstance().getPreferences().getPreviousBestServer();

			if (bestServer != previousBestServer) {
				try {
					sleep(5000);
				} catch (InterruptedException e) {
					// ignore
				}

				if (usingServer) {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								String message = MessageFormat.format(Messages.downloadVersionInfo_newServer, String.valueOf(bestServer));
								MessageDialog dialog= new MessageDialog(null, Messages.downloadVersionInfo_newVersionTitle, null, message, MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0);
								dialog.open();

							} catch (Exception e) {
								Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
								PotentialGraphicsMethodsShared.reportErrorToUser(display, e);
							}
						}
					});
				} else {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							try {
								String message = MessageFormat.format(Messages.downloadVersionInfo_newVersion, String.valueOf(bestServer));
								MessageDialog dialog= new MessageDialog(null, Messages.downloadVersionInfo_newVersionTitle, null, message, MessageDialog.INFORMATION, new String[] { Messages.downloadVersionInfo_downloadNewVersion, IDialogConstants.OK_LABEL }, 0);
								int button = dialog.open();

								if (button == 0) {
									Program.launch(MessageFormat.format(Messages.downloadVersionInfo_versionUrl, String.valueOf(bestServer)));
								}

							} catch (Exception e) {
								Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
								PotentialGraphicsMethodsShared.reportErrorToUser(display, e);
							}
						}
					});
				}

				LoginInfoCache.getInstance().savePreviousBestServer(bestServer);
			}

			return;
		}

		Log.getInstance().log(Level.DEBUG, DownloadVersionInfo.class, "This is the best server");
	}

	public static int downloadVersion(String urlString) throws Exception {
		final URL url = new URL(urlString);
		InputStream inputStream = null;
		BufferedReader bufferedReader = null;

		try {
		    Pattern pattern = Pattern.compile(".*(\\d{8}).*");

			inputStream = url.openStream();
			bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
			String line;

			while ((line = bufferedReader.readLine()) != null) {
			    Matcher matcher = pattern.matcher(line);
			    
			    if (matcher.matches()) {
			    		return Integer.parseInt(matcher.group(1));
			    }
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, DownloadVersionInfo.class, "Exception while loading version information", e);
			throw e;

		} finally {
			try {
				bufferedReader.close();
				inputStream.close();
			} catch (Exception e) {
				// ignore
			}
		}

		RuntimeException runtimeException = new RuntimeException("Version information not found in " + urlString);
		Log.getInstance().log(Level.INFO, DownloadVersionInfo.class, "Exception while loading version information", runtimeException);
		throw runtimeException;
	}
}
