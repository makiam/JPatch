package com.jpatch.entity;

import com.jpatch.afw.attributes.GenericAttr;

public interface SceneGraphLeaf {
	GenericAttr<SceneGraphNode> getParentAttribute();
}
