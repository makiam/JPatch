package com.jpatch.afw.attributes;

public class HardBoundedDoubleAttr extends DoubleAttr {
	private final double min;
	private final double max;
	
	public HardBoundedDoubleAttr(double min, double max) {
		this(min, max, min);
	}
	
	public HardBoundedDoubleAttr(double min, double max, double value) {
		super(value);
		if (min > max) {
			throw new IllegalArgumentException("min (" + min + ") > max (" + max + ")");
		}
		if (value < min || value > max) {
			throw new IllegalArgumentException("value (" + value + ") is outside of range (" + min + " .. " + max + ")");
		}
		this.min = min;
		this.max = max;
	}
	
	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	@Override
	public double setDouble(double value) {
		if (value < min) {
			return super.setDouble(min);
		} else if (value > max) {
			return super.setDouble(max);
		} else {
			return super.setDouble(value);
		}
	}
}
