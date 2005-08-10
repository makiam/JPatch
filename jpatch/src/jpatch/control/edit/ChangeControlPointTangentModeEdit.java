package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  Changes the bLoop flag of a ControlPoint
 */
public class ChangeControlPointTangentModeEdit extends JPatchAbstractUndoableEdit {

	private ControlPoint cp;
	private int iMode;
	
	/**
	* @param cp The ControlPoint to change
	* @param mode The new value for mode
	**/
	public ChangeControlPointTangentModeEdit(ControlPoint cp, int mode) {
		this.cp = cp;
		iMode = mode;
		swap();
	}


	/**
	 *  redoes the operation
	 */
	public void redo() {
		swap();
	}


	/**
	 *  undoes the operation
	 */
	public void undo() {
		swap();
	}
	
	private void swap() {
		int dummy = iMode;
		iMode = cp.getMode();
		cp.setMode(dummy);
		cp.setTangentsValid(false);
	}
}


