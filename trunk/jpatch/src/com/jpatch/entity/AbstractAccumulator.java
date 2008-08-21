package com.jpatch.entity;

public abstract class AbstractAccumulator implements Accumulator {
	public final int compareTo(Accumulator o) {
		final int hThis = System.identityHashCode(this);
		final int hOther = System.identityHashCode(o);
		return hThis < hOther ? -1 : hThis > hOther ? 1 : 0;
	}
}
