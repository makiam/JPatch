/*
 * $Id: DeleteDefaultSelectionsAction.java,v 1.4 2005/09/28 14:02:12 sascha_l Exp $
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

package jpatch.boundary.action;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import jpatch.boundary.*;

import jpatch.control.edit.*;

/**
 * @author lois
 * modified by sascha
 * This Action removes all selections starting with a "*" character
 * @version $Revision: 1.4 $
 */
public final class DeleteDefaultSelectionsAction extends AbstractAction {
	
	private static final long serialVersionUID = 8956044610625919326L;
		
	public DeleteDefaultSelectionsAction() {
		super("Delete * Selections");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		/* make a new CompoundEdit */
		JPatchActionEdit edit = new JPatchActionEdit("Remove * Selections");
		
		/* make a new(!) list containing all selections */
		ArrayList list = new ArrayList(MainFrame.getInstance().getModel().getSelections());
		
		/* cycle through list and add RemoveSelectionEdits if the name starts with an "*" */
		for (Iterator it = list.iterator(); it.hasNext(); ) {
			Selection selection = (Selection) it.next();
			if (selection.toString().startsWith("*"))
				edit.addEdit(new AtomicRemoveSelection(selection));		
		}
		
		/* add edit to undomanager */
		MainFrame.getInstance().getUndoManager().addEdit(edit);
	}
}