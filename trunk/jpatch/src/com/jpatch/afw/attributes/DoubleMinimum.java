package com.jpatch.afw.attributes;

public class DoubleMinimum extends DoubleLimit {
	
	public DoubleMinimum(DoubleAttr minimum) {
		super(minimum);
	}
	
	@Override
	public double attributeWillChange(Attribute source, double value) {
		return Math.max(this.limit.getDouble(), value);
	}
}
