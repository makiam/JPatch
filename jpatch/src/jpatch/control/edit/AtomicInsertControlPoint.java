package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  This Edit will insert ControlPoint A after ControlPoint B
 *
 * @author     aledinsk
 * @created    08. Juni 2003
 */
public class AtomicInsertControlPoint extends JPatchAtomicEdit implements JPatchRootEdit {

//	private Curve curve;
	private ControlPoint cpA;
//	private ControlPoint cpAprev;
	private ControlPoint cpB;
	private ControlPoint cpBnext;
	
	/**
	 *This Edit will insert ControlPoint A after ControlPoint B
	 */
	
	public AtomicInsertControlPoint(ControlPoint A, ControlPoint B) {
		cpA = A;
		cpB = B;
//		cpAprev = cpA.getPrev();
		cpBnext = cpB.getNext();
//		curve = cpB.getCurve();
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
		if (cpB.getPrev() != null)
			cpB.getPrev().invalidateTangents();
		cpB.invalidateTangents();
		cpA.invalidateTangents();
		if (cpA.getNext() != null)
			cpA.getNext().invalidateTangents();
			
	}


	/**
	 *  undoes the operation
	 */
	public void undo() {
		if (cpA.getNext() != null) {
			cpA.getNext().setPrev(cpB);
			cpA.setNext(null);
		}
//		cpA.setPrev(cpAprev);
		cpB.setNext(cpBnext);
//		cpA.setCurve(curve);
		if (cpB.getPrev() != null)
			cpB.getPrev().invalidateTangents();
		cpB.invalidateTangents();
		if (cpBnext != null)
			cpBnext.invalidateTangents();
	}
	
	public String getName() {
		return "insert controlpoint";
	}
	
	public int sizeOf() {
		return 8 + 4 + 4 + 4;
	}
}


