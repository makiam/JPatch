package jpatch.boundary;

import javax.swing.tree.*;
import java.util.*;

public class JPatchTreeNode extends JPatchTreeLeaf {
	protected ArrayList listChildren = new ArrayList();
	
	public JPatchTreeNode() {
	}
	
	public JPatchTreeNode(String name) {
		super(name);
	}
	
	public JPatchTreeNode(int type, JPatchTreeNode parent) {
		super(type,parent);
	}
	
	public JPatchTreeNode(int type, JPatchTreeNode parent,String name) {
		super(type,parent,name);
	}
	
	public void add(JPatchTreeLeaf newChild) {
		listChildren.add(newChild);
		newChild.treenodeParent = this;
	}

	public void add(int index, JPatchTreeLeaf newChild) {
		listChildren.add(index, newChild);
		newChild.treenodeParent = this;
	}
	
	public void remove(TreeNode child) {
		listChildren.remove(child);
	}
	
	public Enumeration children() {
		return Collections.enumeration(listChildren);
	}
	
	public boolean getAllowsChildren() {
		return true;
	}
	
	public TreeNode getChildAt(int childIndex) {
		return (TreeNode)listChildren.get(childIndex);
	}
	
	public int getChildCount() {
		return listChildren.size();
	}
	
	public int getIndex(TreeNode node) {
		return listChildren.indexOf(node);
	}
	
	public TreeNode getParent() {
		return treenodeParent;
	}
	
	public boolean isLeaf() {
		return (listChildren.size() == 0);
	}
}
