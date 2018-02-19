/*
 * Copyright © 2016-2018 by Devon Bowen.
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
import org.easotope.shared.admin.events.AcidTempUpdated;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcAll;
import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.NumericValue;
import org.easotope.shared.core.tables.Permissions;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class AcidTempUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private AcidTemp acidTemp = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		return permissions.isCanEditSampleTypes();
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<AcidTemp,Integer> acidTempDao = DaoManager.createDao(connectionSource, AcidTemp.class);
		List<AcidTemp> allAcidTempsForSampleType = acidTempDao.queryForEq(AcidTemp.SAMPLETYPEID_FIELD_NAME, acidTemp.getSampleTypeId());

		for (AcidTemp thisAcidTemp : allAcidTempsForSampleType) {
			if (thisAcidTemp.getTemperature() == acidTemp.getTemperature() && thisAcidTemp.getSampleTypeId() == acidTemp.getSampleTypeId() && thisAcidTemp.getId() != acidTemp.getId()) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.acidTempUpdate_acidTemperatureAlreadyExists);
				return;
			}
		}

		boolean valuesChanged = false;

		if (acidTemp.getId() == DatabaseConstants.EMPTY_DB_ID) {
			acidTempDao.create(acidTemp);
			valuesChanged = true;

		} else {
			AcidTemp oldAcidTemp = acidTempDao.queryForId(acidTemp.getId());

			if (oldAcidTemp == null) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.acidTempUpdate_acidTempDoesNotExist, new Object[] { acidTemp.getId() } );
				return;
			}

			// check if any values change

			Map<Integer,NumericValue> oldValues = oldAcidTemp.getValues();
			Map<Integer,NumericValue> newValues = acidTemp.getValues();

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

			acidTempDao.update(acidTemp);
		}

		addEvent(new AcidTempUpdated(acidTemp));

		if (valuesChanged) {
			addEvent(new CorrIntervalsNeedRecalcAll());
		}
	}

	public void setAcidTemp(AcidTemp acidTemp) {
		this.acidTemp = acidTemp;
	}

	public AcidTemp getAcidTemp() {
		return acidTemp;
	}
}
