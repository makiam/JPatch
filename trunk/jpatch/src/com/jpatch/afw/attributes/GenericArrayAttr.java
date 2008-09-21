package com.jpatch.afw.attributes;

public class GenericArrayAttr<T> extends AbstractAttribute {
	private final GenericAttr<T>[] genericAttrs;
	private boolean changed;
	
	private final AttributePostChangeListener listener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			fireAttributeHasChanged();
			changed = true;
		}
	};
	
	public GenericArrayAttr(int dimensions) {
		genericAttrs = new GenericAttr[dimensions];
		for (int i = 0; i < dimensions; i++) {
			genericAttrs[i] = new GenericAttr<T>();
			genericAttrs[i].addAttributePostChangeListener(listener);
		}
	}
	
	public final int getDimensions() {
		return genericAttrs.length;
	}
	
	public GenericAttr<T> getAttr(int index) {
		return genericAttrs[index];
	}
	
	public T getValue(int index) {
		return genericAttrs[index].getValue();
	}
	
	public GenericAttr<T>[] getAttributes(final GenericAttr<T>[] attributes) {
		assert attributes.length == genericAttrs.length;
		System.arraycopy(genericAttrs, 0, attributes, 0, genericAttrs.length);
		return attributes;
	}
	
	public T[] getValues(final T[] values) {
		assert values.length == genericAttrs.length;
		for (int i = 0; i < values.length; i++) {
			values[i] = genericAttrs[i].getValue();
		}
		return values;
	}
	
	public void setValue(int index, T value) {
		genericAttrs[index].setValue(value);
	}
	
	public void setValues(T[] values) {
		assert values.length == genericAttrs.length;
		changed = false;
		fireEvents = false;
		for (int i = 0; i < values.length; i++) {
			genericAttrs[i].setValue(values[i]);
		}
		fireEvents = true;
		if (changed) {
			fireAttributeHasChanged();
		}
	}
}
