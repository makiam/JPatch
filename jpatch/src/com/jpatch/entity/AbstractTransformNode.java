package com.jpatch.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * The base class for all TransformNodes (like regular TransformNodes and Bones)
 */
public abstract class AbstractTransformNode extends Transform {
	/**
	 * A list of child abstractTransformNodes (may be empty)
	 */
	protected List<AbstractTransformNode> children = new ArrayList<AbstractTransformNode>(0);
	
	/**
	 * Recursively computes the transformation matrix of this TransformNode and all of its children (and their children, etc.)
	 * This implementation first calls computeMatrix() and computeTransformedValues() on this object and finally calles
	 * computeBranch() on each child.
	 */
	@Override
	public void computeBranch() {
		super.computeBranch();
		for (AbstractTransformNode child : children) {
			child.computeBranch();
		}
	}
}
