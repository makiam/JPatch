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
	
	/**
	 *Constructor for the RemoveControlPointFromCurveEdit object
	 *The controlPoint will be removed immediately
	 *
	 * @param  cp  ControlPoint to remove
	 */
	public AtomicRemoveControlPointFromCurve(ControlPoint cp) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + cp + ")");
		/*
		 *  store ControlPoint, Curve, next and previous ControlPoints for undo
		 */
		this.cp = cp;
		cpNext = cp.getNext();
		cpPrev = cp.getPrev();
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
		if (cpNext != null) {				// if next cp exists
			cpNext.setPrev(cp);			// point it's prev to us
			if (cp.getLoop()) {
				cpNext.setLoop(false);
			}
		}
		if (cpPrev != null) {				// if prev cp exists
			cpPrev.setNext(cp);			// point it's next to us
		}
		cp.setNext(cpNext);
		cp.setPrev(cpPrev);
	}


	/**
	 *  This Method removes the ControlPoint from the curve
	 */
	private void remove() {
		if (cpNext != null) {				// if next cp exists
			cpNext.setPrev(cpPrev);			// point next cp's prev to prev cp (might be null)
			if (cp.getLoop()) {
				cpNext.setLoop(true);
			}
		}
		if (cpPrev != null) {				// if prev cp exitsis
			cpPrev.setNext(cpNext);			// point prev cp's next to next cp (might be null)
		}
	}
	
	public int sizeOf() {
		return 8 + 4 + 4 + 4;
	}
}

