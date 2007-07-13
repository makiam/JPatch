package com.jpatch.entity;

import com.jpatch.afw.attributes.*;

import java.util.*;

import javax.vecmath.Matrix4d;

/**
 * The base class for all TransformNodes (like regular TransformNodes and Bones)
 */
public class AbstractTransformNode extends AbstractSceneGraphNode {
	private final Transform transform = new Transform();
	
	public Transform getTransform() {
		return transform;
	}
	
}
