package jpatch.control.edit;

import jpatch.entity.*;

public class ChangeCurveStartEdit extends JPatchAbstractUndoableEdit {
	
	private Curve curve;
	private ControlPoint cpStart;

	public ChangeCurveStartEdit(Curve curve, ControlPoint start) {
		this.curve = curve;
		cpStart = start;
		swap();
		//System.out.println("ChangeCurveStart " + curve.hashCode() + " newstart = " + curve.getStart().number());
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		ControlPoint cp = cpStart;
		cpStart = curve.getStart();
		curve.setStart(cp);
	}
}
