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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcById;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class RepStepParamsUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private RepStepParams repStepParams = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		return permissions.isCanEditCorrIntervals();
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);

		HashMap<String,Object> fieldValues = new HashMap<String,Object>();
		fieldValues.put(RepStepParams.CORR_INTERVAL_ID_FIELD_NAME, repStepParams.getCorrIntervalId());
		fieldValues.put(RepStepParams.ANALYSIS_ID_FIELD_NAME, repStepParams.getAnalysisId());
		fieldValues.put(RepStepParams.POSITION_FIELD_NAME, repStepParams.getPosition());

		List<RepStepParams> repStepParamsList = repStepParamsDao.queryForFieldValues(fieldValues);
		RepStepParams oldRepStepParams = null;

		if (repStepParamsList.size() == 1) {
			oldRepStepParams = repStepParamsList.get(0);
		}

		if (oldRepStepParams != null && repStepParams.getId() != oldRepStepParams.getId()) {
			String message = MessageFormat.format(Messages.stepParametersUpdate_stepParameterIdMismatch, repStepParams.getId(), oldRepStepParams.getId());
			setStatus(Command.Status.EXECUTION_ERROR, message);
			return;
		}

		repStepParamsDao.createOrUpdate(repStepParams);

		CorrIntervalsNeedRecalcById corrIntervalsNeedRecalcById = new CorrIntervalsNeedRecalcById();
		corrIntervalsNeedRecalcById.addCorrIntervalId(repStepParams.getCorrIntervalId());
		addEvent(corrIntervalsNeedRecalcById);
	}

	public void setRepStepParameters(RepStepParams repStepParams) {
		this.repStepParams = repStepParams;
	}

	public RepStepParams getRepStepParameters() {
		return repStepParams;
	}
}
