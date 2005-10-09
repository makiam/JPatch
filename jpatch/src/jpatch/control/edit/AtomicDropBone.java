/*
 * $Id: AtomicDropBone.java,v 1.1 2005/10/09 07:43:06 sascha_l Exp $
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
public class AtomicDropBone extends JPatchAtomicEdit {

	private Bone bone;
	
	public AtomicDropBone(Bone bone) {
		this.bone = bone;
		redo();
	}
	
	public void undo() {
		MainFrame.getInstance().getModel().addBone(bone);
	}

	public void redo() {
		MainFrame.getInstance().getModel().removeBone(bone);
	}

	public int sizeOf() {
		return 8 + 4;
	}

}
