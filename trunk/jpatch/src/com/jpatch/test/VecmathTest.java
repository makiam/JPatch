package com.jpatch.test;

import java.util.*;
import javax.vecmath.*;

public class VecmathTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Point3d p0 = new Point3d(1, 2, 3);
		Point3d p1 = new Point3d(1, 2, 3);
		System.out.println(p0.hashCode());
		System.out.println(p1.hashCode());
		System.out.println(p0.equals((Object) p1));
		
		Map<Point3d, String> map1 = new Hashtable<Point3d, String>();
		map1.put(p0, "p0");
		map1.put(p1, "p1");
		System.out.println(map1.get(p0));
		System.out.println(map1.get(p1));
		
		System.out.println("---");
		Test t0 = new Test(1);
		Test t1 = new Test(1);
		System.out.println(t0.hashCode());
		System.out.println(t1.hashCode());
		System.out.println(t0.equals(t1));
		
		Map<Test, String> map2 = new HashMap<Test, String>();
		map2.put(t0, "t0");
		map2.put(t1, "t1");
		System.out.println(map2.get(t0));
		System.out.println(map2.get((Object) t1));
	}

	
	private static class Test {
		final int i;
		private Test(int i) {
			this.i = i;
		}
		
		public int hashCode() {
			return i;
		}
		
		public boolean equals(Object o) {
			if (o instanceof Test) {
				return ((Test) o).i == i;
			}
			return false;
		}
	}
}
