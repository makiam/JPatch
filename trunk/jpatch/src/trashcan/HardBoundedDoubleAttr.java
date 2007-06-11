package trashcan;

import com.jpatch.afw.attributes.DoubleAttr;

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
		return super.setDouble(getBoundedValue(value));
	}
	
	public double getBoundedValue(double value) {
		return value < min ? min : value > max ? max : value;
	}
}
