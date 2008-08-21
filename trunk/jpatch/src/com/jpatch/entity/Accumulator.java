package com.jpatch.entity;

public interface Accumulator extends Comparable<Accumulator>{
	void reset();
	boolean isZero();
	int accumulate(double[] values, int index);
	int readout(double[] values, int index);
	int getDimensions();
}
