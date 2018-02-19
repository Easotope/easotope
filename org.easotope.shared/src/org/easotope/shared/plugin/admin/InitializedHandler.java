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

package org.easotope.shared.plugin.admin;

import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.events.Initialized;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.admin.AcidTempParameter;
import org.easotope.shared.admin.SciConstantNames;
import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.admin.tables.MassSpec;
import org.easotope.shared.admin.tables.Options;
import org.easotope.shared.admin.tables.Options.OverviewResolution;
import org.easotope.shared.admin.tables.RefGas;
import org.easotope.shared.admin.tables.SampleType;
import org.easotope.shared.admin.tables.SciConstant;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.core.NumericValue;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class InitializedHandler {
	public static ArrayList<Event> execute(Initialized event, RawFileManager rawFileManager, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, Options.class);
			Dao<Options,Integer> optionsDao = DaoManager.createDao(connectionSource, Options.class);

			Options options = new Options();
			options.setOverviewResolution(OverviewResolution.REPLICATE);
			optionsDao.create(options);

			TableUtils.createTable(connectionSource, MassSpec.class);
			TableUtils.createTable(connectionSource, RefGas.class);
			TableUtils.createTable(connectionSource, Standard.class);

			TableUtils.createTable(connectionSource, SciConstant.class);
			Dao<SciConstant,Integer> constantDao = DaoManager.createDao(connectionSource, SciConstant.class);

			SciConstant constant = new SciConstant();
			constant.setEnumeration(SciConstantNames.λ);
			constant.setValue(SciConstantNames.λ.getDefaultValue());
			constant.setReference(SciConstantNames.λ.getDefaultReference());
			constantDao.create(constant);

			constant = new SciConstant();
			constant.setEnumeration(SciConstantNames.R13_VPDB);
			constant.setValue(SciConstantNames.R13_VPDB.getDefaultValue());
			constant.setReference(SciConstantNames.R13_VPDB.getDefaultReference());
			constantDao.create(constant);

			constant = new SciConstant();
			constant.setEnumeration(SciConstantNames.R17_VSMOW);
			constant.setValue(SciConstantNames.R17_VSMOW.getDefaultValue());
			constant.setReference(SciConstantNames.R17_VSMOW.getDefaultReference());
			constantDao.create(constant);

			constant = new SciConstant();
			constant.setEnumeration(SciConstantNames.R18_VSMOW);
			constant.setValue(SciConstantNames.R18_VSMOW.getDefaultValue());
			constant.setReference(SciConstantNames.R18_VSMOW.getDefaultReference());
			constantDao.create(constant);

			constant = new SciConstant();
			constant.setEnumeration(SciConstantNames.δ18O_VPDB_VSMOW);
			constant.setValue(SciConstantNames.δ18O_VPDB_VSMOW.getDefaultValue());
			constant.setReference(SciConstantNames.δ18O_VPDB_VSMOW.getDefaultReference());
			constantDao.create(constant);

			constant = new SciConstant();
			constant.setEnumeration(SciConstantNames.KELVIN_CELCIUS);
			constant.setValue(SciConstantNames.KELVIN_CELCIUS.getDefaultValue());
			constant.setReference(SciConstantNames.KELVIN_CELCIUS.getDefaultReference());
			constantDao.create(constant);

			TableUtils.createTable(connectionSource, SampleType.class);
			Dao<SampleType,Integer> sampleTypeDao = DaoManager.createDao(connectionSource, SampleType.class);

			SampleType aragoniteSampleType = new SampleType();
			aragoniteSampleType.setName("Aragonite");
			aragoniteSampleType.setDescription("");
			aragoniteSampleType.setHasAcidTemps(true);
			sampleTypeDao.create(aragoniteSampleType);

			SampleType calciteSampleType = new SampleType();
			calciteSampleType.setName("Calcite");
			calciteSampleType.setDescription("");
			calciteSampleType.setHasAcidTemps(true);
			sampleTypeDao.create(calciteSampleType);

			SampleType co2SampleType = new SampleType();
			co2SampleType.setName("CO2");
			co2SampleType.setDescription("");
			co2SampleType.setHasAcidTemps(false);
			sampleTypeDao.create(co2SampleType);

			SampleType dolomiteSampleType = new SampleType();
			dolomiteSampleType.setName("Dolomite");
			dolomiteSampleType.setDescription("");
			dolomiteSampleType.setHasAcidTemps(true);
			sampleTypeDao.create(dolomiteSampleType);

			SampleType magnesiteSampleType = new SampleType();
			magnesiteSampleType.setName("Magnesite");
			magnesiteSampleType.setDescription("");
			magnesiteSampleType.setHasAcidTemps(true);
			sampleTypeDao.create(magnesiteSampleType);

			SampleType phosphateSampleType = new SampleType();
			phosphateSampleType.setName("Phosphate");
			phosphateSampleType.setDescription("Carbon-fluoro apatite");
			phosphateSampleType.setHasAcidTemps(true);
			sampleTypeDao.create(phosphateSampleType);

			TableUtils.createTable(connectionSource, AcidTemp.class);
			Dao<AcidTemp,Integer> acidTempDao = DaoManager.createDao(connectionSource, AcidTemp.class);

			AcidTemp acidTemp = new AcidTemp();
			acidTemp.setTemperature(90.0);
			acidTemp.setSampleTypeId(aragoniteSampleType.getId());
			acidTemp.setDescription("");
			HashMap<Integer,NumericValue> values = new HashMap<Integer,NumericValue>();
			values.put(AcidTempParameter.δ18O.ordinal(), new NumericValue(1.008541256, "Kim et al. 2007"));
			values.put(AcidTempParameter.clumped.ordinal(), new NumericValue(0.069, "Guo et al. 2009 and Wacker et al. 2013"));
			acidTemp.setValues(values);
			acidTempDao.create(acidTemp);

			acidTemp = new AcidTemp();
			acidTemp.setTemperature(90.0);
			acidTemp.setSampleTypeId(calciteSampleType.getId());
			acidTemp.setDescription("");
			values = new HashMap<Integer,NumericValue>();
			values.put(AcidTempParameter.δ18O.ordinal(), new NumericValue(1.008128581, "Kim et al. 2007"));
			values.put(AcidTempParameter.clumped.ordinal(), new NumericValue(0.069, "Guo et al. 2009 and Wacker et al. 2013"));
			acidTemp.setValues(values);
			acidTempDao.create(acidTemp);

			acidTemp = new AcidTemp();
			acidTemp.setTemperature(80.0);
			acidTemp.setSampleTypeId(calciteSampleType.getId());
			acidTemp.setDescription("");
			values = new HashMap<Integer,NumericValue>();
			values.put(AcidTempParameter.δ18O.ordinal(), new NumericValue(1.008410826, "Kim et al. 2007"));
			values.put(AcidTempParameter.clumped.ordinal(), new NumericValue(0.061, "Guo et al. 2009 and Wacker et al. 2013"));
			acidTemp.setValues(values);
			acidTempDao.create(acidTemp);

			acidTemp = new AcidTemp();
			acidTemp.setTemperature(70.0);
			acidTemp.setSampleTypeId(calciteSampleType.getId());
			acidTemp.setDescription("");
			values = new HashMap<Integer,NumericValue>();
			values.put(AcidTempParameter.δ18O.ordinal(), new NumericValue(1.008714222, "Kim et al. 2007"));
			values.put(AcidTempParameter.clumped.ordinal(), new NumericValue(0.052, "Guo et al. 2009 and Wacker et al. 2013"));
			acidTemp.setValues(values);
			acidTempDao.create(acidTemp);

			acidTemp = new AcidTemp();
			acidTemp.setTemperature(25.0);
			acidTemp.setSampleTypeId(calciteSampleType.getId());
			acidTemp.setDescription("");
			values = new HashMap<Integer,NumericValue>();
			values.put(AcidTempParameter.δ18O.ordinal(), new NumericValue(1.0103, "Kim et al. 2007"));
			acidTemp.setValues(values);
			acidTempDao.create(acidTemp);

			acidTemp = new AcidTemp();
			acidTemp.setTemperature(90.0);
			acidTemp.setSampleTypeId(dolomiteSampleType.getId());
			acidTemp.setDescription("");
			values = new HashMap<Integer,NumericValue>();
			values.put(AcidTempParameter.δ18O.ordinal(), new NumericValue(1.0093, "Rosenbaum & Sheppard 1986"));
			values.put(AcidTempParameter.clumped.ordinal(), new NumericValue(0.069, "Guo et al. 2009 and Wacker et al. 2013"));
			acidTemp.setValues(values);
			acidTempDao.create(acidTemp);

			acidTemp = new AcidTemp();
			acidTemp.setTemperature(90.0);
			acidTemp.setSampleTypeId(magnesiteSampleType.getId());
			acidTemp.setDescription("");
			values = new HashMap<Integer,NumericValue>();
			values.put(AcidTempParameter.δ18O.ordinal(), new NumericValue(1.009455, ""));
			values.put(AcidTempParameter.clumped.ordinal(), new NumericValue(0.069, "Guo et al. 2009 and Wacker et al. 2013"));
			acidTemp.setValues(values);
			acidTempDao.create(acidTemp);

			acidTemp = new AcidTemp();
			acidTemp.setTemperature(90.0);
			acidTemp.setSampleTypeId(phosphateSampleType.getId());
			acidTemp.setDescription("");
			values = new HashMap<Integer,NumericValue>();
			values.put(AcidTempParameter.δ18O.ordinal(), new NumericValue(1.007259532, "Passey et al 2007"));
			values.put(AcidTempParameter.clumped.ordinal(), new NumericValue(0.069, "Guo et al. 2009 and Wacker et al. 2013"));
			acidTemp.setValues(values);
			acidTempDao.create(acidTemp);

			acidTemp = new AcidTemp();
			acidTemp.setTemperature(50.0);
			acidTemp.setSampleTypeId(phosphateSampleType.getId());
			acidTemp.setDescription("");
			values = new HashMap<Integer,NumericValue>();
			values.put(AcidTempParameter.δ18O.ordinal(), new NumericValue(1.008874316, "Passey et al 2007"));
			acidTemp.setValues(values);
			acidTempDao.create(acidTemp);

		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, InitializedHandler.class, "Error during initialization in admin plugin.", e);
		}

		return null;
	}
}
