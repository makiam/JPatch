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

public class RemoveControlPointEdit extends JPatchCompoundEdit {

	/**
	 * Constructor for RemoveControlPointEdit
	 *
	 * @param  cp  ControlPoint to be removed
	 */
	public RemoveControlPointEdit(ControlPoint cp) {
		super("Remove Controlpoint");
		Curve curve = cp.getCurve();
		
		if (cp.getNext() != null && cp.getNext().getNextAttached() != null && cp.getNext().getNextAttached().isHook()) {
			if (cp.getPrev() != null && cp.getPrev().getNextAttached() != null && cp.getPrev().getNextAttached().isHook()) {
				addEdit(new RemoveCurveEdit(curve));
				return;
			}
		}
		/*
		 * remove hooks
		 */
		if (cp.getParentHook() != null && cp.getParentHook().getChildHook() == cp) {
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
		 * if the curve's lenght is < 3 remove the whole curve
		 */
		if (curve.getLength() < 3 && curve.getModel() != null) {
			///*
			// * remove the curve from the model
			// */
			//addEdit(new RemoveCurveFromModelEdit(curve));
			///*
			// * detach all points on the deleted curve
			// */
			//ControlPoint[] acpCurve = curve.getControlPointArray();
			//for (int c = 0; c < acpCurve.length; c++) {
			//	addEdit(new DetachControlPointEdit(acpCurve[c]));
			//	addEdit(new ChangeCPCurveEdit(acpCurve[c],null));
			//}
			
			addEdit(new RemoveCurveEdit(curve));
		}
		/*
		 * or else just remove the controlPoint
		 */
		else {
			addEdit(new RemoveControlPointFromCurveEdit(cp));
			addEdit(new RemoveControlPointFromSelectionsEdit(cp));
		}
	}
}
