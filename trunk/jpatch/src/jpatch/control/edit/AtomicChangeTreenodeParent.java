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
package jpatch.control.edit;

import jpatch.boundary.tree.*;

/**
 * @author sascha
 *
 */
public class AtomicChangeTreenodeParent extends JPatchAtomicEdit implements JPatchRootEdit {
	private JPatchTreeNode[] children;
	private JPatchTreeNode parent;
	
	public AtomicChangeTreenodeParent(JPatchTreeNode children[], JPatchTreeNode newParent) {
		this.children = children;
		this.parent = newParent;
		swap();
	}

	public String getName() {
		return "Change tree hierarchy";
	}
	
	public void redo() {
		swap();
	}

	public int sizeOf() {
		// TODO Auto-generated method stub
		return 8 + 8 + children.length * 4 + 4;
	}

	public void undo() {
		swap();
	}
	
	private void swap() {
		JPatchTreeNode tmp = (JPatchTreeNode) children[0].getParent();
		for (JPatchTreeNode child : children) {
			parent.add(child);
		}
		parent = tmp;
	}
}
