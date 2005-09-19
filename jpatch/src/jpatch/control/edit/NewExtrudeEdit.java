package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;
import jpatch.boundary.selection.*;

public class NewExtrudeEdit extends CloneCommonEdit {
	
	private static int iSequenceNumber = 1;
	
	public NewExtrudeEdit(ControlPoint[] controlPointsToClone) {
		super(controlPointsToClone);
		buildCloneMap(false);
		cloneControlPoints();
		cloneCurves();
		extrude();
		PointSelection ps = createNewSelection();
		if (ps.getSize() > 0) {
			ps.setName("*extruded points #" + iSequenceNumber++);
			addEdit(new AtomicChangeSelection(ps));
			addEdit(new AtomicAddSelection(ps));
		}
	}
	
	private void extrude() {
		for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			ControlPoint cpClone = (ControlPoint) it.next();
			ControlPoint cpOriginal = getOriginal(cpClone);
			if (cpClone.isHead() && !cpClone.isHook() && (cpClone.getPrev() != null || cpClone.getNext() != null)) {
				ControlPoint cpNew = new ControlPoint();
				cpNew.attachTo(cpClone);
				
				/* find a good loose end */
				ControlPoint cpEnd = null;
				if (!cpOriginal.isSingle()) {
					ControlPoint[] acpStack = cpOriginal.getStack();
					loop:
					for (int i = 0; i < acpStack.length; i++) {
						if (acpStack[i].getPrev() == null && acpStack[i].getNext() != null && getClone(acpStack[i].getNext()) == null) {
							cpEnd = acpStack[i];
							break loop;
						} else if (acpStack[i].getNext() == null && acpStack[i].getPrev() != null && getClone(acpStack[i].getPrev()) == null) {
							cpEnd = acpStack[i];
							break loop;
						}
					}
				}
				
				if (cpEnd == null) {
					/* if not create a new cp, */
					cpEnd = new ControlPoint();
					
					/* attach it to the cpToClone */
					addEdit(new AtomicAttachControlPoints(cpEnd,cpOriginal.getTail()));
					
					/* and add it as a new curve */
					cpNew.appendTo(cpEnd);
					Curve curve = new Curve(cpEnd);
					curve.validate();
					addEdit(new AtomicAddCurve(curve));
				} else {
					if (cpEnd.isEnd()) {
						addEdit(new ChangeControlPointNextEdit(cpEnd,cpNew));
						cpNew.setPrev(cpEnd);
						cpNew.setCurve(cpEnd.getCurve());
					} else if (cpEnd.isStart()) {
						addEdit(new ChangeControlPointPrevEdit(cpEnd,cpNew));
						cpNew.setNext(cpEnd);
						cpNew.setCurve(cpEnd.getCurve());
						addEdit(new AtomicChangeCurveStart(cpEnd.getCurve(),cpNew));
					} else {
						System.out.println("error in extrudeEdit");
					}
				}
			}
		}
	}
}

