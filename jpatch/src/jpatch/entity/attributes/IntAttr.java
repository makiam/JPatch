package jpatch.entity.attributes;

public class IntAttr extends AbstractAttribute {
	private int value;
	
	public IntAttr() { }
	
	public IntAttr(int value) {
		this.value = value;
	}
	
	public int getInt() {
		return value;
	}
	
	public void setInt(int value) {
		this.value = value;
	}

	@Override
	protected void overrideValue(AbstractAttribute a) {
		value = ((IntAttr) a).value;
	}

	@Override
	public int compareTo(AbstractAttribute a) {
		int otherValue = ((IntAttr) a).value;
		return (value < otherValue ? -1 : (value == otherValue ? 0 : 1));
	}
	
	@Override
	public String toString() {
		return "IntAttr:" + value;
	}
}
