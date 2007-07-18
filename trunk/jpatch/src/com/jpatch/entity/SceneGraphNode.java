package com.jpatch.entity;

import java.util.Collection;
import java.util.HashSet;

import com.jpatch.afw.attributes.*;

import javax.vecmath.Matrix4d;

public abstract class SceneGraphNode extends SceneGraphLeaf {
	final CollectionAttr<SceneGraphLeaf> childrenAttr = new CollectionAttr<SceneGraphLeaf>(HashSet.class);
	private final UnmodifiableCollectionAttr<SceneGraphLeaf> childrenAttrView = new UnmodifiableCollectionAttr<SceneGraphLeaf>(childrenAttr);
	
	public UnmodifiableCollectionAttr<SceneGraphLeaf> getChildrenAttribute() {
		return childrenAttrView;
	}
	
	public abstract Transform getTransform();
}
