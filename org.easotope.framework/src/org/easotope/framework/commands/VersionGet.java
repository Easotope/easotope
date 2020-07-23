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

package org.easotope.framework.commands;

import java.util.Hashtable;

import org.easotope.framework.core.util.SystemProperty;
import org.easotope.framework.dbcore.tables.Version;
import org.easotope.framework.dbcore.util.RawFileManager;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class VersionGet extends Command {
	private static final long serialVersionUID = 1L;

	private int clientVersion;
	private int serverVersion;
	private boolean serverDeniesConnection;
	private boolean serverInitialized = false;

	public VersionGet() {
		clientVersion = SystemProperty.getVersion();
	}

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		return true;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		serverVersion = SystemProperty.getVersion();
		serverDeniesConnection = clientVersion < SystemProperty.getOldestCompatVersion() || clientVersion > SystemProperty.getVersion();

		Dao<Version, Integer> versionDao = DaoManager.createDao(connectionSource, Version.class);

		if (versionDao.isTableExists()) {
			serverInitialized = versionDao.queryForId(1) != null;
		}
	}

	public int getClientVersion() {
		return clientVersion;
	}

	public int getServerVersion() {
		return serverVersion;
	}

	public boolean isServerDeniesConnection() {
		return serverDeniesConnection;
	}

	public boolean isServerInitialized() {
		return serverInitialized;
	}
}
