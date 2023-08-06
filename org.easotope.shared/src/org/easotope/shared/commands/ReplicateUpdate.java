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

package org.easotope.shared.commands;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.events.CorrIntervalsNeedRecalcByTime;
import org.easotope.shared.core.AuthenticationKeys;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.compute.ComputeAcquisitionParsed;
import org.easotope.shared.rawdata.events.ReplicateUpdated;
import org.easotope.shared.rawdata.tables.AcquisitionInputV0;
import org.easotope.shared.rawdata.tables.AcquisitionParsedV2;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class ReplicateUpdate extends Command {
	private static final long serialVersionUID = 1L;

	private ReplicateV1 replicate;
	private transient ReplicateV1 oldReplicate;
	private ArrayList<Acquisition> acquisitions;
	private transient Sample sample;
	private boolean explode = false;
	private boolean allowDuplicates = false;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);

		if (allowDuplicates && !permissions.isCanImportDuplicates()) {
			replicate = null;
			acquisitions = null;
			return false;
		}

		if (replicate.getId() != DatabaseConstants.EMPTY_DB_ID) {
			Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
			oldReplicate = replicateDao.queryForId(replicate.getId());
		}

		if (replicate.getSampleId() != DatabaseConstants.EMPTY_DB_ID) {
			Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
			sample = sampleDao.queryForId(replicate.getSampleId());
		}

		if (permissions.isCanEditAllReplicates()) {
			return true;
		}

		if (replicate.getId() != DatabaseConstants.EMPTY_DB_ID && (oldReplicate == null || oldReplicate.getUserId() != user.getId())) {
			replicate = null;
			acquisitions = null;
			return false;
		}

		if (replicate.getSampleId() != DatabaseConstants.EMPTY_DB_ID && (sample == null || sample.getUserId() != user.getId())) {
			replicate = null;
			acquisitions = null;
			return false;
		}

		return true;
	}

	@Override
	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {

		// if duplicates are not allowed, verify that none exist

		Dao<AcquisitionInputV0,Integer> acquisitionInputDao = DaoManager.createDao(connectionSource, AcquisitionInputV0.class);
		Dao<AcquisitionParsedV2,Integer> acquisitionParsedDao = DaoManager.createDao(connectionSource, AcquisitionParsedV2.class);
		Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);

		if (!allowDuplicates) {
			String duplicates = getDuplicates(rawFileManager, rawFileDao, acquisitionInputDao, acquisitionParsedDao);

			if (!duplicates.isEmpty()) {			
				String message = MessageFormat.format(Messages.replicateUpdate_duplicateReplicate, duplicates);
				setStatus(Command.Status.VERIFY_AND_RESEND, message);

				replicate = null;
				acquisitions = null;

				return;
			}
		}

		// figure out which user id we need to use for the replicates

		Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);

		User user = (User) authenticationObjects.get(AuthenticationKeys.USER);
		Permissions permissions = (Permissions) authenticationObjects.get(AuthenticationKeys.PERMISSIONS);

		int userId = DatabaseConstants.EMPTY_DB_ID;

		if (permissions.isCanEditAllReplicates()) {
			userId = replicate.getUserId();
		} else {
			if (replicate.getSampleId() != DatabaseConstants.EMPTY_DB_ID) {
				userId = sample.getUserId();
			} else {
				userId = user.getId();
			}
		}

		replicate.setUserId(userId);

		// create or update the primary replicate

		CorrIntervalsNeedRecalcByTime corrIntervalsNeedRecalc = new CorrIntervalsNeedRecalcByTime();

		if (replicate.getId() == DatabaseConstants.EMPTY_DB_ID) {
			replicateDao.create(replicate);

		} else {
			if (oldReplicate == null) {
				oldReplicate = replicateDao.queryForId(replicate.getId());
			}

			if (oldReplicate == null) {
				setStatus(Command.Status.EXECUTION_ERROR, Messages.replicateUpdate_doesNotExist, new Object[] { replicate.getId() } );
				return;
			}

			if (oldReplicate.getStandardId() != DatabaseConstants.EMPTY_DB_ID) {
				corrIntervalsNeedRecalc.addTime(oldReplicate.getMassSpecId(), oldReplicate.getDate());
			}

			replicateDao.update(replicate);
		}

		// if any of our acquisitions are new create the acquisitionParsed database entries

		HashMap<byte[],RawFile> fileBytesToRawFile = new HashMap<byte[],RawFile>();

		for (Acquisition acquisition : acquisitions) {
			if (acquisition.getFileBytes() != null) {
				byte[] theseBytes = acquisition.getFileBytes();
				RawFile rawFile = null;

				for (byte[] alreadyDone : fileBytesToRawFile.keySet()) {
					if (Arrays.equals(alreadyDone, theseBytes)) {
						rawFile = fileBytesToRawFile.get(alreadyDone);
					}
				}

				if (rawFile == null) {
					String filename = rawFileManager.writeRawFile(theseBytes);

					rawFile = acquisition.getRawFile();
					rawFile.setUserId(userId);
					rawFile.setDatabaseName(filename);

					rawFileDao.create(rawFile);
					fileBytesToRawFile.put(theseBytes, rawFile);
				}

				ComputeAcquisitionParsed computeAcquisitionParsed = new ComputeAcquisitionParsed(rawFile, theseBytes, false, acquisition.getAcquisitionInput().getAssumedTimeZone());

				for (AcquisitionParsedV2 acquisitionParsed : computeAcquisitionParsed.getMaps()) {
					if (acquisitionParsed.getDate() == acquisition.getAcquisitionParsed().getDate()) {
						acquisitionParsedDao.create(acquisitionParsed);
						acquisition.setAcquisitionParsed(acquisitionParsed);
						break;
					}
				}

				acquisition.setFileBytes(null);
			}
		}

		// if we're exploding the replicate, create copies and fill in replicate array

		ArrayList<ReplicateV1> replicates = new ArrayList<ReplicateV1>();
		replicates.add(replicate);

		if (explode) {
			boolean isFirst = true;
			boolean disableAll = replicate.isDisabled();

			for (Acquisition acquisition : acquisitions) {
				if (isFirst) {
					if (acquisition.getAcquisitionParsed().getDate() != replicate.getDate()) {
						replicate.setDate(acquisition.getAcquisitionParsed().getDate());
						replicate.setDisabled(disableAll ? true : acquisition.getAcquisitionInput().isDisabled());
						replicateDao.update(replicate);
					}

					isFirst = false;

				} else {
					ReplicateV1 newReplicate = new ReplicateV1(replicate);
					newReplicate.setId(DatabaseConstants.EMPTY_DB_ID);
					newReplicate.setDate(acquisition.getAcquisitionParsed().getDate());
					newReplicate.setDisabled(disableAll ? true : acquisition.getAcquisitionInput().isDisabled());

					replicateDao.create(newReplicate);
					replicates.add(newReplicate);
				}
			}
		}

		// remove any old acquisitions associated with the primary replicate

		for (AcquisitionInputV0 acquisitionInput : acquisitionInputDao.queryForEq(AcquisitionInputV0.REPLICATEID_FIELD_NAME, replicates.get(0).getId())) {
			acquisitionInputDao.deleteById(acquisitionInput.getId());
		}

		// create the AcquisitionInput database entries

		int count = 0;
		for (Acquisition acquisition : acquisitions) {
			AcquisitionInputV0 acquisitionInput = acquisition.getAcquisitionInput();

			if (acquisitionInput.getId() == DatabaseConstants.EMPTY_DB_ID) {
				acquisitionInput.setRawFileId(acquisition.getRawFile().getId());
				acquisitionInput.setAcquisitionParsedId(acquisition.getAcquisitionParsed().getId());
			}

			int replicateId = (replicates.size() == 1) ? replicates.get(0).getId() : replicates.get(count).getId();
			acquisitionInput.setReplicateId(replicateId);

			acquisitionInput.setId(DatabaseConstants.EMPTY_DB_ID);
			acquisitionInputDao.create(acquisitionInput);

			AcquisitionParsedV2 acquisitionParsed = acquisitionParsedDao.queryForId(acquisitionInput.getAcquisitionParsedId());
			acquisition.setAcquisitionParsed(acquisitionParsed);

			acquisition.setRawFile(null);
			count++;
		}

		// create replicate update event

		for (ReplicateV1 replicate : replicates) {
			int oldSampleId = DatabaseConstants.EMPTY_DB_ID;
			boolean oldSampleHasChildren = false;
			int newSampleId = DatabaseConstants.EMPTY_DB_ID;
			String newSampleName = null;
			int oldProjectId = DatabaseConstants.EMPTY_DB_ID;
			int newProjectId = DatabaseConstants.EMPTY_DB_ID;

			if (oldReplicate != null && oldReplicate.getSampleId() != DatabaseConstants.EMPTY_DB_ID) {
				oldSampleId = oldReplicate.getSampleId();

				Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
				Sample oldSample = sampleDao.queryForId(oldSampleId);
				oldProjectId = oldSample.getProjectId();

				QueryBuilder<ReplicateV1,Integer> queryBuilder = replicateDao.queryBuilder();
				Where<ReplicateV1,Integer> where = queryBuilder.where();
				where = where.eq(ReplicateV1.SAMPLEID_FIELD_NAME, oldSampleId);

				oldSampleHasChildren = replicateDao.queryForFirst(queryBuilder.prepare()) != null;
			}

			if (sample != null) {
				newSampleId = sample.getId();
				newSampleName = sample.getName();
				newProjectId = sample.getProjectId();
			}

			ReplicateUpdated replicateUpdated = new ReplicateUpdated(replicate, oldSampleId, oldSampleHasChildren, newSampleId, newSampleName, oldProjectId, newProjectId);
			addEvent(replicateUpdated);
			
			if (replicate.getStandardId() != DatabaseConstants.EMPTY_DB_ID) {
				corrIntervalsNeedRecalc.addTime(replicate.getMassSpecId(), replicate.getDate());
			}
		}

		if (corrIntervalsNeedRecalc.getFromToRanges().size() != 0) {
			addEvent(corrIntervalsNeedRecalc);
		}

		// remove big data for return trip to client

		replicate = null;
		acquisitions = null;
	}

	private String getDuplicates(RawFileManager rawFileManager, Dao<RawFile,Integer> rawFileDao, Dao<AcquisitionInputV0,Integer> acquisitionInputDao, Dao<AcquisitionParsedV2,Integer> acquisitionParsedDao) throws Exception {
		String result = "";
		int count = 1;

		nextAcquisition:
		for (Acquisition acquisition : acquisitions) {
			if (acquisition.getFileBytes() != null) {
				List<AcquisitionParsedV2> acquisitionParseds = acquisitionParsedDao.queryForEq(AcquisitionParsedV2.DATE_FIELD_NAME, acquisition.getAcquisitionParsed().getDate());

				if (acquisitionParseds != null) {
					for (AcquisitionParsedV2 acquisitionParsed : acquisitionParseds) {
						List<AcquisitionInputV0> acquisitionInputs = acquisitionInputDao.queryForEq(AcquisitionInputV0.ACQUISITION_PARSED_ID_FIELD_NAME, acquisitionParsed.getId());

						RawFile rawFile = rawFileDao.queryForId(acquisitionInputs.get(0).getRawFileId());
						byte[] oldFileBytes = rawFileManager.readRawFile(rawFile.getDatabaseName());

						if (Arrays.equals(acquisition.getFileBytes(), oldFileBytes)) {
							result += result.isEmpty() ? count : ", " + count;
							count++;
							continue nextAcquisition;
						}
					}
				}
			}

			count++;
		}

		return result;
	}

	public void setReplicate(ReplicateV1 replicate) {
		this.replicate = replicate;
	}

	public void setAcquisitions(ArrayList<Acquisition> acquisitions) {
		this.acquisitions = acquisitions;
	}

	public boolean isExplode() {
		return explode;
	}

	public void setExplode(boolean explode) {
		this.explode = explode;
	}

	public void setAllowDuplicates(boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
	}
}
