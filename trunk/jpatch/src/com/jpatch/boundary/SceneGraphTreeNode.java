package com.jpatch.boundary;

import com.jpatch.entity.SceneGraphNode;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * MutableTreeNode implementation, used to manage SceneGraphNodes/Leafs
 * @author sascha
 */
public class SceneGraphTreeNode implements MutableTreeNode {
	
	private final TreeManager treeManager;
	private MutableTreeNode parent;
	private final SceneGraphNode sceneGraphNode;
	private final List<SceneGraphTreeNode> leafs = new ArrayList<SceneGraphTreeNode>();
	
	public SceneGraphTreeNode(TreeManager manager, SceneGraphNode sceneGraphNode) {
		treeManager = manager;
		this.sceneGraphNode = sceneGraphNode;
	}
	
	
	/**
	 * Returns the SceneGraphNode managed by this SceneGraphTreeNode
	 * @return the SceneGraphNode managed by this SceneGraphTreeNode
	 */
	public SceneGraphNode getSceneGraphNode() {
		return sceneGraphNode;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.MutableTreeNode#insert(javax.swing.tree.MutableTreeNode, int)
	 */
	public void insert(MutableTreeNode newChild, int childIndex) {
		if (sceneGraphNode == null) {
			throw new IllegalStateException("can't add child to leaf " + this);
		}
		MutableTreeNode oldParent = (MutableTreeNode) newChild.getParent();
		if (oldParent != null) {
			oldParent.remove(newChild);
	    }
		newChild.setParent(this);
		leafs.add(childIndex, (SceneGraphTreeNode) newChild);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.MutableTreeNode#remove(int)
	 */
	public void remove(int index) {
		remove(leafs.get(index));
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.MutableTreeNode#remove(javax.swing.tree.MutableTreeNode)
	 */
	public void remove(MutableTreeNode node) {
		node.setParent(null);
		leafs.remove(node);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.MutableTreeNode#removeFromParent()
	 */
	public void removeFromParent() {
		parent.remove(this);	// will call setParent(null) on this node
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.MutableTreeNode#setParent(javax.swing.tree.MutableTreeNode)
	 */
	public void setParent(MutableTreeNode newParent) {
		parent = newParent;
		if (parent instanceof SceneGraphTreeNode) {
			treeManager.treeNodeInsert(((SceneGraphTreeNode) parent).sceneGraphNode, sceneGraphNode);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.MutableTreeNode#setUserObject(java.lang.Object)
	 */
	public void setUserObject(Object object) {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#children()
	 */
	public Enumeration children() {
		return new Enumeration() {
			int index = 0;
			
			public boolean hasMoreElements() {
				boolean hasNext = index < leafs.size();
				if (index == 0 && hasNext) {
					sceneGraphNode.getChildrenAttribute().addAttributePostChangeListener(TreeManager.CONCURRENT_MODIFICATION_LISTENER);
				} else if (!hasNext) {
					sceneGraphNode.getChildrenAttribute().removeAttributePostChangeListener(TreeManager.CONCURRENT_MODIFICATION_LISTENER);
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

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getAllowsChildren()
	 */
	public boolean getAllowsChildren() {
		return sceneGraphNode != null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getChildAt(int)
	 */
	public TreeNode getChildAt(int childIndex) {
		return leafs.get(childIndex);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getChildCount()
	 */
	public int getChildCount() {
		return leafs.size();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getIndex(javax.swing.tree.TreeNode)
	 */
	public int getIndex(TreeNode node) {
		return leafs.indexOf(node);
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#getParent()
	 */
	public TreeNode getParent() {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.swing.tree.TreeNode#isLeaf()
	 */
	public boolean isLeaf() {
		return leafs.size() == 0;
	}
}