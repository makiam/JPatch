/*
 * $Id: JPatchUndoManager.java,v 1.3 2005/09/04 18:30:31 sascha_l Exp $
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
import java.io.*;

/**
 * The JPatchUndoManager stores JPatchUndoableEdits in a list and provides methods to add, redo and undo edits.<br>
 * It keeps track of the position in the position inside the list
 *
 * @version	$Revision: 1.3 $
 * @author	Sascha Ledinsky
 */
public class JPatchUndoManager {
	/** list holding all edits */
	private List lstEdits = new ArrayList();
	/** depht of undo-list */
	private int iDepth;
	/** pointer to the current element in the undo-list */
	private int iPos = 0;
	/** enable flag */
	private boolean bEnabled = true;
	
	private boolean bChange = false;
	
	private boolean bOpen = false;
	
	private int iStop = 0;
	
	/**
	 * Constructor
	 * @param depth The depth of the undo buffer
	 */
	public JPatchUndoManager(int depth) {
		iDepth = depth;
	}
	
	public void clear() {
		lstEdits.clear();
		iPos = 0;
	}
	
	public void setStop() {
		iStop = iPos;
	}
	
	public void clearStop() {
		iStop = 0;
	}
	
	public void rewind() {
		iPos = iStop;
		iStop = 0;
	}
	
	public void addEdit(JPatchUndoableEdit edit) {
//		if (edit instanceof ChangeToolEdit)
//			addEdit(edit, true);
//		else
			addEdit(edit, false);
	}
			
	/**
	 * adds a new edit to the list. <br>
	 * If the buffer size is exceeded, the first entry in the list will be dropped.
	 * If we're not on the end of the list (due to one or more call of undo) all edits
	 * after the last not undone edit will be dropped.
	 * @param edit The edit to add
	 */
	public void addEdit(JPatchUndoableEdit edit, boolean open) {
		if (bOpen) {
			addToCurrentEdit(edit, open);
			return;
		}
		bOpen = open;
		//System.out.println("UndoManager.addEdit(" + edit + ")");
		if (bEnabled) {
			if (iPos == lstEdits.size()) {			// check if we are at the end of the list
				lstEdits.add(edit);
				if (lstEdits.size() > iDepth) {
					lstEdits.remove(0);		// remove first item if list is full
					if (iStop > 0) iStop--;		// move back stop marker
				} else {
					iPos++;				// increase pointer
				}
			} else {
				while (iPos < lstEdits.size()) {	// remove all edits after current one
					lstEdits.remove(iPos);
				}
				lstEdits.add(edit);			// add new edit
				iPos++;					// increase pointer
			}
			bChange = true;
		}
	}

	private void addToCurrentEdit(JPatchUndoableEdit edit, boolean open) {
		bOpen = open;
		JPatchCompoundEdit compoundEdit = (JPatchCompoundEdit) lstEdits.get(iPos - 1);
		compoundEdit.addEdit(edit);
	}
	
	public void setChange(boolean change) {
		bChange = change;
	}
	
	public boolean hasChanged() {
		return bChange;
	}
	
	public void setEnabled(boolean enable) {
		bEnabled = enable;
	}
	
	/**
	 * undoes an edit
	 */
	public void undo() {
		if (iPos > iStop) {
			((JPatchUndoableEdit)lstEdits.get(--iPos)).undo();
		}
	}
	
	/**
	 * redoes an edit
	 */
	public void redo() {
		if (iPos < lstEdits.size()) {
			((JPatchUndoableEdit)lstEdits.get(iPos++)).redo();
		}
	}
	
	/**
	 * checks if it is possible to undo
	 * @return true if it is possible to undo, false otherwise
	 */
	public boolean canUndo() {
		return (iPos > iStop);
	}
	
	/**
	 * checks if it is possible to redo
	 * @return true if it is possible to redo, false otherwise
	 */
	public boolean canRedo() {
		return (iPos < lstEdits.size());
	}
	
	/**
	 * Returns the name of the next edit which can be undone
	 * @return name of the next edit which can be undone or "" othewise
	 */
	public String undoName() {
		if (iPos > iStop) {
			return ((JPatchUndoableEdit)lstEdits.get(iPos - 1)).getName();
		} else {
			return "";
		}
	}
	
	/**
	 * Returns the name of the next edit which can be redone
	 * @return name of the next edit which can be redone "" otherwise
	 */
	public String redoName() {
		if (iPos < lstEdits.size()) {
			return ((JPatchUndoableEdit)lstEdits.get(iPos)).getName();
		} else {
			return "";
		}
	}
	
//	/**
//	 * A main method to test the undoManager
//	 */
//	public static void main(String[] args) throws IOException {
//		JPatchUndoManager undoManager = new JPatchUndoManager(10);
//		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//		String cmd = "";
//		int edit = 1;
//		System.out.println("UndoManager test");
//		undoManager.dump();
//		System.out.println("type add, undo, redo, setstop, clearstop or end");
//		while (!cmd.equals("end")) {
//			System.out.print(">");
//			cmd = reader.readLine();
//			if (cmd.equals("add")) {
//				undoManager.addEdit(new JPatchDummyEdit("Edit " + edit++));
//				undoManager.dump();
//			} else if (cmd.equals("undo")) {
//				undoManager.undo();
//				undoManager.dump();
//			} else if (cmd.equals("redo")) {
//				undoManager.redo();
//				undoManager.dump();
//			} else if (cmd.equals("setstop")) {
//				undoManager.setStop();
//				undoManager.dump();
//			} else if (cmd.equals("clearstop")) {
//				undoManager.clearStop();
//				undoManager.dump();
//			} else {
//				System.out.println("?");
//				System.out.println("type add, undo, redo, setstop, clearstop or end");
//			}
//		}
//	}

//	/**
//	 * a dump method used by the test-code
//	 */
//	public void dump() {
//		System.out.println();
//		for (int e = 0; e < iDepth; e++) {
//			String prefix = (e == iStop) ? "#" : " ";
//			if (e == iPos) {
//				prefix += "-->";
//			} else {
//				prefix += "   ";
//			}
//			prefix += e + " ";
//			if (e < lstEdits.size()) {
//				JPatchAbstractUndoableEdit edit = (JPatchAbstractUndoableEdit)lstEdits.get(e);
//				//System.out.println(edit.name() + " (" + edit.getClass().getName() + ")");
//				edit.dump(prefix);
//			} else {
//				System.out.println("open = " + bOpen);
//				return;
//			}
//		}
//		if (iPos == iDepth) {
//			if (iStop == iPos) {
//				System.out.println("#-->");
//			} else {
//				System.out.println(" -->");
//			}
//		}
//	}
}
