package com.jpatch.test;

import java.util.*;

public class SetBenchmark {
	static final int COUNT = 10000;
	
	public static void main(String[] args) {
		new SetBenchmark();
	}
	
	final Object[] collections = new Object[] {
		new HashSet<Integer>(),
		new LinkedHashSet<Integer>(),
		new TreeSet<Integer>(),
		new ArrayList<Integer>(),
		new LinkedList<Integer>(),
		new Integer[COUNT]
	};
	
	final long[] fill = new long[collections.length];
	final long[] loop = new long[collections.length];
	final long[] remove = new long[collections.length];
	
	final Integer[] integers = new Integer[COUNT];
	
	SetBenchmark() {
		for (int i = 0; i < COUNT; i++) {
			integers[i] = i;
		}
		for (int i = 0; i < 20; i++) {
			System.out.println(i);
			for (int j = 0; j < collections.length; j++) {
				benchmark(j);
			}
		}
		
		for (int i = 0; i < collections.length; i++) {
			System.out.println(collections[i].getClass().getName() + "\t  " + fill[i] + "\t  " + loop[i] + "\t  " + remove[i]);
		}
	}
	
	void benchmark(int i) {
		Object collection = collections[i];
		
		long t;
		
		t = System.nanoTime();
		if (collection instanceof Collection) {
			fill((Collection) collection);
		} else {
			fill((Integer[]) collection);
		}
		fill[i] = System.nanoTime() - t;
		
		t = System.nanoTime();
		if (collection instanceof Collection) {
			loop((Collection) collection);
		} else {
			loop((Integer[]) collection);
		}
		loop[i] = System.nanoTime() - t;
		
		t = System.nanoTime();
		if (collection instanceof Collection) {
			remove((Collection) collection);
		} else {
			remove((Integer[]) collection);
		}
		remove[i] = System.nanoTime() - t;
	}
	
	void fill(Collection<Integer> collection) {
		for (int i = 0; i < COUNT; i++) {
			collection.add(integers[i]);
		}
	}
	
	void loop(Collection<Integer> collection) {
		int i = 0;
		for (Integer s : collection) {
			i += s;
		}
	}
	
	void remove(Collection<Integer> collection) {
		for (Integer i : integers) {
			collection.remove(i);
		}
	}
	
	void fill(Integer[] array) {
		for (int i = 0; i < COUNT; i++) {
			array[i] = integers[i];
		}
	}
	
	void remove(Integer[] array) {
		;
	}
	
	void loop(Integer[] array) {
		int i = 0;
		for (Integer s : array) {
			i += s;
		}
	}
	
}
