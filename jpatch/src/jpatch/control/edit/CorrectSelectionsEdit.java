package jpatch.control.edit;

import java.util.*;

import jpatch.entity.*;
import jpatch.boundary.*;

public class CorrectSelectionsEdit extends JPatchCompoundEdit {
	private CorrectSelectionsEdit() { }
	
	public static CorrectSelectionsEdit detachPoint(ControlPoint cp) {
//		CorrectSelectionsEdit edit = new CorrectSelectionsEdit();
//		List selections = cp.getCurve().getModel().getSelectionsContaining(cp);
//		if (cp.getNextAttached() != null) {
//			ArrayList listCp = new ArrayList();
//			listCp.add(cp);
//			for (Iterator it = selections.iterator(); it.hasNext(); ) {
//				PointSelection ps = (PointSelection) it.next();
//				edit.addEdit(new AddControlPointsToSelectionEdit(ps, listCp));
//			}
//		} else if (cp.getPrevAttached() != null) {
//			ArrayList listCp = new ArrayList();
//			listCp.add(cp.getPrevAttached());
//			for (Iterator it = selections.iterator(); it.hasNext(); ) {
//				PointSelection ps = (PointSelection) it.next();
//				edit.addEdit(new AddControlPointsToSelectionEdit(ps, listCp));
//			}
//		}
//		edit.addEdit(new FullyDetachControlPointEdit(cp));
//		return edit;
		CorrectSelectionsEdit edit = new CorrectSelectionsEdit();
		ControlPoint head = cp.getHead();
		for (Iterator it = MainFrame.getInstance().getSelectionsContaining(head).iterator(); it.hasNext(); ) {
			NewSelection selection = (NewSelection) it.next();
			Float weight = (Float) selection.getMap().get(head);
			if (cp.getNextAttached() != null) {
				HashMap map = new HashMap();
				map.put(cp, weight);
				edit.addEdit(new AddControlPointsToSelectionEdit(selection, map));
			}
		}
		edit.addEdit(new FullyDetachControlPointEdit(cp));
		return edit;
	}
	
	public static CorrectSelectionsEdit attachPoints(ControlPoint cpA, ControlPoint cpB) {
//		//System.out.println("*");
//		CorrectSelectionsEdit edit = new CorrectSelectionsEdit();
//		List selections = cpA.getCurve().getModel().getSelectionsContaining(cpA);
//		ArrayList listA = new ArrayList();
//		listA.add(cpA);
//		ArrayList listB = new ArrayList();
//		listB.add(cpB);
//		edit.addEdit(new AttachControlPointsEdit(cpA, cpB));
//		for (Iterator it = selections.iterator(); it.hasNext(); ) {
//			PointSelection ps = (PointSelection) it.next();
//			//System.out.println(ps.getName());
//			edit.addEdit(new RemoveControlPointsFromSelectionEdit(ps, listA));
//			edit.addEdit(new AddControlPointsToSelectionEdit(ps, listB));
//		}
//		return edit;
		CorrectSelectionsEdit edit = new CorrectSelectionsEdit();
		for (Iterator it = MainFrame.getInstance().getSelectionsContaining(cpA).iterator(); it.hasNext(); ) {
			NewSelection selection = (NewSelection) it.next();
			Float weight = (Float) selection.getMap().get(cpA);
			HashMap map = new HashMap();
			map.put(cpA, weight);
			edit.addEdit(new RemoveControlPointsFromSelectionEdit(selection, map));
			map = new HashMap();
			map.put(cpB, weight);
			edit.addEdit(new AddControlPointsToSelectionEdit(selection, map));
		}
		edit.addEdit(new AtomicAttachControlPoints(cpA, cpB));
		return edit;
	}
}

