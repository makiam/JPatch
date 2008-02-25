package com.jpatch.afw;
import java.util.*;

public class BitEnum<T extends Enum> {
	private final BitSet bitSet = new BitSet();
	
	public BitEnum(T ... bits) {
		for (T bit : bits) {
			bitSet.set(bit.ordinal());
		}
	}
	
	public boolean is(T bit) {
		return bitSet.get(bit.ordinal());
	}
	
	public boolean isAny(T ... bits) {
		for (T bit : bits) {
			if (bitSet.get(bit.ordinal())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isAll(T ... bits) {
		for (T bit : bits) {
			if (!bitSet.get(bit.ordinal())) {
				return false;
			}
		}
		return true;
	}
}
