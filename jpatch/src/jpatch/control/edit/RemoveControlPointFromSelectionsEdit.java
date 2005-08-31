package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public class RemoveControlPointFromSelectionsEdit extends JPatchCompoundEdit {
	
	public RemoveControlPointFromSelectionsEdit(ControlPoint cp) {
		strName = "cp " + cp;
		//System.out.println("\t\t\tremove cp from selections cp " + cp.number());
		ArrayList remove = new ArrayList();
		for (Iterator it = MainFrame.getInstance().getSelectionsContaining(cp).iterator(); it.hasNext(); ) {
			NewSelection selection = (NewSelection) it.next();
			addEdit(new RemoveControlPointFromSelectionEdit(cp, selection));
			if (selection.getMap().size() == 0 && !selection.isActive()) {
				remove.add(selection);
			}
		}
		for (Iterator it = remove.iterator(); it.hasNext(); ) {
			NewSelection selection = (NewSelection) it.next();
			addEdit(new RemoveSelectionEdit(selection));
			if (MainFrame.getInstance().getSelection() == selection) {
				addEdit(new ChangeSelectionEdit(null));
			}
			
		}
		addEdit(new RemoveControlPointFromMorphsEdit(cp, MainFrame.getInstance().getModel()));
	}
}
