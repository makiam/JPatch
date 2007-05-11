package com.jpatch.afw.attributes;

public class IntAttr extends AbstractAttribute {
	protected int value;

	public IntAttr() {
		this(0);
	}
	
	public IntAttr(int value) {
		setInt(value);
	}
	
	public int getInt() {
		return value;
	}

	public int setInt(int value) {
		if (value != this.value) {
			this.value = fireAttributeWillChange(value);
			fireAttributeHasChanged();
		}
		return this.value;
	}
	
	@Override
	public String toString() {
		return Integer.toString(value);
	}
}
