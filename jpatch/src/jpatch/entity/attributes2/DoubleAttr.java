package jpatch.entity.attributes2;

public class DoubleAttr extends AbstractAttribute implements DoubleValue {
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

	public void setDouble(double value) {
		if (value != this.value) {
			this.value = value;
			fireAttributeChanged();
		}
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
}
