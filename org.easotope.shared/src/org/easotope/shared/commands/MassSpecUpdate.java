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

import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcAll;
import org.easotope.shared.admin.events.MassSpecUpdated;
import org.easotope.shared.admin.tables.MassSpec;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

public class MassSpecUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private MassSpec massSpec = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);
		return permissions.isCanEditMassSpecs();
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		Dao<MassSpec,Integer> massSpecDao = DaoManager.createDao(connectionSource, MassSpec.class);

		List<MassSpec> massSpecsWithSameName = massSpecDao.queryForEq(MassSpec.NAME_FIELD_NAME, massSpec.getName());

		for (MassSpec thatMassSpec : massSpecsWithSameName) {
			if (thatMassSpec.getId() != massSpec.getId()) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.massSpecUpdate_massSpecAlreadyExists, new Object[] { massSpec.getName() } );
				return;
			}
		}

		boolean nameChanged = false;
		MassSpecUpdated massSpecUpdatedEvent = new MassSpecUpdated(massSpec);

		if (massSpec.getId() == DatabaseConstants.EMPTY_DB_ID) {
			massSpecDao.create(massSpec);
			massSpecUpdatedEvent.setMassSpecNew(true);

		} else {
			MassSpec oldMassSpec = massSpecDao.queryForId(massSpec.getId());

			if (oldMassSpec == null) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.massSpecUpdate_massSpecDoesNotExist, new Object[] { massSpec.getId() } );
				return;
			}

			if (!oldMassSpec.getName().equals(massSpec.getName())) {
				nameChanged = true;
			}
			
			massSpecDao.update(massSpec);
		}

		addEvent(massSpecUpdatedEvent);

		if (nameChanged) {
			addEvent(new CorrIntervalsNeedRecalcAll());
		}
	}

	public MassSpec getMassSpec() {
		return massSpec;
	}

	public void setMassSpec(MassSpec massSpec) {
		this.massSpec = massSpec;
	}
}
