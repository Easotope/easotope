package org.easotope.client.rawdata.replicate.widget.acquisition;

import java.util.HashMap;

import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.InputParameter;

public class Lidi2DisableCalculator {
	HashMap<InputParameter, Double[]> remappedMeasurements;

	public Lidi2DisableCalculator(HashMap<InputParameter, Double[]> remappedMeasurements) {
		this.remappedMeasurements = remappedMeasurements;
	}

	public void disableCycles(boolean[] disabledCycles) {
		int num44Seen = 0;
		double min44Seen = Double.POSITIVE_INFINITY;
		double max44Seen = Double.NEGATIVE_INFINITY;
		
		for (Double value : remappedMeasurements.get(InputParameter.V44_Sample)) {
			if (value != null) {
				min44Seen = Math.min(min44Seen, value);
				max44Seen = Math.max(max44Seen, value);
				num44Seen++;
			}
		}
		
		if (num44Seen < 2) {
			return;
		}
		
		double range = max44Seen - min44Seen;
		
		double rangeFactor = LoginInfoCache.getInstance().getPreferences().getLidi2RefRange() / 100.0;
		double min44Threshold = min44Seen - (range * rangeFactor);
		double max44Threshold = max44Seen + (range * rangeFactor);

		Double[] refs = remappedMeasurements.get(InputParameter.V44_Ref);

		for (int i=0; i<disabledCycles.length; i++) {
			if (refs[i] != null && (refs[i] < min44Threshold || refs[i] > max44Threshold)) {
				disabledCycles[i] = true;
			}
		}
	}
}
