/*
 * $Id:$
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
package jpatch.boundary.tree;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

/**
 * @author sascha
 *
 */
public class JPatchTreeModel extends DefaultTreeModel {

	public JPatchTreeModel() {
		super(new JPatchTreeNode(), false);
		((JPatchTreeNode) getRoot()).setTreeModel(this);
	}

	
	@Override
	public void setRoot(TreeNode root) {
		super.setRoot(root);
		if (root != null)
			((JPatchTreeNode) root).setTreeModel(this);
	}


	@Override
	public void nodesWereInserted(TreeNode node, int[] childIndices) {
		super.nodesWereInserted(node, childIndices);
		for (int childIndex : childIndices)
			((JPatchTreeNode) node.getChildAt(childIndex)).setTreeModel(this);
	}


	@Override
	public void nodesWereRemoved(TreeNode node, int[] childIndices, Object[] removedChildren) {
		super.nodesWereRemoved(node, childIndices, removedChildren);
//		for (Object object : removedChildren)
//			System.out.println("removedChild: " + object);
		for (Object child : removedChildren)
			((JPatchTreeNode) child).setTreeModel(null);
	}
	
	
}
