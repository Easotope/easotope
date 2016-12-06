/*
 * Copyright © 2016 by Devon Bowen.
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
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.AuthenticationKeys;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.analysis.events.RepAnalysisChoiceUpdated;
import org.easotope.shared.analysis.tables.RepAnalysisChoice;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class RepAnalysisChoiceUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private int sampleUserId;
	private int sampleId;
	private int samAnalysisId;
	private HashMap<Integer,Integer> repAnalysisChoice;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
		Sample sample = sampleDao.queryForId(sampleId);
		sampleUserId = sample.getUserId();

		return user.getIsAdmin() || sampleUserId == user.getId();
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<RepAnalysisChoice,Integer> repSelectionDao = DaoManager.createDao(connectionSource, RepAnalysisChoice.class);

		HashMap<String,Object> fields = new HashMap<String,Object>();
		fields.put(RepAnalysisChoice.SAMPLE_ID_FIELD_NAME, sampleId);
		fields.put(RepAnalysisChoice.SAM_ANALYSIS_ID_FIELD_NAME, samAnalysisId);
		List<RepAnalysisChoice> list = repSelectionDao.queryForFieldValues(fields);

		if (list == null || list.size() == 0) {
			RepAnalysisChoice newRepAnalysisChoice = new RepAnalysisChoice();
			newRepAnalysisChoice.setSampleId(sampleId);
			newRepAnalysisChoice.setSamAnalysisId(samAnalysisId);
			newRepAnalysisChoice.setRepIdsToRepAnalysisChoice(repAnalysisChoice);
			repSelectionDao.create(newRepAnalysisChoice);

		} else {
			RepAnalysisChoice oldRepAnalysisChoice = list.get(0);
			oldRepAnalysisChoice.getRepIdsToRepAnalysisChoice().putAll(repAnalysisChoice);
			repSelectionDao.update(oldRepAnalysisChoice);
		}

		addEvent(new RepAnalysisChoiceUpdated(sampleId, samAnalysisId));
	}

	public int getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}

	public int getSamAnalysisId() {
		return samAnalysisId;
	}

	public void setSamAnalysisId(int samAnalysisId) {
		this.samAnalysisId = samAnalysisId;
	}

	public HashMap<Integer,Integer> getRepAnalysisChoice() {
		return repAnalysisChoice;
	}

	public void setRepAnalysisChoice(HashMap<Integer,Integer> repAnalysisChoice) {
		this.repAnalysisChoice = repAnalysisChoice;
	}
}
