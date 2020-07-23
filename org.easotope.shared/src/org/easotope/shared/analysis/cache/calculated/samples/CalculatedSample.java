/*
 * Copyright © 2016-2020 by Devon Bowen.
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

package org.easotope.shared.analysis.cache.calculated.samples;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.shared.analysis.tables.CalcSampleCache;
import org.easotope.shared.core.scratchpad.ColumnOrdering;
import org.easotope.shared.core.scratchpad.FormatLookup;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.core.scratchpad.ScratchPad;

public class CalculatedSample implements Serializable {
	private static final long serialVersionUID = 1L;

	private CalcSampleCache calcSampleCache;

	public CalculatedSample(CalcSampleCache calcSampleCache) {
		this.calcSampleCache = calcSampleCache;
	}

	public int getSampleId() {
		return calcSampleCache.getSampleId();
	}

	public int getSampleAnalysisId() {
		return calcSampleCache.getSampleAnalysisId();
	}

	public ScratchPad<SamplePad> getSampleScratchPad() {
		return calcSampleCache.getScratchPad();
	}

	public ColumnOrdering getColumnOrdering() {
		return calcSampleCache.getColumnOrdering();
	}

	public FormatLookup getFormatLookup() {
		return calcSampleCache.getFormatLookup();
	}

	public String getErrorMessage() {
		return calcSampleCache.getErrorMessage();
	}

	public String getErrorSamStep() {
		return calcSampleCache.getErrorSamStep();
	}

	public ArrayList<HashMap<String, String>> getDependencies() {
		return calcSampleCache.getDependencies();
	}

	public int[] getReplicateIds() {
		return calcSampleCache.getReplicateIds();
	}

	public int[][] getPotentialRepAnalyses() {
		return calcSampleCache.getPotentialRepAnalyses();
	}
}
