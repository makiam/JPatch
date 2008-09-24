package com.jpatch.entity;

public interface Accumulator {
	void reset();
	boolean isZero();
	void accumulate(Accumulator acc);
	void set(Accumulator acc);
	Accumulator createValue();
}
