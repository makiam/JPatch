/*
 * $Id: JPatchCompoundEdit.java,v 1.6 2005/09/19 12:40:15 sascha_l Exp $
 *
 * Copyright (c) 2004 Sascha Ledinsky
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
 * along with Foobar; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package jpatch.control.edit;

import java.util.*;

/**
 * A JPatchCompoundEdit allows to group edits.<br>
 * It's undo() method simply calls the undo() methods of all child
 * edits (in reverse order). It's redo method calls the redo() methods of all child edits.
 *
 * @version	$Revision: 1.6 $
 * @author	Sascha Ledinsky
 */
public abstract class JPatchCompoundEdit implements JPatchUndoableEdit {
	
	/** a list holding all child edits */
	protected List listEdits = new ArrayList(1);
	
	protected JPatchCompoundEdit() { }
	
	/**
	 * adds an edit to the list
	 * @param edit The edit to add
	 */
	protected void addEdit(JPatchUndoableEdit edit) {
		if (edit.isAtomic() || ((JPatchCompoundEdit) edit).isValid()) {
			listEdits.add(edit);
		}
	}
	
	/**
	 * undoes all child edits in reverse order
	 */
	public final void undo() {
		//System.out.println("undo " + strName);
		if (listEdits.size() == 0) {
			throw new IllegalStateException(this + " is empty!");
		}
		for (int e = listEdits.size() - 1; e >= 0; e--) {
			((JPatchUndoableEdit)listEdits.get(e)).undo();
		}
	}
	
	/**
	 * redoes all child edits
	 */
	public final void redo() {
		//System.out.println("redo " + strName);
		if (listEdits.size() == 0) {
			throw new IllegalStateException("CompoundEdit " + hashCode() + " is empty!");
		}
		for (int e = 0; e < listEdits.size(); e++) {
			((JPatchUndoableEdit)listEdits.get(e)).redo();
		}
	}
	
	/**
	 * returns the size of the edit
	 */
	public final int size() {
		return listEdits.size();
	}
	
	public final boolean isAtomic() {
		return false;
	}
	
	public final boolean isValid() {
		return (listEdits.size() > 0);
	}
	
	public final int sizeOf() {
		int size = 8 + 4 + (8 + 4 + 4 + 4);
		for (Iterator it = listEdits.iterator(); it.hasNext(); ) {
			size += (4 + ((JPatchUndoableEdit) it.next()).sizeOf());
		}
		return size;
	}
	
	public void debug(String prefix) {
		String name = this instanceof JPatchRootEdit ? getClass().getName() + " \"" + ((JPatchRootEdit) this).getName() + "\"" : getClass().getName();
		System.out.println(prefix + name + " " + sizeOf() + ":");
		for (Iterator it = listEdits.iterator(); it.hasNext(); ) {
			((JPatchUndoableEdit) it.next()).debug(prefix + "    ");
		}
	}
}
