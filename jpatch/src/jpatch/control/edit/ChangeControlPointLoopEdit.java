package jpatch.control.edit;

import jpatch.entity.*;

/**
 *  Changes the bLoop flag of a ControlPoint
 */
public class ChangeControlPointLoopEdit extends JPatchAbstractUndoableEdit {

	private ControlPoint cp;
	private boolean bLoop;
	
	/**
	* @param cp The ControlPoint to change
	* @param loop The new value for bLoop
	**/
	public ChangeControlPointLoopEdit(ControlPoint cp, boolean loop) {
		this.cp = cp;
		bLoop = loop;
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
		boolean dummy = bLoop;
		bLoop = cp.getLoop();
		cp.setLoop(dummy);
	}
}


