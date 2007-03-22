package jpatch.entity.attributes2;

public class ArrayAttr<T> extends AbstractAttribute {
	T[] array;
	int index;
	
	public ArrayAttr(T[] array) {
		if (array == null) {
			throw new NullPointerException();
		}
		if (array.length == 0) {
			throw new IllegalArgumentException("array " + array + " is empty");
		}
		this.array = array;
	}
	
	public void setIndex(int index) {
		if (index < 0 || index >= array.length) {
			throw new ArrayIndexOutOfBoundsException(index);
		}
		this.index = index;
	}
	
	public void setObject(T object) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == object) {
				index = i;
				return;
			}
		}
		if (object == null) {
			throw new NullPointerException();
		} else {
			throw new IllegalArgumentException(object + " is not an array element");
		}
	}
	
	public int getIndex() {
		return index;
	}
	
	public T getObject() {
		return array[index];
	}
}
