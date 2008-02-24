package com.jpatch.test;

public class ArrayTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Object[] a = new Object[] { 1, 2, "test", "x" };
		Object[] b = a.clone();
		System.out.println(a.hashCode());
		System.out.println(b.hashCode());
		System.out.println(a.equals(b));
	}

}
