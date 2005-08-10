package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  Undoable AppendControlPointEdit
 *
 * @author     aledinsk
 * @created    08. Juni 2003
 */
public class InsertControlPointEdit extends JPatchAbstractUndoableEdit {

	private Curve curve;
	private ControlPoint cpA;
	private ControlPoint cpAprev;
	private ControlPoint cpB;
	private ControlPoint cpBnext;
	
	/**
	 *This Edit will append ControlPoint A to ControlPoint B
	 */
	
	public InsertControlPointEdit(ControlPoint A, ControlPoint B) {
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
		if (cpB.getNext() != null) {
			cpB.getNext().setPrev(cpA);
			cpA.setNext(cpB.getNext());
		}
		cpA.setPrev(cpB);
		cpB.setNext(cpA);
		cpA.setCurve(cpB.getCurve());
	}


	/**
	 *  undoes the operation
	 */
	public void undo() {
		if (cpA.getNext() != null) {
			cpA.getNext().setPrev(cpB);
			cpA.setNext(null);
		}
		cpA.setPrev(cpAprev);
		cpB.setNext(cpBnext);
		cpA.setCurve(curve);
	}
}


