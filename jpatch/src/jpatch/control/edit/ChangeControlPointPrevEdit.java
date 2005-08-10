package jpatch.control.edit;

import jpatch.entity.*;

public class ChangeControlPointPrevEdit extends JPatchAbstractUndoableEdit {
	
	private ControlPoint cp;
	private ControlPoint cpPrev;
	
	public ChangeControlPointPrevEdit(ControlPoint cp, ControlPoint cpPrev) {
		this.cp = cp;
		this.cpPrev = cpPrev;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		ControlPoint dummy = cpPrev;
		cpPrev = cp.getPrev();
		cp.setPrev(dummy);
	}
}
