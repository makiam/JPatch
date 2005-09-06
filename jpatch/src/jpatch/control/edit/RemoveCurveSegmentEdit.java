package jpatch.control.edit;

import java.util.*;
import jpatch.boundary.*;
import jpatch.entity.*;

public class RemoveCurveSegmentEdit extends JPatchCompoundEdit {
	
	public RemoveCurveSegmentEdit(ControlPoint cp) {
		Curve curve = cp.getCurve();
		ControlPoint cpNext = cp.getNext();
		
		/* remove hooks... */
		if (cp.getChildHook() != null) {
			//System.out.println("*");
			if (cp.getChildHook().getCurve() != null) {
				
				/* remove hook patches */
				Curve hookCurve = cp.getChildHook().getCurve();
				HashSet patches = new HashSet();
				for (ControlPoint hcp = hookCurve.getStart(); hcp != null; hcp = hcp.getNextCheckNextLoop()) {
					patches.addAll(MainFrame.getInstance().getModel().getPatchesContaining(hcp));
				}
				for (Iterator it = patches.iterator(); it.hasNext(); ) {
					Patch patch = (Patch) it.next();
					//if (patch.getModel() != null) {
						addEdit(new RemovePatchFromModelEdit(patch));
					//}
				}
				
				/* remove hook curve */
				addEdit(new CompoundDropCurve(cp.getChildHook().getCurve()));
			}
			addEdit(new ChangeCPChildHookEdit(cp, null));
		}
		
		/* remove the curve segment */
		addEdit(new ChangeControlPointPrevEdit(cpNext,null));
		addEdit(new ChangeControlPointNextEdit(cp,null));
		
		/* remove patches */
		ArrayList patches1 = MainFrame.getInstance().getModel().getPatchesContaining(cp);
		ArrayList patches2 = MainFrame.getInstance().getModel().getPatchesContaining(cpNext);
		for (Iterator it = patches1.iterator(); it.hasNext();) {
			Patch patch = (Patch) it.next();
			if (patches2.contains(patch) && patch.getModel() != null) {
				addEdit(new RemovePatchFromModelEdit(patch));
			}
		}
		
		/* check if curve is closed */
		if (curve.isClosed()) {
			
			/* change curve start */
			addEdit(new ChangeControlPointLoopEdit(curve.getStart(),false));
			addEdit(new AtomicChangeCurveStart(cp.getCurve(),cpNext));
		} else {
			
			/* check if we are the first cp on the curve */
			if (cp.getPrev() == null) {
				
				
				/* check if the next cp is the last on the curve */
				if (cpNext.getNext() == null) {
				
					/* remove hooks */
					if (cpNext.getNextAttached() != null && cpNext.getNextAttached().isHook()) {
						Curve hookCurve = cpNext.getNextAttached().getCurve();
						addEdit(new RemoveControlPointFromSelectionsEdit(cpNext.getNextAttached()));
						addEdit(new AtomicRemoveControlPointFromCurve(cpNext.getNextAttached()));
						if (hookCurve.getLength() == 2) {
							addEdit(new ChangeCPChildHookEdit(hookCurve.getStart().getParentHook(),null));
							//addEdit(new RemoveCurveFromModelEdit(hookCurve));
							addEdit(new CompoundDropCurve(hookCurve));
						}
					}
					
					/* remove the entire curve */
					addEdit(new AtomicRemoveCurve(curve));
					addEdit(new AtomicDetatchControlPoint(cp));
					addEdit(new AtomicDetatchControlPoint(cpNext));
					addEdit(new RemoveControlPointFromSelectionsEdit(cp));
					addEdit(new RemoveControlPointFromSelectionsEdit(cpNext));
				} else {
					
					/* remove the controlpoint */
					addEdit(new RemoveControlPointFromSelectionsEdit(cp));
					addEdit(new AtomicDetatchControlPoint(cp));
					addEdit(new AtomicChangeCurveStart(cp.getCurve(),cpNext));
					addEdit(new ChangeCPCurveEdit(cp,null));
				}
				
				/* remove hooks */
				if (cp.getNextAttached() != null && cp.getNextAttached().isHook()) {
					Curve hookCurve = cp.getNextAttached().getCurve();
					addEdit(new AtomicRemoveControlPointFromCurve(cp.getNextAttached()));
					addEdit(new RemoveControlPointFromSelectionsEdit(cp.getNextAttached()));
					if (hookCurve.getLength() == 2) {
						addEdit(new ChangeCPChildHookEdit(hookCurve.getStart().getParentHook(),null));
						addEdit(new CompoundDropCurve(hookCurve));
					}
				}
			} else {
				
				/* check if the next cp is not the last on the curve */
				if (cpNext.getNext() != null) {
				
					/* add a new curve */
					Curve newCurve = new Curve(cpNext);
					addEdit(new AtomicAddCurve(newCurve));
					addEdit(new ValidateCurveEdit(newCurve));
				} else {
					/* remove hooks */
					if (cpNext.getNextAttached() != null && cpNext.getNextAttached().isHook()) {
						Curve hookCurve = cpNext.getNextAttached().getCurve();
						addEdit(new RemoveControlPointFromSelectionsEdit(cpNext.getNextAttached()));
						addEdit(new AtomicRemoveControlPointFromCurve(cpNext.getNextAttached()));
						if (hookCurve.getLength() == 2) {
							addEdit(new ChangeCPChildHookEdit(hookCurve.getStart().getParentHook(),null));
							//addEdit(new RemoveCurveFromModelEdit(hookCurve));
							addEdit(new CompoundDropCurve(hookCurve));
						}
					}
					
					/* remove the next cp */
					addEdit(new AtomicDetatchControlPoint(cpNext));
					addEdit(new RemoveControlPointFromSelectionsEdit(cpNext));
				}
			}
		}
	}
}
