package com.jpatch.afw.attributes;

import java.util.*;

/**
 * An Attribute that wraps around a collection and notifies
 * its listeners if elements were added to or removed
 * from the collection.
 * @param <T> type of the elements in the collection
 */
public class MutableCollectionAttr<T> extends CollectionAttr<T> {
	
	/**
	 * Creates a new CollectionAttr that uses the specified collection type as backing collection
	 * @param collectionClass the Collection class to be used as backing collection
	 */
	public MutableCollectionAttr(Class<? extends Collection> collectionClass) {
		super(collectionClass);
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
}
