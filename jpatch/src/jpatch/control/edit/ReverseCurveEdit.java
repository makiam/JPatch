package jpatch.control.edit;

import jpatch.entity.*;

public class ReverseCurveEdit extends JPatchAbstractUndoableEdit {
	
	private Curve curve;
	
	public ReverseCurveEdit(Curve curve) {
		this.curve = curve;
		curve.reverse();
	}
	
	public String name() {
		return "reverse curve";
	}
	
	public void undo() {
		curve.reverse();
	}
	
	public void redo() {
		curve.reverse();
	}
}

			
