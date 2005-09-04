package jpatch.control.edit;

import jpatch.entity.*;

public class CompoundRemoveHookCurve extends JPatchCompoundEdit {
	
	public CompoundRemoveHookCurve(Curve curve) {
		addEdit(new AtomicRemoveCurve(curve));
		ControlPoint[] acp = curve.getControlPointArray();
		if (acp != null) {
			for (int n = 0; n < acp.length; n++) {
				
				if (acp[n].getNextAttached() != null && acp[n].getNextAttached().isHook()) {
					Curve hookCurve = acp[n].getNextAttached().getCurve();
//					addEdit(new RemoveControlPointFromSelectionsEdit(acp[n].getNextAttached()));
					addEdit(new AtomicRemoveControlPointFromCurve(acp[n].getNextAttached()));
					if (hookCurve.getLength() == 2) {
						addEdit(new AtomicChangeControlPoint.ChildHook(hookCurve.getStart().getParentHook(),null));
						addEdit(new RemoveControlPointFromSelectionsEdit(hookCurve.getStart()));
						addEdit(new RemoveControlPointFromSelectionsEdit(hookCurve.getStart().getEnd()));
						addEdit(new AtomicRemoveCurve(hookCurve));
					}
				}
				//System.out.println("rem curve pt " + acp[n].number());
				addEdit(new AtomicDetatchControlPoint(acp[n]));
				addEdit(new AtomicChangeControlPoint.Curve(acp[n],null));
				//System.out.println(acp[n].getCurve());
				//ControlPoint[] acpStack = acp[n].getStack();
				
				if (acp[n].getHookPos() > 0 && acp[n].getHookPos() < 1) {
					//System.out.println("x " + acp[n] + " " + acp[n].getPrevAttached());
					if (acp[n].getPrevAttached() != null) {
						addEdit(new CompoundDeleteControlPoint(acp[n].getPrevAttached()));
					}
				}
				//addEdit(new DeleteControlPointEdit(acp[n]));
//				addEdit(new RemoveControlPointFromSelectionsEdit(acp[n]));
			}
		}
	}
}
