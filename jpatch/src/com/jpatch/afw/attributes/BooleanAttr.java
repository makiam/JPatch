package com.jpatch.afw.attributes;

/**
 * An attribute that wraps a value of boolean type.
 * @author sascha
 *
 */
public class BooleanAttr extends AbstractScalarAttribute {
	/**
	 * The boolean value this attribute represents
	 */
	boolean value;

	/**
	 * Constructs a newly allocated BooleanAttr with a value of false.
	 */
	public BooleanAttr() {
		this(false);
	}
	
	/**
	 * Constructs a newly allocated BooleanAttr with the specified value
	 * @param value the value of this BooleanAttr
	 */
	public BooleanAttr(boolean value) {
		this.value = value;
	}
	
	/**
	 * Returns the value of this BooleanAttr as boolean
	 * @return the value of this BooleanAttr as boolean
	 */
	public boolean getBoolean() {
		return value;
	}

	/**
	 * Sets the value of this BooleanAttr to the specified value.
	 * The attributeWillChange method of all registered AttributePreChangeListeners will be called and
	 * this Attribute's value will be set to the value returned by the last AttributePreChangeListener (or to the
	 * specified value if no AttributePreChangeListener was registered). Finally the attributeHasChanged method
	 * of all registered AttributePostChangeListeners will be called.
	 * @param value the value to set this Attribute to
	 * @return the value the Attribute has been set too (might differ from the specified value because of vetos by AttributePreChangeListeners)
	 */
	public boolean setBoolean(boolean value) {
		if (value != this.value) {
			this.value = fireAttributeWillChange(value);
			fireAttributeHasChanged();
		}
		return this.value;
	}
}
