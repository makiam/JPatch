package com.jpatch.entity;

public class ScalarAccumulator extends AbstractAccumulator {
	private double value;
	
	public boolean isZero() {
		return value == 0;
	}

	public void accumulate(Accumulator acc) {
		value += ((ScalarAccumulator) acc).value;
	}
	
	public void reset() {
		value = 0;
	}
	
	public ScalarAccumulator getValue() {
		ScalarAccumulator a = new ScalarAccumulator();
		a.value = value;
		return a;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public double getDouble() {
		return value;
	}
}
