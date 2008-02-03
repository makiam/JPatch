package com.jpatch.afw.attributes;

public class IntMinimum extends IntLimit {
	
	public IntMinimum(IntAttr minimum) {
		super(minimum);
	}
	
	public IntMinimum(int minimum) {
		super(minimum);
	}
	
	@Override
	public int attributeWillChange(ScalarAttribute source, int value) {
		return Math.max(this.limit.getInt(), value);
	}
}
