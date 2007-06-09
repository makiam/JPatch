package com.jpatch.afw.attributes;

public class DoubleAttr extends AbstractAttribute {
	protected double value;

	public DoubleAttr() {
		this(0.0);
	}
	
	public DoubleAttr(double value) {
		this.value = value;
	}
	
	public double getDouble() {
		return value;
	}

	public double setDouble(double value) {
		System.out.println("old=" + this.value + " new=" + value);
		if (value != this.value) {
			this.value = fireAttributeWillChange(value);
			fireAttributeHasChanged();
		}
		return this.value;
	}

	@Override
	public String toString() {
		return Double.toString(value);
	}
}
