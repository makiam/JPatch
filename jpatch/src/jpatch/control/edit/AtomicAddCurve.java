package jpatch.control.edit;

import jpatch.entity.*;

public class AtomicAddCurve extends JPatchAtomicEdit {
	
	private Model model;
	private Curve curve;
	
	public AtomicAddCurve(Curve curve, Model model) {
		this.curve = curve;
		this.model = model;
		redo();
	}
	
	public void undo() {
		model.removeCurve(curve);
	}
	
	public void redo() {
		model.addCurve(curve);
		curve.validate();
	}
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}

			
