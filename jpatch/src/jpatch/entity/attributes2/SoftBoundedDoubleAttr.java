package jpatch.entity.attributes2;

public class SoftBoundedDoubleAttr extends DoubleAttr implements BoundedDoubleValue {
	private final DoubleAttr minAttr;
	private final DoubleAttr maxAttr;
	private final BooleanAttr minLimitAttr;
	private final BooleanAttr maxLimitAttr;
	
	private final AttributeListener attributeListener = new AttributeListener() {
		public void attributeChanged(Attribute source) {
			setDouble(value);
		}
	};
	
	public SoftBoundedDoubleAttr(double min, double max) {
		this(min, max, min);
	}
	
	public SoftBoundedDoubleAttr(double min, double max, double value) {
		this(min, max, value, new LinearMapping());
	}
	
	public SoftBoundedDoubleAttr(double min, double max, double value, Mapping mapping) {
		super(value, mapping);
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
		minAttr.addAttributeListener(attributeListener);
		maxAttr.addAttributeListener(attributeListener);
		minLimitAttr.addAttributeListener(attributeListener);
		maxLimitAttr.addAttributeListener(attributeListener);
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
	public void setDouble(double value) {
		if (minLimitAttr.getBoolean() && value < minAttr.getDouble()) {
			super.setDouble(minAttr.getDouble());
		} else if (maxLimitAttr.getBoolean() && value > maxAttr.getDouble()) {
			super.setDouble(maxAttr.getDouble());
		} else {
			super.setDouble(value);
		}
	}
}
