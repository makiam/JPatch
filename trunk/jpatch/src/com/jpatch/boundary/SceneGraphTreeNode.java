package com.jpatch.boundary;

import com.jpatch.afw.Utils;
import com.jpatch.afw.attributes.*;
import com.jpatch.entity.SceneGraphLeaf;
import com.jpatch.entity.SceneGraphNode;

import java.util.*;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

public class SceneGraphTreeNode implements MutableTreeNode {
	private static final AttributePostChangeListener CONCURRENT_MODIFICATION_LISTENER = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			throw new ConcurrentModificationException();
		}
	};
	private MutableTreeNode parent;
	private final SceneGraphLeaf sceneGraphLeaf;
	private final SceneGraphNode sceneGraphNode;
	private final List<SceneGraphTreeNode> leafs = new ArrayList<SceneGraphTreeNode>();
	
	public SceneGraphTreeNode(SceneGraphLeaf sceneGraphLeaf) {
		this.sceneGraphLeaf = sceneGraphLeaf;
		if (sceneGraphLeaf instanceof SceneGraphNode) {
			sceneGraphNode = (SceneGraphNode) sceneGraphLeaf;
//			for (SceneGraphLeaf leaf : sceneGraphNode.getChildrenAttribute().getElements()) {
//				leafs.add(new SceneGraphTreeNode(leaf));
//			}
		} else {
			sceneGraphNode = null;
		}
	}
	
	public void insert(MutableTreeNode child, int index) {
		Utils.trace(child, index);
		if (sceneGraphNode == null) {
			throw new IllegalStateException("can't add child to leaf " + this);
		}
		child.setParent(this);
		SceneGraphLeaf userChild = ((SceneGraphTreeNode) child).sceneGraphLeaf;
		userChild.getParentAttribute().setValue(sceneGraphNode);
		leafs.add(index, (SceneGraphTreeNode) child);
	}

	public void remove(int index) {
		Utils.trace(index);
		remove(leafs.get(index));
	}

	public void remove(MutableTreeNode node) {
		Utils.trace(node);
		node.setParent(null);
		SceneGraphLeaf userChild = ((SceneGraphTreeNode) node).sceneGraphLeaf;
		userChild.getParentAttribute().setValue(null);
		leafs.remove(node);
	}

	public void removeFromParent() {
		Utils.trace();
		parent = null;
	}

	public void setParent(MutableTreeNode newParent) {
		Utils.trace(newParent);
		parent = newParent;
	}

	public void setUserObject(Object object) {
		throw new UnsupportedOperationException();
	}

	public Enumeration children() {
		Utils.trace();
		return new Enumeration() {
			int index = 0;
			
			public boolean hasMoreElements() {
				boolean hasNext = index < leafs.size();
				if (index == 0 && hasNext) {
					sceneGraphNode.getChildrenAttribute().addAttributePostChangeListener(CONCURRENT_MODIFICATION_LISTENER);
				} else if (!hasNext) {
					sceneGraphNode.getChildrenAttribute().removeAttributePostChangeListener(CONCURRENT_MODIFICATION_LISTENER);
				}
				return hasNext;
			}

			public Object nextElement() {
				try {
					return leafs.get(index++);
				} catch (IndexOutOfBoundsException e) {
					throw new NoSuchElementException(e.getMessage());
				}
			}
		};
	}

	public boolean getAllowsChildren() {
		Utils.trace();
		return sceneGraphNode != null;
	}

	public TreeNode getChildAt(int childIndex) {
		Utils.trace(childIndex);
		return leafs.get(childIndex);
	}

	public int getChildCount() {
		Utils.trace();
		return leafs.size();
	}

	public int getIndex(TreeNode node) {
		Utils.trace(node);
		return leafs.indexOf(node);
	}

	public TreeNode getParent() {
		Utils.trace();
		Utils.trace();
		return parent;
	}

	public boolean isLeaf() {
		Utils.trace();
		return leafs.size() == 0;
	}

}
