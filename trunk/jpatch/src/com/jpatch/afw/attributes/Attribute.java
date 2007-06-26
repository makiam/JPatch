package com.jpatch.afw.attributes;
/**
 * An Attribute is a wrapper around a primitive or object. Unlike the primitive wrappers in java.lang
 * (Integer, Boolean, Float, etc.), Attributes are deliberately mutable.
 * The purpose of an Attribute is to provide event notification when the wrapped value was changed.
 * This interface only defines the methods needed to add and remove <i>AttributePostChangeListener</i>.
 * <p>
 * Classes implementing this interface should call the <i>attributeHasChanged</i> method on every registered
 * <i>AttributePostChangeListener</i> when the attribute has changed.
 * @see AbstractScalarAttribute
 * @author sascha
 * TODO move detailed descriptions into a separate package.html javadoc file
 */
public interface Attribute {
	
	/**
	 * Adds the specified AttributePostChangeListener to this Attribute's listener-list
	 * @param listener the listener to add
	 */
	public void addAttributePostChangeListener(AttributePostChangeListener listener);
	
	/**
	 * Removes the specified AttributePostChangeListener from this Attribute's listener-list
	 * @param listener the listener to add
	 */
	public void removeAttributePostChangeListener(AttributePostChangeListener listener);
}
