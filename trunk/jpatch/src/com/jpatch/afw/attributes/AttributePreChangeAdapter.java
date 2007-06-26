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

	public boolean attributeWillChange(ScalarAttribute source, boolean value) {
		throw new UnsupportedOperationException();
	}

	public int attributeWillChange(ScalarAttribute source, int value) {
		throw new UnsupportedOperationException();
	}

	public double attributeWillChange(ScalarAttribute source, double value) {
		throw new UnsupportedOperationException();
	}

	public T attributeWillChange(ScalarAttribute source, T value) {
		throw new UnsupportedOperationException();
	}

}
