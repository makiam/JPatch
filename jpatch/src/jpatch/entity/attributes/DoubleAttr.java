package jpatch.entity.attributes;

public class DoubleAttr extends AbstractAttribute {
	private double value;
	
	public DoubleAttr() { }
	
	public DoubleAttr(double value) {
		this.value = value;
	}
	
	public double getDouble() {
		return value;
	}
	
	public void setDouble(double value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "DoubleAttr:" + value;
	}
}
