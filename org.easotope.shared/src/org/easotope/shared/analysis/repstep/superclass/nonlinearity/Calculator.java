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

package org.easotope.shared.analysis.repstep.superclass.nonlinearity;

import java.util.HashMap;
import java.util.HashSet;

import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.superclass.nonlinearity.dependencies.Dependencies;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.math.MultiLineCommonSlopeRegression;

public abstract class Calculator extends RepStepCalculator {
	public abstract VolatileKeys getVolatiles();
	public abstract String getInputLabelSampleCorrector();
	public abstract String getInputLabelSampleCorrectee();
	public abstract String getInputLabelStandardCorrector();
	public abstract String getInputLabelStandardCorrectee();
	public abstract String getOutputLabelSlope();
	public abstract String getOutputLabelIntercepts();
	public abstract String getOutputLabel();

	public static final int DEFAULT_WINDOW_TYPE = WindowType.Window.ordinal();
	public static final int DEFAULT_MIN_NUM_STANDARDS_BEFORE_AFTER = 10;

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
		HashSet<NonlinearityPoint> nonlinearityPoints = new HashSet<NonlinearityPoint>();
		StandardReplicatePads standardReplicatePads = getStandardReplicatePads(replicatePads, targetPadNumber, getWindowType(), getMinNumStandardsBeforeAfter(), new NonlinearityStandardVerifier(acceptableStandardIds));

		for (ReplicatePad replicatePad : standardReplicatePads.getUsable()) {
			int standardId = replicatePad.getSourceId();
			Double corrector = getDouble(replicatePad, getInputLabelStandardCorrector());
			Double correctee = getDouble(replicatePad, getInputLabelStandardCorrectee());

			regression.addPoint(groups.get(standardId), corrector, correctee);

			Standard standard = standardIdToStandard.get(standardId);
			nonlinearityPoints.add(new NonlinearityPoint(standard, replicatePad.getDate(), corrector, correctee, standard.getColorId(), standard.getShapeId(), false));
		}

		for (ReplicatePad replicatePad : standardReplicatePads.getDisabled()) {
			int standardId = replicatePad.getSourceId();
			Double corrector = getDouble(replicatePad, getInputLabelStandardCorrector());
			Double correctee = getDouble(replicatePad, getInputLabelStandardCorrectee());

			if (!Double.isNaN(corrector) && !Double.isNaN(correctee)) {
				Standard standard = standardIdToStandard.get(standardId);
				nonlinearityPoints.add(new NonlinearityPoint(standard, replicatePad.getDate(), corrector, correctee, standard.getColorId(), standard.getShapeId(), true));
			}
		}

		Double corrector = getDouble(replicatePads[targetPadNumber], getInputLabelSampleCorrector());
		Double correctee = getDouble(replicatePads[targetPadNumber], getInputLabelSampleCorrectee());

		if (!regression.getInvalid()) {
			Double correction = -corrector * regression.getSlope();
			correctee += correction;

			replicatePads[targetPadNumber].setVolatileData(getVolatiles().getVolatileDataLinearRegressionKey(), regression);
			replicatePads[targetPadNumber].setVolatileData(getVolatiles().getVolatileDataNonlinearityCorrectionKey(), correction);

			replicatePads[targetPadNumber].setValue(labelToColumnName(getOutputLabelSlope()), regression.getSlope());

			String regressionIntercepts = "";
			HashMap<Integer,Double> intercepts = regression.getIntercepts();

			if (intercepts != null) {
				for (int key : intercepts.keySet()) {
					Double value = intercepts.get(key);

					if (!regressionIntercepts.isEmpty()) {
						regressionIntercepts += ",";
					}

					regressionIntercepts += key + "=" + value;
				}
			}

			if (!regressionIntercepts.isEmpty()) {
				replicatePads[targetPadNumber].setValue(labelToColumnName(getOutputLabelIntercepts()), regressionIntercepts);
			}
		}

		nonlinearityPoints.add(new NonlinearityPoint(null, replicatePads[targetPadNumber].getDate(), corrector, correctee, -1, -1, false));		
		replicatePads[targetPadNumber].setVolatileData(getVolatiles().getVolatileDataNonlinearityPointsKey(), nonlinearityPoints);

		replicatePads[targetPadNumber].setValue(labelToColumnName(getOutputLabel()), correctee);
	}

	public class NonlinearityPoint {
		private Standard standard;
		private long date;
		private double corrector;
		private double correctee;
		private int colorId;
		private int shapeId;
		private boolean disabled;

		public NonlinearityPoint(Standard standard, long date, double corrector, double correctee, int colorId, int shapeId, boolean disabled) {
			this.standard = standard;
			this.date = date;
			this.corrector = corrector;
			this.correctee = correctee;
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

		public double getCorrector() {
			return corrector;
		}

		public double getCorrectee() {
			return correctee;
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

	public class NonlinearityStandardVerifier extends SimpleStandardVerifier {
		public NonlinearityStandardVerifier(HashSet<Integer> acceptableStandardIds) {
			super(acceptableStandardIds);
		}

		@Override
		public boolean replicatePadIsAcceptable(ReplicatePad replicatePad) {
			Double corrector = getDouble(replicatePad, getInputLabelStandardCorrector());
			Double correctee = getDouble(replicatePad, getInputLabelStandardCorrectee());

			if (corrector != null && !Double.isNaN(corrector) && correctee != null && !Double.isNaN(correctee)) {
				return true;
			}

			return false;
		}
	}
}
