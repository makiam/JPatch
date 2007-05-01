package com.jpatch.afw.attributes;

public class GenericAttr<T> extends AbstractAttribute<T> {
	protected T value;

	public GenericAttr() {
		this(null);
	}
	
	public GenericAttr(T value) {
		this.value = value;
	}
	
	
	public T getObject() {
		return value;
	}

	public T setObject(T value) {
		if ((value == null && this.value != null) || (!value.equals(this.value))) {
			this.value = fireAttributeWillChange(value);
			fireAttributeHasChanged();
		}
		return this.value;
	}
}
