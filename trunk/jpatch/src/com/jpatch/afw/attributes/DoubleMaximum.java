package com.jpatch.afw.attributes;

public class DoubleMaximum extends DoubleLimit {
	
	public DoubleMaximum(DoubleAttr maximum) {
		super(maximum);
	}
	
	@Override
	public double attributeWillChange(Attribute source, double value) {
		return Math.min(this.limit.getDouble(), value);
	}
}
