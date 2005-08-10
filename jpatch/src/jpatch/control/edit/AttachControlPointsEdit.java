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
public class AttachControlPointsEdit extends JPatchAbstractUndoableEdit {

	private ControlPoint cpA;
	private ControlPoint cpAnextAttached;
	private ControlPoint cpB;
	private ControlPoint cpBprevAttached;


	/**
	* Creates and performes the edit
	* @param  A  ControlPoint A
	* @param  B  ControlPoint B
	*/
	public AttachControlPointsEdit(ControlPoint A, ControlPoint B) {
		cpA = A;
		cpB = B;
		cpAnextAttached = cpA.getNextAttached();
		cpBprevAttached = cpB.getPrevAttached();
		redo();
	}


	/**
	 *  redoes the operation
	 */
	public void redo() {
		cpA.setNextAttached(cpB);
		cpB.setPrevAttached(cpA);
	}


	/**
	 *  undoes the operation
	 */
	public void undo() {
		cpA.setNextAttached(cpAnextAttached);
		cpB.setPrevAttached(cpBprevAttached);
	}
}


