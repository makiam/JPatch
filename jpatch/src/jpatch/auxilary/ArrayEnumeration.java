package jpatch.auxilary;

import java.util.*;

public final class ArrayEnumeration
implements Enumeration {
	private int iIndex = 0;
	private Object[] array;
	
	public ArrayEnumeration(Object[] array) {
		this.array = array;
	}
	
	public final boolean hasMoreElements() {
		return (iIndex < array.length);
	}
	
	public final Object nextElement() {
		return array[iIndex++];
	}
}
