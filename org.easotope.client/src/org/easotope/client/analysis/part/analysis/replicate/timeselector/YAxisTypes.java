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

package org.easotope.client.analysis.part.analysis.replicate.timeselector;

import org.eclipse.swt.graphics.GC;

public class YAxisTypes {
	enum ItemType { NONE, SAMPLE, STANDARD, SCAN, CORRINTERVAL };

	private int samplesY;
	private int standardsY;
	private int scansY;
	private int corrIntervalBaseY;
	private int corrIntervalTopY;
	private int textTopY;

	public void setImageParams(GC gc, int canvasSizeY) {
		textTopY = canvasSizeY - gc.getFontMetrics().getHeight() - 1;
		corrIntervalBaseY = textTopY - 8;
		corrIntervalTopY = corrIntervalBaseY - 12;
		samplesY = (int) Math.round(corrIntervalTopY * 0.25);
		standardsY = (int) Math.round(corrIntervalTopY * 0.50);
		scansY = (int) Math.round(corrIntervalTopY * 0.75);
	}

	int getSamplesY() {
		return samplesY;
	}

	int getStandardsY() {
		return standardsY;
	}

	int getScansY() {
		return scansY;
	}

	int getCorrIntervalTopY() {
		return corrIntervalTopY;
	}

	int getCorrIntervalBaseY() {
		return corrIntervalBaseY;
	}

	ItemType getItemTypeForY(int y) {
		int distanceSamples = Math.abs(samplesY - y);
		int distanceStandards = Math.abs(standardsY - y);
		int distanceScans = Math.abs(scansY - y);
		int distanceCorrIntervalTopY = Math.abs(corrIntervalTopY - y);

		if (distanceSamples < distanceStandards && distanceSamples < distanceScans && distanceSamples < distanceCorrIntervalTopY) {
			return ItemType.SAMPLE;
		}

		if (distanceStandards < distanceScans && distanceStandards < distanceCorrIntervalTopY) {
			return ItemType.STANDARD;
		}

		if (distanceScans < distanceCorrIntervalTopY) {
			return ItemType.SCAN;
		}

		return ItemType.CORRINTERVAL;
	}
}
