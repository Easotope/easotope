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

package org.easotope.shared.analysis.repstep.superclass.etf;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.execute.RepStepCalculator;
import org.easotope.shared.analysis.execute.dependency.DependencyManager;
import org.easotope.shared.analysis.repstep.superclass.etf.dependencies.Dependencies;
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
	public abstract String getInputStandardPreCorrecteeLabel();
	public abstract String getInputStandardPostCorrecteeLabel();
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
		ArrayList<GraphPoint> standardGraphPoints = new ArrayList<GraphPoint>();
		ArrayList<ConnectingLine> connectingLines = new ArrayList<ConnectingLine>();
		HashSet<GraphPoint> standardPostNonlinearityGraphPoints = new HashSet<GraphPoint>();

		StandardReplicatePads standardReplicatePads = getStandardReplicatePads(replicatePads, padNumber, getWindowType(), getMinNumStandardsBeforeAFter(), new EtfStandardVerifier(standardIdToStandard.keySet(), getAcidTemperature()));

		for (ReplicatePad replicatePad : standardReplicatePads.getUsable()) {
			Double standardCorrector = getDouble(replicatePad, getInputStandardCorrectorLabel());
			Double standardPreCorrectee = getDouble(replicatePad, getInputStandardPreCorrecteeLabel());
			Double standardPostCorrectee = getDouble(replicatePad, getInputStandardPostCorrecteeLabel());

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

				tempAverageLine.addNumber(standardPostCorrectee);
			}

			standardGraphPoints.add(new GraphPoint(replicateId, sourceId, date, standard.getName(), standardCorrector, standardPreCorrectee, colorId, shapeId, false));

			GraphPoint graphPoint = new GraphPoint(replicateId, sourceId, date, standard.getName(), 0.0d, standardPostCorrectee, colorId, shapeId, false);
			standardGraphPoints.add(graphPoint);
			standardPostNonlinearityGraphPoints.add(graphPoint);

			connectingLines.add(new ConnectingLine(standardCorrector, standardPreCorrectee, 0.0d, standardPostCorrectee, colorId, false));
		}

		for (ReplicatePad replicatePad : standardReplicatePads.getDisabled()) {
			Double standardCorrector = getDouble(replicatePad, getInputStandardCorrectorLabel());
			Double standardPreCorrectee = getDouble(replicatePad, getInputStandardPreCorrecteeLabel());
			Double standardPostCorrectee = getDouble(replicatePad, getInputStandardPostCorrecteeLabel());

			int replicateId = replicatePad.getReplicateId();
			int sourceId = replicatePad.getSourceId();
			Standard standard = standardIdToStandard.get(sourceId);
			long date = replicatePad.getDate();
			int colorId = standard.getColorId();
			int shapeId = standard.getShapeId();

			standardGraphPoints.add(new GraphPoint(replicateId, sourceId, date, standard.getName(), standardCorrector, standardPreCorrectee, colorId, shapeId, true));
			standardGraphPoints.add(new GraphPoint(replicateId, sourceId, date, standard.getName(), 0.0d, standardPostCorrectee, colorId, shapeId, true));
			connectingLines.add(new ConnectingLine(standardCorrector, standardPreCorrectee, 0.0d, standardPostCorrectee, colorId, true));
		}

		replicatePads[padNumber].setVolatileData(getVolatiles().getVolatileDataStandardGraphPointsKey(), standardGraphPoints);
		replicatePads[padNumber].setVolatileData(getVolatiles().getVolatileDataConnectingLines(), connectingLines);

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
			for (GraphPoint graphPoint : standardPostNonlinearityGraphPoints) {
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

				etfGraphPoints.add(new GraphPoint(replicateId, standardId, date, standard.getName(), x, y, standard.getColorId(), standard.getShapeId(), disabled));					
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
		private boolean disabled;

		public GraphPoint(int replicateId, int standardId, long date, String name, double x, double y, int colorId, int shapeId, boolean disabled) {
			this.replicateId = replicateId;
			this.standardId = standardId;
			this.date = date;
			this.name = name;
			this.x = x;
			this.y = y;
			this.colorId = colorId;
			this.shapeId = shapeId;
			this.disabled = disabled;
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
			return disabled;
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
	
	public class ConnectingLine {
		private double x1;
		private double y1;
		private double x2;
		private double y2;
		private int colorId;
		private boolean disabled;

		ConnectingLine(double x1, double y1, double x2, double y2, int colorId, boolean disabled) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
			this.colorId = colorId;
			this.disabled = disabled;
		}

		public double getX1() {
			return x1;
		}

		public double getY1() {
			return y1;
		}

		public double getX2() {
			return x2;
		}

		public double getY2() {
			return y2;
		}

		public int getColorId() {
			return colorId;
		}

		public boolean getDisabled() {
			return disabled;
		}
	}

	public class EtfStandardVerifier extends SimpleStandardVerifier {
		private double targetReplicateAcidTemperature;

		public EtfStandardVerifier(Set<Integer> acceptableStandardIds, Double targetReplicateAcidTemperature) {
			super(acceptableStandardIds);
			this.targetReplicateAcidTemperature = targetReplicateAcidTemperature == null ? 0.0d : targetReplicateAcidTemperature;
		}

		@Override
		public boolean replicatePadIsAcceptable(ReplicatePad replicatePad) {
			Double standardδ = getDouble(replicatePad, getInputStandardCorrectorLabel());
			Double standardΔ = getDouble(replicatePad, getInputStandardPreCorrecteeLabel());

			if (standardδ == null || standardΔ == null) {
				return false;
			}

			Double standardAcidTemp = getDouble(replicatePad, getInputStandardAcidTempLabel());

			if (standardAcidTemp == null) {
				return true;
			}

			return DoubleTools.essentiallyEqual(standardAcidTemp, targetReplicateAcidTemperature);
		}
	}
}
