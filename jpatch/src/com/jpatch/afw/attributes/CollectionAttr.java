package com.jpatch.afw.attributes;

import java.util.*;

/**
 * An Attribute that wraps around a collection and notifies
 * its listeners if elements were added to or removed
 * from the collection.
 * @param <T> type of the elements in the collection
 */
public class CollectionAttr<T> extends AbstractAttribute {
	/**
	 * Backing collection
	 */
	final Collection<T> collection;
	
	/**
	 * An anmodifiable view of the backing collection - this is passed to clients that call getElements().
	 */
	private final Collection<T> unmodifiableView;
	
	
	CollectionAttr(Collection<T> collection) {
		this.collection = collection;
		unmodifiableView = Collections.unmodifiableCollection(this.collection);
	}
	
	/**
	 * Creates a new CollectionAttr that uses the specified collection type as backing collection
	 * @param collectionClass the Collection class to be used as backing collection
	 */
	@SuppressWarnings("unchecked")
	public CollectionAttr(Class<? extends Collection> collectionClass) {
		try{
			collection = collectionClass.newInstance();
			unmodifiableView = Collections.unmodifiableCollection(collection);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Creates a new CollectionAttr that uses the specified collection type as backing collection
	 * @param collectionClass the Collection class to be used as backing collection
	 * @param elements the elements to add to this collection
	 */
	@SuppressWarnings("unchecked")
	public CollectionAttr(Class<? extends Collection> collectionClass, T[] elements) {
		this(collectionClass);
		for (T element : elements) {
			add(element);
		}
	}
	
	/**
	 * Returns an Collection of all elements of this CollectionAttr
	 * @return an Collection of all elements of this CollectionAttr
	 */
	public Collection<T> getElements() {
		return unmodifiableView;
	}
	
	/**
	 * Adds the specified element to this CollectionAttr
	 * @param element element to be added to this CollectionAttr
	 */
	public void add(T element) {
		collection.add(element);
		fireAttributeHasChanged();
	}
	
	/**
	 * Adds all the elements of the specified collection to this CollectionAttr
	 * @param elements collection containing the elements to be added to this CollectionAttr
	 */
	public void addAll(Collection<T> elements) {
		collection.addAll(elements);
		fireAttributeHasChanged();
	}
	
	/**
	 * Removes the specified element from this CollectionAttr
	 * @param element element to be added to this CollectionAttr
	 */
	public void remove(T element) {
		collection.remove(element);
		fireAttributeHasChanged();
	}
	
	/**
	 * Removes all the elements of the specified collection from this CollectionAttr
	 * @param elements collection containing the elements to be removed to this CollectionAttr
	 */
	public void removeAll(Collection<T> elements) {
		collection.removeAll(elements);
		fireAttributeHasChanged();
	}
	
	/**
	 * Retains only the elements of the specified collection in this CollectionAttr
	 * @param elements collection containing the elements to be retained in this CollectionAttr
	 */
	public void retainAll(Collection<T> elements) {
		collection.retainAll(elements);
		fireAttributeHasChanged();
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
	
	/**
	 * Returns the number of elements in this CollectionAttr
	 * @return the number of elements in this CollectionAttr
	 */
	public int size() {
		return collection.size();
	}
}
