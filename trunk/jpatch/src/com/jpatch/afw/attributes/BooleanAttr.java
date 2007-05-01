package com.jpatch.afw.attributes;

public class BooleanAttr extends AbstractAttribute {
	protected boolean value;

	public BooleanAttr() {
		this(false);
	}
	
	public BooleanAttr(boolean value) {
		this.value = value;
	}
	
	
	public boolean getBoolean() {
		return value;
	}

	public boolean setBoolean(boolean value) {
		if (value != this.value) {
			this.value = fireAttributeWillChange(value);
			fireAttributeHasChanged();
		}
		return this.value;
	}
}
