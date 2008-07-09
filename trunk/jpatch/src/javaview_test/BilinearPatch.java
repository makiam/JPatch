package javaview_test;

import java.util.*;

public class BilinearPatch {
	final double s0, t0, s1, t1;
	private final double[] v00;
	private final double[] v01;
	private final double[] v10;
	private final double[] v11;
	private final double ds, dt, dsdt;
	private final int dim;
	
	public BilinearPatch(double s0, double t0, double s1, double t1, double[] v00, double[] v01, double[] v10, double[] v11) {
		this.s0 = s0;
		this.t0 = t0;
		this.s1 = s1;
		this.t1 = t1;
		ds = 1.0 / (s1 - s0);
		dt = 1.0 / (t1 - t0);
		dsdt = ds * dt;
		this.v00 = v00.clone();
		this.v01 = v01.clone();
		this.v10 = v10.clone();
		this.v11 = v11.clone();
		dim = v00.length;
	}
	
	public boolean contains(double s, double t) {
		return (s0 <= s && s <= s1 && t0 <= t && t <= t1);
	}
	
	public List<BilinearPatch> split(double s, double t, double[] vector) {
		assert contains(s, t);
		List<BilinearPatch> patches = new ArrayList<BilinearPatch>(4);
		double ns = (s - s0) * ds;
		double nt = (t - t0) * dt;
		double[] v0t = interpolate(v00, v01, nt, new double[dim]);
		double[] v1t = interpolate(v10, v11, nt, new double[dim]);
		double[] vs0 = interpolate(v00, v10, ns, new double[dim]);
		double[] vs1 = interpolate(v01, v11, ns, new double[dim]);
//		double[] vst = interpolate(v0t, v1t, ns, new double[dim]);
		patches.add(new BilinearPatch(s0, t0, s, t, v00, v0t, vs0, vector));
		patches.add(new BilinearPatch(s0, t, s, t1, v0t, v01, vector, vs1));
		patches.add(new BilinearPatch(s, t0, s1, t, vs0, vector, v10, v1t));
		patches.add(new BilinearPatch(s, t, s1, t1, vector, vs1, v1t, v11));
		return patches;
	}
	/*
	 * st
	 * 00
	 * 01
	 * 10
	 * 11
	 */
	public double[] interpolate(double s, double t, double[] vector) {
		double w00 = (s1 - s) * (t1 - t) * dsdt;
		double w01 = (s1 - s) * (t - t0) * dsdt;
		double w10 = (s - s0) * (t1 - t) * dsdt;
		double w11 = (s - s0) * (t - t0) * dsdt;
		for (int i = 0; i < dim; i++) {
			vector[i] = v00[i] * w00 + v01[i] * w01+ v10[i] * w10 + v11[i] * w11;
		}
		return vector;
	}
	
	private static double[] interpolate(double[] a, double[] b, double s, double[] result) {
		double s1 = 1 - s;
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i] * s1 + b[i] * s;
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "patch (" + s0 + "," + t0 + " - " + s1 + "," + t1 + ")";
	}
}
