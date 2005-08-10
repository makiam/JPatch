package jpatch.control.edit;

import jpatch.entity.*;

public class ComplexAppendControlPointsEdit extends JPatchCompoundEdit {
	
	private ControlPoint cpA;
	private ControlPoint cpB;
	
	public ComplexAppendControlPointsEdit(ControlPoint A, ControlPoint B) {
		// append A to B - A will disappear
		super("append");
		cpA = A;
		cpB = B;
		addEdit(new RemoveControlPointFromSelectionsEdit(cpA));
		//System.out.println("cplx A:" + cpA + " B:" + cpB);
		if ((cpA.getNext() != null && cpA.getPrev() != null) || (cpB.getNext() != null && cpB.getPrev() != null)) {
			throw new IllegalStateException("can't append - cp's are no curve ends");
		}
		if (cpA.getCurve() != cpB.getCurve()) {
			if (cpB.getNext() != null) {
				//System.out.println("rev B");
				addEdit(new ReverseCurveEdit(cpB.getCurve()));
			}
			if (cpA.getPrev() != null) {
				//System.out.println("rev A");
				addEdit(new ReverseCurveEdit(cpA.getCurve()));
			}
			
			if (cpA.getChildHook() != null) {
				//System.out.println("hook");
				addEdit(new ChangeCPChildHookEdit(cpB,cpA.getChildHook()));
				addEdit(new ChangeCPParentHookEdit(cpA.getChildHook(),cpB));
				//cpB.setChildHook(cpA.getChildHook());
				//cpA.getChildHook().setParentHook(cpB);
			}
			
			addEdit(new RemoveCurveFromModelEdit(cpA.getCurve()));
			addEdit(new SimpleAppendControlPointsEdit(cpA.getNext(),cpB));
			addEdit(new ValidateCurveEdit(cpB.getCurve()));
		} else {
			if (cpA.getPrev() != null) {
				addEdit(new SimpleAppendControlPointsEdit(cpB,cpA.getPrev()));
				addEdit(new ChangeControlPointLoopEdit(cpB,true));
			} else {
				addEdit(new SimpleAppendControlPointsEdit(cpA,cpB.getPrev()));
				addEdit(new ChangeControlPointLoopEdit(cpA,true));
			}
		}
	}
}
