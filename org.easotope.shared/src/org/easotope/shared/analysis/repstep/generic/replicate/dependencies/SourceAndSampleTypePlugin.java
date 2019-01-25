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

package org.easotope.shared.analysis.repstep.generic.replicate.dependencies;

import java.util.HashMap;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.Messages;
import org.easotope.shared.admin.cache.sampletype.SampleTypeCache;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.tables.SampleType;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.execute.dependency.DependencyPlugin;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.tables.Sample;

public class SourceAndSampleTypePlugin extends DependencyPlugin {
	private Sample sample = null;
	private Standard standard = null;
	private String source = null;
	private SampleType sampleType = null;
	private SourceAndSampleType sourceAndSampleType = null;

	SourceAndSampleTypePlugin() {
		super("source and sample type");
	}

	@Override
	public Object getObject() {
		return sourceAndSampleType;
	}

	@Override
	public HashMap<String,String> getPrintableValues(DependencyManager dependencyManager) {
		HashMap<String,String> result = new HashMap<String,String>();

		result.put(Messages.sourceAndSampleTypePlugin_source, sourceAndSampleType.getSource());
		result.put(Messages.sourceAndSampleTypePlugin_sampleType, sourceAndSampleType.getSampleType());

		return result;
	}

	@Override
	public int requestObject(DependencyManager dependencyManager) {
		int sampleId = dependencyManager.getReplicate().getSampleId();

		if (sampleId != DatabaseConstants.EMPTY_DB_ID) {
			return InputCache.getInstance().sampleGet(dependencyManager, sampleId);
		}

		int standardId = dependencyManager.getReplicate().getStandardId();

		return StandardCache.getInstance().standardGet(standardId, dependencyManager);
	}

	@Override
	public int receivedObject(DependencyManager dependencyManager, Object object) {
		if (object instanceof Sample) {
			sample = (Sample) object;
			source = sample.getName();
			return SampleTypeCache.getInstance().sampleTypeGet(sample.getSampleTypeId(), dependencyManager);

		} else if (object instanceof Standard) {
			standard = (Standard) object;
			source = standard.getName();
			return SampleTypeCache.getInstance().sampleTypeGet(standard.getSampleTypeId(), dependencyManager);

		} else if (object instanceof SampleType) {
			sampleType = (SampleType) object;
			sourceAndSampleType = new SourceAndSampleType(source, sampleType.getName());
		}

		return Command.UNDEFINED_ID;
	}

	@Override
	public boolean verifyCurrentObject(DependencyManager dependencyManager) {
		return sourceAndSampleType != null;
	}

	@Override
	public AbstractCache[] getCachesToListenTo() {
		return new AbstractCache[] { InputCache.getInstance(), StandardCache.getInstance(), SampleTypeCache.getInstance() };
	}

	@Override
	public boolean isNoLongerValid(DependencyManager dependencyManager, Object object) {
		if (object instanceof Sample) {
			Sample newSample = (Sample) object;

			if (sample != null && sample.getId() == newSample.getId()) {
				return true;
			}

		} else if (object instanceof Standard) {
			Standard newStandard = (Standard) object;

			if (standard != null && standard.getId() == newStandard.getId()) {
				return true;
			}

		} else if (object instanceof SampleType) {
			SampleType newSampleType = (SampleType) object;
			
			if (sampleType != null && sampleType.getId() == newSampleType.getId()) {
				return true;
			}
		}

		return false;
	}

	class SourceAndSampleType {
		private String source = "UNDEFINED";
		private String sampleType = "UNDEFINED";

		SourceAndSampleType(String source, String sampleType) {
			this.source = source;
			this.sampleType = sampleType;
		}

		public String getSource() {
			return source;
		}

		public String getSampleType() {
			return sampleType;
		}
	}
}
