package jpatch.control.edit;

import java.util.*;
import jpatch.boundary.*;
import jpatch.entity.*;

/**
 *  Extends a selection
 */
public class CompoundExpandSelection extends JPatchCompoundEdit implements JPatchRootEdit {
	
	public CompoundExpandSelection(OLDSelection selection) {
		Map pointsToAdd = new HashMap();
		Map pointsToRemove = new HashMap();
		if (selection.getDirection() != 0) {
			OLDControlPoint cpa = (OLDControlPoint) selection.getHotObject();
			for (OLDControlPoint cp = cpa.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
				pointsToAdd.put(cp.getHead(), new Float(1));
			}
			if (!cpa.isHead()) {
				pointsToRemove.put(cpa, new Float(1));
			}
		} else {
			HashMap mapPoints = new HashMap();
			OLDControlPoint[] acp = selection.getControlPointArray();
			for (int i = 0; i < acp.length; i++)
				selectPoint(acp[i], mapPoints);
			pointsToAdd.putAll(mapPoints);
			for (Iterator it = selection.getObjects().iterator(); it.hasNext(); )
				pointsToAdd.remove(it.next());
		}
		addEdit(new AtomicModifySelection.AddObjects(selection, pointsToAdd));
		addEdit(new AtomicModifySelection.RemoveObjects(selection,pointsToRemove));
		selection.setDirection(0);
		addEdit(new AtomicModifySelection.Pivot(selection, selection.getCenter(), true));
	}
	
	public String getName() {
		return "extend selection";
	}
	
	private void selectPoint(OLDControlPoint cp, HashMap mapPoints) {
		if (cp != null) {
			cp = cp.getHead();
			if (!mapPoints.keySet().contains(cp)) {
				mapPoints.put(cp, new Float(1));
				OLDControlPoint[] stack = cp.getStack();
				for (int i = 0; i < stack.length; i++) {
					selectPoint(stack[i].getNext(), mapPoints);
					selectPoint(stack[i].getPrev(), mapPoints);
				}
			}
		}
	}
}

