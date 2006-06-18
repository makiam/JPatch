package jpatch.entity;

import java.util.*;
import javax.swing.tree.*;

public class TransformNodeTreeNode implements MutableTreeNode {
	private TransformNode transformNode;
	private boolean inserted;
	
	public TransformNodeTreeNode(TransformNode transformNode) {
		this.transformNode = transformNode;
	}

	public TransformNode getTransformNode() {
		return transformNode;
	}
	
	public void insert(MutableTreeNode child, int index) {
		if (!(child instanceof TransformNodeTreeNode))
			throw new IllegalArgumentException(child.toString());
		TransformNode childTransformNode = ((TransformNodeTreeNode) child).getTransformNode();
		transformNode.getChildren().add(index - 1, childTransformNode);
		child.setParent(this);
	}

	public void remove(int index) {
		transformNode.getChildren().remove(index - 1);
	}

	public void remove(MutableTreeNode node) {
		if (!(node instanceof TransformNodeTreeNode))
			throw new IllegalArgumentException(node.toString());
		TransformNode childTransformNode = ((TransformNodeTreeNode) node).getTransformNode();
		transformNode.getChildren().remove(childTransformNode);
		childTransformNode.setParent(null);
	}

	public void setUserObject(Object object) {
		throw new UnsupportedOperationException();
	}

	public void removeFromParent() {
		((MutableTreeNode) getParent()).remove(this);
	}

	public void setParent(MutableTreeNode newParent) {
		if (newParent == null) {
			inserted = false;
			return;
		}
		inserted = true;
		if (newParent == JPatchObject.ROOT_NODE) {
			transformNode.setParent(null);
			return;
		}
		if (!(newParent instanceof TransformNodeTreeNode))
			throw new IllegalArgumentException(newParent.toString());
		TransformNode parentTransformNode = ((TransformNodeTreeNode) newParent).getTransformNode();
		transformNode.setParent(parentTransformNode);
	}

	public TreeNode getChildAt(int childIndex) {
		if (childIndex == 0)
			return transformNode.getObject().getTreeNode();
		else
			return transformNode.getChildren().get(childIndex - 1).getTreeNode();
	}

	public int getChildCount() {
		return transformNode.getChildren().size() + 1;
	}

	public TreeNode getParent() {
		System.out.println("getParent() " + inserted);
		if (!inserted)
			return null;
		if (transformNode.getParent() == null)
			return JPatchObject.ROOT_NODE;
		return transformNode.getParent().getTreeNode();
	}

	public int getIndex(TreeNode node) {
		System.out.println(transformNode.getObject().getTreeNode().hashCode());
		if (transformNode.getObject().getTreeNode() == node)
			return 0;
		if (!(node instanceof TransformNodeTreeNode))
			throw new IllegalArgumentException(Integer.toString(node.hashCode()));
		TransformNode childTransformNode = ((TransformNodeTreeNode) node).getTransformNode();
		int index = transformNode.getChildren().indexOf(childTransformNode);
		if (index == -1)
			return -1;
		return index + 1;
	}

	public boolean getAllowsChildren() {
		return true;
	}

	public boolean isLeaf() {
		return false;
	}

	public Enumeration children() {
		return new Enumeration() {
			private int index = -1;
			public boolean hasMoreElements() {
				return index < transformNode.getChildren().size();
			}

			public Object nextElement() {
				if (index == -1)
					return transformNode.getObject().getTreeNode();
				else
					return transformNode.getChildren().get(index).getTreeNode();
			}
			
		};
	}

}
