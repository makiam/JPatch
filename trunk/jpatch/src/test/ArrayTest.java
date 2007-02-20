package test;

import java.util.*;
import java.lang.reflect.*;

public class ArrayTest<T> {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArrayTest<Double> at = new ArrayTest<Double>();
		
		Double[] x = at.newArray(Double.class);
		
		x.clone();
		System.out.println(x.getClass());
	}
	
	public ArrayTest() {
		
	}
	
	public T[] newArray(Class<T> c) {
		return (T[]) Array.newInstance(c, 10);
	}
}
