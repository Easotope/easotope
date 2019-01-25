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

package org.easotope.shared.plugin.analysis.databaseupgradehandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.HashMap;

import javax.xml.bind.DatatypeConverter;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.network.Serialization;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.admin.tables.RefGas;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.core.NumericValue;
import org.easotope.shared.core.tables.Permissions;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.ConnectionSource;

public class Upgrade20161009 extends DatabaseUpgrade {
	private static HashMap<String,String> fromToMapping = new HashMap<String,String>();

	@Override
	public int appliesToVersion() {
		return 20161009;
	}

	@Override
	public int resultsInVersion() {
		return 20161129;
	}

	@Override
	public boolean upgrade(RawFileManager rawFileManager, ConnectionSource connectionSource) {
		try {
			Dao<Permissions,Integer> permissionsDao = DaoManager.createDao(connectionSource, Permissions.class);
			permissionsDao.executeRaw("UPDATE " + Permissions.TABLE_NAME + " SET " + Permissions.CANEDITCONSTANTS_FIELD_NAME + "=1 WHERE " + Permissions.USERID_FIELD_NAME + "=1");

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while fixing \"can edit constants\" permission for admin.", e);
			return false;
		}
 
		try {
			Dao<AcidTemp,Integer> acidTempDao = DaoManager.createDao(connectionSource, AcidTemp.class);
			GenericRawResults<String[]> rawResults = acidTempDao.queryRaw("select " + AcidTemp.ID_FIELD_NAME + "," + AcidTemp.VALUES_FIELD_NAME + " from " + AcidTemp.TABLE_NAME);

			for (String[] array : rawResults.getResults()) {
				if (array[1] != null) {
					byte[] oldBytes = DatatypeConverter.parseHexBinary(array[1]);
					@SuppressWarnings("unchecked")
					HashMap<Integer,NumericValue> oldHashMap = (HashMap<Integer,NumericValue>) bytesToObject(oldBytes);
					HashMap<Integer,Object[]> newHashMap = new HashMap<Integer,Object[]>();

					for (Integer key : oldHashMap.keySet()) {
						NumericValue numericValue = oldHashMap.get(key);

						if (numericValue == null) {
							newHashMap.put(key, null);
						} else {
							newHashMap.put(key, new Object[] { numericValue.getValue(), numericValue.getDescription(), numericValue.getReference() });		
						}
					}

					byte[] newBytes = Serialization.objectToBytes(newHashMap);
					String newString = DatatypeConverter.printHexBinary(newBytes);

					acidTempDao.updateRaw("UPDATE " + AcidTemp.TABLE_NAME + " SET " + AcidTemp.VALUES_FIELD_NAME + "='" + newString + "' WHERE " + AcidTemp.ID_FIELD_NAME + "=" + array[0] + ";");
				}
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while fixing NumericValue for acid temps.", e);
			return false;
		}

		try {
			Dao<RefGas,Integer> refGasDao = DaoManager.createDao(connectionSource, RefGas.class);
			GenericRawResults<String[]> rawResults = refGasDao.queryRaw("select " + RefGas.ID_FIELD_NAME + "," + RefGas.VALUES_FIELD_NAME + " from " + RefGas.TABLE_NAME);

			for (String[] array : rawResults.getResults()) {
				if (array[1] != null) {
					byte[] oldBytes = DatatypeConverter.parseHexBinary(array[1]);
					@SuppressWarnings("unchecked")
					HashMap<Integer,NumericValue> oldHashMap = (HashMap<Integer,NumericValue>) bytesToObject(oldBytes);
					HashMap<Integer,Object[]> newHashMap = new HashMap<Integer,Object[]>();

					for (Integer key : oldHashMap.keySet()) {
						NumericValue numericValue = oldHashMap.get(key);

						if (numericValue == null) {
							newHashMap.put(key, null);
						} else {
							newHashMap.put(key, new Object[] { numericValue.getValue(), numericValue.getDescription(), numericValue.getReference() });		
						}
					}

					byte[] newBytes = Serialization.objectToBytes(newHashMap);
					String newString = DatatypeConverter.printHexBinary(newBytes);

					refGasDao.updateRaw("UPDATE " + RefGas.TABLE_NAME + " SET " + RefGas.VALUES_FIELD_NAME + "='" + newString + "' WHERE " + RefGas.ID_FIELD_NAME + "=" + array[0] + ";");
				}
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while fixing NumericValue for ref gasses.", e);
			return false;
		}

		try {
			Dao<Standard,Integer> standardDao = DaoManager.createDao(connectionSource, Standard.class);
			GenericRawResults<String[]> rawResults = standardDao.queryRaw("select " + Standard.ID_FIELD_NAME + "," + Standard.VALUES_FIELD_NAME + " from " + Standard.TABLE_NAME);

			for (String[] array : rawResults.getResults()) {
				if (array[1] != null) {
					byte[] oldBytes = DatatypeConverter.parseHexBinary(array[1]);
					@SuppressWarnings("unchecked")
					HashMap<Integer,NumericValue> oldHashMap = (HashMap<Integer,NumericValue>) bytesToObject(oldBytes);
					HashMap<Integer,Object[]> newHashMap = new HashMap<Integer,Object[]>();

					for (Integer key : oldHashMap.keySet()) {
						NumericValue numericValue = oldHashMap.get(key);

						if (numericValue == null) {
							newHashMap.put(key, null);
						} else {
							newHashMap.put(key, new Object[] { numericValue.getValue(), numericValue.getDescription(), numericValue.getReference() });		
						}
					}

					byte[] newBytes = Serialization.objectToBytes(newHashMap);
					String newString = DatatypeConverter.printHexBinary(newBytes);

					standardDao.updateRaw("UPDATE " + Standard.TABLE_NAME + " SET " + Standard.VALUES_FIELD_NAME + "='" + newString + "' WHERE " + Standard.ID_FIELD_NAME + "=" + array[0] + ";");
				}
			}

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, Upgrade20160105.class, "Error while fixing NumericValue for standards.", e);
			return false;
		}

		rebuildScanFileParsed = true;
		rebuildAcquisitionsParsed = true;
		rebuildAnalyses = true;

		return true;
	}
	
	public static Serializable bytesToObject(byte[] bytes) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = getSwappingOIS(new ByteArrayInputStream(bytes));
		Serializable object = (Serializable) objectInputStream.readObject();
		objectInputStream.close();

		return object;
	}

	public static ObjectInputStream getSwappingOIS(InputStream in) throws IOException, ClassNotFoundException {
		return new ObjectInputStream(in) {
			protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
				Class<?> defaultValue = super.resolveClass(objectStreamClass);

				for (String from : fromToMapping.keySet()) {
					String to = fromToMapping.get(from);

					String name = objectStreamClass.getName().replaceFirst("^" + from, to);
					name = name.replaceFirst("^\\[L" + from, "[L" + to);

					if (!name.equals(objectStreamClass.getName())) {
						return Class.forName(name);
					}
				}

				return defaultValue;
			}

			protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
				ObjectStreamClass defaultValue = super.readClassDescriptor();

				for (String from : fromToMapping.keySet()) {
					String to = fromToMapping.get(from);

					String name = defaultValue.getName().replaceFirst("^" + from, to);
					name = name.replaceFirst("^\\[L" + from, "[L" + to);

					if (!name.equals(defaultValue.getName())) {
						return ObjectStreamClass.lookup(Class.forName(name));
					}
				}

				return defaultValue;
			}
		};
	}

	static {
		fromToMapping.put("com.carbonateresearch.easotope.core.NumericValue", NumericValue.class.getName());
	}
}
