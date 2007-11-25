package com.jpatch.entity;

import java.util.HashSet;

import com.jpatch.afw.attributes.*;
import com.jpatch.afw.vecmath.Transform;

public abstract class SceneGraphNode implements NamedObject {
	private final GenericAttr<SceneGraphNode> parentAttr;
	private final CollectionAttr<SceneGraphNode> childrenAttr = new CollectionAttr<SceneGraphNode>(HashSet.class);
	private final UnmodifiableCollectionAttr<SceneGraphNode> childrenAttrView = new UnmodifiableCollectionAttr<SceneGraphNode>(childrenAttr);
	private final GenericAttr<String> nameAttr = new GenericAttr<String>("");
	
	protected SceneGraphNode() {
		this(null);
	}
	
	protected SceneGraphNode(SceneGraphNode parent) {
		parentAttr = new GenericAttr<SceneGraphNode>(parent);
		parentAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<SceneGraphNode>() {
			@Override
			public SceneGraphNode attributeWillChange(ScalarAttribute source, SceneGraphNode value) {
				SceneGraphNode parent = (SceneGraphNode) parentAttr.getValue();
				if (parent != null) {
					parent.childrenAttr.remove(SceneGraphNode.this);
				}
				return value;
			}
		});
		parentAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				SceneGraphNode parent = (SceneGraphNode) parentAttr.getValue();
				if (parent != null) {
					parent.childrenAttr.add(SceneGraphNode.this);
				}
			}
		});
	}
	
	public GenericAttr<SceneGraphNode> getParentAttribute() {
		return parentAttr;
	}
	
	public UnmodifiableCollectionAttr<SceneGraphNode> getChildrenAttribute() {
		return childrenAttrView;
	}
	
	/**
	 * Returns the name attribute
	 * @return the name attribute
	 */
	public GenericAttr<String> getNameAttribute() {
		return nameAttr;
	}
	
	@Override
	public String toString() {
		return nameAttr.getValue();
	}
	
//	public abstract Transform getTransform();
	
}
