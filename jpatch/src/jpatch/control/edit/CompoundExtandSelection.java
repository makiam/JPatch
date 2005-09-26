package jpatch.control.edit;

import java.util.*;
import jpatch.boundary.*;
import jpatch.entity.*;

/**
 *  Extends a selection
 */
public class CompoundExtandSelection extends JPatchCompoundEdit implements JPatchRootEdit {
	
	public CompoundExtandSelection(Selection selection) {
		Map pointsToAdd = new HashMap();
		Map pointsToRemove = new HashMap();
		if (selection.getDirection() != 0) {
			ControlPoint cpa = (ControlPoint) selection.getHotObject();
			for (ControlPoint cp = cpa.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
				pointsToAdd.put(cp.getHead(), new Float(1));
			}
			if (!cpa.isHead()) {
				pointsToRemove.put(cpa, new Float(1));
			}
		} else {
			HashMap mapPoints = new HashMap();
			ControlPoint[] acp = selection.getControlPointArray();
			for (int i = 0; i < acp.length; i++)
				selectPoint(acp[i], mapPoints);
			pointsToAdd.putAll(mapPoints);
			for (Iterator it = selection.getObjects().iterator(); it.hasNext(); )
				pointsToAdd.remove(it.next());
		}
		addEdit(new AtomicModifySelection.AddObjects(selection, pointsToAdd));
		addEdit(new AtomicModifySelection.RemoveObjects(selection,pointsToRemove));
	}
	
	public String getName() {
		return "extend selection";
	}
	
	private void selectPoint(ControlPoint cp, HashMap mapPoints) {
		if (cp != null) {
			cp = cp.getHead();
			if (!mapPoints.keySet().contains(cp)) {
				mapPoints.put(cp, new Float(1));
				ControlPoint[] stack = cp.getStack();
				for (int i = 0; i < stack.length; i++) {
					selectPoint(stack[i].getNext(), mapPoints);
					selectPoint(stack[i].getPrev(), mapPoints);
				}
			}
		}
	}
}

