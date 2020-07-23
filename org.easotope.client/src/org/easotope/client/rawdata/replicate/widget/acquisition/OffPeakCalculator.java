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

package org.easotope.client.rawdata.replicate.widget.acquisition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.easotope.shared.math.Statistics;
import org.easotope.shared.rawdata.InputParameter;

public class OffPeakCalculator {
	private final double NUM_STANDARD_DEVIATIONS_LIMIT = 3.0d;
	private final int MIN_NUMBER_OF_ON_PEAK_CYCLES_REQUIRED = 4;

	private ArrayList<Integer> offPeakCycles = new ArrayList<Integer>();

	public OffPeakCalculator(HashMap<InputParameter,Double[]> measurements) {
		lookForOffPeak(measurements, true);

		if (offPeakCycles.size() == 0) {
			lookForOffPeak(measurements, false);
		}
	}

	public void lookForOffPeak(HashMap<InputParameter,Double[]> measurements, boolean nullComesFirst) {
		int numCycles = -1;

		HashMap<InputParameter,Measurement[]> verticals = new HashMap<InputParameter,Measurement[]>();
		Vector<HashSet<Integer>> horizontals = new Vector<HashSet<Integer>>();

		for (InputParameter inputParameter : measurements.keySet()) {
			Double[] doubles = measurements.get(inputParameter);

			if (numCycles == -1) {
				numCycles = doubles.length;

				if (numCycles < MIN_NUMBER_OF_ON_PEAK_CYCLES_REQUIRED + 1) {
					return;
				}

				horizontals.setSize(numCycles);

				for (int i=0; i<numCycles; i++) {
					horizontals.set(i, new HashSet<Integer>());
				}
			}

			Measurement[] sortedMeasurements = new Measurement[numCycles];

			for (int i=0; i<doubles.length; i++) {
				sortedMeasurements[i] = new Measurement(i, doubles[i], nullComesFirst);
			}

			Arrays.sort(sortedMeasurements);
			verticals.put(inputParameter, sortedMeasurements);

			for (int i=0; i<sortedMeasurements.length; i++) {
				horizontals.get(i).add(sortedMeasurements[i].cycle);
			}
		}
		
		int maxPossibleOffPeak = numCycles - MIN_NUMBER_OF_ON_PEAK_CYCLES_REQUIRED;

		for (int row=0; row<maxPossibleOffPeak; row++) {
			if (hasCycleBreakAfterRow(row, verticals, horizontals) && hasStatisticalBreakAfterRow(row, verticals, horizontals)) {
				HashSet<Integer> upperCycles = new HashSet<Integer>();

				for (int j=0; j<=row; j++) {
					upperCycles.addAll(horizontals.get(j));
				}

				offPeakCycles.addAll(upperCycles);
				return;
			}
		}
	}

	private boolean hasCycleBreakAfterRow(int row, HashMap<InputParameter,Measurement[]> verticals, Vector<HashSet<Integer>> horizontals) {
		HashSet<Integer> upperCycles = new HashSet<Integer>();
		HashSet<Integer> lowerCycles = new HashSet<Integer>();

		for (int j=0; j<horizontals.size(); j++) {
			if (j <= row) {
				upperCycles.addAll(horizontals.get(j));
			} else {
				lowerCycles.addAll(horizontals.get(j));
			}
		}

		for (Integer integer : upperCycles) {
			if (lowerCycles.contains(integer)) {
				return false;
			}
		}

		return true;
	}

	private boolean hasStatisticalBreakAfterRow(int row, HashMap<InputParameter,Measurement[]> verticals, Vector<HashSet<Integer>> horizontals) {
		for (InputParameter inputParameter : verticals.keySet()) {
			Measurement[] measurements = verticals.get(inputParameter);

			if (measurements[row].value == null) {
				continue;
			}

			Statistics statistics = new Statistics();

			for (int i=row+1; i<measurements.length; i++) {
				if (measurements[i].value != null) {
					statistics.addNumber(measurements[i].value);
				}
			}

			double distance = Math.abs(statistics.getMean() - measurements[row].value);
			double distanceLimit = NUM_STANDARD_DEVIATIONS_LIMIT * statistics.getStandardDeviationSample();

			if (distance < distanceLimit) {
				return false;
			}
		}

		return true;
	}

	public ArrayList<Integer> getOffPeakCycles() {
		return offPeakCycles;
	}

	private class Measurement implements Comparable<Measurement> {
		private int cycle;
		private Double value;
		boolean nullComesFirst;

		Measurement(int cycle, Double value, boolean nullComesFirst) {
			this.cycle = cycle;
			this.value = value;
			this.nullComesFirst = nullComesFirst;
		}

		@Override
		public int compareTo(Measurement that) {
			if (this.value == null && that.value == null) {
				return 0;
			}

			if (this.value == null || that.value == null) {
				if (this.value == null && nullComesFirst || that.value == null && !nullComesFirst) {
					return Integer.MIN_VALUE;
				} else {
					return Integer.MAX_VALUE;
				}
			}

			return this.value.compareTo(that.value);
		}
	}
}
