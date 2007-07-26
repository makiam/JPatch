package com.jpatch.boundary;

import com.jpatch.afw.attributes.*;
import com.jpatch.entity.*;

import java.util.*;

import javax.swing.tree.*;

public class TreeManager {
	
	/**
	 * maps sceneGraph-nodes to tree-nodes
	 */
	private final Map<SceneGraphNode, SceneGraphTreeNode> treeNodeMap = new HashMap<SceneGraphNode, SceneGraphTreeNode>();
	
	/**
	 * The Swing TreeModel
	 */
	private final DefaultTreeModel treeModel;

	/**
	 * Used to detect concurrent modifications when iterating over treeNode children
	 */
	static final AttributePostChangeListener CONCURRENT_MODIFICATION_LISTENER = new AttributePostChangeListener() {
		public void attributeHasChanged(Attribute source) {
			throw new ConcurrentModificationException();
		}
	};
	
	/**
	 * Flag to prevent update loops
	 */
	private boolean ignoreModelChange = false;
	
	/**
	 * Creates a new TreeManager for the specified TreeModel
	 * @param treeModel the TreeModel to use
	 */
	public TreeManager(DefaultTreeModel treeModel) {
		this.treeModel = treeModel;
	}
	
	/**
	 * Creates a new MutableTreeNode for the specified SceneGraphLeaf/Node
	 * @param node the SceneGraphLeaf/Node
	 * @return a new MutableTreeNode for the specified SceneGraphLeaf/Node
	 */
	public MutableTreeNode createTreeNodeFor(final SceneGraphNode node) {
		/* check if node is already managed */
		if (treeNodeMap.containsKey(node)) {
			throw new IllegalStateException(node + " is already managed by " + this);
		}
		
		/* if the node has a parent, the parent must be managed too */
		if (node.getParentAttribute().getValue() != null && !treeNodeMap.containsKey(node.getParentAttribute().getValue())) {
			throw new IllegalStateException("parent of " + node + " is not managed by " + this);
		}
		
		/* create new SceneGraphTreeNode and add it to the treeMap */
		final SceneGraphTreeNode treeNode = new SceneGraphTreeNode(this, node);
		treeNodeMap.put(node, treeNode);
		
		/* if it's not a leaf, recursively call this method for all children */
		if (node instanceof SceneGraphNode) {
			for (SceneGraphNode child : ((SceneGraphNode) node).getChildrenAttribute().getElements()) {
				createTreeNodeFor(child);
			}
		}
		
//		/* if the node has a parent, add the new treeNode to the parent treeNode */
//		if (node.getParentAttribute().getValue() != null) {
//			MutableTreeNode parentTreeNode = treeNodeMap.get(node.getParentAttribute().getValue());
//			parentTreeNode.insert(treeNode, parentTreeNode.getChildCount());
//		} else {
//			/* else, add the new treeNode to the root treeNode */
//			MutableTreeNode rootTreeNode = (MutableTreeNode) treeModel.getRoot();
//			rootTreeNode.insert(treeNode, rootTreeNode.getChildCount());
//		}
		
		/* listen for parent-changes on the node to synchronize the tree-model */
		node.getParentAttribute().addAttributePostChangeListener(new AttributePostChangeListener() {
			public void attributeHasChanged(Attribute source) {
				sceneGraphNodeParentChanged(node);
			}
		});
		
		return treeNode;
	}
	
	/**
	 * Returns the MutableTreeNode for the specified SceneGraphLeaf/Node
	 * @param node the SceneGraphLeaf/Node to look for
	 * @return the MutableTreeNode for the specified SceneGraphLeaf/Node
	 */
	public MutableTreeNode getTreeNodeFor(SceneGraphNode node) {
		return treeNodeMap.get(node);
	}
	
	/**
	 * called when the parent attribute of the sceneGraphNode has changed,
	 * to update the TreeModel accordingly
	 * @param node the SceneGraphLeaf/Node whose parent has changed
	 */
	void sceneGraphNodeParentChanged(SceneGraphNode node) {
		if (!ignoreModelChange) {
			ignoreModelChange = true;
			SceneGraphTreeNode treeNode = (SceneGraphTreeNode) getTreeNodeFor(node);
			if (treeNode == null) {
				throw new IllegalArgumentException(node.toString());
			}
			SceneGraphNode newParent = node.getParentAttribute().getValue();
			if (treeNode.getParent() != null) {
				treeModel.removeNodeFromParent(treeNode);
			}
			MutableTreeNode parentTreeNode = treeNodeMap.get(newParent);
			if (parentTreeNode == null) {
				parentTreeNode = (MutableTreeNode) treeModel.getRoot();
			}
			treeModel.insertNodeInto(treeNode, parentTreeNode, parentTreeNode.getChildCount());
			ignoreModelChange = false;
		}
	}
	
	/**
	 * called when the parent of a TreeNode has changed to update the
	 * SceneGraphNode/Leaf accordingly.
	 * @param parent the new parent
	 * @param child the child
	 */
	void treeNodeInsert(SceneGraphNode parent, SceneGraphNode child) {
		if (!ignoreModelChange) {
			ignoreModelChange = true;
			child.getParentAttribute().setValue(parent);
			ignoreModelChange = false;
		}
	}
}
