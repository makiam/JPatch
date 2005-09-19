package jpatch.control.edit;

import jpatch.entity.*;

public class CompoundHook extends JPatchCompoundEdit {

	public CompoundHook(ControlPoint cpTargetHook, ControlPoint cpTarget, float hookPos) {
		if (cpTarget.getChildHook() == null) {
			ControlPoint startHook = cpTarget.createEmptyHookCurve();
			addEdit(new AtomicChangeControlPoint.ChildHook(cpTarget, startHook));
			addEdit(new AtomicAddCurve(startHook));
		}
		ControlPoint cp = cpTarget.getChildHook();
		while (cp.getNext() != null && cp.getNext().getHookPos() < hookPos) {
			cp = cp.getNext();
		}
		ControlPoint cpHook = new ControlPoint();
		cpHook.setHookPos(hookPos);
		addEdit(new AtomicInsertControlPoint(cpHook,cp));
		addEdit(new AtomicAttachControlPoints(cpTargetHook, cpHook));
	}
}
