package com.jpatch.afw.attributes;

/**
 * An abstract implementation of AttributePreChangeListener.
 * To avoid programming errors, all methods throw an UnsupportedOperationException().
 * 
 * @author sascha
 *
 * @param <T> the object class if the listener is intended to be used for GenericAttr attribtues.
 */
public abstract class AttributePreChangeAdapter<T> implements AttributePreChangeListener<T> {

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
