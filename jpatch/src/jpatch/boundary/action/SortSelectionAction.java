/*
 * $Id: SortSelectionAction.java,v 1.2 2005/09/20 16:17:54 sascha_l Exp $
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
import javax.swing.tree.*;

import jpatch.boundary.*;

import jpatch.entity.*;


/**
 * @author lois
 * modified by sascha
 * This Action sorts all the elements under the Selections treenode.
 * @version $Revision: 1.2 $
 */

public final class SortSelectionAction extends AbstractAction {
	
	private static final long serialVersionUID = 8956044610625919326L;
	
	public SortSelectionAction() {
		super("Sort Selections");
	}
	
	public void actionPerformed(ActionEvent actionEvent) {
		Model model = MainFrame.getInstance().getModel();
		
		/* make a new(!) list containing all selections */
		ArrayList list = new ArrayList(model.getSelections());
		
		/* sort it (by simply comparing the string representations of the selections) */
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return (o1.toString().compareToIgnoreCase(o2.toString()));
			}
			public boolean equals(Object o1, Object o2) {
				return (compare(o1, o2) == 0);
			}
		});
		
		/* remove all selections and add them again in order. */
		for (Iterator it = list.iterator(); it.hasNext(); model.removeSelection((Selection) it.next()));
		for (Iterator it = list.iterator(); it.hasNext(); model.addSelection((Selection) it.next()));
		
		/* reload the tree model */
		((DefaultTreeModel)MainFrame.getInstance().getTree().getModel()).reload();
		
		/* make the slections visible */
		TreePath path = ((Selection) list.get(list.size() - 1)).getTreePath();
		MainFrame.getInstance().getTree().makeVisible(path);
	}
	
//	// quick sort
//	private void swap(int i, int j) {
//		if (i != j) {
//			Selection tmp1 = (Selection)node.getChildAt(i);
//			Selection tmp2 = (Selection)node.getChildAt(j);
//			new RemoveSelectionEdit(tmp1);
//			new RemoveSelectionEdit(tmp2);
//			new AddSelectionEdit(i, tmp2);
//			new AddSelectionEdit(j, tmp1);
//		}
//	}
//	
//	private int partition(int begin, int end) {
//		int index = begin + RND.nextInt(end - begin + 1);
//		Selection pivot = (Selection)node.getChildAt(index);
//		swap(index, end);        
//		for (int i = index = begin; i < end; ++ i) {
//			if (((Selection)node.getChildAt(i)).toString().compareToIgnoreCase(pivot.toString()) <= 0) {
//				swap(index++, i);
//			}
//		}
//		swap(index, end);        
//		return (index);
//	}
//	
//	private void qsort(int begin, int end) {
//		if (end > begin) {
//			int index = partition(begin, end);
//			qsort(begin, index - 1);
//			qsort(index + 1,  end);
//		}
//	}	
	
}