package com.jpatch.afw.attributes;

public interface AttributeListener<T> {
	public boolean attributeWillChange(Attribute source, boolean value);
	public int attributeWillChange(Attribute source, int value);
	public double attributeWillChange(Attribute source, double value);
	public T attributeWillChange(Attribute source, T value);
	public void attributeHasChanged(Attribute source);
}
