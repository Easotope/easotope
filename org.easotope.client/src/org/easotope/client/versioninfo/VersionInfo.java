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

package org.easotope.client.versioninfo;

import java.util.concurrent.CountDownLatch;

import org.easotope.framework.commands.Command;
import org.easotope.framework.commands.VersionGet;
import org.easotope.framework.dbcore.cmdprocessors.CommandListener;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;


public class VersionInfo implements CommandListener {
	private static VersionInfo instance;

	private int serverVersion = Integer.MIN_VALUE;
	private boolean serverDeniesConnection = true;

	private CountDownLatch countdownLatch = new CountDownLatch(1);

	public VersionInfo() {
		VersionGet versionGet = new VersionGet();
		ProcessorManager.getInstance().getProcessor().process(versionGet, null, this);
	}

	public static VersionInfo getInstance() {
		if (instance == null) {
			instance = new VersionInfo();
		}

		try {
			instance.countdownLatch.await();
		} catch (InterruptedException e) {
			// do nothing
		}
		
		return instance;
	}

	public int getServerVersion() {
		return serverVersion;
	}

	public boolean isServerDeniesConnection() {
		return serverDeniesConnection;
	}

	@Override
	public void commandExecuted(Command command) {
		VersionGet versionGet = (VersionGet) command;

		serverVersion = versionGet.getServerVersion();
		serverDeniesConnection = versionGet.isServerDeniesConnection();

		countdownLatch.countDown();
	}
}
