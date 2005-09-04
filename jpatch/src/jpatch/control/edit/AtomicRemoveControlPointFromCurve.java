package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  This edit removes a single control point from a curve.
 *  It does nothing else!
 *
 * @author     Sascha Ledinsky
 * @created    26. Dezember 2003
 */
public class AtomicRemoveControlPointFromCurve extends JPatchAtomicEdit {
	private ControlPoint cp;
	private ControlPoint cpNext;
	private ControlPoint cpPrev;
	private Curve curve;
	private boolean bStart;
	
	/**
	 *Constructor for the RemoveControlPointFromCurveEdit object
	 *The controlPoint will be removed immediately
	 *
	 * @param  cp  ControlPoint to remove
	 */
	public AtomicRemoveControlPointFromCurve(ControlPoint cp) {
		/*
		 *  store ControlPoint, Curve, next and previous ControlPoints for undo
		 */
		this.cp = cp;
		curve = cp.getCurve();
		cpNext = cp.getNext();
		cpPrev = cp.getPrev();
		bStart = (curve.getStart() == cp);		// set bStart flag is we're the start of the curve
		remove();					// remove the ControlPoint
	}

	public void redo() {
		remove();
	}

	public void undo() {
		readdEdit();
	}

	/**
	 *  This method adds the ControlPoint to the curve again
	 */
	private void readdEdit() {
		cp.setCurve(curve);				// set curve
		if (cpNext != null) {				// if next cp exists
			cpNext.setPrev(cp);			// point it's prev to us
			if (cp.getLoop()) {
				cpNext.setLoop(false);
			}
		}
		if (cpPrev != null) {				// if prev cp exists
			cpPrev.setNext(cp);			// point it's next to us
		}
		if (bStart) {					// cp has been start of curve
			curve.setStart(cp);			// set start of curve to cp
		}
		cp.setNext(cpNext);
		cp.setPrev(cpPrev);
	}


	/**
	 *  This Method removes the ControlPoint from the curve
	 */
	private void remove() {
		cp.setCurve(null);				// set curve to null
		if (cpNext != null) {				// if next cp exists
			cpNext.setPrev(cpPrev);			// point next cp's prev to prev cp (might be null)
			if (cp.getLoop()) {
				cpNext.setLoop(true);
			}
		}
		if (cpPrev != null) {				// if prev cp exitsis
			cpPrev.setNext(cpNext);			// point prev cp's next to next cp (might be null)
		}
		if (bStart) {					// cp has been start of curve
			curve.setStart(cpNext);			// set start of curve to next cp (might be null)
		}
	}
}

