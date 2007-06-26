package com.jpatch.afw.attributes;
/**
 * Adds pre-event notification to Attributes
 * This interface only defines the methods needed to add and remove <i>AttributePreChangeListeners</i>
 * <p>
 * Classes implementing this interface should call the <i>attributeWillChange</i> method on every registered
 * <i>AttributePreChangeListener</i> when the attribute is about to change.
 * <p>
 * <i>AttributePreChangeListeners</i> are able to "veto" against a change by returing another value than
 * the one that has been passed with the <i>attributeWillChange</i> method. The Attribute will eventually
 * set its value to the value that was returned by the <i>attributeWillChange</i> method of the last
 * registered <i>AttributePreChangeListener</i> and then fire it's <i>attributeHasChanged</i> notifications.
 * 
 * @see AbstractScalarAttribute
 * @author sascha
 * TODO move detailed descriptions into a separate package.html javadoc file
 */
public interface ScalarAttribute extends Attribute {

	/**
	 * Adds the specified AttributePreChangeListener to this Attribute's listener-list
	 * @param listener the listener to add
	 */
	public void addAttributePreChangeListener(AttributePreChangeListener listener);
	
	/**
	 * Removes the specified AttributePreChangeListener from this Attribute's listener-list
	 * @param listener the listener to remove
	 */
	public void removeAttributePreChangeListener(AttributePreChangeListener listener);
}
