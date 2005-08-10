package jpatch.control.edit;

import jpatch.entity.*;
import jpatch.boundary.*;

public class CreateCurveEdit extends JPatchAbstractUndoableEdit {
	
	private Curve curve;
	
	public CreateCurveEdit(Curve curve) {
		this.curve = curve;
		MainFrame.getInstance().getModel().addCurve(curve);
	}
	
	public String getPresentationName() {
		return "create curve";
	}
	
	public void undo() {
		curve.remove();
	}
	
	public void redo() {
		MainFrame.getInstance().getModel().addCurve(curve);
	}
}

			
