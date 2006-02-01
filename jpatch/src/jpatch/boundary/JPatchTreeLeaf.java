/*
 * $Id: JPatchTreeLeaf.java,v 1.4 2006/02/01 21:11:28 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package jpatch.boundary;

import java.util.*;

import javax.swing.tree.*;

/**
 * @author sascha
 *
 */
public class JPatchTreeLeaf implements MutableTreeNode {
	private static final Enumeration emptyEnumeration = new Enumeration() {
		public boolean hasMoreElements() {
			return false;
		}
		public Object nextElement() {
			return null;
		}
	};
	protected MutableTreeNode parent;
	protected String strName;
	
	public JPatchTreeLeaf() {
		super();
	}

	public JPatchTreeLeaf(String name) {
		this();
		strName = name;
	}
	
	public String getName() {
		return strName;
	}
	
	public void setName(String name) {
		strName = name;
	}
	
	public String toString() {
		return strName;
	}
	
	/*
	 * MutableTreeNode interface implementation
	 */
	public void insert(MutableTreeNode child, int index) {
		throw new UnsupportedOperationException("Can't insert child on tree-leave");
	}

	public void remove(int index) {
		throw new UnsupportedOperationException("Can't remove child from tree-leave");
	}

	public void remove(MutableTreeNode node) {
		throw new UnsupportedOperationException("Can't remove child from tree-leave");
	}

	public void setUserObject(Object object) {
		throw new UnsupportedOperationException();
	}

	public void removeFromParent() {
		parent.remove(this);
	}

	public void setParent(MutableTreeNode newParent) {
		parent = newParent;
	}

	public TreeNode getChildAt(int childIndex) {
		return null;
	}

	public int getChildCount() {
		return 0;
	}

	public TreeNode getParent() {
		return parent;
	}

	public int getIndex(TreeNode node) {
		throw new UnsupportedOperationException();
	}

	public boolean getAllowsChildren() {
		return false;
	}

	public boolean isLeaf() {
		return true;
	}

	public Enumeration children() {
		return emptyEnumeration;
	}
}
