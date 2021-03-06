package com.jpatch.afw.attributes;

public abstract class DoubleLimit extends AttributePreChangeAdapter {
	final DoubleAttr limit;
	
	public DoubleLimit(DoubleAttr limit) {
		this.limit = limit;
	}
	
	public DoubleLimit(double limit) {
		this.limit = new DoubleAttr(limit);
	}
	
	public DoubleAttr getAttr() {
		return limit;
	}
	
	public double getValue() {
		return limit.getDouble();
	}
}
