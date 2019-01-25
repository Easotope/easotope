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

package org.easotope.shared.analysis.repstep.co2.d48offsetpbl;

import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.math.Statistics;

public class Calculator extends RepStepCalculator {
	public static final String INPUT_LABEL_SAMPLE_Δ48 = "Sample Δ48";
	public static final String INPUT_LABEL_STANDARD_Δ48 = "Standard Δ48";
	public static final String OUTPUT_LABEL_Δ48_OFFSET = "Δ48 Offset";

	public static final int DEFAULT_WINDOW_TYPE = WindowType.Window.ordinal();
	public static final int DEFAULT_MIN_NUM_STANDARDS_BEFORE_AFTER = 10;
	public static final int[] DEFAULT_STANDARD_IDS = new int[0];

	public static String getVolatileDataD48OffsetPointsKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_D48_OFFSET_POINTS";
	}

	public static String getVolatileDataStatisticsKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_STATISTICS";
	}

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	public WindowType getWindowType() {
		Integer parameter = (Integer) getParameter(PARAMETER_WINDOW_TYPE);
		return parameter == null ? WindowType.values()[DEFAULT_WINDOW_TYPE] : WindowType.values()[parameter];
	}

	public int getMinNumStandardsBeforeAfter() {
		Integer parameter = (Integer) getParameter(PARAMETER_MIN_NUM_STANDARDS_BEFORE_AFTER);
		return parameter == null ? DEFAULT_MIN_NUM_STANDARDS_BEFORE_AFTER : parameter;
	}

	public int[] getStandardIds() {
		int[] ids = new int[0];
		Object parameter = (Object) getParameter(PARAMETER_STANDARD_IDS);

		if (parameter instanceof int[]) {
			ids = (int[]) parameter;

		} else if (parameter instanceof HashMap<?,?>) {
			@SuppressWarnings("unchecked")
			HashMap<Integer,Integer> hash = (HashMap<Integer,Integer>) parameter;
			ids = new int[hash.size()];
			int count = 0;

			for (int key : hash.keySet()) {
				ids[count++] = key;
			}
		}

		return ids;
	}

	@Override
	public DependencyManager getDependencyManager(ReplicatePad[] replicatePads, int sampleNumber) {
		return null;
	}

	@Override
	public void calculate(ReplicatePad[] replicatePads, int targetPadNumber, DependencyManager dependencyManager) {
		Double sampleΔ48 = getDouble(replicatePads[targetPadNumber], INPUT_LABEL_SAMPLE_Δ48);

		StandardReplicatePads standardReplicatePads = getStandardReplicatePads(replicatePads, targetPadNumber, getWindowType(), getMinNumStandardsBeforeAfter(), new SimpleStandardVerifier(getStandardIds()));
		Statistics statistics = new Statistics();
		ArrayList<D48OffsetPoint> d48OffsetPoints = new ArrayList<D48OffsetPoint>();

		for (ReplicatePad replicatePad : standardReplicatePads.getUsable()) {
			Double value = getDouble(replicatePad, INPUT_LABEL_STANDARD_Δ48);

			if (value != null) {
				statistics.addNumber(value);
				d48OffsetPoints.add(new D48OffsetPoint(replicatePad.getSourceId(), replicatePad.getDate(), value, false));
			}
		}

		for (ReplicatePad replicatePad : standardReplicatePads.getDisabled()) {
			Double value = getDouble(replicatePad, INPUT_LABEL_STANDARD_Δ48);

			if (value != null) {
				d48OffsetPoints.add(new D48OffsetPoint(replicatePad.getSourceId(), replicatePad.getDate(), value, true));
			}
		}

		if (sampleΔ48 != null) {
			if (statistics.getSampleSize() != 0) {
				replicatePads[targetPadNumber].setValue(labelToColumnName(OUTPUT_LABEL_Δ48_OFFSET), sampleΔ48 - statistics.getMean());
			} else {
				replicatePads[targetPadNumber].setValue(labelToColumnName(OUTPUT_LABEL_Δ48_OFFSET), Double.NaN);
			}
		} else {
			replicatePads[targetPadNumber].setValue(labelToColumnName(OUTPUT_LABEL_Δ48_OFFSET), Double.NaN);
		}

		replicatePads[targetPadNumber].setVolatileData(getVolatileDataD48OffsetPointsKey(), d48OffsetPoints);
		replicatePads[targetPadNumber].setVolatileData(getVolatileDataStatisticsKey(), statistics);
	}

	public class D48OffsetPoint {
		private int standardId;
		private long date;
		private double Δ48;
		private boolean disabled;

		public D48OffsetPoint(int standardId, long date, double D48, boolean disabled) {
			this.standardId = standardId;
			this.date = date;
			this.Δ48 = D48;
			this.disabled = disabled;
		}

		public int getStandardId() {
			return standardId;
		}

		public long getDate() {
			return date;
		}

		public double getΔ48() {
			return Δ48;
		}

		public boolean isDisabled() {
			return disabled;
		}
	}
}
