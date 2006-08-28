package jpatch.control.edit;

import jpatch.entity.*;

public class CompoundHook extends JPatchCompoundEdit {

	public CompoundHook(OLDControlPoint cpTargetHook, OLDControlPoint cpTarget, float hookPos) {
		if (cpTarget.getChildHook() == null) {
			OLDControlPoint startHook = cpTarget.createEmptyHookCurve();
			addEdit(new AtomicChangeControlPoint.ChildHook(cpTarget, startHook));
			addEdit(new AtomicAddCurve(startHook));
		}
		OLDControlPoint cp = cpTarget.getChildHook();
		while (cp.getNext() != null && cp.getNext().getHookPos() < hookPos) {
			cp = cp.getNext();
		}
		OLDControlPoint cpHook = new OLDControlPoint();
		cpHook.setHookPos(hookPos);
		addEdit(new AtomicInsertControlPoint(cpHook,cp));
		addEdit(new AtomicAttachControlPoints(cpTargetHook, cpHook));
	}
}
