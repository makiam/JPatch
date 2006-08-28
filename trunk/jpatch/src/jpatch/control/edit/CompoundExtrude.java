package jpatch.control.edit;

import java.util.*;
import jpatch.entity.*;
import jpatch.boundary.*;

public class CompoundExtrude extends AbstractClone implements JPatchRootEdit {
	
	private static int iSequenceNumber = 1;
	
	public CompoundExtrude(OLDControlPoint[] controlPointsToClone) {
		super(controlPointsToClone);
		buildCloneMap(false);
		cloneControlPoints();
		cloneCurves();
		extrude();
		OLDSelection selection = createNewSelection();
		if (selection.getMap().size() > 0) {
			selection.setName("*extruded points #" + iSequenceNumber++);
			addEdit(new AtomicChangeSelection(selection.cloneSelection()));
			addEdit(new AtomicAddSelection(selection));
		}
	}
	
	private void extrude() {
		for (Iterator it = mapOriginals.keySet().iterator(); it.hasNext(); ) {
			OLDControlPoint cpClone = (OLDControlPoint) it.next();
			OLDControlPoint cpOriginal = getOriginal(cpClone);
			if (cpClone.isHead() && !cpClone.isHook() && (cpClone.getPrev() != null || cpClone.getNext() != null)) {
				OLDControlPoint cpNew = new OLDControlPoint();
				cpNew.attachTo(cpClone);
				
				/* find a good loose end */
				OLDControlPoint cpEnd = null;
				if (!cpOriginal.isSingle()) {
					OLDControlPoint[] acpStack = cpOriginal.getStack();
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
					cpEnd = new OLDControlPoint();
					
					/* attach it to the cpToClone */
					addEdit(new AtomicAttachControlPoints(cpEnd,cpOriginal.getTail()));
					
					/* and add it as a new curve */
					cpNew.appendTo(cpEnd);
//					Curve curve = new Curve(cpEnd);
//					curve.validate();
					addEdit(new AtomicAddCurve(cpEnd));
				} else {
					if (cpEnd.isEnd()) {
						addEdit(new AtomicChangeControlPoint.Next(cpEnd,cpNew));
						cpNew.setPrev(cpEnd);
//						cpNew.setCurve(cpEnd.getCurve());
					} else if (cpEnd.isStart()) {
						addEdit(new AtomicChangeControlPoint.Prev(cpEnd,cpNew));
						cpNew.setNext(cpEnd);
//						cpNew.setCurve(cpEnd.getCurve());
						addEdit(new AtomicRemoveCurve(cpEnd));
						addEdit(new AtomicAddCurve(cpNew));
//						addEdit(new AtomicChangeCurveStart(cpEnd.getCurve(),cpNew));
					} else {
						System.out.println("error in extrudeEdit");
					}
				}
			}
		}
	}
	
	public String getName() {
		return "extrude";
	}
}

