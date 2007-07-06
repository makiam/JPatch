package com.jpatch.afw.ui;

import com.jpatch.afw.attributes.NamedObject;

import java.util.*;

import javax.swing.event.*;
import javax.swing.tree.*;


@SuppressWarnings("serial")
public class JPatchTreeNode extends DefaultMutableTreeNode implements Comparable {
	private static final Map<Class, Integer> classOrder = new HashMap<Class, Integer>();
	
	private Iterable userChildren = new Iterable() {
		public Iterator iterator() {
			return new Iterator() {
				Enumeration e = children();
				public boolean hasNext() {
					if (e.hasMoreElements()) {
						return true;
					}
					e = null;
					return false;
				}
				public Object next() {
					return ((DefaultMutableTreeNode) e.nextElement()).getUserObject();
				}
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};
		}
	};
	
	/*
	 * initialize classOrder
	 */
	static {
//		classOrder.put(Model.class, 1);
//		classOrder.put(Project.class, 1);
//		classOrder.put(TransformNode.class, 99);
	}
	
	private JPatchTreeModel treeModel;
//	private StringAttr name;
	
	public JPatchTreeNode() { }
	
	public JPatchTreeNode(NamedObject namedObject) {
		setUserObject(namedObject);
//		name = (Attribute.String) jpatchObject.name;
//		name.addAttributeListener(this);
	}
	
	public String getName() {
		if (userObject == null) {
			return "ROOT";
		}
		return ((NamedObject) userObject).getNameAttribute().getValue();
	}
	
	@SuppressWarnings("unchecked")
	public void add(JPatchTreeNode node) {
		if (children == null)
			insert(node, 0);
		else {
			System.out.println(children);
			System.out.println(Collections.binarySearch(children, node));
			insert(node, -1 - Collections.binarySearch(children, node));
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
	
	
	@Override
	public void setParent(MutableTreeNode newParent) {
		super.setParent(newParent);
		JPatchObject userParent = newParent == null ? null : ((JPatchTreeNode) newParent).getUserObject();
		getUserObject().setParent(userParent); 
	}

//	@Override
//	public JPatchObject getUserObject() {
//		return (JPatchObject) userObject;
//	}
	
	public void setTreeModel(JPatchTreeModel treeModel) {
		System.out.println(hashCode() + " setTreeModel");
		this.treeModel = treeModel;
	}

	public void attributeChanged(Attribute attribute) {
		JPatchTreeNode parent = (JPatchTreeNode) getParent();
		removeFromParent();
		parent.add(this);
	}

	public int compareTo(Object o) {
		System.out.println(this + " compareTo " + o);
		JPatchTreeNode node = (JPatchTreeNode) o;
		int result = 0;
		if (userObject != null && node.userObject != null)
			result = classOrder.get(userObject.getClass()).compareTo(classOrder.get(node.userObject.getClass()));
		if (result == 0)
			result = getName().compareTo(node.getName());
		return result;
	}
	
	@Override
	public String toString() {
		return "JPatchTreenode@" + hashCode() + "(" + userObject + ")";
	}
	
	public void dump(String prefix) {
		System.out.println(prefix + this);
		prefix += "  ";
		if (children != null) {
			for (Object child : children) {
				((JPatchTreeNode) child).dump(prefix);
			}
		}
	}
	
	public Iterable getUserChildren() {
		return userChildren;
	}
	
	public Object getUserParent() {
		if (parent == null) {
			return null;
		}
		return ((DefaultMutableTreeNode) parent).getUserObject();
	}
}
