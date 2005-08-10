package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.*;

public class RemoveCurveFromModelEdit extends JPatchAbstractUndoableEdit {
	
	private Curve curve;
	
	public RemoveCurveFromModelEdit(Curve curve) {
		//System.out.println("RemoveCurveFromModel(" + curve.hashCode() + ")");
		this.curve = curve;
		curve.remove();
	}
	
	public String name() {
		return "remove curve";
	}
	
	public void undo() {
		curve.validate();
		MainFrame.getInstance().getModel().addCurve(curve);
	}
	
	public void redo() {
		curve.remove();
	}
}
