package com.jpatch.afw.attributes;

public class GenericAttr<T> extends AbstractScalarAttribute<T> {
	T value;

	public GenericAttr() {
		this(null);
	}
	
	public GenericAttr(T value) {
		this.value = value;
	}
	
	
	public T getValue() {
		return value;
	}

	public T setValue(T value) {
		if ((value == null && this.value != null) || (!value.equals(this.value))) {
			this.value = fireAttributeWillChange(value);
			fireAttributeHasChanged();
		}
		return this.value;
	}
}
