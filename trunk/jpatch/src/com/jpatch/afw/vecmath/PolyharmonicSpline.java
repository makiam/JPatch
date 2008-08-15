package com.jpatch.afw.vecmath;

/**
 * PolyharmonicSpline class.
 * See <a href="http://en.wikipedia.org/wiki/Polyharmonic_spline">http://en.wikipedia.org/wiki/Polyharmonic_spline</a>.
 * 
 * @author Sascha Ledinsky
 * Released under the GNU General Public License Version 2 (GPLv2)
 */

public class PolyharmonicSpline {
	/** locations of the centers */
	private final double[][] c;
	/** weights */
	private final double[] x;
	/** value dimensions */
	private int dd;
	/** k parameter */
	private int k;
	
	/**
	 * Creates a new PolyharmonicSpline object
	 * @param centers the spline centers
	 * @param values the values at the spline centers
	 * @param k the k parameter (must be > 1)
	 */
	public PolyharmonicSpline(double[][] centers, double[][] values, int k) {
		c = centers;
		dd = values[0].length;
		double[] y = new double[values.length * dd];
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < dd; j++) {
				y[i * dd + j] = values[i][j];
			}
		}
		this.k = k;
		
		int dim = c.length + 1 + c[0].length;
		
		/* fill matrix */
		double[] matrix = new double[dim * dim];
		for (int i = 0; i < c.length; i++) {		// row
			for (int j = 0; j < c.length; j++) {	// column
				matrix[i * dim + j] = phi(c[i], c[j]);
			}
			matrix[c.length + i * dim] = 1;
			matrix[c.length * dim + i] = 1;
			for (int j = 0; j < c[0].length; j++) {
				matrix[i * dim + c.length + 1 + j] = c[i][j];
				matrix[(c.length + 1 + j) * dim + i] = c[i][j];
			}
		}
		
		double[] b = new double[dim * dd];
		x = new double[dim * dd];
		System.arraycopy(y, 0, b, 0, y.length);
		
		/* solve the system */
		MatrixUtils.solve(matrix, b, x);
	}
	
	/**
	 * Evaluate the spline at pos.
	 * @param pos the position vector
	 * @param y array to store the result vector
	 */
	public void evaluate(double[] pos, double[] y) {
		for (int i = 0; i < y.length; i++) {
			y[i] = x[c.length * dd + i];
		}
		for (int i = 0; i < c.length; i++) {
			double phi = phi(pos, c[i]);
			int row = i * dd;
			for (int j = 0; j < y.length; j++) {
				y[j] += x[row + j] * phi;
			}
			
		}
		for (int i = 0; i < c[0].length; i++) {
			int row = (c.length + 1 + i) * dd;
			for (int j = 0; j < y.length; j++) {
				y[j] += x[row + j] * pos[i];
			}
		}
	}
	
	/**
	 * The radial basis function of the spline.
	 * phi(r) = r^k for k = 1,3,5,...
	 * phi(r) = r^k*ln(r) with k = 2,4,6...
	 * where r = ||a - b||
	 * 
	 * @param a point a (usually a point to evaluate)
	 * @param b point b (usually a center of the spline)
	 * @return r^k for k = 1,3,5,..., r^k*ln(r) with k = 2,4,6... where r = ||a - b||
	 */
	private double phi(double[] a, double[] b) {
		double r = distance(a, b);
		switch (k) {
		case 1:
			return r;
		case 2:
			if (r < 1) {
				return r * Math.log(Math.pow(r, r));	// since ln(0) = -infinity
			} else {
				return r * r * Math.log(r);
			}
		case 3:
			return r * r * r;
		default:
			if (k % 2 == 0) {
				if (r < 1) {
					return Math.pow(r, k - 1) * Math.log(Math.pow(r, r));	// since ln(0) = -infinity
				} else {
					return Math.pow(r, k) * Math.log(r);
				}
			} else {
				return Math.pow(r, k);
			}
		}
	}
	
	/**
	 * Compute the distance between points a and b
	 * @param a array representing point a
	 * @param b array representing point b
	 * @return the distance between a and b
	 * @throws IllegalArgumentException if a.length ≠ b.length
	 * @throws NullPointerException if one of the parameters is null
	 */
	private double distance(double[] a, double[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("a and b must have same length (" + a.length + " ≠ " + b.length + ")");
		}
		double dsq = 0;
		for (int i = 0; i < a.length; i++) {
			double d = a[i] - b[i];
			dsq += d * d;
		}
		return Math.sqrt(dsq);
	}

}
