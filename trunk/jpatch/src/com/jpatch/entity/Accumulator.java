package com.jpatch.entity;

public interface Accumulator {
	void reset();
//	boolean isZero();
	void accumulateActive(double[] value);
	void set(double[] value);
//	Accumulator createValue();
}
