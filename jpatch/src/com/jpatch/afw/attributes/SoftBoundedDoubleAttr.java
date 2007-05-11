package com.jpatch.afw.attributes;

public class SoftBoundedDoubleAttr extends DoubleAttr {
	private final DoubleAttr minAttr;
	private final DoubleAttr maxAttr;
	private final BooleanAttr minLimitAttr;
	private final BooleanAttr maxLimitAttr;
	
	private final AttributePostChangeListener attributeListener = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			setDouble(value);
		}
	};
	
	public SoftBoundedDoubleAttr(double min, double max) {
		this(min, max, min);
	}
	
	public SoftBoundedDoubleAttr(double min, double max, double value) {
		super(value);
		if (min > max) {
			throw new IllegalArgumentException("min (" + min + ") > max (" + max + ")");
		}
		if (value < min || value > max) {
			throw new IllegalArgumentException("value (" + value + ") is outside of range (" + min + " .. " + max + ")");
		}
		minAttr = new DoubleAttr(min);
		maxAttr = new DoubleAttr(max);
		minLimitAttr = new BooleanAttr(false);
		maxLimitAttr = new BooleanAttr(false);
		minAttr.addAttributePostChangeListener(attributeListener);
		maxAttr.addAttributePostChangeListener(attributeListener);
		minLimitAttr.addAttributePostChangeListener(attributeListener);
		maxLimitAttr.addAttributePostChangeListener(attributeListener);
	}
	
	public double getMin() {
		return minAttr.getDouble();
	}

	public double getMax() {
		return maxAttr.getDouble();
	}

	public DoubleAttr getMinAttr() {
		return minAttr;
	}
	
	public DoubleAttr getMaxAttr() {
		return maxAttr;
	}
	
	public BooleanAttr getMinLimitAttr() {
		return minLimitAttr;
	}
	
	public BooleanAttr getMaxLimitAttr() {
		return maxLimitAttr;
	}
	
	@Override
	public double setDouble(double value) {
		if (minLimitAttr.getBoolean() && value < minAttr.getDouble()) {
			return super.setDouble(minAttr.getDouble());
		} else if (maxLimitAttr.getBoolean() && value > maxAttr.getDouble()) {
			return super.setDouble(maxAttr.getDouble());
		} else {
			return super.setDouble(value);
		}
	}
}
