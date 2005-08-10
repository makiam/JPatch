package jpatch.control.edit;

import jpatch.entity.*;

public class ChangeControlPointNextEdit extends JPatchAbstractUndoableEdit {
	
	private ControlPoint cp;
	private ControlPoint cpNext;
	
	public ChangeControlPointNextEdit(ControlPoint cp, ControlPoint cpNext) {
		this.cp = cp;
		this.cpNext = cpNext;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		ControlPoint dummy = cpNext;
		cpNext = cp.getNext();
		cp.setNext(dummy);
	}
}
