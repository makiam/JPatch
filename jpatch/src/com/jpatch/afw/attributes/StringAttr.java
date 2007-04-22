package com.jpatch.afw.attributes;

public class StringAttr extends AbstractAttribute {
	protected String value;

	public StringAttr(String value) {
		this.value = value;
	}
	
	
	public String getString() {
		return value;
	}

	public void setString(String value) {
		if (!value.equals(this.value)) {
			this.value = value;
			fireAttributeChanged();
		}
	}
}
