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

package org.easotope.shared.analysis.repstep.co2.d48offset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.co2.d48offset.dependencies.Dependencies;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.math.MultiLineCommonSlopeRegression;

public class Calculator extends RepStepCalculator {
	public static final String INPUT_LABEL_SAMPLE_δ48 = "Sample δ48";
	public static final String INPUT_LABEL_SAMPLE_Δ48 = "Sample Δ48";
	public static final String INPUT_LABEL_STANDARD_δ48 = "Standard δ48";
	public static final String INPUT_LABEL_STANDARD_Δ48 = "Standard Δ48";
	public static final String OUTPUT_LABEL_Δ48 = "Δ48 Nonlinearity Corrected";
	public static final String OUTPUT_LABEL_Δ48_OFFSET_AVERAGE = "Δ48 Offset Average";

	public static final int DEFAULT_WINDOW_TYPE = WindowType.Window.ordinal();
	public static final int DEFAULT_MIN_NUM_STANDARDS_BEFORE_AFTER = 10;
	public static final HashMap<Integer,Integer> DEFAULT_STANDARD_IDS = new HashMap<Integer,Integer>();

	public static String getVolatileDataD48OffsetPointsKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_D48_OFFSET_POINTS";
	}

	public static String getVolatileDataLinearRegressionKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_LINEAR_REGRESSION";
	}

	public static String getVolatileDataD48CorrectionKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_D48_CORRECTION";
	}

	public static String getVolatileDataD48OffsetKey() {
		return Calculator.class.getName() + "VOLATILE_DATA_D48_OFFSET";
	}

	public WindowType getWindowType() {
		Integer parameter = (Integer) getParameter(PARAMETER_WINDOW_TYPE);
		return parameter == null ? WindowType.values()[DEFAULT_WINDOW_TYPE] : WindowType.values()[parameter];
	}

	public int getMinNumStandardsBeforeAfter() {
		Integer parameter = (Integer) getParameter(PARAMETER_MIN_NUM_STANDARDS_BEFORE_AFTER);
		return parameter == null ? DEFAULT_MIN_NUM_STANDARDS_BEFORE_AFTER : parameter;
	}

	public HashMap<Integer,Integer> getGroups() {
		HashMap<Integer,Integer> groups = null;
		Object obj = (Object) getParameter(PARAMETER_STANDARD_IDS);

		if (obj == null) {
			groups = new HashMap<Integer,Integer>();

		} else if (obj instanceof int[]) {
			int[] ids = (int[]) obj;
			groups = new HashMap<Integer,Integer>();

			for (int id : ids) {
				groups.put(id, 1);
			}

		} else {
			@SuppressWarnings("unchecked")
			HashMap<Integer,Integer> tmp = (HashMap<Integer,Integer>) obj;
			groups = tmp;
		}
		
		return groups;
	}

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	@Override
	public DependencyManager getDependencyManager(ReplicatePad[] replicatePads, int standardNumber) {
		return new Dependencies();
	}

	@Override
	public void calculate(ReplicatePad[] replicatePads, int targetPadNumber, DependencyManager dependencyManager) {
		HashMap<Integer,Standard> standardIdToStandard = new HashMap<Integer,Standard>();

		for (Standard standard : ((Dependencies) dependencyManager).getStandards()) {
			standardIdToStandard.put(standard.getId(), standard);
		}

		HashSet<Integer> acceptableStandardIds = new HashSet<Integer>();
		HashMap<Integer,Integer> groups = getGroups();

		for (int id : groups.keySet()) {
			Integer value = groups.get(id);

			if (value != null && value != 0) {
				acceptableStandardIds.add(id);
			}
		}

		MultiLineCommonSlopeRegression regression = new MultiLineCommonSlopeRegression();
		HashSet<D48OffsetPoint> d48OffsetPoints = new HashSet<D48OffsetPoint>();
		StandardReplicatePads standardReplicatePads = getStandardReplicatePads(replicatePads, targetPadNumber, getWindowType(), getMinNumStandardsBeforeAfter(), new D48OffsetStandardVerifier(acceptableStandardIds));

		for (ReplicatePad replicatePad : standardReplicatePads.getUsable()) {
			int standardId = replicatePad.getSourceId();
			Double δ48 = getDouble(replicatePad, INPUT_LABEL_STANDARD_δ48);
			Double Δ48 = getDouble(replicatePad, INPUT_LABEL_STANDARD_Δ48);

			regression.addPoint(groups.get(standardId), δ48, Δ48);

			Standard standard = standardIdToStandard.get(standardId);
			d48OffsetPoints.add(new D48OffsetPoint(standard, replicatePad.getDate(), δ48, Δ48, standard.getColorId(), standard.getShapeId(), false));
		}

		for (ReplicatePad replicatePad : standardReplicatePads.getDisabled()) {
			int standardId = replicatePad.getSourceId();
			Double δ48 = getDouble(replicatePad, INPUT_LABEL_STANDARD_δ48);
			Double Δ48 = getDouble(replicatePad, INPUT_LABEL_STANDARD_Δ48);

			if (!Double.isNaN(δ48) && !Double.isNaN(Δ48)) {
				Standard standard = standardIdToStandard.get(standardId);
				d48OffsetPoints.add(new D48OffsetPoint(standard, replicatePad.getDate(), δ48, Δ48, standard.getColorId(), standard.getShapeId(), true));
			}
		}

		Double δ48 = getDouble(replicatePads[targetPadNumber], INPUT_LABEL_SAMPLE_δ48);
		Double Δ48 = getDouble(replicatePads[targetPadNumber], INPUT_LABEL_SAMPLE_Δ48);
		double offsetAverage = 0.0d;

		if (!regression.getInvalid()) {
			Double correction = -δ48 * regression.getSlope();
			Δ48 += correction;

			HashMap<Integer,Double> intercepts = regression.getIntercepts();
			Set<Integer> groupKeys = intercepts.keySet();

			for (int group : groupKeys) {
				double intercept = intercepts.get(group);
				offsetAverage += Δ48 - intercept;
			}

			if (groupKeys.size() == 0) {
				offsetAverage = Double.NaN;
			} else {
				offsetAverage /= groupKeys.size();
			}

			replicatePads[targetPadNumber].setVolatileData(getVolatileDataLinearRegressionKey(), regression);
			replicatePads[targetPadNumber].setVolatileData(getVolatileDataD48CorrectionKey(), correction);
			replicatePads[targetPadNumber].setVolatileData(getVolatileDataD48OffsetKey(), offsetAverage);
		}

		d48OffsetPoints.add(new D48OffsetPoint(null, replicatePads[targetPadNumber].getDate(), δ48, Δ48, -1, -1, false));
		replicatePads[targetPadNumber].setVolatileData(getVolatileDataD48OffsetPointsKey(), d48OffsetPoints);

		replicatePads[targetPadNumber].setValue(labelToColumnName(OUTPUT_LABEL_Δ48_OFFSET_AVERAGE), offsetAverage);
		replicatePads[targetPadNumber].setValue(labelToColumnName(OUTPUT_LABEL_Δ48), Δ48);
	}

	public class D48OffsetPoint {
		private Standard standard;
		private long date;
		private double δ48;
		private double Δ48;
		private int colorId;
		private int shapeId;
		private boolean disabled;

		public D48OffsetPoint(Standard standard, long date, double d48, double D48, int colorId, int shapeId, boolean disabled) {
			this.standard = standard;
			this.date = date;
			this.δ48 = d48;
			this.Δ48 = D48;
			this.colorId = colorId;
			this.shapeId = shapeId;
			this.disabled = disabled;
		}

		public Standard getStandard() {
			return standard;
		}

		public long getDate() {
			return date;
		}

		public double getδ48() {
			return δ48;
		}

		public double getΔ48() {
			return Δ48;
		}

		public int getColorId() {
			return colorId;
		}

		public int getShapeId() {
			return shapeId;
		}

		public boolean getDisabled() {
			return disabled;
		}
	}

	public class D48OffsetStandardVerifier extends SimpleStandardVerifier {
		public D48OffsetStandardVerifier(HashSet<Integer> potentialStandardIds) {
			super(potentialStandardIds);
		}

		@Override
		public boolean replicatePadIsAcceptable(ReplicatePad replicatePad) {
			Double δ48 = getDouble(replicatePad, INPUT_LABEL_STANDARD_δ48);
			Double Δ48 = getDouble(replicatePad, INPUT_LABEL_STANDARD_Δ48);

			if (δ48 != null && !Double.isNaN(δ48) && Δ48 != null && !Double.isNaN(Δ48)) {
				return true;
			}

			return false;
		}
	}
}
