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

package org.easotope.shared.commands;

import java.util.HashMap;
import java.util.Hashtable;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.admin.tables.RefGas;
import org.easotope.shared.analysis.tables.CorrIntervalV1;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class CorrIntervalListGet extends Command {
	private static final long serialVersionUID = 1L;

	private int massSpecId;
	private HashMap<Integer,Long> validFromList = new HashMap<Integer,Long>();
	private HashMap<Integer,int[]> selectedDataAnalysisList = new HashMap<Integer,int[]>();
	private HashMap<Integer,Integer> batchDelimiterList = new HashMap<Integer,Integer>();
	private HashMap<Integer,Integer[]> channelToMZX10List = new HashMap<Integer,Integer[]>();

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		return true;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<CorrIntervalV1,Integer> corrIntervalDao = DaoManager.createDao(connectionSource, CorrIntervalV1.class);

		for (CorrIntervalV1 corrInterval : corrIntervalDao.queryForEq(RefGas.MASSSPECID_FIELD_NAME, massSpecId)) {
			validFromList.put(corrInterval.getId(), corrInterval.getValidFrom());
			selectedDataAnalysisList.put(corrInterval.getId(), corrInterval.getDataAnalysis());
			batchDelimiterList.put(corrInterval.getId(), corrInterval.getBatchDelimiter());
			channelToMZX10List.put(corrInterval.getId(), corrInterval.getChannelToMzX10());
		}
	}

	public int getMassSpecId() {
		return massSpecId;
	}

	public void setMassSpecId(int massSpecId) {
		this.massSpecId = massSpecId;
	}

	public HashMap<Integer,Long> getValidFromList() {
		return validFromList;
	}

	public HashMap<Integer,int[]> getSelectedDataAnalysisList() {
		return selectedDataAnalysisList;
	}

	public HashMap<Integer,Integer> getBatchDelimiterList() {
		return batchDelimiterList;
	}

	public HashMap<Integer,Integer[]> getChannelToMZX10List() {
		return channelToMZX10List;
	}
}
