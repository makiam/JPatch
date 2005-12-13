package jpatch.control.edit;

import java.util.*;

import jpatch.boundary.*;
import jpatch.entity.*;

public class CompoundRemoveCurveSegment extends JPatchCompoundEdit {
	
	public CompoundRemoveCurveSegment(ControlPoint cp) {
		ControlPoint cpNext = cp.getNext();
		/* remove hooks... */
		if (cp.getChildHook() != null)
			addEdit(new CompoundDropCurve(cp.getChildHook()));
		
		/* remove the curve segment */
		addEdit(new AtomicChangeControlPoint.Prev(cpNext,null));
		addEdit(new AtomicChangeControlPoint.Next(cp,null));
		
		/* remove patches */
		for (Iterator it = (new HashSet(MainFrame.getInstance().getModel().getPatchSet())).iterator(); it.hasNext();) {
			Patch patch = (Patch) it.next();
			if (patch.contains(cp) && patch.contains(cpNext))
				addEdit(new AtomicRemovePatch(patch));
		}
		
		/* check if curve is closed */
		if (cp.getStart().getLoop()) {
			/* change curve start */
			addEdit(new AtomicRemoveCurve(cp.getStart()));
			addEdit(new AtomicAddCurve(cpNext));
			addEdit(new AtomicChangeControlPoint.Loop(cp.getStart()));
		} else {
			// check if this was the stat of the curve
			if (cp.getPrev() == null) {
				// YES
				// remove the curve
				addEdit(new AtomicRemoveCurve(cp));
				// drop first cp
				addEdit(new CompoundDropControlPoint(cp));
			}
			// check if this was the end of the curve
			if (cpNext.getNext() != null)
				// NO
				// add new curve
				addEdit(new AtomicAddCurve(cpNext));
			else
				// drop last cp
				addEdit(new CompoundDropControlPoint(cpNext));
		}
	}
}
			
//			/* check if we are the first cp on the curve */
//			if (cp.getPrev() == null) {
//				
//				
//				/* check if the next cp is the last on the curve */
//				if (cpNext.getNext() == null) {
//				
//					/* remove hooks */
//					if (cpNext.getNextAttached() != null && cpNext.getNextAttached().isHook()) {
//						Curve hookCurve = cpNext.getNextAttached().getCurve();
//						addEdit(new RemoveControlPointFromSelectionsEdit(cpNext.getNextAttached()));
//						addEdit(new AtomicRemoveControlPointFromCurve(cpNext.getNextAttached()));
//						if (hookCurve.getLength() == 2) {
//							addEdit(new ChangeCPChildHookEdit(hookCurve.getStart().getParentHook(),null));
//							//addEdit(new RemoveCurveFromModelEdit(hookCurve));
//							addEdit(new CompoundDropCurve(hookCurve));
//						}
//					}
//					
//					/* remove the entire curve */
//					addEdit(new AtomicRemoveCurve(curve));
//					addEdit(new AtomicDetatchControlPoint(cp));
//					addEdit(new AtomicDetatchControlPoint(cpNext));
//					addEdit(new RemoveControlPointFromSelectionsEdit(cp));
//					addEdit(new RemoveControlPointFromSelectionsEdit(cpNext));
//				} else {
//					
//					/* remove the controlpoint */
//					addEdit(new RemoveControlPointFromSelectionsEdit(cp));
//					addEdit(new AtomicDetatchControlPoint(cp));
//					addEdit(new AtomicChangeCurveStart(cp.getCurve(),cpNext));
//					addEdit(new ChangeCPCurveEdit(cp,null));
//				}
//				
//				/* remove hooks */
//				if (cp.getNextAttached() != null && cp.getNextAttached().isHook()) {
//					Curve hookCurve = cp.getNextAttached().getCurve();
//					addEdit(new AtomicRemoveControlPointFromCurve(cp.getNextAttached()));
//					addEdit(new RemoveControlPointFromSelectionsEdit(cp.getNextAttached()));
//					if (hookCurve.getLength() == 2) {
//						addEdit(new ChangeCPChildHookEdit(hookCurve.getStart().getParentHook(),null));
//						addEdit(new CompoundDropCurve(hookCurve));
//					}
//				}
//			} else {
//				
//				/* check if the next cp is not the last on the curve */
//				if (cpNext.getNext() != null) {
//				
//					/* add a new curve */
//					Curve newCurve = new Curve(cpNext);
//					addEdit(new AtomicAddCurve(newCurve));
//					addEdit(new ValidateCurveEdit(newCurve));
//				} else {
//					/* remove hooks */
//					if (cpNext.getNextAttached() != null && cpNext.getNextAttached().isHook()) {
//						Curve hookCurve = cpNext.getNextAttached().getCurve();
//						addEdit(new RemoveControlPointFromSelectionsEdit(cpNext.getNextAttached()));
//						addEdit(new AtomicRemoveControlPointFromCurve(cpNext.getNextAttached()));
//						if (hookCurve.getLength() == 2) {
//							addEdit(new ChangeCPChildHookEdit(hookCurve.getStart().getParentHook(),null));
//							//addEdit(new RemoveCurveFromModelEdit(hookCurve));
//							addEdit(new CompoundDropCurve(hookCurve));
//						}
//					}
//					
//					/* remove the next cp */
//					addEdit(new AtomicDetatchControlPoint(cpNext));
//					addEdit(new RemoveControlPointFromSelectionsEdit(cpNext));
//				}
//			}
//		}
//	}
//}
