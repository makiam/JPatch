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
}
