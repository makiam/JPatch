package com.jpatch.afw.attributes;

/**
 * A listener to listen for attribute pre-change events.
 * To keep the API simple, methods for all possible (primitive and Object)
 * attributes have been merged into a single listener interface.
 * For conveniance, implementors can subclass AttributePreChangeAdapter.
 * @author sascha
 * @see AttributePreChangeAdapter
 * @param <T> the object class if the listener is intended to be used for GenericAttr attribtues.
 */
public interface AttributePreChangeListener<T> {
	/**
	 * This method gets called from Attributes whose value is about to change.
	 * The listener can "veto" by returing a different value that the passed one.
	 * @param source the Attribute whose value is about to change
	 * @param value the value the attribute intends to change to
	 * @return the value the attribute should change to
	 */
	public boolean attributeWillChange(Attribute source, boolean value);
	
	/**
	 * This method gets called from Attributes whose value is about to change.
	 * The listener can "veto" by returing a different value that the passed one.
	 * @param source the Attribute whose value is about to change
	 * @param value the value the attribute intends to change to
	 * @return the value the attribute should change to
	 */
	public int attributeWillChange(Attribute source, int value);
	
	/**
	 * This method gets called from Attributes whose value is about to change.
	 * The listener can "veto" by returing a different value that the passed one.
	 * @param source the Attribute whose value is about to change
	 * @param value the value the attribute intends to change to
	 * @return the value the attribute should change to
	 */
	public double attributeWillChange(Attribute source, double value);
	
	/**
	 * This method gets called from Attributes whose value is about to change.
	 * The listener can "veto" by returing a different value that the passed one.
	 * @param source the Attribute whose value is about to change
	 * @param value the value the attribute intends to change to
	 * @return the value the attribute should change to
	 */
	public T attributeWillChange(Attribute source, T value);
}
