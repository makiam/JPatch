package test;

import java.util.*;

public class ArrayCloneTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double[] a = new double[] { 1, 2, 3 };
		double[] b = a.clone();
		a[1] = -2;
		System.out.println(Arrays.toString(a));
		System.out.println(Arrays.toString(b));
	}

}
