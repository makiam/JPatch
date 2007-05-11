package com.jpatch.afw.attributes;

public interface Attribute {
	public void addAttributePreChangeListener(AttributePreChangeListener listener);
	public void addAttributePostChangeListener(AttributePostChangeListener listener);
	public void removeAttributePreChangeListener(AttributePreChangeListener listener);
	public void removeAttributePostChangeListener(AttributePostChangeListener listener);
}
