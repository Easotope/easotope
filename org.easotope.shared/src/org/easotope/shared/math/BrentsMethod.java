/*
 * This code was taken directly from an older version of the page:
 *
 * https://en.wikipedia.org/wiki/Brent%27s_method
 *
 * It needs to be migrated to use the Apache math library once version 4 is released.
 * Version 3 seems to have problems since the class is deprecated.
 */

package org.easotope.shared.math;

public class BrentsMethod {
	public static final double TOLERANCE = 0.000000000001;

	public static double solve(Function function, double lowerLimit, double upperLimit, double errorTolerance) {
		double a = lowerLimit;
		double b = upperLimit;
		double c = 0;
		double d = Double.MAX_VALUE;

		double fa = function.solve(a);
		double fb = function.solve(b);

		double fc = 0;
		double s = 0;
		double fs = 0;

		// if f(a) f(b) >= 0 then error-exit
		if (fa * fb >= 0) {
			throw new RuntimeException("Limits a and b do not surround a root.");
		}

		// if |f(a)| < |f(b)| then swap (a,b) end if
		if (Math.abs(fa) < Math.abs(fb)) {
			double tmp = a;
			a = b;
			b = tmp;
			
			tmp = fa;
			fa = fb;
			fb = tmp;
		}

		c = a;
		fc = fa;
		boolean mflag = true;
		int i = 0;

		while (!(fb==0) && (Math.abs(a-b) > errorTolerance)) {
			if ((fa != fc) && (fb != fc)) {
				// Inverse quadratic interpolation
				s = a * fb * fc / (fa - fb) / (fa - fc) + b * fa * fc / (fb - fa) / (fb - fc) + c * fa * fb / (fc - fa) / (fc - fb);
			} else {
				// Secant Rule
				s = b - fb * (b - a) / (fb - fa);
			}

			double tmp2 = (3 * a + b) / 4;
			if ((!(((s > tmp2) && (s < b)) || ((s < tmp2) && (s > b)))) || (mflag && (Math.abs(s - b) >= (Math.abs(b - c) / 2))) || (!mflag && (Math.abs(s - b) >= (Math.abs(c - d) / 2)))) {
				s = (a + b) / 2;
				mflag = true;
			} else {
				if ((mflag && (Math.abs(b - c) < errorTolerance)) || (!mflag && (Math.abs(c - d) < errorTolerance))) {
					s = (a + b) / 2;
					mflag = true;
				} else {
					mflag = false;
				}
			}
			fs = function.solve(s);
			d = c;
			c = b;
			fc = fb;
			if (fa * fs < 0) {
				b = s;
				fb = fs;
			} else {
				a = s;
				fa = fs;
			}

			// if |f(a)| < |f(b)| then swap (a,b) end if
			if (Math.abs(fa) < Math.abs(fb)) {
				double tmp = a;
				a = b;
				b = tmp;
				
				tmp = fa;
				fa = fb;
				fb = tmp;
			}
			i++;
			if (i > 1000) {
				throw new RuntimeException("Too many iterations.");
			}
		}
		return b;
	}

	public interface Function {
		public double solve(double parameter);
	}
}
