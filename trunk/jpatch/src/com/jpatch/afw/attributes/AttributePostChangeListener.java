package com.jpatch.afw.attributes;

public interface AttributePostChangeListener {
	/**
	 * This methods gets be called from Attributes whose values have changed.
	 * @param source the Attribute whose value has changed
	 */
	public void attributeHasChanged(Attribute source);
}
