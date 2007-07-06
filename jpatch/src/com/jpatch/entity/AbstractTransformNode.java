package com.jpatch.entity;

import com.jpatch.afw.attributes.*;

import java.util.*;

/**
 * The base class for all TransformNodes (like regular TransformNodes and Bones)
 */
public abstract class AbstractTransformNode extends Transform implements SceneGraphLeaf, SceneGraphNode {
	/**
	 * An attribute storing the parent node in the scene graph hierarchy
	 */
	protected final GenericAttr<SceneGraphNode> parentAttr = new GenericAttr<SceneGraphNode>();
	
	/**
	 * A list of child SceneGraphLeafs (may be empty)
	 */
	protected final CollectionAttr<SceneGraphLeaf> childrenAttr = new CollectionAttr<SceneGraphLeaf>(HashSet.class);
	private final Set<SceneGraphLeaf> oldChildren = new HashSet<SceneGraphLeaf>();
	
	public AbstractTransformNode() {
		parentAttr.addAttributePreChangeListener(new AttributePreChangeAdapter<SceneGraphNode>() {
			@Override
			public SceneGraphNode attributeWillChange(ScalarAttribute source, SceneGraphNode value) {
				SceneGraphNode parent = parentAttr.getValue();
				if (parent != null) {
					parent.getChildrenAttribute().remove(AbstractTransformNode.this);
				}
				return value;
			}
		});
		parentAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				SceneGraphNode parent = parentAttr.getValue();
				if (parent != null) {
					parent.getChildrenAttribute().add(AbstractTransformNode.this);
				}
			}
		});
		childrenAttr.addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				Collection<SceneGraphLeaf> children = childrenAttr.getElements();
				for (SceneGraphLeaf child : children) {
					if (!oldChildren.contains(child)) {
						child.getParentAttribute().setValue(AbstractTransformNode.this);
					}
				}
				for (SceneGraphLeaf oldChild : oldChildren) {
					if (!children.contains(oldChild)) {
						oldChild.getParentAttribute().setValue(null);
					}
				}
				oldChildren.clear();
				oldChildren.addAll(childrenAttr.getElements());
			}
		});
	}
	
	/**
	 * Recursively computes the transformation matrix of this TransformNode and all of its children (and their children, etc.)
	 * This implementation first calls computeMatrix() and computeTransformedValues() on this object and finally calles
	 * computeBranch() on each child.
	 */
	@Override
	public void computeBranch() {
		super.computeBranch();
		for (SceneGraphLeaf child : childrenAttr.getElements()) {
			if (child instanceof Transform) {
				((Transform) child).computeBranch();
			}
		}
	}

	public GenericAttr<SceneGraphNode> getParentAttribute() {
		return parentAttr;
	}

	public CollectionAttr<SceneGraphLeaf> getChildrenAttribute() {
		return childrenAttr;
	}
}
