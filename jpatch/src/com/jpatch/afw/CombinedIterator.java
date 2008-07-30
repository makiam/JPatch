package com.jpatch.afw;

import java.util.*;

public class CombinedIterator<T> implements Iterator<T> {
	private final Iterator<T>[] iterators;
	private int index = 0;
	private T nextElement;
	
	public CombinedIterator(Iterator<T>... iterators) {
		this.iterators = iterators.clone();
		nextElement = getNextElement();
	}
	
	public boolean hasNext() {
		return nextElement != null;
	}

	public T next() {
		if (nextElement != null) {
			T tmp = nextElement;
			nextElement = getNextElement();
			return tmp;
		}
		throw new NoSuchElementException();
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	private T getNextElement() {
		if (iterators[index].hasNext()) {
			return iterators[index].next();
		} else {
			index++;
			if (index < iterators.length) {
				return getNextElement();
			}
		}
		return null;
	}
}
