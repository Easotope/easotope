package org.easotope.shared.analysis.repstep.co2.bulkrefcalc;

public class MzLabels {
	private String mz;
	private String inputLabel;
	private String outputLabel;

	public MzLabels(String mz, String inputLabel, String outputLabel) {
		this.mz = mz;
		this.inputLabel = inputLabel;
		this.outputLabel = outputLabel;
	}

	public String getMz() {
		return mz;
	}

	public String getInputLabel() {
		return inputLabel;
	}

	public String getOutputLabel() {
		return outputLabel;
	}
}
