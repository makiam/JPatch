package com.jpatch.afw;

import java.util.*;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.vecmath.Matrix4d;

public class Utils {
	/**
	 * Returns an Enumeration that iterates over all elements of the specified Iterator
	 * @param it the Iterator that backs this Enumeration
	 * @return an Enumeration that iterates over all elements of the specified Iterator
	 */
	public static Enumeration createEnumeration(final Iterator it) {
		return new Enumeration() {
			public boolean hasMoreElements() {
				return it.hasNext();
			}
			public Object nextElement() {
				return it.next();
			}
		};
	}
	
	/**
	 * Returns an Iterator that iterates over all elements of the specified Enumeration.
	 * The Iterator's <i>remove()</i> method is not supported, calling it will throw an
	 * <i>UnsupportedOperationException</i>.
	 * @param e the Enumeration that backs this Iterator
	 * @return an Iterator that iterates over all elements of the specified Enumeration
	 */
	public static Iterator createIterator(final Enumeration e) {
		return new Iterator() {
			public boolean hasNext() {
				return e.hasMoreElements();
			}
			public Object next() {
				return e.nextElement();
			}
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	public static void trace(Object ... args) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		System.err.println(stackTrace[3]);
		if (args.length > 0) {
			System.err.print("    args: (");
			for (int i = 0; i < args.length; i++) {
				System.err.print(args[i].toString());
				if (i < args.length - 1) {
					System.err.print(", ");
				} else {
					System.err.println(")");
				}
			}
		}
		int min = 4;
		for (int i = 4; i < stackTrace.length; i++) {
			if (stackTrace[i].getClassName().startsWith("com.jpatch")) {
				min = i;
				break;
			}
		}
		min = 8;
		
		for (int i = 3; i < min; i++) {
			System.err.println("        called by --> " + stackTrace[i]);
		}
	}
	
	/**
	 * Returns the distance from the root node to the specified node.
	 * @param node
	 * @return the distance from the root node to the specified node.
	 * If the specified node is the root node, 0 is returned.
	 */
	public static int getTreeNodeLevel(TreeNode node) {
		int level = 0;
		while (node != null) {
			node = node.getParent();
			level++;
		}
		return level - 1;
	}
	
	public static Matrix4d createIdentityMatrix() {
		Matrix4d matrix = new Matrix4d();
		matrix.setIdentity();
		return matrix;
	}
	
	/**
	 * Returns the TreePath from the root node to the specified node.
	 * @param node
	 * @return the TreePath from the root node to the specified node.
	 */
	public static TreePath createTreePath(TreeNode node) {
		int n = getTreeNodeLevel(node) + 1;
		Object[] path = new Object[n];
		for (int i = n - 1; i >= 0; i--) {
			path[i] = node;
			node = node.getParent();
		}
		return new TreePath(path);
	}
	
	public static JComponent getValidateRoot(JComponent component) {
//		trace(component);
		return recurseValidateRoot(component);
	}
	
	private static JComponent recurseValidateRoot(JComponent component) {
		if (component == null) {
			return null;
		} else if (component.isValidateRoot()) {
			return component;
		} else {
			return recurseValidateRoot((JComponent) component.getParent());
		}
	}
	
	public static final ListSelectionModel NULL_SELECTION_MODEL = new DefaultListSelectionModel() {

		@Override
		public int getAnchorSelectionIndex() {
			return -1;
		}

		@Override
		public int getLeadSelectionIndex() {
			return -1;
		}

		@Override
		public int getMaxSelectionIndex() {
			return -1;
		}

		@Override
		public int getMinSelectionIndex() {
			return -1;
		}

		@Override
		public boolean isSelectedIndex(int index) {
			return false;
		}

		@Override
		public boolean isSelectionEmpty() {
			return true;
		}
		
	};
}
