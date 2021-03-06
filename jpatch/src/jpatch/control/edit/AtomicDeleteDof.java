/*
 * $Id$
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

import jpatch.boundary.*;
import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class AtomicDeleteDof extends JPatchAtomicEdit implements JPatchRootEdit {
//	private Bone bone;
	private RotationDof dof;
	
	public AtomicDeleteDof(RotationDof dof) {
//		bone = dof.getBone();
		this.dof = dof;
		redo();
	}
	
	public void undo() {
		MainFrame.getInstance().getTreeModel().insertNodeInto(dof, dof.getBone(), dof.getBone().getChildCount());
		dof.getBone().addDofAxis(dof.getType());
	}
	
	public void redo() {
		dof.getBone().removeDofAxis(dof.getType());
		MainFrame.getInstance().getTreeModel().removeNodeFromParent(dof);
	}

	
	public int sizeOf() {
		return 8 + 4;
	}

	public String getName() {
		return "delete degree-of-freedom";
	}
}
