package test;

import java.util.*;

import javax.swing.*;

public class HashVsBinary {
	private final Long[] array;
	private final Map<Long, Integer> hash = new HashMap<Long, Integer>();
	private final ArrayList<Long> list = new ArrayList<Long>();
	
	public static void main(String[] args) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		
		HashVsBinary test = new HashVsBinary(1000);
//		/* warmup */
//		for (int i = 0; i < 5; i++) {
//			test.lookup(i, 1000);
//		}
//		
//		/* test */
//		for (int n = 0; n < 7; n++) {
//			int cap = (int) Math.pow(10, n);
//			int count = 10000000 / cap;
//			test = new HashVsBinary(cap);
//			System.out.println(cap + "/" + count + ":");
//			for (int i = 0; i < 5; i++) {
//				System.out.println(i + " " + test.lookup(i, count));
//			}
//		}
		
		/* warmup */
		for (int i = 0; i < 2; i++) {
			test.clone(i, 1000000);
		}
		
		/* test */
		for (int i = 0; i < 2; i++) {
			System.out.println(i + " " + test.clone(i, 1000000));
		}
	}
	
	private HashVsBinary(int capacity) {
		array = new Long[capacity];
		for (int i = 0; i < capacity; i++) {
			Long l = new Long(i * 10);
			array[i] = l;
			hash.put(l, i);
			list.add(l);
		}
	}
	
	private long cast(int test, int count) {
		long t = System.currentTimeMillis();
		Long l = new Long(100);
		Object o = new Long(100);
		Long b = null;
		switch (test) {
		case 0:	// cast
			for (int i = 0; i < count; i++) {
				b = l;
			}
			break;
		case 1:	// cast
			for (int i = 0; i < count; i++) {
				b = (Long) o;
			}
			break;
		}
		o = b;
		return System.currentTimeMillis() - t;
	}

	private static class Test implements Cloneable {
		final long value;
		
		Test(long l) {
			value = l;
		}
		
		public Test clone() {
			try {
				return (Test) super.clone();
			} catch (CloneNotSupportedException e) {
				return null;
			}
		}
	}
	
	private long clone(int test, int count) {
		long t = System.currentTimeMillis();
		Test t1 = new Test(100);
		Test t2 = null;
		switch (test) {
		case 0:	// cast
			for (int i = 0; i < count; i++) {
				t2 = new Test(100);
			}
			break;
		case 1:	// cast
			for (int i = 0; i < count; i++) {
				t2 = t1.clone();
			}
			break;
		}
		t1 = t2;
		return System.currentTimeMillis() - t;
	}
	
	private long lookup(int test, int count) {
		long t = System.currentTimeMillis();
		long a = 0;
		switch (test) {
		case 0:	// array
			for (int i = 0; i < count; i++) {
				for (int j = 0; j < array.length; j++) {
					int index = Arrays.binarySearch(array, array[j]);
//					System.out.println(j + " " + index + " " + array[j]);
				}
			}
			break;
		case 1: // hash
			for (int i = 0; i < count; i++) {
				for (int j = 0; j < array.length; j++) {
					int index = hash.get(array[j]);
//					System.out.println(j + " " + index + " " + array[j]);
				}
			}
			break;
		case 2:	// array
			for (int i = 0; i < count; i++) {
				for (Long l : array) {
					a += l;
				}
			}
			break;
		case 3: // hash
			for (int i = 0; i < count; i++) {
				for (Long l : hash.keySet()) {
					a += l;
				}
			}
			break;
		case 4: // hash
			for (int i = 0; i < count; i++) {
				for (Long l : list) {
					a += l;
				}
			}
			break;
		}
		return System.currentTimeMillis() - t;
	}
}
