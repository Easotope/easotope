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

package org.easotope.shared.analysis.repstep.superclass.etfpbl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.superclass.etfpbl.dependencies.Dependencies;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.core.DoubleTools;
import org.easotope.shared.core.exception.EasotopeStepException;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.math.LinearRegression;
import org.easotope.shared.math.Statistics;

public abstract class Calculator extends RepStepCalculator {
	public final static String PARAMETER_ACID_TEMPERATURE = "PARAMETER_ACID_TEMPERATURE";
	public final static String PARAMETER_AVERAGE_STANDARDS_FIRST = "PARAMETER_AVERAGE_STANDARDS_FIRST";

	public static final Double DEFAULT_ACID_TEMPERATURE = 0.0d;
	public static final int DEFAULT_WINDOW_TYPE = WindowType.CorrInterval.ordinal();
	public static final int DEFAULT_MIN_NUM_STANDARDS_BEFORE_AFTER = 10;
	public static final int[] DEFAULT_STANDARD_IDS = new int[0];
	public static final boolean DEFAULT_AVERAGE_STANDARDS_FIRST = false;
	
	public abstract VolatileKeys getVolatiles();
	public abstract String getInputLabel();
	public abstract String getInputStandardCorrectorLabel();
	public abstract String getInputStandardCorrecteeLabel();
	public abstract String getInputStandardAcidTempLabel();
	public abstract String getOutputEtfSlopeLabel();
	public abstract String getOutputEtfInterceptLabel();
	public abstract String getOutputLabel();
	public abstract String getStandardNotConfiguredLabel();
	public abstract int getStandardParameter();
	public abstract int getAcidTempParameter();

	public Calculator(RepStep repStep) {
		super(repStep);
	}

	public Double getAcidTemperature() {
		Double parameter = (Double) getParameter(PARAMETER_ACID_TEMPERATURE);
		return parameter == null ? DEFAULT_ACID_TEMPERATURE : parameter;
	}

	public WindowType getWindowType() {
		Integer parameter = (Integer) getParameter(PARAMETER_WINDOW_TYPE);
		return parameter == null ? WindowType.values()[DEFAULT_WINDOW_TYPE] : WindowType.values()[parameter];
	}

	public int getMinNumStandardsBeforeAFter() {
		Integer parameter = (Integer) getParameter(PARAMETER_MIN_NUM_STANDARDS_BEFORE_AFTER);
		return parameter == null ? DEFAULT_MIN_NUM_STANDARDS_BEFORE_AFTER : parameter;
	}

	public int[] getStandardIds() {
		Object parameter = (Object) getParameter(PARAMETER_STANDARD_IDS);
		return parameter == null ? new int[0] : (int[]) parameter;
	}

	public boolean getAverageStandardsFirst() {
		Boolean parameter = (Boolean) getParameter(PARAMETER_AVERAGE_STANDARDS_FIRST);
		return parameter == null ? DEFAULT_AVERAGE_STANDARDS_FIRST : parameter;
	}

