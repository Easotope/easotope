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

package org.easotope.shared.core.scratchpad;

import java.util.ArrayList;

import org.easotope.shared.math.Statistics;

public class Accumulator {
	private Pad owningPad;
	private String property;
	private boolean isRecursive;
	private double[] frozenValues;

	Accumulator(Pad owningPad, String property, boolean isRecursive) {
		this.owningPad = owningPad;
		this.property = property;
		this.isRecursive = isRecursive;
	}

	Accumulator(double meanValue, double stdDevSampleValue, double stdErrValue) {
		this.frozenValues = new double[] { meanValue, stdDevSampleValue, stdErrValue };
	}

	Pad getOwningPad() {
		return owningPad;
	}

	String getProperty() {
		return property;
	}

	boolean isRecursive() {
		return isRecursive;
	}

	public double[] getMeanStdDevSampleAndStdError() {
		if (frozenValues != null) {
			return frozenValues;
		}

		double mean = 0;
		double standardDeviationSample = 0;
		double standardErrorSample = 0;

		if (owningPad.children.size() == 1) {
			Object object = owningPad.children.get(0).getValue(property);
			
			if (object instanceof Accumulator) {
				double[] values = ((Accumulator) object).getMeanStdDevSampleAndStdError();

				mean = values[0];
				standardDeviationSample = values[1];
				standardErrorSample = values[2];
			}

		} else {
			Statistics statistics = new Statistics();
			addChildPadsToStatistics(owningPad.children, statistics);

			mean = statistics.getMean();
			standardDeviationSample = statistics.getStandardDeviationSample();
			standardErrorSample = statistics.getStandardErrorSample();
		}

		return new double[] { mean, standardDeviationSample, standardErrorSample };
	}

	private void addChildPadsToStatistics(ArrayList<Pad> children, Statistics statistics) {
		for (Pad pad : children) {
			Object object = pad.getValue(property);

			if (object instanceof Double) {
				statistics.addNumber((Double) pad.getValue(property));

			} else if (object instanceof Integer) {
				statistics.addNumber((Integer) pad.getValue(property));

			} else if (object instanceof Accumulator) {
				Accumulator accumulator = (Accumulator) object;

				if (!accumulator.isFrozen() && isRecursive) {
					addChildPadsToStatistics(pad.getChildPads(), statistics);
				} else {
					// TODO should we do something special for combining std dev and std err??
					statistics.addNumber(((Accumulator) object).getMeanStdDevSampleAndStdError()[0]);
				}
			}
		}
	}
	
	boolean isFrozen() {
		return frozenValues != null;
	}

	@Override
	public String toString() {
		return String.valueOf(getMeanStdDevSampleAndStdError()[0]);
	}
}