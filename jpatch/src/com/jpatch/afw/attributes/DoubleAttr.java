package com.jpatch.afw.attributes;

public class DoubleAttr extends AbstractAttribute {
	protected Mapping mapping;
	protected double value;

	public DoubleAttr() {
		this(0);
	}
	
	public DoubleAttr(double value) {
		this(value, new LinearMapping());
	}
	
	public DoubleAttr(double value, Mapping mapping) {
		setDouble(value);
		this.mapping = mapping;
	}
	
	public double getDouble() {
		return value;
	}

	public double setDouble(double value) {
		if (value != this.value) {
			this.value = fireAttributeWillChange(value);
			fireAttributeHasChanged();
		}
		return this.value;
	}

	public Mapping getMapping() {
		return mapping;
	}

	public double getMappedValue() {
		return mapping.getMappedValue(getDouble());
	}

	public void setMappedValue(double mappedValue) {
		setDouble(mapping.getValue(mappedValue));
	}
	
	@Override
	public String toString() {
		return Double.toString(value);
	}
}
