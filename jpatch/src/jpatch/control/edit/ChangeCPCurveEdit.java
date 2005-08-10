package jpatch.control.edit;

import jpatch.entity.*;

public class ChangeCPCurveEdit extends JPatchAbstractUndoableEdit {
	
	private ControlPoint cp;
	private Curve curveOld;
	
	public ChangeCPCurveEdit(ControlPoint cp, Curve curve) {
		this.cp = cp;
		curveOld = curve;
		swap();
	}
	
	public void undo() {
		swap();
	}
	
	public void redo() {
		swap();
	}
	
	private void swap() {
		Curve curveDummy = curveOld;
		curveOld = cp.getCurve();
		cp.setCurve(curveDummy);
	}
}
