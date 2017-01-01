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

package org.easotope.shared.analysis.repstep.generic.replicate.dependencies;

import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.generic.replicate.dependencies.SourceAndSampleTypePlugin.SourceAndSampleType;

public class Dependencies extends DependencyManager {
	public Dependencies() {
		addPlugin(new MassSpecPlugin());
		addPlugin(new SourceAndSampleTypePlugin());
		addPlugin(new CorrIntervalPlugin());
		addPlugin(new AcidInfoPlugin());
	}

	public String getMassSpec() {
		return (String) getDependencyPlugins().get(0).getObject();
	}

	public String getSource() {
		SourceAndSampleType sourceAndSampleType = (SourceAndSampleType) getDependencyPlugins().get(1).getObject();
		return sourceAndSampleType.getSource();
	}

	public String getSampleType() {
		SourceAndSampleType sourceAndSampleType = (SourceAndSampleType) getDependencyPlugins().get(1).getObject();
		return sourceAndSampleType.getSampleType();
	}

	public Long getCorrIntervalTimeStamp() {
		return (Long) getDependencyPlugins().get(2).getObject();
	}

	public Double getAcidTemperature() {
		AcidTemp acidTemp = (AcidTemp) getDependencyPlugins().get(3).getObject();

		if (acidTemp == null) {
			return null;
		}

		return acidTemp.getTemperature();
	}
}
