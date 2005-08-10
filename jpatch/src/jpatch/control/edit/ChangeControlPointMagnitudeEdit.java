package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  Changes the bLoop flag of a ControlPoint
 */
public class ChangeControlPointMagnitudeEdit extends JPatchAbstractUndoableEdit {

	private ControlPoint cp;
	private float fMagnitude;
	
	/**
	* @param cp The ControlPoint to change
	* @param loop The new value for bLoop
	**/
	public ChangeControlPointMagnitudeEdit(ControlPoint cp, float magnitude) {
		this.cp = cp;
		fMagnitude = magnitude;
		//swap();
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
		float dummy = fMagnitude;
		fMagnitude = cp.getInMagnitude();
		cp.setMagnitude(dummy);
		cp.invalidateTangents();
	}
}


