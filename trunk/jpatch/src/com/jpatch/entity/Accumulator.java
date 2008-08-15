package com.jpatch.entity;

public class Accumulator implements Comparable<Accumulator>{
	private double value;
	
	void reset() {
		value = 0;
	}
	
	void add(double vector) {
		this.value += vector;
	}

	public final int compareTo(Accumulator other) {
		final int hThis = System.identityHashCode(this);
		final int hOther = System.identityHashCode(other);
		return hThis < hOther ? -1 : hThis > hOther ? 1 : 0;
	}
	
	public double getValue() {
		return value;
	}
}
