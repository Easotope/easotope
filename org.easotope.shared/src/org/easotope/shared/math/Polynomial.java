package org.easotope.shared.math;

public class Polynomial {
	private double[] coefficients;
	
	public Polynomial(double[] coefficients) {
		this.coefficients = coefficients;
	}

	public double evaluate(double x) {
		double sum = 0.0;
		double var = 1.0;

		for (int i=0; i<coefficients.length; i++) {
			sum += var * coefficients[i];
			var *= x;
		}
		
		return sum;
	}
	
	public double[] getCoefficients() {
		return coefficients;
	}
}
