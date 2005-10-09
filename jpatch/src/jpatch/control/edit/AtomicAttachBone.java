/*
 * $Id: AtomicAttachBone.java,v 1.1 2005/10/09 07:43:06 sascha_l Exp $
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

import javax.vecmath.*;
import jpatch.entity.*;

/**
 * @author sascha
 *
 */
public class AtomicAttachBone extends JPatchAtomicEdit implements JPatchRootEdit {
	private Bone boneChild;
	private Bone boneParent;
	
	public AtomicAttachBone(Bone child, Bone parent) {
		boneChild = child;
		boneParent = parent;
		redo();
	}
	
	public void undo() {
		boneChild.detach();
	}
	
	public void redo() {
		boneChild.attachTo(boneParent);
	}

	public int sizeOf() {
		return 8 + 4 + 4;
	}

	public String getName() {
		return "attach bone";
	}
}
