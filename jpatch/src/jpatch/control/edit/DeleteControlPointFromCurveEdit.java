package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  This edit deletes a single control point and thus splits or opens the curve
 *
 * @author     Sascha Ledinsky
 * @created    26. Dezember 2003
 */
public class DeleteControlPointFromCurveEdit extends JPatchAbstractUndoableEdit {
	private ControlPoint cp;
	private ControlPoint cpNext;
	private ControlPoint cpPrev;
	private Curve curve;

	/**
	 * Constructor
	 * The controlPoint will be deleted immediately
	 *
	 * @param  cp  ControlPoint to remove
	 */
	public DeleteControlPointFromCurveEdit(ControlPoint cp) {
		/*
		 *  store ControlPoint, Curve, next and previous ControlPoints for undo
		 */
		//System.out.println("\t\tdeleteControlPointFromCurve " + cp);
		this.cp = cp;
		curve = cp.getCurve();
		cpNext = cp.getNext();
		cpPrev = cp.getPrev();
		delete();					// remove the ControlPoint
	}


	/**
	 *  redoes the edit
	 */
	public void redo() {
		delete();
	}


	/**
	 *  undoes the edit
	 */
	public void undo() {
		undelete();
	}


	/**
	 *  This method inserts the ControlPoint to the curve again
	 */
	private void undelete() {
		cp.setCurve(curve);				// set curve
		if (cpNext != null) {				// if next cp exists
			cpNext.setPrev(cp);			// point it's prev to us
		}
		if (cpPrev != null) {				// if prev cp exists
			cpPrev.setNext(cp);			// point it's next to us
		}
	}


	/**
	 *  This Method deletes the ControlPoint from the curve
	 */
	private void delete() {
		//System.out.println("delete " + cp);
		cp.setCurve(null);
		if (cpPrev != null) {
			cpPrev.setNext(null);
		}
		if (cpNext != null) {
			cpNext.setPrev(null);
		}
	}
}
