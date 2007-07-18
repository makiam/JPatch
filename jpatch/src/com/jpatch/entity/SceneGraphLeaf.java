package com.jpatch.entity;

import com.jpatch.afw.attributes.*;

public class SceneGraphLeaf {
	protected final GenericAttr<SceneGraphNode> parentAttr;
	
	protected SceneGraphLeaf() {
		this(null);
	}
	
	protected SceneGraphLeaf(SceneGraphNode parent) {
		parentAttr = new GenericAttr<SceneGraphNode>(parent);
		parentAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<SceneGraphNode>() {
			@Override
			public SceneGraphNode attributeWillChange(ScalarAttribute source, SceneGraphNode value) {
				SceneGraphNode parent = (SceneGraphNode) parentAttr.getValue();
				if (parent != null) {
					parent.childrenAttr.remove(SceneGraphLeaf.this);
				}
				return value;
			}
		});
		parentAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				SceneGraphNode parent = (SceneGraphNode) parentAttr.getValue();
				if (parent != null) {
					parent.childrenAttr.add(SceneGraphLeaf.this);
				}
			}
		});
	}
	
	public GenericAttr<SceneGraphNode> getParentAttribute() {
		return parentAttr;
	}
}
