package jpatch.entity.attributes2;

public class HardBoundedDoubleAttr extends DoubleAttr implements BoundedDoubleValue {
	private final double min;
	private final double max;
	
	public HardBoundedDoubleAttr(double min, double max) {
		this(min, max, min);
	}
	
	public HardBoundedDoubleAttr(double min, double max, double value) {
		this(min, max, value, LinearMapping.getInstance());
	}
	
	public HardBoundedDoubleAttr(double min, double max, double value, Mapping mapping) {
		super(value, mapping);
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
	public void setDouble(double value) {
		if (value < min) {
			super.setDouble(min);
		} else if (value > max) {
			super.setDouble(max);
		} else {
			super.setDouble(value);
		}
	}
}
