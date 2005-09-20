package jpatch.control.edit;

import java.util.*;

import jpatch.entity.*;

/**
 *  Extends a selection
 */
public class ExtendSelectionEdit extends JPatchCompoundEdit {
	
	public ExtendSelectionEdit(PointSelection ps, boolean complete) {
		Collection pointsToAdd = new ArrayList();
		Collection pointsToRemove = new ArrayList();
		if (!complete) {
			ControlPoint cpa = ps.getControlPoint();
			for (ControlPoint cp = cpa.getCurve().getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
				pointsToAdd.add(cp.getHead());
			}
			if (!cpa.isHead()) {
				pointsToRemove.add(cpa);
			}
		} else {
			HashSet setPoints = new HashSet();
			ControlPoint[] acp = ps.getControlPointArray();
			for (int i = 0; i < acp.length; selectPoint(acp[i++], setPoints));
			pointsToAdd.addAll(setPoints);
			pointsToAdd.removeAll(ps.getSelectedControlPoints());
		}
		addEdit(new AddControlPointsToSelectionEdit(ps,pointsToAdd));
		addEdit(new RemoveControlPointsFromSelectionEdit(ps,pointsToRemove));
	}
	
	private void selectPoint(ControlPoint cp, HashSet setPoints) {
		if (cp != null) {
			cp = cp.getHead();
			if (!setPoints.contains(cp)) {
				setPoints.add(cp);
				ControlPoint[] stack = cp.getStack();
				for (int i = 0; i < stack.length; i++) {
					selectPoint(stack[i].getNext(), setPoints);
					selectPoint(stack[i].getPrev(), setPoints);
				}
			}
		}
	}
}

