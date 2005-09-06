package jpatch.control.edit;

import jpatch.entity.*;

public class AtomicChangeCurveStart extends JPatchAtomicEdit {
	private Curve curve;
	private ControlPoint cpStart;

	public AtomicChangeCurveStart(Curve curve, ControlPoint start) {
		this.curve = curve;
		cpStart = start;
		swap();
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
	
	public int sizeOf() {
		return 8 + 4 + 4;
	}
}
