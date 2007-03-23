package jpatch.entity.attributes2;

public class IntAttr extends AbstractAttribute implements IntValue {
	protected int value;

	public IntAttr() {
		this(0);
	}
	
	public IntAttr(int value) {
		setInt(value);
	}
	
	public int getInt() {
		return value;
	}

	public void setInt(int value) {
		if (value != this.value) {
			this.value = value;
			fireAttributeChanged();
		}
	}
}
