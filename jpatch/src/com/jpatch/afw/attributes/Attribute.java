package com.jpatch.afw.attributes;
/**
 * An Attribute is a wrapper around a primitive or object. Unlike the primitive wrappers in java.lang
 * (Integer, Boolean, Float, etc.), Attributes are deliberately mutable.
 * The purpose of an Attribute is to provide event notification when the wrapped value was changed.
 * Unlike the event processing in the AWT and in Swing, Attributes provide <i>pre</i>- and
 * <i>post</i>-event-notifications.
 * <p>
 * This interface only defines the methods needed to add and remove <i>AttributePreChangeListeners</i>
 * and <i>AttributePostChangeListener</i>.
 * <p>
 * Classes implementing this interface should call the <i>attributeWillChange</i> method on every registered
 * <i>AttributePreChangeListener</i> and the <i>attributeHasChanged</i> method on every registered
 * <i>AttributePostChangeListener</i>.
 * <p>
 * <i>AttributePreChangeListeners</i> are able to "veto" against a change by returing another value than
 * the one that has been passed with the <i>attributeWillChange</i> method. The Attribute will eventually
 * set its value to the value that was returned by the <i>attributeWillChange</i> method of the last
 * registered <i>AttributePreChangeListener</i> and then fire it's <i>attributeHasChanged</i> notifications.
 * @see AbstractAttribute
 * @author sascha
 * TODO move detailed descriptions into a separate package.html javadoc file
 */
public interface Attribute {
	/**
	 * Adds the specified AttributePreChangeListener to this Attribute's listener-list
	 * @param listener the listener to add
	 */
	public void addAttributePreChangeListener(AttributePreChangeListener listener);
	
	/**
	 * Adds the specified AttributePostChangeListener to this Attribute's listener-list
	 * @param listener the listener to add
	 */
	public void addAttributePostChangeListener(AttributePostChangeListener listener);
	
	/**
	 * Removes the specified AttributePreChangeListener from this Attribute's listener-list
	 * @param listener the listener to remove
	 */
	public void removeAttributePreChangeListener(AttributePreChangeListener listener);
	
	/**
	 * Removes the specified AttributePostChangeListener from this Attribute's listener-list
	 * @param listener the listener to add
	 */
	public void removeAttributePostChangeListener(AttributePostChangeListener listener);
}
