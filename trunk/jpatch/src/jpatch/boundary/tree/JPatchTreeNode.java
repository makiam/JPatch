package jpatch.boundary.tree;

import java.util.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import jpatch.entity.*;

@SuppressWarnings("serial")
public class JPatchTreeNode extends DefaultMutableTreeNode implements ChangeListener {
	private static Comparator<JPatchTreeNode> nodeNameComparator = new Comparator<JPatchTreeNode>() {
		public int compare(JPatchTreeNode node1, JPatchTreeNode node2) {
			return node1.getName().compareTo(node2.getName());
		}
	};
	
	private JPatchTreeModel treeModel;
	
	private Attribute<String> name;
	
	public JPatchTreeNode() { }
	
	@SuppressWarnings("unchecked")
	public JPatchTreeNode(JPatchObject jpatchObject) {
		setUserObject(jpatchObject);
		name = jpatchObject.getAttribute("Name");
		name.addChangeListener(this);
	}

	public String getName() {
		if (name == null)
			return "ROOT";
		return name.getValue();
	}
	
	@SuppressWarnings("unchecked")
	public void add(JPatchTreeNode node) {
		if (children == null)
			insert(node, 0);
		else {
			System.out.println(children);
			System.out.println(Collections.binarySearch(children, node, nodeNameComparator));
			insert(node, -1 - Collections.binarySearch(children, node, nodeNameComparator));
		}
	}
	
	@Override
	public void insert(MutableTreeNode newChild, int childIndex) {
		super.insert(newChild, childIndex);
		treeModel.nodesWereInserted(this, new int[] { childIndex });
	}
	
	@Override
	public void remove(int childIndex) {
		Object[] removedChildren = new Object[] { getChildAt(childIndex) };
		super.remove(childIndex);
		treeModel.nodesWereRemoved(this, new int[] { childIndex }, removedChildren);
	}
	
	public void stateChanged(ChangeEvent e) {
		JPatchTreeNode parent = (JPatchTreeNode) getParent();
		removeFromParent();
		parent.add(this);
	}
	
	public void setTreeModel(JPatchTreeModel treeModel) {
		System.out.println(hashCode() + " setTreeModel");
		this.treeModel = treeModel;
	}
}
