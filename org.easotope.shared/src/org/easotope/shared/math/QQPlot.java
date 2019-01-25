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

package org.easotope.shared.math;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.distribution.NormalDistribution;

public class QQPlot {
	ArrayList<Point> points = new ArrayList<Point>();
	boolean needsRecalculation = false;

	public void addValue(double value) {
		points.add(new Point(value, 0.0d, null));
		needsRecalculation = true;
	}

	public void addValue(double value, Object object) {
		points.add(new Point(value, 0.0d, object));
		needsRecalculation = true;
	}

	public int getNumPoints() {
		return points.size();
	}

	public ArrayList<Point> getPoints() {
		if (needsRecalculation) {
			Statistics statistics = new Statistics();

			for (Point point : points) {
				statistics.addNumber(point.getSampleQuantile());
			}

			NormalDistribution distribution = new NormalDistribution(statistics.getMean(), statistics.getStandardDeviationSample());

			Collections.sort(points);

			int i=1;
			for (Point point : points) {
				double percentile = (double) i / ((double) points.size() + 1.0d);			
				point.setTheoreticalQuantile(distribution.inverseCumulativeProbability(percentile));
				i++;
			}
		}

		return points;
	}

	public class Point implements Comparable<Point> {
		double sampleQuantile;
		double theoreticalQuantile;
		Object object;

		Point(double sampleQuantile, double theoreticalQuantile, Object object) {
			this.sampleQuantile = sampleQuantile;
			this.theoreticalQuantile = theoreticalQuantile;
			this.object = object;
		}

		public double getSampleQuantile() {
			return sampleQuantile;
		}

		public void setTheoreticalQuantile(double theoreticalQuantile) {
			this.theoreticalQuantile = theoreticalQuantile;
		}

		public double getTheoreticalQuantile() {
			return theoreticalQuantile;
		}

		public Object getObject() {
			return object;
		}

		@Override
		public int compareTo(Point that) {
			return ((Double) this.sampleQuantile).compareTo(that.sampleQuantile);
		}
	}
}
