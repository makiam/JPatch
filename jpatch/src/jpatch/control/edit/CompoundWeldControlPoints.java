package jpatch.control.edit;

import jpatch.entity.*;

public class CompoundWeldControlPoints extends JPatchCompoundEdit {

	public CompoundWeldControlPoints(ControlPoint cpA, ControlPoint cpB) {
		ControlPoint cpLooseEndA = cpA.getLooseEnd();
		ControlPoint cpLooseEndB = cpB.getLooseEnd();
		// do both, cpA and cpB have 
		if(cpLooseEndA == null || cpLooseEndB == null) {
			// NO
			// attach cpA's head to cpB's tail
			addEdit(new CompoundAttachControlPoints(cpA.getHead(),cpB.getTail()));
			// return
			return;
		}
		// has loose-end of cpA next-attached?
		if (cpLooseEndA.getNextAttached() != null) {
			// YES
			// attach loose-end of cpB's head to loose-end of cpA's next attached
			addEdit(new CompoundAttachControlPoints(cpLooseEndB.getHead(), cpLooseEndA.getNextAttached()));
		}
		// has loose-end of cpA prev-attached?
		if (cpLooseEndA.getPrevAttached() != null) {
			// YES
			// attach loose-end of cpA's prev-attached to loose-end of cpB's tail
			addEdit(new CompoundAttachControlPoints(cpLooseEndA.getPrevAttached(),cpLooseEndB.getTail()));
		}
		// append loose-end of cpA to loose-end of cpB
		addEdit(new CompoundAppendControlPoints(cpLooseEndA, cpLooseEndB));
	}
}
