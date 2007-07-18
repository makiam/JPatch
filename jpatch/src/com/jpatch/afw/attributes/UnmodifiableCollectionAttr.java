package com.jpatch.afw.attributes;

import java.util.Collection;

public class UnmodifiableCollectionAttr<T> extends CollectionAttr<T> {
	public UnmodifiableCollectionAttr(CollectionAttr<T> backingCollection) {
		super(backingCollection.collection);
		backingCollection.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				fireAttributeHasChanged();
			}
		});
	}

	@Override
	public void add(T element) {
		throw new UnsupportedOperationException(this + " is an UnmodifiableCollection");
	}

	@Override
	public void addAll(Collection<T> elements) {
		throw new UnsupportedOperationException(this + " is an UnmodifiableCollection");
	}

	@Override
	public void remove(T element) {
		throw new UnsupportedOperationException(this + " is an UnmodifiableCollection");
	}

	@Override
	public void removeAll(Collection<T> elements) {
		throw new UnsupportedOperationException(this + " is an UnmodifiableCollection");
	}

	@Override
	public void retainAll(Collection<T> elements) {
		throw new UnsupportedOperationException(this + " is an UnmodifiableCollection");
	}
}
