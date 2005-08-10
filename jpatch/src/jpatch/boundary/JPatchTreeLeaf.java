package jpatch.boundary;

import javax.swing.tree.*;
import java.util.*;

public class JPatchTreeLeaf
implements TreeNode {
	public static final int ROOT = 0;
	public static final int MODEL = 1;
	public static final int MATERIALS = 2;
	public static final int MORPHS = 3;
	public static final int BONES = 4;
	public static final int MORPHGROUP = 5;
	public static final int BONE = 6;
	public static final int RDOF = 7;
	public static final int TDOF = 8;
	public static final int MUSCLE = 9;
	public static final int MATERIAL = 10;
	public static final int MORPH = 11;
	public static final int SELECTIONS = 12;
	public static final int SELECTION = 13;
	
	/*
	private static final String[] astrName = new String[] {
		"Root",
		"Model",
		"Materials",
		"Expressions",
		"Bones",
		"Expression Group",
		"Bone",
		"rotation DOF",
		"translation DOF",
		"Muscle",
		"Material",
		"Expression"
	};
	*/
	
	protected int iNodeType;
	protected String strName;
	protected JPatchTreeNode treenodeParent;
	
	public JPatchTreeLeaf() {
	}
	
	public JPatchTreeLeaf(String name) {
		strName = name;
	}
	
	public JPatchTreeLeaf(int type, JPatchTreeNode parent) {
		iNodeType = type;
		setParent(parent);
	}
	
	public JPatchTreeLeaf(int type, JPatchTreeNode parent, String name) {
		iNodeType = type;
		setParent(parent);
		strName = name;
	}
	
	protected void setParent(JPatchTreeNode newParent) {
		if (treenodeParent != null) {
			treenodeParent.remove(this);
		}
		if (newParent != null) {
			newParent.add(this);
		} else {
			treenodeParent = null;
		}
	}
	
	public Enumeration children() {
		return null;
	}
	
	public boolean getAllowsChildren() {
		return false;
	}
	
	public TreeNode getChildAt(int childIndex) {
		return null;
	}
	
	public int getChildCount() {
		return 0;
	}
	
	public int getIndex(TreeNode node) {
		return -1;
	}
	
	public TreeNode getParent() {
		return treenodeParent;
	}
	
	public boolean isLeaf() {
		return true;
	}
	
	public int getNodeType() {
		return iNodeType;
	}
	
	public String toString() {
		return strName;
	}
	
	public void setName(String name) {
		strName = name;
	}
	
	public String getName() {
		return strName;
	}
	
	protected int countPath(int n) {
		if (treenodeParent != null) {
			return treenodeParent.countPath(++n);
		}
		return ++n;
	}
	
	public int getPathCount() {
		return countPath(0);
	}
	
	protected void walkPath(Object[] path, int n) {
		path[path.length - n - 1] = this;
		if (treenodeParent != null) {
			treenodeParent.walkPath(path, ++n);
		} 
	}
	
	public Object[] getPath() {
		Object[] path = new Object[getPathCount()];
		walkPath(path,0);
		return path;
	}
	
	public TreePath getTreePath() {
		return new TreePath(getPath());
	}
}
