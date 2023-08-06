package org.easotope.shared.rawdata.parser.nu.sample;

import java.util.ArrayList;
import java.util.Collections;

import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.parser.MapBuilder;

public class CycleMeasurements {
	private boolean isLidi2;
	private ArrayList<CycleMeasurement> list = new ArrayList<CycleMeasurement>();

	CycleMeasurements(boolean isLidi2) {
		this.isLidi2 = isLidi2;
	}

	public void addCycle(InputParameter parameter, int cycle, double measurement, Long timestamp) {
		list.add(new CycleMeasurement(parameter, cycle, measurement, timestamp));
	}

	public MapBuilder getAsMapBuilder() {
		MapBuilder mapBuilder = new MapBuilder();
		
		if (isLidi2) {
			Collections.sort(list);

			int newCycleNumber = 0;
			long currentTime = list.get(0).getTimestamp();

			for (CycleMeasurement data : list) {
				if (currentTime != data.getTimestamp()) {
					newCycleNumber++;
					currentTime = data.getTimestamp();
				}

				data.setCycleNumber(newCycleNumber);
			}
		}

		for (CycleMeasurement data : list) {
			mapBuilder.put(data.getParameter(), data.getCycleNumber(), data.getMeasurement());
		}
		
		if (isLidi2) {
			mapBuilder.equalizeMeasurementVectors();
		}

		return mapBuilder;
	}

	class CycleMeasurement implements Comparable<CycleMeasurement> {
		private InputParameter parameter;
		private int cycleNumber;
		private double measurement;
		private Long timestamp;

		public CycleMeasurement(InputParameter parameter, int cycleNumber, double measurement, Long timestamp) {
			this.parameter = parameter;
			this.cycleNumber = cycleNumber;
			this.measurement = measurement;
			this.timestamp = timestamp;
		}

		public InputParameter getParameter() {
			return parameter;
		}

		public int getCycleNumber() {
			return cycleNumber;
		}
		
		public void setCycleNumber(int cycleNumber) {
			this.cycleNumber = cycleNumber;
		}

		public double getMeasurement() {
			return measurement;
		}

		public Long getTimestamp() {
			return timestamp;
		}

		@Override
		public int compareTo(CycleMeasurement that) {
			return this.timestamp.compareTo(that.timestamp);
		}
	}
}
