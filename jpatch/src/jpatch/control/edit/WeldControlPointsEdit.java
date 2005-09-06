package jpatch.control.edit;

import jpatch.entity.*;

public class WeldControlPointsEdit extends JPatchCompoundEdit {

	public WeldControlPointsEdit(ControlPoint cpA, ControlPoint cpB) {
		super("weld");
		ControlPoint cpLooseEndA = cpA.getLooseEnd();
		ControlPoint cpLooseEndB = cpB.getLooseEnd();
		if(cpLooseEndA != null && cpLooseEndB != null) {
			
			if (cpLooseEndA.getNextAttached() != null) {
				addEdit(CorrectSelectionsEdit.attachPoints(cpLooseEndB.getHead(),cpLooseEndA.getNextAttached()));
			}
			if (cpLooseEndA.getPrevAttached() != null) {
				addEdit(CorrectSelectionsEdit.attachPoints(cpLooseEndA.getPrevAttached(),cpLooseEndB.getTail()));
			}
			addEdit(new CompoundAppendControlPoints(cpLooseEndA, cpLooseEndB));
		} else {
			addEdit(CorrectSelectionsEdit.attachPoints(cpA.getHead(),cpB.getTail()));
		}
	}
}
