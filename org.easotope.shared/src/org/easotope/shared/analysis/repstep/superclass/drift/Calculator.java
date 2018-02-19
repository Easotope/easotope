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

package org.easotope.shared.analysis.repstep.superclass.drift;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.shared.admin.StandardParameter;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.superclass.drift.dependencies.Dependencies;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.math.LinearRegression;
import org.easotope.shared.math.Statistics;

public abstract class Calculator extends RepStepCalculator {
	public final static String PARAMETER_APPLY_STRETCHING = "PARAMETER_APPLY_STRETCHING";

	public static final int DEFAULT_WINDOW_TYPE = WindowType.Window.ordinal();
	public static final int DEFAULT_MIN_NUM_STANDARDS_BEFORE_AFTER = 3;
	public static final int[] DEFAULT_STANDARD_IDS = new int[0];
	public static final boolean DEFAULT_APPLY_STRETCHING = true;

	public abstract VolatileKeys getVolatiles();
	public abstract String getInputLabel();
	public abstract String getOutputLabel();
	public abstract StandardParameter getStandardParameter();
	public abstract Double getExpectedStandardValue(Standard standard, DependencyManager dependencyManager);

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
		Object parameter = (Object) getParameter(PARAMETER_STANDARD_IDS);
		return parameter == null ? new int[0] : (int[]) parameter;
	}

	public boolean getApplyStretching() {
		Boolean parameter = (Boolean) getParameter(PARAMETER_APPLY_STRETCHING);
		return parameter == null ? DEFAULT_APPLY_STRETCHING : parameter;
	}

	@Override
	public DependencyManager getDependencyManager(ReplicatePad[] replicatePads, int sampleNumber) {
		return new Dependencies();
	}

	@Override
	public void calculate(ReplicatePad[] replicatePads, int targetPadNumber, DependencyManager dependencyManager) {
		HashMap<Integer,Double> standardIdToExpectedValue = new HashMap<Integer,Double>();
		HashMap<Integer,Standard> standardIdToStandard = new HashMap<Integer,Standard>();

		for (Standard standard : ((Dependencies) dependencyManager).getStandards()) {
			standardIdToStandard.put(standard.getId(), standard);
			standardIdToExpectedValue.put(standard.getId(), getExpectedStandardValue(standard, dependencyManager));
		}

		HashSet<Integer> acceptableStandardIds = new HashSet<Integer>();

		for (int id : getStandardIds()) {
			if (standardIdToExpectedValue.get(id) != null) {
				acceptableStandardIds.add(id);
			}
		}

		StandardReplicatePads standardReplicatePads = getStandardReplicatePads(replicatePads, targetPadNumber, getWindowType(), getMinNumStandardsBeforeAfter(), new SimpleStandardVerifier(acceptableStandardIds));
		HashSet<Integer> uniqueStandardIds = new HashSet<Integer>();
		HashSet<DriftPoint> driftPoints = new HashSet<DriftPoint>();

		for (ReplicatePad replicatePad : standardReplicatePads.getUsable()) {
			int standardId = replicatePad.getSourceId();
			double expectedValue = standardIdToExpectedValue.get(standardId);
			Double measuredValue = getDouble(replicatePad, getInputLabel());
			Standard standard = standardIdToStandard.get(standardId);

			uniqueStandardIds.add(replicatePad.getSourceId());
			driftPoints.add(new DriftPoint(replicatePad.getReplicateId(), standard, expectedValue, measuredValue, replicatePad.getDate(), false));
		}

		Double value = getDouble(replicatePads[targetPadNumber], getInputLabel());
		double offset = 0.0d;

		if (driftPoints.size() != 0) {
			if (uniqueStandardIds.size() > 1 && getApplyStretching()) {
				offset = getStretchedOffset(replicatePads, targetPadNumber, driftPoints, value);
			} else {
				offset = getUnstretchedOffset(replicatePads, targetPadNumber, driftPoints);
			}
		}

		for (ReplicatePad replicatePad : standardReplicatePads.getDisabled()) {
			int standardId = replicatePad.getSourceId();
			double expectedValue = standardIdToExpectedValue.get(standardId);
			Double measuredValue = getDouble(replicatePad, getInputLabel());
			Standard standard = standardIdToStandard.get(standardId);

			driftPoints.add(new DriftPoint(replicatePad.getReplicateId(), standard, expectedValue, measuredValue, replicatePad.getDate(), true));
		}

		replicatePads[targetPadNumber].setVolatileData(getVolatiles().getVolatileDataDriftPointsKey(), driftPoints);
		replicatePads[targetPadNumber].setVolatileData(getVolatiles().getVolatileDataOffsetKey(), offset);

		if (value != null) {
			double oldValue = value;
			value += offset;
			boolean isDisabled = (Boolean) replicatePads[targetPadNumber].getValue(Pad.DISABLED);
			driftPoints.add(new DriftPoint(replicatePads[targetPadNumber].getReplicateId(), null, value, oldValue, replicatePads[targetPadNumber].getDate(), isDisabled));
		}

		replicatePads[targetPadNumber].setValue(labelToColumnName(getOutputLabel()), value);
	}

	private double getStretchedOffset(ReplicatePad[] replicatePads, int padNumber, HashSet<DriftPoint> driftPoints, double measuredValue) {
		LinearRegression linearRegression = new LinearRegression();

		for (DriftPoint driftPoint : driftPoints) {
			linearRegression.addCoordinate(driftPoint.getMeasuredValue(), driftPoint.getExpectedValue());
		}

		if (linearRegression.isInvalid()) {
			return getUnstretchedOffset(replicatePads, padNumber, driftPoints);
		}

		replicatePads[padNumber].setVolatileData(getVolatiles().getVolatileDataLinearRegressionKey(), linearRegression);
		double expected = linearRegression.getSlope() * measuredValue + linearRegression.getIntercept();

		return expected - measuredValue;
	}

	private double getUnstretchedOffset(ReplicatePad[] replicatePads, int padNumber, HashSet<DriftPoint> driftPoints) {
		Statistics statistics = new Statistics();

		for (DriftPoint driftPoint : driftPoints) {
			statistics.addNumber(driftPoint.getExpectedValue() - driftPoint.getMeasuredValue());
		}

		return statistics.getSampleSize() != 0 ? statistics.getMean() : 0.0d;
	}

	public class DriftPoint {
		private int replicateId;
		private Standard standard;
		private double expectedValue;
		private double measuredValue;
		private long date;
		private boolean disabled;

		public DriftPoint(int replicateId, Standard standard, double expectedValue, double measuredValue, long date, boolean disabled) {
			this.replicateId = replicateId;
			this.standard = standard;
			this.expectedValue = expectedValue;
			this.measuredValue = measuredValue;
			this.date = date;
			this.disabled = disabled;
		}

		public int getReplicateId() {
			return replicateId;
		}

		public Standard getStandard() {
			return standard;
		}

		public double getExpectedValue() {
			return expectedValue;
		}

		public double getMeasuredValue() {
			return measuredValue;
		}

		public long getDate() {
			return date;
		}
		
		public boolean getDisabled() {
			return disabled;
		}
	}
}
