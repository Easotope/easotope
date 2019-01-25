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

package org.easotope.shared.commands;

import java.util.Hashtable;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.RepAnalysisList;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.RepAnalysisListItem;
import org.easotope.shared.analysis.tables.RepAnalysis;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class RepAnalysisListGet extends Command {
	private static final long serialVersionUID = 1L;

	private RepAnalysisList repAnalysisList = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		return true;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		repAnalysisList = new RepAnalysisList();

		Dao<RepAnalysis,Integer> repAnalysisDao = DaoManager.createDao(connectionSource, RepAnalysis.class);

		for (RepAnalysis repAnalysis : repAnalysisDao.queryForAll()) {
			repAnalysisList.put(repAnalysis.getId(), new RepAnalysisListItem(repAnalysis.getName()));
		}
	}

	public RepAnalysisList getRepAnalysisList() {
		return repAnalysisList;
	}
}