	@Override
	public void calculate(ReplicatePad[] replicatePads, int padNumber, DependencyManager dependencyManager) throws EasotopeStepException {
		Dependencies dependencies = (Dependencies) dependencyManager;
		HashMap<Integer,Standard> standardIdToStandard = new HashMap<Integer,Standard>();

		Standard[] standards = dependencies.getStandards();
		for (int i=0; i<standards.length; i++) {
			standardIdToStandard.put(standards[i].getId(), standards[i]);
		}

		HashMap<Integer,AverageLine> standardIdToAverageLines = new HashMap<Integer,AverageLine>();
		HashSet<GraphPoint> standardGraphPoints = new HashSet<GraphPoint>();

		StandardReplicatePads standardReplicatePads = getStandardReplicatePads(replicatePads, padNumber, getWindowType(), getMinNumStandardsBeforeAFter(), new EtfStandardVerifier(standardIdToStandard.keySet(), getAcidTemperature(), this));

		for (ReplicatePad replicatePad : standardReplicatePads.getUsable()) {
			Double standarCorrector = getDouble(replicatePad, getInputStandardCorrectorLabel());
			Double standarCorrectee = getDouble(replicatePad, getInputStandardCorrecteeLabel());

			int sourceId = replicatePad.getSourceId();
			Standard standard = standardIdToStandard.get(sourceId);

			int replicateId = replicatePad.getReplicateId();
			long date = replicatePad.getDate();
			int colorId = standard.getColorId();
			int shapeId = standard.getShapeId();

			if (getAverageStandardsFirst()) {
				AverageLine tempAverageLine = standardIdToAverageLines.get(sourceId);

				if (tempAverageLine == null) {
					tempAverageLine = new AverageLine(colorId);
					standardIdToAverageLines.put(sourceId, tempAverageLine);
				}

				tempAverageLine.addNumber(standarCorrectee);
			}

			if (!Double.isNaN(standarCorrector) && !Double.isNaN(standarCorrectee)) {
				standardGraphPoints.add(new GraphPoint(replicateId, sourceId, date, standard.getName(), standarCorrector, standarCorrectee, colorId, shapeId, false));
			}
		}

		for (ReplicatePad replicatePad : standardReplicatePads.getDisabled()) {
			Double standardCorrector = getDouble(replicatePad, getInputStandardCorrectorLabel());
			Double standardCorrectee = getDouble(replicatePad, getInputStandardCorrecteeLabel());

			int replicateId = replicatePad.getReplicateId();
			int sourceId = replicatePad.getSourceId();
			Standard standard = standardIdToStandard.get(sourceId);
			long date = replicatePad.getDate();
			int colorId = standard.getColorId();
			int shapeId = standard.getShapeId();

			if (!Double.isNaN(standardCorrector) && !Double.isNaN(standardCorrectee)) {
				standardGraphPoints.add(new GraphPoint(replicateId, sourceId, date, standard.getName(), standardCorrector, standardCorrectee, colorId, shapeId, true));
			}
		}

		replicatePads[padNumber].setVolatileData(getVolatiles().getVolatileDataStandardGraphPointsKey(), standardGraphPoints);

		if (getAverageStandardsFirst()) {
			replicatePads[padNumber].setVolatileData(getVolatiles().getVolatileDataAverageLinesKey(), new HashSet<AverageLine>(standardIdToAverageLines.values()));
		}

		AcidTemp[] acidTemps = dependencies.getAcidTemps();
		HashMap<Integer,Double> standardIdToKnownValueNoAcidCorr = new HashMap<Integer,Double>();

		for (int i=0; i<standards.length; i++) {
			Standard standard = standards[i];
			AcidTemp acidTemp = acidTemps[i];

			try {
				double knownValue = standard.getValues().get(getStandardParameter()).getValue();
				double alpha = acidTemp == null ? 0.0d : acidTemp.getValues().get(getAcidTempParameter()).getValue();
				double knownValueNoAcidCorr = knownValue - alpha;

				standardIdToKnownValueNoAcidCorr.put(standard.getId(), knownValueNoAcidCorr);

			} catch (Exception e) {
				// ignore
			}
		}

		LinearRegression regression = new LinearRegression(!getAverageStandardsFirst());
		HashSet<GraphPoint> etfGraphPoints = new HashSet<GraphPoint>();

		if (getAverageStandardsFirst()) {
			for (int standardId : standardIdToAverageLines.keySet()) {
				Standard standard = standardIdToStandard.get(standardId);

				if (!standardIdToKnownValueNoAcidCorr.containsKey(standardId)) {
					String message = MessageFormat.format(getStandardNotConfiguredLabel(), standard.getName());
					throw new EasotopeStepException(message);
				}

				AverageLine line = standardIdToAverageLines.get(standardId);
				double x = line.getIntercept();
				double y = standardIdToKnownValueNoAcidCorr.get(standardId);
				regression.addCoordinate(x, y);

				etfGraphPoints.add(new GraphPoint(-1, -1, -1, standard.getName(), x, y, standard.getColorId(), standard.getShapeId(), false));
			}
		} else {
			for (GraphPoint graphPoint : standardGraphPoints) {
				int standardId = graphPoint.getStandardId();
				Standard standard = standardIdToStandard.get(standardId);

				if (!standardIdToKnownValueNoAcidCorr.containsKey(standardId)) {
					String message = MessageFormat.format(getStandardNotConfiguredLabel(), standard.getName());
					throw new EasotopeStepException(message);
				}

				double x = graphPoint.getY();
				double y = standardIdToKnownValueNoAcidCorr.get(standardId);

				int replicateId = graphPoint.getReplicateId();
				long date = graphPoint.getDate();
				boolean disabled = graphPoint.getDisabled();

				if (!disabled) {
					regression.addCoordinate(x, y);
				}

				etfGraphPoints.add(new GraphPoint(replicateId, -1, date, standard.getName(), x, y, standard.getColorId(), standard.getShapeId(), disabled));					
			}
		}

		replicatePads[padNumber].setVolatileData(getVolatiles().getVolatileDataEtfGraphPointsKey(), etfGraphPoints);

		if (!regression.isInvalid()) {
			replicatePads[padNumber].setVolatileData(getVolatiles().getVolatileDataEtfRegressionKey(), regression);
			replicatePads[padNumber].setValue(labelToColumnName(getOutputEtfSlopeLabel()), regression.getSlope());
			replicatePads[padNumber].setValue(labelToColumnName(getOutputEtfInterceptLabel()), regression.getIntercept());
		} else {
			replicatePads[padNumber].setValue(labelToColumnName(getOutputEtfSlopeLabel()), Double.NaN);
			replicatePads[padNumber].setValue(labelToColumnName(getOutputEtfInterceptLabel()), Double.NaN);
		}

		Double value = getDouble(replicatePads[padNumber], getInputLabel());

		if (value != null && !regression.isInvalid()) {
			double y = regression.getSlope() * value + regression.getIntercept();
			int replicateId = replicatePads[padNumber].getReplicateId();
			long replicateDate = replicatePads[padNumber].getDate();
			boolean isDisabled = (Boolean) replicatePads[padNumber].getValue(Pad.DISABLED);
			etfGraphPoints.add(new GraphPoint(replicateId, -1, replicateDate, null, value, y, -1, -1, isDisabled));
			value = y;
		}

	    replicatePads[padNumber].setValue(labelToColumnName(getOutputLabel()), value);
	}

