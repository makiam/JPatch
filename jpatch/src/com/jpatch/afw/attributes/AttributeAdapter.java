package com.jpatch.afw.attributes;

public class AttributeAdapter<T> implements AttributeListener<T> {

	public void attributeHasChanged(Attribute source) { }

	public boolean attributeWillChange(Attribute source, boolean value) {
		return value;
	}

	public int attributeWillChange(Attribute source, int value) {
		return value;
	}
	
	public double attributeWillChange(Attribute source, double value) {
		return value;
	}
	
	public T attributeWillChange(Attribute source, T value) {
		return value;
	}
}
