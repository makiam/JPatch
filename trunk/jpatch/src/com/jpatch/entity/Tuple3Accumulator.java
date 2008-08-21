package com.jpatch.entity;

import javax.vecmath.*;

public class Tuple3Accumulator extends AbstractAccumulator {
	private double x, y, z;
	
	public int accumulate(double[] values, int index) {
		x += values[index];
		y += values[index + 1];
		z += values[index + 2];
		return 3;
	}

	public int readout(double[] values, int index) {
		values[index] += x;
		values[index + 1] += y;
		values[index + 2] += z;
		return 3;
	}
	
	public void reset() {
		x = y = z = 0;
	}

	public boolean isZero() {
		return x == 0 && y == 0 && z == 0;
	}
	
	public int getDimensions() {
		return 3;
	}
	
	public void setValue(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setValue(Tuple3d tuple) {
		setValue(tuple.x, tuple.y, tuple.z);
	}
	
	public Tuple3d getValue(Tuple3d tuple) {
		tuple.set(x, y, z);
		return tuple;
	}
}
