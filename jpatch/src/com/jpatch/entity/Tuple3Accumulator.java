package com.jpatch.entity;

import javax.vecmath.*;

public class Tuple3Accumulator extends AbstractAccumulator {
	private double x, y, z;
	
	public void accumulate(Accumulator acc) {
		Tuple3Accumulator t3a = (Tuple3Accumulator) acc;
		x += t3a.x;
		y += t3a.y;
		z += t3a.z;
	}
	
	public void reset() {
		x = y = z = 0;
	}

	public boolean isZero() {
		return x == 0 && y == 0 && z == 0;
	}
	
	public Tuple3Accumulator getValue() {
		Tuple3Accumulator a = new Tuple3Accumulator();
		a.x = x;
		a.y = y;
		a.z = z;
		return a;
	}
	
	public void setTuple(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setTuple(Tuple3d tuple) {
		setTuple(tuple.x, tuple.y, tuple.z);
	}
	
	public Tuple3d getTuple(Tuple3d tuple) {
		tuple.set(x, y, z);
		return tuple;
	}
}
