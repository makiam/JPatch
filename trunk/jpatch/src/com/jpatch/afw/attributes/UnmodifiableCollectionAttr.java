package com.jpatch.afw.attributes;

public class UnmodifiableCollectionAttr<T> extends CollectionAttr<T> {
	public UnmodifiableCollectionAttr(CollectionAttr<T> backingCollection) {
		super(backingCollection.collection);
		backingCollection.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				fireAttributeHasChanged();
			}
		});
	}
}
