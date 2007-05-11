package com.jpatch.afw.attributes;

public class AttributePreChangeAdapter<T> implements AttributePreChangeListener<T> {

	public boolean attributeWillChange(Attribute source, boolean value) {
		throw new UnsupportedOperationException();
	}

	public int attributeWillChange(Attribute source, int value) {
		throw new UnsupportedOperationException();
	}

	public double attributeWillChange(Attribute source, double value) {
		throw new UnsupportedOperationException();
	}

	public T attributeWillChange(Attribute source, T value) {
		throw new UnsupportedOperationException();
	}

}
