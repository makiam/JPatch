package com.jpatch.entity;

public class ScalarAccumulator extends AbstractAccumulator {
	private double value;
	
	public int accumulate(double[] values, int index) {
		value += values[index];
		return 1;
	}

	public int readout(double[] values, int index) {
		values[index] += value;
		return 1;
	}
	
	public int getDimensions() {
		return 1;
	}
	
	public void reset() {
		value = 0;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}

	public boolean isZero() {
		return value == 0;
	}
}
