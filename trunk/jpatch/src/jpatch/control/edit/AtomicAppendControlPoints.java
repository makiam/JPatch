package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  Undoable AppendControlPointEdit
 *
 * @author     aledinsk
 * @created    08. Juni 2003
 */
public final class AtomicAppendControlPoints extends JPatchAtomicEdit {
	private OLDControlPoint cpA;
	private OLDControlPoint cpAprev;
	private OLDControlPoint cpB;
	private OLDControlPoint cpBnext;
	
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
	public AtomicAppendControlPoints(OLDControlPoint A, OLDControlPoint B) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + A + ", " + B + ")");
		cpA = A;
		cpB = B;
		cpAprev = cpA.getPrev();
		cpBnext = cpB.getNext();
		redo();
	}
 
	public void redo() {
		cpA.setPrev(cpB);
		cpB.setNext(cpA);
//		invalidateTangents();
	}

	public void undo() {
		cpA.setPrev(cpAprev);
		cpB.setNext(cpBnext);
//		invalidateTangents();
	}
	
	public int sizeOf() {
		return 8 + 4 + 4 + 4 + 4 + 4;
	}
	
//	private void invalidateTangents() {
//		if (cpB.getPrev() != null)
//			cpB.getPrev().invalidateTangents();
//		cpB.invalidateTangents();
//		cpA.invalidateTangents();
//		if (cpA.getNext() != null)
//			cpA.getNext().invalidateTangents();
//	}
}


