package com.jpatch.afw.attributes;

public class DoubleArrayAttr extends AbstractAttribute {
	private final DoubleAttr[] doubleAttrs;
	private boolean changed;
	
	private final AttributePostChangeListener listener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			fireAttributeHasChanged();
			changed = true;
		}
	};
	
	public DoubleArrayAttr(int dimensions) {
		doubleAttrs = new DoubleAttr[dimensions];
		for (int i = 0; i < dimensions; i++) {
			doubleAttrs[i] = new DoubleAttr();
			doubleAttrs[i].addAttributePostChangeListener(listener);
		}
	}
	
	public final int getDimensions() {
		return doubleAttrs.length;
	}
	
	public DoubleAttr getAttr(int index) {
		return doubleAttrs[index];
	}
	
	public double getDouble(int index) {
		return doubleAttrs[index].getDouble();
	}
	
	public DoubleAttr[] getAttributes(final DoubleAttr[] attributes) {
		assert attributes.length == doubleAttrs.length;
		System.arraycopy(doubleAttrs, 0, attributes, 0, doubleAttrs.length);
		return attributes;
	}
	
	public double[] getDoubles(final double[] values) {
		assert values.length == doubleAttrs.length;
		for (int i = 0; i < values.length; i++) {
			values[i] = doubleAttrs[i].getDouble();
		}
		return values;
	}
	
	public void setDouble(int index, double value) {
		doubleAttrs[index].setDouble(value);
	}
	
	public void setDoubles(double[] values) {
		assert values.length == doubleAttrs.length;
		changed = false;
		fireEvents = false;
		for (int i = 0; i < values.length; i++) {
			doubleAttrs[i].setDouble(values[i]);
		}
		fireEvents = true;
		if (changed) {
			fireAttributeHasChanged();
		}
	}
}
