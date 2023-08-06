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

package org.easotope.shared.math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;

public class MultiLineCommonSlopeRegression {
	private HashSet<Integer> groups = new HashSet<Integer>();
	private ArrayList<Integer> groupList = new ArrayList<Integer>();
	private ArrayList<Double> xList = new ArrayList<Double>();
	private ArrayList<Double> yList = new ArrayList<Double>();

	private double slope;
	private HashMap<Integer,Double> intercepts = null;
	private double r2;

	public void addPoint(int group, double x, double y) {
		groups.add(group);

		this.groupList.add(group);		
		this.xList.add(x);
		this.yList.add(y);
		
		intercepts = null;
	}

	private void calculate() {
		if (intercepts != null || yList.size() < 2) {
			return;
		}

		double[] yArray = new double[yList.size()];

		for (int i=0; i<yList.size(); i++) {
			yArray[i] = yList.get(i);
		}

		double[][] xArray = new double[xList.size()][groups.size()];
		Vector<Integer> columnToGroup = new Vector<Integer>(groups);
		HashMap<Integer,Integer> groupToColumn = new HashMap<Integer,Integer>();

		int column = 0;
		for (Integer group : columnToGroup) {
			groupToColumn.put(group, column++);
		}

		for (int i=0; i<xList.size(); i++) {
			int group = groupList.get(i);
			column = groupToColumn.get(group);

			double x = xList.get(i);
			xArray[i][0] = x;

			if (column != 0) {
				xArray[i][groupToColumn.get(group)] = 1.0d;
			}
		}
		
		OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
		regression.newSampleData(yArray, xArray);
		double[] regressionParameters = null;

		try {
			regressionParameters = regression.estimateRegressionParameters();
		} catch (Exception e) {
			Log.getInstance().log(Level.INFO, this, "regression failed", e);
			return;
		}
		
		slope = regressionParameters[1];

		intercepts = new HashMap<Integer,Double>();
		intercepts.put(columnToGroup.get(0), regressionParameters[0]);

		for (int i=2; i<regressionParameters.length; i++) {
			intercepts.put(columnToGroup.get(i-1), regressionParameters[0] + regressionParameters[i]);
		}

		r2 = regression.calculateRSquared();
	}

	public double getSlope() {
		calculate();
		return slope;
	}

	public HashMap<Integer,Double> getIntercepts() {
		calculate();
		return intercepts;
	}

	public double getR2() {
		calculate();
		return r2;
	}

	public boolean getInvalid() {
		calculate();
		return intercepts == null;
	}
}
