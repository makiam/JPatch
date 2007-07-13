package com.jpatch.entity;

import com.jpatch.afw.attributes.*;

public class AbstractSceneGraphLeaf implements SceneGraphLeaf {
	private final GenericAttr<SceneGraphNode> parentAttr;
	
	protected AbstractSceneGraphLeaf() {
		this(null);
	}
	
	protected AbstractSceneGraphLeaf(AbstractSceneGraphNode parent) {
		parentAttr = new GenericAttr<SceneGraphNode>(parent);
		parentAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<SceneGraphNode>() {
			@Override
			public SceneGraphNode attributeWillChange(ScalarAttribute source, SceneGraphNode value) {
				AbstractSceneGraphNode parent = (AbstractSceneGraphNode) parentAttr.getValue();
				if (parent != null) {
					parent.childrenAttr.remove(AbstractSceneGraphLeaf.this);
				}
				return value;
			}
		});
		parentAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				AbstractSceneGraphNode parent = (AbstractSceneGraphNode) parentAttr.getValue();
				if (parent != null) {
					parent.childrenAttr.add(AbstractSceneGraphLeaf.this);
				}
			}
		});
	}
	
	public GenericAttr<SceneGraphNode> getParentAttribute() {
		return parentAttr;
	}
}
