package com.jpatch.entity;

public interface Accumulator extends Comparable<Accumulator> {
	void reset();
	boolean isZero();
	void accumulate(Accumulator acc);
	Accumulator getValue();
}
