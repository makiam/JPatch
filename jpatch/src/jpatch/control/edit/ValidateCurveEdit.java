package jpatch.control.edit;

import jpatch.entity.*;

public class ValidateCurveEdit extends JPatchCompoundEdit {
	
	public ValidateCurveEdit(Curve curve) {
		super("");
		for (ControlPoint cp = curve.getStart(); cp != null; cp = cp.getNextCheckNextLoop()) {
			if (cp.getCurve() != curve) {
				addEdit(new ChangeCPCurveEdit(cp,curve));
			}
		}
	}
}
