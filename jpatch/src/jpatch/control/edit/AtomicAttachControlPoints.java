package jpatch.control.edit;

import jpatch.entity.*;

/**
* Undoable AttachControlPointEdit
*
* This Edit attaches ControlPoint A to ControlPoint B
*
* After the edit the fields will look like this:
*
* cp	cpNextAttached	cpPrevAttached
*  A		B	   (unchanged)
*  B	  (unchanged)		A
*
**/
public class AtomicAttachControlPoints extends JPatchAtomicEdit {

	private ControlPoint cpA;
	private ControlPoint cpAnextAttached;
	private ControlPoint cpB;
	private ControlPoint cpBprevAttached;


	/**
	* Creates and performes the edit
	* @param  A  ControlPoint A
	* @param  B  ControlPoint B
	*/
	public AtomicAttachControlPoints(ControlPoint A, ControlPoint B) {
		if (DEBUG)
			System.out.println(getClass().getName() + "(" + A + ", " + B + ")");
		cpA = A;
		cpB = B;
		cpAnextAttached = cpA.getNextAttached();
		cpBprevAttached = cpB.getPrevAttached();
		redo();
	}

	public void redo() {
		cpA.setNextAttached(cpB);
		cpB.setPrevAttached(cpA);
	}

	public void undo() {
		cpA.setNextAttached(cpAnextAttached);
		cpB.setPrevAttached(cpBprevAttached);
	}
	
	public int sizeOf() {
		return 8 + 4 + 4 + 4 + 4;
	}
}


