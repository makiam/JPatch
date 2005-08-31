/*
 * $Id: JPatchCompoundEdit.java,v 1.2 2005/08/31 16:04:43 sascha_l Exp $
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
 * @version	$Revision: 1.2 $
 * @author	Sascha Ledinsky
 */
public class JPatchCompoundEdit extends JPatchAbstractUndoableEdit {

	/** a list holding all child edits */
	protected List lstEdits = new ArrayList();
	
	/** name of the edit */
	protected String strName;
	
	/** Constructor */
	public JPatchCompoundEdit() {
		strName = "";
	}
	
	/**
	 * Constructor
	 * @param name The name of the edit
	 */
	public JPatchCompoundEdit(String name) {
		strName = name;
	}
	
	/**
	 * adds an edit to the list
	 * @param edit The edit to add
	 */
	public void addEdit(JPatchUndoableEdit edit) {
		if (edit.isAtomic() || ((JPatchCompoundEdit) edit).isValid()) {
			lstEdits.add(edit);
		}
	}
	
	/**
	 * undoes all child edits in reverse order
	 */
	public void undo() {
		//System.out.println("undo " + strName);
		if (lstEdits.size() == 0) {
			throw new IllegalStateException("CompoundEdit " + hashCode() + "\"" + strName + "\" is empty!");
		}
		for (int e = lstEdits.size() - 1; e >= 0; e--) {
			((JPatchUndoableEdit)lstEdits.get(e)).undo();
		}
	}
	
	/**
	 * redoes all child edits
	 */
	public void redo() {
		//System.out.println("redo " + strName);
		if (lstEdits.size() == 0) {
			throw new IllegalStateException("CompoundEdit " + hashCode() + " is empty!");
		}
		for (int e = 0; e < lstEdits.size(); e++) {
			((JPatchUndoableEdit)lstEdits.get(e)).redo();
		}
	}
	
	/**
	 * returns the name of this edit
	 * @return the name of this edit
	 */
	public String name() {
		return strName;
	}
	
	/**
	 * returns the size of the edit
	 */
	public int size() {
		return lstEdits.size();
	}
	
	public boolean isAtomic() {
		return false;
	}
	
	public boolean isValid() {
		return (lstEdits.size() > 0);
	}
	
	public void dump(String prefix) {
		System.out.println(prefix + getClass().getName() + " \"" + name() + "\":");
		for (Iterator it = lstEdits.iterator(); it.hasNext(); ) {
			((JPatchAbstractUndoableEdit) it.next()).dump(prefix + "    ");
		}
	}
}
