package com.jpatch.afw.attributes;

public abstract class IntLimit extends AttributePreChangeAdapter {
	final IntAttr limit;
	
	public IntLimit(IntAttr limit) {
		this.limit = limit;
	}
	
	public IntLimit(int limit) {
		this.limit = new IntAttr(limit);
	}
	
	public IntAttr getAttr() {
		return limit;
	}
	
	public int getValue() {
		return limit.getInt();
	}
}
