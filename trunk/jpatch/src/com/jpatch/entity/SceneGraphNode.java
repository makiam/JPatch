package com.jpatch.entity;

import com.jpatch.afw.attributes.CollectionAttr;

import javax.vecmath.Matrix4d;

public interface SceneGraphNode extends SceneGraphLeaf {
	CollectionAttr<SceneGraphLeaf> getChildrenAttribute();
	Matrix4d getMatrix(Matrix4d matrix);
}
