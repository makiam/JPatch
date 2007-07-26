package com.jpatch.entity;

import java.util.HashSet;

import com.jpatch.afw.attributes.*;

public abstract class SceneGraphNode {
	private final GenericAttr<SceneGraphNode> parentAttr;
	private final CollectionAttr<SceneGraphNode> childrenAttr = new CollectionAttr<SceneGraphNode>(HashSet.class);
	private final UnmodifiableCollectionAttr<SceneGraphNode> childrenAttrView = new UnmodifiableCollectionAttr<SceneGraphNode>(childrenAttr);
	
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
	
	public abstract Transform getTransform();
	
}
