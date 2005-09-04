package jpatch.control.edit;

import jpatch.entity.*;

public class HookEdit extends JPatchCompoundEdit {

	public HookEdit(ControlPoint cpTargetHook, ControlPoint cpTarget, float hookPos) {
		super("hook");
		if (cpTarget.getChildHook() == null) {
			Curve hookCurve = cpTarget.createEmptyHookCurve();
			addEdit(new ChangeCPChildHookEdit(cpTarget));
			addEdit(new AtomicAddCurve(hookCurve));
		}
		ControlPoint cp = cpTarget.getChildHook();
		while (cp.getNext() != null && cp.getNext().getHookPos() < hookPos) {
			cp = cp.getNext();
		}
		ControlPoint cpHook = new ControlPoint();
		cpHook.setHookPos(hookPos);
		addEdit(new InsertControlPointEdit(cpHook,cp));
		addEdit(new AtomicAttachControlPoints(cpTargetHook, cpHook));
	}
}
