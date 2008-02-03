package com.jpatch.afw.attributes;

public class IntMaximum extends IntLimit {
	
	public IntMaximum(IntAttr maximum) {
		super(maximum);
	}
	
	public IntMaximum(int maximum) {
		super(maximum);
	}
	
	@Override
	public int attributeWillChange(ScalarAttribute source, int value) {
		return Math.min(this.limit.getInt(), value);
	}
}
