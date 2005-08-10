package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;
import jpatch.boundary.*;
import jpatch.boundary.selection.*;

public class RemoveControlPointFromSelectionsEdit extends JPatchCompoundEdit {
	
	public RemoveControlPointFromSelectionsEdit(ControlPoint cp) {
		//System.out.println("\t\t\tremove cp from selections cp " + cp.number());
		ArrayList remove = new ArrayList();
		for (Iterator it = MainFrame.getInstance().getModel().getSelections().iterator(); it.hasNext(); ) {
			PointSelection ps = (PointSelection) it.next();
			if (ps.contains(cp)) {
				addEdit(new RemoveControlPointFromSelectionEdit(cp, ps));
				if (ps.getSize() == 0) {
					remove.add(ps);
				}
			}
		}
		for (Iterator it = remove.iterator(); it.hasNext(); ) {
			addEdit(new RemoveSelectionEdit((PointSelection) it.next()));
		}
		addEdit(new RemoveControlPointFromMorphsEdit(cp, MainFrame.getInstance().getModel()));
	}
}
