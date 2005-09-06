package jpatch.control.edit;

import jpatch.entity.*;

public class CompoundAppendControlPoints extends JPatchCompoundEdit {
	
	public CompoundAppendControlPoints(ControlPoint cpA, ControlPoint cpB) {
		// cpA and cpB on the same curve?
		if(cpA.getCurve() == cpB.getCurve()) {
			// YES
			// is cpA curve-start and cpB curve-end?
			if (cpA.getPrev() == null && cpB.getNext() == null)
				// YES
				// reverse curve
				addEdit(new AtomicReverseCurve(cpA.getCurve()));
			else
				// NO
				// is cpB curve-start and cpA curve-end?
				if (cpB.getPrev() != null || cpA.getNext() != null)
					// NO
					// throw exception
					throw new IllegalArgumentException("can't attach - not curve start/end");
			// is curve lenght >= 4?
			if (cpA.getCurve().getLength() < 4)
				// NO
				// throw exception
				throw new IllegalArgumentException("can't attach - loop curve too short");
			// append cpB to cpA.cpPrev
			addEdit(new AtomicAppendControlPoints(cpB, cpA.getPrev()));
			// set loop flag on cpB
			addEdit(new AtomicChangeControlPoint.Loop(cpB));
		} else {
			// is cpB curve-end?
			if (cpB.getNext() != null) {
				// NO
				// is cpB curve-start?
				if (cpB.getPrev() == null)
					// YES
					// reverse cpB's curve
					addEdit(new AtomicReverseCurve(cpB.getCurve()));
				else
					// NO
					// throw exception
					throw new IllegalArgumentException("can't attach - not curve start/end");
			}
			// is cpA curve-start?
			if (cpA.getPrev() != null) {
				// NO
				// is cpA curve-end?
				if (cpA.getNext() == null)
					// YES
					// reverse cpA's curve
					addEdit(new AtomicReverseCurve(cpA.getCurve()));
				else
					// NO
					// throw exception
					throw new IllegalArgumentException("can't attach - not curve start/end");
			}
			
		}
		
	}
}

//	private ControlPoint cpA;
//	private ControlPoint cpB;
//	
//	public CompoundAppendControlPoints(ControlPoint A, ControlPoint B) {
//		// append A to B - A will disappear
//		super("append");
//		cpA = A;
//		cpB = B;
//		addEdit(new RemoveControlPointFromSelectionsEdit(cpA));
//		//System.out.println("cplx A:" + cpA + " B:" + cpB);
//		if ((cpA.getNext() != null && cpA.getPrev() != null) || (cpB.getNext() != null && cpB.getPrev() != null)) {
//			throw new IllegalStateException("can't append - cp's are no curve ends");
//		}
//		if (cpA.getCurve() != cpB.getCurve()) {
//			if (cpB.getNext() != null) {
//				//System.out.println("rev B");
//				addEdit(new ReverseCurveEdit(cpB.getCurve()));
//			}
//			if (cpA.getPrev() != null) {
//				//System.out.println("rev A");
//				addEdit(new ReverseCurveEdit(cpA.getCurve()));
//			}
//			
//			if (cpA.getChildHook() != null) {
//				//System.out.println("hook");
//				addEdit(new ChangeCPChildHookEdit(cpB,cpA.getChildHook()));
//				addEdit(new ChangeCPParentHookEdit(cpA.getChildHook(),cpB));
//				//cpB.setChildHook(cpA.getChildHook());
//				//cpA.getChildHook().setParentHook(cpB);
//			}
//			
//			addEdit(new AtomicRemoveCurve(cpA.getCurve()));
//			addEdit(new AtomicAppendControlPoints(cpA.getNext(),cpB));
//			addEdit(new ValidateCurveEdit(cpB.getCurve()));
//		} else {
//			if (cpA.getPrev() != null) {
//				addEdit(new AtomicAppendControlPoints(cpB,cpA.getPrev()));
//				addEdit(new ChangeControlPointLoopEdit(cpB,true));
//			} else {
//				addEdit(new AtomicAppendControlPoints(cpA,cpB.getPrev()));
//				addEdit(new ChangeControlPointLoopEdit(cpA,true));
//			}
//		}
//	}

