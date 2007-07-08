package com.jpatch.afw;

import java.util.*;

public class Utils {
	/**
	 * Returns an Enumeration that iterates over all elements of the specified Iterator
	 * @param it the Iterator that backs this Enumeration
	 * @return an Enumeration that iterates over all elements of the specified Iterator
	 */
	public static Enumeration createEnumeration(final Iterator it) {
		return new Enumeration() {
			public boolean hasMoreElements() {
				return it.hasNext();
			}
			public Object nextElement() {
				return it.next();
			}
		};
	}
	
	/**
	 * Returns an Iterator that iterates over all elements of the specified Enumeration.
	 * The Iterator's <i>remove()</i> method is not supported, calling it will throw an
	 * <i>UnsupportedOperationException</i>.
	 * @param e the Enumeration that backs this Iterator
	 * @return an Iterator that iterates over all elements of the specified Enumeration
	 */
	public static Iterator createIterator(final Enumeration e) {
		return new Iterator() {
			public boolean hasNext() {
				return e.hasMoreElements();
			}
			public Object next() {
				return e.nextElement();
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
