package com.jpatch.entity;

import com.jpatch.afw.attributes.*;

import javax.vecmath.Matrix4d;

public interface SceneGraphNode extends SceneGraphLeaf {
	CollectionAttr<SceneGraphLeaf> getChildrenAttribute();
	Transform getTransform();
}
