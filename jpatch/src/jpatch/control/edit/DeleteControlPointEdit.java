package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  Thois edit removes a ControlPoint and all appended ControlPoints.
 *  It will create a new CompoundEdit with one RemoveControlPointFromCurveEdit
 *  for each attached ControlPoint
 *
 * @author     aledinsk
 * @created    27. Dezember 2003
 */

public class DeleteControlPointEdit extends JPatchCompoundEdit {

	/**
	 * Constructor for RemoveControlPointEdit
	 *
	 * @param  cp  ControlPoint to be removed
	 */
	public DeleteControlPointEdit(ControlPoint cp) {
		super("Remove Controlpoint");
		//System.out.println("\tdelete controlpoint edit " + cp.number());
		Curve curve = cp.getCurve();	// get the curve we're on
		if (curve != null) {
			/*
			 * remove hooks
			 */
			if (cp.getParentHook() != null && cp.getParentHook().getChildHook() == cp) {
				//System.out.println("*");
				addEdit(new ChangeCPChildHookEdit(cp.getParentHook(),null));
			}	
			if (cp.getChildHook() != null) {
				if (cp.getChildHook().getCurve() != null) {
					addEdit(new RemoveCurveEdit(cp.getChildHook().getCurve()));
				}
				addEdit(new ChangeCPChildHookEdit(cp,null));
			}
			if (cp.getPrev() != null && cp.getPrev().getChildHook() != null) {
				if (cp.getPrev().getChildHook().getCurve() != null) {
					addEdit(new RemoveCurveEdit(cp.getPrev().getChildHook().getCurve()));
				}
				addEdit(new ChangeCPChildHookEdit(cp.getPrev(),null));
			}
			/*
			 * if the curve is open
			 */
			if (!curve.isClosed()) {
				//System.out.println("not closed");
				/*
				 * if we are not the first or second point on the curve
				 */
				if (cp.getPrev() != null && cp.getPrev().getPrev() != null) {
					addEdit(new DeleteControlPointFromCurveEdit(cp));		// delete the cp
					addEdit(new RemoveControlPointFromSelectionsEdit(cp));
					/*
					 * if we are not the last or second last point on the curve
					 */
					if (cp.getNext() != null && cp.getNext().getNext() != null) {
						Curve newCurve = new Curve(cp.getNext());
						addEdit(new CreateCurveEdit(newCurve));	// create a new curve
						addEdit(new ValidateCurveEdit(newCurve)); // validate new curve
					} 
					/*
					 * if we are the second last point
					 */
					else if (cp.getNext() != null) {
						addEdit(new DetachControlPointEdit(cp.getNext())); // detach and
						addEdit(new ChangeCPCurveEdit(cp.getNext(),null)); // remove the last point from the curve
						addEdit(new RemoveControlPointFromSelectionsEdit(cp.getNext()));
						/*
						 * check if we have to remove a hook
						 */
						if (cp.getNext().getNextAttached() != null && cp.getNext().getNextAttached().isHook()) {
							//System.out.println("+++");
							Curve hookCurve = cp.getNext().getNextAttached().getCurve();
							addEdit(new RemoveControlPointFromSelectionsEdit(cp.getNext().getNextAttached()));
							addEdit(new RemoveControlPointFromCurveEdit(cp.getNext().getNextAttached()));
							if (hookCurve.getLength() == 2) {
								addEdit(new ChangeCPChildHookEdit(hookCurve.getStart().getParentHook(),null));
								//addEdit(new RemoveCurveFromModelEdit(hookCurve));
								addEdit(new RemoveCurveEdit(hookCurve));
							}
						}
					}
				}
                
				/*
				 * if we may are the first or second point, but not the last or second last one
				 */
				else if (cp.getNext() != null && cp.getNext().getNext() != null) {
					/*
					 * if we're the second point
					 */
					if (cp.getPrev() != null) {
						addEdit(new DetachControlPointEdit(cp.getPrev())); // detach and
						addEdit(new ChangeCPCurveEdit(cp.getPrev(),null)); // remove the first point from the curve
						addEdit(new RemoveControlPointFromSelectionsEdit(cp.getPrev()));
						/*
						 * check if we have to remove a hook
						 */
						if (cp.getPrev().getNextAttached() != null && cp.getPrev().getNextAttached().isHook()) {
							Curve hookCurve = cp.getPrev().getNextAttached().getCurve();
							addEdit(new RemoveControlPointFromSelectionsEdit(cp.getPrev().getNextAttached()));
							addEdit(new RemoveControlPointFromCurveEdit(cp.getPrev().getNextAttached()));
							if (hookCurve.getLength() == 2) {
								addEdit(new ChangeCPChildHookEdit(hookCurve.getStart().getParentHook(),null));
								//addEdit(new RemoveCurveFromModelEdit(hookCurve));
								addEdit(new RemoveCurveEdit(hookCurve));
							}
						}
					}
					addEdit(new DeleteControlPointFromCurveEdit(cp));		// delete the cp
					addEdit(new RemoveControlPointFromSelectionsEdit(cp));
					addEdit(new ChangeCurveStartEdit(curve,cp.getNext()));		// change curve start
					
				}
				
				
				//else if (cp.getNext() == null && cp.getPrev() == null) {
				//	addEdit(new DeleteControlPointFromCurveEdit(cp));
				//}
				
				/*
				 * or else, (only two points left)
				 */
				else if (curve.getModel() != null) {
					///*
					// * and remove the curve from the model
					// */
					//
					///*
					// * detach all points on the curve
					// */
					//ControlPoint[] acpCurve = curve.getControlPointArray();
					//for (int c = 0; c < acpCurve.length; c++) {
					//	addEdit(new DetachControlPointEdit(acpCurve[c]));
					//	addEdit(new ChangeCPCurveEdit(acpCurve[c],null));
					//}
					//
					//addEdit(new RemoveCurveFromModelEdit(curve));
					addEdit(new RemoveCurveEdit(curve));
				}
			}
                
			/*
			 * else (if the curve is closed)
			 */
			else {
				//System.out.println("closed");
				/*
				 * if there are less than 3 points on the curve
				 */
				if (curve.getLength() < 3 && curve.getModel() != null) {
					///*
					// * remove the curve from the model
					// */
					//addEdit(new RemoveCurveFromModelEdit(curve));
					///*
					// * detach all points
					// */
					//ControlPoint[] acpCurve = curve.getControlPointArray();
					//for (int c = 0; c < acpCurve.length; c++) {
					//	addEdit(new DetachControlPointEdit(acpCurve[c]));
					//	addEdit(new ChangeCPCurveEdit(acpCurve[c],null));
					//}
					addEdit(new RemoveCurveEdit(curve));
					
				} else {
					ControlPoint cpNewStart = cp.getNext();			// the curve will be opened, this is the new start
					addEdit(new DeleteControlPointFromCurveEdit(cp));	// delete our point
					addEdit(new RemoveControlPointFromSelectionsEdit(cp));
					if (cpNewStart != null) {
						//System.out.println("newStart");
						addEdit(new ChangeControlPointLoopEdit(curve.getStart(), false));
						addEdit(new ChangeCurveStartEdit(curve, cpNewStart));	// change curve start
					} else {
						System.err.println("********** there's a problem in DeleteControlPointEdit!!! *************");
						for (ControlPoint cpCurve = curve.getStart(); cpCurve != null; cpCurve = cpCurve.getNextCheckNextLoop()) {
							if (cpCurve != cp) {
								addEdit(new DetachControlPointEdit(cpCurve));
							}
						}
						addEdit(new RemoveCurveFromModelEdit(curve));
					}
				}
			}
		}
	}
}
