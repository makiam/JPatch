package jpatch.control.edit;

import jpatch.entity.*;

public class AtomicRemoveCurve extends JPatchAtomicEdit {
	
	private Model model;
	private Curve curve;
	
	public AtomicRemoveCurve(Curve curve) {
		this.curve = curve;
		model = curve.getModel();
		model.removeCurve(curve);
	}
	
	public void undo() {
		curve.validate();
		model.addCurve(curve);
	}
	
	public void redo() {
		model.removeCurve(curve);
	}
}
