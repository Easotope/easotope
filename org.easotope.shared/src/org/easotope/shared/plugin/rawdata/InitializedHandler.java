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

package org.easotope.shared.plugin.rawdata;

import java.sql.SQLException;
import java.util.ArrayList;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.events.Initialized;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;
import org.easotope.shared.rawdata.tables.Project;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;
import org.easotope.shared.rawdata.tables.ScanFileInputV0;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;
import org.easotope.shared.rawdata.tables.ScanV3;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class InitializedHandler {
	public static ArrayList<Event> execute(Initialized event, RawFileManager rawFileManager, ConnectionSource connectionSource) {		
		try {
			TableUtils.createTableIfNotExists(connectionSource, Project.class);
			TableUtils.createTableIfNotExists(connectionSource, Sample.class);
			TableUtils.createTableIfNotExists(connectionSource, RawFile.class);

			TableUtils.createTableIfNotExists(connectionSource, ReplicateV1.class);
			TableUtils.createTableIfNotExists(connectionSource, AcquisitionInputV0.class);
			TableUtils.createTableIfNotExists(connectionSource, AcquisitionParsedV2.class);

			TableUtils.createTableIfNotExists(connectionSource, ScanV3.class);
			TableUtils.createTableIfNotExists(connectionSource, ScanFileInputV0.class);
			TableUtils.createTableIfNotExists(connectionSource, ScanFileParsedV2.class);

		} catch (SQLException e) {
			Log.getInstance().log(Level.INFO, InitializedHandler.class, "Error during initialization in rawdata plugin.", e);
		}

		return null;
	}
}
