package com.jpatch.afw.attributes;

import java.util.Collection;
import java.util.Collections;

/**
 * An Attribute that wraps around a collection and notifies
 * its listeners if elements were added to or removed
 * from the collection. This is the baseclass of all CollectionAttr classes
 * like MutableCollectionAttr and UnmodifiableCollectionAttr.
 * @param <T> type of the elements in the collection
 */
public abstract class CollectionAttr<T> extends AbstractAttribute {
	/**
	 * Backing collection
	 */
	final Collection<T> collection;
	
	/**
	 * An anmodifiable view of the backing collection - this is passed to clients that call getElements().
	 */
	private final Collection<T> unmodifiableView;
	
	/**
	 * Package private constructor needed by MutableCollectionAttr
	 * @param collectionClass the Collection class to be used as backing collection
	 */
	@SuppressWarnings("unchecked")
	CollectionAttr(Class<? extends Collection> collectionClass) {
		try {
			collection = collectionClass.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		unmodifiableView = Collections.unmodifiableCollection(collection);
	}
			
	/**
	 * Creates a new CollectionAttr that uses the specified collection type as backing collection
	 * @param collectionClass the Collection class to be used as backing collection
	 */
	public CollectionAttr(Collection<T> collection) {
		this.collection = collection;
		this.unmodifiableView = Collections.unmodifiableCollection(this.collection);
	}
	
	/**
	 * Returns an Collection of all elements of this CollectionAttr
	 * @return an Collection of all elements of this CollectionAttr. It's actually an unmodifiable
	 * view of the collection, so attempts to modify it will result in an UnsupportedOperationException.
	 */
	public Collection<T> getElements() {
		return unmodifiableView;
	}
	
	/**
	 * Returns the number of elements in this CollectionAttr
	 * @return the number of elements in this CollectionAttr
	 */
	public int size() {
		return collection.size();
	}
	
	/**
	 * Returns true if this CollectionAttr contains the specified object.
	 * @param object object whose precence in this CollectionAttr is to be tested
	 * @return true is this CollectionAttr contains the specified object, false otherwise.
	 */
	public boolean contains(Object object) {
		return collection.contains(object);
	}
	
	/**
	 * Returns true if this CollectionAttr contains all of the elements in the specified collection.
	 * @param objects collection to be checked for containment in this CollectionAttr
	 * @return true if this CollectionAttr contains all of the elements in the specified collection, false otherwise.
	 */
	public boolean containsAll(Collection objects) {
		return collection.containsAll(objects);
	}
}
