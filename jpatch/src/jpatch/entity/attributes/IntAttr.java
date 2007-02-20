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
	public String toString() {
		return "IntAttr:" + value;
	}
}
