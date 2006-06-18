package jpatch.entity;

import javax.swing.tree.*;

public interface JPatchObject {
	public static final MutableTreeNode ROOT_NODE = new DefaultMutableTreeNode("ROOT");
	MutableTreeNode getTreeNode();
}
