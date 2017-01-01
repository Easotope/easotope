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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcAll;
import org.easotope.shared.admin.events.StandardUpdated;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.NumericValue;
import org.easotope.shared.core.tables.Permissions;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class StandardUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private Standard standard = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		return permissions.isCanEditStandards();
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<Standard,Integer> standardDao = DaoManager.createDao(connectionSource, Standard.class);

		List<Standard> standardsWithSameName = standardDao.queryForEq(Standard.NAME_FIELD_NAME, standard.getName());

		for (Standard thatStandard : standardsWithSameName) {
			if (thatStandard.getId() != standard.getId()) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.standardUpdate_standardAlreadyExists, new Object[] { standard.getName() } );
				return;
			}
		}

		boolean valuesChanged = false;

		if (standard.getId() == DatabaseConstants.EMPTY_DB_ID) {
			standardDao.create(standard);
			valuesChanged = true;

		} else {
			Standard oldStandard = standardDao.queryForId(standard.getId());
			
			if (oldStandard == null) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.standardUpdate_standardDoesNotExist, new Object[] { standard.getId() } );
				return;
			}

			if (!oldStandard.getName().equals(standard.getName())) {
				valuesChanged = true;
			}

			// check if any values change

			Map<Integer,NumericValue> oldValues = oldStandard.getValues();
			Map<Integer,NumericValue> newValues = standard.getValues();

			if (oldValues.size() != newValues.size()) {
				valuesChanged = true;

			} else {
				for (Integer key : newValues.keySet()) {
					NumericValue newNumericValue = newValues.get(key);
					NumericValue oldNumericValue = oldValues.get(key);

					if (oldNumericValue == null) {
						valuesChanged = true;
						break;

					} else {
						if (newNumericValue.getValue() != oldNumericValue.getValue()) {
							valuesChanged = true;
							break;
						}

						if (newNumericValue.getDescription() != oldNumericValue.getDescription()) {
							valuesChanged = true;
							break;
						}
					}
				}
			}

			standardDao.update(standard);
		}

		addEvent(new StandardUpdated(standard));

		if (valuesChanged) {
			addEvent(new CorrIntervalsNeedRecalcAll());
		}
	}

	public Standard getStandard() {
		return standard;
	}

	public void setStandard(Standard standard) {
		this.standard = standard;
	}
}
