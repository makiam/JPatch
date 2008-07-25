package com.jpatch.entity.sds2;

public class ArrayCastTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		B[] array = new B[1];
		test(array);

	}
	
	
	private static class A { }
	private static class B extends A { }
	
	private static A[] test(A[] array) {
		array[0] = new A();
		return array;
	}
}
