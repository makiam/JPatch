package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  Undoable AppendControlPointEdit
 *
 * @author     aledinsk
 * @created    08. Juni 2003
 */
public class SimpleAppendControlPointsEdit extends JPatchAbstractUndoableEdit {

	private Curve curve;
	private ControlPoint cpA;
	private ControlPoint cpAprev;
	private ControlPoint cpB;
	private ControlPoint cpBnext;
	/**
	 *This Edit will append ControlPoint A to ControlPoint B
	 *
	 * After the edit the fields will look like this:
	 *
	 * cp	  cpNext	cpPrev
	 *  A  (unchanged)         B
	 *  B	  A	      (unchanged)
	 *
	 * @param  A  ControlPoint A
	 * @param  B  ControlPoint B
	 */
	 public SimpleAppendControlPointsEdit(ControlPoint A, ControlPoint B) {
		cpA = A;
		cpB = B;
		cpAprev = cpA.getPrev();
		cpBnext = cpB.getNext();
		curve = cpB.getCurve();
		redo();
	}


	/**
	 *  redoes the operation
	 */
	public void redo() {
		//System.out.println("redo SimpleAppendControlPointsEdit(" + cpA + "," + cpB + ")");
		cpA.setPrev(cpB);
		cpB.setNext(cpA);
		cpA.setCurve(cpB.getCurve());
	}


	/**
	 *  undoes the operation
	 */
	public void undo() {
		//System.out.println("undo SimpleAppendControlPointsEdit(" + cpA + "," + cpB + ")");
		cpA.setPrev(cpAprev);
		cpB.setNext(cpBnext);
		cpA.setCurve(curve);
	}
}


