package com.jpatch.afw;

import java.util.*;

public class CombinedIterable<T> implements Iterable<T> {
	private final Iterable<T>[] iterables;
	
	public CombinedIterable(Iterable<T>... iterables) {
		this.iterables = iterables.clone();
	}
	
	@SuppressWarnings("unchecked")
	public Iterator<T> iterator() {
		Iterator[] iterators = new Iterator[iterables.length];
		for (int i = 0; i < iterators.length; i++) {
			iterators[i] = iterables[i].iterator();
		}
		return new CombinedIterator<T>(iterators);
	}
	
	public static void main(String[] args) {
		List<String> a = new ArrayList<String>();
		List<String> b = new ArrayList<String>();
//		a.add("a");
//		a.add("b");
		b.add("c");
		b.add("d");
		for (String s : new CombinedIterable<String>(a, b)) {
			System.out.print(s);
		}
	}
}
