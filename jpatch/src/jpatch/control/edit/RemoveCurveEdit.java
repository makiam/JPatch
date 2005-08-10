package jpatch.control.edit;

import jpatch.entity.*;

public class RemoveCurveEdit extends JPatchCompoundEdit {
	
	public RemoveCurveEdit(Curve curve) {
		super("REMOVE CURVE");
		//System.out.println("\t\tremove curve " + curve);
		addEdit(new RemoveCurveFromModelEdit(curve));
		ControlPoint[] acp = curve.getControlPointArray();
		if (acp != null) {
			for (int n = 0; n < acp.length; n++) {
				
				if (acp[n].getNextAttached() != null && acp[n].getNextAttached().isHook()) {
					Curve hookCurve = acp[n].getNextAttached().getCurve();
					addEdit(new RemoveControlPointFromSelectionsEdit(acp[n].getNextAttached()));
					addEdit(new RemoveControlPointFromCurveEdit(acp[n].getNextAttached()));
					if (hookCurve.getLength() == 2) {
						addEdit(new ChangeCPChildHookEdit(hookCurve.getStart().getParentHook(),null));
						addEdit(new RemoveControlPointFromSelectionsEdit(hookCurve.getStart()));
						addEdit(new RemoveControlPointFromSelectionsEdit(hookCurve.getStart().getEnd()));
						addEdit(new RemoveCurveFromModelEdit(hookCurve));
					}
				}
				//System.out.println("rem curve pt " + acp[n].number());
				addEdit(new DetachControlPointEdit(acp[n]));
				addEdit(new ChangeCPCurveEdit(acp[n],null));
				//System.out.println(acp[n].getCurve());
				//ControlPoint[] acpStack = acp[n].getStack();
				
				if (acp[n].getHookPos() > 0 && acp[n].getHookPos() < 1) {
					//System.out.println("x " + acp[n] + " " + acp[n].getPrevAttached());
					if (acp[n].getPrevAttached() != null) {
						addEdit(new DeleteControlPointEdit(acp[n].getPrevAttached()));
					}
				}
				//addEdit(new DeleteControlPointEdit(acp[n]));
				addEdit(new RemoveControlPointFromSelectionsEdit(acp[n]));
			}
		}
	}
}