	public class GraphPoint {
		private int replicateId;
		private int standardId;
		private long date;
		private String name;
		private double x;
		private double y;
		private int colorId;
		private int shapeId;
		private boolean isDisabled;

		public GraphPoint(int replicateId, int standardId, long date, String name, double x, double y, int colorId, int shapeId, boolean isDisabled) {
			this.replicateId = replicateId;
			this.standardId = standardId;
			this.date = date;
			this.name = name;
			this.x = x;
			this.y = y;
			this.colorId = colorId;
			this.shapeId = shapeId;
			this.isDisabled = isDisabled;
		}

		public int getReplicateId() {
			return replicateId;
		}

		public int getStandardId() {
			return standardId;
		}

		public long getDate() {
			return date;
		}

		public String getName() {
			return name;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public int getColorId() {
			return colorId;
		}

		public int getShapeId() {
			return shapeId;
		}

		public boolean getDisabled() {
			return isDisabled;
		}
	}

	public class AverageLine {
		private Statistics statistics = new Statistics();
		private int colorId;

		AverageLine(int colorId) {
			this.colorId = colorId;
		}

		public void addNumber(double y) {
			double intercept = y;
			statistics.addNumber(intercept);
		}
		
		public double getIntercept() {
			return statistics.getMean();			
		}

		public int getColorId() {
			return colorId;
		}
	}

	public class EtfStandardVerifier extends SimpleStandardVerifier {
		private double targetReplicateAcidTemperature;
		private Calculator calculator;

		public EtfStandardVerifier(Set<Integer> acceptableStandardIds, Double targetReplicateAcidTemperature, Calculator calculator) {
			super(acceptableStandardIds);
			this.targetReplicateAcidTemperature = targetReplicateAcidTemperature == null ? 0.0d : targetReplicateAcidTemperature;
			this.calculator = calculator;
		}

		@Override
		public boolean replicatePadIsAcceptable(ReplicatePad replicatePad) {
			Double standardCorrector = getDouble(replicatePad, calculator.getInputStandardCorrectorLabel());
			Double standardCorrectee = getDouble(replicatePad, calculator.getInputStandardCorrecteeLabel());

			if (standardCorrector == null || standardCorrectee == null) {
				return false;
			}

			Double standardAcidTemp = getDouble(replicatePad, calculator.getInputStandardAcidTempLabel());

			if (standardAcidTemp == null) {
				return true;
			}

			return DoubleTools.essentiallyEqual(standardAcidTemp, targetReplicateAcidTemperature);
		}
	}
}
