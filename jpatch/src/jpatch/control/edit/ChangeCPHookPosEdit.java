package jpatch.control.edit;

import jpatch.entity.*;

public class ChangeCPHookPosEdit extends JPatchAbstractUndoableEdit {
	
	private ControlPoint cp;
	private float fHookPos;
	
	public ChangeCPHookPosEdit(ControlPoint cp, float hookPos) {
		this.cp = cp;
		fHookPos = hookPos;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		float dummy = fHookPos;
		fHookPos = cp.getHookPos();
		cp.setHookPos(dummy);
	}
}
