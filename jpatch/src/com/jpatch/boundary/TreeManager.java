package com.jpatch.boundary;

import com.jpatch.afw.attributes.Attribute;
import com.jpatch.afw.attributes.AttributePostChangeListener;
import com.jpatch.entity.*;

import java.util.*;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

public class TreeManager {
	private final Map<SceneGraphLeaf, SceneGraphTreeNode> treeNodeMap = new HashMap<SceneGraphLeaf, SceneGraphTreeNode>();
	private final DefaultTreeModel treeModel;
	
	public TreeManager(DefaultTreeModel treeModel) {
		this.treeModel = treeModel;
	}
	
	public SceneGraphTreeNode createTreeNodeFor(final SceneGraphLeaf node) {
		/* check if node is already managed */
		if (treeNodeMap.containsKey(node)) {
			throw new IllegalStateException(node + " is already managed by " + this);
		}
		
		/* if the node has a parent, the parent must be managed too */
		if (node.getParentAttribute().getValue() != null && !treeNodeMap.containsKey(node.getParentAttribute().getValue())) {
			throw new IllegalStateException("parent of " + node + " is not managed by " + this);
		}
		
		/* create new SceneGraphTreeNode and add it to the treeMap */
		final SceneGraphTreeNode treeNode = new SceneGraphTreeNode(node);
		treeNodeMap.put(node, treeNode);
		
		/* if it's not a leaf, recursively call this method for all children */
		if (node instanceof SceneGraphNode) {
			for (SceneGraphLeaf child : ((SceneGraphNode) node).getChildrenAttribute().getElements()) {
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
			boolean ignore = false;
			public void attributeHasChanged(Attribute source) {
				if (!ignore) {
					ignore = true;
					if (treeNode.getParent() != null) {
						System.out.println("removing " + treeNode + " from " + treeNode.getParent());
//						Thread.dumpStack();
						treeModel.removeNodeFromParent(treeNode);
					}
					System.out.println("new parent node = " + node.getParentAttribute().getValue());
					MutableTreeNode parentTreeNode = treeNodeMap.get(node.getParentAttribute().getValue());
					if (parentTreeNode == null) {
						parentTreeNode = (MutableTreeNode) treeModel.getRoot();
					}
					System.out.println("new parent treenode = " + parentTreeNode);
					parentTreeNode.insert(treeNode, parentTreeNode.getChildCount());
					ignore = false;
				}
			}
		});
		
		return treeNode;
	}
	
	public SceneGraphTreeNode getTreeNodeFor(SceneGraphLeaf node) {
		return treeNodeMap.get(node);
	}
}
